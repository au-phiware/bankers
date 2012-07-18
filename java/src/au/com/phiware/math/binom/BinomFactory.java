package au.com.phiware.math.binom;

import au.com.phiware.math.ring.BitArithmetic;

public interface BinomFactory<V extends Number> {

	public abstract Binom<V> createBinom(int n, int k);

	public abstract BitArithmetic<V> getArithmetic();

}
