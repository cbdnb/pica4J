package de.dnb.gnd.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.function.Predicate;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.filtering.RejectEmptyStrings;
import de.dnb.gnd.exceptions.ExceptionHandler;
import de.dnb.gnd.exceptions.IgnoringHandler;
import de.dnb.gnd.exceptions.WrappingHandler;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.TagDB;

/**
 * Diese Klasse kapselt einen eigenen {@link RecordReader}. Die
 * Defaulteinstellungen sind die der {@link RecordReader}-Klasse, können
 * aber nach Bedarf verändert werden (wird durchgereicht).
 *
 * <br>In der Unterklasse muss mindestens
 * {@link DownloadWorker#processRecord(Record)} überschrieben werden.
 *
 *
 * @author Christian_2
 *
 */
public abstract class DownloadWorker {

  /**
   * Ordner, in dem nach zu verarbeitenden Files gesucht wird.
   */
  private File inputFolder = null;

  /**
   * Zu verarbeitende Files fangen mir diesem Präfix an.
   */
  private String prefix = "";

  /**
   * Wrapping sollte die Regel sein, um Fehler im Download rasch zu erkennen.
   */
  private ExceptionHandler handler = new WrappingHandler();

  /**
   * Der eigene {@link RecordReader}. Ist gekapselt und wird nicht
   * mit anderen Anwendungen geteilt.
   */
  protected RecordReader reader = new RecordReader();

  /**
   * {@link GNDTagDB} ist default.
   */
  protected TagDB defaultTagDB = GNDTagDB.getDB();

  /**
   * ob Default-DB benutzt wird.
   */
  protected boolean useDefaultDB = false;

  /**
   * UTF-8 ist default.
   */
  private String charset = "UTF-8";

  private boolean ignoreMARC = false;

  private String lineDelimiter = "(\r)?\n";

  private String recordDelimiter = "SET: ";

  private Predicate<String> streamFilter = new RejectEmptyStrings();

  /**
   * Standardverhalten.
   */
  protected PrintStream outputStream = System.out;

  /**
   * Ordner, in dem nach zu verarbeitenden Files gesucht wird. Ist zu Anfang
   * null, um dumme Fehler zu vermeiden.
   *
   * @param aFolder	nicht null.
   */
  public final void setInputFolder(final File aFolder) {
    RangeCheckUtils.assertReferenceParamNotNull("aFolder", aFolder);
    inputFolder = aFolder;
  }

  /**
   * Ordner, in dem nach zu verarbeitenden Files gesucht wird. Ist zu Anfang
   * null, um dumme Fehler zu vermeiden.
   *
   * @param aFolder	nicht null.
   */
  public final void setInputFolder(final String aFolder) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("aFolder", aFolder);
    inputFolder = new File(aFolder);
  }

  /**
   * Womit fangen die bearbeiteten Dateien an?
   * @param aFilePrefix	nicht null, nicht leer.
   */
  public final void setFilePrefix(final String aFilePrefix) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("filePrefix", aFilePrefix);
    prefix = aFilePrefix;
  }

  /**
   * Wird an Parser übergeben.  Default ist {@link WrappingHandler}, denn
   * Wrapping sollte die Regel sein, um Fehler im Download rasch zu erkennen.
   * Kann auch während der Verarbeitung noch geändert werden.
   *
   * @param aHandler	nicht null
   */
  public final void setHandler(final ExceptionHandler aHandler) {
    RangeCheckUtils.assertReferenceParamNotNull("aHandler", aHandler);
    handler = aHandler;
    reader.setHandler(aHandler);
  }

  public void setRecordDelimiter(final String recordDelimiter) {
    RangeCheckUtils.assertReferenceParamNotNull("recordDelimiter", recordDelimiter);
    this.recordDelimiter = recordDelimiter;
    reader.setRecordDelimiter(recordDelimiter);
  }

  /**
   * Ändert den Zeilentrenner. Wird an den Reader durchgereicht.
   * Kann auch während der Verarbeitung noch geändert werden.
   *
   * @param lineDelimiter the line delimiter to set. Not null, not empty.
   */
  public final void setLineDelimiter(final String lineDelimiter) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("lineDelimiter", lineDelimiter);
    this.lineDelimiter = lineDelimiter;
    reader.setLineDelimiter(lineDelimiter);
  }

  /**
   * Überlese beim Parsen von Pica+-Daten die Felder, die in
   * MARC21 üblich sind, die aber nicht zum Pica-Format gehören.
   * Kann auch während der Verarbeitung noch geändert werden.
   *
   * @param ignoreMARC	beliebig, wenn true, werden MARC-Felder
   * 						überlesen. false ist die Standardeinstellung
   * 						für IBW-Downloads.
   */
  public void setIgnoreMARC(final boolean ignoreMARC) {
    this.ignoreMARC = ignoreMARC;
    reader.setIgnoreMARC(ignoreMARC);
  }

  /**
   * {@link GNDTagDB} ist default. Wird an den reader übergeben.
   * Kann auch während der Verarbeitung noch geändert werden.
   *
   * @param db nicht null.
   */
  public final void setDefaultTagDB(final TagDB db) {
    RangeCheckUtils.assertReferenceParamNotNull("db", db);
    defaultTagDB = db;
    reader.setDefaultTagDB(db);
  }

  /**
   * {@link GNDTagDB} ist default. Wird an den reader übergeben.
   * Kann auch während der Verarbeitung noch geändert werden.
   *
   * @param db nicht null.
   */
  public final void useDefaultTagDB(final boolean useIt) {
    useDefaultDB = useIt;
    reader.useDefaultDB(useIt);
  }

  /**
   * UTF-8 ist default.
   * Muss vor der Verarbeitung gesetzt werden.
   *
   * @param aCharset nicht null.
   */
  public final void setCharset(final String aCharset) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("aCharset", aCharset);
    charset = aCharset;
  }

  /**
   * Filter, der VOR dem Parsen entscheidet, ob der Datensatz überhaupt
   * benötigt wird. Für große Datenmengen sinnvoll, da das Parsen
   * des Datensatzes etwa 95% der Zeit benötigt. Bei einem Filter, der
   * auf Enthaltensein eines Teilstrings überprüft, tritt nach
   * Erfahrung eine Beschleunigung um den Faktor 4 auf.
   *
   * Standardmäßig werden alle Datensätze aktzeptiert.
   *
   * @param streamFilter	Prädikat, das Strings akzeptiert. Es wird
   *                      empfohlen. {@link ContainsTag} zu verwenden.
   */
  public final void setStreamFilter(final Predicate<String> streamFilter) {
    this.streamFilter = streamFilter;
    reader.setStreamFilter(streamFilter);
  }

  /**
   * Wenn mehrere Files bearbeitet werden sollen. Diese liegen im
   * selben Ordner und fangen alle mit demselben Präfix an.
   *
   * Sollte eines der Files gzipped sein, so wird auch das bearbeitet.
   * Allerdings werden dann keine gzip-spezifischen Einstellungen
   * vorgenommen (anderer Record Separator ...). Diese Einstellungen
   * müssen vorher global vorgenommen werden.
   *
   * @throws IOException	übliche Fehler beim Dateizugriff.
   */
  public final void processAllFiles() throws IOException {
    final File[] files = inputFolder.listFiles();
    for (final File file : files) {
      if (file.getName().startsWith(prefix)) {
        System.err.println(file);
        processFile(file);
      }
    }
  }

  /**
   * Delegiert an processFile(InputStream inputStream).
   * Verwendet den vorher gesetzten {@link ExceptionHandler}
   * und die vorher festgelegte {@link TagDB}.
   * <br>
   * Sollte das File gzipped sein, so wird auch das bearbeitet.
   * Allerdings werden dann keine gzip-spezifischen Einstellungen
   * vorgenommen (anderer Record Separator ...). Diese Einstellungen
   * müssen vorher global vorgenommen werden.
   *
   * @param file			nicht null.
   * @throws IOException	wenn Probleme mit file auftreten.
   */
  public final void processFile(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    System.err.println("Bearbeite Datei: " + file.getName());
    processInputStream(FileUtils.getMatchingInputStream(file));
  }

  /**
   * Delegiert an processFile(InputStream inputStream).
   * Verwendet den vorher gesetzten {@link ExceptionHandler}
   * und die vorher festgelegte {@link TagDB}.
   * <br>
   * Sollte das File gzipped sein, so wird auch das bearbeitet.
   * Allerdings werden dann keine gzip-spezifischen Einstellungen
   * vorgenommen (anderer Record Separator ...). Diese Einstellungen
   * müssen vorher global vorgenommen werden.
   *
   * @param files         auch leer, keines davon null
   * @throws IOException  wenn Probleme mit file auftreten
   */
  public final void processFiles(final String... files) throws IOException {
    for (final String file : files) {
      processFile(file);
    }
  }

  /**
   * Delegiert an processFile(InputStream inputStream).
   * Verwendet den vorher gesetzten {@link ExceptionHandler}
   * und die vorher festgelegte {@link TagDB}.
   * <br>
   * Sollte das File gzipped sein, so wird auch das bearbeitet.
   * Allerdings werden dann keine gzip-spezifischen Einstellungen
   * vorgenommen (anderer Record Separator ...). Diese Einstellungen
   * müssen vorher global vorgenommen werden.
   *
   * @param filename		nicht null.
   * @throws IOException	wenn Probleme mit file auftreten.
   */
  public final void processFile(final String filename) throws IOException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("filename", filename);
    final File file = new File(filename);
    processFile(file);
  }

  /**
   * Zentrale Prozedur. Verwendet die vorher gesetzten
   * Parameter.
   *
   * @param inputStream	nicht null.
   * @throws IOException	wenn Probleme mit file auftreten.
   */
  public final void processInputStream(final InputStream inputStream) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("inputStream", inputStream);

    reader.setSource(inputStream, charset);
    reader.setHandler(handler);
    reader.setDefaultTagDB(defaultTagDB);
    reader.setIgnoreMARC(ignoreMARC);
    reader.setLineDelimiter(lineDelimiter);
    reader.setRecordDelimiter(recordDelimiter);
    reader.setStreamFilter(streamFilter);
    reader.useDefaultDB(useDefaultDB);

    while (reader.hasNext()) {
      final Record record = reader.next();
      try {
        processRecord(record);
      } catch (final Exception e) {
        handler.handle(e, "Bei der Verabeitung von Datensatz mit idn " + record.getId()
          + "ist ein Fehler aufgetreten");
      }

    }
  }

  /**
   * Leitet die Ausgabe in eine Datei um. Die Datei wird überschrieben,
   * wenn schon vorhanden.
   *
   * @param filename					nicht null nicht leer
   * @throws FileNotFoundException
   */
  public final void setOutputFile(final String filename) throws FileNotFoundException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("filename", filename);
    outputStream = new PrintStream(filename);
  }

  /**
   *
   * Schreibt einen String. Alle anderen Datentypen müssen in Strings
   * umgewandelt werden, da print nicht überschrieben ist.
   *
   * @param s	beliebig
   */
  public final void print(final String s) {
    outputStream.print(s);
  }

  /**
   * Schreibt eine Collection zeilenweise, eingeleitet mit Tabulator.
   * @param it  nicht null
   */
  public final void printIterable(final Iterable<?> it) {
    it.forEach(el -> outputStream.println("\t" + el));
  }

  /**
   *
   * Schreibt einen String in die nächste Tabellenspalte.
   * Alle anderen Datentypen müssen in Strings
   * umgewandelt werden.
   *
   * @param s	beliebig
   */
  public final void tab(final String s) {
    outputStream.print("\t" + s);
  }

  /**
   *
   * Rückt in die in die nächste Tabellenspalte vor.
   *
   */
  public final void tab() {
    outputStream.print("\t");
  }

  /**
   *
   * Schreibt einen Zeilenumbruch.
   *
   * @param s	beliebig
   */
  public final void println() {
    outputStream.println();
  }

  /**
   *
   * Schreibt einen String und macht dann einen Zeilenumbruch.
   * Alle anderen Datentypen müssen in Strings
   * umgewandelt werden, da println nicht überschrieben ist.
   *
   * @param s	beliebig
   */
  public final void println(final String s) {
    outputStream.println(s);
  }

  /**
   * Vereinfacht den Aufruf, da alle für gzip-Files üblichen globalen
   * Einstellungen vorgenommen werden (s. {@link #gzipSettings()}.
   * Soll eine individuelle Einstellung vorgenommen werden, so muss
   * {@link #processFile(String)} verwendet werden.
   *
   * @param filename		nicht null
   * @throws IOException	wenn file nicht exisitert...
   */
  public final void processGZipFile(final String filename) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("filename", filename);
    gzipSettings();
    processInputStream(FileUtils.getGZipInputStream(filename));
  }

  /**
   * Vereinfacht den Aufruf, da alle für gzip-Files üblichen globalen
   * Einstellungen vorgenommen werden (s. {@link #gzipSettings()}.
   * Soll eine individuelle Einstellung vorgenommen werden, so muss
   * {@link #processFile(File)} verwendet werden.
   *
   * @param file		nicht null
   * @throws IOException	wenn file nicht exisitert...
   */
  public final void processGZipFile(final File file) throws IOException {
    gzipSettings();
    processInputStream(FileUtils.getGZipInputStream(file));
  }

  /**
   * Vereinfacht den Aufruf, da alle für gzip-Files üblichen globalen
   * Einstellungen vorgenommen werden (s. {@link #gzipSettings()}.
   * Soll eine individuelle Einstellung vorgenommen werden, so muss
   * {@link #processFile(File)} verwendet werden.
   *
   * @param files         auch leer, keines null
   * @throws IOException  wenn ein file nicht exisitert...
   */
  public final void processGZipFiles(final String... files) throws IOException {
    for (final String file : files) {
      processGZipFile(file);
    }
  }

  /**
   * Empfohlene Einstellungen von GZip-Files.
   *
   *  <br>
   * <br> RecordDelimiter: \n
   * <br>	LineDelimiter: RS
   * <br>	IgnoreMARC: true
   * <br>	Handler: IgnoringHandler
   * <br>	DefaultTagDB: BibTagDB
   */
  public final void gzipSettings() {
    setRecordDelimiter("\n");
    setLineDelimiter(Constants.RS);
    setIgnoreMARC(true);
    setHandler(new IgnoringHandler()); // bei so großen Files nötig
    setDefaultTagDB(BibTagDB.getDB());
  }

  /**
   * Empfohlene Standard-Einstellungen.
   *  <br>
   * <br> RecordDelimiter: "SET: "
   * <br>	LineDelimiter: "(\r)?\n"
   * <br>	IgnoreMARC: false
   * <br>	Handler: WrappingHandler
   * <br>	DefaultTagDB: GNDTagDB
   */
  public final void defaultSettings() {
    setRecordDelimiter("SET: ");
    setLineDelimiter("(\r)?\n");
    setIgnoreMARC(false);
    setHandler(new WrappingHandler());
    setDefaultTagDB(GNDTagDB.getDB());
  }

  /**
   * Empfohlene Einstellungen für Datashop-Downloads.
   *  <br>
   * <br> RecordDelimiter: GS
   * <br>	LineDelimiter: RS
   * <br>	IgnoreMARC: true
   * <br>	Handler: WrappingHandler
   * <br>	DefaultTagDB: GNDTagDB
   */
  public final void datashopSettings() {
    setRecordDelimiter(Constants.GS);
    setLineDelimiter(Constants.RS);
    setIgnoreMARC(true);
    setHandler(new WrappingHandler());
    setDefaultTagDB(GNDTagDB.getDB());
  }

  @Override
  protected void finalize() throws Throwable {
    FileUtils.safeClose(outputStream);
    super.finalize();
  }

  /**
   *
   * @param record	Der zu bearbeitende Datensatz, nicht null.
   */
  protected abstract void processRecord(Record record);

  /*
   * Beispielanwendung unter Einsatz von einem (oder auch mehreren)
   * Downloadbearbeitern.
   */
  public static void main(final String[] args) throws IOException {

    final DownloadWorker db = new DownloadWorker() {
      @Override
      public void processRecord(final Record ds) {
        System.out.println(ds.getId());
      }
    };

    db.gzipSettings();
    db.processFile("D:/Normdaten/DNBGND_g.dat.gz");

  }

}
