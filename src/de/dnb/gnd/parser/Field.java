package de.dnb.gnd.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import javax.naming.OperationNotSupportedException;

import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.Tag;

/**
 * Enthält alle Zeilen zum gleichen Tag. Die Zeilen werden in einer
 * LinkedHashSet gespeichert, um einerseits die Einfügereihenfolge zu wahren,
 * andererseits aber Doppelungen zu vermeiden. Dadurch kann es zu Veränderungen
 * am Datensatz kommen, da die WinIBW auch Doppelungen zulässt.
 *
 * Veränderlich, daher clone().
 */
public final class Field implements Iterable<Line>, Cloneable {

  public final Tag tag;

  /**
   * nicht privat, damit Zeilen hinzugefügt oder entfernt werden können.
   * Das soll aber
   * nur von Record aus möglich sein, da sonst bei einem leeren Feld
   * dieses auch noch aus dem Record entfernt werden müsste.
   */
  /*private*/ArrayList<Line> lines = new ArrayList<>();

  @SuppressWarnings("unchecked")
  @Override
  public Field clone() {
    Field cloned;
    try {
      cloned = (Field) super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new InternalError(e.getMessage());
    }
    cloned.lines = (ArrayList<Line>) lines.clone();
    return cloned;
  }

  /**
   * Damit Felder nicht nur in Standardreihenfolge, sondern auch
   * in Reihenfolger der Pica+-Tags aufgezählt werden können.
   *
   * @author Christian
   *
   */
  public static class PicaPlusComparator implements Comparator<Field>, Serializable {

    private static final long serialVersionUID = -5449135955835489731L;

    @Override
    public final int compare(final Field f1, final Field f2) {
      final Tag tag1 = f1.tag;
      final Tag tag2 = f2.tag;
      return Tag.comparePicaPlus(tag1, tag2);
    }
  }

  /**
   *
   * Verkürzter Konstruktor, tag wird aus line entnommen.
   * Garantiert, dass immer eine Zeile enthalten ist.
   *
   * @param line	nicht null
   */
  public Field(final Line line) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    tag = line.getTag();
    lines.add(line);
  }

  public static void main(final String[] args)
    throws IllFormattedLineException,
    OperationNotSupportedException {
  }

  /**
   * Fügt line oder den Inhalt von line zum Feld hinzu. Dabei wird darauf
   * geachtet, dass eine Expansion nicht entfernt wird, um die
   * darin steckende Information zu erhalten. Das kann für die Manipulation
   * von Feldern von Interesse sein. Die Zeilen des Feldes werden hier wie eine
   * LinkedHashSet behandelt. Eine Zeile (ohne Expansion) kann also nur
   * einmal in einem Feld enthalten sein.
   * <br> Zum Parsen wird {@link this#add(Line) verwendet}
   *
   * @param line 	nicht null.	Ist line expandiert und ist das Feld
   * 				wiederholbar, so wird eine eventuell vorhandene
   * 				gleiche Zeile gnadenlos überschrieben.
   *        <br> Ist line nicht wiederholbar, wird zumindest versucht, aus beiden Zeilen
   *        eine neue zu basteln. Klappt das nicht, gibt es eine
   *        OperationNotSupportedException.
   * @throws OperationNotSupportedException
   *          	Wenn weder line noch der Inhalt zum Feld hinzugefügt
   *          	werden darf.
   */
  public void addWithoutDuplicates(final Line line) throws OperationNotSupportedException {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    if (tag != line.getTag())
      throw new IllegalArgumentException(
        "tags stimmen nicht überein field: " + tag + " line: " + line.getTag());
    if (tag.repeatability == Repeatability.REPEATABLE) {
      /* eine eventuell nicht-expandierte gleiche Zeile
       durch die neue an gleicher Stelle ersetzen.
       Die neue enthält möglicherweise mehr Information */
      final Line oldLine = FilterUtils.findPointerTo(lines, line);
      if (oldLine != null) {
        if (line.isExpanded())
          FilterUtils.replace(lines, oldLine, line);
      } else
        lines.add(line);
    } else {
      final Iterator<Line> iterator = lines.iterator();
      // Das sollte klappen, da Field garantiert nie leer ist:
      final Line myLine = iterator.next();
      // auftretende Exception wird durchgereicht:
      final Line newLine = myLine.add(line);
      lines.remove(myLine);
      lines.add(newLine);
    }

  }

  /**
   * Fügt line zum Feld hinzu.
   *
   * @param line  nicht null. Ist das Feld
   *        wiederholbar, so wird angehängt. Eine eventuell vorhandene
   *        gleiche Zeile kann somit gedoppelt werden. Das macht beim Parsen Sinn,
   *        da es immer wieder Datensätze gibt, die die gleiche Zeile zweimal
   *        enthalten.
   * @throws OperationNotSupportedException
   *            Wenn Feld nicht wiederholbar.
   * @throws IllegalArgumentException
   *            Wenn die Tags nicht übereinstimmen
   */
  public void add(final Line line) throws OperationNotSupportedException {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    if (tag != line.getTag())
      throw new IllegalArgumentException(
        "tags stimmen nicht überein field: " + tag + " line: " + line.getTag());
    if (tag.repeatability == Repeatability.REPEATABLE) {
      lines.add(line);
    } else {
      throw new OperationNotSupportedException("Nicht wiederholbar: " + line.getTag());
    }

  }

  /**
   * Paketöffentlich, da Field entfernt werden muss, wenn leer.
   * Entfernt Zeile aus Field.
   * @param line	nicht null.
   * @return		true, wenn line in lines enthalten war,
   * 				also Field geändert worden ist.
   */
  boolean remove(final Line line) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    return lines.remove(line);
  }

  /**
   * Ungefährlich, da Line immutable.
   * @return	eine nicht leere Liste von Zeilen.
   */
  public Collection<Line> getLines() {
    return Collections.unmodifiableCollection(lines);
  }

  /**
   * Ersetzt eine alte Zeile durch eine neue an der Position
   * der alten.
   *
   * @param original		nicht null
   * @param replacement	nicht null
   * @return				true, wenn ersetzt wurde.
   */
  public boolean replace(final Line original, final Line replacement) {
    RangeCheckUtils.assertReferenceParamNotNull("original", original);
    RangeCheckUtils.assertReferenceParamNotNull("replacement", replacement);
    return FilterUtils.replace(lines, original, replacement);
  }

  /**
   * gibt die anzahl der Zeilen.
   * @return Wert > 0.
   */
  public int size() {
    return lines.size();
  }

  @Override
  public Iterator<Line> iterator() {
    return lines.iterator();
  }

  @Override
  public String toString() {
    return lines.toString();
  }

  /**
   * Betrachtet nur den Tag, da sich die Zeilen ändern
   * können.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    return result;
  }

  /**
   * Zwei Felder sind gleich, wenn sie denselben Tag haben und
   * wenn ihre Line-Mengen gleich sind. Auf die Reihenfolge und
   * die Expansionen kommt es hierbei nicht an.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Field)) {
      return false;
    }
    final Field other = (Field) obj;
    if (!tag.equals(other.tag)) {
      return false;
    } else if (lines == null) {
      if (other.lines != null) {
        return false;
      }
    } else if (!lines.equals(other.lines)) {
      return false;
    }
    if (tag == null && (other.tag != null)) {
      return false;

    }
    return true;
  }

}
