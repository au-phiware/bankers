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
#define ulsize(S) ((S) * sizeof(unsigned long))

#define SEP "\n"
#define length 64
#define setBit(B) (B |= (1ul << (length - 1)))
#define rowOffset(X) (((X) * ((X) + 1)) / 2 - 1)
#define choose(N, C) binom[rowOffset(N) + (C)]

int threads = 256;
__constant__ unsigned long binom[rowOffset(length + 1)];

__global__ void compute (unsigned long* a)
{
    int i = blockDim.x * blockIdx.x + threadIdx.x;
    if (a[i] == 0) return;

    unsigned long b = 0;
    unsigned int c = 1, n = length, j = rowOffset(length);
    unsigned long e = a[i] - 1;

    while (binom[j + 1] <= e) {
        ++j, ++c;
        e -= binom[j];
    }

    do {
        j -= n;
        if (e == 0 || binom[j] > e)
            --j, --c, setBit(b);
        else
            e -= binom[j];
    } while (--n && c && ((b >>= 1) || 1));
    b >>= n;

    a[i] = b;
}

__global__ void binom_kernel(unsigned long *table, const int n)
{
    __shared__ unsigned long cache[0x100];
    int col = blockDim.x * blockIdx.x + threadIdx.x;

    for (int row = 1; row <= n; row++) {
        int i = rowOffset(row) + col;
        __syncthreads();
        if (col == 0 || col == row) {
            table[i] = cache[i & 0xff] = 1;
        } else if (col < row) {
            int j = i - row;
            table[i] = cache[i & 0xff] = cache[j & 0xff] + cache[(j - 1) & 0xff];
        }
    }
}
void initBinomTable()
{
    cudaError_t err = cudaSuccess;
    size_t size = ulsize(rowOffset(length + 1));
    unsigned long *hbinom = (unsigned long *)malloc(size);
    unsigned long *dbinom = NULL;

    if (hbinom == NULL) {
        fprintf(stderr, "Failed to allocate host binom table!\n");
        exit(EXIT_FAILURE);
    }

    choke(err, EXIT_FAILURE, "allocate device binom table",
            cudaMalloc((void **)&dbinom, size));

    binom_kernel<<<1, length + 1>>>(dbinom, length);
    choke(err, EXIT_FAILURE, "launch binom kernel",
            cudaGetLastError());

    choke(err, EXIT_FAILURE, "copy binom table from device to host",
            cudaMemcpy(hbinom, dbinom, size, cudaMemcpyDeviceToHost));

    choke(err, EXIT_FAILURE, "free device binom table",
            cudaFree(dbinom));

    choke(err, EXIT_FAILURE, "copy binom table from host to device (constant memory)",
            cudaMemcpyToSymbol(binom, hbinom, size));

    free(hbinom);
}

/*
 * Parse command line arguments
 */
int parse(int argc, char ** argv, unsigned long * input)
{
    unsigned long x;
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
            x = strtoul(argv[i], NULL, 10);
            input[i - skip] = x;
        }
    }

    return argc - skip;
}

/*
 * Main program accepts one parameter: the number of the row
 * of Pascal's triangle to print.
 */
int main (int argc, char ** argv)
{
    cudaError_t err = cudaSuccess;
    int blocks, asize, size = argc - 1;
    unsigned long *harray = (unsigned long *)malloc(ulsize(size));
    unsigned long *darray = NULL;

    initBinomTable();

    if (harray == NULL) {
        fprintf(stderr, "Failed to allocate host array!\n");
        exit(EXIT_FAILURE);
    }

    size = parse(argc, argv, harray);
    blocks = (size + threads - 1) / threads;
    asize = blocks * size;

    choke(err, EXIT_FAILURE, "allocate device array",
            cudaMalloc((void **)&darray, ulsize(asize)));

    choke(err, EXIT_FAILURE, "initialise device array",
            cudaMemset(darray, 0, ulsize(asize)));

    choke(err, EXIT_FAILURE, "copy array from host to device",
            cudaMemcpy(darray, harray, ulsize(size), cudaMemcpyHostToDevice));

    compute<<<blocks, threads>>>(darray);
    choke(err, EXIT_FAILURE, "launch compute kernel",
            cudaGetLastError());

    choke(err, EXIT_FAILURE, "copy array from device to host",
            cudaMemcpy(harray, darray, ulsize(size), cudaMemcpyDeviceToHost));

    choke(err, EXIT_FAILURE, "free device array",
            cudaFree(darray));

    printf("%lU", *harray);
    for(int i = 1; i < size; i++) {
        printf(SEP "%lU", harray[i]);
    }
    printf("\n");
    
    free(harray);

    choke(err, EXIT_FAILURE, "deinitialize the device",
            cudaDeviceReset());

    return 0;
}
