package de.dnb.gnd.parser.line;

import java.io.Serializable;
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
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.GNDPersonTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.formatter.RDAFormatter;

public class GNDPersonLine extends Line implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 869238384380402819L;
  /**
   * $a, Nachname
   */
  private Subfield familyName;

  /**
   * @return $a, Nachname
   */
  public Subfield getFamilyName() {
    return familyName;
  }

  /**
   * @param familyName $a, Nachname
   */
  public void setFamilyName(final Subfield familyName) {
    this.familyName = familyName;
  }

  /**
   * $d, Vorname
   */
  private Subfield firstName;

  /**
   * @return $d, Vorname
   */
  public Subfield getFirstName() {
    return firstName;
  }

  /**
   * $c, z.B. Scheppensted, Maria$cvon
   */
  private Subfield prefix;

  /**
   * @return $c, z.B. Scheppensted, Maria$cvon
   */
  public Subfield getPrefix() {
    return prefix;
  }

  /**
   * $P, z.B. $PBernadette$lHeilige
   */
  private Subfield personalName;

  /**
   * @return $P, z.B. $PBernadette$lHeilige
   */
  public Subfield getPersonalName() {
    return personalName;
  }

  /**
   * @param personalName $P, z.B. $PBernadette$lHeilige
   */
  public void setPersonalName(final Subfield personalName) {
    this.personalName = personalName;
  }

  /**
   * $n, $PElisabeth$nI.$lEngland, Königin
   */
  private Subfield zaehlung;

  /**
   * @return $n in $PElisabeth$nI.$lEngland, Königin
   */
  public Subfield getZaehlung() {
    return zaehlung;
  }

  /**
   * $l, $PElisabeth$nI.$lEngland, Königin
   */
  private Subfield territorium;

  /**
   * @return $l in $PElisabeth$nI.$lEngland, Königin
   */
  public Subfield getTerritorium() {
    return territorium;
  }

  GNDPersonLine(final GNDPersonTag tag, final Collection<Subfield> subfieldColl) {
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
    /*
     * Neue Regel: Nachname ohne Vorname kommt in $P, Präfix $c wird
     * dann immer nachgestellt:
     */
    subs.add(personalName);
    if (format == Format.PICA3) {
      subs.add(familyName);
      subs.add(firstName);
      subs.add(prefix);
      subs.addAll(super.getSubfields(format));
    } else {
      subs.add(firstName);
      subs.add(prefix);
      subs.add(familyName);
      subs.addAll(super.getSubfields(format));
    }

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
    else if (indChar == 'P')
      personalName = subfield;
    else if (indChar == 'n') {
      zaehlung = subfield;
      super.add(subfield);
    } else if (indChar == 'l') {
      territorium = subfield;
      super.add(subfield);
    } else
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
    result = prime * result + ((personalName == null) ? 0 : personalName.hashCode());
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
    if (!(obj instanceof GNDPersonLine)) {
      return false;
    }
    final GNDPersonLine other = (GNDPersonLine) obj;
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
    if (personalName == null) {
      if (other.personalName != null) {
        return false;
      }
    } else if (!personalName.equals(other.personalName)) {
      return false;
    }
    return true;
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final Line line = LineParser.parse("100 $PGuichard$lTroyes, Bischof ", GNDTagDB.getDB(), false);

    System.out.println(line.getSubfields(Format.PICA3));
    System.out.println(RecordUtils.toPicaWithoutTag(line));
    System.out.println(RecordUtils.toPica(line, Format.PICA_PLUS, true, '$'));
    System.out.println(line);
    final RDAFormatter formatter = new RDAFormatter(new Record("", GNDTagDB.getDB()));
    System.out.println(formatter.format(line));
  }

}
