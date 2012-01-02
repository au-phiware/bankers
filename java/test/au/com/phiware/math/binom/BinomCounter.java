/**
 * 
 */
package au.com.phiware.math.binom;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class BinomCounter {
	private static Map<Integer, Integer> counter;
	
	public static void increment(Integer n, Integer k) {
		if (n > 0 && k > 0) {
			int key = n * (n + 1) / 2 + k;
			int value = 0;
			if (counter == null)
				counter = new HashMap<Integer, Integer>();
			if (counter.containsKey(key))
				value = counter.get(key);
			counter.put(key, value + 1);
		}
	}
	
	public static int getNodeCount() {
		if (counter == null)
			return 0;
		return counter.size();
	}
	public static boolean hasAllOnes() {
		if (counter != null)
			for (int count : counter.values())
				if (count > 1)
					return false;
		return true;
	}

	public static void resetCounter() {
		if (counter != null)
			counter.clear();
	}
}
