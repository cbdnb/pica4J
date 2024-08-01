/**
 *
 */
package de.dnb.basics.collections;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.Equals;
import de.dnb.basics.filtering.Greater;
import de.dnb.basics.filtering.GreaterOrEqual;
import de.dnb.basics.filtering.Lower;
import de.dnb.basics.filtering.LowerOrEqual;
import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * Klasse, die eine Häufigkeitsverteilung implementiert. Objekte des Typs <V>
 * (values) werden gezählt und können mit Häufigkeit ausgegeben werden.
 * Diese Objekte vom Typ V können auch null sein.
 *
 * @param <V>	Typ der Objekte, die gezählt werden.
 *
 * @author Christian_2
 *
 */
public class Frequency<V> implements Iterable<V>, Serializable, Map<V, Long> {

  private static final long serialVersionUID = -959798635321945008L;
  protected Map<V, Long> values2long;

  /**
   * Erhöht den Zähler für value um 1, legt diesen an,
   * wenn noch nicht vorhanden.
   *
   * @param value auch null
   *
   */
  public void add(final V value) {
    increment(value, 1L);
  }

  /**
   * Legt einen neuen Schlüssel an,
   * wenn noch nicht vorhanden. Der Wert
   * eines neuen Schlüssels ist 0.
   *
   * @param value auch null
   */
  public void addKey(final V value) {
    increment(value, 0);
  }

  /**
   * Inkrementiert für jeden Wert von other um den Betrag in other (sofern dieser
   * ungleich null).
   *
   * @param other  nicht null
   */
  public final void addAll(final Map<? extends V, ? extends Long> other) {
    RangeCheckUtils.assertReferenceParamNotNull("other", other);
    other.forEach((v, l) ->
    {
      if (l != null)
        increment(v, l);
    });
  }

  /**
   * Inkrementiert für jeden Wert von other um 1.
   *
   * @param c  nicht null
   */
  public final void addCollection(final Collection<V> c) {
    RangeCheckUtils.assertReferenceParamNotNull("other", c);
    c.forEach((v) -> increment(v, 1));
  }

  /**
   *
   * Fügt increment Einträge für value hinzu.
   *
   * @param value		auch null
   * @param increment	Differenz, kann auch 0 oder negativ(!) sein
   */
  public void increment(final V value, final long increment) {
    Long l = values2long.get(value);
    if (l == null) {
      l = Long.valueOf(increment);
    } else {
      l += increment;
    }
    values2long.put(value, l);
  }

  /**
   * Setzt zurück.
   */
  @Override
  public final void clear() {
    values2long.clear();
  }

  /**
   *
   * @return Häufigkeitverteilung als Menge von {@link Entry}.
   */
  public final Set<Map.Entry<V, Long>> getEntries() {
    return values2long.entrySet();
  }

  /**
   *
   * @return Häufigkeitverteilung als Menge von {@link Pair}.
   */
  public final Collection<Pair<V, Long>> getDistribution() {
    return Pair.getPairs(getEntries());
  }

  /**
  *
  * @return Häufigkeitverteilung als Menge von {@link Pair}, beginnend mit dem häufigsten
  *         Eintrag.
  */
  public final Collection<Pair<V, Long>> getOrderedDistribution() {
    final List<Pair<V, Long>> dist = new ArrayList<>(getDistribution());
    final Comparator<Pair<V, Long>> myComp = Comparator.comparing(Pair::getSecond);
    Collections.sort(dist, myComp.reversed());
    return dist;
  }

  /**
   *
   * @return	Iterator über die gezählten Werte + Anzahl
   */
  public final Iterator<Map.Entry<V, Long>> entrySetIterator() {
    return values2long.entrySet().iterator();
  }

  /**
   *
   * @return	Iterator über die gezählten Werte
   */
  public final Iterator<V> valuesIterator() {
    return values2long.keySet().iterator();
  }

  /**
   *
   * @return Summe aller Häufigkeiten
   */
  public final long getSum() {
    long l = 0;
    for (final Long n : values2long.values()) {
      l += n;
    }
    return l;
  }

  /**
   *
   * @param value	auch null
   * @return		Anzahl der Einträge von value,
   *              insbesondere 0, wenn kein Eintrag vorhanden
   */
  @Override
  public final Long get(final Object value) {
    final Long l = values2long.get(value);
    if (l == null)
      return 0L;
    else
      return l;
  }

  /**
   *
   * @return  Zahl der Einträge
   */
  @Override
  public final int size() {
    return values2long.size();
  }

  /**
   *
   * @return  Durchschnitt oder NaN, wenn noch keine Einträge
   */
  public final double getAverage() {
    return (double) getSum() / (double) size();
  }

  /**
   *
   * @param count   Wert
   * @return      alle, für die Häufigkeit gleich count ist
   */
  public final Collection<V> getEquals(final long count) {
    return filterKeysByFrequency(new Equals<Long>(count));
  }

  /**
   *
   * @param frequencyPredicate    Ein Filterkriterium für die Häufigkeiten
   * @return                      Liste der Werte, deren Häufigkeiten
   *                              das Kriterium erfüllen. Für die Art
   *                              der Liste gibt es keine Garantie!
   */
  public final Collection<V> filterKeysByFrequency(final Predicate<Long> frequencyPredicate) {
    Objects.requireNonNull(frequencyPredicate);
    //@formatter:off
        final List<V> list = entrySet()
                            .stream()
                            .filter(entry -> frequencyPredicate
                                                .test(entry.getValue()))
                            .map(entry -> entry.getKey())
                            .collect(Collectors.toList());
        return list;
        //@formatter:on
  }

  /**
   *
   * @param min   kleinster Wert
   * @return      alle, für die Häufigkeit größer als min ist
   */
  public final Collection<V> getGreater(final long min) {
    return filterKeysByFrequency(new Greater<Long>(min));
  }

  /**
   *
   * @param min   kleinster Wert
   * @return      alle, für die Häufigkeit größer oder gleich min ist
   */
  public final Collection<V> getGreaterOrEqual(final long min) {
    return filterKeysByFrequency(new GreaterOrEqual<Long>(min));
  }

  /**
   *
   * @param max   größter Wert
   * @return      alle, für die Häufigkeit größer oder gleich min ist
   */
  public final Collection<V> getLowerOrEqual(final long max) {
    return filterKeysByFrequency(new LowerOrEqual<Long>(max));
  }

  /**
   *
   * @param max   größter Wert
   * @return      alle, für die Häufigkeit größer alsh min ist
   */
  public final Collection<V> getLower(final long max) {
    return filterKeysByFrequency(new Lower<Long>(max));
  }

  /**
   * Gibt die max häufigsten Einträge als Liste, beginnend mit dem häufigsten.
   *
   * @param max Maximalzahl der betrachteten "Rekorde"
   * @return  "Rekorde"
   *
   */
  public final List<Pair<V, Long>> getRecords(final int max) {
    final Comparator<V> freqComparator = (o1, o2) -> (int) (get(o1) - get(o2));
    final BoundedPriorityQueue<V> priorityQueue = new BoundedPriorityQueue<>(max, freqComparator);
    keySet().forEach(key -> priorityQueue.add(key));
    final List<Pair<V, Long>> pairs = new ArrayList<>(max);
    priorityQueue.ordered().forEach(v -> pairs.add(new Pair<>(v, get(v))));
    return pairs;
  }

  /**
   *
   * @param value	auch null
   * @return		value enthalten und ungleich 0
   */
  public final boolean contains(final Object value) {
    return get(value) != 0L;
  }

  /**
   *
   * @param value Schlüssel
   * @return      Schlüssel in der zugrundeliegenden Map enthalten
   */
  @Override
  public final boolean containsKey(final Object value) {
    return values2long.containsKey(value);
  }

  @Override
  public String toString() {
    final Set<Map.Entry<V, Long>> set = getEntries();
    String s = "";
    for (final Iterator<Entry<V, Long>> iterator = set.iterator(); iterator.hasNext();) {
      final Map.Entry<V, Long> entry = iterator.next();
      s += entry.getKey() + "\t" + entry.getValue();
      if (iterator.hasNext())
        s += "\n";
    }
    return s;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {
    final Frequency<Character> frequency = new Frequency<>();
    for (int i = 1; i <= 69; i++) {
      frequency.increment((char) (i + 50), i);
    }
    System.out.println(frequency.getOrderedDistribution());

  }

  @Override
  public final Iterator<V> iterator() {
    return valuesIterator();
  }

  /**
   * Hilfsmethode: <code>getDistribution().stream()</code>
   *
   * @return  Strem von Paaren.
   */
  public Stream<Pair<V, Long>> pairStream() {
    return getDistribution().stream();
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

  @SuppressWarnings("unchecked")
  /**
   * Lädt eine Häufigkeitsverteilung.
   *
   * @param fileName                  nicht null
   * @return                          eine Häufigkeitsverteilung,
   *                                  die in einer Datei gespeichert war
   * @throws ClassNotFoundException   Wenn nicht in Frequency konvertiert
   *                                  werden kann
   * @throws IOException              sonst
   */
  public static <
      V>
    Frequency<V>
    load(final String fileName) throws ClassNotFoundException, IOException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    final Frequency<V> freq = (Frequency<V>) objectin.readObject();
    FileUtils.safeClose(objectin);
    return freq;
  }

  /**
   *
   * @return Die Werte, siehe {@link Map#keySet()}
   */
  @Override
  public Set<V> keySet() {
    return values2long.keySet();
  }

  /**
   *
   */
  public Frequency() {
    values2long = new HashMap<V, Long>();
  }

  /**
   * Lädt eine Häufigkeitsverteilung.
   *
   * @param fileName                  nicht null
   *                                  die in einer Datei gespeichert war
   * @throws ClassNotFoundException   Wenn nicht in Frequency konvertiert
   *                                  werden kann
   * @throws IOException              sonst
   */
  public Frequency(final String fileName) throws IOException, ClassNotFoundException {
    this();
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final Frequency<V> readObject = (Frequency<V>) objectin.readObject();
    final Frequency<V> freq = readObject;
    FileUtils.safeClose(objectin);
    addAll(freq);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((values2long == null) ? 0 : values2long.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Frequency other = (Frequency) obj;
    if (values2long == null) {
      if (other.values2long != null)
        return false;
    } else if (!values2long.equals(other.values2long))
      return false;
    return true;
  }

  @Override
  public boolean isEmpty() {
    return values2long.isEmpty();
  }

  @Override
  public boolean containsValue(final Object value) {
    return values2long.containsValue(value);
  }

  @Override
  public Long put(final V key, final Long value) {
    return values2long.put(key, value);
  }

  @Override
  public Long remove(final Object key) {
    return values2long.remove(key);
  }

  @Override
  public void putAll(final Map<? extends V, ? extends Long> m) {
    values2long.putAll(m);
  }

  @Override
  public Collection<Long> values() {
    return values2long.values();
  }

  @Override
  public Set<Entry<V, Long>> entrySet() {
    return values2long.entrySet();
  }

  public void write2File(final String filename) throws IOException {
    final PrintWriter pw = FileUtils.oeffneAusgabeDatei(filename, false);
    pw.print(toString());
    FileUtils.safeClose(pw);
  }

}
