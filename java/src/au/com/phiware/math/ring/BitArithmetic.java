/**
 * 
 */
package au.com.phiware.math.ring;

/**
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public interface BitArithmetic<V extends Number> extends IntegralArithmetic<V> {
	public boolean testBit(V a, int n);
	public V setBit(V a, int n);
	public V clearBit(V a, int n);
	public V flipBit(V a, int n);
	public int signum(V a);
	public V reverse(V a);
	
	public V or(V a, V b);
	public V and(V a, V b);
	public V nand(V a, V b);
	public V xor(V a, V b);
	public V not(V a);
	public V shiftLeft(V a, int n);
	public V shiftRight(V a, int n);
	
	public int maxBitLength();
	public int bitCount(V a);
	public int highestOneBit(V a);
	public int lowestOneBit(V a);
	
	public String toString(V a, int radix);
}
