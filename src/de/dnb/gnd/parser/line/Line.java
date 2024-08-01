package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.RecordUtils;

/**
 * Repräsentiert eine Zeile = Tag + Liste von Unterfeldern.
 * Immutable, daher kein clone().
 *
 * @param <T>		der mit Line verbundene Tag-Typ.
 *
 * @author Baumann
 *
 */
public abstract class Line {

  protected Tag tag;

  /**
   * Gibt eine neue Zeile, die Unterfelder von dieser und der
   * anderen Zeile werden zusammengeführt.
   *
   * @param otherLine		nicht null.
   * @return				Neue Zeile mit zusammengeführten Unterfeldern.
   * @throws OperationNotSupportedException
   * 						Wenn es kein Wiederholungszeichen (;) gibt.
   */
  public abstract Line add(final Line otherLine) throws OperationNotSupportedException;

  private String idnRelated = null;
  private String expansion = null;
  protected Collection<Subfield> subfields;

  protected Line(final Tag aTag) {
    RangeCheckUtils.assertReferenceParamNotNull("aTag", aTag);
    tag = aTag;
  }

  /**
   * wird nur von den Konstruktoren aufgerufen.
   *
   * @param subfield nicht null
   */
  protected void add(final Subfield subfield) {
    RangeCheckUtils.assertReferenceParamNotNull("subfield", subfield);
    subfields.add(subfield);
    final Indicator indicator = subfield.getIndicator();
    if (indicator == TagDB.DOLLAR_9)
      idnRelated = subfield.getContent();
    else if (indicator == TagDB.DOLLAR_8)
      expansion = subfield.getContent();
  }

  /**
   * Der tag.
   *
   * @return nicht null.
   */
  public final Tag getTag() {
    return tag;
  }

  /**
   * Gibt die Unterfelder in der Reihenfolge des gewünschten
   * Formats. Abweichungen bisher nur bei Personennamen. Änderungen an
   * dieser Liste haben keine Auswirkungen auf die ursprüngliche Zeile.
   *
   * @param format pica3 oder pica+
   * @return nicht null, modifizierbar.
   */
  public List<Subfield> getSubfields(final Format format) {
    return new LinkedList<>(subfields);
  }

  /**
   * Gibt die Unterfelder in der Reihenfolge von pica3.
   *
   * @return nicht null, modifizierbar.
   */
  public List<Subfield> getSubfields() {
    return getSubfields(Format.PICA3);
  }

  @Override
  public String toString() {
    return RecordUtils.toString(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((subfields == null) ? 0
      : RecordUtils.removeExpansion(RecordUtils.getRelevantSubfields(tag, subfields)).hashCode());
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Line other = (Line) obj;
    if (tag == null) {
      if (other.tag != null)
        return false;
    } else if (!tag.equals(other.tag))
      return false;
    if (subfields == null) {
      if (other.subfields != null)
        return false;
    } else {
      Collection<Subfield> ownSubs = RecordUtils.removeExpansion(subfields);
      ownSubs = RecordUtils.getRelevantSubfields(tag, ownSubs);
      Collection<Subfield> otherSubs = RecordUtils.removeExpansion(other.subfields);
      otherSubs = RecordUtils.getRelevantSubfields(tag, otherSubs);
      if (!ownSubs.equals(otherSubs))
        return false;
    }

    return true;
  }

  /**
   * Enthält der Datensatz eine Relation?
   * @return	true, wenn ja.
   */
  public final boolean isRelated() {
    return idnRelated != null;
  }

  /**
   * Die relationierte IDN in $9.
   * @return	IDN oder null
   */
  public final String getIdnRelated() {
    return idnRelated;
  }

  /**
   * @deprecated
   * Die Expansion des relationierten Datensatzes in $8.
   * @return	Expansion oder null
   */
  @Deprecated
  public final String getExpansion() {
    return expansion;
  }

  /**
   * Enthält der Datensatz eine Expansion?
   * @return	true, wenn ja.
   */
  public final boolean isExpanded() {
    return expansion != null;
  }

}
