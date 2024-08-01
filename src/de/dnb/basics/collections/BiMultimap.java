/**
 *
 */
package de.dnb.basics.collections;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementiert im Falle von K==V einen ungerichteten Multigraphen, also einen Graphen mit
 * eventuell mehreren Verbindungen von Knoten A nach Knoten B.
 * <br>
 * Zur Instanziierung benutze {@link #createListMap()} oder {@link #createSetMap()}
 *
 * @author baumann
 *
 *
 * <br>
 *
 */
public class BiMultimap<K, V> implements IMultimap<K, V>, IBiMap<K, V>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 5807263668426425336L;
  private Multimap<K, V> map;
  private Multimap<V, K> inverse;

  @Override
  public void add(final K index, final V value) {
    map.add(index, value);
    inverse.add(value, index);
  }

  /**
   * Legt einen neuen Index an. Wenn keine Einträge zu index
   * hinzugefügt werden, dann entspricht das einem Endpunkt/Einzelpunkt im Graphen.
   *
   * @param index auch null
   */
  public void add(final K index) {
    map.add(index);
  }

  @Override
  public boolean containsKey(final K key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(final V value) {
    return inverse.containsKey(value);
  }

  @Override
  public void clear() {
    map.clear();
    inverse.clear();
  }

  @Override
  public Iterator<K> keysIterator() {
    return map.keysIterator();
  }

  @Override
  public Set<K> getKeySet() {
    return map.getKeySet();
  }

  @Override
  public Collection<V> get(final K key) {
    return map.getNullSafe(key);
  }

  /**
   *
   * @param key beliebig
   * @return  Werte ohne Wiederholungen
   */
  @Override
  public Set<V> getValueSet(final K key) {
    return new HashSet<>(get(key));
  }

  @Override
  public Iterator<V> valuesIterator() {
    return inverse.keysIterator();
  }

  public Collection<K> getKeys(final V value) {
    return inverse.getNullSafe(value);
  }

  @Override
  public Set<K> getKeySet(final V value) {
    return new HashSet<>(getKeys(value));
  }

  /**
   * @param args
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static void main(final String[] args) throws IOException, ClassNotFoundException {
    final BiMultimap<Integer, String> bimm = BiMultimap.createListMap();
    bimm.add(3);
    bimm.add(3, "a");
    bimm.add(3, "a");
    bimm.add(3, "a");
    bimm.add(2, "a");
    bimm.add(3, "b");
    bimm.add(3, "b");
    bimm.add(3, "b");
    bimm.add(3, "b");
    System.out.println(bimm);
    System.out.println();
    System.out.println(bimm.remove(3));
    System.out.println(bimm);
    System.out.println();
    System.out.println(bimm.remove(3, "a"));
    System.out.println(bimm);
    System.out.println();
    bimm.removeAll(3, "b");
    System.out.println(bimm);
    System.out.println();
    bimm.removeAll(3, "a");
    System.out.println(bimm.inverse);

  }

  /**
   * Auch Duplikate in der Datenbank möglich, bei Graphen mehr als eine Verbindung
   * zwischen den Knoten.
   *
   * @param <K> keys
   * @param <V> values
   * @return  neue Map
   */
  public static <K, V> BiMultimap<K, V> createListMap() {
    final BiMultimap<K, V> biMultimap = new BiMultimap<K, V>();
    biMultimap.map = new ListMultimap<>();
    biMultimap.inverse = new ListMultimap<>();
    return biMultimap;
  }

  /**
   * Keine Duplikate in der Datenbank, bei Graphen nur eine Verbindung
   * zwischen den Knoten.
   *
   * @param <K> keys
   * @param <V> values
   * @return  neue Map
   */
  public static <K, V> BiMultimap<K, V> createSetMap() {
    final BiMultimap<K, V> biMultimap = new BiMultimap<K, V>();
    biMultimap.map = new SetMultimap<>();
    biMultimap.inverse = new SetMultimap<>();
    return biMultimap;
  }

  private BiMultimap() {
  }

  @Override
  public void addAll(final K index, final Iterable<V> values) {
    map.addAll(index, values);
    values.forEach(value -> inverse.add(value, index));
  }

  @Override
  public void addAll(final K index, final V... values) {
    addAll(index, Arrays.asList(values));
  }

  /**
   *
   * @return  umgekehrte BiMUltimap. Diese ist nur eine Sicht auf
   *           die ursprünglichen Daten. Änderungen an der neuen
   *           Map wirken sich auch auf die alte aus.
   */
  @Override
  public BiMultimap<V, K> inverseMap() {
    final BiMultimap<V, K> inv = createListMap();
    inv.map = inverse;
    inv.inverse = map;
    return inv;
  }

  @Override
  public String toString() {
    return map.toString();
  }

  @Override
  public Set<V> getValueSet() {
    return inverse.getKeySet();
  }

  @Override
  public Collection<V> remove(final K key) {
    final Collection<V> values = map.getNullSafe(key);
    for (final V v : values) {
      removeAll(key, v);
    }
    return values;
  }

  /**
   *
   * @param key   beliebig
   * @param value beliebig
   * @return      true: Kante key - value war mindestens einmal vorhanden. Nach der Operation wurde
   *              eine Kante entfernt. Gehen von einem Punkt keine Kanten mehr aus, wird auch
   *              der Punkt entfernt.
   */
  public boolean remove(final K key, final V value) {
    Collection<V> values = map.getNullSafe(key);
    if (!values.contains(value))
      return false;
    else {
      values = map.map.get(key);
      values.remove(value);
      if (values.isEmpty())
        map.map.remove(key);
      final Collection<K> keys = inverse.map.get(value);
      keys.remove(key);
      if (keys.isEmpty())
        inverse.map.remove(value);
      return true;
    }
  }

  public void removeAll(final K key, final V value) {
    while (remove(key, value))
      ;
  }

}
