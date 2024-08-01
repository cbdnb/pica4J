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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;

/**
 *  Randomized Ternary Search Trie
 *
 *  siehe <a href="https://arxiv.org/pdf/1606.04042">
 *  Randomized Ternary Search Tries by Nicolai Diethelm
 *  </a>.
 *  <p>
 *  <b>Christians Notiz:</b> Optimal für geordnete Eingabedaten.
 *  Das betrifft vor allem Suche und Löschvorgang. Der
 *  {@link AlphabetTrie} ist bei der Eingabe etwas schneller.
 *  Der Speicherverbrauch ist beim {@link TST} besser.
 *
 *
 *  @param <V>  Typ
 */
public class RandomizedTST<V> extends AbstractTrie<V> {
    /**
     *
     */
    private static final int MAX_RAND = 1 << 30;
    private int size; // size
    private Node<V> root; // root of TST
    private final Random random = new Random();

    private static class Node<V> {

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Node [ch=" + ch + ", val=" + val + ", left=" + left
                + ", mid=" + mid + ", right=" + right + "]";
        }

        private char ch; // character
        private V val; // value associated with string
        private Node<V> left, mid, right; // left, middle, and right subtries

        private int prio;
        private int strPrio;

    }

    /**
     * Initializes an empty string symbol table.
     */
    public RandomizedTST() {
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
            throw new IllegalArgumentException(
                "calls get() with null argument");
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
            throw new IllegalArgumentException(
                "calls get() with null argument");
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
            root = put(key, 0, root, val);
        }
        //        System.err.println(root);
    }

    private Node<V> put(final String s, final int i, Node<V> x, final V val) {

        final char charS = s.charAt(i);
        if (x == null) {
            x = new Node<>();
            x.ch = charS;
            x.prio = 0;
            x.strPrio = 0;
        }
        if (charS < x.ch) {
            x.left = put(s, i, x.left, val);
            if (x.left.prio > x.prio) {
                x = rotateWithLeft(x);
            }
        } else if (charS > x.ch) {
            x.right = put(s, i, x.right, val);
            if (x.right.prio > x.prio) {
                x = rotateWithRight(x);
            }
        } else { // richtiger Knoten
            if (i < s.length() - 1) {
                x.mid = put(s, i + 1, x.mid, val);
            } else { // Ziel erreicht
                if (x.strPrio == 0) {
                    x.strPrio = randomInteger();
                }
                if (x.val == null) {
                    size++;
                }
                x.val = val;
            }
            final int midprio = x.mid == null ? 0 : x.mid.prio;
            x.prio = Integer.max(x.strPrio, midprio);
        }
        return x;

    }

    /* (non-Javadoc)
     * @see de.dnb.basics.collections.Trie#delete(java.lang.String)
     */
    @Override
    public void removeKey(final String key) {
        root = delete(key, 0, root);
    }

    /**
     * @param s  Schlüssel
     * @param i  Rekursionstiefe
     * @param x  Knoten, an dem begonnen wird
     * @return
     */
    private Node<V> delete(final String s, final int i, Node<V> x) {

        if (x != null) {
            if (s.charAt(i) < x.ch) {
                x.left = delete(s, i, x.left);
            } else if (s.charAt(i) > x.ch) {
                x.right = delete(s, i, x.right);
            } else {
                if (i < s.length() - 1) {
                    x.mid = delete(s, i + 1, x.mid);
                } else { // gefunden
                    x.strPrio = 0;
                    if (x.val != null)
                        size--;
                    x.val = null;

                }
                final int midPrio = x.mid == null ? 0 : x.mid.prio;
                x.prio = Integer.max(x.strPrio, midPrio);
                x = heapifyOrDelete(x);
            }
        }
        return x;

    }

    private Node<V> rotateWithLeft(final Node<V> x) {
        final Node<V> y = x.left;
        x.left = y.right;
        y.right = x;
        return y;
    }

    private Node<V> rotateWithRight(final Node<V> x) {
        final Node<V> y = x.right;
        x.right = y.left;
        y.left = x;
        return y;
    }

    private Node<V> heapifyOrDelete(Node<V> x) {
        int xPrio;
        int leftPrio;
        int rightPrio;
        if (x != null) {
            xPrio = x.prio;
            leftPrio = x.left != null ? x.left.prio : 0;
            rightPrio = x.right != null ? x.right.prio : 0;
        } else {
            xPrio = 0;
            leftPrio = 0;
            rightPrio = 0;
        }

        if (xPrio < leftPrio || xPrio < rightPrio) {
            if (leftPrio > rightPrio) {
                x = rotateWithLeft(x);
                x.right = heapifyOrDelete(x.right);
            } else {
                x = rotateWithRight(x);
                x.left = heapifyOrDelete(x.left);
            }
        } else if (xPrio == 0) {
            x = null;
        }
        return x;
    }

    private int randomInteger() {
        return random.nextInt(MAX_RAND) + 1;
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
            throw new IllegalArgumentException(
                "calls longestPrefixOf() with null argument");
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
            throw new IllegalArgumentException(
                "calls keysWithPrefix() with null argument");
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
    private void collect(
        final Node<V> x,
        final StringBuilder prefix,
        final Collection<String> keysFound) {
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

        final RandomizedTST<Integer> trie = new RandomizedTST<Integer>();

        trie.putValue("b", 1);
        trie.putValue("a", 1);
        trie.putValue("ab", 1);
        trie.putValue("aba", 1);
        trie.putValue("abb", 1);

        trie.dump();
        System.out.println();
        System.out.println(trie);
        System.out.println(trie.root);
        
        System.out.println(trie.longestPrefixOf(""));

    }

}
