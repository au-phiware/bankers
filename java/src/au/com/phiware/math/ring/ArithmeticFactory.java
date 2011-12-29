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
		RingArithmetic<V> a;

		try {
			return getBitArithmeticForNumber(n);
		} catch (ClassNotFoundException e) {}
		
		throw new ClassNotFoundException();
	}

	public static <V extends Number> BitArithmetic<V> getBitArithmeticForNumber(V n) throws ClassNotFoundException {
		
		if (n instanceof BigInteger)
			return (BitArithmetic<V>) BigIntegerArithmetic.getInstance();
		
		if (n instanceof Long)
			return (BitArithmetic<V>) LongArithmetic.getInstance();
		
		if (n instanceof Integer)
			return (BitArithmetic<V>) IntegerArithmetic.getInstance();
		
		throw new ClassNotFoundException();
	}
}
