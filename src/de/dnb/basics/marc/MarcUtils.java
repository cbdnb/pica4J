package de.dnb.basics.marc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlHandler;
import org.marc4j.MarcXmlParser;
import org.marc4j.MarcXmlReader;
import org.marc4j.RecordStack;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.xml.sax.InputSource;

import de.dnb.basics.applicationComponents.StreamUtils;
import de.dnb.basics.applicationComponents.strings.StringInputStream;
import de.dnb.basics.applicationComponents.strings.StringOutputStream;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.HTMLEntities;
import de.dnb.basics.utils.HTMLUtils;

public final class MarcUtils {

  private static Comparator<VariableField> vfComparator = (vf1, vf2) ->
  {
    final String tag1 = vf1.getTag();
    final String tag2 = vf2.getTag();
    return tag1.compareTo(tag2);
  };

  private MarcUtils() {
  }

  /**
   * Liest Record im xml-Format aus der Zwischenablage.
   * Wandelt XML (auch ohne Vorspann) in Marc-Record um.
   *
   * @return	record oder null.
   */
  public static Record readXMLfromClip() {
    final String xml = StringUtils.readClipboard();
    return xml2Record(xml);
  }

  /**
   * Anzuwenden, wenn der String mehrere Datensätze enthält.
   * Dann wird der erste geliefert.
   * Wandelt XML (auch ohne Vorspann) in Marc-Record um.
   *
   * @param xml   auch null
   * @return      null, wenn xml == null
   */
  public static Record xml2Record(String xml) {
    if (xml == null)
      return null;
    if (!xml.startsWith("<?xml version")) {
      xml = "<?xml version=\"1.0\"?>"
        + "<mx:collection xmlns:mx=\"http://www.loc.gov/MARC21/slim\">" + xml + "</mx:collection>";
    }
    /*
     * .getBytes():
     * "This method always replaces malformed-input and
     * unmappable-character sequences with this charset's default
     * replacement byte array."
     */
    final InputStream input = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
    final MarcXmlReader reader = new MarcXmlReader(input);
    final Record record = reader.next();
    return record;
  }

  /**
   * Ersetzt '$i' durch ' |i|:'.
   *
   * @param record	nicht null
   * @return			besser lesbar
   */
  public static String readableFormat(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    String s = record.toString();
    s = s.replaceAll("\\$(.)", " |$1|:");
    return s;
  }

  /**
   * List einen Datensatz im Marc-Format von der Zwischenablage.
   *
   * @return	Datensatz oder null
   */
  public static Record readfromClip() {
    final String strg = StringUtils.readClipboard();
    if (strg == null)
      return null;
    final StringInputStream inputStr = new StringInputStream(strg);
    final MarcStreamReader reader = new MarcStreamReader(inputStr);
    final Record record = reader.next();
    return record;
  }

  /**
   *
   * @param record nicht null
   * @param tag		nicht null
   * @return          tag in record enthalten
   */
  public static boolean containsField(final Record record, final String tag) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    final List<VariableField> fields = record.getVariableFields(tag);
    return !fields.isEmpty();
  }

  /**
   * Enthält das Datenfeld den code (Code ist unser Indikator)?
   *
   * @param dataField	nicht null
   * @param code		beliebig
   * @return          Indikator code im Feld enthalten
   */
  public static boolean containsCode(final DataField dataField, final char code) {
    RangeCheckUtils.assertReferenceParamNotNull("dataField", dataField);
    return !dataField.getSubfields(code).isEmpty();
  }

  /**
   * Hilfsmethode mit varargs.
   *
   * @param record nicht null
   * @param tags	nicht null
   * @return		alle variablen Felder
   */
  public static List<VariableField> getVariableFields(final Record record, final String... tags) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
    return record.getVariableFields(tags);
  }

  /**
   *
   * @param record	nicht null
   * @param tags		nicht null
   * @return			Liste der Datenfelder (nicht null) oder wirft
   * 					IllegalArgumentException, wenn
   * 					einer der tags zu keinem Datenfeld gehört
   */
  public static List<DataField> getDataFields(final Record record, final String... tags) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
    final List<DataField> dataFields = new LinkedList<>();
    for (final VariableField variableField : record.getVariableFields(tags)) {
      try {
        dataFields.add((DataField) variableField);
      } catch (final ClassCastException e) {
        throw new IllegalArgumentException(variableField.getTag() + " ist kein Datenfeld-Tag");
      }
    }
    return dataFields;
  }

  /**
   * Liefert das erste Datenfeld, dessen Tag mit '1' beginnt, also
   * das Feld, das den bevorzugten Namen enthält.
   *
   * @param record 	nicht null
   * @return			Heading oder null
   */
  public static DataField getHeadingField(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> datafields = record.getDataFields();
    for (final DataField dataField : datafields) {
      final String tag = dataField.getTag();
      if (StringUtils.charAt(tag, 0) == '1')
        return dataField;
    }
    return null;
  }

  /**
   * Gibt die Felder, die Oberbegriffe enthalten, also 5XX-Felder.
   *
   * @param record	nicht null
   * @return			nicht null
   */
  public static Collection<DataField> getOBBFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> obList = new LinkedList<>();
    final List<DataField> datafields = record.getDataFields();
    for (final DataField dataField : datafields) {
      final String tag = dataField.getTag();
      if (StringUtils.charAt(tag, 0) == '5')
        obList.add(dataField);
    }
    return obList;
  }

  /**
   * Gibt die Quellen.
   *
   * @param record	nicht null
   * @return			670-Felder, nicht null
   */
  public static Collection<DataField> getSourceFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> sources = new LinkedList<>();
    final List<VariableField> sourcefields = record.getVariableFields("670");
    for (final VariableField variableField : sourcefields) {
      sources.add((DataField) variableField);
    }
    return sources;
  }

  /**
   * Gibt die Erläuterungen zum Datensatz.
   *
   * @param record	nicht null
   * @return			680-Felder, nicht null
   */
  public static Collection<DataField> getPublicGeneralNoteFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> notes = new LinkedList<>();
    final List<VariableField> notefields = record.getVariableFields("680");
    for (final VariableField variableField : notefields) {
      notes.add((DataField) variableField);
    }
    return notes;
  }

  /**
   *
   * Die id.
   *
   * @param record	nicht null
   * @return			id
   */
  public static String getContolNumber(final Record record) {
    Objects.requireNonNull(record);
    return record.getControlNumber();
  }

  /**
   *
   * Gibt das Feld fester Länge:<br>
   * Fourteen character positions (00-13) that contain positionally-defined
   * data elements that provide coded information about the record as a
   * whole or about data in field 153 (Classification Number). Each
   * character position must contain either a defined code or a fill
   * character ( | ).
   *
   * @param record    nicht null
   * @return          Feld 008
   */
  public static String getFixedLengthDataElements(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final ControlField fixedField = (ControlField) record.getVariableField("008");
    return fixedField.getData();
  }

  /**
   * @param record  nicht null
   * @return        Eine Zusammenfassung der Elemente in Feld 008
   */
  public static FixedLengthDataElement getElement008(final Record record) {
    final String fixed = getFixedLengthDataElements(record);
    final FixedLengthDataElement element = new FixedLengthDataElement(fixed);
    return element;
  }

  /**
   * Gibt die verlinkten Datensätze aus den Erläuterungen zum Datensatz, also
   * die $a-Unterfelder der 680- und 681-Felder.
   *
   * @param record	nicht null
   * @return			$a der 680- und 681-Felder, nicht null
   */
  public static Collection<String> getHeadingFromPublicGeneralNoteFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<String> links = new LinkedList<>();
    final List<DataField> notefields = getDataFields(record, "680", "681");
    for (final DataField dataField : notefields) {
      final List<Subfield> dollarAs = (dataField).getSubfields('a');
      for (final Subfield dollarA : dollarAs) {
        links.add(dollarA.getData());
      }
    }
    return links;
  }

  /**
   * Gibt die Verweisungsvermerke zum Datensatz.
   *
   * @param record	nicht null
   * @return			681-Felder, nicht null
   */
  public static Collection<DataField> getTracingNoteFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> notes = new LinkedList<>();
    final List<VariableField> notefields = record.getVariableFields("681");
    for (final VariableField variableField : notefields) {
      notes.add((DataField) variableField);
    }
    return notes;
  }

  /**
   * Gibt die Synonyme.
   *
   * @param record	nicht null
   * @return			Felder, deren Tag mit '4' beginnt, nicht null
   */
  public static Collection<DataField> getSynonymFields(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<DataField> obList = new LinkedList<>();
    final List<DataField> datafields = record.getDataFields();
    for (final DataField dataField : datafields) {
      final String tag = dataField.getTag();
      if (StringUtils.charAt(tag, 0) == '4')
        obList.add(dataField);
    }
    return obList;
  }

  /**
   *
   * @param record  auch null
   * @param tag     nicht null
   * @return        die Zahl der zu tag gehörigenFelder oder 0
   */
  public static int getFieldCount(final Record record, final String tag) {
    if (record == null)
      return 0;
    final List<VariableField> variableFields = record.getVariableFields(tag);
    if (variableFields == null)
      return 0;
    else
      return variableFields.size();
  }

  /**
   *
   * @param record        auch null
   * @param tag           nicht null
   * @param indicator     beliebig
   * @return              die Zahl der zu tag und indicator
   *                      gehörigenFelder oder 0
   */
  public static int getSubfieldCount(final Record record, final String tag, final char indicator) {
    if (record == null)
      return 0;
    final List<VariableField> variableFields = record.getVariableFields(tag);
    if (variableFields == null)
      return 0;
    int i = 0;

    for (final VariableField vf : variableFields) {
      if (!(vf instanceof DataField))
        continue;
      final DataField dataField = (DataField) vf;
      final List<Subfield> subs = dataField.getSubfields(indicator);
      if (subs == null)
        continue;
      i += subs.size();
    }
    return i;
  }

  public static void main(final String[] args) throws ParseException {

    final Record record = readXMLfromClip();
    System.out.println(record);

    System.out.println(normalize(record));
  }

  /**
   * @param record            nicht null
   * @return                  Datum oder null
   *
   */
  public static Date getDate(final Record record) {
    final ControlField field = (ControlField) record.getVariableField("005");
    if (field == null)
      return null;
    final String data = field.getData();
    final SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddkkmmss.S");
    Date date;
    try {
      date = format1.parse(data);
    } catch (final ParseException e) {
      return null;
    }
    return date;
  }

  /**
   * Entfernt alle Felder zu tags.
   *
   * @param record
   *            nicht null
   * @param tags
   *            auch null
   */
  public static void removeFields(final Record record, final String... tags) {
    if (tags == null)
      return;
    final List<VariableField> vfs = record.getVariableFields(tags);
    vfs.forEach(record::removeVariableField);
  }

  /**
   * Entfernt alle Felder der Liste.
   *
   * @param record
   *            auch null
   * @param variableFields
   *            auch null
   */
  public static
    void
    removeVariableFields(final Record record, final List<VariableField> variableFields) {
    if (record == null)
      return;
    if (variableFields == null)
      return;
    variableFields.forEach(record::removeVariableField);
  }

  /**
   * Entfernt alle Felder ab tag. Alle Felder beginnend mit tag werden
   * entfernt.
   *
   * @param record
   *            nicht null
   * @param tag
   *            auch null, dann keine Aktion
   */
  public static void removeFieldsBeginningWithTag(final Record record, final String tag) {
    if (tag == null)
      return;
    final List<VariableField> vfs = record.getVariableFields();
    for (final VariableField variableField : vfs) {
      final String currentTag = variableField.getTag();
      if (tag.compareTo(currentTag) <= 0)
        record.removeVariableField(variableField);
    }
  }

  /**
   * @param record  nicht null
   * @param tagPattern   nicht null
   * @return        Die variablen Felder, deren Tags dem Muster entsprechen
   */
  public static
    List<VariableField>
    getFieldsWithTagPattern(final Record record, final Pattern tagPattern) {

    final ArrayList<VariableField> variableFields = new ArrayList<>();
    final List<VariableField> vfs = record.getVariableFields();
    for (final VariableField variableField : vfs) {
      final String currentTag = variableField.getTag();
      final Matcher matcher = tagPattern.matcher(currentTag);
      if (matcher.matches())
        variableFields.add(variableField);
    }
    return variableFields;
  }

  /**
   * @param record  nicht null
   * @param tagPattern   nicht null
   * @return        Die variablen Felder, deren Tags dem Muster entsprechen
   */
  public static
    List<VariableField>
    getFieldsWithTagPattern(final Record record, final String tagPattern) {
    final Pattern pattern = Pattern.compile(tagPattern);
    final List<VariableField> fieldsWithTagPattern = getFieldsWithTagPattern(record, pattern);
    return fieldsWithTagPattern;
  }

  /**
   * Entfernt alle Felder hinter tag. Alle Felder frößer als tag werden
   * entfernt.
   *
   * @param record
   *            nicht null
   * @param tag
   *            auch null, dann keine Aktion
   */
  public static void removeFieldsGreaterThanTag(final Record record, final String tag) {
    if (tag == null)
      return;
    final List<VariableField> vfs = record.getVariableFields();
    for (final VariableField variableField : vfs) {
      final String currentTag = variableField.getTag();
      if (tag.compareTo(currentTag) < 0)
        record.removeVariableField(variableField);
    }
  }

  /**
   * Entfernt alle Felder hinter tag. Alle Felder frößer als tag werden
   * entfernt.
   *
   * @param record
   *            nicht null
   * @param tag1
   *            nicht null
   * @param tag2
   *            nicht null
   */
  public static
    List<VariableField>
    getFieldsBetween(final Record record, final String tag1, final String tag2) {
    Objects.requireNonNull(record);
    Objects.requireNonNull(tag1);
    Objects.requireNonNull(tag2);
    final Between<String> between = new Between<String>(tag1, tag2);
    final ArrayList<VariableField> variableFields = new ArrayList<>();
    final List<VariableField> vfs = record.getVariableFields();

    for (final VariableField variableField : vfs) {
      final String currentTag = variableField.getTag();
      if (between.test(currentTag))
        variableFields.add(variableField);
    }
    return variableFields;
  }

  /**
   * Behaält alle Felder zu tags bei.
   *
   * @param record
   *            nicht null
   * @param tags
   *            nicht null
   */
  public static void retainFields(final Record record, final String... tags) {
    final List<VariableField> allFields = record.getVariableFields();
    final List<VariableField> taggedFields = record.getVariableFields(tags);
    for (final VariableField variableField : allFields) {
      if (!taggedFields.contains(variableField))
        record.removeVariableField(variableField);
    }
  }

  /**
   *
   * Korrigiert Datensatz, wenn Felder geändert wurden. Insbesondere
   * muss die Länge im Leader neu berechnet werden.
   *
   * @param record    nicht null
   * @return          neuen Marc-Datensatz mit korrektem Leader und sortierten
   *                  Feldern
   *
   */
  public static Record normalize(final Record record) {
    Objects.requireNonNull(record);

    /*
     * Reihenfolge korrigieren
     */
    final List<VariableField> vfs = record.getVariableFields();
    Collections.sort(vfs, vfComparator);
    removeVariableFields(record, vfs);
    vfs.forEach(record::addVariableField);

    final StringOutputStream output = new StringOutputStream();
    final MarcWriter marcWriter = new MarcStreamWriter(output, "UTF-8");
    marcWriter.write(record);

    final byte[] inputData = output.getContent();
    final InputStream input = new ByteArrayInputStream(inputData);
    final MarcReader marcReader = new MarcStreamReader(input, "UTF-8");

    final Record record2 = marcReader.next();
    return record2;
  }

  /**
   *
   * @param record    nicht null
   * @param tag       nicht null
   * @param code      nicht 0
   * @return          den Inhalt des ersten Tags und des ersten Unterfeldes
   *                  oder null
   */
  public static String getContent(final Record record, final String tag, final char code) {
    final DataField field = (DataField) record.getVariableField(tag);
    if (field == null)
      return null;
    final Subfield sub = field.getSubfield(code);
    String content = null;
    if (sub != null)
      content = sub.getData();
    return content;
  }

  /**
   * Bessere Methode, da kein eigener Thread genutzt wird.
   * Dadurch werden Fehlermeldungen unterdrückt. Allerdings
   * sollte sicher sein, dass nur ein Record im inputStream
   * enthalten ist.
   *
   * @param inputStream
   *            auch null
   * @return neuen Datensatz oder null
   */
  public static Record readFrom(final InputStream inputStream) {
    if (inputStream == null)
      return null;

    Record record = null;
    try {
      record = null;

      final RecordStack queue = new RecordStack();
      final MarcXmlHandler handler = new MarcXmlHandler(queue);
      final MarcXmlParser parser = new MarcXmlParser(handler);

      final InputSource input = new InputSource(inputStream);
      parser.parse(input);
      record = queue.pop();
      StreamUtils.safeClose(inputStream);
    } catch (final Exception e) {
      // nix
    }
    return record;
  }

  /**
   * Anzuwenden, wenn sicher ist, dass der String
   * nur einen Record enthält.
   *
   * @param xml
   *            auch null
   * @return Marc-Record oder null
   */
  public static Record readFrom(final String xml) {
    if (xml == null)
      return null;
    final InputStream input = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
    final Record record = readFrom(input);
    return record;
  }

  /**
   *
   * @param dataField nicht null
   * @return          Inhalt von Unterfeld $a oder null
   */
  public static String getName(final DataField dataField) {
    final Subfield subfield = dataField.getSubfield('a');
    if (subfield == null)
      return null;
    else
      return subfield.getData();
  }

  /**
   *
   * @param record  nicht null
   * @return        Name im 1XX-Feld oder null
   */
  public static String getPreferredName(final Record record) {
    DataField headingField = getHeadingField(record);
    if (headingField == null) {
      headingField = (DataField) record.getVariableField("245");
    }
    if (headingField == null)
      return null;
    else
      return getName(headingField);
  }

  /**
   *
   * @param record  nicht null
   * @return        Inhalte der 5XX-Felder, nicht null
   */
  public static List<String> getOBB(final Record record) {
    final Collection<DataField> obbFields = getOBBFields(record);
    return FilterUtils.map(obbFields, MarcUtils::getName);
  }

  /**
   *
   * @param record  nicht null
   * @return        5XX-Felder vorhanden
   */
  public static boolean hasOB(final Record record) {
    return !getOBB(record).isEmpty();
  }

  /**
   * Formatiert Marc-Datensatz als HTML: '\n' wird zu break, Überschrift wird
   * hinzugefügt.
   *
   * @param record
   *            nicht null
   * @return html
   */
  public static String toHTML(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    String r = record.toString();
    r = Normalizer.normalize(r, Form.NFC);
    r = r.replaceAll("\u009c ", " @");
    r = r.replaceAll("˜", "");
    r = HTMLEntities.allCharacters(r);
    r = r.replaceAll("&#152;", "");
    r = r.replace("\n", "<br>");
    return "<h3>Marc-Datensatz:</h3>" + HTMLUtils.HTML_PARAGRAPH_OPEN + r
      + HTMLUtils.HTML_PARAGRAPH_CLOSE;
  }

  /**
   *
   * @param record  auch null
   * @return        ist Normdatensatz
   */
  public static boolean isAuthority(final Record record) {
    return RecordType.AUTHORITY == getRecordType(record);
  }

  /**
  *
  * @param record  auch null
  * @return        ist Klassifikation
  */
  public static boolean isClassification(final Record record) {
    return RecordType.CLASSIFICATION == getRecordType(record);
  }

  /**
  *
  * @param record  auch null
  * @return        ist Titeldatensatz
  */
  public static boolean isBibliographic(final Record record) {
    return RecordType.BIBLIOGRAPHIC == getRecordType(record);
  }

  /**
   *
   * @param record auch null
   * @return        auch null
   */
  public static RecordType getRecordType(final Record record) {
    return RecordType.getType(record);
  }

  public static String extractInfo(final Record record) {
    String info = "";

    final RecordType type = getRecordType(record);

    info += "Art des Datensatzes: " + type.german;

    final FixedLengthDataElement feld008 = getElement008(record);
    info += " -- Eingabedatum: " + feld008.getEingabeDatum();

    if (type.equals(RecordType.AUTHORITY)) {
      info += " -- Art des Normdatensatzes: " + feld008.getArtDesNormdatensatzes();
    } else if (type.equals(RecordType.BIBLIOGRAPHIC)) {
      info += " -- Art des Titeldatensatzes: " + MarcBibUtils.getArtDesTiteldatensatzes(record);
    }
    return info;
  }

}
