/**
 *
 */
package de.dnb.basics.tries;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import de.dnb.basics.applicationComponents.Streams;

/**
 * Implementiert einige Methoden.
 *
 * @author baumann
 *
 * @param <V>   Typ
 */
public abstract class AbstractTrie<V> implements Trie<V>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -6053341311922185549L;

  @Override
  public abstract V getValue(String key);

  /**
   * Does this symbol table contain the given key?
   * @param       key the key
   * @return      {@code true} if this symbol table contains {@code key} and
   *              {@code false} otherwise
   * @throws      NullPointerException if {@code key} is {@code null}
   */
  @Override
  public boolean contains(final String key) {
    return getValue(key) != null;
  }

  @Override
  public abstract void putValue(String key, V val);

  @Override
  public abstract int size();

  @Override
  public abstract void clear();

  @Override
  public final boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public abstract Set<String> keySet();

  @Override
  public abstract Collection<String> keysWithPrefix(String prefix);

  @Override
  public abstract Collection<String> keysThatMatch(String pattern);

  @Override
  public abstract String longestPrefixOf(String query);

  @Override
  public final V getValueOfLongestPrefix(final String query) {
    return Tries.getValueOfLongestPrefix(this, query);

  }

  @Override
  public abstract void removeKey(String key);

  @Override
  public final Stream<Entry<String, V>> stream() {
    return Streams.getStreamFromIterable(this);
  }

  @Override
  public final Iterator<Entry<String, V>> iterator() {
    return Tries.iterator(this);
  }

  @Override
  public final boolean containsKey(final Object key) {
    return Tries.containsKey(this, key);
  }

  @Override
  public final boolean containsValue(final Object value) {
    return Tries.containsValue(this, value);
  }

  @Override
  public final Set<java.util.Map.Entry<String, V>> entrySet() {
    return Tries.entrySet(this);
  }

  @Override
  public final V get(final Object key) {
    return Tries.get(this, key);
  }

  @Override
  public final V remove(final Object key) {
    return Tries.remove(this, key);
  }

  @Override
  public final V put(final String key, final V value) {
    return Tries.put(this, key, value);
  }

  @Override
  public final Collection<V> values() {
    return Tries.values(this);
  }

  /* (non-Javadoc)
   * @see java.util.Map#putAll(java.util.Map)
   */
  @Override
  public final void putAll(final Map<? extends String, ? extends V> map) {
    Tries.putAll(this, map);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(final Object obj) {
    return Tries.equals(this, obj);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Tries.hashCode(this);
  }

  @Override
  public Collection<V> valuesWithPrefix(final String prefix) {
    return Tries.valuesWithPrefix(this, prefix);
  }

}
