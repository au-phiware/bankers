package au.com.phiware.math.binom;

public interface Binom<V extends Number> {

	public abstract V value();

	public abstract V sum();

	public abstract Binom<V> back();

	public abstract Binom<V> down();

	public abstract Binom<V> next();

	public abstract Binom<V> up();

	/**
	 * Perform down then next movement.
	 * @return the result of this.down().next()
	 */
	public abstract Binom<V> right();

	/**
	 * Perform back then up movement.
	 * @return the result of this.back().up()
	 */
	public abstract Binom<V> left();

	public abstract double doubleValue();

	public abstract float floatValue();

	public abstract int intValue();

	public abstract long longValue();

	public abstract int getRow();

	public abstract int getColumn();

}