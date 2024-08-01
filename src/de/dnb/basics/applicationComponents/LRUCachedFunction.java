package de.dnb.basics.applicationComponents;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class LRUCachedFunction<X, F> implements Function<X, F> {

	public static final int DEFAULT_MAX_SIZE = 1000;

	private final Map<X, F> cache;

	public LRUCachedFunction() {
		this(DEFAULT_MAX_SIZE);
	}

	public LRUCachedFunction(final int maxSize) {

		this.cache =
			Collections
				.synchronizedMap(new LinkedHashMap<X, F>(maxSize + 1,
					.75F, true) {

					private static final long serialVersionUID =
						-7593682543810266797L;

					// diese Funktion kann vom Benutzer überschrieben werden,
					// um ein Cache zu implementieren.
					@Override
					protected boolean removeEldestEntry(
						final Map.Entry<X, F> eldest) {
						return size() > maxSize;
					}
				});
	}

	@Override
	public synchronized F apply(final X x) {
		F value = cache.get(x);
		if (value == null) {
			value = calculate(x);
			cache.put(x, value);
		}
		return value;
	}

	protected abstract F calculate(X x);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final LRUCachedFunction<BigInteger, BigInteger> function =
				new LRUCachedFunction<BigInteger, BigInteger>(4) {
					@Override
					protected BigInteger calculate(final BigInteger x) {
						if (x.compareTo(BigInteger.ONE) < 0)
							return BigInteger.ONE;
						else
							return this.apply(x.subtract(BigInteger.ONE))
								.multiply(x);
					}
				};

			for (int i = 1; i <= 100; i++) {
				System.out.println(function.apply(BigInteger.valueOf(i)));
				System.out.println(function.cache.keySet());
			}

	}

}
