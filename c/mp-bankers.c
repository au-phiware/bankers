#include <stdio.h>
#include <stdlib.h>
#include <gmp.h>
/*
 * Print binary representation of b.
 * Use `.' instead of `0' for readability.
 */
void output (mpz_t b, mp_bitcnt_t n)
{
    char *str = calloc(n + 2, sizeof(char));
    mp_bitcnt_t i = 0;
    while (i < n)
        if (mpz_tstbit(b, i))
            str[i++] = '1';
        else
            str[i++] = '.';
    str[i] = '\n';
    puts(str);
}
/*
 * Returns the Banker's number at the specified position, a.
 * Derived from the recursive bit flip method.
 */
void compute (mpz_t b, mpz_t a, mp_bitcnt_t n)
{
    mpz_set_ui(b, 0);
    if (mpz_cmp_ui(a, 0) == 0)
        return;

    mp_bitcnt_t c = 0;
    mpz_t e, binom;
    mpz_init2(binom, n);
    mpz_init_set(e, a);
    mpz_bin_uiui(binom, n, c);
    do {
        mpz_sub(e, e, binom);
        mpz_bin_uiui(binom, n, ++c);
    } while (mpz_cmp(binom, e) <= 0);

    do {
        n--;
        if (mpz_cmp_ui(e, 0) == 0
                || (mpz_bin_uiui(binom, n, c - 1), mpz_cmp(binom, e) > 0))
            c--, mpz_setbit(b, n);
        else
            mpz_sub(e, e, binom);
    } while (c > 0 && n > 0);
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
void inverse (mpz_t a, mpz_t b, mp_bitcnt_t n) {
    mpz_set_ui(a, 0);
    mp_bitcnt_t i = n, c = mpz_popcount(b), c_n = c;
    mpz_t binom;
    mpz_init2(binom, n);

    for (i = n - 1; i >= 0 && c > 0; i--)
        if (mpz_tstbit(b, i)) {
            mpz_bin_uiui(binom, n, --c);
            mpz_add(a, a, binom);
        } else {
            mpz_bin_uiui(binom, i, c - 1);
            mpz_add(a, a, binom);
        }
}
/*
 * Recursive function
 */
void next (mpz_t b, mpz_t prev, mp_bitcnt_t n)
{
    if (prev != b)
        mpz_set(b, prev);
    mp_bitcnt_t z = 0, i = 0;
    while (i < n && mpz_tstbit(b, i))
        mpz_clrbit(b, i++);
    while (i < n && !mpz_tstbit(b, i))
        z++, i++;
    if (i < n)
        mpz_clrbit(b, i);
    while (i >= z)
        mpz_setbit(b, --i);
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
    mp_bitcnt_t n = atol(argv[1]);
    char *a_format = calloc(25, sizeof(char));
    sprintf(a_format, "%%%dZd: ", (int)(n * 0.3) + 1);
    mpz_t b, a;
    mpz_init2(b, n);
    mpz_init2(a, n);
    for (mpz_set_ui(a, 0); n >= mpz_sizeinbase(a, 2); mpz_add_ui(a, a, 1)) {
        compute(b, a, n);
        gmp_printf(a_format, a);
        output(b, n);
    }
    mpz_set_ui(b, 0);
    for (;;) {
        inverse(a, b, n);
        gmp_printf("%20Zd: ", a);
        output(b, n);
        if (mpz_popcount(b) == n) break;
        next(b, b, n);
    }
    return 0;
}
