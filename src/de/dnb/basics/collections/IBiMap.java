/**
 *
 */
package de.dnb.basics.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Eine in beide Richtungen funktionierende Abbildung; genauer eine Relation,
 * also eine Teilmenge des Kartesischen Produkten von K und V.
 *
 * @author baumann
 *
 * @param <K> Type der Keys
 * @param <V> Typ der Values
 */
public interface IBiMap<K, V> {

  void add(K index, V value);

  boolean containsKey(K key);

  boolean containsValue(V value);

  void clear();

  Iterator<K> keysIterator();

  /**
   *
   * @param key beliebig
   * @return  Werte ohne Wiederholungen, nicht null
   */
  Set<V> getValueSet(K key);

  /**
  *
  * @return Values als Menge für Join
  */
  Set<V> getValueSet();

  Iterator<V> valuesIterator();

  /**
  *
  * @param value beliebig
  * @return  Keys ohne Wiederholungen, nicht null
  */
  Set<K> getKeySet(V value);

  /**
  *
  * @return Keys als Menge für Join
  */
  Set<K> getKeySet();

  /**
   * Wenn es nur um die Suche geht, kann man einen speicheraufwendigen Join
   * vermeiden. Zum Join von m1 mit m2:
   *
   * <br><br><code>m2.searchValues(m1.searchValues(key2));</code>
   *
   * @param keys  nicht null
   * @return      nicht null.
   *              Für Suche: die zugehörigen Values. Die Reihenfolge
   *              der keys wird weitestgehend beibehalten, da
   *              LinkedHashSet.
   */
  default Set<V> searchValues(final Collection<K> keys) {
    Objects.requireNonNull(keys, "keys darf nicht null sein");
    final LinkedHashSet<V> ret = new LinkedHashSet<>();
    keys.stream().distinct().forEach(key -> ret.addAll(getValueSet(key)));
    return ret;
  }

  /**
  * Wenn es nur um die Suche geht, kann man einen speicheraufwendigen Join
  * vermeiden. Zum Join von m1 mit m2:
  *
  * <br><br><code>m2.searchValues(m1.searchValues(key2));</code>
  *
  * @param keys  nicht null
  * @return       nicht null. Für Suche: die zugehörigen Values.
  *               Die Reihenfolge
   *              der keys wird weitestgehend beibehalten, da
   *              LinkedHashSet.
  */
  default Set<V> searchValues(final K... keys) {
    return searchValues(Arrays.asList(keys));
  }

  /**
   * Wenn es nur um die Suche geht, kann man einen speicheraufwendigen Join
   * vermeiden. Zum Join von m1 mit m2:
   *
   * <br><br><code>m1.searchKeys(m2.searchKeys(key2));</code>
   *
   * @param values  nicht null
   * @return        für Suche: die zugehörigen Keys.
   *                Nicht null. Die Reihenfolge
   *              der values wird weitestgehend beibehalten, da
   *              LinkedHashSet.
   */
  default Set<K> searchKeys(final Collection<V> values) {
    Objects.requireNonNull(values, "values darf nicht null sein");
    final LinkedHashSet<K> ret = new LinkedHashSet<>();
    values.stream().distinct().forEach(key -> ret.addAll(getKeySet(key)));
    return ret;
  }

  /**
  * Wenn es nur um die Suche geht, kann man einen speicheraufwendigen Join
  * vermeiden. Zum Join von m1 mit m2:
  *
  * <br><br><code>m1.searchKeys(m2.searchKeys(key2));</code>
  * @param values  nicht null
  * @return       Nicht null. Für Suche: die zugehörigen Keys. Die Reihenfolge
   *              der values wird weitestgehend beibehalten, da
   *              LinkedHashSet.
  *
  */
  default Set<K> searchKeys(final V... values) {
    return searchKeys(Arrays.asList(values));
  }

  /**
   *
   * @return  umgekehrte BiMultimap. Diese ist nur eine Sicht auf
   *           die ursprünglichen Daten. Änderungen an der neuen
   *           Map wirken sich auch auf die alte aus.
   */
  IBiMap<V, K> inverseMap();

  /**
   * (K1, V1) * (V1, V2) -> (K1, V2)
   * @param <K1>    Typ des Keys der ersten
   * @param <V1>    Typ der Values des ersten und der Keys des zweiten
   * @param <V2>    Typ der Values des zweiten
   * @param bimap1  erste
   * @param bimap2  zweite
   * @return        join
   */
  static <
      K1, V1, V2>
    IBiMap<K1, V2>
    join(final IBiMap<K1, V1> bimap1, final IBiMap<V1, V2> bimap2) {
    final BiMultimap<K1, V2> biMultimap = BiMultimap.createListMap();
    final Set<V1> values1 = bimap1.getValueSet();
    values1.forEach(v1 ->
    {
      final Set<K1> keys1 = bimap1.getKeySet(v1);
      final Set<V2> values2 = bimap2.getValueSet(v1);
      CartesianProducts.cartesianPairStream(keys1, values2)
        .forEach(p -> biMultimap.add(p.first, p.second));
    });
    return biMultimap;
  }

  /**
  * Wenn es nur um die Suche geht, kann man einen speicheraufwendigen Join
  * vermeiden:
  *
  * key -> Values1 -> Values2
  *
  * @param <K1>    Typ des Keys der ersten
  * @param <V1>    Typ der Values des ersten und der Keys des zweiten
  * @param <V2>    Typ der Values des zweiten
  * @param bimap1  erste
  * @param bimap2  zweite
  * @param key     key der ersten, zu dem alle Values der zweiten gesucht werden
  * @return        Suchergebnis
   */
  static <
      K1, V1, V2>
    Set<V2>
    search(final IBiMap<K1, V1> bimap1, final IBiMap<V1, V2> bimap2, final K1 key) {
    final Set<V1> values1 = bimap1.getValueSet(key);
    final Set<V2> values2 = new HashSet<>();
    values1.forEach(v1 ->
    {
      values2.addAll(bimap2.getValueSet(v1));
    });
    return values2;
  }

  /**
   * (K, V) * (V, V2) -> (K, V2)
   *
   * @param <V2>        Typ der anderen Values
   * @param otherBiMapI andere, die eigenen Values und die Keys der anderen stimmen überein
   * @return            Join
   */
  default <V2> IBiMap<K, V2> joinRight(final IBiMap<V, V2> otherBiMapI) {
    return join(this, otherBiMapI);
  }

  /**
   * (K1, K) * (K, V) -> (K1, V)
   *
   * @param <K1>        Typ  der anderen Keys
   * @param otherBiMapI andere, die eigenen Keys und die Values der anderen stimmen überein
   * @return            Join
   */
  default <K1> IBiMap<K1, V> joinLeft(final IBiMap<K1, K> otherBiMapI) {
    return join(otherBiMapI, this);
  }

  /**
   * (K, V) * (K, V2) -> (V, V2)
   *
   * @param <V2>        Typ der anderen Values
   * @param otherBiMapI andere, die eigenen Keys und die Keys der anderen stimmen überein
   * @return            Join
   */
  default <V2> IBiMap<V, V2> joinKeys(final IBiMap<K, V2> otherBiMapI) {
    return join(this.inverseMap(), otherBiMapI);
  }

  /**
   * (K, V) * (K2, V) -> (K, K2)
   *
   * @param <K2>        Typ der anderen Keys
   * @param otherBiMapI andere, die eigenen Values und die Values der anderen stimmen überein
   * @return            Join
   */
  default <K2> IBiMap<K, K2> joinValues(final IBiMap<K2, V> otherBiMapI) {
    return join(this, otherBiMapI.inverseMap());
  }

  /**
   * Gibt einen relationalen Datenbankblick auf die BiMap: Jeder
   * key wird mit jedem value gepaart ausgegeben.
   * <br>Doppelte Tabellenzeilen werden aber weggefiltert.
   * Das kann bei Multigraphen einen falschen Eindruck
   * vermitteln!
   * @return
   */
  default String toTable() {
    final StringBuffer table = new StringBuffer();
    for (final K key : getKeySet()) {
      for (final V value : getValueSet(key)) {
        table.append(key);
        table.append('\t');
        table.append(value);
        table.append('\n');
      }
    }
    return table.toString();
  }

}
