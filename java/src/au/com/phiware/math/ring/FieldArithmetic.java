/**
 * 
 */
package au.com.phiware.math.ring;

import java.util.Set;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public interface FieldArithmetic<V extends Number> extends RingArithmetic<V> {
	public V one();
	public V divide(V a, V b);
	public V root(V a, V b);
}
