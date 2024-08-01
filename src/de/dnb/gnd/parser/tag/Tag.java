package de.dnb.gnd.parser.tag;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Pica3Comparator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.GNDLineFactory;
import de.dnb.gnd.parser.line.LineFactory;

/**
 * Hat mehrere Aufgaben:
 * 	-	Beschreibung der Tags zu liefern und eine Übersetzung pica <-> pica+
 * 		zu liefern. Auf der pica3-Beschreibung basieren die üblichen
 * 		Vergleichsoperationen.
 * 	-	Die Gesamtmenge der Indikatoren zu liefern.
 * 	-	Die Factory zu liefern.
 *
 * Da von der {@link TagDB} noch Änderungen vorgenommen werden, sind nicht alle
 * Membervariablen final. Trotzdem immutable, daher kein clone().
 *
 * @param <T> 	Ein Untertyp zu Tag, da die Liste der vererbten Tags (z.B.
 * 				erbt 5110 von 5100) damit parametriert ist.
 *
 * @author baumann
 *
 */
public abstract class Tag implements Comparable<Tag> {

  /**
   *
   */
  private static final Pica3Comparator PICA3_COMPARATOR = new Pica3Comparator();
  public final String pica3;
  public final String picaPlus;
  public final String german;
  public final Repeatability repeatability;
  public final String english;
  public final String marc;
  public final char marcIndicator1;
  public final char marcIndicator2;

  /**
   * Zu vernachlässigender Indikator: kommt in Pica3 nicht vor,
   * jedoch in Pica+.
   */
  protected Indicator ignorableIndicator = null;

  public Indicator getIgnorableIndicator() {
    if (ignorableIndicator != null)
      return ignorableIndicator;
    final List<Tag> inheritedTags = getInherited();
    for (final Tag tag : inheritedTags) {
      if (tag.ignorableIndicator != null)
        return tag.ignorableIndicator;
    }
    return null;
  }

  public boolean hasIgnorableIndicator() {
    return getIgnorableIndicator() != null;
  }

  public boolean isIgnorable(final Indicator indicator) {
    return StringUtils.equals(indicator, getIgnorableIndicator());
  }

  /**
   * aleph-Feld-Nr. Wenn null, dann in Aleph nicht definiert.
   */
  public final String aleph;

  private final List<Tag> inherited = new LinkedList<>();

  public List<Tag> getInherited() {
    return Collections.unmodifiableList(inherited);
  }

  public void addInherited(final Tag tag) {
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    inherited.add(tag);
  }

  /**
   * Grundlegende Datenstruktur.
   */
  protected Map<Character, Indicator> indicatorMap = new LinkedHashMap<>();

  @SuppressWarnings("boxing")
  public final void add(final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    indicatorMap.put(indicator.indicatorChar, indicator);
  }

  final void addIgnorable(final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    if (ignorableIndicator == null)
      ignorableIndicator = indicator;
    else
      throw new IllegalArgumentException("Es gibt schon einen ignorierbaren Indikator");
    add(indicator);
  }

  // ------- Hilfsstruktur zur Speicherung von Zwischenergebnissen:
  protected LinkedHashSet<Indicator> allIndicators;

  /**
   * Liefert alle Unterfelder, auch die relationierten.
   *
   * @return Set != null, eventuell leer.
   */
  public final Set<Indicator> getAllIndicators() {
    if (allIndicators == null) {
      allIndicators = new LinkedHashSet<>(getOwnIndicators());
      if (related != null) {
        allIndicators.addAll(related.getOwnIndicators());
      }
    }
    return Collections.unmodifiableSet(allIndicators);
  }

  /**
   * Liefert eine Map, die von den Indikatorzeichen auf die Indikator-
   * Objekte verweist.
   *
   * @param optionalsIncluded		Wenn die Zeile keine Relation enthält,
   * 								obwohl sie eine enthalten könnte (z.B.
   * 								5XX). Dann werden durch
   * 								<pre><code>optionalsIncluded == true</code></pre>
   * 								die Felder aus 1XX hinzugenommen.
   *
   * @return						nicht null.
   */
  public Map<Character, Indicator> getIndicatorMap(final boolean optionalsIncluded) {
    final Map<Character, Indicator> map = new LinkedHashMap<>(indicatorMap);
    for (final Tag tag : inherited) {
      map.putAll(tag.indicatorMap);
    }
    if (optionalsIncluded && related != null)
      map.putAll(related.indicatorMap);
    return map;

  }

  /**
   * Liefert das Unterfeld, das im pica3-Format am Anfang
   * stehen darf/muss. Bei Titeldaten (alt) immer null.
   *
   * @return Indicator eventuell null.
   */
  public Indicator getDefaultFirst() {
    return null;
  }

  /**
   * Vergleicht pica+ für den Fall der Ausgabe im pica+-Format.
   *
   * @param tag1	nicht null
   * @param tag2	nicht null
   * @return		Vergleich
   */
  public static final int comparePicaPlus(final Tag tag1, final Tag tag2) {
    RangeCheckUtils.assertReferenceParamNotNull("tag1", tag1);
    RangeCheckUtils.assertReferenceParamNotNull("tag2", tag2);
    final String picap1 = tag1.picaPlus;
    final String picap2 = tag2.picaPlus;

    // Ausnahmen in Titeldaten
    if (picap1.equals("208@/01") && (picap2.equals("201B/01") || picap2.equals("203@/01")))
      return -1;
    if (picap2.equals("208@/01") && (picap1.equals("201B/01") || picap1.equals("203@/01")))
      return 1;
    return picap1.compareTo(picap2);
  }

  Tag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("pica3", pica3);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("picaPlus", picaPlus);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("german", german);
    this.pica3 = pica3;
    this.picaPlus = picaPlus;
    this.marc = marc;
    this.german = german;
    this.english = english;
    this.repeatability = repeatability;
    aleph = marc;
    marcIndicator1 = 0;
    marcIndicator2 = 0;
  }

  Tag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english,
    final String aleph) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("pica3", pica3);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("picaPlus", picaPlus);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("german", german);
    if (aleph != null)
      RangeCheckUtils.assertStringParamNotNullOrWhitespace("aleph", aleph);
    this.pica3 = pica3;
    this.picaPlus = picaPlus;
    this.marc = marc;
    this.german = german;
    this.english = english;
    this.repeatability = repeatability;
    this.aleph = aleph;
    marcIndicator1 = 0;
    marcIndicator2 = 0;
  }

  /**
   *
   * @param pica3
   * @param picaPlus
   * @param german
   * @param repeatability
   * @param marc
   * @param marcIndicator1	Marc verwendet Indicator in anderem Sinne
   * 							als wir
   * @param marcIndicator2	Marc verwendet Indicator in anderem Sinne
   * 							als wir
   * @param english
   */
  Tag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final char marcIndicator1,
    final char marcIndicator2,
    final String english) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("pica3", pica3);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("picaPlus", picaPlus);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("german", german);
    this.pica3 = pica3;
    this.picaPlus = picaPlus;
    this.marc = marc;
    this.german = german;
    this.english = english;
    this.repeatability = repeatability;
    aleph = marc;
    this.marcIndicator1 = marcIndicator1;
    this.marcIndicator2 = marcIndicator2;
  }

  @Override
  public final String toString() {
    return pica3 + " " + german + " (" + repeatability + ")";
  }

  @Override
  public final int compareTo(final Tag otherTag) {
    RangeCheckUtils.assertReferenceParamNotNull("otherTag", otherTag);
    final String thisPica3 = pica3;
    final String otherpica3 = otherTag.pica3;
    return PICA3_COMPARATOR.compare(thisPica3, otherpica3);
  }

  @Override
  public final int hashCode() {
    return pica3.hashCode();
  }

  @Override
  public final boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Tag other = (Tag) obj;
    if (pica3 == null) {
      if (other.pica3 != null)
        return false;
    } else if (!pica3.equals(other.pica3))
      return false;
    return true;
  }

  /**
   * Liefert die {@link GNDLineFactory}.
   *
   * @return	nicht null
   */
  public abstract LineFactory getLineFactory();

  /**
   * Gibt den Indikator, sofern er bei diesem Tag erlaubt ist.
   *
   * @param ind Indikatorzeichen nach "$"
   *
   * @return		Indikator oder null.
   */
  public abstract Indicator getIndicator(final char ind);

  /**
   * Manche Tags (alle 5XX) können, müssen aber keine Relation enthalten.
   * Enthalten sie keine Relation, so borgen sie vom relationierten GNDTag
   * alle Unterfelder.
   */
  Tag related = null;

  protected LinkedHashSet<Indicator> ownIndicators;

  /**
   * Liefert die eigenen Unterfelder, also genau die, die zusätzlich zu $8
   * und $9  auftauchen dürfen.
   * Diese Liste kann z.B. beim Aufbau von GUIs gut sein.
   *
   * Die Reihenfolge der Indikatoren entspricht der Einfügereihenfolge. Damit
   * kann für den Parsevorgang auf eine definierte Reihenfolge zurückgegriffen
   * werden. Das kann z.B. für Postfixe interessant werden.
   *
   * @return Set != null, eventuell leer.
   */
  public Set<Indicator> getOwnIndicators() {
    if (ownIndicators == null) {
      ownIndicators = new LinkedHashSet<>(indicatorMap.values());
      for (final Tag tag : inherited) {
        ownIndicators.addAll(tag.getOwnIndicators());
      }
    }
    return Collections.unmodifiableSet(ownIndicators);
  }

  /**
   * Gibt den Indikator.
   *
   * @param ind Indikatorzeichen nach "$"
   * @param optionalsIncluded
   * 				optional sind die relationierten Felder
   * 				(z.B. 100 zu 500), wenn keine Relations-IDN
   * 				bekannt ist.
   * @return		Indikator oder null. Besser NULLINDIKATOR?
   */
  @SuppressWarnings("boxing")
  public final Indicator getIndicator(final char ind, final boolean optionalsIncluded) {
    Indicator found = indicatorMap.get(ind);
    if (found != null)
      return found;
    for (final Tag tag : inherited) {
      found = tag.getIndicator(ind, optionalsIncluded);
      if (found != null)
        return found;
    }
    if (optionalsIncluded && related != null)
      found = related.getIndicator(ind, true);
    return found;

  }

}
