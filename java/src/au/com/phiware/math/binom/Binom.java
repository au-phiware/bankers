/**
 * 
 */
package au.com.phiware.math.binom;

import java.lang.ref.SoftReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arit.IntegralArithmetics;
import arit.impl.LongArithmetics;

/**
 * Represents a portion of Pascal's triangle, whereby the value of the root of
 * the tree is the sum of it's two adjacent nodes and the leaf nodes are value
 * one. E.g. 
 *      0  1  2
 *  5:       10  <-- 4 choose 2 
 *          / |
 *  4:     4  6
 *       / | /|
 *  3:  1  3  3
 *       / | /|
 *  2:  1  2  1
 *       / |
 *  1:  1  1
 *   
 * @author Corin Lawson <me@corinlawson.com.au>
 *
 */
public class Binom<V extends Number> extends Number {
	private static final Logger log = LoggerFactory.getLogger(Binom.class);

	protected class BinomNode {
		V value;
		int n, k;
		SoftReference<BinomNode> up, next;
		BinomNode down, back;
		
		public BinomNode up() {
			if (up != null)
				return up.get();
			return null;
		}

		public BinomNode up(BinomNode up) {
			if (up == null)
				this.up = null;
			else if (this.up == null || up != this.up.get())
				this.up = new SoftReference<BinomNode>(up);
			return up;
		}

		public BinomNode next() {
			if (next != null)
				return next.get();
			return null;
		}

		public BinomNode next(BinomNode next) {
			if (next == null)
				this.next = null;
			else if (this.next == null || next != this.next.get())
				this.next = new SoftReference<BinomNode>(next);
			return next;
		}

		public BinomNode down() {
			return down;
		}

		public BinomNode down(BinomNode down) {
			return this.down = down;
		}

		public BinomNode back() {
			return back;
		}

		public BinomNode back(BinomNode back) {
			return this.back = back;
		}

		BinomNode(int n, int k) {
			log.debug("{} choose {}", n,k);
			this.n = n;
			this.k = k;
		}
	}
	IntegralArithmetics<V> arithmetics;
	BinomNode root;
	
	public Binom(IntegralArithmetics<V> arithmetics, int n, int k) {
		this(arithmetics);
		root = new BinomNode(n, k);
	}
	
	private Binom(IntegralArithmetics<V> arithmetics, BinomNode node) {
		this(arithmetics);
		root = node;
	}

	private Binom(IntegralArithmetics<V> arithmetics) {
		this.arithmetics = arithmetics;
	}

	V value() {
		if (root.value == null)
			buildNode(root);
		return root.value;
	}
	
	V sum() {
		if (root.value == null)
			buildNode(root);
		V sum = root.value;
		BinomNode step = root.back();
		if (step != null) {
			sum = arithmetics.add(sum, arithmetics.one());
			int k = step.k;
			while (step.k > 0) {
				sum = arithmetics.add(sum, arithmetics.shiftLeft(arithmetics.add(step.value, arithmetics.one()), k - step.k));
				step = step.back();
			}
		} else if (root.k == root.n) {
			sum = arithmetics.shiftLeft(arithmetics.one(), root.n);
		}
		return sum;
	}

	private V buildNode(BinomNode node) {
		if (node.n <= 1 || node.k == 0 || node.k == node.n)
			return node.value = arithmetics.one();
		
		BinomNode step;
		BinomNode down = node.down();
		BinomNode back = node.back();

		if (down == null && (step = node.next()) != null && (step = step.down()) != null && (step = step.back()) != null)
			down = step;
		if (down == null && (step = node.back()) != null && (step = step.down()) != null && (step = step.next()) != null)
			down = step;
		if (down == null)
			down = new BinomNode(node.n - 1, node.k);
		node.down(down);
		node.down.up(node);
		
		if (back == null && (step = node.up()) != null && (step = step.back()) != null && (step = step.down()) != null)
			back = step;
		if (back == null && (step = node.down()) != null && (step = step.back()) != null && (step = step.up()) != null)
			back = step;
		if (back == null)
			back = new BinomNode(node.n - 1, node.k - 1);
		node.back(back);
		node.back.next(node);

		if (back.value == null)
			buildNode(back);
		if (down.value == null)
			buildNode(down);

		node.value = arithmetics.add(down.value, back.value);
		log.debug("{} choose {} = {}", new Object[]{node.n,node.k, node.value});

		return node.value;
	}
	
	public Binom<V> next() {
		BinomNode step, node = root.next();
		if (node == null && (step = root.up()) != null && (step = step.next()) != null && (step = step.down()) != null)
			node = step;
		if (node == null && (step = root.down()) != null && (step = step.next()) != null && (step = step.up()) != null)
			node = step;
		if (node == null) {
			node = new BinomNode(root.n + 1, root.k + 1);
			if (root.down() != null)
				node.down(root.down().next());
			node.back(root);
		}
		return new Binom<V>(arithmetics, node);
	}

	public Binom<V> up() {
		BinomNode step, node = root.up();
		if (node == null && (step = root.next()) != null && (step = step.up()) != null && (step = step.back()) != null)
			node = step;
		if (node == null && (step = root.back()) != null && (step = step.up()) != null && (step = step.next()) != null)
			node = step;
		if (node == null) {
			node = new BinomNode(root.n + 1, root.k);
			if (root.back() != null)
				node.back(root.back().up());
			node.down(root);
		}
		return new Binom<V>(arithmetics, node);
	}

	public Binom<V> down() {
		if (root.n == root.k)
			return null;
		value();
		BinomNode node = root.down();
		if (node == null && root.n > 0)
			node = new BinomNode(root.n - 1, root.k);
		return new Binom<V>(arithmetics, node);
	}

	public Binom<V> back() {
		if (root.k == 0)
			return null;
		value();
		BinomNode node = root.back();
		if (node == null && root.n == root.k)
			node = new BinomNode(root.n - 1, root.k - 1);
		return new Binom<V>(arithmetics, node);
	}

	@Override
	public double doubleValue() {
		return value().doubleValue();
	}

	@Override
	public float floatValue() {
		return value().floatValue();
	}

	@Override
	public int intValue() {
		return value().intValue();
	}

	@Override
	public long longValue() {
		return value().longValue();
	}
	
	public static void main(String[] argv) {
		if (argv.length > 0) {
			int n = new Integer(argv[0]);
			IntegralArithmetics<Long> arithmetics = LongArithmetics.getInstance();
			if (argv.length > 1) {
				for (int i = 1; i < argv.length; i++) {
					Binom<Long> binom = new Binom<Long>(arithmetics, n, new Integer(argv[i]));
					System.out.printf("%d ", binom.longValue());
				}
			} else {
				for (int k = 0; k <= n; k++) {
					Binom<Long> binom = new Binom<Long>(arithmetics, n, k);
					System.out.printf("%d ", binom.longValue());
				}
			}
		}
	}
}
