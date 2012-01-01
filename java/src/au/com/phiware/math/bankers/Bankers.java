/**
 * 
 */
package au.com.phiware.math.bankers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.phiware.math.binom.Binom;
import au.com.phiware.math.ring.ArithmeticFactory;
import au.com.phiware.math.ring.BitArithmetic;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public abstract class Bankers<V extends Number> {
	private static final Logger log = LoggerFactory.getLogger(Bankers.class);
	private BitArithmetic<V> arithmetic;
	private int length;

	public Bankers(int length, BitArithmetic<V> arithmetic) {
		this.length = length;
		this.arithmetic = arithmetic;
	}
	@SuppressWarnings("unchecked")
	public Bankers(int length) throws ClassNotFoundException {
		this.length = length;

		ParameterizedType superType = (ParameterizedType) this.getClass().getGenericSuperclass();
		while (!Bankers.class.equals(superType.getRawType()))
			superType = (ParameterizedType) ((Class<?>) superType.getRawType()).getGenericSuperclass();
		Type[] actualType = superType.getActualTypeArguments();
		arithmetic = ArithmeticFactory.getBitArithmetic((Class<V>) actualType[0]);

		if (length > arithmetic.maxBitLength())
			throw new IllegalArgumentException("Length, "+length+", too big. Try different component class.");
	}

	public int length() {
		return length;
	}

	public V next(V b) {
		V next = arithmetic.zero();
		int z = 0, i = length - 1;
		
		while (i >= 0 && arithmetic.testBit(b, i))
			i--;
		while (i >= 0 && !arithmetic.testBit(b, i)) {
			z++;
			i--;
		}
		
		V passthru = arithmetic.subtract(
					arithmetic.shiftLeft(arithmetic.one(), i + 1),
					arithmetic.one()
				);
		
		next = arithmetic.or(
				next,
				arithmetic.nand(
					arithmetic.subtract(
						arithmetic.shiftLeft(arithmetic.one(), length - z + 1),
						arithmetic.one()
					),
					passthru
				)
			);
		if (i > 0) {
			passthru = arithmetic.shiftRight(passthru, 1);
			next = arithmetic.or(next, arithmetic.and(b, passthru));
		}
		
		return next;
	}
	
	public V to(V a) {
		V e, b = arithmetic.zero();
		
		if (a.equals(b)) return b;
		
		Binom<V> binom = new Binom<V>(arithmetic, length, 0);
		while (arithmetic.compare(binom.right().sum(), a) <= 0)
			binom = binom.right();
		e = arithmetic.subtract(a, binom.sum());
		
		debug(binom);
		binom = binom.down();
		for (int i = 0; binom != null; i++) {
			debug(binom);
			if (arithmetic.compare(binom.value(), e) > 0) {
				b = arithmetic.setBit(b, i);
				binom = binom.back();
			} else {
				e = arithmetic.subtract(e, binom.value());
				binom = binom.down();
			}
		}
		
		return b;
	}

	public V from(V b) {
		int n = 0, c = arithmetic.bitCount(b);
		
		if (c == 0)
			return arithmetic.zero();
		
		Binom<V> binom = new Binom<V>(arithmetic, length, c - 1);
		V a = binom.sum();
		
		debug(binom);
		binom = binom.down();
		while (binom != null && c > 0) {
			debug(binom);
			if (arithmetic.testBit(b, n++)) {
				binom = binom.back();
				c--;
			} else {
				a = arithmetic.add(a, binom.value());
				binom = binom.down();
			}
		}
		
		log.debug("from {} to {}", b, a);
		return a;
	}

	private void debug(Binom<V> binom) {
		if (log.isDebugEnabled()) {
			char[] str = new char[binom.getRow() + 1];

			Arrays.fill(str, '.');
			str[binom.getColumn()] = 'X';

			log.debug(new String(str));
		}
	}
	
	public static void main(String[] argv) {
		try {
			if (argv.length > 0) {
				int n = new Integer(argv[0]), m = 1 << n;
				if (argv.length > 1 || argv[0].matches("[01]+")) {
					for (int i = 0; i < argv.length; i++) {
						try {
							Bankers<Long> bankers = new Bankers<Long>(argv[i].length()){};
							System.out.println(
								String.format(
									"%"+((int)Math.log10(argv[i].length()) + 1)+"d",
									bankers.from(Long.parseLong(argv[i], 2))
								)
							);
						} catch (NumberFormatException e) {
							if (i > 0) {
								Bankers<Long> bankers = new Bankers<Long>(n){};
								System.out.println(
									String.format(
										"%"+n+"s",
										Long.toBinaryString(bankers.to(new Long(argv[i])))
									).replaceAll("[ 0]", ".")
								);
							}
						}
					}
				} else {
					Bankers<Long> bankers = new Bankers<Long>(n){};
					long b = 0L;
					for (long i = 1; i < m; i++)
						System.out.println(
							String.format(
								"%"+n+"s",
								Long.toBinaryString(b = bankers.next(b))
							).replaceAll("[ 0]", ".")
						);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
