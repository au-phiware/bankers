/**
 * 
 */
package au.com.phiware.math.bankers;

import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.Test;

import au.com.phiware.math.binom.BinomCounter;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class BankersTest {

	public void testMonotonicity(Bankers<Integer> bankers) {
		int b = 0;
		int c = 0;
		for (int i = 0; i < 1 << bankers.length(); i++) {
			b = bankers.next(b);
			assertTrue(b+" at "+i+" should have no less than "+c+" ones", Integer.bitCount(b) >= c);
			c = Integer.bitCount(b);
		}
	}

	/**
	 * The number of ones in the Banker's sequence should never decrease. 
	 * @throws ClassNotFoundException if arithmetic is not found for Integer
	 */
	@Test
	public void testMonotonicityUpTo10() throws ClassNotFoundException {
		for (int n = 2; n <= 10; n++)
			testMonotonicity(new Bankers<Integer>(n){});
	}

	public void testIsomorphism(Bankers<Integer> bankers) {
		int b = 0;
		BitSet seen = new BitSet(1 << bankers.length());
		for (int i = 0; i < 1 << bankers.length(); i++) {
			b = bankers.next(b);
			assertFalse(b+" at "+i+" should not have been seen", seen.get(b));
			seen.set(b);
		}
	}

	/**
	 * The natural numbers should have a one-to-one mapping to the Banker's numbers. 
	 * @throws ClassNotFoundException if arithmetic is not found for Integer
	 */
	@Test
	public void testIsomorphismUpTo10() throws ClassNotFoundException {
		for (int n = 2; n <= 10; n++)
			testIsomorphism(new Bankers<Integer>(n){});
	}

	public void testFrom(Bankers<Integer> bankers) {
		Integer i = 0;
		Integer b = 0;
		for (; i < 1 << bankers.length(); i++) {
			BinomCounter.resetCounter();
			assertEquals("next and from should be the same for length "+bankers.length(), i, bankers.from(b));
			assertTrue("Should be efficient at "+i+" of length "+bankers.length(), BinomCounter.hasAllOnes());
			b = bankers.next(b);
		}
	}
	
	@Test
	public void testFromUpTo10() throws ClassNotFoundException {
		for (int n = 2; n <= 10; n++)
			testFrom(new Bankers<Integer>(n){});
	}

	public void testTo(Bankers<Integer> bankers) {
		Integer i = 0;
		Integer b = 0;
		for (; i < 1 << bankers.length(); i++) {
			BinomCounter.resetCounter();
			assertEquals("next and to should be the same for length "+bankers.length(), b, bankers.to(i));
			assertTrue("Should be efficient at "+i+" of length "+bankers.length(), BinomCounter.hasAllOnes());
			b = bankers.next(b);
		}
	}
	
	@Test
	public void testToUpTo10() throws ClassNotFoundException {
		for (int n = 2; n <= 10; n++)
			testTo(new Bankers<Integer>(n){});
	}
}
