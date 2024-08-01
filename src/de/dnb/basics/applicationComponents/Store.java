/**
 *
 */
package de.dnb.basics.applicationComponents;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * Simple Datenstruktur, die häufig erzeugte Objekte, die aber nur wenige
 * Ausprägungen haben, speichert.
 *
 * @author baumann
 *
 */
public class Store<T> {

  private final Map<T, T> store;
  private final int defaultCapacity = 500;

  /**
   * @param initialCapacity Anfangskapazität
   */
  public Store(final int initialCapacity) {
    super();
    store = new HashMap<>(initialCapacity);
  }

  /**
   *
   */
  public Store() {
    super();
    store = new HashMap<>(defaultCapacity);

  }

  public int size() {
    return store.size();
  }

  /**
   * Liefert das unikale Objekt oder legt es an und speichert es.
   *
   * @param value beliebig
   * @return  das unikale Objekt.
   */
  public T get(final T value) {
    if (store.containsKey(value))
      return store.get(value);
    else {
      store.put(value, value);
      return value;
    }
  }

  public Set<T> getAll() {
    return store.keySet();
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final Store<Pair<Integer, Integer>> store = new Store<>();
    for (int i = 0; i < 1000; i++) {
      store.get(new Pair<>(i % 2, i % 2));
    }
    System.out.println(store.size());
  }

}
