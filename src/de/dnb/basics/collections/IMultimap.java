/**
 *
 */
package de.dnb.basics.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author baumann
 *
 * @param <K> keys
 * @param <V> values
 */
public interface IMultimap<K, V> {

  /**
   * Füge weiteren Wert zum Schlüssel hinzu.
   *
   * @param index		kann je nach Implementierung auch null sein
   * @param value		kann je nach Implementierung auch null sein
   */
  void add(K index, V value);

  /**
   *
   * @param key	beliebig
   * @return		key in Multimap enthalten. Das ist auch dann der Fall,
   * 				wenn über Aufruf von {@link #add(Object)} eine
   * 				leere Value-Collection erzeugt wurde
   */
  boolean containsKey(K key);

  boolean containsValue(V value);

  /**
   * Zurücksetzen.
   */
  void clear();

  /**
   * Iterator über Schlüssel.
   *
   * @return	nicht null
   */
  Iterator<K> keysIterator();

  /**
   *
   * @return  nicht null
   */
  Set<K> getKeySet();

  /**
   *
   * Gibt eine neue Collection der Werte zu key. Diese kann verändert
   * werden. Vorsicht ist geboten, wenn der Zustand Werte selbst
   * verändert wird.
   *
   * @param key	Schlüssel
   * @return		Die Werte, die zum Schlüssel gehören (das kann auch eine
   * 				leere Collection sein) oder null, wenn kein
   * 				Mapping vorliegt
   */
  Collection<V> get(K key);

  /**
   *
   * @return 	Iterator über alle Werte. Kommt ein Wert in mehreren
   * 			Collections vor, so wird er auch bei Iteration
   * 			mehrfach erfasst.
   */
  Iterator<V> valuesIterator();

  /**
   * Fügt unter dem index die values hinzu. Ist values leer:
   * keine Wirkung.
   *
   * @param index   kann je nach Implementierung auch null sein
   * @param values  nicht null
   */
  void addAll(final K index, final Iterable<V> values);

  /**
   * Fügt die Werte der Multimap hinzu. Ist die Wertemenge leer:
   * keine Wirkung.
   *
   * @param index     kann je nach Implementierung auch null sein
   * @param values    nicht null
   */
  void addAll(final K index, final V... values);

  /**
   * Removes the mapping for a key from this map if it is present (optional operation).
   * More formally, if this map contains a mapping from key k to value v such that
   * Objects.equals(key, k), that mapping is removed. (The map can contain at most one such mapping.)
   *
   * Returns the value to which this map previously associated the key, or null if the
   * map contained no mapping for the key.
   * If this map permits null values, then a return value of null does not necessarily indicate
   * that the map contained no mapping for the key; it's also possible that the map explicitly
   * mapped the key to null.
   *
   * The map will not contain a mapping for the specified key once the call returns.
   *
   * @param key
   * @return
   */
  Collection<V> remove(K key);

}
