package de.dnb.basics.applicationComponents;

import java.math.BigInteger;
import java.util.function.Function;

/**
 * Eine Memory-Funktion. implements IFunction&lt;X, F>. Benutzt einen
 * LFU-Cache. Die Methode {@link LFUCachedFunction#calculate(Object)}
 * muss überschrieben werden.
 *
 * @author Christian_2
 *
 * @param <X>   Domain
 * @param <F>   Range
 */
public abstract class LFUCachedFunction<X, F> implements Function<X, F> {

    public static final int DEFAULT_MAX_SIZE = 1000;

    private final LFUCache<X, F> cache;

    /**
     * Cache-Größe: 1000, Prozentsatz der zu entfernenden: 80%
     */
    public LFUCachedFunction() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     *
     * @param maxCacheSize     > 0
     * @param evictionFactor   zwischen 0 und 1
     */
    public LFUCachedFunction(final int maxCacheSize, final float evictionFactor) {
        this.cache = new LFUCache<>(maxCacheSize, evictionFactor);
    }

    /**
     * Prozentsatz der zu entfernenden: 80%
     * @param maxCacheSize > 0
     */
    public LFUCachedFunction(final int maxCacheSize) {
        this(maxCacheSize, 0.8f);
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

    /**
     * Zu überschreibende Methode, die die eigentliche (aufwendige) Arbeit
     * macht. Um diese berechnungsintensive Methode zu entlasten, wird
     * der Cache verwendet.
     *
     * @param x
     * @return
     */
    protected abstract F calculate(X x);

    /**
     * Factory-Methode zum Einpacken einer Funktion in eine LFU-Funktion.
     * @param function  nicht null
     * @param   <X> Range
     * @param   <F> Domain
     * @return          neue Funktion
     */
    public static <X, F> Function<X, F> create(
        final Function<X, F> function) {
        return new LFUCachedFunction<X, F>() {
            @Override
            protected F calculate(final X x) {
                return function.apply(x);
            }
        };
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {

        final Function<BigInteger, BigInteger> function2 =
            new Function<BigInteger, BigInteger>() {
                @Override
                public BigInteger apply(final BigInteger x) {
                    if (x.compareTo(BigInteger.ONE) < 0)
                        return BigInteger.ONE;
                    else
                        return this.apply(x.subtract(BigInteger.ONE)).multiply(
                            x);
                }
            };
        final Function<BigInteger, BigInteger> function = create(function2);

        for (int i = 1; i <= 1000; i++) {
            System.out.println(function2.apply(BigInteger.valueOf(i)));
        }

        System.out.println();
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 1; i <= 1000; i++) {
            System.out.println(function.apply(BigInteger.valueOf(i)));
        }

    }
}
