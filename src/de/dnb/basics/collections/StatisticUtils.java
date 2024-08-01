/**
 *
 */
package de.dnb.basics.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * @author baumann
 *
 */
public class StatisticUtils {

  /**
   * Eine Darstellung eines Kreuzproduktes, das von 2
   * Parametern abhängt als (Excel-)Tabelle:
   * </br>
   * </br>
   * <table>
   * <tr>
   *      <th>rowheading<th>
   *      <th>Überschrift 1<th>
   *      <th>Überschrift 2<th>
   *      <th>...<th>
   * </tr>
   * <tr>
   *      <th>Zeile 1<th>
   *      <th>V<sub>11</sub><th>
   *      <th>V<sub>12</sub><th>
   *      <th>...<th>
   * </tr>
   * <tr>
   *      <th>Zeile 2<th>
   *      <th>V<sub>21</sub><th>
   *      <th>V<sub>22</sub><th>
   *      <th>...<th>
   * </tr>
   * </table>
   * @param crossproduct     nicht null, nur zwei verschiedene Parameter (wird
   *                      nicht überprüft)
   * @param rowHeading    Überschrift der 1. Spalte
   * @param rows          Zeilen
   * @param columns       Spalten
   * @param nullValue     Ersatzwert für V = null
   * @param <V>           Typ
   * @return              Eine Tabellenrepräsentation
   */
  public static <V> String getTableFrom(
    final CrossProduct<V> crossproduct,
    final String rowHeading,
    final Iterable<?> rows,
    final Iterable<?> columns,
    final String nullValue) {
    final StringBuffer s = new StringBuffer(rowHeading);
    columns.forEach(col ->
    {
      s.append("\t" + col);
    });
    rows.forEach(row ->

    {
      s.append("\n" + row);
      columns.forEach(col ->
      {
        final V value = crossproduct.get(row, col);
        if (value != null)
          s.append("\t" + value);
        else
          s.append("\t" + nullValue);
      });
    });
    return s.toString();
  }

  /**
   *
   * @param frequency Eine 2-dimensionale Häufigkeitsverteilung,
   *                  die als Tabelle angesehen wird. Nicht null.
   * @return          Die Summe der Spalteneinträge in jeder Zeile
   */
  public static CrossProductFrequency getRowSum(final CrossProductFrequency frequency) {
    return getSumForIndex(frequency, 1);
  }

  /**
  *
  * @param frequency Eine 2-dimensionale Häufigkeitsverteilung,
  *                  die als Tabelle angesehen wird. Nicht null.
  * @return          Die Summe der Zeileneinträge in jeder Spalte
  */
  public static CrossProductFrequency getColumnSum(final CrossProductFrequency frequency) {
    return getSumForIndex(frequency, 0);
  }

  /**
   * Eine Art Tensorverjüngung. Es wird eine neue Verteilung zurückgegeben,
   * die einen Index weniger enthält. Über diesen Index wird summiert.
   *
   *
   * @param frequency Eine n-dimensionale Häufigkeitsverteilung f. Nicht null.
   * @param ind     Die Position des Index, über den summiert wird.
   * @return          <code>&sum;
   *                  <sub>i<sub>ind</sub></sub>
   *                  f
   *                  <sub>i<sub>1</sub>,
   *                  i<sub>2</sub>,...,
   *                  i<sub>ind</sub>,...,
   *                  i<sub>n</sub></sub></code>
   * @throws IndexOutOfBoundsException
   *                  wenn ind unpassend gewählt
   */
  public static
    CrossProductFrequency
    getSumForIndex(final CrossProductFrequency frequency, final int ind)
      throws IndexOutOfBoundsException {
    final CrossProductFrequency rowFreq = new CrossProductFrequency();
    frequency.forEach(keys ->
    {
      final List<?> list = reduceKeys(ind, keys);
      rowFreq.increment(list, frequency.get(keys));
    });
    return rowFreq;
  }

  /**
   * @param ind
   * @param keys
   * @return
   */
  private static List<?> reduceKeys(final int ind, final Collection<? extends Object> keys) {
    final List<?> list = ListUtils.convertToModifiableList(keys);
    list.remove(ind);
    return list;
  }

  /**
   * Wandel eine Häufigkeitstabelle in Prozentwerte um.
   * In der Regel wird man für eine Tabelle
   * <code>index = 1</code>
   * für Zeilen und
   * <code>index = 0</code>
   * für Spalten wählen.
   *
   * @param frequency nicht null
   * @param index     innerhalb der Indexgrenzen liegend
   * @return          Eine prozentuale Auflistung für
   *                  den gewählten Summationsindex.
   */
  public static
    CrossProductMap<Double>
    getPercents(final CrossProductFrequency frequency, final int index) {
    final CrossProductMap<Double> map = new CrossProductMap<>();
    final CrossProductFrequency sums = getSumForIndex(frequency, index);
    frequency.forEach(keys ->
    {
      final List<?> reducedKey = reduceKeys(index, keys);
      final long sum = sums.get(reducedKey);
      final long count = frequency.get(keys);
      final double percents = (double) count / (double) sum * 100.0d;
      map.put(percents, keys);
    });
    return map;
  }

  /**
  *
  *
  *  Die V-Werte sind möglicherweise total durcheinander. Sie werden
  * zunächst in die "natürliche" Reihenfolge gebracht. Danach werden die
  * Häufigkeiten der zugehörigen Werte von map aufsummiert. Dadurch
  * bekommt man eine streng monoton steigende Funktion, die
  * (<a href="https://de.wikipedia.org/wiki/Verteilungsfunktion">Verteilungsfunktion</a>)  *
  * @param <V> typ
  * @param map Häufigkeit = "Wahrscheinlichkeitsdichte" in natürlicher Reihenfolge
  *             der V-Werte.
  *
  * @return    Funktion proportional der Verteilungsfunktion: P((-∞,x]).
  *            Proportionalitätsfaktor ist die Summe über map.
  */
  public static <
      V extends Comparable<? super V>>
    BiMap<V, Long>
    getCDF(final Map<? extends V, Long> map) {
    return getCDF(map, false);
  }

  /**
   *
   * Die V-Werte sind möglicherweise total durcheinander. Sie werden
   * zunächst in die "richtige" Reihenfolge gebracht. Danach werden die
   * Häufigkeiten der zugehörigen Werte von map aufsummiert. Dadurch
   * bekommt man eine streng monoton steigende Funktion, die
   * (<a href="https://de.wikipedia.org/wiki/Verteilungsfunktion">Verteilungsfunktion</a>)
   *
   * @param <V> typ
   * @param map Häufigkeit = "Wahrscheinlichkeitsdichte"
   * @param descending false -> in natürlicher Ordnung, true -> umgekehrt
   * @return    Funktion proportional der Verteilungsfunktion: P((-∞,x]).
   *            Proportionalitätsfaktor ist die Summe über map.
   */
  public static <V extends Comparable<? super V>> BiMap<V, Long> getCDF(
    final Map<? extends V, Long> map,
    final boolean descending) {
    RangeCheckUtils.assertMapParamNotNullOrEmpty("map", map);
    final BiMap<V, Long> biMap = new BiMap<>();
    final AtomicLong sum = new AtomicLong();

    NavigableSet<V> ts = new TreeSet<>(map.keySet());
    if (descending)
      ts = ts.descendingSet();

    ts.forEach(v ->
    {
      final Long c = map.get(v);
      if (c == null || c.longValue() == 0L)
        return;
      final long intermed = sum.addAndGet(c);
      biMap.add(v, intermed);
    });

    return biMap;
  }

  /**
   *
   * @param <V> int, long, double, float
   * @param cdf Verteilungsfunktion, nicht null, streng monoton
   * @param p   0<=p<=1, Anteil der Werte unterhalb des Rückgabewertes
   *
   * @return    Quantil. Für dieses gilt, dass der Anteil der Werte kleiner als
   *            dieser Wert gleich p ist.
   */
  public static <V extends Number> Number getQuantile(final BiMap<V, Long> cdf, final double p) {
    RangeCheckUtils.assertMapParamNotNullOrEmpty("cdf", cdf);
    final TreeSet<Long> valSet = new TreeSet<>(cdf.getValueSet());
    final double fraction = p * getCDFcount(cdf);
    final Long first = valSet.first();
    if (fraction < first)
      return cdf.getKey(first);
    final Long last = valSet.last();
    if (fraction >= last)
      return cdf.getKey(last);

    final long trunc = (long) fraction;
    final boolean fracIsInt = (fraction - trunc) == 0;
    if (fracIsInt) {
      if (!valSet.contains(trunc)) {
        return cdf.getKey(valSet.ceiling(trunc));
      } else {
        final V xi = cdf.getKey(trunc);
        final V xi_plus_1 = cdf.getKey(valSet.ceiling(trunc + 1));
        if (xi instanceof Long) {
          return (xi.longValue() + xi_plus_1.longValue()) / 2;
        }
        if (xi instanceof Integer) {
          //          System.err.println(xi);
          //          System.err.println(xi_plus_1);
          return (xi.intValue() + xi_plus_1.intValue()) / 2;
        }
        if (xi instanceof Float) {
          return (xi.floatValue() + xi_plus_1.floatValue()) / 2;
        }
        if (xi instanceof Double) {
          return (xi.doubleValue() + xi_plus_1.doubleValue()) / 2;
        }
      }
    }
    return cdf.getKey(valSet.ceiling(trunc + 1));
  }

  /**
   *
   * @param <V> Key-Typ
   * @param cdf streng monoton, nicht null, nicht leer
   * @return    eine Tabellendarstellung, beginnen bei den kleinsten Werten
   */
  public static <V extends Comparable<? super V>> String cdf2string(final Map<V, Long> cdf) {
    RangeCheckUtils.assertMapParamNotNullOrEmpty("cdf", cdf);
    // Richtung feststellen:
    final NavigableSet<V> ts = orderKeys(cdf);
    final StringBuffer buffer = new StringBuffer("CDF:\n");
    ts.forEach(v -> buffer.append(v + "\t" + cdf.get(v) + "\n"));
    return buffer.toString();
  }

  /**
  *
  * @param <V>        Key-Typ
  * @param map        nicht null, nicht leer
  * @return           eine Tabellendarstellung, keys der Reihenfolge nach
  *                   geordnet
  */
  public static <V extends Comparable<? super V>> String map2string(final Map<V, ?> map) {
    return map2string(map, false);
  }

  /**
  *
  * @param <V>        Key-Typ
  * @param map        nicht null, nicht leer
  * @param descending beginnend mit dem größten Key?
  * @return           eine Tabellendarstellung, keys geordnet
  */
  public static <V extends Comparable<? super V>> String map2string(
    final Map<V, ?> map,
    final boolean descending) {
    RangeCheckUtils.assertMapParamNotNullOrEmpty("map", map);
    // Richtung feststellen:
    NavigableSet<V> ts = new TreeSet<>(map.keySet());
    if (descending)
      ts = ts.descendingSet();
    final StringBuffer buffer = new StringBuffer("Map:\n");
    ts.forEach(v -> buffer.append(v + "\t" + map.get(v) + "\n"));
    return buffer.toString();
  }

  /**
   * @param <V> Typ der keys
   * @param cdf monotone Funktion
   * @return    keys in steigender Reihenfolge der Werte
   */
  private static <
      V extends Comparable<? super V>>
    NavigableSet<V>
    orderKeys(final Map<V, Long> cdf) {
    NavigableSet<V> ts = new TreeSet<>(cdf.keySet());
    if (cdf.get(ts.first()) > cdf.get(ts.last())) {
      ts = ts.descendingSet();
    }
    return ts;
  }

  /**
   * @param <V> Typ der keys
   * @param map monotone Funktion
   * @return    größter und kleinster key
   */
  public static <V extends Comparable<? super V>> Pair<V, V> getRange(final Map<V, ?> map) {
    final TreeSet<V> ts = new TreeSet<>(map.keySet());
    return new Pair<V, V>(ts.first(), ts.last());
  }

  /**
   *
   * @param <V> Typ
   * @param cdf nicht null, monoton steigend
   * @return    Anzahl der Einträge des zugrundeliegenden Histogramms
   *            = Summe über Häufigkeiten = erster/letzter Wert von cdf
   */
  public static <V> long getCDFcount(final BiMap<V, Long> cdf) {
    RangeCheckUtils.assertMapParamNotNullOrEmpty("cdf", cdf);
    return Collections.max(cdf.values());
  }

  /**
   *
   * @param <K> keys
   * @param <V> values, müssen {@link Comparable} implementieren
   * @param map Map. Diese enthält die Zuordnung key->value wild durcheinander.
   * @return    eine Map rank->(key, value). Die Key-Value-Paare sind nach ihrem Rang
   *            sortiert. Das Paar mit dem höchsten Wert an der Position 0, das zweithöchste
   *            an der Position 1, usw.
   */
  public static <K, V extends Comparable<V>> Map<Integer, Pair<K, V>> rankMap(final Map<K, V> map) {
    @SuppressWarnings("unchecked")
    final K[] keys = (K[]) map.keySet().toArray();
    Arrays.sort(keys, Comparator.comparing(map::get).reversed());
    final Map<Integer, Pair<K, V>> newMap = new HashMap<>();
    for (int i = 0; i < keys.length; i++) {
      final K key = keys[i];
      final V value = map.get(key);
      newMap.put(i, new Pair<>(key, value));
    }
    return newMap;
  }

  public static <K> void increment(final Map<K, Double> map, final K key, final double increment) {
    map.merge(key, increment, Double::sum);
  }

  public static <K> void increment(final Map<K, Float> map, final K key, final float increment) {
    map.merge(key, increment, Float::sum);
  }

  public static <K> void increment(final Map<K, Long> map, final K key, final long increment) {
    map.merge(key, increment, Long::sum);
  }

  public static <K> void increment(final Map<K, Integer> map, final K key, final int increment) {
    map.merge(key, increment, Integer::sum);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final Map<String, Integer> freq = new HashMap<>();

    increment(freq, "a", 1);
    increment(freq, "a", 1);

    System.out.println(freq);
    System.out.println(rankMap(freq));

  }

}
