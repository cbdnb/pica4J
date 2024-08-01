/**
 *
 */
package de.dnb.basics.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import de.dnb.basics.applicationComponents.strings.StringUtils;

/**
 * Implementation of Multimap that uses a {@link BoundedPriorityQueue} to store
 * the values for a given key. A {@link HashMap} associates each key with a
 * {@link BoundedPriorityQueue} of values.
 *
 * This multimap allows duplicate key-value pairs. After adding a new key-value
 * pair equal to an existing key-value pair, the PriorityMultimap can contain
 * entries for both the new value and the old value, if the
 * BoundedPriorityQueue's capacity allows this. Keys and values cannot be null.
 *
 * @author Christian_2
 *
 * @param <K>
 * @param <V>
 */
public class PriorityMultimap<K, V> extends Multimap<K, V> implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -5011925643362709114L;
  private final int capacity;
  private final Comparator<? super V> comparator;

  public PriorityMultimap(final int capacity) {
    super(new HashMap<K, Collection<V>>());
    this.capacity = capacity;
    comparator = null;
  }

  /**
   * @param values
   * @param capacity
   * @param comparator
   */
  public PriorityMultimap(final int capacity, final Comparator<? super V> comparator) {
    super(new HashMap<K, Collection<V>>());
    this.capacity = capacity;
    this.comparator = comparator;
  }

  @Override
  protected Collection<V> getNewValueCollection() {
    return new BoundedPriorityQueue<>(capacity, comparator);
  }

  @Override
  public String toString() {
    String s = "";
    for (final Iterator<K> iterator = keysIterator(); iterator.hasNext();) {
      final K k = iterator.next();
      final BoundedPriorityQueue<V> kValues = (BoundedPriorityQueue<V>) get(k);
      final String listAsString = StringUtils.concatenate("\t", kValues.ordered());
      s += k + "\t" + listAsString;
      if (iterator.hasNext())
        s += "\n";
    }
    return s;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
  }

}
