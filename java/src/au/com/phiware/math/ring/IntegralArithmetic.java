/**
 * 
 */
package au.com.phiware.math.ring;

import java.util.Set;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public interface IntegralArithmetic<V extends Number> extends RingArithmetic<V> {
	public V one();

	public Set<V> factors(V a);
	public V[] primeFactorization(V a);
	public V gcd(V a, V b);

	public boolean congruent(V a, V b, V mod);
	public V mod(V a, V b);
}
