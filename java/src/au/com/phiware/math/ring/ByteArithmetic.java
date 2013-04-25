/**
 * 
 */
package au.com.phiware.math.ring;

import java.util.Set;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class ByteArithmetic implements BitArithmetic<Byte> {

	private static final ByteArithmetic a = new ByteArithmetic();
	private ByteArithmetic() {}
	public static ByteArithmetic getInstance() {
		return a;
	}

	@Override
	public int maxBitLength() {
		return Byte.SIZE;
	}

	@Override
	public Byte one() {
		return 1;
	}

	@Override
	public Set<Byte> factors(Byte a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Byte[] primeFactorization(Byte a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Byte gcd(Byte a, Byte b) {
		byte r;
		while (b != 0) {
			r = (byte) (a % b);
			a = b;
			b = r;
		}               
		return a;
	}

	@Override
	public boolean congruent(Byte a, Byte b, Byte mod) {
		return (a - b) % mod == 0;
	}

	@Override
	public Byte mod(Byte a, Byte b) {
		byte r = (byte) (a % b);
		return (byte) (r >= 0 ? r : (r + b));
	}

	@Override
	public Byte zero() {
		return 0;
	}

	@Override
	public Byte add(Byte a, Byte b) {
		return (byte) (a + b);
	}

	@Override
	public Byte negate(Byte a) {
		return (byte) -a;
	}

	@Override
	public Byte subtract(Byte a, Byte b) {
		return (byte) (a - b);
	}

	@Override
	public Byte multiply(Byte a, Byte b) {
		return (byte) (a * b);
	}

	@Override
	public Byte pow(Byte a, Byte b) {
		return (byte) Math.pow(a, b);
	}

	@Override
	public Byte max(Byte a, Byte b) {
		return (byte) Math.max(a, b);
	}

	@Override
	public Byte min(Byte a, Byte b) {
		return (byte) Math.min(a, b);
	}

	@Override
	public boolean testBit(Byte a, int n) {
		return (a & (1 << n)) != 0;
	}

	@Override
	public Byte setBit(Byte a, int n) {
		return (byte) (a | (1 << n));
	}

	@Override
	public Byte clearBit(Byte a, int n) {
		return (byte) (a & ~(1 << n));
	}

	@Override
	public Byte flipBit(Byte a, int n) {
		if (testBit(a, n))
			return clearBit(a, n);
		else
			return setBit(a, n);
	}

	@Override
	public int signum(Byte a) {
		return Integer.signum(a);
	}

	@Override
	public Byte reverse(Byte a) {
		return (byte) (Integer.reverse(a) >> (Integer.SIZE - Byte.SIZE));
	}

	@Override
	public Byte or(Byte a, Byte b) {
		return (byte) (a | b);
	}

	@Override
	public Byte and(Byte a, Byte b) {
		return (byte) (a & b);
	}

	@Override
	public Byte nand(Byte a, Byte b) {
		return (byte) (a & ~b);
	}

	@Override
	public Byte xor(Byte a, Byte b) {
		return (byte) (a ^ b);
	}

	@Override
	public Byte not(Byte a) {
		return (byte) ~a;
	}

	@Override
	public Byte shiftLeft(Byte a, int n) {
		return (byte) (a << n);
	}

	@Override
	public Byte shiftRight(Byte a, int n) {
		return (byte) (a >> n);
	}

	@Override
	public int bitCount(Byte a) {
		return Integer.bitCount(a & 0xFF);
	}

	@Override
	public int highestOneBit(Byte a) {
		return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(a & 0xFF);
	}

	@Override
	public int lowestOneBit(Byte a) {
		if (a == 0)
			return -1;
		return Integer.numberOfTrailingZeros(a & 0xFF);
	}

	@Override
	public String toString(Byte a, int radix) {
		return Integer.toString(a, radix);
	}

	@Override
	public int compare(Byte a, Byte b) {
		return a - b;
	}
}
