/**
 *
 */
package de.dnb.basics.marc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * @author baumann
 *
 */
public class MarcTag {

  /**
   * Nummer.
   */
  public final String marc;
  public final String german;
  public final Repeatability repeatability;
  public final String english;
  public final String marcIndicator1;
  public final String marcIndicator2;

  List<MarcTag> inherited = new LinkedList<>();

  /**
   * Grundlegende Datenstruktur.
   */
  protected Map<Character, MarcSubfieldIndicator> indicatorMap = new LinkedHashMap<>();

  //------- Hilfsstruktur zur Speicherung von Zwischenergebnissen:
  protected LinkedHashSet<MarcSubfieldIndicator> allIndicators;

  /**
   *
   * @param code beliebig
   * @return Indikator oder null
   */
  public final MarcSubfieldIndicator getSubfieldIndicator(final char code) {
    MarcSubfieldIndicator found = indicatorMap.get(code);
    if (found != null)
      return found;
    for (final MarcTag tag : inherited) {
      found = tag.getSubfieldIndicator(code);
      if (found != null)
        return found;
    }
    return found;
  }

  /**
   * Liefert alle Unterfelder.
   *
   * @return Set != null, eventuell leer.
   */
  public final Set<MarcSubfieldIndicator> getAllIndicators() {
    if (allIndicators == null) {
      allIndicators = new LinkedHashSet<MarcSubfieldIndicator>(indicatorMap.values());
      inherited.forEach(otherTag ->
      {
        final Set<MarcSubfieldIndicator> otherInds = otherTag.getAllIndicators();
        allIndicators.addAll(otherInds);
      });
    }
    return Collections.unmodifiableSet(allIndicators);
  }

  public static final Comparator<Character> myComparator = new MarcSubfieldComparator();

  /**
   * Liefert alle Unterfelder.
   *
   * @return Set != null, eventuell leer.
   */
  public final Collection<MarcSubfieldIndicator> getSortedIndicators() {
    final List<MarcSubfieldIndicator> myList = new ArrayList<>(getAllIndicators());
    Collections.sort(myList, (ind1, ind2) -> myComparator.compare(ind1.code, ind2.code));
    return myList;
  }

  /**
   * @param marc
   * @param german
   * @param repeatability
   * @param english
   * @param marcIndicator1
   * @param marcIndicator2
   */
  MarcTag(
    final String marc,
    final String german,
    final Repeatability repeatability,
    final String english,
    final String marcIndicator1,
    final String marcIndicator2) {
    super();
    this.marc = marc;
    this.german = german;
    this.repeatability = repeatability;
    this.english = english;
    this.marcIndicator1 = marcIndicator1;
    this.marcIndicator2 = marcIndicator2;
  }

  public void addInherited(final MarcTag tag) {
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    inherited.add(tag);
  }

  public final void add(final MarcSubfieldIndicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    indicatorMap.put(indicator.code, indicator);
  }

  @Override
  public final String toString() {
    return marc + " " + german + " (" + repeatability + ")";
  }

}
