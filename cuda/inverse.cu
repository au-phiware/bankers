// System includes
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

// CUDA runtime
#include <cuda_runtime.h>

#define choke(ERR, EXIT, MSG, ...)          \
{   ERR = __VA_ARGS__;                      \
    if (ERR != cudaSuccess) {               \
        fprintf(stderr,                     \
                "Failed to " MSG ": %s.\n", \
                cudaGetErrorString(ERR));   \
        if (EXIT) exit(EXIT);               \
}   }

// CUDA Capability v1.1 can only handle 32bit numbers
// with atomicAdd
// length <= sizeof(banker_t) && length <= COUNT_MAX
#define length 64

#if length > 32
// don't exceed 2^sizeof(count_t) bit length
typedef unsigned long long int banker_t;
typedef unsigned char count_t;
#define FMT "%llU"
#else
// (don't exceed 2^sizeof(count_t) bit length)
typedef unsigned int banker_t;
typedef unsigned char count_t;
#define FMT "%u"
#endif

#define SEP "\n"
#define sharedMemorySize (0x4000 - 0x10)
// binomial coeffiecient function
// (indexes into the binom table aka Pascal's triangle)
#define choose(N, Y) ((Y) > (N) / 2 ? binom[rowOffset(N) + (N) - (Y)] : binom[rowOffset(N) + (Y)])
// Index of the Xth row of the binom table
// Note: each row is folded
#define rowOffset(X) (((((X) + 1) * ((X) + 1)) / 4) - 1)
// Maximum number of inputs per block with the available sharedMemorySize
#define maxBlockWidth (((sharedMemorySize - sizeof(banker_t) * rowOffset(length + 1)) / length) / sizeof(count_t))

int threads = 256;

__global__ void inverse (banker_t* io)
{
    __shared__ count_t count[maxBlockWidth][length];
    __shared__ banker_t binom[rowOffset(length + 1)];
    int x = blockDim.x * blockIdx.x + threadIdx.x;
    int y = threadIdx.y;
    banker_t b = io[x], i = 1;

    for (int row = 1; row <= length; row++, i <<= 1) {
        int j = rowOffset(row) + y;
        __syncthreads();

        if (y == 0) {
            // Count cardinality at each bit position
            count[threadIdx.x][row - 1] = b & i ? 1 : 0;
            if (row > 1)
                count[threadIdx.x][row - 1] += count[threadIdx.x][row - 2];

            // Every binom row starts (and ends) with 1
            binom[j] = 1;
        } else if (threadIdx.x == 0 && y <= row / 2) {
            // Compute every cell of binom for row
            // cell is the sum of the two cells below it
            int k = j - (row + 1) / 2 - 1;
            binom[j]  = binom[k];
            // Center cell of every other row is double the cell in the
            // previous row (so we skip the increment)
            if (!(row % 2 == 0 && y == row / 2))
                k++;
            binom[j] += binom[k];
        }
    }

    // short-circuit for zero (zero maps to zero)
    if (b == 0) return;

    __syncthreads();
    count_t c = count[threadIdx.x][length - 1];
#if length > 32 && __CUDA_ARCH__ < 120
    if (y == 0) {
        banker_t a = 0;
        count_t *c_y = &count[threadIdx.x][y];
        for (i = 1; (b & i || *c_y < c) && y < length; y++, c_y++, i <<= 1)
            if (b & i)
                a += choose(length, *c_y - 1);
            else
                a += choose(length - y - 1, c - *c_y - 1);

        io[x] = a;
    }
#else
    io[x] = 0;
    count_t c_y = count[threadIdx.x][y];

    if (b & (1 << y))
        atomicadd(io + x, choose(length, c_y - 1));
    else if (c_y < c)
        atomicadd(io + x, choose(length - y - 1, c - c_y - 1));
#endif
}

/*
 * Parse command line arguments
 */
int parse(int argc, char ** argv, banker_t * input)
{
    banker_t x;
    int skip = 1;

    if (argc < 2)
    {
        printf("Usage: %s [-threads=count] n ...\n", argv[0]);
        exit(1);
    }

    for (int i = 1; i < argc; i++) {
        if (strncmp("-threads=", argv[i], 9) == 0) {
            skip++;
            threads = atoi(&argv[i][9]);
        } else {
            x = (banker_t) strtoull(argv[i], NULL, 10);
            input[i - skip] = x;
        }
    }

    return argc - skip;
}

void setBestThreadSize() {}

/*
 * Main program accepts one parameter: the number of the row
 * of Pascal's triangle to print.
 */
int main (int argc, char ** argv)
{
    cudaError_t err = cudaSuccess;
    int blocks, asize, size = argc - 1;
    banker_t *harray = (banker_t *)malloc(size * sizeof(banker_t));
    banker_t *darray = NULL;

    if (harray == NULL) {
        fprintf(stderr, "Failed to allocate host array!\n");
        exit(EXIT_FAILURE);
    }

    size = parse(argc, argv, harray);
    if (threads < 1) setBestThreadSize();
    if (threads < length / 2) threads = length / 2;
    threads /= length;
    if (threads > maxBlockWidth) threads = maxBlockWidth;
    blocks = (size + threads - 1) / threads;
    asize = blocks * size;

    choke(err, EXIT_FAILURE, "allocate device array",
            cudaMalloc((void **)&darray, asize * sizeof(banker_t)));

    choke(err, EXIT_FAILURE, "initialise device array",
            cudaMemset(darray, 0, asize * sizeof(banker_t)));

    choke(err, EXIT_FAILURE, "copy array from host to device",
            cudaMemcpy(darray, harray, size * sizeof(banker_t), cudaMemcpyHostToDevice));

    dim3 t (threads, length);
    inverse<<<blocks, t>>>(darray);
    choke(err, EXIT_FAILURE, "launch inverse kernel",
            cudaGetLastError());

    choke(err, EXIT_FAILURE, "complete inverse kernel",
            cudaThreadSynchronize());

    choke(err, EXIT_FAILURE, "copy array from device to host",
            cudaMemcpy(harray, darray, size * sizeof(banker_t), cudaMemcpyDeviceToHost));

    choke(err, EXIT_FAILURE, "free device array",
            cudaFree(darray));

    printf(FMT, *harray);
    for(int i = 1; i < size; i++) {
        printf(SEP FMT, harray[i]);
    }
    printf("\n");
    
    free(harray);

    choke(err, EXIT_FAILURE, "deinitialize the device",
            cudaDeviceReset());

    return 0;
}
