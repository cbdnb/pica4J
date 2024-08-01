/**
 *
 */
package de.dnb.basics.collections;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Eine Bijektive Abbildung. Beim Einfügen mittels
 * {@link BiMap#put(Object, Object)} wird eine neue
 * Abbildung hergestellt, die alte gelöscht!
 *
 * @author baumann
 *
 */
public class BiMap<K, V> implements Map<K, V>, IBiMap<K, V>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -1624956918533645295L;
  Map<K, V> map = new HashMap<>();
  Map<V, K> inverse = new HashMap<>();

  /**
   * @param args
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static void main(final String[] args) throws IOException, ClassNotFoundException {
    final BiMap<String, Integer> map = new BiMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put(null, 2);
    System.out.println(map.toTable());
    System.out.println(map.getKey(1));
    System.out.println(map.get("a"));
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return inverse.containsKey(value);
  }

  @Override
  public V get(final Object key) {
    return map.get(key);
  }

  @Override
  public V put(final K key, final V value) {
    if (map.containsKey(key)) {
      inverse.remove(map.get(key));
    }
    if (inverse.containsKey(value)) {
      map.remove(inverse.get(value));
    }
    final V obj = map.put(key, value);
    inverse.put(value, key);
    return obj;
  }

  @Override
  public V remove(final Object key) {
    V value = null;
    if (map.containsKey(key)) {
      value = map.remove(key);
      inverse.remove(value);
    }
    return value;
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    m.forEach((k, v) -> put(k, v));
  }

  @Override
  public void clear() {
    map.clear();
    inverse.clear();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    return inverse.keySet();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  @Override
  public boolean equals(final Object obj) {
    return map.equals(obj);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public String toString() {
    return map.toString();
  }

  /**
   *
   * @param value beliebig
   * @return      key oder null
   */
  public K getKey(final Object value) {
    return inverse.get(value);
  }

  public K removeValue(final Object value) {
    K key = null;
    if (inverse.containsKey(value)) {
      key = inverse.remove(value);
      map.remove(key);
    }
    return key;
  }

  @Override
  public void add(final K index, final V value) {
    put(index, value);
  }

  @Override
  public Iterator<K> keysIterator() {
    return keySet().iterator();
  }

  @Override
  public Set<V> getValueSet(final K key) {
    final V value = get(key);
    return value == null ? Collections.emptySet() : Collections.singleton(value);
  }

  @Override
  public Iterator<V> valuesIterator() {
    return values().iterator();
  }

  @Override
  public Set<K> getKeySet(final V value) {
    final K key = getKey(value);
    return key == null ? Collections.emptySet() : Collections.singleton(key);
  }

  @Override
  public BiMap<V, K> inverseMap() {
    final BiMap<V, K> inv = new BiMap<>();
    inv.map = inverse;
    inv.inverse = map;
    return inv;
  }

  @Override
  public Set<V> getValueSet() {
    return inverse.keySet();
  }

  @Override
  public Set<K> getKeySet() {
    return keySet();
  }

}
