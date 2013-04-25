/**
 * 
 */
package au.com.phiware.math.ring;

import java.util.Set;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class IntegerArithmetic implements BitArithmetic<Integer> {

	private static final IntegerArithmetic a = new IntegerArithmetic();
	private IntegerArithmetic() {}
	public static IntegerArithmetic getInstance() {
		return a;
	}

	@Override
	public int maxBitLength() {
		return Integer.SIZE;
	}

	@Override
	public Integer one() {
		return 1;
	}

	@Override
	public Set<Integer> factors(Integer a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[] primeFactorization(Integer a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer gcd(Integer a, Integer b) {
		int r;
		while (b != 0) {
			r = a % b;
			a = b;
			b = r;
		}               
		return a;
	}

	@Override
	public boolean congruent(Integer a, Integer b, Integer mod) {
		return (a - b) % mod == 0;
	}

	@Override
	public Integer mod(Integer a, Integer b) {
		int r = a % b;
		return r >= 0 ? r : (r + b);
	}

	@Override
	public Integer zero() {
		return 0;
	}

	@Override
	public Integer add(Integer a, Integer b) {
		if (a == null || b == null)
			throw new NullPointerException();
		return a + b;
	}

	@Override
	public Integer negate(Integer a) {
		return -a;
	}

	@Override
	public Integer subtract(Integer a, Integer b) {
		return a - b;
	}

	@Override
	public Integer multiply(Integer a, Integer b) {
		return a * b;
	}

	@Override
	public Integer pow(Integer a, Integer b) {
		return (int) Math.pow(a, b);
	}

	@Override
	public Integer max(Integer a, Integer b) {
		return Math.max(a, b);
	}

	@Override
	public Integer min(Integer a, Integer b) {
		return Math.min(a, b);
	}

	@Override
	public boolean testBit(Integer a, int n) {
		return (a & (1 << n)) != 0;
	}

	@Override
	public Integer setBit(Integer a, int n) {
		return a | (1 << n);
	}

	@Override
	public Integer clearBit(Integer a, int n) {
		return a & ~(1 << n);
	}

	@Override
	public Integer flipBit(Integer a, int n) {
		if (testBit(a, n))
			return clearBit(a, n);
		else
			return setBit(a, n);
	}

	@Override
	public int signum(Integer a) {
		return Integer.signum(a);
	}

	@Override
	public Integer reverse(Integer a) {
		return Integer.reverse(a);
	}

	@Override
	public Integer or(Integer a, Integer b) {
		return a | b;
	}

	@Override
	public Integer and(Integer a, Integer b) {
		return a & b;
	}

	@Override
	public Integer nand(Integer a, Integer b) {
		return a & ~b;
	}

	@Override
	public Integer xor(Integer a, Integer b) {
		return a ^ b;
	}

	@Override
	public Integer not(Integer a) {
		return ~a;
	}

	@Override
	public Integer shiftLeft(Integer a, int n) {
		return a << n;
	}

	@Override
	public Integer shiftRight(Integer a, int n) {
		return a >> n;
	}

	@Override
	public int bitCount(Integer a) {
		return Integer.bitCount(a);
	}

	@Override
	public int highestOneBit(Integer a) {
		return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(a);
	}

	@Override
	public int lowestOneBit(Integer a) {
		if (a == 0)
			return -1;
		return Integer.numberOfTrailingZeros(a);
	}

	@Override
	public String toString(Integer a, int radix) {
		return Integer.toString(a, radix);
	}

	@Override
	public int compare(Integer a, Integer b) {
		return a - b;
	}
}
