package de.dnb.gnd.parser;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.NullIterator;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.RecordUtils;

/**
 * Kann verändert werden, daher clone().
 *
 * @author Christian
 *
 */
public final class Record implements Iterable<Line>, Cloneable, Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = -7690277804131571405L;

  /*private*/Map<Tag, Field> fieldMap = new TreeMap<>();

  String rawData = "";

  private final String id;

  public final TagDB tagDB;

  public Record(final String id, final TagDB tagDB) {
    super();
    this.id = id;
    this.tagDB = tagDB;
  }

  @Override
  public Record clone() {
    Record cloned;
    try {
      cloned = (Record) super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new InternalError(e.getMessage());
    }
    final Map<Tag, Field> newFieldMap = new TreeMap<>();
    cloned.fieldMap = newFieldMap;
    final Set<Map.Entry<Tag, Field>> entryset = fieldMap.entrySet();
    for (final Entry<Tag, Field> entry : entryset) {
      final Tag tag = entry.getKey();
      final Field field = entry.getValue().clone();
      newFieldMap.put(tag, field);
    }

    return cloned;
  }

  /**
   * Gibt eine neue Liste aller Felder, die  unbedenklich
   * verändert werden kann.
   *
   * @return	Ein Clone der Felder.
   */
  public Collection<Field> getFields() {
    final Collection<Field> fields = fieldMap.values();
    final List<Field> newFields = new LinkedList<>();
    for (final Field field : fields) {
      newFields.add(field.clone());
    }
    return newFields;
  }

  /**
   * Gibt eine neue Liste aller Zeilen, die  unbedenklich
   * verändert werden kann.
   *
   * @return  Ein Clone der Zeilen.
   */
  public List<Line> getLines() {
    final List<Line> newFields = new LinkedList<>();
    for (final Line line : this) {
      newFields.add(line);
    }
    return newFields;
  }

  /**
   * Gibt ein Clone eines Feldes oder null. Der ursprüngliche
   * Datensatz kann unbedenklich verändert werden.
   *
   * @param tag	nicht null
   * @return		Feld oder null.
   */
  public Field getField(final Tag tag) {
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    if (tag == null)
      throw new IllegalArgumentException("Tag ist null");
    final Field field = fieldMap.get(tag);
    if (field == null)
      return null;
    else
      return field.clone();
  }

  /**
   * Alias für set().
   *
   * @param field	nicht null.
   */
  public void add(final Field field) {
    RangeCheckUtils.assertReferenceParamNotNull("field", field);
    setField(field);
  }

  /**
   * Fügt eine Zeile zum Datensatz hinzu.
   *
   * @param line  nicht null
   * @throws OperationNotSupportedException
   *          Wenn weder die Zeile noch ihr Inhalt zum Datensatz hinzugefügt
   *          werden darf.
   */
  public void add(final Line line) throws OperationNotSupportedException {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    final Tag tag = line.getTag();
    Field field = fieldMap.get(tag);
    if (field == null) {
      if (!tagDB.contains(tag))
        throw new IllegalArgumentException("tag der zeile " + line
          + " ist nicht in der Datenbank von Datensatz " + id + " vorhanden");
      field = new Field(line);
      fieldMap.put(tag, field);
    } else
      try {
        field.add(line);
      } catch (final OperationNotSupportedException e) {
        throw new OperationNotSupportedException(
          e.getMessage() + " ; idn: " + id + " Zeile: " + line);
      }
  }

  /**
   * Fügt eine Zeile zum Datensatz hinzu. Es werden keine Dopplungen zugelassen.
   *
   * @param line  nicht null
   * @throws OperationNotSupportedException
   *          Wenn weder die Zeile noch ihr Inhalt zum Datensatz hinzugefügt
   *          werden darf.
   */
  public void addWithoutDuplicates(final Line line) throws OperationNotSupportedException {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    final Tag tag = line.getTag();
    Field field = fieldMap.get(tag);
    if (field == null) {
      if (!tagDB.contains(tag))
        throw new IllegalArgumentException("tag der zeile " + line
          + " ist nicht in der Datenbank von Datensatz " + id + " vorhanden");
      field = new Field(line);
      fieldMap.put(tag, field);
    } else
      try {
        field.addWithoutDuplicates(line);
      } catch (final OperationNotSupportedException e) {
        throw new OperationNotSupportedException(
          e.getMessage() + " ; idn: " + id + " Zeile: " + line);
      }
  }

  /**
   * Entfernt das zu tag gehörige Feld, sofern vorhanden.
   *
   * @param tag	beliebig.
   */
  public void removeField(final Tag tag) {
    if (tag != null)
      fieldMap.remove(tag);
  }

  /**
   * (Er)setzt das zum tag von field gehörige Feld.
   *
   * @param field nicht null.
   */
  public void setField(final Field field) {
    RangeCheckUtils.assertReferenceParamNotNull("field", field);
    fieldMap.put(field.tag, field);
  }

  /**
   * Entfernt eine Zeile vom Datensatz. Sollte das zugehörige Feld auch leer
   * sein, wird auch dieses entfernt.
   *
   * @param line	nicht null.
   * @return	ob die Zeile entfernt werden konnte.
   */
  public boolean remove(final Line line) {
    final Tag tag = line.getTag();
    final Field field = fieldMap.get(tag);
    if (field == null)
      return false;
    final boolean removed = field.remove(line);
    // leere Felder ganz entfernen:
    if (field.getLines().isEmpty())
      fieldMap.remove(tag);
    return removed;
  }

  public String getRawData() {
    return rawData;
  }

  /**
   * Gibt den Inhalt des Feldes 003@. Wenn nicht vorhanden, die
   * anderweitig geparste ID.
   *
   * @return	ID oder null, wenn nicht ermittelbar.
   */
  public String getId() {
    String idRet = RecordUtils.getContentOfSubfield(this, "003@", '0');
    if (idRet != null)
      return idRet;
    else {
      if (id != null)
        return id;
    }
    idRet = RecordUtils.getContentOfSubfield(this, "007G", '0');
    if (idRet != null)
      return idRet;
    else {
      return RecordUtils.getContentOfSubfield(this, "007K", '0');
    }
  }

  /**
   * Gibt die vergebenen Tags.
   *
   * @return	neue Menge, die bedenkenlos geändert werden kann.
   */
  public LinkedHashSet<Tag> getTags() {
    return new LinkedHashSet<>(fieldMap.keySet());
  }

  final class MyIterator implements Iterator<Line> {

    private MyIterator(final Format format) {
      if (format == Format.PICA_PLUS) {
        final Collection<Field> theFields = getFields();
        final TreeSet<Field> set = new TreeSet<>(new Field.PicaPlusComparator());
        set.addAll(theFields);
        final Iterator<Field> iteratorF = set.iterator();
        fieldsIterator = iteratorF;
      } else
        fieldsIterator = getFields().iterator();
      lineIterator = new NullIterator<>();
    }

    private Iterator<Line> lineIterator;

    private Iterator<Field> fieldsIterator;

    @Override
    public boolean hasNext() {
      /*
       * Felder sind per definitionem nicht leer (enthalten mindestens
       * eine Zeile)
       */
      return fieldsIterator.hasNext() || lineIterator.hasNext();
    }

    @Override
    public Line next() {
      if (!lineIterator.hasNext()) {
        /*
         * Ist möglich, da fieldsIterator.hasNext() garantiert, dass
         * da mindestens noch ein Feld in fieldMap vorhanden ist.
         * Da aber Felder per definitionem nicht leer sind, enthält das
         * nächste Feld immer noch eine Zeile:
         */
        lineIterator = fieldsIterator.next().iterator();
      }
      return lineIterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  @Override
  public Iterator<Line> iterator() {
    return new MyIterator(Format.PICA3);
  }

  public Iterator<Line> picaPlusIterator() {
    return new MyIterator(Format.PICA_PLUS);
  }

  @Override
  public String toString() {
    String s = "IDN: " + id;
    for (final Line line : this) {
      s += Constants.LINE_SEPARATOR + line;
    }
    return s;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    // fieldMap dürfen nicht einbezogen werden, da veränderlich!
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /**
   * Überprüft, ob alle Felder (unabhängig von Reihenfolge und Expansion) und
   * die idn gleich sind.
   */
  @Override
  public boolean equals(final Object obj) {
    if (!contentEquals(obj)) {
      return false;
    }
    final Record other = (Record) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /**
   * Überprüft, ob alle Felder (unabhängig von Reihenfolge und Expansion) aber
   * nicht die idns gleich sind.
   */
  public boolean contentEquals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Record)) {
      return false;
    }
    final Record other = (Record) obj;
    if (fieldMap == null) {
      if (other.fieldMap != null) {
        return false;
      }
    } else if (!fieldMap.equals(other.fieldMap)) {
      return false;
    }
    return true;
  }

  /**
   * @param args
   * @throws IllFormattedLineException
   * @throws OperationNotSupportedException
   */
  public static void main(final String[] args)
    throws IllFormattedLineException,
    OperationNotSupportedException {
    final Record record = RecordUtils.readFromClip();

    System.out.println(record.getId());
  }

}
