/**
 * 
 */
package au.com.phiware.math.ring;

import java.math.BigInteger;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class ArithmeticFactory {
	public static <V extends Number> RingArithmetic<V> getArithmeticForNumber(V n) throws ClassNotFoundException {
		try {
			return getBitArithmeticForNumber(n);
		} catch (ClassNotFoundException e) {}
		
		throw new ClassNotFoundException();
	}

	@SuppressWarnings("unchecked")
	public static <V extends Number> BitArithmetic<V> getBitArithmetic(Class<V> type) throws ClassNotFoundException {
		
		if (type.isAssignableFrom(BigInteger.class))
			return (BitArithmetic<V>) BigIntegerArithmetic.getInstance();
		
		if (type.isAssignableFrom(Long.class))
			return (BitArithmetic<V>) LongArithmetic.getInstance();
		
		if (type.isAssignableFrom(Integer.class))
			return (BitArithmetic<V>) IntegerArithmetic.getInstance();
		
		throw new ClassNotFoundException();
	}

	@SuppressWarnings("unchecked")
	public static <V extends Number> BitArithmetic<V> getBitArithmeticForNumber(V n) throws ClassNotFoundException {
		return (BitArithmetic<V>) getBitArithmetic(n.getClass());
	}
}
