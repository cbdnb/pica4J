/**
 *
 */
package de.dnb.basics.collections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.dnb.basics.applicationComponents.MyFileUtils;

/**
 * Implementation of Multimap that uses an TreeSet to store the values for a
 * given key. A TreeMap associates each key with an TreeSet of values.
 *
 * This multimap allows no duplicate key-value pairs.
 *  After adding a new
 * key-value pair equal to an existing key-value pair, the TreeMultimap
 * will contain entries only for the old value.
 * Keys and values are not allowed to be null.
 *
 * @author Christian_2
 *
 * @param <K>
 * @param <V>
 */
public class TreeMultimap<K extends Comparable<K>, V extends Comparable<V>> extends Multimap<K, V>
  implements Serializable {

  @Override
  protected Collection<V> getNewValueCollection() {
    return new TreeSet<>(valueComparator);
  }

  /**
   *
   */
  private static final long serialVersionUID = 5189502140941869729L;

  private final Comparator<V> valueComparator;

  public TreeMultimap() {
    super(new TreeMap<K, Collection<V>>());
    valueComparator = null;
  }

  public TreeMultimap(final Comparator<K> keyComparator, final Comparator<V> valueComparator) {
    super(new TreeMap<K, Collection<V>>(keyComparator));
    this.valueComparator = valueComparator;
  }

  /**
   * Parameterlos wegen Serialisierung.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public TreeMultimap(final String fileName) throws IOException, ClassNotFoundException {
    this();
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final TreeMultimap<K, V> readObject = (TreeMultimap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    addAll(readObject);
  }

  /**
   * Returns the least key greater than or
   * equal to the given key, or null if there is no such key.
   * @param key the key
   * @return    the least key greater than or equal to key, or
   *            null if there is no such key
   * @throws ClassCastException
   *            if the specified key cannot be compared with the
   *            keys currently in the map
   * @throws NullPointerException
   *            if the specified key is null and this map uses
   *            natural ordering, or its comparator does not permit null keys
   */
  public K ceilingKey(final K key) {
    return ((TreeMap<K, Collection<V>>) map).ceilingKey(key);
  }

  /**
   * Returns the greatest key less than or equal to the given key,
   * or null if there is no such key.
   * @param key the key
   * @return    Returns the greatest key less than or equal to the
   *            given key, or null if there is no such key
   * @throws ClassCastException
   *            if the specified key cannot be compared with the
   *            keys currently in the map
   * @throws NullPointerException
   *            if the specified key is null and this map uses
   *            natural ordering, or its comparator does not permit null keys
   */
  public K floorKey(final K key) {
    return ((TreeMap<K, Collection<V>>) map).floorKey(key);
  }

  /**
   * Returns a reverse order NavigableSet view of the keys contained in
   * this map. The set's iterator returns the keys in descending order.
   * The set is backed by the map, so changes to the map are reflected
   * in the set, and vice-versa. If the map is modified while an iteration
   * over the set is in progress (except through the iterator's own remove
   * operation), the results of the iteration are undefined. The set
   * supports element removal, which removes the corresponding mapping
   * from the map, via the Iterator.remove, Set.remove, removeAll, retainAll,
   * and clear operations. It does not support the add or addAll operations.
   *
   * @return a reverse order navigable set view of the keys in this map
   */
  public NavigableSet<K> descendingKeySet() {
    return ((TreeMap<K, Collection<V>>) map).descendingKeySet();
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final TreeMultimap<String, String> multimap =
      new TreeMultimap<>(String.CASE_INSENSITIVE_ORDER, String.CASE_INSENSITIVE_ORDER);
    multimap.add("F");
    multimap.add("A", "x");
    multimap.add("A", "Y");
    multimap.add("A", "z");
    multimap.add("A", "X");

    multimap.add("b", "1");
    multimap.add("b", "2");

    multimap.add("C", "1");

    multimap.add("A");

    System.out.println(multimap);
    System.out.println();

    System.out.println(multimap.ceilingKey("D"));

    System.out.println();

    try {
      multimap.safe("d:/temp/mu.out");
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("-------------");
    try {
      final TreeMultimap<String, String> mm = new TreeMultimap<>("d:/temp/mu.out");
      System.out.println(mm);
    } catch (ClassNotFoundException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
