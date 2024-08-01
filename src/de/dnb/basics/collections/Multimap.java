package de.dnb.basics.collections;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.applicationComponents.NullIterator;
import de.dnb.basics.applicationComponents.strings.StringUtils;

/**
 * A collection similar to a Map, but which may associate multiple
 * values with a single key. If you call add(K, V) twice, with the same key
 * but different values, the multimap contains mappings from the key to
 * both values.
 *
 * Depending on the implementation, a multimap may or may not allow duplicate
 * key-value pairs. In other words, the multimap contents after adding the same
 * key and value twice varies between implementations. In multimaps allowing
 * duplicates, the multimap will contain two mappings, and get will return
 * a collection that includes the value twice. In multimaps not supporting
 * duplicates, the multimap will contain a single mapping from the key to the
 * value, and get will return a collection that includes the value once.
 *
 * @author Christian_2
 *
 * @param <K>
 * @param <V>
 */
public abstract class Multimap<K, V> implements Iterable<K>, Serializable, IMultimap<K, V> {

  /**
   *
   */
  private static final long serialVersionUID = 9199230459419376721L;

  /**
   * Nicht final wegen Serialisierung.
   */
  protected Map<K, Collection<V>> map;

  protected Multimap(final Map<K, Collection<V>> values) {
    super();
    map = values;
  }

  /**
   * Parameterlos wegen Serialisierung.
   */
  public Multimap() {
  }

  /**
   * Muss in einer Unterklasse überschrieben werden.
   *
   * @return	neue Collection der Werte
   */
  protected abstract Collection<V> getNewValueCollection();

  /**
   * Füge weiteren Wert zum Schlüssel hinzu.
   *
   * @param index		kann je nach Implementierung auch null sein
   * @param value		kann je nach Implementierung auch null sein
   */
  @Override
  public final void add(final K index, final V value) {
    Collection<V> vColl = map.get(index);
    if (vColl == null) {
      vColl = getNewValueCollection();
      map.put(index, vColl);
    }
    vColl.add(value);
  }

  @Override
  public final void addAll(final K index, final V... values) {
    Objects.requireNonNull(values);
    addAll(index, Arrays.asList(values));
  }

  @Override
  public final void addAll(final K index, final Iterable<V> values) {
    Objects.requireNonNull(values);
    values.forEach(v -> add(index, v));
  }

  /**
   * Fügt alle Werte einer anderen Multimap
   * zur eigenen hinzu.
   *
   * @param other nicht null
   */
  public final void addAll(final Multimap<K, V> other) {
    Objects.requireNonNull(other);
    other.forEach(k ->
    {
      addAll(k, other.get(k));
    });

  }

  /**
   * Erzeugt einen leeren Eintrag für den Schlüssel key, wenn die zugehörigen
   * Werte noch unbekannt sind. Sonst keine Aktion.
   *
   * @param key	Schlüssel, kann je nach Implementierung auch null sein
   */
  public final void add(final K key) {
    Collection<V> vColl = map.get(key);
    if (vColl == null) {
      vColl = getNewValueCollection();
      map.put(key, vColl);
    }
  }

  /**
   *
   * @param key	beliebig
   * @return		key in Multimap enthalten. Das ist auch dann der Fall,
   * 				wenn über Aufruf von {@link #add(Object)} eine
   * 				leere Value-Collection erzeugt wurde
   */
  @Override
  public final boolean containsKey(final K key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(final V value) {
    for (final Collection<V> collection : map.values()) {
      if (collection.contains(value)) {
        return true;
      }
    }
    return false;
  }

  public boolean containsEntry(final Object key, final Object value) {
    final Collection<V> collection = map.get(key);
    return collection != null && collection.contains(value);
  }

  /**
   * Zurücksetzen.
   */
  @Override
  public final void clear() {
    map.clear();
  }

  @Override
  public Collection<V> remove(final K key) {
    return map.remove(key);
  }

  /**
   * Iterator über Schlüssel.
   *
   * @return	nicht null
   */
  @Override
  public final Iterator<K> keysIterator() {
    return map.keySet().iterator();
  }

  /**
   *
   * @return  nicht null
   */
  @Override
  public final Set<K> getKeySet() {
    return map.keySet();
  }

  @Override
  public final Iterator<K> iterator() {
    return keysIterator();
  }

  /**
   *
   * Gibt eine neue Collection der Werte zu key. Diese kann verändert
   * werden. Vorsicht ist geboten, wenn der Zustand Werte selbst
   * verändert wird.
   *
   * @param key	Schlüssel
   * @return		Die Werte, die zum Schlüssel gehören (das kann auch eine
   * 				leere Collection sein) oder null, wenn kein
   * 				Mapping vorliegt
   */
  @Override
  public Collection<V> get(final K key) {
    final Collection<V> collectionForKey = map.get(key);
    if (collectionForKey == null)
      return null;
    else {
      final Collection<V> valueCollection = getNewValueCollection();
      valueCollection.addAll(collectionForKey);
      return valueCollection;
    }
  }

  /**
   *
   * @param key	Schlüssel
   * @return		Eine neue Collection der Werte zu key, nicht null,
   * 				eventuell leer, veränderbar.
   */
  public final Collection<V> getNullSafe(final K key) {
    final Collection<V> kValues = get(key);
    if (kValues != null)
      return kValues;
    else
      return new LinkedList<>();
  }

  /**
   * Gibt eine neue Collection vom Typ, der von
   * {@link #getNewValueCollection()} geliefert wird, und die alle
   * Werte enthält.
   *
   * @return	nicht null. Eventuell leer, je nach Typ der Value-Collection
   * 			kann auch jeder value einmal oder mahrmals auftreten.
   */
  public final Collection<V> flatten() {
    final Collection<V> valueCollection = getNewValueCollection();
    map.values().forEach(coll -> valueCollection.addAll(coll));
    return valueCollection;
  }

  /**
   *
   * @return 	Iterator über alle Werte. Kommt ein Wert in mehreren
   * 			Collections vor, so wird er auch bei Iteration
   * 			mehrfach erfasst.
   */
  @Override
  public final Iterator<V> valuesIterator() {
    return new ValuesIterator();
  }

  private class ValuesIterator implements Iterator<V> {

    final Iterator<K> keysIterator = keysIterator();

    K actualKey;

    Collection<V> actualCollection;

    Iterator<V> actualValueIterator = new NullIterator<V>();

    @Override
    public boolean hasNext() {
      setPointerToNextNonemptyCollectionIfNecessary();
      return actualValueIterator.hasNext();
    }

    @Override
    public V next() {
      setPointerToNextNonemptyCollectionIfNecessary();
      return actualValueIterator.next();
    }

    /**
     * Für den Fall, dass der Zeiger auf das Ende einer Collection
     * zeigt:
     * 	setzt den Zeiger auf den Beginn der nächsten nichtleeren
     * 	Collection, wenn es noch eine gibt.
     */
    private void setPointerToNextNonemptyCollectionIfNecessary() {
      // nicht nötig?
      if (actualValueIterator.hasNext())
        return;
      getNextNonemptyCollectionIfExists();
    }

    /**
     *
     */
    private void getNextNonemptyCollectionIfExists() {
      while (keysIterator.hasNext()) {
        actualKey = keysIterator.next();
        // nicht get() verwenden, da neue Collection!
        actualCollection = map.get(actualKey);
        actualValueIterator = actualCollection.iterator();
        if (!actualCollection.isEmpty()) {
          return;
        }
      }
    }

    @Override
    public void remove() {
      actualValueIterator.remove();
    }

  }

  /**
   * Anzahl der Schlüssel.
   *
   * @return	Zahl
   */
  public int getKeyCount() {
    return map.size();
  }

  /**
   *
   * @return  Anzahl der in der Map gespeichertern Werte.
   */
  public int getValueCount() {
    int c = 0;
    for (final K key : getKeySet()) {
      c += getNullSafe(key).size();
    }
    return c;
  }

  @Override
  public String toString() {
    String s = "";
    for (final Iterator<K> iterator = keysIterator(); iterator.hasNext();) {
      final K k = iterator.next();
      final Collection<V> kValues = get(k);
      final String listAsString = StringUtils.concatenate("\t", kValues);
      s += k + "\t" + listAsString;
      if (iterator.hasNext())
        s += "\n";
    }
    return s;
  }

  /**
   *
   * Speichert als Datei ab.
   *
   * @param fileName                  nicht null
   * @throws FileNotFoundException    wenn nicht gefunden
   * @throws IOException              sonst
   */
  public final void safe(final String fileName) throws FileNotFoundException, IOException {
    final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
    out.writeObject(this);
    FileUtils.safeClose(out);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final Multimap<Character, Integer> multimap = new ListMultimap<>();
    multimap.add('a', 1);

    multimap.add('a', 1);

    multimap.add('a', 2);

    multimap.add('a', 3);

    multimap.add('b', 1);
    multimap.add('b', 1);
    multimap.add('b', 1);
    multimap.add('c');
    System.out.println(multimap);

    System.out.println(multimap.getValueCount());

  }

}
