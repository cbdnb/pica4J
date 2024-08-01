/**
 *
 */
package de.dnb.basics.applicationComponents.collections;

import org.junit.Test;

import de.dnb.basics.tries.Alphabet;
import de.dnb.basics.tries.AlphabetTrie;
import de.dnb.basics.tries.ArrayTrie;
import de.dnb.basics.tries.OrderedTrie;
import de.dnb.basics.tries.RandomizedTST;
import de.dnb.basics.tries.TST;
import de.dnb.basics.tries.Trie;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * @author baumann
 *
 */
public class TrieTest {

  @Test
  public void test() {
    final Trie<Integer> tst = new TST<>();
    final Trie<Integer> alphabet = new AlphabetTrie<>(Alphabet.EXTENDED_ASCII);
    final Trie<Integer> orderedTrie = new OrderedTrie<>();
    final Trie<Integer> arrayTrie = new ArrayTrie<>();
    final Trie<Integer> randTrie = new RandomizedTST<>();

    test(alphabet);
    test(orderedTrie);
    test(arrayTrie);
    test(tst);
    test(randTrie);
  }

  private void test(final Trie<Integer> trie) {
    testPutGet(trie);
    testPrefix(trie);
    testOrder(trie);
    testIsEmpty(trie);
    testDelete(trie);
    testHashEquals(trie);
  }

  /**
   * @param trie
   */
  private void testHashEquals(final Trie<Integer> trie) {
    trie.clear();
    final HashMap<String, Integer> hashmap = new HashMap<>();
    final LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>();
    final TreeMap<String, Integer> treeMap = new TreeMap<>();
    for (int i = 0; i < 100; i++) {
      trie.put(i + "", i);
      hashmap.put(i + "", i);
      linkedHashMap.put(i + "", i);
      treeMap.put(i + "", i);
    }
    assertTrue(trie.equals(hashmap));
    assertTrue(trie.equals(linkedHashMap));
    assertTrue(trie.equals(treeMap));
    assertFalse(trie.equals(null));
    assertTrue(trie.hashCode() == hashmap.hashCode());
    assertTrue(trie.hashCode() == linkedHashMap.hashCode());
    assertTrue(trie.hashCode() == treeMap.hashCode());

  }

  /**
   * @param trie
   */
  private void testDelete(final Trie<Integer> trie) {
    /*
     * Baum für TST:
     *           m
     *        /    \
     *       d      s
     *      / \     /\
     *    c    g   q  t
     *        /
     *       f
     */
    trie.putValue("m", 1);
    trie.putValue("d", 1);
    trie.putValue("s", 1);
    trie.putValue("c", 1);
    trie.putValue("g", 1);
    trie.putValue("f", 1);
    trie.putValue("q", 1);
    trie.putValue("t", 1);
    trie.putValue("r", 1);
    assertTrue(trie.size() == 9);
    assertTrue(trie.keySet().size() == 9);

    // mittleren löschen:
    trie.removeKey("m");

    assertTrue(trie.size() == 8);
    assertTrue(trie.keySet().size() == 8);

  }

  /**
   * @param trie
   */
  private void testIsEmpty(final Trie<Integer> trie) {
    trie.clear();

    trie.putValue("b", 1);
    trie.putValue("a", 1);
    trie.putValue("ab", 1);
    trie.putValue("aba", 1);
    trie.putValue("abb", 1);

    trie.putValue("b", null);
    trie.putValue("a", null);
    trie.putValue("ab", null);
    trie.putValue("aba", null);
    trie.putValue("abb", null);

    assertTrue(trie.isEmpty());

    trie.putValue("b", null);
    trie.putValue("a", null);
    trie.putValue("ab", null);
    trie.putValue("aba", null);
    trie.putValue("abb", null);

    assertTrue(trie.isEmpty());

  }

  /**
   * @param trie
   */
  private void testOrder(final Trie<Integer> trie) {
    final Trie<Integer> orderedTrie = new OrderedTrie<>();
    orderedTrie.putValue("a", 3);
    orderedTrie.putValue("ab", 3);
    orderedTrie.putValue("ac", 3);
    orderedTrie.putValue("ad", 3);
    orderedTrie.putValue("ada", 3);
    orderedTrie.putValue("adb", 3);
    orderedTrie.putValue("ae", 3);

    trie.clear();
    trie.putValue("a", 3);
    trie.putValue("ab", 3);
    trie.putValue("ac", 3);
    trie.putValue("ad", 3);
    trie.putValue("ada", 3);
    trie.putValue("adb", 3);
    trie.putValue("ae", 3);
    assertEquals(orderedTrie.keySet(), trie.keySet());

    trie.clear();
    trie.putValue("ae", 3);
    trie.putValue("adb", 3);
    trie.putValue("ada", 3);
    trie.putValue("ad", 3);
    trie.putValue("ac", 3);
    trie.putValue("ab", 3);
    trie.putValue("a", 3);

    assertEquals(orderedTrie.keySet(), trie.keySet());

  }

  /**
   * @param trie
   */
  private void testPrefix(final Trie<Integer> trie) {
    trie.clear();

    trie.putValue("a", 1);
    trie.putValue("aa", 1);
    trie.putValue("aaa", 1);
    trie.putValue("ab", 1);
    trie.putValue("ac", 1);

    if (!(trie instanceof AlphabetTrie)) {
      assertEquals(3, trie.keysThatMatch("a.").size());
    }

    assertEquals(5, trie.keysWithPrefix("a").size());
    assertEquals(2, trie.keysWithPrefix("aa").size());
    assertEquals(1, trie.keysWithPrefix("aaa").size());
    assertEquals(0, trie.keysWithPrefix("aaad").size());

    assertEquals("aa", trie.longestPrefixOf("aac"));
    assertEquals("aaa", trie.longestPrefixOf("aaac"));
    assertEquals("ab", trie.longestPrefixOf("ab"));
    assertEquals("a", trie.longestPrefixOf("ad"));
    assertNull(trie.longestPrefixOf("x"));

  }

  /**
   * @param trie
   */
  public void testPutGet(final Trie<Integer> trie) {
    trie.clear();

    assertEquals(0, trie.size());
    trie.putValue("a", 1);
    trie.putValue("a", 1);
    trie.removeKey("x");
    assertEquals(1, trie.size());
    trie.putValue("a", 2);
    assertEquals(1, trie.size());
    trie.putValue("a", null);
    assertEquals(0, trie.size());

    trie.clear();
    trie.removeKey("x");
    assertEquals(0, trie.size());
    if (!(trie instanceof TST || trie instanceof RandomizedTST)) {
      assertFalse(trie.contains(""));
    }
    assertFalse(trie.contains("y"));

    trie.putValue("a", 2);
    assertTrue(trie.contains("a"));
    assertEquals(1, trie.size());
    assertFalse(trie.contains("y"));

    assertEquals(new Integer(2), trie.getValue("a"));
    assertNull(trie.getValue("x"));
  }

}
