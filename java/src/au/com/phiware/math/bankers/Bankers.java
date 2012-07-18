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
import au.com.phiware.math.binom.BinomFactory;
import au.com.phiware.math.binom.Binomials;
import au.com.phiware.math.ring.ArithmeticFactory;
import au.com.phiware.math.ring.BitArithmetic;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public abstract class Bankers<V extends Number> {
	private static final Logger log = LoggerFactory.getLogger(Bankers.class);
	private BinomFactory<V> binomFactory;
	private final int length;
	private final V mask;

	private V constructMask() {
		BitArithmetic<V> a = getArithmetic();
		V topBit = a.setBit(a.zero(), length - 1);
		return a.add(topBit, a.subtract(topBit, a.one()));
	}
	public Bankers(int length, BinomFactory<V> binomFactory) {
		this.length = length;
		this.binomFactory = binomFactory;
		this.mask = constructMask();
	}
	@SuppressWarnings("unchecked")
	public Bankers(int length) throws ClassNotFoundException {
		this.length = length;

		ParameterizedType superType = (ParameterizedType) this.getClass().getGenericSuperclass();
		while (!Bankers.class.equals(superType.getRawType()))
			superType = (ParameterizedType) ((Class<?>) superType.getRawType()).getGenericSuperclass();
		Type[] actualType = superType.getActualTypeArguments();
		BitArithmetic<V> arithmetic = ArithmeticFactory.getBitArithmetic((Class<V>) actualType[0]);

		if (length > arithmetic.maxBitLength())
			throw new IllegalArgumentException("Length, "+length+", is too big. Try a different component class.");

		binomFactory = Binomials.defaultBinomFactory(arithmetic);

		this.mask = constructMask();
	}

	public BitArithmetic<V> getArithmetic() {
		return binomFactory.getArithmetic();
	}

	public int length() {
		return length;
	}

	public V next(V b) {
		BitArithmetic<V> a = getArithmetic();

		V next = a.zero();
		int z = 0, i = length - 1;
		
		while (i >= 0 && a.testBit(b, i))
			i--;
		while (i >= 0 && !a.testBit(b, i)) {
			z++;
			i--;
		}
		
		V passthru = a.subtract(
					a.shiftLeft(a.one(), i + 1),
					a.one()
				);
		
		next = a.or(
				next,
				a.nand(
					a.subtract(
						a.shiftLeft(a.one(), length - z + 1),
						a.one()
					),
					passthru
				)
			);
		if (i > 0) {
			passthru = a.shiftRight(passthru, 1);
			next = a.or(next, a.and(b, passthru));
		}
		
		return next;
	}
	
	private Map<Integer, SoftReference<Binom<V>>> binomRow = new HashMap<Integer, SoftReference<Binom<V>>>();
	private Binom<V> getBinom(int k) {
		Binom<V> binom = null;
		if (binomRow.containsKey(k))
			binom = binomRow.get(k).get();
		if (binom == null) {
			binom = binomFactory.createBinom(length, k);
			//binom = new au.com.phiware.math.binom.BinomCounter<V>(arithmetic, length, k);
			binomRow.put(k, new SoftReference<Binom<V>>(binom));
		}
		return binom;
	}
	
	public V to(V v) {
		BitArithmetic<V> a = getArithmetic();
		V e, b = a.zero();
		
		if (a.testBit(v, length() - 1)) {
			v = a.xor(v, mask);
			b = a.xor(to(v), mask);
		} else {
			if (v.equals(b)) return b;
			
			Binom<V> binom = getBinom(0);
			while (a.compare(binom.right().sum(), v) <= 0)
				binom = binom.right();
			e = a.subtract(v, binom.sum());
			
			debug(binom);
			binom = binom.down();
			for (int i = 0; binom != null; i++) {
				debug(binom);
				if (a.compare(binom.value(), e) > 0) {
					b = a.setBit(b, i);
					binom = binom.back();
				} else {
					e = a.subtract(e, binom.value());
					binom = binom.down();
				}
			}
		}
		
		return b;
	}

	public V from(V b) {
		BitArithmetic<V> a = getArithmetic();
		int n = 0, c = a.bitCount(b);
		
		if (c == 0)
			return a.zero();
		
		Binom<V> binom = getBinom(c - 1);
		V v = binom.sum();
		
		debug(binom);
		binom = binom.down();
		while (binom != null && c > 0) {
			debug(binom);
			if (a.testBit(b, n++)) {
				binom = binom.back();
				c--;
			} else {
				v = a.add(v, binom.value());
				binom = binom.down();
			}
		}
		
		log.debug("from {} to {}", b, v);
		return v;
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
				int n = new Integer(argv[0]);
				long m = 1L << n;
				Bankers<Long> bankers = new Bankers<Long>(n){};
				if (argv.length > 1 || argv[0].matches("[01]+")) {
					for (int i = 0; i < argv.length; i++) {
						if (argv[i].matches("0[01]+") || argv[i].matches("0b[01]+")
								|| (argv[i].matches("1[01]+")
										&& Long.parseLong(argv[i]) >= 1 << n)) {
							String b = argv[i].replaceFirst("^0b", "");
							Bankers<Long> bbankers = new Bankers<Long>(b.length()){};
							System.out.println(
								String.format(
									"%"+((int) Math.log10(b.length()) + 1)+"d",
									bbankers.from(Long.parseLong(b, 2))
								)
							);
						} else {
							if (i > 0) {
								System.out.print(
										String.format(
											"%"+n+"s",
											Long.toBinaryString(new Long(argv[i]))
										).replaceAll("[ 0]", ".")
										+ " : "
									);
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
					long b = 0L;
					System.out.println("Generating Banker's sequence for length, "+n+"...");
					for (long i = 1; i < m; i++) {
						b = bankers.to(i);
						System.out.print(
								String.format(
									"%"+n+"s",
									Long.toBinaryString(i)
								).replaceAll("[ 0]", ".")
								+ " : "
							);
						System.out.println(
							String.format(
								"%"+n+"s",
								Long.toBinaryString(b)
							).replaceAll("[ 0]", ".")
						);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
