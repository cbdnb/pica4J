/**
 *
 */
package de.dnb.basics.tries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Ein Trie, der mehrfache (wiederholbare) Einträge
 * unter einem Schlüssel erlaubt.
 *
 * @author baumann
 *
 */
public class TrieMultimap<V> extends TST<List<V>> implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 3330092305677998190L;

  public void addValue(final String key, final V val) {
    if (containsKey(key)) {
      final List<V> list = get(key);
      list.add(val);
    } else {
      final List<V> list = new LinkedList<>();
      list.add(val);
      putValue(key, list);
    }
  }

  /**
   *
   * @param prefix  nicht null
   * @return        anders als bei {@link Trie#valuesWithPrefix(String)}
   *                wird keine Liste von Listen, sondern eine Liste von
   *                Werten zurückgegeben.
   */
  public Collection<V> flatValuesWithPrefix(final String prefix) {
    Objects.requireNonNull(prefix);
    final List<V> values = new ArrayList<>();
    final Collection<String> keys = keysWithPrefix(prefix);
    keys.forEach(key ->
    {
      final List<V> valCol = getValue(key);
      values.addAll(valCol);
    });
    return values;
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    keySet().forEach(key ->
    {
      buffer.append(key + ": " + getValue(key) + "\n");
    });
    return buffer.toString();
  }

  public List<V> getNullSafe(final String key) {
    final List<V> value = super.getValue(key);

    return value == null ? new LinkedList<>() : value;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final TrieMultimap<Integer> multimap = new TrieMultimap<>();
    multimap.addValue("a", 1);
    multimap.addValue("a", 1);
    multimap.addValue("a", 2);
    multimap.addValue("ab", 3);
    multimap.addValue("b", 1);
    System.out.println(multimap);
    System.out.println(multimap.flatValuesWithPrefix("a"));
    multimap.removeKey("a");
    System.out.println(multimap.getNullSafe("a"));
  }

}
