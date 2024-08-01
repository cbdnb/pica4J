package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.SubfieldUtils;

public class ParseUtils {

  private ParseUtils() {
  }

  /**
   * Hat eines der Unterfelder als Indikator den "-ohne-"-Indikator,
   * der weder Prä- noch Postfix hat?
   * @param subfields		nicht null
   * @return				true, wenn enthalten
   */
  public static boolean containsOhne(final Collection<Subfield> subfields) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    for (final Subfield subfield : subfields) {
      if (ParseUtils.isOhne(subfield.getIndicator()))
        return true;
    }
    return false;
  }

  /**
   * Ist einer der Indikatoren der "-ohne-"-Indikator,
   * der weder Prä- noch Postfix hat?
   * @param indicators	nicht null
   * @return				true, wenn enthalten
   */
  public static boolean containsOhneIndicator(final Collection<Indicator> indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    for (final Indicator indicator : indicators) {
      if (ParseUtils.isOhne(indicator))
        return true;
    }
    return false;
  }

  /**
   * Splittet einen Pica+-String in die durch splittingIndicators
   * vorgegebenen Unterfelder.
   * <br><br> Für Titeldaten müssen die 1. und 2. Map
   * untersucht werden, da in den Expansionen (anders als bei GND) alle
   * denkbaren Unterfelder stehen können. In dem Fall, wo kein 'ƒ', sondern
   * ein '$' als Unterfeldtrenner vorliegt (Korrekturmodus), kann es
   * dann zu unerwünschten Kollisionen kommen.
   * @param contentStr			nicht null
   * @return						null im Nichterfolgsfall; das kann auch
   *                              dann der Fall sein, wenn z.B. ein $9-Feld
   *                              nicht belegt ist (dieses wird als
   *                              obligatorisch angesehen)
   */
  public static
    List<Subfield>
    picaPlusSplitInSubfields(final String contentStr, final Map<Character, Indicator> indMap) {
    RangeCheckUtils.assertReferenceParamNotNull("contentStr", contentStr);
    RangeCheckUtils.assertReferenceParamNotNull("indMap", indMap);
    // Veränderbar
    final LinkedHashMap<Character, Indicator> splittingIndicators = new LinkedHashMap<>(indMap);
    final int length = contentStr.length();
    int allowedDollar = 0;
    Indicator ind = splittingIndicators.get(contentStr.charAt(1));
    if (ind == null)
      return null;
    ParseUtils.remove(splittingIndicators, ind);
    final char subfieldSeparator = contentStr.charAt(0);
    int nextDollarPos = contentStr.indexOf(subfieldSeparator, allowedDollar + 2);

    final List<Subfield> subfieldList = new LinkedList<>();
    while (nextDollarPos != -1 && nextDollarPos < length - 1) {
      final char nextIndChar = contentStr.charAt(nextDollarPos + 1);
      final Indicator nextInd = splittingIndicators.get(nextIndChar);
      if (nextInd != null) {
        ParseUtils.remove(splittingIndicators, nextInd);
        final String subContent = contentStr.substring(allowedDollar + 2, nextDollarPos);
        try {
          subfieldList.add(new Subfield(ind, subContent));
        } catch (final IllFormattedLineException e) {
          return null;
        }
        ind = nextInd;
        allowedDollar = nextDollarPos;
      }
      nextDollarPos = contentStr.indexOf(subfieldSeparator, nextDollarPos + 2);
    }
    final String subContent = contentStr.substring(allowedDollar + 2);
    try {
      subfieldList.add(new Subfield(ind, subContent));
    } catch (final IllFormattedLineException e) {
      return null;
    }
    // $9 erzwingen:
    if (indMap.containsKey('9') && !SubfieldUtils.containsIndicatorInSubfields('9', subfieldList))
      return null;
    return subfieldList;
  }

  /**
   * Splittet einen Pica+-String in alle tag bekannten Unterfelder.
   * <br><br>
   * Indikatoren, die unbekannt sind, werden nicht berücksichtigt. Bei
   * nicht wiederholbaren Indikatoren wird nur der erste berücksichtigt.
   *
   * @param contentStr	nicht null
   * @param tag			nicht null
   * @return				eventuell leer
   */
  public static
    List<Subfield>
    simplePicaPlusSplitInSubfields(final String contentStr, final Tag tag) {
    RangeCheckUtils.assertReferenceParamNotNull("contentStr", contentStr);
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    if (contentStr.length() <= 2)
      throw new IllegalArgumentException("contentStr zu kurz");
    final Map<Character, Indicator> splittingIndicators = tag.getIndicatorMap(true);
    final char subSep = contentStr.charAt(0);
    final List<Subfield> subfieldList = new LinkedList<>();
    final String[] strings = contentStr.split(Pattern.quote("" + subSep));
    for (int i = 1; i < strings.length; i++) {
      final String fraction = strings[i];
      if (fraction.isEmpty())
        continue;
      final char indChar = fraction.charAt(0);
      Indicator ind = splittingIndicators.get(indChar);
      if (ind == null) {
        // Versuch mit t->a:
        if (indChar == 't')
          ind = splittingIndicators.get('a');
        if (ind == null)
          continue;
      }
      final String subContent = fraction.substring(1);

      try {
        subfieldList.add(new Subfield(ind, subContent));
        remove(splittingIndicators, ind);
      } catch (final IllFormattedLineException e) {
        // nix
      }
    }
    return subfieldList;
  }

  /**
   * entfernt einen Indikator aus der Map, wenn nicht wiederholbar.
   * @param splittingIndicators
   * @param ind
   */
  static void remove(final Map<Character, Indicator> splittingIndicators, final Indicator ind) {
    if (ind.repeatability == Repeatability.NON_REPEATABLE)
      splittingIndicators.remove(ind.indicatorChar);
  }

  /**
   * Liefert den "-ohne-"-Indikator, der weder Prä- noch Postfix hat.
   * @param indicators	nicht null
   * @return				Gefundenen Indikator oder null
   */
  public static Indicator getOhne(final Collection<Indicator> indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    for (final Indicator indicator : indicators) {
      if (ParseUtils.isOhne(indicator))
        return indicator;
    }
    return null;
  }

  /**
   * Hat der Indikator weder Prä- noch Postfix?
   * @param 	indicator	nicht null
   * @return	true, wenn wahr
   */
  public static boolean isOhne(final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    return "".equals(indicator.prefix) && "".equals(indicator.postfix);
  }

}
