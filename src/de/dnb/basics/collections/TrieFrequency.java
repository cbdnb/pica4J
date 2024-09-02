/**
 *
 */
package de.dnb.basics.collections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.tries.TST;
import de.dnb.basics.tries.Trie;

/**
 * Frequency, die auf einem Trie basiert. Dieser
 * Trie muss zunächst initialisiert werden.
 *
 * @author baumann
 *
 */
public class TrieFrequency extends Frequency<String> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private Consumer<String> errorFN = null;

  /**
   * Einfacher Konstruktor.
   */
  public TrieFrequency() {
    values2long = new TST<>();
  }

  /**
   * @param trie  nicht null
   */
  public TrieFrequency(final Trie<Long> trie) {
    Objects.requireNonNull(trie);
    values2long = trie;
  }

  /**
   * Fügt einen Schlüssel zum Trie hinzu.
   *
   * @param key nicht null
   */
  @Override
  public void addKey(final String key) {
    Objects.requireNonNull(key);
    values2long.put(key, 0L);
  }

  /**
   * Fügt Schlüssel zum Trie hinzu.
   *
   * @param keys nicht null
   */
  public void addKeys(final Iterable<String> keys) {
    Objects.requireNonNull(keys);
    keys.forEach(k -> addKey(k));
  }

  /**
   * Fügt Schlüssel zum Trie hinzu.
   *
   * @param keys nicht null
   */
  public void addKeys(final String... keys) {
    addKeys(Arrays.asList(keys));
  }

  /**
   * inkrementiert das längste Präfix von value. Gibt eine
   * Fehlermeldung, wenn dieses Präfix nicht existiert.
   * Die Verarbeitung des Fehlers kann über {@link #setErrorFN(Consumer)}
   * eingestellt werden.
   *
   * @param value     wenn null, wird optionale Fehlerverarbeitung
   *                  angewandt
   * @param increment inkrement
   */
  @Override
  public void increment(final String value, final long increment) {
    if (value == null) {
      if (errorFN != null) {
        errorFN.accept(value);
      }
      return;
    }
    final Trie<Long> trie = (Trie<Long>) values2long;
    final String key = trie.longestPrefixOf(value);
    if (key != null) {
      Long l = trie.get(key);
      l += increment;
      values2long.put(key, l);
    } else {
      if (errorFN != null) {
        errorFN.accept(value);
      }
    }
  }

  /**
   * inkrementiert das längste Präfix von value um 1. Gibt eine
   * Fehlermeldung, wenn dieses Präfix nicht existiert.
   *
   * @param value     nicht null
   *
   */
  @Override
  public void add(final String value) {
    increment(value, 1L);
  }

  /**
   * @param fileName
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public TrieFrequency(final String fileName) throws IOException, ClassNotFoundException {
    this();
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    final TrieFrequency readObject = (TrieFrequency) objectin.readObject();
    final TrieFrequency freq = readObject;
    MyFileUtils.safeClose(objectin);
    addAll(freq);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {

    final Trie<Long> trie = new TST<>();
    trie.putValue("y", 0L);
    TrieFrequency frequency = new TrieFrequency(trie);
    frequency.add("y");
    frequency.add("ya");
    frequency.add("yb");
    System.out.println(frequency);

    System.out.println();

    frequency = new TrieFrequency();
    frequency.setErrorFN(System.err::println);
    frequency.addKeys("a", "c");
    frequency.add("a");
    frequency.add("ab");
    frequency.add("b");

    frequency.addCollection(Arrays.asList("ac", "ad"));

    System.out.println(frequency);

  }

  /**
   * @param errorFN   Fehlerfunktion, wenn
   *                  Präfix nicht existiert oder wenn versucht
   *                  wird, null einzufügen. Empfohlen:
   *                  System.err::println
   */
  public void setErrorFN(final Consumer<String> errorFN) {
    this.errorFN = errorFN;
  }

}
