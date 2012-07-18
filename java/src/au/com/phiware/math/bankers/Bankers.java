/**
 * 
 */
package au.com.phiware.math.bankers;

import java.lang.ref.SoftReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
	private final int length;
	private final V maxValue;

	private V constructMaxValue() {
		V topBit = arithmetic.setBit(arithmetic.zero(), length - 1);
		return arithmetic.add(topBit, arithmetic.subtract(topBit, arithmetic.one()));
	}
	public Bankers(int length, BitArithmetic<V> arithmetic) {
		this.length = length;
		this.arithmetic = arithmetic;
		this.maxValue = constructMaxValue();
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

		this.maxValue = constructMaxValue();
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
	
	private Map<Integer, SoftReference<Binom<V>>> binomRow = new HashMap<Integer, SoftReference<Binom<V>>>();
	private Binom<V> getBinom(int k) {
		Binom<V> binom = null;
		if (binomRow.containsKey(k))
			binom = binomRow.get(k).get();
		if (binom == null) {
			binom = new Binom<V>(arithmetic, length, k);
			//binom = new au.com.phiware.math.binom.BinomCounter<V>(arithmetic, length, k);
			binomRow.put(k, new SoftReference<Binom<V>>(binom));
		}
		return binom;
	}
	
	public V to(V a) {
		V e, b = arithmetic.zero();
		
		if (arithmetic.testBit(a, length() - 1)) {
			a = arithmetic.xor(a, maxValue);
			b = arithmetic.xor(to(a), maxValue);
		} else {
			if (a.equals(b)) return b;
			
			Binom<V> binom = getBinom(0);
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
		}
		
		return b;
	}

	public V from(V b) {
		int n = 0, c = arithmetic.bitCount(b);
		
		if (c == 0)
			return arithmetic.zero();
		
		Binom<V> binom = getBinom(c - 1);
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
						if (argv[i].matches("0[01]+") || argv[i].matches("0b[01]+")
								|| (argv[i].matches("1[01]+")
										&& Long.parseLong(argv[i]) >= 1 << n)) {
							String b = argv[i].replaceFirst("^0b", "");
							Bankers<Long> bankers = new Bankers<Long>(b.length()){};
							System.out.println(
								String.format(
									"%"+((int) Math.log10(b.length()) + 1)+"d",
									bankers.from(Long.parseLong(b, 2))
								)
							);
						} else {
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
