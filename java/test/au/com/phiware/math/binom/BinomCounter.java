/**
 * 
 */
package au.com.phiware.math.binom;

import java.util.HashMap;
import java.util.Map;

import arit.IntegralArithmetics;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class BinomCounter<V extends Number> extends Binom<V> {
	private static final long serialVersionUID = -2329260083589546111L;
	static Map<Integer, Integer> counter;
	
	public BinomCounter(IntegralArithmetics<V> arithmetics, int n, int k) {
		super(arithmetics, n, k);
	}

	protected BinomNode createNode(int n, int k) {
		increment(n, k);
		return new BinomNode(n, k);
	}

	private void increment(int n, int k) {
		int key = n * (n + 1) / 2 + k;
		int value = 0;
		if (counter == null)
			counter = new HashMap<Integer, Integer>();
		if (counter.containsKey(key))
			value = counter.get(key);
		counter.put(key, value + 1);
	}
	
	public int getCount(int n, int k) {
		return counter.get(n * (n + 1) / 2 + k) + 1;
	}
	
	public static int getNodeCount() {
		return counter.size();
	}
	public static boolean hasAllOnes() {
		for (int count : counter.values())
			if (count > 1)
				return false;
		return true;
	}
}
