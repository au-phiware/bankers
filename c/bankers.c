#include <stdio.h>
#include <stdlib.h>
/*
 * Bit string length (specified in program args).
 */
static unsigned int length;
/*
 * Print binary representation of b.
 * Use `.' instead of `0' for readability.
 */
void output (unsigned int b)
{
    unsigned int i;
    for (i = 0; i < length; i++) {
        if (b & 1)
            printf("1");
        else
            printf(".");
        b = b >> 1;
    }
    printf("\n");
}
/*
 * Compute the binomial coefficient of `n choose k'.
 * Use the fact that binom(n, k) = binom(n, n - k).
 * Use a lookup table (triangle, actually) for speed.
 * Otherwise it's dumb (heart) recursion.
 */
unsigned int binom (unsigned int n, unsigned int k) {
    static unsigned int *quick;
    if (n < 0 || k < 0 || k > n)
        return -1;
    if (n == 0)
        return 0;
    if (n == k || k == 0)
        return 1;
    if (k > n / 2)
        k = n - k;

    if (!quick)
        quick = calloc((length * (length + 1)) / 4, sizeof(unsigned int));

    unsigned int i = (n * (n - 1)) / 4 + k - 1;
    if (quick[i] == 0)
        quick[i] = binom(n - 1, k - 1) + binom(n - 1, k);

    return quick[i];
}
/*
 * Returns the Banker's number at the specified position, a.
 * Derived from the recursive bit flip method.
 */
unsigned int compute (unsigned int a)
{
    unsigned int b = 0;
    if (a == 0)
        return b;

    unsigned int c = 0, e = a, n = length;
    do {
        e -= binom(n, c++);
    } while (binom(n, c) <= e);
    if (binom(n - 1, c - 1) > e)
        c--, b |= 1;

    while (c > 0 && n > 1) {
        if ((b & 1) == 0)
            e -= binom(n - 1, c - 1);
        n--, b <<= 1;
        if (e == 0 || binom(n - 1, c - 1) > e)
            c--, b |= 1;
    }
    b <<= n - 1;
    return b;
}
/*
 * Returns the position of the specified Banker's number, b.
 * The ones contribute \sum_{i=0}^{c-1}{\binom{n}{i}}.
 * Each zero (except leading zeros) contributes
 *  \binom{n-i-1}{c-c_i-1}, where
 *   n is the bit string length,
 *   c is the cardinality of the bit string,
 *   c_i is the cardinality up to bit i, and
 *   i is the position of the zero in the string.
 */
unsigned int inverse (unsigned int b) {
    unsigned int a = 0, c = 0, i = 1, n = length;
    for (; i <= b; i <<= 1)
        if (b & i) c++;
    for (i = 1 << (n - 1); i > 0 && c > 0; --n, i >>= 1)
        if (b & i)
            a += binom(length, --c);
        else
            a += binom(n - 1, c - 1);

    return a;
}
/*
 * Recursive function
 */
unsigned int next (unsigned int b)
{
    unsigned int z = 0, y = 0, i = 1, max = 1 << length;
    while (i < max && b & i)
        y++, i <<= 1;
    while (i < max && !(b & i))
        z++, i <<= 1;
    if (i < max) {
        b &= ~i;
        i = ~((1 << z + y + 1) - 1);
    }
    b &= i;
    i = (1 << y + z) - (1 << z - 1);
    b |= i;
    return b & max - 1;
}
/* 
 * Main program accepts one parameter: the number of bits
 * in the bit string.
 */
int main (int argc, char ** argv)
{
    if (argc != 2)
    {
        printf("Usage: %s n\n", argv[0]);
        exit(1);
    }
    length = atoi(argv[1]);
    unsigned int b = 0;
    for (; b < (1 << length) - 1; b++) {
        output(compute(b));
    }
    return 0;
    for (b = 0; b < (1 << length) - 1; b = next(b)) {
        printf("%4d: ", inverse(b));
        output(compute(inverse(b)));
    }
    for (b = 0; b < (1 << length) - 1; b++) {
        printf("%4d: ", inverse(compute(b)));
        output(compute(b));
    }
    return 0;
}
