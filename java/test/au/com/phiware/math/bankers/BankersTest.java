/**
 * 
 */
package au.com.phiware.math.bankers;

import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.Test;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class BankersTest {

	public void testMonotonicity(Bankers bankers) {
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
	 */
	@Test
	public void testMonotonicityUpTo10() {
		for (int n = 2; n <= 10; n++)
			testMonotonicity(Bankers.getBanker(n));
	}

	public void testIsomorphism(Bankers bankers) {
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
	 */
	@Test
	public void testIsomorphismUpTo10() {
		for (int n = 2; n <= 10; n++)
			testIsomorphism(Bankers.getBanker(n));
	}

}
