/******************************************************************************
 *  Compilation:  javac TST.java
 *  Execution:    java TST < words.txt
 *  Dependencies: StdIn.java
 *  Data files:   http://algs4.cs.princeton.edu/52trie/shellsST.txt
 *
 *  Symbol table with string keys, implemented using a ternary search
 *  trie (TST).
 *
 *
 *  % java TST < shellsST.txt
 *  keys(""):
 *  by 4
 *  sea 6
 *  sells 1
 *  she 0
 *  shells 3
 *  shore 7
 *  the 5
 *
 *  longestPrefixOf("shellsort"):
 *  shells
 *
 *  keysWithPrefix("shor"):
 *  shore
 *
 *  keysThatMatch(".he.l."):
 *  shells
 *
 *  % java TST
 *  theory the now is the time for all good men
 *
 *  Remarks
 *  --------
 *    - can't use a key that is the empty string ""
 *
 ******************************************************************************/

package de.dnb.basics.tries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;

/**
 *  The {@code TST} class represents an symbol table of key-value
 *  pairs, with string keys and generic values.
 *  It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 *  <em>delete</em>, <em>size</em>, and <em>is-empty</em> methods.
 *  It also provides character-based methods for finding the string
 *  in the symbol table that is the <em>longest prefix</em> of a given prefix,
 *  finding all strings in the symbol table that <em>start with</em> a given prefix,
 *  and finding all strings in the symbol table that <em>match</em> a given pattern.
 *  A symbol table implements the <em>associative array</em> abstraction:
 *  when associating a value with a key that is already in the symbol table,
 *  the convention is to replace the old value with the new value.
 *  Unlike {@link java.util.Map}, this class uses the convention that
 *  values cannot be {@code null}—setting the
 *  value associated with a key to {@code null}
 *  is equivalent to deleting the key
 *  from the symbol table.
 *  <p>
 *  This implementation uses a ternary search trie.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://algs4.cs.princeton.edu/52trie">
 *  Section 5.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *  <p>
 *  <b>Christians Notiz:</b> Optimal für zufällige Eingabedaten.
 *  Das betrifft alle Parameter, also
 *  Eingabe, Suche, Löschen und Speicherbedarf. Für geordnete Daten ist
 *  der {@link RandomizedTST} vorzuziehen. Aber auch in diesem Fall
 *  ist er Speicherverbrauch optimal.
 *
 *  @param <V>  Typ
 */
public class TST<V> extends AbstractTrie<V> {
  /**
  *
  */
  private static final long serialVersionUID = 5813089164996823847L;
  private int size; // size
  private Node<V> root; // root of TST
  private final Random random = new Random();

  private static class Node<Value> implements Serializable {

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "Node [ch=" + ch + ", val=" + val + ", left=" + left + ", mid=" + mid + ", right="
        + right + "]";
    }

    private char ch; // character
    private Value val; // value associated with string
    private Node<Value> left, mid, right; // left, middle, and right subtries

  }

  /**
   * Initializes an empty string symbol table.
   */
  public TST() {
  }

  /**
   * Returns the number of key-value pairs in this symbol table.
   * @return the number of key-value pairs in this symbol table
   */
  @Override
  public int size() {
    return size;
  }

  /* (non-Javadoc)
   * @see de.dnb.basics.collections.Trie#clear()
   */
  @Override
  public void clear() {
    root = null;
    size = 0;
  }

  /**
   * Returns the value associated with the given key.
   * @param key the key
   * @return the value associated with the given key if the key is in the symbol table
   *     and {@code null} if the key is not in the symbol table
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  @Override
  public V getValue(final String key) {
    if (key == null) {
      throw new IllegalArgumentException("calls get() with null argument");
    }
    if (key.length() == 0)
      throw new IllegalArgumentException("key must have length >= 1");
    final Node<V> x = get(root, key, 0);
    if (x == null)
      return null;
    return x.val;
  }

  // return subtrie corresponding to given key
  private Node<V> get(final Node<V> x, final String key, final int d) {
    if (x == null)
      return null;
    if (key == null) {
      throw new IllegalArgumentException("calls get() with null argument");
    }
    if (key.length() == 0)
      throw new IllegalArgumentException("key must have length >= 1");
    final char c = key.charAt(d);
    if (c < x.ch)
      return get(x.left, key, d);
    else if (c > x.ch)
      return get(x.right, key, d);
    else if (d < key.length() - 1)
      return get(x.mid, key, d + 1);
    else
      return x;
  }

  /**
   * Inserts the key-value pair into the symbol table,
   * overwriting the old value
   * with the new value if the key is already in the symbol table.
   * If the value is {@code null}, this effectively deletes
   * the key from the symbol table.
   * @param key the key
   * @param val the value
   *
   */
  @Override
  public void putValue(final String key, final V val) {
    if (val == null)
      removeKey(key);
    else {
      root = put(root, key, val, 0);
    }
    //        System.err.println(root);
  }

  private Node<V> put(Node<V> node, final String key, final V val, final int d) {
    final char c = key.charAt(d);
    if (node == null) {
      node = new Node<V>();
      node.ch = c;
    }
    if (c < node.ch) {
      node.left = put(node.left, key, val, d);
    } else if (c > node.ch) {
      node.right = put(node.right, key, val, d);
    } else if (d < key.length() - 1) {
      node.mid = put(node.mid, key, val, d + 1);
    } else {
      if (node.val == null) {
        size++;
      }
      node.val = val;
    }
    return node;
  }

  /* (non-Javadoc)
   * @see de.dnb.basics.collections.Trie#delete(java.lang.String)
   */
  @Override
  public void removeKey(final String key) {
    root = delete(root, key, 0);
  }

  /**
   * @param node  Knoten, an dem begonnen wird
   * @param key   Schlüssel
   * @param i     Rekursionstiefe
   * @return
   */
  private Node<V> delete(Node<V> node, final String key, final int i) {
    //        System.err.println(i);
    //        System.err.println(node);
    if (node == null)
      return null;
    if (key.charAt(i) == node.ch) {
      if (i == key.length() - 1) {
        if (node.val != null)
          size--;
        node.val = null;
      } else {
        node.mid = delete(node.mid, key, i + 1);
      }
    } else if (key.charAt(i) < node.ch) {
      node.left = delete(node.left, key, i);
    } else {
      node.right = delete(node.right, key, i);
    }

    //jetzt node entfernen, wenn es nötig sein sollte:
    if (node.val != null) {
      return node;
    }

    // val == null:
    if (node.mid != null) {
      return node;
    }
    if (node.left == null) {
      return node.right;
    }
    if (node.right == null) {
      return node.left;
    }
    // also rechts und links erwas:
    final Node<V> t = node;
    if (random.nextBoolean()) {
      node = min(t.right); // Siehe Sedgewick Listing 3.11 Seite 436.
      node.right = deleteMin(t.right);
      node.left = t.left;
    } else {
      node = max(t.left);
      node.left = deleteMax(t.left);
      node.right = t.right;
    }
    return node;
  }

  private Node<V> deleteMin(final Node<V> x) {
    if (x.left == null)
      return x.right;
    x.left = deleteMin(x.left);
    return x;
  }

  private Node<V> deleteMax(final Node<V> x) {
    if (x.right == null)
      return x.left;
    x.right = deleteMax(x.right);
    return x;
  }

  private Node<V> min(final Node<V> x) {
    if (x.left == null)
      return x;
    return min(x.left);
  }

  private Node<V> max(final Node<V> x) {
    if (x.right == null)
      return x;
    return max(x.right);
  }

  /**
   * Returns the string in the symbol table that is the longest prefix of {@code query},
   * or {@code null}, if no such string.
   * @param query the query string
   * @return the string in the symbol table that is the longest prefix of {@code query},
   *     or {@code null} if no such string
   * @throws IllegalArgumentException if {@code query} is {@code null}
   */
  @Override
  public String longestPrefixOf(final String query) {
    if (query == null) {
      throw new IllegalArgumentException("calls longestPrefixOf() with null argument");
    }
    if (query.length() == 0)
      return null;
    int length = 0;
    Node<V> x = root;
    int i = 0;
    while (x != null && i < query.length()) {
      final char c = query.charAt(i);
      if (c < x.ch)
        x = x.left;
      else if (c > x.ch)
        x = x.right;
      else {
        i++;
        if (x.val != null)
          length = i;
        x = x.mid;
      }
    }
    if (length == 0)
      return null;
    else
      return query.substring(0, length);
  }

  /**
   * Returns all keys in the symbol table as an {@code Iterable}.
   * To iterate over all of the keys in the symbol table named {@code st},
   * use the foreach notation: {@code for (Key key : st.keys())}.
   * @return all keys in the symbol table as an {@code Iterable}
   */
  @Override
  public Set<String> keySet() {
    final Set<String> keys = new LinkedHashSet<>(size);
    collect(root, new StringBuilder(), keys);
    return keys;
  }

  /**
   * Returns all of the keys in the set that start with {@code prefix}.
   * @param prefix the prefix
   * @return all of the keys in the set that start with {@code prefix},
   *     as an iterable
   * @throws IllegalArgumentException if {@code prefix} is {@code null}
   */
  @Override
  public Collection<String> keysWithPrefix(final String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException("calls keysWithPrefix() with null argument");
    }
    final Collection<String> keys = new ArrayList<String>();
    final Node<V> x = get(root, prefix, 0);
    if (x == null)
      return keys;
    if (x.val != null)
      keys.add(prefix);
    collect(x.mid, new StringBuilder(prefix), keys);
    return keys;
  }

  // all keys in subtrie rooted at x with given prefix
  private
    void
    collect(final Node<V> x, final StringBuilder prefix, final Collection<String> keysFound) {
    if (x == null)
      return;
    collect(x.left, prefix, keysFound);
    if (x.val != null)
      keysFound.add(prefix.toString() + x.ch);
    collect(x.mid, prefix.append(x.ch), keysFound);
    prefix.deleteCharAt(prefix.length() - 1);
    collect(x.right, prefix, keysFound);
  }

  /**
   * Returns all of the keys in the symbol table that match {@code pattern},
   * where . symbol is treated as a wildcard character.
   * @param pattern the pattern
   * @return all of the keys in the symbol table that match {@code pattern},
   *     as an iterable, where . is treated as a wildcard character.
   */
  @Override
  public Collection<String> keysThatMatch(final String pattern) {
    final Collection<String> matchingKeys = new ArrayList<String>();
    collect(root, new StringBuilder(), 0, pattern, matchingKeys);
    return matchingKeys;
  }

  private void collect(
    final Node<V> x,
    final StringBuilder prefix,
    final int i,
    final String pattern,
    final Collection<String> strings) {
    if (x == null)
      return;
    final char c = pattern.charAt(i);
    if (c == '.' || c < x.ch)
      collect(x.left, prefix, i, pattern, strings);
    if (c == '.' || c == x.ch) {
      if (i == pattern.length() - 1 && x.val != null)
        strings.add(prefix.toString() + x.ch);
      if (i < pattern.length() - 1) {
        collect(x.mid, prefix.append(x.ch), i + 1, pattern, strings);
        prefix.deleteCharAt(prefix.length() - 1);
      }
    }
    if (c == '.' || c > x.ch)
      collect(x.right, prefix, i, pattern, strings);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TST: " + keySet();
  }

  private void dump(final Node<V> node, final int depth) {
    if (node == null)
      return;

    if (node.val != null)
      System.out.print('*');
    else
      System.out.print(' ');
    System.out.print(node.ch);
    System.out.print("  -r-  ");
    dump(node.right, depth + 1);
    System.out.println();
    System.out.print(StringUtils.padding(depth * 9, ' '));
    System.out.print("    -m-  ");
    dump(node.mid, depth + 1);
    System.out.println();
    System.out.print(StringUtils.padding(depth * 9, ' '));
    System.out.print("    -l-  ");
    dump(node.left, depth + 1);
    //        System.out.println();

  }

  public void dump() {
    dump(root, 0);
    System.out.println();
  }

  /**
   * Unit tests the {@code TST} data type.
   *
   * @param args the command-line arguments
   */
  public static void main(final String[] args) {

    final TST<Integer> trie = new TST<Integer>();

    trie.putValue("m", 1);
    trie.putValue("d", 1);
    trie.putValue("s", 1);
    trie.putValue("c", 1);
    trie.putValue("g", 1);
    trie.putValue("f", 1);
    trie.putValue("q", 1);
    trie.putValue("t", 1);
    trie.putValue("r", 1);
    System.out.println(trie);
    trie.dump();

    System.out.println();
    System.out.println();
    trie.removeKey("m");
    System.out.println(trie);

    trie.dump();

  }

}

/******************************************************************************
 *  Copyright 2002-2016, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/
