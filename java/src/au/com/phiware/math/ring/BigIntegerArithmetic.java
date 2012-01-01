/**
 * 
 */
package au.com.phiware.math.ring;

import java.math.BigInteger;
import java.util.Set;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class BigIntegerArithmetic implements BitArithmetic<BigInteger> {

	private static final BigIntegerArithmetic a = new BigIntegerArithmetic();
	private BigIntegerArithmetic() {}
	public static BigIntegerArithmetic getInstance() {
		return a;
	}

	@Override
	public int maxBitLength() {
		return Integer.MAX_VALUE;
	}

	@Override
	public BigInteger one() {
		return BigInteger.ONE;
	}

	@Override
	public Set<BigInteger> factors(BigInteger a) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public BigInteger[] primeFactorization(BigInteger a) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public BigInteger gcd(BigInteger a, BigInteger b) {
		return a.gcd(b);
	}

	@Override
	public boolean congruent(BigInteger a, BigInteger b, BigInteger mod) {
		return mod(subtract(a, b), mod).equals(zero());
	}

	@Override
	public BigInteger mod(BigInteger a, BigInteger b) {
		return a.mod(b);
	}

	@Override
	public BigInteger zero() {
		return BigInteger.ONE;
	}

	@Override
	public BigInteger add(BigInteger a, BigInteger b) {
		return a.add(b);
	}

	@Override
	public BigInteger negate(BigInteger a) {
		return a.negate();
	}

	@Override
	public BigInteger subtract(BigInteger a, BigInteger b) {
		return a.subtract(b);
	}

	@Override
	public BigInteger multiply(BigInteger a, BigInteger b) {
		return a.multiply(b);
	}

	@Override
	public BigInteger pow(BigInteger a, BigInteger b) {
		return a.pow(b.intValue());
	}

	@Override
	public BigInteger max(BigInteger a, BigInteger b) {
		return a.max(b);
	}

	@Override
	public BigInteger min(BigInteger a, BigInteger b) {
		return a.min(b);
	}

	@Override
	public boolean testBit(BigInteger a, int n) {
		return a.testBit(n);
	}

	@Override
	public BigInteger setBit(BigInteger a, int n) {
		return a.setBit(n);
	}

	@Override
	public BigInteger clearBit(BigInteger a, int n) {
		return a.clearBit(n);
	}

	@Override
	public BigInteger flipBit(BigInteger a, int n) {
		if (testBit(a, n))
			return clearBit(a, n);
		else
			return setBit(a, n);
	}

	@Override
	public int signum(BigInteger a) {
		return a.signum();
	}

	@Override
	public BigInteger or(BigInteger a, BigInteger b) {
		return a.or(b);
	}

	@Override
	public BigInteger and(BigInteger a, BigInteger b) {
		return a.add(b);
	}

	@Override
	public BigInteger nand(BigInteger a, BigInteger b) {
		return a.andNot(b);
	}

	@Override
	public BigInteger xor(BigInteger a, BigInteger b) {
		return a.xor(b);
	}

	@Override
	public BigInteger not(BigInteger a, BigInteger b) {
		return a.not();
	}

	@Override
	public BigInteger shiftLeft(BigInteger a, int n) {
		return a.shiftLeft(n);
	}

	@Override
	public BigInteger shiftRight(BigInteger a, int n) {
		return a.shiftRight(n);
	}

	@Override
	public int bitCount(BigInteger a) {
		return a.bitCount();
	}

	@Override
	public int highestOneBit(BigInteger a) {
		return a.bitLength() - 1;
	}

	@Override
	public int lowestOneBit(BigInteger a) {
		return a.getLowestSetBit();
	}

	@Override
	public String toString(BigInteger a, int radix) {
		return a.toString(radix);
	}

	@Override
	public int compare(BigInteger a, BigInteger b) {
		return a.compareTo(b);
	}
	
	@Override
	public BigInteger reverse(BigInteger a) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
