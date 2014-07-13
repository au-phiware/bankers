// System includes
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

// CUDA runtime
#include <cuda_runtime.h>

#define choke(ERR, EXIT, STMT, ...)         \
{   ERR = STMT;                             \
    if (ERR != cudaSuccess) {               \
        fprintf(stderr, "Failed to ");      \
        fprintf(stderr, __VA_ARGS__);       \
        fprintf(stderr, ": %s.\n",          \
                cudaGetErrorString(ERR));   \
        if (EXIT) exit(EXIT);               \
}   }
#define debugf(...) if (debug) fprintf(stderr, __VA_ARGS__)

#ifndef SEP
#define SEP "\n"
#endif

// CUDA Capability v1.1 can only handle 32bit numbers
// with atomicAdd
// length <= sizeof(banker_t) && length <= COUNT_MAX
#ifndef length
#define length 64
#endif
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

#define sharedMemorySize (0x4000 - 0x10)
// binomial coeffiecient function
// (indexes into the binom table aka Pascal's triangle)
#define choose(N, Y) ((Y) > (N) / 2 ? binom[rowOffset(N) + (N) - (Y)] : binom[rowOffset(N) + (Y)])
// Index of the Xth row of the binom table
// Note: each row is folded
#define rowOffset(X) (((((X) + 1) * ((X) + 1)) / 4) - 1)
// Maximum number of inputs per block with the available sharedMemorySize
#define maxBlockWidth (((sharedMemorySize - sizeof(banker_t) * rowOffset(length + 1)) / length) / sizeof(count_t))
// The block height (y dimension) must be the max width of the binom table
#define blockHeight (length / 2)

// Total number of threads per block
int threads = 0;

// Debug flag
int debug = 0;

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
        unsigned int n = length;

        for (i = 1; n-- && c > 0; i <<= 1)
            if (b & i)
                --c, a += choose(length, c);
            else
                a += choose(n, c - 1);

        io[x] = a;
    }
#else
    io[x] = 0;
    count_t c_y = count[threadIdx.x][y];

    if (b & (1 << y))
        atomicAdd(io + x, choose(length, c_y - 1));
    else if (c_y < c)
        atomicAdd(io + x, choose(length - y - 1, c - c_y - 1));
#endif
}

void usage(char *argv) {
    printf("Usage: %s [--threads=count] [--skip=n] [--limit=m]\n", argv);
    exit(1);
}

/*
 * Parse command line arguments
 */
int parse(int argc, char ** argv, banker_t ** inputPtr)
{
    banker_t x;
    unsigned int i, skip = 0, limit = 0;
    char *line = NULL;
    size_t n = 0;

    for (i = 1; i < argc; i++) {
        if (strncmp("--threads=", argv[i], 10) == 0) {
            threads = atoi(&argv[i][10]);
        } else if (strncmp("--skip=", argv[i], 7) == 0) {
            skip = atoi(&argv[i][7]);
        } else if (strncmp("--limit=", argv[i], 8) == 0) {
            limit = atoi(&argv[i][8]);
        } else if (strncmp("--debug", argv[i], 7) == 0) {
            debug = 1;
        } else {
            usage(*argv);
        }
    }
    for (i = 0; (limit == 0 || i < limit + skip) && getline(&line, &n, stdin) != -1; i++) {
        x = (banker_t) strtoull(line, NULL, 10);
        if (i >= skip) {
            if (((i - skip) % 0x100) == 0) {
                *inputPtr = (banker_t *)realloc(*inputPtr, (i - skip + 0x100) * sizeof(banker_t));
                if (*inputPtr == NULL) {
                    fprintf(stderr, "Failed to (re)allocate host array!\n");
                    exit(EXIT_FAILURE);
                }
            }
            (*inputPtr)[i - skip] = x;
        }
    }
    if (line) free(line);

    return i - skip;
}

void setBestThreadSize() {
    threads = 256;
}

/*
 * Main program accepts one parameter: the number of the row
 * of Pascal's triangle to print.
 */
int main (int argc, char ** argv)
{
    cudaError_t err = cudaSuccess;
    int blocks, asize, size = argc - 1;
    banker_t *harray = NULL;
    banker_t *darray = NULL;

    // Parse argv and return the number of inputs specified by the user
    size = parse(argc, argv, &harray);
    if (harray == NULL) {
        fprintf(stderr, "Failed to allocate host array!\n");
        exit(EXIT_FAILURE);
    }

    // Find a good number of threads if none explicitly specified by the user
    if (threads < 1) setBestThreadSize();
    // The height of the block must accomodate the width of the binom table
    if (threads < blockHeight) threads = blockHeight;
    // The total number of threads per block must accommodate the count array
    if (threads > maxBlockWidth) threads = maxBlockWidth;
    // threads must be divisible by the block height
    if (threads % blockHeight != 0)
        threads = (threads / blockHeight) * blockHeight;
    // The number of blocks must cover the input size
    blocks = (size + threads - 1) / threads;
    // The aligned size (exact multiple of threads)
    asize = blocks * threads;

    choke(err, EXIT_FAILURE,
            cudaMalloc((void **)&darray, asize * sizeof(banker_t)),
            "allocate device array of %zu bytes",
            asize * sizeof(banker_t));

    choke(err, EXIT_FAILURE,
            cudaMemset(darray, 0, asize * sizeof(banker_t)),
            "initialise device array");

    choke(err, EXIT_FAILURE,
            cudaMemcpy(darray, harray, size * sizeof(banker_t), cudaMemcpyHostToDevice),
            "copy array from host to device");

    debugf("Launching %d block%s of %d by %d threads...\n",
                blocks, blocks == 1 ? "" : "s", threads / blockHeight, blockHeight);
    dim3 t (threads / blockHeight, blockHeight);
    inverse<<<blocks, t>>>(darray);
    choke(err, EXIT_FAILURE,
            cudaGetLastError(),
            "launch inverse kernel");

    choke(err, EXIT_FAILURE,
            cudaThreadSynchronize(),
            "complete inverse kernel");

    choke(err, EXIT_FAILURE,
            cudaMemcpy(harray, darray, size * sizeof(banker_t), cudaMemcpyDeviceToHost),
            "copy array from device to host");

    choke(err, EXIT_FAILURE,
            cudaFree(darray),
            "free device array");

    printf(FMT, *harray);
    for(int i = 1; i < size; i++) {
        printf(SEP FMT, harray[i]);
    }
    printf("\n");

    free(harray);

    choke(err, EXIT_FAILURE,
            cudaDeviceReset(),
            "deinitialize the device");

    return 0;
}
