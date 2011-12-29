/**
 * 
 */
package au.com.phiware.math.binom;

import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.phiware.math.ring.ArithmeticFactory;
import au.com.phiware.math.ring.BigIntegerArithmetic;
import au.com.phiware.math.ring.BitArithmetic;
import au.com.phiware.math.ring.IntegerArithmetic;
import au.com.phiware.math.ring.LongArithmetic;

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
	private static final long serialVersionUID = -8905223911595724921L;
	private static final Logger log = LoggerFactory.getLogger(Binom.class);

	public static <V extends Number> Binom<V> createBinom(V n, V k) throws ClassNotFoundException {
		BitArithmetic<V> arithmetic = ArithmeticFactory.getBitArithmeticForNumber(n);
		return new Binom<V>(arithmetic, n.intValue(), k.intValue());
	}
	
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
			this.n = n;
			this.k = k;
		}
	}
	
	protected BinomNode createNode(int n, int k) {
		log.debug("{} choose {}", n,k);
		return new BinomNode(n, k);
	}
	
	BitArithmetic<V> arithmetics;
	BinomNode root;
	boolean folded = false;
	
	public Binom(BitArithmetic<V> arithmetics, int n, int k) {
		this(arithmetics);
		if (n < 0 || k < 0 || k > n)
			throw new IllegalArgumentException(MessageFormat.format("Undefined value for n = {0} and k = {1}.", n, k));

		if (k > n / 2) {
			root = createNode(n, n - k);
			folded = true;
		} else
			root = createNode(n, k);
	}
	
	private Binom(BitArithmetic<V> arithmetics, BinomNode node, boolean folded) {
		this(arithmetics);
		root = node;
		this.folded = folded;
	}

	private Binom(BitArithmetic<V> arithmetics) {
		this.arithmetics = arithmetics;
	}

	V value() {
		if (root.value == null)
			buildNode(root);
		return root.value;
	}
	
	private V       one()           { return arithmetics.one      ();     }
	private V       add(V a, V   b) { return arithmetics.add      (a, b); }
	private V shiftLeft(V a, int b) { return arithmetics.shiftLeft(a, b); }
	
	V sum() {
		if (root.value == null)
			buildNode(root);
		V sum = root.value;
		boolean folded = this.folded;
		if (root.k == (folded ? 0 : root.n))
			sum = shiftLeft(one(), root.n);
		else if (root.k != 0) {
			BinomNode step = folded ? downNode(root) : root.back();
			if (step != null) {
				sum = add(sum, one());
				int i = 0,
				    max = folded ? step.n - step.k : step.k;
				while (i < max) {
					if (step.n % 2 == 0 && step.k == step.n / 2) folded = false;
					sum = add(sum, shiftLeft(add(step.value, one()), i++));
					step = folded ? downNode(step) : step.back();
				}
			}
		}

		return sum;
	}

	private V buildNode(BinomNode node) {
		if (node.n <= 1 || node.k == 0 || node.k == node.n)
			return node.value = one();
		log.debug("{} choose {} = ?", new Object[]{node.n,node.k});
		
		node.back(backNode(node));
		if (node.k > (node.n - 1) / 2)
			node.down(backNode(node));
		else
			node.down(downNode(node));
		
		if (node.back.value == null)
			buildNode(node.back);
		if (node.down.value == null)
			buildNode(node.down);

		node.value = add(node.down.value, node.back.value);
		log.debug("{} choose {} = {}", new Object[]{node.n, node.k, node.value});

		return node.value;
	}
	
	private BinomNode backNode(BinomNode root) {
		BinomNode step, node = root.back();
		if (node == null) {
			if (root.k == 0)
				return null;
			if (root.n == root.k)
				node = createNode(root.n - 1, root.k - 1);
			if (node == null && (step = root.up()) != null && (step = step.back()) != null && (step = step.down()) != null)
				node = step;
			if (node == null && (step = root.down()) != null && (step = step.back()) != null && (step = step.up()) != null)
				node = step;
			if (node == null)
				node = createNode(root.n - 1, root.k - 1);
			if (root.up() != null && root.up().back() != null)
				node.up(root.up().back());
			node.next(root);
		}
		return node;
	}
	private BinomNode downNode(BinomNode root) {
		BinomNode step, node = root.down();
		if (node == null) {
			if (root.n == root.k || root.n <= 0)
				return null;
			if (root.k == 0 && root.n > 0)
				return createNode(root.n - 1, root.k);
			if (node == null && (step = root.next()) != null && (step = step.down()) != null)
				if ((step = (root.k + 1 > root.n / 2) ? step.down() : step.back()) != null)
					node = step;
			if (node == null && (step = root.back()) != null && (step = step.down()) != null && (step = step.next()) != null)
				node = step;
			if (node == null)
				node = createNode(root.n - 1, root.k);
			if (root.next() != null && root.next().down() != null)
				node.next(root.next().down());
			node.up(root);
		}
		return node;
	}
	private BinomNode nextNode(BinomNode root) {
		BinomNode step, node = root.next();
		if (node == null && (step = root.up()) != null && (step = step.next()) != null && (step = step.down()) != null)
			node = step;
		if (node == null && (step = root.down()) != null && (step = step.next()) != null && (step = step.up()) != null)
			node = step;
		if (node == null) {
			node = createNode(root.n + 1, root.k + 1);
			if (root.down() != null && root.down().next() != null)
				node.down(root.down().next());
			node.back(root);
		}
		return node;
	}
	private BinomNode upNode(BinomNode root) {
		BinomNode step, node = root.up();
		if (node == null && (step = root.next()) != null && (step = step.up()) != null && (step = step.back()) != null)
			node = step;
		if (node == null && (step = root.back()) != null && (step = step.up()) != null && (step = step.next()) != null)
			node = step;
		if (node == null) {
			node = createNode(root.n + 1, root.k);
			if (root.back() != null && root.back().up() != null)
				node.back(root.back().up());
			node.down(root);
		}
		return node;
	}

	public Binom<V> back() {
		if (folded && root.k - 1 > (root.n - 1) / 2)
			return new Binom<V>(arithmetics, downNode(root), false);
		else if (folded)
			return new Binom<V>(arithmetics, downNode(root), true);
		else 
			return new Binom<V>(arithmetics, backNode(root), false);
	}
	public Binom<V> down() {
		if (folded || root.k > (root.n - 1) / 2)
			return new Binom<V>(arithmetics, backNode(root), true);
		else
			return new Binom<V>(arithmetics, downNode(root), false);
	}
	public Binom<V> next() {
		if (folded || root.k + 1 > (root.n + 1) / 2)
			return new Binom<V>(arithmetics, upNode(root), true);
		else
			return new Binom<V>(arithmetics, nextNode(root), false);
	}
	public Binom<V> up() {
		return new Binom<V>(arithmetics, folded ? nextNode(root) : upNode(root), folded);
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
			BitArithmetic<Long> arithmetic = LongArithmetic.getInstance();
			if (argv.length > 1) {
				for (int i = 1; i < argv.length; i++) {
					Binom<Long> binom = new Binom<Long>(arithmetic, n, new Integer(argv[i]));
					System.out.printf("%d %d\n", binom.longValue(), binom.sum().longValue());
				}
			} else {
				for (int k = 0; k <= n; k++) {
					Binom<Long> binom = new Binom<Long>(arithmetic, n, k);
					System.out.printf("%d %d\n", binom.longValue(), binom.sum().longValue());
				}
			}
		}
	}
}
