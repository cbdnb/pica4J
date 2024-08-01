package de.dnb.gnd.parser.tag;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import de.dnb.basics.filtering.FilterUtils;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.GNDLineFactory;

/**
 * Hat mehrere Aufgaben:
 * 	-	Beschreibung der Tags zu liefern und eine Übersetzung pica <-> pica+
 * 		zu liefern.
 * 	-	Die zulässigen Unterfelder zu liefern.
 *
 * Da von {@link GNDTagDB} noch Änderungen vorgenommen werden, sind nicht alle
 * Membervariablen final. Trotzdem immutable, daher kein clone().
 *
 * @author baumann
 *
 */
public abstract class GNDTag extends Tag {

  /**
   *  Tags, von denen Unterfelder übernommen werden.
   *  z.B. 5102 von 5100.
   *  Das können aber auch künstlich gebildete Tags wie 5XX sein.
   *  Das erleichtert die
   *  Wartbarkeit, wenn Änderungen an einer ganzen Gruppe vorgenommen werden.
   */

  public static final String OHNE = "-ohne-";

  // ------- Hilfsstrukturen zur Speicherung von Zwischenergebnissen:
  /*
   * Die Indikatoren, die Deskriptionszeichen (Präfixe) enthalten
   * (z.B. ", " bei 100 $d)
   */
  private Set<Indicator> descriptionStrings;

  //@formatter:off
	GNDTag(
			final String pica3,
			final String picaPlus,
			final String german,
			final Repeatability repeatability,
			final String marc,
			final String english) {
		super(pica3, picaPlus, german, repeatability, marc, english);
	}

	GNDTag(
		final String pica3,
		final String picaPlus,
		final String german,
		final Repeatability repeatability,
		final String marc,
		final String english,
		final String aleph) {
	super(pica3, picaPlus, german, repeatability, marc, english, aleph);
}

	@Override
	public abstract GNDLineFactory getLineFactory();
	//@formatter:on

  protected Indicator defaultFirst = null;

  public final void addDefaultFirst(final Indicator indicator) {
    add(indicator);
    defaultFirst = indicator;
  }

  @Override
  public final Indicator getIndicator(final char ind) {
    return getIndicator(ind, true);
  }

  /**
   * Liefert die Unterfelder, die auch im pica3-Format durch
   * Deskriptionszeichen ersetzt werden müssen. (Gilt nur für GND).
   * Liefert nicht $9, da hierfür eine Sonderbehandlung beim Parsen
   * erforderlich ist.
   * Liefert auch die relationierten Unterfelder (wegen Expansion).
   *
   * @return Set != null, eventuell leer.
   */
  public final Set<Indicator> getDescriptionStrings() {
    if (descriptionStrings == null) {

      descriptionStrings = new HashSet<>(getAllIndicators());

      /**
       * null-Präfixe und $9 entfernen.
       */
      final Predicate<Indicator> predicate = indicator -> (indicator.prefix != null)
        && indicator != TagDB.DOLLAR_9 && indicator != TagDB.DOLLAR_8;
      FilterUtils.filter(descriptionStrings, predicate);

    }
    return Collections.unmodifiableSet(descriptionStrings);
  }

  /**
   * Liefert das Unterfeld, das im pica3-Format am Anfang
   * stehen darf/muss.
   *
   * @return Indicator eventuell null.
   */
  @Override
  public final Indicator getDefaultFirst() {
    Indicator sub = defaultFirst;
    if (sub != null)
      return sub;
    for (final Tag tag : getInherited()) {
      sub = tag.getDefaultFirst();
      if (sub != null)
        return sub;
    }
    if (related == null)
      return null;
    else
      return related.getDefaultFirst();
  }

  public static void main(final String[] args) {
    final GNDTag tag = (GNDTag) GNDTagDB.getDB().findTag("500");
    System.out.println(tag.related);
    System.out.println(tag.getInherited());
    System.out.println(tag.indicatorMap);
    System.out.println(tag.getIndicatorMap(false));
  }

}
