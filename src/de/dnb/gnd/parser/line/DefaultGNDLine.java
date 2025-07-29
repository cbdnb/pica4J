package de.dnb.gnd.parser.line;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.DefaultGNDTag;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;

public final class DefaultGNDLine extends Line implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 3869492408231534272L;

  protected DefaultGNDLine(final GNDTag aTag) {
    super(aTag);
    subfields = new LinkedList<Subfield>();
  }

  /**
   * Paketöffentlich, daher immutable, kein clone().
   * 
   * @param aTag 		nicht null
   * @param subfieldColl	nicht null.
   */
  DefaultGNDLine(final GNDTag aTag, final Collection<Subfield> subfieldColl) {
    this(aTag);
    RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfieldColl", subfieldColl);
    for (final Subfield subfield : subfieldColl) {
      add(subfield);
    }
  }

  DefaultGNDLine(final GNDTag tag, final Subfield... subfields) throws IllFormattedLineException {
    this(tag, Arrays.asList(subfields));
  }

  /**
   * 
   * Addiert den Inhalt von line zum Feld hinzu.
   * 
   * @param otherLine nicht null
   * @throws OperationNotSupportedException 
   * 			Wenn der Inhalt von otherLine nicht 
   * 			hinzugefügt werden darf.
   */
  @Override
  public DefaultGNDLine add(final Line otherLine) throws OperationNotSupportedException {
    RangeCheckUtils.assertReferenceParamNotNull("otherLine", otherLine);
    if (tag != otherLine.tag)
      throw new IllegalArgumentException("add: tags stimmen nicht überein");
    final Collection<Indicator> indicators = tag.getOwnIndicators();
    if (indicators.size() != 1)
      throw new OperationNotSupportedException(
        "zu GNDTag \"" + tag + "\" kann nicht addiert werden");
    final Indicator indicator = indicators.iterator().next();
    if (indicator.repeatability != Repeatability.REPEATABLE)
      throw new OperationNotSupportedException("Indikator " + indicator + " nicht wiederholbar");

    final Collection<Subfield> subfields = new LinkedList<>(this.subfields);
    subfields.addAll(otherLine.getSubfields());

    return new DefaultGNDLine((DefaultGNDTag) tag, subfields);

  }

  public static void main(final String[] args)
    throws IllFormattedLineException,
    OperationNotSupportedException {
    final Line line1 = LineParser.parse("675 a", GNDTagDB.getDB(), false);
    final Line line2 = LineParser.parse("675 a", GNDTagDB.getDB(), false);
    System.out.println(line1 == (line2));

  }

}
