package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.NullPredicate;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.BiblioPersonTag;

public class BiblioPersonLine extends BibliographicLine {

  private Subfield familyName;

  private Subfield firstName;

  private Subfield prefix;

  BiblioPersonLine(final BiblioPersonTag tag, final Collection<Subfield> subfieldColl) {
    super(tag);
    subfields = new LinkedList<Subfield>();
    for (final Subfield subfield : subfieldColl) {
      this.add(subfield);
    }
  }

  @Override
  public final List<Subfield> getSubfields(final Format format) {
    RangeCheckUtils.assertReferenceParamNotNull("format", format);
    final List<Subfield> subs = new LinkedList<>();
    if (format == Format.PICA3) {
      subs.add(familyName);
      subs.add(firstName);
      subs.add(prefix);
    } else {
      subs.add(firstName);
      subs.add(prefix);
      subs.add(familyName);
    }
    subs.addAll(super.getSubfields(format));
    final Predicate<Subfield> nullFilter = new NullPredicate<Subfield>().negate();
    return FilterUtils.newFilteredList(subs, nullFilter);

  }

  @Override
  public final Line add(final Line otherLine) throws OperationNotSupportedException {
    throw new OperationNotSupportedException(
      "Kann zu einer Person keinen " + "Inhalt einer anderen Person hinzufügen");
  }

  @Override
  protected final void add(final Subfield subfield) {
    RangeCheckUtils.assertReferenceParamNotNull("subfield", subfield);
    final Indicator ind = subfield.getIndicator();
    final char indChar = ind.indicatorChar;
    if (indChar == 'a')
      familyName = subfield;
    else if (indChar == 'd')
      firstName = subfield;
    else if (indChar == 'c')
      prefix = subfield;
    else
      super.add(subfield);
  }

  @Override
  public final String toString() {
    return super.toString();
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((familyName == null) ? 0 : familyName.hashCode());
    result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
    result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
    return result;
  }

  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof BiblioPersonLine)) {
      return false;
    }
    final BiblioPersonLine other = (BiblioPersonLine) obj;
    if (familyName == null) {
      if (other.familyName != null) {
        return false;
      }
    } else if (!familyName.equals(other.familyName)) {
      return false;
    }
    if (firstName == null) {
      if (other.firstName != null) {
        return false;
      }
    } else if (!firstName.equals(other.firstName)) {
      return false;
    }
    if (prefix == null) {
      if (other.prefix != null) {
        return false;
      }
    } else if (!prefix.equals(other.prefix)) {
      return false;
    }
    return true;
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final Line line1;
  }

}
