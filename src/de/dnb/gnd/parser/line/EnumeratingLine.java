package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.EnumeratingTag;
import de.dnb.gnd.parser.tag.GNDTagDB;

/**
 * Felder (Zeilen), die Informationen -in der Regel Codes- akkumulieren:
 * Entitätencode, Ländercode, GND-Systematik ...
 *
 * @author baumann
 *
 */
public class EnumeratingLine extends Line {

  public EnumeratingLine(final EnumeratingTag aTag, final Collection<Subfield> subfieldColl) {
    //@formatter:off
		super(aTag);
		//@formatter:on
    subfields = new LinkedHashSet<Subfield>();
    for (final Subfield subfield : subfieldColl) {
      super.add(subfield);
    }
  }

  @Override
  public final EnumeratingLine add(final Line otherLine) throws OperationNotSupportedException {
    RangeCheckUtils.assertReferenceParamNotNull("otherLine", otherLine);
    //@formatter:off
		if (tag != otherLine.tag)
			throw new IllegalArgumentException(
					"EnumeratingLine.add: tags stimmen nicht überein");
		//@formatter:on
    final Collection<Subfield> newSubfields = new LinkedList<>(subfields);
    newSubfields.addAll(otherLine.subfields);
    return new EnumeratingLine((EnumeratingTag) tag, newSubfields);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public final boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EnumeratingLine)) {
      return false;
    }
    final EnumeratingLine other = (EnumeratingLine) obj;
    if (tag == null) {
      if (other.tag != null)
        return false;
    } else if (!tag.equals(other.tag))
      return false;
    // nur noch Set-Gleichheit:
    return subfields.equals(other.subfields);
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((subfields == null) ? 0 : subfields).hashCode();
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    return result;
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final Line line1 = LineParser.parse("008 a;b", GNDTagDB.getDB(), false);
    final Line line2 = LineParser.parse("008 b;a", GNDTagDB.getDB(), false);
    System.out.println(line1.hashCode());
    System.out.println(line2.hashCode());
  }

}
