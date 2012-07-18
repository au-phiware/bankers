package au.com.phiware.math.binom;

import au.com.phiware.math.ring.BitArithmetic;

public final class Binomials {
	public static <V extends Number> BinomFactory<V> defaultBinomFactory(BitArithmetic<V> arithmetic) {
		return new BinomGraph<V>(arithmetic);
	}
}
