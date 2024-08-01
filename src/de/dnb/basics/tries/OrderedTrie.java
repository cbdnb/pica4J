
package de.dnb.basics.tries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Ein Trie, der eine {@link TreeMap} zur Verwaltung der
 * Kindknoten verwendet. Da Java hier Rot-Schwarz-Bäume
 * einsetzt, sind Verwaltungsaufwand und Speicherbedarf minimal und man
 * bekommt einen balancierten Binärbaum, ohne viel
 * zu investieren. Insgesamt aber nicht zu empfehlen, da in allen
 * Belangen schlechter als alle Konkurrenten.
 *
 *  @param <V> Typ der Werte
 *
 */
public class OrderedTrie<V> extends AbstractTrie<V> {

    private Node<V> root; // root of trie
    private int size; // number of keys in trie

    /**
     *  R-way trie node
     * @author baumann
     *
     * @param <V>
     */
    private static class Node<V> {
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Node [val=" + val + ", next=" + next + "]";
        }

        private V val;
        private final TreeMap<Character, Node<V>> next = new TreeMap<>();
    }

    /**
     * Initializes an empty string symbol table.
     */
    public OrderedTrie() {
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
        return get(node.next.get(c), key, d + 1);
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
        node.next.put(c, put(node.next.get(c), key, val, d + 1));
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

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#clear()
     */
    @Override
    public void clear() {
        root = null;
        size = 0;
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
        node.next.keySet().forEach(c ->
        {
            prefix.append(c);
            collect(node.next.get(c), prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        });
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
            node.next.keySet().forEach(ch ->
            {
                prefix.append(ch);
                collect(node.next.get(ch), prefix, pattern, results);
                prefix.deleteCharAt(prefix.length() - 1);
            });
        } else {
            prefix.append(c);
            collect(node.next.get(c), prefix, pattern, results);
            prefix.deleteCharAt(prefix.length() - 1);
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
        return longestPrefixOf(node.next.get(c), query, d + 1, length);
    }

    /**
     * Removes the key from the set if the key is present.
     * @param key the key
     * @throws NullPointerException if {@code key} is {@code null}
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
            final Node<V> deleted = delete(x.next.get(c), key, d + 1);
            if (deleted != null)
                x.next.put(c, deleted);
            else
                x.next.remove(c);
        }

        if (x.val != null)
            return x;
        else
            return x.next.isEmpty() ? null : x;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OrderedTrie: " + keySet();
    }

    /**
     * Unit tests the {@code TrieST} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(final String[] args) {

        final OrderedTrie<Integer> trie = new OrderedTrie<>();
        trie.putValue("", 0);
        trie.putValue("a", 1);
        trie.putValue("ab", 2);
        trie.putValue("ac", 3);
        trie.putValue("aa", 4);

        System.out.println(trie.getValue(""));
        System.out.println(trie.longestPrefixOf("aaa"));
        System.out.println(trie.getValueOfLongestPrefix("x"));

        System.out.println(trie);

        trie.removeKey("ab");
        trie.removeKey("");
        System.out.println(trie);
        System.out.println(trie.root);
    }
}
