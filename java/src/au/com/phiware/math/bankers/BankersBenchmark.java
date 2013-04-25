package au.com.phiware.math.bankers;

import au.com.phiware.math.ring.BitArithmetic;

//import com.github.ericburnett.EnumeratedSubsets;
//import java.math.BigInteger;
//import java.util.BitSet;

import org.junit.Test;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

public class BankersBenchmark extends AbstractBenchmark {
	static int length = 32;
	static Bankers<Integer> bankers;
	static {
		try {
			bankers = new Bankers<Integer>(length){};
		} catch (ClassNotFoundException ignored) {}
	}
	
	public <V extends Number> void testTo(Bankers<V> bankers) {
		BitArithmetic<V> a = bankers.getArithmetic();
		V i = a.zero();
		for (; a.bitCount(i) < bankers.length(); i = a.add(i, a.one())) {
			bankers.to(i);
		}
	}
	
	@BenchmarkOptions(callgc = false, benchmarkRounds = 1, warmupRounds = 1)
	@Test
	public void testPhimath() throws ClassNotFoundException {
		testTo(bankers);
	}

	/*
	@BenchmarkOptions(callgc = false, benchmarkRounds = 1, warmupRounds = 1)
	@Test
	public void testEricBurnett() throws ClassNotFoundException {
		int l = length;
            for (int k = 1; k <= l; ++k) {
                BigInteger i = BigInteger.ZERO;
                while (true) {
                    BitSet b = EnumeratedSubsets.GenerateSubset(l, k, i);
                    if (b == null) break;
                    i = i.add(BigInteger.ONE);
                }
            }
	}
	*/
}
