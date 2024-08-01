/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringMetrics;
import de.dnb.basics.applicationComponents.strings.StringMetrics.StringDistance;
import de.dnb.basics.applicationComponents.strings.StringMetrics.Thesaurus;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.collections.BiMultimap;
import de.dnb.basics.collections.CollectionUtils;
import de.dnb.basics.collections.IBiMap;

/**
 * @author baumann
 *
 */
public class IDNFinderLeven {

  // Normiert
  private final IBiMap<Integer, String> idn2nameNorm;

  // Namen in der ursprünglichen Form für lesbare Ausgabe:
  private final IBiMap<Integer, String> idn2name;

  //Normiert
  private final IBiMap<Integer, String> idns2verweisungenNorm;

  private final Triplett<Collection<String>, Collection<String>, Integer> keinTreffer;

  private final Thesaurus thesaurus;

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
  public IDNFinderLeven(
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
    final Set<String> alleNormiert = CollectionUtils.union(normNames, normVerweisungen);
    thesaurus = new Thesaurus(alleNormiert);

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
   * 5	n:1 Editdistanz = 1
   * 6	n:1 Editdistanz = 2
   * 7	n:1 Editdistanz = 3
   * 8	n:1 Editdistanz = 4
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

    final List<StringDistance> dists = StringMetrics.wordsWithinDistance(thesaurus, 4, name, 4);
    if (!dists.isEmpty()) {
      final double mindist = dists.get(0).getDistance();
      final Set<String> candidates = dists.stream().filter(dist -> dist.getDistance() == mindist)
        .map(StringDistance::getWord).collect(Collectors.toSet());
      idns = CollectionUtils.union(idn2nameNorm.searchKeys(candidates),
        idns2verweisungenNorm.searchKeys(candidates));
      return makeTriplett(idns, (int) (4 + mindist));
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
    return new Triplett<Collection<String>, Collection<String>, Integer>(idns, names, level);
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
    source.getKeySet().forEach(idn ->
    {
      source.getValueSet(idn).forEach(value ->
      {
        dest.add(idn, normiere(value));
        // Alle Sonderzeichen weg, ob das gut geht?
        value = value.replaceAll("\\p{Punct}", "");
        dest.add(idn, normiere(value));
      });
    });
    return dest;
  }

}
