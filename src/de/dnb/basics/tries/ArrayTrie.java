
package de.dnb.basics.tries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
 *  This implementation uses a 256-way trie.
 *  The <em>put</em>, <em>contains</em>, <em>delete</em>, and
 *  <em>longest prefix</em> operations take time proportional to the length
 *  of the key (in the worst case). Construction takes constant time.
 *  The <em>size</em>, and <em>is-empty</em> operations take constant time.
 *  Construction takes constant time.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://algs4.cs.princeton.edu/52trie">Section 5.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @param <V> type of values
 *
 */
public class ArrayTrie<V> extends AbstractTrie<V> {
    private static final int R = 256; // extended ASCII

    private Node<V> root; // root of trie
    private int size; // number of keys in trie

    /**
     *  R-way trie node
     * @author baumann
     *
     * @param <V>
     */
    private static class Node<V> {
        private V val;
        @SuppressWarnings("unchecked")
        private final Node<V>[] next = new Node[R];
    }

    /**
     * Initializes an empty string symbol table.
     */
    public ArrayTrie() {
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#get(java.lang.String)
     */
    @Override
    public V getValue(final String key) {
        Objects.requireNonNull(key, "key darf nicht null sein");
        final Node<V> x = get(root, key, 0);
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
    private Node<V> get(final Node<V> node, final String key, final int d) {
        if (node == null)
            return null;
        if (d == key.length())
            return node;
        final char c = key.charAt(d);
        return get(node.next[c], key, d + 1);
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#put(java.lang.String, V)
     */
    @Override
    public void putValue(final String key, final V val) {
        if (val == null)
            removeKey(key);
        else
            root = put(root, key, val, 0);
    }

    /**
     *
     * @param node  Knoten, an dem gesucht wird
     *              (darf null sein)
     * @param key   Schlüssel
     * @param val   abzulegender Wert
     * @param d     Rekursionstiefe
     * @return      einzuhängender Knoten
     */
    private
        Node<V>
        put(Node<V> node, final String key, final V val, final int d) {
        if (node == null)
            node = new Node<V>();
        if (d == key.length()) {
            if (node.val == null) {
                size++;
            }
            node.val = val;
            return node;
        }
        final char c = key.charAt(d);
        node.next[c] = put(node.next[c], key, val, d + 1);
        return node;
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#size()
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

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#keys()
     */
    @Override
    public Set<String> keySet() {
        return keysWithPrefix("");
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#keysWithPrefix(java.lang.String)
     */
    @Override
    public Set<String> keysWithPrefix(final String prefix) {
        final Set<String> results = new LinkedHashSet<>(size);
        final Node<V> node = get(root, prefix, 0);
        collect(node, new StringBuilder(prefix), results);
        return results;
    }

    /**
     * Sammelt ab node alle Schlüssel, die das prefix haben.
     *
     * @param node      Knoten, ab dem gesucht wird
     * @param prefix    Präfix
     * @param results   enthält die Schlüssel, die auf prefix
     *                  passen
     */
    private void collect(
        final Node<V> node,
        final StringBuilder prefix,
        final Collection<String> results) {
        if (node == null)
            return;
        if (node.val != null)
            results.add(prefix.toString());
        for (char c = 0; c < R; c++) {
            prefix.append(c);
            collect(node.next[c], prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#keysThatMatch(java.lang.String)
     */
    @Override
    public Collection<String> keysThatMatch(final String pattern) {
        final Collection<String> results = new ArrayList<String>();
        collect(root, new StringBuilder(), pattern, results);
        return results;
    }

    /**
     * Sammelt ab dem Startknoten alle Schlüssel, die dem
     * Muster entsprechen. Wildcard ist '.' .
     *
     * @param node      Startknoten
     * @param prefix    Bisheriges gefundenes Präfix
     * @param pattern   Suchmuster
     * @param results   Hier werden die gefundenen Schlüssel
     *                  gesammelt
     */
    private void collect(
        final Node<V> node,
        final StringBuilder prefix,
        final String pattern,
        final Collection<String> results) {
        if (node == null)
            return;
        final int prefLength = prefix.length();
        if (prefLength == pattern.length() && node.val != null)
            results.add(prefix.toString());
        if (prefLength == pattern.length())
            return;
        final char c = pattern.charAt(prefLength);
        if (c == '.') {
            for (char ch = 0; ch < R; ch++) {
                prefix.append(ch);
                collect(node.next[ch], prefix, pattern, results);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        } else {
            prefix.append(c);
            collect(node.next[c], prefix, pattern, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#longestPrefixOf(java.lang.String)
     */
    @Override
    public String longestPrefixOf(final String query) {
        final int length = longestPrefixOf(root, query, 0, -1);
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
    private int longestPrefixOf(
        final Node<V> node,
        final String query,
        final int d,
        int length) {
        if (node == null)
            return length;
        if (node.val != null)
            length = d;
        if (d == query.length())
            return length;
        final char c = query.charAt(d);
        return longestPrefixOf(node.next[c], query, d + 1, length);
    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#delete(java.lang.String)
     */
    @Override
    public void removeKey(final String key) {
        root = delete(root, key, 0);
    }

    private Node<V> delete(final Node<V> x, final String key, final int d) {
        if (x == null)
            return null;
        if (d == key.length()) {
            if (x.val != null)
                size--;
            x.val = null;
        } else {
            final char c = key.charAt(d);
            x.next[c] = delete(x.next[c], key, d + 1);
        }

        // remove subtrie rooted at x if it is completely empty
        if (x.val != null)
            return x;
        for (int c = 0; c < R; c++)
            if (x.next[c] != null)
                return x;
        return null;
    }

    /**
     * Unit tests the {@code TrieST} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(final String[] args) {

        final Trie<Integer> trieST = new ArrayTrie<>();

        trieST.putValue("a", 1);
        trieST.putValue("ab", 2);
        trieST.putValue("ac", 3);
        trieST.putValue("aa", 4);

        System.out.println(trieST.longestPrefixOf("x"));

        System.out.println(trieST.keysThatMatch("a."));

        System.out.println(trieST.getValue(""));

        System.out.println(trieST.longestPrefixOf(""));

        System.out.println(trieST.getValueOfLongestPrefix("x"));

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
