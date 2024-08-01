package de.dnb.gnd.utils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.applicationComponents.tuples.Quadruplett;
import de.dnb.basics.collections.ListMultimap;
import de.dnb.basics.collections.Multimap;
import de.dnb.basics.filtering.Between;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.utils.DDC_SG.HUNDERTER;
import de.dnb.gnd.utils.SubjectUtils.TIEFE;

/**
 * Utilities zu den DDC-Sachgruppen.
 *
 * @author baumann
 *
 */
public final class SGUtils {

  private SGUtils() {

  }

  private static TreeMap<Between<String>, DDC_SG> allIntervalls() {
    fillIntervals();
    return interval2sg;
  }

  /**
  *
  * Gibt zur ddc die Sachgruppe.
  *
  * @param ddc   auch null
  * @return      Sachgruppe
  *
  */
  public static Optional<DDC_SG> getSGNullSafe(final String ddc) {
    final DDC_SG sg = getSG(ddc);
    return Optional.ofNullable(sg);
  }

  /**
   *
   * Gibt zur ddc die DDC-Sachgruppe.
   *
   * @param ddc   auch null
   * @return      Sachgruppe oder null
   *
   */
  public static DDC_SG getSG(final String ddc) {
    if (ddc == null)
      return null;
    fillIntervals();
    final TreeMap<Between<String>, DDC_SG> ints2SG = allIntervalls();
    final Between<String> dummy = new Between<String>(ddc, DDC_SG.END);
    Between<String> current = ints2SG.floorKey(dummy);
    /*
     * eventuell so:
     *              current[  ]   dummy[]
     * korrekt[                                ]
     *
     * Daher Test auf Enthaltensein:
     */
    while (current != null) {
      if (current.test(ddc)) {
        return ints2SG.get(current);
      } else {
        current = ints2SG.lowerKey(current);
      }
    }
    return null;
  }

  /**
   *  Gibt Sachgruppen in sortierter Reihenfolge.
   *
   * @return Sachgruppen ohne"B", also alle möglichen Haupsachgruppen,
   *          veränderbar - als String!
   */
  public static Collection<String> allDHSasString() {
    final Collection<String> dhs = new TreeSet<>(alleDDCSachgruppen());
    dhs.remove("B");
    return dhs;
  }

  /**
  *
  * @return Sachgruppen ohne"B", also alle möglichen Hauptsachgruppen,
  * veränderbar
  */
  public static Collection<DDC_SG> alleDHS() {
    final Set<DDC_SG> sg = DDC_SG.enumSet();
    sg.remove(DDC_SG.SG_B);
    return sg;
  }

  /**
  *
  * @return Sachgruppen (mit "B"), veränderbar
  */
  public static Collection<DDC_SG> allDDC_SG() {
    return DDC_SG.enumSet();
  }

  /**
   * @return  alle (auch "B") als String
   */
  public static Collection<String> alleDDCSachgruppen() {
    final EnumSet<DDC_SG> eset = DDC_SG.enumSet();
    return eset.stream().map(DDC_SG::getDDCString).collect(Collectors.toSet());
  }

  /**
   * Abbildung Intervall auf Sg: (von, bis) -> DDC-SG.
   */
  private static TreeMap<Between<String>, DDC_SG> interval2sg;

  /**
   * Füllt die Datenstruktur interval2sg mit Intervallen.
   */
  private static void fillIntervals() {
    if (interval2sg != null)
      return;
    interval2sg = new TreeMap<>();
    DDC_SG.enumSet().forEach(sg ->
    {
      final Collection<Between<String>> intervals = sg.getIntervals();
      intervals.forEach(in ->
      {
        interval2sg.put(in, sg);
      });
    });
  }

  /**
   * Gibt die beste DDC-Hauptsachgruppe eines Datensatzes + beste Erfassungsart aus $E.
   * Die Vorzugsreihenfolge hängt vom Feld $E ab und lautet:
   * <br>
   * i -> p -> keine -> m -> a
   *
   * @param record    nicht null
   * @return          (SG, $E)
   *                  <br>Sachgruppe eventuell maschinell aus alten
   *                  SGG gewonnen und im ersten Unterfeld $m zu finden.
   *                  Eventuell liegt ein Gesamtdatenabzug vor. Dann wird versucht,
   *                  aus Feld 4000, Unterfeld $S etwas zu extrahieren.
   *                  Wenn nichts gefunden: (null, null).
   */
  public static Pair<String, String> getDhsStringPair(final Record record) {
    Objects.requireNonNull(record);
    Line line = getDHSLine(record);
    if (line == null) {
      line = BibRecUtils.getMainTitleLine(record);
      if (line != null)
        return new Pair<>(SubfieldUtils.getContentOfFirstSubfield(line, 'S'),
          SubfieldUtils.getContentOfFirstSubfield(line, 'E'));
      else
        return Pair.getNullPair();
    }
    return getDhsStringPair(line);
  }

  /**
   * Gibt die {@link DDC_SG}-Hauptsachgruppe eines Datensatzes. Diese muss
   * korrekt sein, ansonsten wird null zurückgegeben.
   * <br><br>
   * Die Vorzugsreihenfolge hängt vom Feld $E ab und lautet:
   * <br>
   * i -> p -> keine -> m -> a
   *
   * @param record    nicht null
   * @return          Eine Sachgruppe, eventuell maschinell aus alten
   *                  SGG gewonnen und im ersten Unterfeld $m zu finden.
   *                  Ein Versuch wird bei Af-Sätzen im Gesamtdatenabzug
   *                  auch mit 4000, $S, gemacht.
   *                  Wenn nichts gefunden: null.
   */
  public static DDC_SG getDDCDHS(final Record record) {
    Objects.requireNonNull(record);
    final String dhs = getDhsStringPair(record).first;
    if (dhs == null)
      return null;
    return DDC_SG.getSG(dhs);
  }

  /**
   * Gibt die {@link SG}-Hauptsachgruppe inklusive der Musiksachgruppen eines Datensatzes.
   * <br><br>
   * Die Vorzugsreihenfolge hängt vom Feld $E ab und lautet:
   * <br>
   * i -> p -> keine -> m -> a
   *
   * @param record    nicht null
   * @return          Eine Sachgruppe, eventuell maschinell aus alten
   *                  SGG gewonnen und im ersten Unterfeld $m zu finden.
   *                  Ein Versuch wird bei Af-Sätzen im Gesamtdatenabzug
   *                  auch mit 4000, $S, gemacht.
   *                  Wenn nichts gefunden: null.
   */
  public static SG getAllDDCDHS(final Record record) {
    Objects.requireNonNull(record);
    final String dhs = getDhsStringPair(record).first;
    if (dhs == null)
      return null;
    final SG sg = MUSIC_SG.getSG(dhs);
    if (sg != null)
      return sg;
    else
      return DDC_SG.getSG(dhs);
  }

  /**
   * Gibt die Sachgruppen eines Datensatzes durch Semikolon getrennt.
   *
   * @param record    nicht null
   * @return          Die Liste der Sachgruppen als String.
   *                  Wenn nichts gefunden: null.
   */
  public static String getSGGSemicola(final Record record) {
    Objects.requireNonNull(record);
    final Line line = SGUtils.getDHSLine(record);
    if (line == null)
      return null;
    return getSGGSemicola(line);
  }

  /**
   * Gibt die Sachgruppen eines Datensatzes durch Semikolon getrennt.
   *
   * @param line    nicht null
   * @return          Die Liste der Sachgruppen als String.
   *                  Wenn nichts gefunden: null.
   */
  public static String getSGGSemicola(final Line line) {
    final String dhs = getDhsStringPair(line).first;
    if (dhs == null)
      return null;
    final List<String> dns = getDNSstrings(line);
    if (dns.isEmpty())
      return dhs;
    else
      return dhs + ";" + StringUtils.concatenate(";", dns);
  }

  /**
   * Gibt die DDC-Nebensachgruppen eines Datensatzes.
   *
   * @param record    nicht null
   * @return          Liste von Sachgruppen, eventuell maschinell aus alten
   *                  SGG gewonnen und in den Unterfeldern $f zu finden
   *                  ($d, wenn alt). Wenn ein Gesamtdatenabzug vorliegt, hilft auch die
   *                  4000-er-Zeile. Die enthält die NSG in $B. Wenn
   *                  keine Nebensachgruppen: leere Liste.
   */
  public static List<String> getDNSstrings(final Record record) {
    Objects.requireNonNull(record);
    Line line = SGUtils.getDHSLine(record);
    if (line == null) {
      // neuer Versuch, da auch in 4000 im Gesamtdatenabzug enthalten:
      line = BibRecUtils.getMainTitleLine(record);
      return SubfieldUtils.getSubfields(line, 'B').stream().map(Subfield::getContent)
        .collect(Collectors.toList());
    }
    return getDNSstrings(line);
  }

  /**
   * Gibt die volle DDC-Hauptsachgruppe eines Datensatzes. Für Belletristik
   * in der Form 830;B.
   *
   * @param record    nicht null
   * @param table TODO
   * @return          Eine Sachgruppe, eventuell maschinell aus alten
   *                  SGG gewonnen und im ersten Unterfeld $m zu finden.
   *                  Wenn nichts gefunden: null.
   */
  public static String getFullDHSString(
    final Record record,
    final Map<Integer, Quadruplett<DDC_SG, DDC_SG, TIEFE, String>> table) {
    Objects.requireNonNull(record);
    final Pair<DDC_SG, DDC_SG> pair = getDHSundDNS(record, table);
    return getFullDHSString(pair);
  }

  /**
   * Gibt die volle DDC-Hauptsachgruppe, entweder aus 5050-Zeile, der 4000 (Bei Gesamtabzug) oder
   * aus dem übergeordneten Titel und der Map. Eine einordnungsrelevante
   * Nebensachgruppe (B, K, S) wird als zweite Komponente zurückgegeben.
   *
   * @param record      nicht null
   * @param idn2Status auch null, idn -> (DHS, DNS, Tiefe, $E)
   * @return          nicht null, sondern (null, null). Sonst ein Paar wie (530, S)
   *                  oder (530, null) oder (830, B).
   */
  public static Pair<DDC_SG, DDC_SG> getDHSundDNS(
    final Record record,
    final Map<Integer, Quadruplett<DDC_SG, DDC_SG, TIEFE, String>> idn2Status) {
    Objects.requireNonNull(record);

    final String dhs = getDhsStringPair(record).first;
    if (dhs != null) {
      // Dann klappt es wohl, also weiter:
      final DDC_SG sg = DDC_SG.getSG(dhs);

      DDC_SG neben = null;
      final List<String> nebenSGG = getDNSstrings(record);
      if (nebenSGG.contains("B"))
        neben = DDC_SG.SG_B;
      else if (nebenSGG.contains("K"))
        neben = DDC_SG.SG_K;
      else if (nebenSGG.contains("S"))
        neben = DDC_SG.SG_S;
      return new Pair<>(sg, neben);
    }

    //Also war es nix; hilft die Map weiter?
    if (idn2Status == null)
      return Pair.getNullPair();

    final int idnBroader = IDNUtils.idn2int(BibRecUtils.getBroaderTitleIDN(record));
    final Quadruplett<DDC_SG, DDC_SG, TIEFE, String> quadruplett = idn2Status.get(idnBroader);
    if (quadruplett != null)
      return new Pair<DDC_SG, DDC_SG>(quadruplett.first, quadruplett.second);
    else
      return Pair.getNullPair();
  }

  /**
   * Gibt die volle DDC-Hauptsachgruppe, entweder aus 5050-Zeile
   * Nebensachgruppe (B, K, S) wird als zweite Komponente zurückgegeben.
   *
   * @param dhsLine   nicht null
   * @return          nicht null, sondern (null, null). Sonst ein Paar wie (530, S)
   *                  oder (530, null) oder (830, B).
   */
  public static Pair<DDC_SG, DDC_SG> getDHSundDNS(final Line dhsLine) {
    Objects.requireNonNull(dhsLine);
    final String dhs = getDhsStringPair(dhsLine).first;
    if (dhs != null) {
      final DDC_SG sg = DDC_SG.getSG(dhs);
      DDC_SG neben = null;
      final List<String> nebenSGG = getDNSstrings(dhsLine);
      if (nebenSGG.contains("B"))
        neben = DDC_SG.SG_B;
      else if (nebenSGG.contains("K"))
        neben = DDC_SG.SG_K;
      else if (nebenSGG.contains("S"))
        neben = DDC_SG.SG_S;
      return new Pair<>(sg, neben);
    }
    return Pair.getNullPair();
  }

  /**
   * @param pair        In der Form (dhs, relevante dns), also (530, null) oder
   *                    (830, B). Sicherheitshalber auch null. Eigentlich sollten aber Null-Paare als
   *                    (null, null) gegeben sein
   * @param bksSeparat  sollen B,K,S als eigene Hunderter ausgegeben werden? Dann
   *                    wird aus (830, B) -> B und aus (510, S) -> S ...
   * @return            Hunderter oder null
   */
  public static
    HUNDERTER
    getHunderterDHS(final Pair<DDC_SG, DDC_SG> pair, final boolean bksSeparat) {
    if (pair == null)
      return null;
    final DDC_SG dhs = pair.first;
    if (dhs == null)
      return null;

    final DDC_SG dns = pair.second;
    if (bksSeparat) {
      if (dns == DDC_SG.SG_B)
        return HUNDERTER.HU_B;
      if (dns == DDC_SG.SG_S)
        return HUNDERTER.HU_S;
      if (dns == DDC_SG.SG_K)
        return HUNDERTER.HU_K;
    }

    return dhs.getHunderter();

  }

  /**
   * @param pair        In der Form (dhs, relevante dns), also (530, null) oder
   *                    (830, B). Sicherheitshalber auch null. Eigentlich sollten aber Null-Paare als
   *                    (null, null) gegeben sein
   * @param bksSeparat  sollen B,K,S als eigene Hunderter ausgegeben werden? Dann
   *                    wird aus (830, B) -> B und aus (510, S) -> S ...
   * @return            Hunderter oder null
   */
  public static String getFullDHSString(final Pair<DDC_SG, DDC_SG> pair) {
    if (pair == null)
      return null;
    final DDC_SG dhs = pair.first;
    if (dhs == null)
      return null;
    final String s = dhs.getDDCString();
    final DDC_SG dns = pair.second;
    if (dns == null)
      return s;
    return s + ";" + dns.getDescription();
  }

  /**
   * @param record      nicht null
   * @param hunderterMap  Map<Integer, Pair<DDC_SG, DDC_SG>> für übergeordnete Titel, auch null möglich
   * @return            Hunderter oder null. B,K,S werden als eigene Hunderter ausgegeben.
   *                    Es wird also 830;B -> B und 510;S -> S ...
   */
  public static HUNDERTER getHunderterDHS(
    final Record record,
    final Map<Integer, Quadruplett<DDC_SG, DDC_SG, TIEFE, String>> hunderterMap) {
    Objects.requireNonNull(record);
    final Pair<DDC_SG, DDC_SG> pair = getDHSundDNS(record, hunderterMap);
    return getHunderterDHS(pair, true);
  }

  /**
   * Gibt die DDC-Hauptsachgruppe und Erfassungsart einer 5050-Zeile.
   *
   * @param line      nicht null
   * @return          (SG, Erfassungsart).
   *                  <br>Sachgruppe, eventuell maschinell aus alten
   *                  SGG gewonnen und im ersten Unterfeld $m zu finden.
   *                  <br>Erfassungsart aus $E.
   *                  <br>Wenn nichts gefunden: (null, null)
   *                  <br>Wenn $E nicht existiert: (SG, null)
   */
  public static Pair<String, String> getDhsStringPair(final Line line) {
    Objects.requireNonNull(line);
    if (!line.getTag().pica3.equals("5050"))
      throw new IllegalArgumentException("keine 5050-Zeile");
    final String sg = SubfieldUtils.getContentOfFirstSubfield(line, 'e');
    final String dollarE = SubfieldUtils.getContentOfFirstSubfield(line, 'E');
    if (sg != null)
      return new Pair<>(sg, dollarE);
    return new Pair<>(SubfieldUtils.getContentOfFirstSubfield(line, 'm'), dollarE);
  }

  /**
   * Gibt die DDC-Nebensachgruppen eines Datensatzes.
   *
   * @param line      nicht null
   * @return          Liste von Sachgruppen, eventuell maschinell aus alten
   *                  SGG gewonnen und in den Unterfeldern $f zu finden
   *                  ($d, wenn alt). Wenn nichts gefunden: leere Liste.
   */
  public static List<String> getDNSstrings(final Line line) {
    Objects.requireNonNull(line);
    if (!line.getTag().pica3.equals("5050"))
      throw new IllegalArgumentException("keine 5050-Zeile");
    final List<String> sg = SubfieldUtils.getContentsOfSubfields(line, 'f');
    if (!sg.isEmpty())
      return sg;
    else
      return SubfieldUtils.getContentsOfSubfields(line, 'd');
  }

  /**
   * Gibt die Zeile mit der DDC-Hauptsachgruppe eines Datensatzes. Hierbei
   * wird keine Gewichtung vorgenommen, sondern die erste 5050-Zeile
   * geliefert.
   *
   * @param record    nicht null
   * @return          null, wenn nichts gefunden
   */
  public static Line getDHSLinePortalStyle(final Record record) {
    Objects.requireNonNull(record);
    final Pair<Line, Integer> pair = RecordUtils.getFirstLineTagGivenAsString(record, "5050");
    if (pair.second == 0)
      return null;
    else
      return pair.first;
  }

  /**
   * Gibt die Zeile mit der DDC-Hauptsachgruppe eines Datensatzes. Hierbei
   * wird eine Gewichtung vorgenommen, die unabhängig von der Reihenfolge
   * im Datensatz ist.
   * <br><br>
   * Die Vorzugsreihenfolge hängt vom Feld $E ab und lautet:
   * <br>
   * i -> p -> keine -> m -> a
   *
   * @param record    nicht null
   * @return          null, wenn nichts gefunden. Vorsicht, in der
   *                  zurückgegebenen Zeile kann das Unterfeld $e
   *                  fehlen!
   */
  public static Line getDHSLine(final Record record) {
    Objects.requireNonNull(record);
    final Collection<Line> lines5050 = SubjectUtils.getSubjectGroupLines(record);
    if (lines5050.isEmpty())
      return null;
    final Multimap<String, Line> type2SG = new ListMultimap<>();
    for (final Line line : lines5050) {
      // Erfassungsart
      final String type = SubfieldUtils.getContentOfFirstSubfield(line, 'E');
      type2SG.add(type, line);
    }
    Collection<Line> hsgLines;

    // "intellektuell
    hsgLines = type2SG.get("i");
    if (hsgLines != null) {
      return hsgLines.iterator().next();
    }

    // Parallelausgabe
    hsgLines = type2SG.get("p");
    if (hsgLines != null) {
      return hsgLines.iterator().next();
    }

    // von Hand eingegeben, ohne Typ in $E
    hsgLines = type2SG.get(null);
    if (hsgLines != null) {
      return hsgLines.iterator().next();
    }

    // maschinell
    hsgLines = type2SG.get("m");
    if (hsgLines != null) {
      return hsgLines.iterator().next();
    }

    // Ablieferung
    hsgLines = type2SG.get("a");
    if (hsgLines != null) {
      return hsgLines.iterator().next();
    }

    // unbekannter Typ in $E
    final Collection<Line> flat = type2SG.flatten();
    // alte HSG:
    if (flat.isEmpty())
      return null;

    return flat.iterator().next();
  }

  /**
   * gibt das Referat zu einer Sachgruppe, sofern sie existiert.
   *
   * @param vermuteteSG   auch null
   * @return              Referat oder null
   */
  public static DDC_SG.REFERATE getReferat(final String vermuteteSG) {
    final DDC_SG sg = DDC_SG.getSG(vermuteteSG);
    if (sg == null)
      return null;
    return sg.getReferat();
  }

  public static void main(final String[] args) {
    final Record record = RecordUtils.readFromClip();
    System.out.println(getDhsStringPair(record));
    System.out.println(getAllDDCDHS(record));
  }

}
