/**
 *
 */
package de.dnb.basics.tries;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * Experimentell gewonnene Ergebnisse. Zeiten und
 * Speicherverbrauch sind nur relativ zu verstehen.
 * <p>
 * <b>Für geordnete Liste (DDC-Nummern):</b>
 * <p>
 * RandomizedTST:
 * <ul>
 * put: 5.4, Suche: 3.8, delete: 5.2,Memory: 2.8
 * </ul>
 * TST:
 * <ul>
 * put: 7.8, Suche: 6.1, delete: 8.3, Memory: 2.2
 * </ul>
 * OrderedTrie:
 * <ul>
 * put: 9.9, Suche: 5.9, delete: 11.0, Memory: 7.8
 * </ul>
 * AlphabetTrie:
 * <ul>
 * put: 5.0, Suche: 5.1, delete: 5.6, Memory: 7.3
 *</ul>
 *
 *<p>
 * <b>Für ungeordnete Liste (Sheakespeare-Dramen):</b>
 * <p>
 * RandomizedTST
 * <ul>put: 3.1, Suche: 2.4, delete: 2.5, Memory: 103</ul>
 * TST
 * <ul>put: 2.3, Suche: 2.2, delete: 2.3, Memory: 83</ul>
 * OrderedTrie
 * <ul>put: 5.5, Suche: 4.9, delete: 4.2, Memory: 292</ul>
 *
 * @author baumann
 *
 * @param <V>
 */
public interface Trie<V> extends Map<String, V>, Iterable<Map.Entry<String, V>>, Serializable {

  /**
   * Returns the value associated with the given key.
   * @param key   the key (not null)
   * @return      the value associated with the given key if the key is
   *              in the symbol table
   *              and {@code null} if the key is not in the symbol table
   * @throws      NullPointerException if {@code key} is {@code null}
   */
  V getValue(String key);

  /**
   * Does this symbol table contain the given key?
   * @param       key the key
   * @return      {@code true} if this symbol table contains {@code key} and
   *              {@code false} otherwise
   * @throws      NullPointerException if {@code key} is {@code null}
   */
  boolean contains(String key);

  /**
   * Inserts the key-value pair into the symbol table,
   * overwriting the old value
   * with the new value if the key is already in the symbol table.
   * If the value is {@code null}, this effectively deletes the key
   * from the symbol table.
   *
   * @param key   the key, not null, not empty
   * @param val   the value
   * @throws      NullPointerException if {@code key} is {@code null}
   */
  void putValue(String key, V val);

  /**
   * Returns all of the keys in the set that start with {@code prefix}.
   * @param   prefix the prefix, not null
   * @return  all of the keys in the set that start with {@code prefix},
   *          as a collection, not null
   */
  Collection<String> keysWithPrefix(String prefix);

  /**
   *
   * @param prefix  nicht null
   * @return        alle werte zu Schlüsseln, die mit {@code prefix}
   *                starten.
   */
  Collection<V> valuesWithPrefix(String prefix);

  /**
   * Returns all of the keys in the symbol table that match {@code pattern},
   * where . symbol is treated as a wildcard character.
   * @param pattern the pattern
   * @return all of the keys in the symbol table that match {@code pattern},
   *     as a collection, where . is treated as a wildcard character.
   */
  Collection<String> keysThatMatch(String pattern);

  /**
   * Returns the string in the symbol table that is
   * the longest prefix of {@code query},
   * or {@code null}, if no such string.
   * @param   query the query string
   * @return  the string in the symbol table
   *          that is the longest prefix of {@code query},
   *          or {@code null} if no such string
   * @throws  NullPointerException if {@code query} is {@code null}
   */
  String longestPrefixOf(String query);

  /**
   *
   * @param query nicht {@code null}
   * @return      gibt es ein Präfix für diesen String?
   * @throws  NullPointerException if {@code query} is {@code null}
   */
  default boolean containsPrefixFor(final String query) {
    return longestPrefixOf(query) != null;

  }

  /**
   *
   * @param query Suchfrage
   * @return      Den Wert der im längsten Präfix
   *              gespeichert ist oder null
   */
  V getValueOfLongestPrefix(String query);

  /**
   * Removes the key from the set if the key is present.
   * @param key the key
   * @throws NullPointerException if {@code key} is {@code null}
   */
  void removeKey(String key);

  /**
   *
   * @return  einen sequentiellen Stream.
   */
  Stream<Entry<String, V>> stream();

}
