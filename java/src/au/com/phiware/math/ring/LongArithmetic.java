package au.com.phiware.math.ring;

import java.util.Set;

public class LongArithmetic implements BitArithmetic<Long> {

	private static final LongArithmetic a = new LongArithmetic();
	private LongArithmetic() {}
	public static LongArithmetic getInstance() {
		return a;
	}

	@Override
	public int maxBitLength() {
		return 64;
	}

	@Override
	public Long one() {
		return 1L;
	}

	@Override
	public Set<Long> factors(Long a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long[] primeFactorization(Long a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long gcd(Long a, Long b) {
		long r;
		while (b != 0) {
			r = a % b;
			a = b;
			b = r;
		}               
		return a;
	}

	@Override
	public boolean congruent(Long a, Long b, Long mod) {
		return (a - b) % mod == 0;
	}

	@Override
	public Long mod(Long a, Long b) {
		long r = a % b;
		return r >= 0 ? r : (r + b);
	}

	@Override
	public Long zero() {
		return 0L;
	}

	@Override
	public Long add(Long a, Long b) {
		return a + b;
	}

	@Override
	public Long negate(Long a) {
		return -a;
	}

	@Override
	public Long subtract(Long a, Long b) {
		return a - b;
	}

	@Override
	public Long multiply(Long a, Long b) {
		return a * b;
	}

	@Override
	public Long pow(Long a, Long b) {
		return (long) Math.pow(a, b);
	}

	@Override
	public Long max(Long a, Long b) {
		return Math.max(a, b);
	}

	@Override
	public Long min(Long a, Long b) {
		return Math.min(a, b);
	}

	@Override
	public boolean testBit(Long a, int n) {
		return (a & (1 << n)) != 0;
	}

	@Override
	public Long setBit(Long a, int n) {
		return a | (1 << n);
	}

	@Override
	public Long clearBit(Long a, int n) {
		return a & ~(1 << n);
	}

	@Override
	public Long flipBit(Long a, int n) {
		if (testBit(a, n))
			return clearBit(a, n);
		else
			return setBit(a, n);
	}

	@Override
	public int signum(Long a) {
		return Long.signum(a);
	}

	@Override
	public Long reverse(Long a) {
		return Long.reverse(a);
	}

	@Override
	public Long or(Long a, Long b) {
		return a | b;
	}

	@Override
	public Long and(Long a, Long b) {
		return a & b;
	}

	@Override
	public Long nand(Long a, Long b) {
		return a & ~b;
	}

	@Override
	public Long xor(Long a, Long b) {
		return a ^ b;
	}

	@Override
	public Long not(Long a, Long b) {
		return ~a;
	}

	@Override
	public Long shiftLeft(Long a, int n) {
		return a << n;
	}

	@Override
	public Long shiftRight(Long a, int n) {
		return a >> n;
	}

	@Override
	public int bitCount(Long a) {
		return Long.bitCount(a);
	}

	@Override
	public int highestOneBit(Long a) {
		return 31 - Long.numberOfLeadingZeros(a);
	}

	@Override
	public int lowestOneBit(Long a) {
		if (a == 0)
			return -1;
		return Long.numberOfTrailingZeros(a);
	}

	@Override
	public String toString(Long a, int radix) {
		return Long.toString(a, radix);
	}

	@Override
	public int compare(Long a, Long b) {
		return a < b ? -1 : (a > b ? 1 : 0);
	}
}
