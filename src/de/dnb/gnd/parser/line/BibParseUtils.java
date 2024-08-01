package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.BibliographicTag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;

public class BibParseUtils {

  public static final TagDB TAG_DB = BibTagDB.getDB();

  /**
   * Collection enthält $9 oder
   * mehr als ein leeres Präfix, kann daher
   * nur in fester Reihenfolge
   * der Unterfelder geparst werden.
   *
   * @param indicators	nicht null
   * @return				true, wenn 2 und mehr.
   */
  public static boolean mustBeParsedInFixOrder(final Collection<Indicator> indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    if (SubfieldUtils.containsIndicator('9', indicators))
      return true;

    int count = 0;
    for (final Indicator indicator : indicators) {
      if (StringUtils.equals("", indicator.prefix))
        count++;
    }
    return count >= 2;
  }

  /**
   * Indikator hat alternatives
   * Präfix zum Anschluss weiterer Unterfelder.
   *
   * @param indicator
   * @return
   */
  public static boolean hasAlternativePrefix(final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    return indicator.prefixAlt != null;
  }

  /**
   * Zerlegt unter gewissen Voraussetzungen den Inhalt
   * einer Zeile in Unterfelder. Dazu darf
   * höchstens ein leeres Präfix vorhanden sein. Beim Parsen
   * wird erlaubt, dass die Unterfelder in beliebiger Reihenfolge
   * vorkommen.
   *
   * @param indicators	nicht null, nicht leer
   * @param contentOfLine	nicht null
   * @return				veränderbare Liste.
   * 						null, wenn nicht gematcht werden kann oder kein
   * 						$9 erkanntwerden kann, obwohl eines da sein sollte.
   * 						Leere Liste	wenn contentOfLine leer.
   */
  public static
    List<Subfield>
    makeSubfieldsVariableOrder(final Set<Indicator> indicators, final String contentOfLine) {

    /*
     * Rekursives Vorgehen:
     * - Wenn contentOfLine mit einem der Präfixe beginnt
     * 		- wenn dazu ein Postfix gehört, suche Postfix
     * 		- sonst suche nächstes passendes Präfix
     * - sonst: Leeres Präfix
     * 		- wenn dazu ein Postfix gehört, suche Postfix
     * 		- sonst suche nächstes Präfix
     * Rufe makeSubfieldsVariableOrder() mit dem Rest von contentOfLine
     * und einer eventuell (nichtwiederholbares Unterfeld) reduzierten
     * Indikatormenge auf.
     *
     * Die Methode bricht ab, wenn
     * - kein Postfix vorhanden ist und kein Präfix mehr gefunden
     * 		wird: Rückgabe: Inhalt von contentOfLine in Unterfeld
     * 		verpackt.
     * - contentOfLine leer ist: Rückgabe: leere Liste
     * - nicht mehr gematcht werden kann: Rückgabe: null
     */
    // Checken:
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    if (mustBeParsedInFixOrder(indicators)) {
      throw new IllegalArgumentException("Mehr als ein leeres Präfix");
    }
    RangeCheckUtils.assertReferenceParamNotNull("contentOfLine", contentOfLine);
    if (contentOfLine.isEmpty())
      return Collections.emptyList();

    // Sonderfälle zu Beginn:
    // Alle Indikatoren verbraucht:
    if (indicators.isEmpty())
      return null;
    // Kein Indikator passt auf den Anfang:
    final Pair<Indicator, String> firstIndicatorP =
      findBeginningIndicator(contentOfLine, indicators);

    if (firstIndicatorP == null) {
      return null;
    }
    // Also gibt es einen zuständigen Indikator
    // Variablen vorbereiten:
    final Indicator firstIndicator = firstIndicatorP.first;
    final String prefix = firstIndicatorP.second;
    final String trimmedContent = contentOfLine.substring(prefix.length());
    final String postfix = firstIndicator.postfix;
    String beginning; // Inhalt des aktuellen Indikators
    final Set<Indicator> restInds = new LinkedHashSet<>(indicators);
    if (firstIndicator.repeatability == Repeatability.NON_REPEATABLE)
      restInds.remove(firstIndicator);
    // werden zurückgegeben:
    final List<Subfield> subfields = new LinkedList<>();
    // entstehen aus dem Rest:
    List<Subfield> restSubs;

    // einfachster Fall: Postfix
    if (!postfix.isEmpty()) {
      final int postfixPos = trimmedContent.indexOf(postfix);
      if (postfixPos == -1)
        return null;
      beginning = trimmedContent.substring(0, postfixPos);
      final String rest = trimmedContent.substring(postfixPos + postfix.length());
      restSubs = makeSubfieldsVariableOrder(restInds, rest);
      if (restSubs == null)
        return null;
      else {
        try {
          subfields.add(new Subfield(firstIndicator, beginning));
          subfields.addAll(restSubs);
          return forcedollar9(indicators, subfields);
        } catch (final IllFormattedLineException e) {
          return null;
        }
      }
    } else {
      // kein Postfix, also neues Präfix suchen:
      int actualIndex = 0;
      while (true) {
        actualIndex = findNextPrefix(trimmedContent, restInds, actualIndex + 1);
        if (actualIndex == -1)
          break;
        final String rest = trimmedContent.substring(actualIndex);
        restSubs = makeSubfieldsVariableOrder(restInds, rest);
        if (restSubs != null) {
          try {
            subfields.clear();
            beginning = trimmedContent.substring(0, actualIndex);
            subfields.add(new Subfield(firstIndicator, beginning));
            subfields.addAll(restSubs);
            return forcedollar9(indicators, subfields);
          } catch (final IllFormattedLineException e) {
            // nichts machen
          }
        }
      } // while (true)
      /*
       *  break, also nichts gefunden. Da kein Postfix vorhanden
       *  muss also alles in trimmedContent zum Unterfeld gehören.
       */
      try {
        subfields.clear();
        subfields.add(new Subfield(firstIndicator, trimmedContent));
        return forcedollar9(indicators, subfields);
      } catch (final IllFormattedLineException e) {
        return null;
      }
    } //if (!postfix.isEmpty())
  }

  /**
   * Zerlegt den Inhalt einer Zeile in Unterfelder. Die Reihenfolge
   * der Indikatoren wird als fest angesehen. Die Zeile wird mittels
   * regulärer Ausdrücke geparst.
   *
   * @param indicators	nicht null, nicht leer
   * @param contentOfLine	nicht null
   * @return				nicht null, eventuell leer, veränderbar.
   */
  public static
    List<Subfield>
    makeSubfieldsFixOrder(final Set<Indicator> indicators, final String contentOfLine) {
    /*
     * Rekursives Vorgehen:
     * - Wenn contentOfLine mit einem der Präfixe beginnt
     * 		- wenn dazu ein Postfix gehört, suche Postfix
     * 		- sonst suche nächstes Präfix
     * - sonst: Leeres Präfix
     * 		- wenn dazu ein Postfix gehört, suche Postfix
     * 		- sonst suche nächstes Präfix
     * Rufe makeSubfields2 mit dem Rest von contentOfLine und einer
     * eventuell (nichtwiederholbares Unterfeld) reduzierten
     * Idikatormenge auf.
     *
     * Die Methode bricht ab, wenn
     * - kein Postfix vorhanden ist und kein Präfix mehr gefunden
     * 		wird: Rückgabe: Inhalt von contentOfLine in Unterfeld
     * 		verpackt.
     * - contentOfLine leer ist: Rückgabe: leere Liste
     * - nicht mehr gematcht werden kann: Rückgabe: null
     */

    // Checken:
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    RangeCheckUtils.assertReferenceParamNotNull("contentOfLine", contentOfLine);
    if (contentOfLine.isEmpty())
      return Collections.emptyList();

    // Sonderfälle zu Beginn:
    // Alle Indikatoren verbraucht:
    if (indicators.isEmpty())
      return null;
    // Kein Indikator passt auf den Anfang:
    final Pair<Indicator, String> firstIndicatorP =
      findBeginningIndicator(contentOfLine, indicators);
    if (firstIndicatorP == null)
      return null;

    // Also gibt es einen zuständigen Indikator
    // Variablen vorbereiten:
    final Indicator firstIndicator = firstIndicatorP.first;
    final String prefix = firstIndicatorP.second;
    final String trimmedContent = contentOfLine.substring(prefix.length());
    final String postfix = firstIndicator.postfix;
    String beginning; // Inhalt des aktuellen Indikators
    final LinkedHashSet<Indicator> restInds = new LinkedHashSet<>(indicators);

    // Alle Indikatoren bis zum gefundenen entfernen:
    for (final Indicator indicator : indicators) {
      if (indicator != firstIndicator) {
        restInds.remove(indicator);
      } else {
        if (firstIndicator.repeatability == Repeatability.NON_REPEATABLE)
          restInds.remove(firstIndicator);
        break;
      }
    }

    // werden zurückgegeben:
    final List<Subfield> subfields = new LinkedList<>();
    // entstehen aus dem Rest:
    List<Subfield> restSubs;

    // einfachster Fall: Postfix
    if (!postfix.isEmpty()) {
      final int postfixPos = trimmedContent.indexOf(postfix);
      if (postfixPos == -1)
        return null;
      beginning = trimmedContent.substring(0, postfixPos);
      final String rest = trimmedContent.substring(postfixPos + postfix.length());
      restSubs = makeSubfieldsFixOrder(restInds, rest);
      if (restSubs == null)
        return null;
      else {
        try {
          subfields.add(new Subfield(firstIndicator, beginning));
          subfields.addAll(restSubs);
          return forcedollar9(indicators, subfields);
        } catch (final IllFormattedLineException e) {
          return null;
        }
      }
    } else {
      // kein Postfix, also neues Präfix suchen:
      int actualIndex = 0;
      while (true) {
        actualIndex = findNextPrefix(trimmedContent, restInds, actualIndex + 1);
        if (actualIndex == -1)
          break;
        final String rest = trimmedContent.substring(actualIndex);
        restSubs = makeSubfieldsFixOrder(restInds, rest);
        if (restSubs != null) {
          try {
            subfields.clear();
            beginning = trimmedContent.substring(0, actualIndex);
            subfields.add(new Subfield(firstIndicator, beginning));
            subfields.addAll(restSubs);
            return forcedollar9(indicators, subfields);
          } catch (final IllFormattedLineException e) {
            // nichts machen
          }
        }
      } // while (true)
      /*
       *  break, also nichts gefunden. Da kein Postfix vorhanden
       *  muss also alles in trimmedContent zum Unterfeld gehören.
       */
      try {
        subfields.clear();
        subfields.add(new Subfield(firstIndicator, trimmedContent));
        return forcedollar9(indicators, subfields);
      } catch (final IllFormattedLineException e) {
        return null;
      }
    } //if (!postfix.isEmpty())
  }

  /**
   * Erzwingt das Auftreten eines $9-Unterfeldes, wenn in
   * der Liste indicators ein $9 vorkommt.
   *
   * @param indicators
   * @param subfields
   * @return
   */
  public static
    List<Subfield>
    forcedollar9(final Set<Indicator> indicators, final List<Subfield> subfields) {
    if (SubfieldUtils.containsIndicator('9', indicators)
      && !SubfieldUtils.containsIndicatorInSubfields('9', subfields)) {
      return null;
    } else {
      return subfields;
    }
  }

  /**
   * Findet den Indikator, dessen Präfix auf den Beginn von rest
   * passt. Wird keiner gefunden, wird der Indikator geliefert,
   * der ein leeres Präfix hat, sofern vorhanden. Bevorzugt wird
   * der Indikator, der aber noch ein Postfix hat, sofern dieses
   * existiert. Sollte ein weiteres Präfix auf den Anfang passen
   * (manche Präfixe sind Präfixe voneinander (z.B. 4700 $c,$f), so
   * wird das längere genommen.
   *
   * @param rest			nicht null
   * @param indicators	nicht null
   * @return				gefundenen Indikator + Präfix oder null
   */
  public static
    Pair<Indicator, String>
    findBeginningIndicator(final String rest, final Collection<Indicator> indicators) {

    RangeCheckUtils.assertReferenceParamNotNull("rest", rest);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);

    Indicator onlyPostfixIndicator = null; // Indikator nur mit Postfix
    Indicator ohneIndicator = null; // Indikator ohne Präfix, ohne Postfix
    // Es könnte noch ein Präfix geben, das länger ist (z.B. 4700 $c,$f)
    Indicator candidate = null;
    for (final Indicator indicator : indicators) {

      final String prefix = indicator.prefix;
      final String postfix = indicator.postfix;
      /*
       * Sonderbehandlung für leere Präfixe.
       */
      if (prefix.isEmpty()) {
        if (postfix.isEmpty() && ohneIndicator == null) {
          ohneIndicator = indicator;
        } else if (!postfix.isEmpty()) {
          final int index = rest.indexOf(postfix, 1);
          if (index != -1)
            onlyPostfixIndicator = indicator;
        }
      } else if (rest.startsWith(prefix)) {
        /*
         * Wenn es leere Indikatoren gibt, stellt sich die Frage:
         * Gibt es eventuell das Präfix weiter hinten?
         * Es wird aber nur gesucht, wenn auch ein Postfix existiert:
         */
        if ((ohneIndicator != null || onlyPostfixIndicator != null) && !postfix.isEmpty()) {
          // Die beiden könnten auch passen, daher:
          final int indexPr = rest.indexOf(prefix, 1);
          // kein weiteres Präfix:
          if (indexPr == -1) {
            return new Pair<>(indicator, prefix);
          }
          // zwar Präfix, aber kein Postfix
          if (indexPr != -1 && rest.indexOf(postfix, indexPr + 1) == -1) {
            return new Pair<>(indicator, prefix);
          }
        } else {
          if (candidate == null) {
            candidate = indicator;
          } else if (indicator.prefix.length() > candidate.prefix.length()) {
            candidate = indicator;
          }
        }

      }
      final String prefixAlt = indicator.prefixAlt;
      if (prefixAlt != null && rest.startsWith(prefixAlt))
        return new Pair<>(indicator, prefixAlt);
    }
    if (candidate != null)
      return new Pair<>(candidate, candidate.prefix);
    if (onlyPostfixIndicator != null)
      return new Pair<>(onlyPostfixIndicator, "");
    else if (ohneIndicator != null)
      return new Pair<>(ohneIndicator, "");
    else
      return null;

  }

  /**
   * Findet das nächste Präfix aus der Indikatormenge auf dem String rest
   * ab der Position fromIndex. Wird bei erneuter Suche als Startindex
   * der gerade gefundene Index verwendet, liefert die neue Suche das
   * selbe Ergebnis.
   * @param rest			nicht null
   * @param indicators	nicht null
   * @param fromIndex		beliebig.
   * @return				gefundenen Index oder -1
   */
  public static
    int
    findNextPrefix(final String rest, final Collection<Indicator> indicators, final int fromIndex) {
    RangeCheckUtils.assertReferenceParamNotNull("rest", rest);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    int bestPosition = -1;

    for (final Indicator indicator : indicators) {
      String prefix = indicator.prefix;
      int foundPos;
      if (!prefix.isEmpty()) {
        foundPos = rest.indexOf(prefix, fromIndex);
        if (foundPos != -1)
          if (bestPosition == -1 || foundPos < bestPosition) {
            bestPosition = foundPos;
          }
      }
      prefix = indicator.prefixAlt;
      if (prefix != null) {
        foundPos = rest.indexOf(prefix, fromIndex);
        if (foundPos != -1)
          if (bestPosition == -1 || foundPos < bestPosition) {
            bestPosition = foundPos;
          }
      }
    }
    return bestPosition;
  }

  /**
   * Gewicht der Collection.
   * @param subfields	nicht null, auch leer
   * @return			Summe der Längen der Präfixe (null-Präfixe werden
   * 					überlesen)
   */
  public static int getWeight(final Collection<Subfield> subfields) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    int i = 0;
    for (final Subfield subfield : subfields) {
      final Indicator indicator = subfield.getIndicator();
      if (indicator.prefix != null)
        i += indicator.prefix.length();
    }
    return i;
  }

  public static void main2(final String[] args) throws IllFormattedLineException {
    final BibliographicTag tag = (BibliographicTag) TAG_DB.findTag("5530");
    final Set<Indicator> set = tag.get1stIndicators();

    System.out.println(makeSubfieldsVariableOrder(set, "Wärmepumpe|31.5|31.6"));
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final Line line = LineParser.parse("5530 Wärmepumpe|31.5|31.6", TAG_DB, false);
    System.out.println(line);
  }

  /**
   * @param args
   */
  public static void main1(final String[] args) {
    final Record record = BibRecUtils.readFromConsole();
    System.out.println(record);
    System.out.println("\n---\n");
    System.out.println(RecordUtils.toPica(record));
    System.out.println("\n---\n");
    System.out.println(RecordUtils.toPica(record, Format.PICA_PLUS, true, "\n", 'ƒ'));
  }
}
