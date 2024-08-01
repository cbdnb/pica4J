package de.dnb.basics.collections;

import java.util.Collection;
import java.util.Collections;

/**
 * Eine {@link Multimap}, die als Schlüssel Cartesische Produkte enthält.
 * <br>(a, b, c, ...) -> v
 * <br> Beim Einfügen werden zu jeder Schlüsselkombination zufällige
 * V-Werte ausgewählt. Das geschieht, in dem zu jeder Schlüsselkombination ein
 * eigenes {@link ReservoirSample} angelegt wird.
 */
public class SamplingMultimap<V> extends CrossProductMultimap<V> {

  private static final long serialVersionUID = 1845659204473141518L;
  private final int sampleSize;

  public SamplingMultimap(final int sampleSize) {
    this.sampleSize = sampleSize;
  }

  @Override
  protected Collection<V> getNewValueCollection() {
    return new ReservoirSample<>(sampleSize);
  }

  @Override
  public Collection<V> get(final Collection<? extends Object> key) {
    final Collection<V> collectionForKey = map.get(key);
    if (collectionForKey == null)
      return null;
    else {
      return Collections.unmodifiableCollection(collectionForKey);
    }
  }

  public static void main(final String[] args) {
    final SamplingMultimap<Integer> multimap = new SamplingMultimap<>(50);
    for (int i = 0; i < 10000000; i++) {

      multimap.addValue(i % 10, "a");
      multimap.addValue(i % 10, "b");
      multimap.addValue(i % 10, "a", "b");
      multimap.addValue(i % 10, "a", "b", "c");
      multimap.addValue(i % 10, "a", "b", "d");

    }

    for (final Collection<? extends Object> collection : multimap) {
      final Collection<Integer> ints = multimap.get(collection);
      System.out.println(collection);
      System.out.println(ListUtils.average(ints));
    }

  }

}
