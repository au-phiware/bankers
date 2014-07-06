// System includes
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

// CUDA runtime
#include <cuda_runtime.h>

// Helper functions and utilities to work with CUDA
//#include <helper_functions.h>

#define rowOffset(X) ((((X) - 1) * ((X) - 1)) / 4)

__global__ void binom(unsigned long *table, const int n)
{
    __shared__ unsigned long cache[0x100];
    int col = blockDim.x * blockIdx.x + threadIdx.x;

    for (int row = 2; row <= n; row++) {
        int i = rowOffset(row) + col;
        __syncthreads();
        if (col == 0) {
            table[i] = cache[i & 0xFF] = row;
        } else if (col < row / 2) {
            int j = rowOffset(row - 1) + col - 1;
            cache[i & 0xFF]  = cache[j & 0xFF];
            if (!(row % 2 == 0 && col == row / 2 - 1))
                j++;
            cache[i & 0xFF] += cache[j & 0xFF];
            table[i] = cache[i & 0xFF];
        }
    }
}

/*
 * Maximum number of rows (specified in program args).
 */
static unsigned int length;

/*
 * Parse command line arguments
 */
unsigned int parse(int argc, char ** argv)
{
    unsigned int i;
    if (argc != 2)
    {
        printf("Usage: %s n\n", argv[0]);
        exit(1);
    }
    i = atoi(argv[1]);
    if (i > 67) {
        fprintf(stderr, "Warning: %U is too big, results will be affected by integer overflow.");
    }
    return i;
}

/*
 * Main program accepts one parameter: the number of the row
 * of Pascal's triangle to print.
 */
int main (int argc, char ** argv)
{
    length = parse(argc, argv);

    cudaError_t err = cudaSuccess;
    size_t size = rowOffset(length + 1) * sizeof(unsigned long);
    unsigned long *table = (unsigned long *)malloc(size);
    unsigned long *d_table = NULL;
    err = cudaMalloc((void **)&d_table, size);

    if (table == NULL) {
        fprintf(stderr, "Failed to allocate host table!\n");
        exit(EXIT_FAILURE);
    }
    if (err != cudaSuccess) {
        fprintf(stderr, "Failed to allocate device table: %s\n", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    binom<<<1, length + 1>>>(d_table, length);
    err = cudaGetLastError();

    if (err != cudaSuccess)
    {
        fprintf(stderr, "Failed to launch binom kernel: %s\n", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    err = cudaMemcpy(table, d_table, size, cudaMemcpyDeviceToHost);

    if (err != cudaSuccess)
    {
        fprintf(stderr, "Failed to copy table from device to host: %s\n", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    err = cudaFree(d_table);

    if (err != cudaSuccess)
    {
        fprintf(stderr, "Failed to free device table: %s\n", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    err = cudaDeviceReset();

    if (err != cudaSuccess)
    {
        fprintf(stderr, "Failed to deinitialize the device: %s\n", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    unsigned int i = rowOffset(length);
    printf("1 %lU", *table);
    for (++i; i < rowOffset(length + 1); i++) {
        printf("%s%lU", table[i]);
    }
    if (length % 2 == 0) i--;
    for (--i; i >= rowOffset(length); i--) {
        printf(" %lU", table[i]);
    }
    printf(" 1\n");

    free(table);
    return 0;
}
