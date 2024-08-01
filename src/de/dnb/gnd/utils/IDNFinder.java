/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.collections.BiMultimap;
import de.dnb.basics.collections.IBiMap;
import de.dnb.basics.tries.TST;
import de.dnb.basics.tries.Trie;

/**
 * @author baumann
 *
 */
public class IDNFinder {

  private final IBiMap<Integer, String> idn2nameNorm;

  // Namen in der ursprünglichen Form für lesbare Ausgabe:
  private final IBiMap<Integer, String> idn2name;

  private final IBiMap<Integer, String> idns2verweisungenNorm;

  // für die Präfix-Suche:
  private final Trie<Collection<Integer>> trieName2idns = new TST<>();

  private final Trie<Collection<Integer>> trieVerweisung2idns = new TST<>();

  final Triplett<Collection<String>, Collection<String>, Integer> keinTreffer;

  /**
   * Die Parameter werden normiert, d.h. getrimmt, kleingeschrieben und
   * Unicode-komprimiert.
   *
   * @param idn2name
   *            1XX-Felder, eine Funktion (keine allgemeine Relation), aber
   *            wegen möglicher Namesgleichehit nicht unbedingt injektiv.
   * @param idns2verweisungen
   *            4XX-Felder, möglicherweise eine n:m-Verbindung
   */
  public IDNFinder(
    final IBiMap<Integer, String> idn2name,
    final IBiMap<Integer, String> idns2verweisungen) {
    super();
    Objects.requireNonNull(idn2name);
    Objects.requireNonNull(idns2verweisungen);
    this.idn2name = idn2name;
    idn2nameNorm = transform(idn2name);
    idns2verweisungenNorm = transform(idns2verweisungen);

    final Set<String> normNames = idn2nameNorm.getValueSet();
    final Set<String> normVerweisungen = idns2verweisungenNorm.getValueSet();

    normNames.forEach(name ->
    {
      final Set<Integer> ints = idn2nameNorm.getKeySet(name);
      trieName2idns.put(name, ints);
    });

    normVerweisungen.forEach(vw ->
    {
      final Set<Integer> ints = idns2verweisungenNorm.getKeySet(vw);
      trieVerweisung2idns.put(vw, ints);
    });
    keinTreffer = makeTriplett(Collections.emptyList(), 9);

  }

  /**
   * Findet mithilfe der übergebenen Tabellen die am besten passenden Idns,
   * zugehörige Namen und den Level.
   *
   * <pre>
   * Level:
   * 1	1:1 name = 1XX
   * 2	n:1 name = 1XX (wegen möglicher doppelter Namen)
   * 3	1:1 name = 4XX
   * 4	n:1 name = 4XX
   * 5	1:1 1XX als längstes Präfix von name
   * 6	n:1 1XX als längstes Präfix von name (wegen möglicher doppelter Namen)
   * 7	1:1 4XX als längstes Präfix von name
   * 8	n:1 4XX als längstes Präfix von name
   * 9	kein Treffer
   * </pre>
   *
   * @param name
   *            beliebig, wird, wenn nicht leer, in Klinbuchstaben und
   *            Unicode-Composition umgewandelt
   * @return Tripel oder NullTripel {@link Triplett#getNullTriplett()}
   */
  public Triplett<Collection<String>, Collection<String>, Integer> find(String name) {
    if (StringUtils.isNullOrWhitespace(name))
      return Triplett.getNullTriplett();
    // normieren:
    name = normiere(name);
    // Level 1 und 2:
    Collection<Integer> idns = idn2nameNorm.searchKeys(name);
    if (idns.size() == 1)
      return makeTriplett(idns, 1);
    if (idns.size() > 1)
      return makeTriplett(idns, 2);
    // Level 3 und 4:
    idns = idns2verweisungenNorm.searchKeys(name);
    if (idns.size() == 1)
      return makeTriplett(idns, 3);
    if (idns.size() > 1)
      return makeTriplett(idns, 4);
    // Level 5 und 6:
    idns = trieName2idns.getValueOfLongestPrefix(name);
    if (idns != null) {
      if (idns.size() == 1)
        return makeTriplett(idns, 5);
      if (idns.size() > 1)
        return makeTriplett(idns, 6);
    }
    // Level 7 und 8:
    idns = trieVerweisung2idns.getValueOfLongestPrefix(name);
    if (idns != null) {
      if (idns.size() == 1)
        return makeTriplett(idns, 7);
      if (idns.size() > 1)
        return makeTriplett(idns, 8);
    }
    return keinTreffer;

  }

  /**
   * @param name
   * @return
   */
  public static String normiere(String name) {
    name = name.trim();
    name = name.toLowerCase();
    name = StringUtils.unicodeComposition(name);
    return name;
  }

  private
    Triplett<Collection<String>, Collection<String>, Integer>
    makeTriplett(final Collection<Integer> intIdns, final Integer level) {
    final List<String> idns = IDNUtils.ints2ppns(intIdns);
    final Collection<String> names = idn2name.searchValues(intIdns);
    return new Triplett<>(idns, names, level);
  }

  public static void main(final String[] args) {
    final String ae = StringUtils.readClipboard();
    final BiMultimap<Integer, String> map = BiMultimap.createSetMap();
    map.add(1, " Äther ");
    map.add(1, "B");
    map.add(2, ae);
    map.add(2, "B");
    System.out.println(map);
    System.out.println();
    System.out.println(transform(map));
  }

  private static BiMultimap<Integer, String> transform(final IBiMap<Integer, String> source) {
    final BiMultimap<Integer, String> dest = BiMultimap.createSetMap();
    source.getKeySet().forEach(idn -> source.getValueSet(idn).forEach(value ->
    {
      dest.add(idn, normiere(value));
      // Alle Sonderzeichen weg, ob das gut geht?
      value = value.replaceAll("\\p{Punct}", "");
      dest.add(idn, normiere(value));
    }));
    return dest;
  }

}
