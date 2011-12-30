/**
 * 
 */
package au.com.phiware.math.bankers;

import java.util.HashMap;
import java.util.Map;

import au.com.phiware.math.ring.BigIntegerArithmetic;
import au.com.phiware.math.ring.BitArithmetic;
import au.com.phiware.math.ring.IntegerArithmetic;
import au.com.phiware.math.ring.LongArithmetic;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class Bankers {
	private BitArithmetic<? extends Number> arithmetic;
	private int length;
	
	private Bankers(int length) {
		if (length <= 32)
			arithmetic = IntegerArithmetic.getInstance();
		else if (length <= 64)
			arithmetic = LongArithmetic.getInstance();
		else
			arithmetic = BigIntegerArithmetic.getInstance();
		this.length = length;
	}

	public int length() {
		return length;
	}

	private static Map<Integer, Bankers> instances = new HashMap<Integer, Bankers>();
	public static Bankers getBanker(int length) {
		if (!instances.containsKey(length))
			instances.put(length, new Bankers(length));
		return instances.get(length);
	}

	public <V extends Number> V next(V b) {
		return null;
	}
}
