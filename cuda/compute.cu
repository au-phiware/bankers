// System includes
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

// CUDA runtime
#include <cuda_runtime.h>

#define choke(ERR, EXIT, STMT, ...)         \
{   ERR = STMT;                             \
    if (ERR != cudaSuccess) {               \
        fprintf(stderr, "compute: ");       \
        fprintf(stderr, "Failed to ");      \
        fprintf(stderr, __VA_ARGS__);       \
        fprintf(stderr, ": %s.\n",          \
                cudaGetErrorString(ERR));   \
        if (EXIT) exit(EXIT);               \
}   }
#define debugf(...) if (debug) fprintf(stderr, "compute: " __VA_ARGS__)

// Debug flag
int debug = 0;
// The current device
int device = 0;
// Device properties if needed
cudaDeviceProp *deviceProp = NULL;

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
#define setBit(B) (B |= (1ull << (length - 1)))
#else
// (don't exceed 2^sizeof(count_t) bit length)
typedef unsigned int banker_t;
typedef unsigned char count_t;
#define FMT "%u"
#define setBit(B) (B |= (1 << (length - 1)))
#endif

// binomial coeffiecient function
// (indexes into the binom table aka Pascal's triangle)
#define choose(N, C) binom[rowOffset(N) + (C)]
// Index of the Xth row of the binom table
#define rowOffset(X) (((X) * ((X) + 1)) / 2 - 1)

// Total number of threads per block
int threads = 0;
// Maximum size that can be allocated or processed on device
int maxInputArraySize = 0;

__constant__ banker_t binom[rowOffset(length + 1)];

__global__ void compute (banker_t* a)
{
    int i = blockDim.x * blockIdx.x + threadIdx.x;
    if (a[i] == 0) return;

    banker_t b = 0;
    unsigned int c = 1, n = length, j = rowOffset(length);
    banker_t e = a[i] - 1;

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

__global__ void binom_kernel(banker_t *table, const int n)
{
    __shared__ banker_t cache[0x100];
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
    size_t size = (rowOffset(length + 1)) * sizeof(banker_t);
    banker_t *hbinom = (banker_t *)malloc(size);
    banker_t *dbinom = NULL;

    if (hbinom == NULL) {
        fprintf(stderr, "Failed to allocate host binom table!\n");
        exit(EXIT_FAILURE);
    }

    choke(err, 0,
            cudaMalloc((void **)&dbinom, size),
            "allocate device binom table");
    if (err != cudaSuccess) {
        size_t memFree, memTot;
        choke(err, EXIT_FAILURE,
                cudaMemGetInfo(&memFree, &memTot),
                "get device memory information");
        debugf("Available device memory is %zu bytes.\n", memFree);
        exit(EXIT_FAILURE);
    }

    debugf("Launching a block of %d threads...\n", length + 1);
    binom_kernel<<<1, length + 1>>>(dbinom, length);
    choke(err, EXIT_FAILURE,
            cudaGetLastError(),
            "launch binom kernel");

    choke(err, EXIT_FAILURE,
            cudaMemcpy(hbinom, dbinom, size, cudaMemcpyDeviceToHost),
            "copy binom table from device to host");

    choke(err, EXIT_FAILURE,
            cudaFree(dbinom),
            "free device binom table");

    choke(err, EXIT_FAILURE,
            cudaMemcpyToSymbol(binom, hbinom, size),
            "copy binom table from host to device (constant memory)");

    free(hbinom);
}

void setDevice() {
    cudaError_t err = cudaSuccess;
    if (device == 0) {
        choke(err, EXIT_FAILURE,
                cudaGetDevice(&device),
                "get current device");
    } else {
        int d;
        choke(err, EXIT_FAILURE,
                cudaGetDevice(&d),
                "get current device");
        if (d != device)
            choke(err, EXIT_FAILURE,
                    cudaSetDevice(device),
                    "set current device to %d", device);
    }
}

void getDeviceProperties() {
    cudaError_t err = cudaSuccess;
    if (deviceProp == NULL) {
        deviceProp = (cudaDeviceProp *)malloc(sizeof(cudaDeviceProp));
        choke(err, EXIT_FAILURE,
                cudaGetDeviceProperties(deviceProp, device),
                "get device (%d) properties", device);
    }
}

void setBestThreadSize() {
    getDeviceProperties();
    threads = deviceProp->maxThreadsDim[0];
}

void setMaxInputArraySize() {
    if (maxInputArraySize == 0) {
        cudaError_t err = cudaSuccess;
        size_t memFree, memTot;
        setDevice();

        choke(err, EXIT_FAILURE,
                cudaMemGetInfo(&memFree, &memTot),
                "get device memory information");
        maxInputArraySize = memFree / sizeof(banker_t) / 2; // Don't be greedy, take half
        debugf("Setting maxInputArraySize to %d with available device memory at %zu bytes.\n", maxInputArraySize, memFree);
    }
}

void usage(char *argv) {
    printf("Usage: %s [--threads=count] [--skip=n] [--limit=m]\n", argv);
    exit(1);
}

/*
 * Parse command line arguments
 */
int parse(int argc, char ** argv, banker_t ** inputPtr, int offset)
{
    banker_t x;
    unsigned int i, skip = 0, limit = 0;
    char *line = NULL;
    size_t n = 0;

    for (i = 1; i < argc; i++) {
        if (strncmp("--threads=", argv[i], 10) == 0) {
            threads = atoi(&argv[i][10]);
        } else if (strncmp("--skip=", argv[i], 7) == 0 && offset == 0) {
            skip = atoi(&argv[i][7]);
        } else if (strncmp("--limit=", argv[i], 8) == 0) {
            limit = atoi(&argv[i][8]);
        } else if (strncmp("--device", argv[i], 8) == 0) {
            device = atoi(&argv[i][8]);
        } else if (strncmp("--debug", argv[i], 7) == 0) {
            debug = 1;
        } else {
            usage(*argv);
        }
    }
    setMaxInputArraySize();
    for (i = 0; (limit == 0 || i < limit - offset + skip) && i < maxInputArraySize && getline(&line, &n, stdin) != -1; i++) {
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

/*
 * Main program accepts one parameter: the number of the row
 * of Pascal's triangle to print.
 */
int main (int argc, char ** argv)
{
    cudaError_t err = cudaSuccess;
    int blocks, asize, size, done = 0;
    banker_t *harray = NULL;
    banker_t *darray = NULL;

    initBinomTable();

    size = parse(argc, argv, &harray, done);
    setDevice();
    if (threads < 1)
        setBestThreadSize();

    do if (size > 0)
    {
        if (harray == NULL) {
            fprintf(stderr, "Failed to allocate host array!\n");
            exit(EXIT_FAILURE);
        }

        blocks = (size + threads - 1) / threads;
        asize = blocks * threads;

        if (darray == NULL)
            choke(err, EXIT_FAILURE,
                    cudaMalloc((void **)&darray, asize * sizeof(banker_t)),
                    "allocate device array");

        choke(err, EXIT_FAILURE,
                cudaMemset(darray, 0, asize * sizeof(banker_t)),
                "initialise device array");

        choke(err, EXIT_FAILURE,
                cudaMemcpy(darray, harray, size * sizeof(banker_t), cudaMemcpyHostToDevice),
                "copy array from host to device");

        debugf("Launching %d block%s of %d thread%s...\n",
                    blocks, blocks == 1 ? "" : "s", threads, threads == 1 ? "" : "s");
        compute<<<blocks, threads>>>(darray);
        choke(err, EXIT_FAILURE,
                cudaGetLastError(),
                "launch compute kernel");

        choke(err, EXIT_FAILURE,
                cudaMemcpy(harray, darray, size * sizeof(banker_t), cudaMemcpyDeviceToHost),
                "copy array from device to host");

        printf(FMT, *harray);
        for(int i = 1; i < size; i++) {
            printf(SEP FMT, harray[i]);
        }
        printf("\n");

        done += size;
    } while ((size = parse(argc, argv, &harray, done)) > 0);

    if (darray != NULL)
        choke(err, EXIT_FAILURE,
                cudaFree(darray),
                "free device array");
    if (harray != NULL)
        free(harray);
    if (deviceProp != NULL)
        free(deviceProp);

    choke(err, EXIT_FAILURE,
            cudaDeviceReset(),
            "deinitialize the device");

    return 0;
}
