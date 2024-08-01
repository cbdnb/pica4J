package de.dnb.gnd.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Quadruplett;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.ExceptionHandler;
import de.dnb.gnd.exceptions.IgnoringHandler;
import de.dnb.gnd.exceptions.WrappingHandler;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.SWDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.IDNUtils;
import de.dnb.gnd.utils.RecordUtils;

/**
 *
 * @author baumann
 *
 *	Nimmt an, dass ein {@link BufferedReader} Datensätze  enthält.
 *
 *	Der Datensatz besteht aus Feldern, diese wiederum aus Zeilen bestehen.
 *
 *
 */
public final class RecordParser {

  // --- Default-Werte: ----

  /**
   *
   * @param tagDB             db
   * @param aHandler          Handler
   * @param aLineDelimiter    Zeilentrenner
   */
  public RecordParser(
    final TagDB tagDB,
    final ExceptionHandler aHandler,
    final String aLineDelimiter) {
    handler = aHandler;
    defaultTagDB = tagDB;
    setLineDelimiter(aLineDelimiter);
  }

  /**
   * Mit GND-DB, Ignorierendem Handler und (CR)?LF.
   */
  public RecordParser() {
    this(GNDTagDB.getDB(), new IgnoringHandler(), "(\r)?\n");
  }

  public static final TagDB BIB_TAG_DB = BibTagDB.getDB();

  public static final TagDB AUTH_TAG_DB = GNDTagDB.getDB();

  public static final TagDB SWD_TAG_DB = SWDTagDB.getDB();

  private ExceptionHandler handler;

  private Record record;

  /**
   * Zum Parsen verwendet
   */
  private TagDB theTagDB;

  private TagDB defaultTagDB;

  /**
   * UNIX: LF
   * Windows: CR LF.
   */
  private String lineDelimiter = "(\r)?\n";

  /**
   * wenn true, werden MARC-Felder überlesen. false ist die
   * Standardeinstellung für IBW-Downloads.
   */
  private boolean ignoreMARC = false;

  private Pattern delimiterPattern;

  private boolean useDefaultDB = false;

  /**
   * Erzwinge Nutzung der Datenbank, die durch
   * {@link #setDefaultTagDB(TagDB)} gesetzt
   * wurde.
   *
   * @param useIt wenn true, wird die Default-
   *              DB genutzt
   */
  public void useDefaultDB(final boolean useIt) {
    useDefaultDB = useIt;
  }

  /**
   * Ändert die Default-Tag-Datenbank (Wird verwendet, wenn der Satztyp
   * nicht sicher erkannt werden kann).
   *
   * @param aTagDB the tag database to set, not null.
   */
  public void setDefaultTagDB(final TagDB aTagDB) {
    RangeCheckUtils.assertReferenceParamNotNull("aTagDB", aTagDB);
    defaultTagDB = aTagDB;
  }

  /**
   * Regulärer Ausdruck als String. Ändert den Zeilentrenner.
   *
   * @param aLineDelimiter the lineDelimiter to set. Not null, not empty.
   */
  public void setLineDelimiter(final String aLineDelimiter) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("alineDelimiter", aLineDelimiter);
    lineDelimiter = aLineDelimiter;
    delimiterPattern = Pattern.compile(lineDelimiter);
  }

  public boolean isIgnoreMARC() {
    return ignoreMARC;
  }

  /**
   * Überlese beim Parsen von Pica+-Daten die Felder, die in
   * MARC21 üblich sind, die aber nicht zum Pica-Format gehören.
   *
   * @param ignoreMARC	beliebig, wenn true, werden MARC-Felder
   * 						überlesen. false ist die Standardeinstellung
   * 						für IBW-Downloads.
   */
  public void setIgnoreMARC(final boolean ignoreMARC) {
    this.ignoreMARC = ignoreMARC;
  }

  /**
   * Grundlegende Methode.
   *
   * Geht nicht davon aus, dass eine IDN in lines enthalten ist.
   * Die idn ist schon anderweitig ermittelt worden
   *
   *
   * @param idn	idn oder null.
   * @param lines nicht null
   * @return		Gültigen Datensatz
   *
   *
   */
  public Record parse(final String idn, final Iterable<String> lines) {

    RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
    // Initialisieren der Variablen
    record = new Record(idn, theTagDB);

    // reader Zeile für Zeile lesen und analysieren:
    for (final String lineStr : lines) {
      if (lineStr != null) {
        if (lineStr.length() != 0) {
          processLine(lineStr, idn);
        }
        record.rawData += lineStr + Constants.LINE_SEPARATOR;
      }
    }
    return record;
  }

  /**
   * Parst einen String auf darin enthaltenen Datensatz. Die
   * idn wird nach dem üblichen Muster extrahiert. Danach erfolgt
   * eine Zerlegung in Zeilen nach dem festgelegten lineDelimiter.
   * Dieser ist per Default \r\n, kann aber durch
   * {@link #setLineDelimiter(String)} geändert werden.
   *
   * @param aRecord	nicht null
   * @return			nicht null
   */
  public Record parse(String aRecord) {
    RangeCheckUtils.assertReferenceParamNotNull("aRecord", aRecord);
    /*
     * notwendig, um auch bei gzip-Dateien ein korrektes
     * Parsen zu gewährleisten. RecordReader übergibt
     * einen Zeilenumbruch am Anfang. Zeilentrenner (besser:
     * Feldtrenner) ist aber Constants.RS
     */
    aRecord = aRecord.trim();
    final String idn = IDNUtils.extractPPNfromFirstLine(aRecord);
    if (useDefaultDB && defaultTagDB != null) {
      theTagDB = defaultTagDB;
    } else {
      if (RecordUtils.containsRecordType(aRecord)) {
        if (RecordUtils.isSWD(aRecord))
          theTagDB = SWD_TAG_DB;
        else if (RecordUtils.isAuthority(aRecord))
          theTagDB = AUTH_TAG_DB;
        else
          theTagDB = BIB_TAG_DB;
      } else
        theTagDB = defaultTagDB;
    }

    final String[] lineArr = delimiterPattern.split(aRecord);
    final List<String> lines = Arrays.asList(lineArr);

    return parse(idn, lines);
  }

  // ----- Hilfsfunktionen: -----------------------------

  /**
   * Verarbeitet line und fügt diese im Erfolgsfalle record hinzu.
   * Eventuelle Fehler werden von handler verarbeitet.
   *
   * @param lineStr	Eine nicht leere Zeile, eventuell mit einem Tag
   * 					beginnend.
   * @param idn		idn zur Fehleranalyse
   *
   */
  private void processLine(final String lineStr, final String idn) {

    RangeCheckUtils.assertReferenceParamNotNull("lineStr", lineStr);

    final boolean controlFound = controlFieldsFoundAndProcessed(lineStr);
    if (controlFound) { // dann Zeile schon verarbeitet
      return;
    }

    // also noch zu verarbeiten, Versuch:
    Line line = null;
    try {
      line = LineParser.parse(lineStr, theTagDB, ignoreMARC);
    } catch (final Exception e) {
      handler.handle(e,
        "Datensatz mit idn " + idn + ": Zeile '" + lineStr + "' nicht richtig gebildet");
    }
    if (line != null) { // Neuer tag
      try {
        record.add(line);
      } catch (final OperationNotSupportedException e) {
        handler.handle(e, "Datensatz mit idn " + idn + ": Addition der Zeile nicht erlaubt");
      }

    }
  }

  /**
   * Verarbeitet die 3 Kontrollfelder
   * 001A	= 001 (GND) = 0200 (Titel)
   * 001B = 002		= 0210
   * 001D = 003		= 0230
   * und gibt zurück, ob das möglich war.
   *
   * @param lineStr	nicht null, nicht leer.
   * @return			Zeile mit Kontrollfeldern gefunden(immer 2. Zeile bei
   * 					download-Daten).
   */
  private boolean controlFieldsFoundAndProcessed(final String lineStr) {

    RangeCheckUtils.assertReferenceParamNotNull("lineStr", lineStr);
    //@formatter:off
		final Quadruplett<String, String, String, String>
			controlFields = StringUtils.getControlFields(lineStr);
		//@formatter:on
    if (controlFields != null) {

      try {
        Tag tagC = theTagDB.findTag("001A");
        String lineString = controlFields.first;
        Line line = LineParser.parse(tagC, Format.PICA3, lineString, ignoreMARC);
        record.add(line);

        tagC = theTagDB.findTag("001B");
        // millis werden automatisch erzeugt:
        lineString = controlFields.second + "$t" + controlFields.third + ".000";
        line = LineParser.parse(tagC, Format.PICA3, lineString, ignoreMARC);
        record.add(line);

        tagC = theTagDB.findTag("001D");
        lineString = controlFields.forth;
        line = LineParser.parse(tagC, Format.PICA3, lineString, ignoreMARC);
        record.add(line);
      } catch (final Exception e) {
        // OperationNotSupported von record.add()
        // IllFormatedLine       von createLine()
        handler.handle(e, "Fehler in Control Fields");
      }

      return true;
    } else {

      return false;
    }
  }

  /**
   * Setzt den Exceptionhandler, der Aktionen durchführt, wenn
   * Fehler beim Parsen auftreten. Der Default ist IgnoringHandler.
   *
   * @param aHandler   Wenn handler == null, wird nichts geändert.
   *
   */
  public void setHandler(final ExceptionHandler aHandler) {
    if (aHandler != null)
      handler = aHandler;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {

    final RecordParser parser = new RecordParser();
    parser.setHandler(new WrappingHandler());
    parser.setIgnoreMARC(false);
    parser.setDefaultTagDB(BIB_TAG_DB);
    final String s = StringUtils.readClipboard();
    final Record record = parser.parse(s);
    System.out.println(record);
  }

}
