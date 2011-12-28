/**
 * 
 */
package au.com.phiware.math.binom;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.text.MessageFormat;

import org.junit.Test;

import arit.IntegralArithmetics;
import arit.impl.*;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class BinomTest {

	static long fact(int n)
    {
        long x = 1;
        for (long i = 1; i <= n; ++i) x *= i;
        return x;
    }
	
	static BigInteger factorial(int n)
    {
        BigInteger x = BigInteger.ONE;
        for (int i = 1; i <= n; ++i) x = x.multiply(BigInteger.valueOf(i));
        return x;
    }
	
	public void testRow(int n) {
		IntegralArithmetics<Integer> arithmetics = IntegerArithmetics.getInstance();
		for(int k = 0; k <= n; k++) {
			BinomCounter.counter = null;
			BinomCounter<Integer> binom = new BinomCounter<Integer>(arithmetics, n, k);
			assertEquals(n + " choose " + k, fact(n)/(fact(k)*fact(n-k)), binom.intValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}
	
	public void testRowSum(int n) {
		IntegralArithmetics<Integer> arithmetics = IntegerArithmetics.getInstance();
		long expected = 0;
		for(int k = 0; k <= n; k++) {
			BinomCounter.counter = null;
			BinomCounter<Integer> binom = new BinomCounter<Integer>(arithmetics, n, k);
			expected += fact(n)/(fact(k)*fact(n-k));
			assertEquals("sum_i=(0..."+k+")("+n+" choose i)", expected, binom.sum().intValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}
	
	@Test
	public void testRowsUpTo10() {
		for (int n = 1; n <= 10; n ++)
			testRow(n);
	}
	
	@Test
	public void testLongRow() {
		IntegralArithmetics<Long> arithmetics = LongArithmetics.getInstance();
		int n = 36;
		for(int k = 0; k <= n; k++) {
			BinomCounter.counter = null;
			BinomCounter<Long> binom = new BinomCounter<Long>(arithmetics, n, k);
			assertEquals(n + " choose " + k, factorial(n).divide(factorial(k).multiply(factorial(n-k))).longValue(), binom.longValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}
	
	@Test
	public void testBigRow() {
		IntegralArithmetics<BigInteger> arithmetics = BigIntegerArithmetics.getInstance();
		int n = 67;
		for(int k = 0; k <= n; k++) {
			BinomCounter.counter = null;
			BinomCounter<BigInteger> binom = new BinomCounter<BigInteger>(arithmetics, n, k);
			assertEquals(n + " choose " + k, factorial(n).divide(factorial(k).multiply(factorial(n-k))), binom.value());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}
	
	@Test
	public void testReallyBigBinom() {
		IntegralArithmetics<BigInteger> arithmetics = BigIntegerArithmetics.getInstance();
		int n = 600, k = 300;
		BinomCounter.counter = null;
		BinomCounter<BigInteger> binom = new BinomCounter<BigInteger>(arithmetics, n, k);
		assertEquals(n + " choose " + k, 
				new BigInteger("135107941996194268514474877978504530397233945449193479925965721786474150408005716961950480198274469818673334131365837249043900490761151591695308427048536947621976068789875968372656"),
				binom.value());
	}

	/**
	 * Test method for {@link au.com.phiware.math.binom.Binom#sum()}.
	 */
	@Test
	public void testRowSumsUpTo10() {
		for (int n = 1; n <= 10; n ++)
			testRowSum(n);
	}
	

	/**
	 * Test method for {@link au.com.phiware.math.binom.Binom#up()}.
	 */
	@Test
	public void testUp() {
		IntegralArithmetics<Integer> arithmetics = IntegerArithmetics.getInstance();
		for(int n = 1; n <= 10; n++)
		for(int k = 0; k <= n; k++) {
			int expected = new BinomCounter<Integer>(arithmetics, n + 1, k).intValue();
			BinomCounter.counter = null;
			BinomCounter<Integer> binom = new BinomCounter<Integer>(arithmetics, n, k);
			assertEquals((n + 1) + " choose " + k, expected, binom.up().intValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}

	/**
	 * Test method for {@link au.com.phiware.math.binom.Binom#down()}.
	 */
	@Test
	public void testDown() {
		IntegralArithmetics<Integer> arithmetics = IntegerArithmetics.getInstance();
		for(int n = 2; n <= 10; n++)
		for(int k = 0; k < n; k++) {
			int expected = new BinomCounter<Integer>(arithmetics, n - 1, k).intValue();
			BinomCounter.counter = null;
			BinomCounter<Integer> binom = new BinomCounter<Integer>(arithmetics, n, k);
			assertEquals((n - 1) + " choose " + k, expected, binom.down().intValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}

	/**
	 * Test method for {@link au.com.phiware.math.binom.Binom#next()}.
	 */
	@Test
	public void testNext() {
		IntegralArithmetics<Integer> arithmetics = IntegerArithmetics.getInstance();
		for(int n = 1; n <= 10; n++)
		for(int k = 0; k < n; k++) {
			int expected = new BinomCounter<Integer>(arithmetics, n + 1, k + 1).intValue();
			BinomCounter.counter = null;
			BinomCounter<Integer> binom = new BinomCounter<Integer>(arithmetics, n, k);
			assertEquals((n + 1) + " choose " + (k + 1), expected, binom.next().intValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}

	/**
	 * Test method for {@link au.com.phiware.math.binom.Binom#back()}.
	 */
	@Test
	public void testBack() {
		IntegralArithmetics<Integer> arithmetics = IntegerArithmetics.getInstance();
		for(int n = 2; n <= 10; n++)
		for(int k = 1; k <= n; k++) {
			int expected = new BinomCounter<Integer>(arithmetics, n - 1, k - 1).intValue();
			BinomCounter.counter = null;
			BinomCounter<Integer> binom = new BinomCounter<Integer>(arithmetics, n, k);
			assertEquals((n - 1) + " choose " + (k - 1), expected, binom.back().intValue());
			assertTrue(BinomCounter.hasAllOnes());
		}
	}

}
