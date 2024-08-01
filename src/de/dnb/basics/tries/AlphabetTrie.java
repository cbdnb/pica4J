/******************************************************************************
 *  Compilation:  javac TrieST.java
 *  Execution:    java TrieST < words.txt
 *  Dependencies: StdIn.java
 *  Data files:   http://algs4.cs.princeton.edu/52trie/shellsST.txt
 *
 *  A string symbol table for extended ASCII strings, implemented
 *  using a 256-way trie.
 *
 *  % java TrieST < shellsST.txt
 *  by 4
 *  sea 6
 *  sells 1
 *  she 0
 *  shells 3
 *  shore 7
 *  the 5
 *
 ******************************************************************************/

package de.dnb.basics.tries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.ss.formula.eval.NotImplementedException;

/**
 *  The {@code TrieST} class represents an symbol table of key-value
 *  pairs, with string keys and generic values.
 *  It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 *  <em>delete</em>, <em>size</em>, and <em>is-empty</em> methods.
 *  It also provides character-based methods for finding the string
 *  in the symbol table that is the <em>longest prefix</em> of a given prefix,
 *  finding all strings in the symbol table that
 *  <em>start with</em> a given prefix,
 *  and finding all strings in the symbol table that
 *  <em>match</em> a given pattern.
 *  A symbol table implements the <em>associative array</em> abstraction:
 *  when associating a value with a key that is already in the symbol table,
 *  the convention is to replace the old value with the new value.
 *  Unlike {@link java.util.Map}, this class uses the convention that
 *  values cannot be {@code null}—setting the
 *  value associated with a key to {@code null}
 *  is equivalent to deleting the key
 *  from the symbol table.
 *  <p>
 *  This implementation uses an alphabet with cardinality R, hence a R-way trie.
 *  The <em>put</em>, <em>contains</em>, <em>delete</em>, and
 *  <em>longest prefix</em> operations take time proportional to the length
 *  of the key (in the worst case). Construction takes constant time.
 *  The <em>size</em>, and <em>is-empty</em> operations take constant time.
 *  Construction takes constant time.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://algs4.cs.princeton.edu/52trie">Section 5.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *  <p>
 *  <b>Christians Notiz:</b> Nur für kleine Alphabete und kurze
 *  Strings geeignet, ansonsten kann der Heap schnell überlaufen.
 *  Nur bei der Eingabe geordneter Daten optimal, hier ist der
 *  {@link RandomizedTST} um ca. 8% langsamer. Insgesamt:
 *  kaum empehlenswert!
 *
 *
 *  @param <V> type of values
 *
 */
public class AlphabetTrie<V> extends AbstractTrie<V> {
  final Alphabet alphabet;

  private Node<V> root; // root of trie
  private int size; // number of keys in trie

  /**
   *  R-way trie node.
   * @author baumann
   *
   * @param <V>
   */
  @SuppressWarnings("hiding")
  private class Node<V> {
    private V val;
    @SuppressWarnings("unchecked")
    private final Node<V>[] indices = new Node[alphabet.radix()];
  }

  /**
   * Initializes an empty string symbol table.
   */
  public AlphabetTrie(final Alphabet alphabet) {
    this.alphabet = alphabet;
  }

  /**
   * Returns the value associated with the given key.
   * @param key   the key (not null)
   * @return      the value associated with the given key if the key is
   *              in the symbol table
   *              and {@code null} if the key is not in the symbol table
   * @throws      NullPointerException if {@code key} is {@code null}
   */
  @Override
  public V getValue(final String key) {
    Objects.requireNonNull(key, "key darf nicht null sein");
    final int[] indices = alphabet.toIndices(key);
    final Node<V> x = get(root, indices, 0);
    if (x == null)
      return null;
    return x.val;
  }

  /**
   *
   * @param node  Knoten, an dem gesucht wird
   * @param key   Schlüssel
   * @param d     Rekursionstiefe
   * @return      Knoten, der dem Schlüssel entspricht
   */
  private Node<V> get(final Node<V> node, final int[] key, final int d) {
    if (node == null)
      return null;
    if (d == key.length)
      return node;
    final int c = key[d];
    return get(node.indices[c], key, d + 1);
  }

  /**
   * Inserts the key-value pair into the symbol table,
   * overwriting the old value
   * with the new value if the key is already in the symbol table.
   * If the value is {@code null}, this effectively deletes the key
   * from the symbol table.
   *
   * @param key   the key (can be empty String)
   * @param val   the value
   * @throws      NullPointerException if {@code key} is {@code null}
   */
  @Override
  public void putValue(final String key, final V val) {
    final int[] indices = alphabet.toIndices(key);
    if (val == null)
      removeKey(key);
    else
      root = put(root, indices, val, 0);
  }

  /**
   *
   * @param node  Knoten, an dem gesucht wird
   *              (darf null sein)
   * @param keyIndices    Indizes der Schlüssel
   * @param val           abzulegender Wert
   * @param d             Rekursionstiefe
   * @return              einzuhängender Knoten
   */
  private Node<V> put(Node<V> node, final int[] keyIndices, final V val, final int d) {
    if (node == null)
      node = new Node<V>();
    if (d == keyIndices.length) {
      if (node.val == null) {
        size++;
      }
      node.val = val;
      return node;
    }
    final int index = keyIndices[d];
    node.indices[index] = put(node.indices[index], keyIndices, val, d + 1);
    return node;
  }

  /**
   * Returns the number of key-value pairs in this symbol table.
   * @return the number of key-value pairs in this symbol table
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * Returns all keys in the symbol table as an {@code Iterable}.
   * To iterate over all of the keys in the symbol table named {@code st},
   * use the foreach notation: {@code for (Key key : st.keys())}.
   * @return all keys in the symbol table as an {@code Iterable}
   */
  @Override
  public Set<String> keySet() {
    return keysWithPrefix("");
  }

  /**
   * Returns all of the keys in the set that start with {@code prefix}.
   * @param   prefix the prefix
   * @return  all of the keys in the set that start with {@code prefix},
   *          as an iterable
   */
  @Override
  public Set<String> keysWithPrefix(final String prefix) {
    final Set<String> results = new LinkedHashSet<>(size);
    final int[] indices = alphabet.toIndices(prefix);
    final ArrayList<Integer> indexList = new ArrayList<>(alphabet.radix());
    for (int i = 0; i < indices.length; i++) {
      indexList.add(indices[i]);
    }
    final Node<V> node = get(root, indices, 0);
    collectKeysWithPrefix(node, indexList, results);
    return results;
  }

  /**
   * Sammelt ab node alle Schlüssel, die das prefix haben.
   *
   * @param node              Knoten, ab dem gesucht wird
   * @param prefixIndsList    Präfix
   * @param results           enthält die Schlüssel, die auf prefix
   *                          passen
   */
  private void collectKeysWithPrefix(
    final Node<V> node,
    final List<Integer> prefixIndsList,
    final Collection<String> results) {
    if (node == null)
      return;
    if (node.val != null)
      results.add(alphabet.toString(prefixIndsList));
    for (int index = 0; index < alphabet.radix(); index++) {
      prefixIndsList.add(index);
      collectKeysWithPrefix(node.indices[index], prefixIndsList, results);
      prefixIndsList.remove(prefixIndsList.size() - 1);
    }
  }

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
  @Override
  public String longestPrefixOf(final String query) {
    final int[] indices = alphabet.toIndices(query);
    final int length = longestPrefixOf(root, indices, 0, -1);
    if (length == -1)
      return null;
    else
      return query.substring(0, length);
  }

  /**
   *  returns the length of the longest string key in the subtrie
   *  rooted at x that is a prefix of the query string,
   *  assuming the first d character match and we have already
   *  found a prefix match of given length (-1 if no such match)
   * @param node      beginning node
   * @param query     query string
   * @param d         character that match
   * @param length    length of already found prefix match
   * @return          the length of the longest string key
   */
  private int longestPrefixOf(final Node<V> node, final int[] query, final int d, int length) {
    if (node == null)
      return length;
    if (node.val != null)
      length = d;
    if (d == query.length)
      return length;
    final int c = query[d];
    return longestPrefixOf(node.indices[c], query, d + 1, length);
  }

  /**
   * Removes the key from the set if the key is present.
   * @param key the key
   * @throws NullPointerException if {@code key} is {@code null}
   */
  @Override
  public void removeKey(final String key) {
    final int[] indices = alphabet.toIndices(key);
    root = delete(root, indices, 0);
  }

  private Node<V> delete(final Node<V> node, final int[] keyInds, final int d) {
    if (node == null)
      return null;
    if (d == keyInds.length) {
      if (node.val != null)
        size--;
      node.val = null;
    } else {
      final int index = keyInds[d];
      node.indices[index] = delete(node.indices[index], keyInds, d + 1);
    }

    // remove subtrie rooted at x if it is completely empty
    if (node.val != null)
      return node;
    for (int c = 0; c < alphabet.radix(); c++)
      if (node.indices[c] != null)
        return node;
    return null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AlphabetTrie: " + keySet();
  }

  /**
   * Unit tests the {@code TrieST} data type.
   *
   * @param args the command-line arguments
   */
  public static void main(final String[] args) {

    final AlphabetTrie<Integer> trie = new AlphabetTrie<>(Alphabet.DDC);

    trie.putValue("111", 1);
    trie.putValue("111.2", 1);
    trie.putValue("111.3", 1);
    trie.putValue("111.4", 1);

    System.out.println(trie);

    trie.removeKey("111");
    System.out.println(trie);
  }

  /**
   * Macht keinen Sinn, da der Punkt auch Bestandteil des
   * Alphabets sein kann (DDC).
   */
  @Override
  public Collection<String> keysThatMatch(final String pattern) {
    throw new NotImplementedException("keysThatMatch macht "
      + "keinen Sinn, da der Punkt auch Bestandteil des" + " Alphabets sein kann.");
  }

  /* (non-Javadoc)
   * @see de.dnb.basics.collections.Trie#clear()
   */
  @Override
  public void clear() {
    root = null;
    size = 0;
  }

}
