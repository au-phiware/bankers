/**
 * 
 */
package au.com.phiware.math.ring;

import java.util.Comparator;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public interface RingArithmetic<V extends Number> extends Comparator<V> {
	public V zero();
	public V add(V a, V b);
	public V negate(V a);
	public V subtract(V a, V b);
	public V multiply(V a, V b);
	public V pow(V a, V b);

	public V max(V a, V b);
	public V min(V a, V b);
}
