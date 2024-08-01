/**
 *
 */
package de.dnb.basics.tries;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Hilfsklasse, die einige sehr schlichte Implementierungen enthält.
 *
 * @author baumann
 *
 */
public class Tries {

  static <V> boolean equals(final Trie<V> trie, final Object o) {
    if (o == null)
      return false;
    if (trie == o)
      return true;

    if (!(o instanceof Map))
      return false;
    final Map<?, ?> m = (Map<?, ?>) o;
    if (m.size() != trie.size())
      return false;
    for (final String key : trie.keySet()) {
      if (!m.containsKey(key))
        return false;
      final Object mVal = m.get(key);
      final V myVal = trie.get(key);
      if (!myVal.equals(mVal))
        return false;
    }
    return true;
  }

  static <V> int hashCode(final Trie<V> trie) {
    int h = 0;
    final Iterator<Entry<String, V>> i = trie.entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  }

  static <V> boolean containsKey(final Trie<V> trie, final Object o) {
    if (o instanceof String) {
      return trie.contains((String) o);
    } else {
      return false;
    }
  }

  static <V> boolean containsValue(final Trie<V> trie, final Object o) {
    final Collection<V> values = values(trie);
    return values.contains(o);
  }

  static <V> Collection<V> values(final Trie<V> trie) {
    final ArrayList<V> vals = new ArrayList<>(trie.size());
    trie.forEach(e -> vals.add(e.getValue()));
    return vals;
  }

  static <V> Set<Map.Entry<String, V>> entrySet(final Trie<V> trie) {
    final Set<Map.Entry<String, V>> entries = new LinkedHashSet<>(trie.size());
    trie.forEach(entries::add);
    return entries;
  }

  static <V> V get(final Trie<V> trie, final Object key) {
    if (key instanceof String)
      return trie.getValue((String) key);
    return null;
  }

  static <V> V remove(final Trie<V> trie, final Object key) {
    if (key instanceof String) {
      final String k = (String) key;
      final V oldValue = trie.getValue(k);
      trie.removeKey(k);
      return oldValue;
    }
    return null;
  }

  static <V> V put(final Trie<V> trie, final String key, final V value) {
    final V oldvalue = trie.getValue(key);
    trie.putValue(key, value);
    return oldvalue;
  }

  static <V> void putAll(final Trie<V> trie, final Map<? extends String, ? extends V> map) {
    map.forEach((k, v) ->
    {
      put(trie, k, v);
    });
  }

  static <V> Iterator<Entry<String, V>> iterator(final Trie<V> trie) {

    final Collection<String> keys = trie.keySet();

    return new Iterator<Map.Entry<String, V>>() {

      private final Iterator<String> keysIt = keys.iterator();

      @Override
      public boolean hasNext() {
        return keysIt.hasNext();
      }

      @Override
      public Entry<String, V> next() {
        final String key = keysIt.next();
        final V value = trie.getValue(key);
        final Entry<String, V> entry = new AbstractMap.SimpleEntry<String, V>(key, value);
        return entry;
      }
    };
  }

  static <V> V getValueOfLongestPrefix(final Trie<V> trie, final String query) {
    final String longest = trie.longestPrefixOf(query);
    if (longest == null) {
      return null;
    }
    return trie.getValue(longest);
  }

  static <V> Collection<V> valuesWithPrefix(final Trie<V> trie, final String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException("calls keysWithPrefix() with null argument");
    }
    final List<V> values = new ArrayList<>();
    final Collection<String> keys = trie.keysWithPrefix(prefix);
    keys.forEach(key -> values.add(trie.getValue(key)));
    return values;
  }

  public static void main(final String[] args) {
    final Trie<Integer> trie = new RandomizedTST<>();
    trie.put("a", 1);
    trie.put("ab", 2);
    trie.put("abc", 3);
    trie.put("ac", 1);
    System.out.println(trie.valuesWithPrefix("a"));
    System.out.println(trie.keysWithPrefix("a"));
  }

}
