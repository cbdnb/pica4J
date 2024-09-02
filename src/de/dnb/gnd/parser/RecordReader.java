package de.dnb.gnd.parser;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.applicationComponents.Streams;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.filtering.RejectEmptyStrings;
import de.dnb.gnd.exceptions.ExceptionHandler;
import de.dnb.gnd.exceptions.IgnoringHandler;
import de.dnb.gnd.exceptions.WrappingHandler;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.ContainsTag;
import de.dnb.gnd.utils.RecordUtils;

/**
 * Zum Einlesen einer Reihe von Datensätzen aus einem Strom.
 * Die Klasse hat reguläre Ausdrücke, um den Beginn eines
 * Datensatzes zu erkennen.
 * <br><br>
 * Als Handler wird der ignorierende, als Datenbank die GND,
 * als Datensatztrenner "SET: " angesehen.
 * <br><br>
 * Die Verwendung als {@link Iterable} ist mit Vorsicht zu verwenden:
 * RecordReader ist nach dem ersten Versuch verbraucht und muss danach neu
 * erzeugt werden!
 *
 * @author baumann
 *
 */
public class RecordReader implements Iterable<Record>, Iterator<Record>, Closeable {

  private String recordDelimiter;

  private Pattern recordDelimiterPat;

  private Scanner scanner;

  private String nextChunk = null;

  private final RecordParser parser = new RecordParser();

  public final TagDB BIB_TAG_DB = BibTagDB.getDB();

  public final TagDB AUTH_TAG_DB = GNDTagDB.getDB();

  private long bytesRead = 0;

  private boolean logBytes = false;

  /**
   *
   * @return  die bisher gelesenen Bytes der Datei, inklusive der verworfenen
   *          Datensätze, um den Fortschritt besser abschätzen zu können
   */
  public long getBytesRead() {
    return bytesRead;
  }

  /**
   * Die gelesenen Bytes werden mitgeschrieben, um den
   * Fortschritt erfasse zu können. Das verlangsamt
   * vermutlich den Lesevorgang.
   *
   * @param log wird mitgelesen?
   */
  public void setByteLogging(final boolean log) {
    logBytes = log;
  }

  /**
   *  Für große Datenmengen können die Records schon einmal vorgefiltert
   *  werden. Standard ist: keine leeren Strings.
   */

  private Predicate<String> streamFilter = new RejectEmptyStrings();

  // Exemplar-Initializer:
  {
    recordDelimiter = "SET: ";
    setDefaultTagDB(GNDTagDB.getDB());
  }

  /**Constructs a new RecordReader that produces values scanned from the
   * specified file.
   *
   * @param source	nicht null, nicht leer
   * @throws FileNotFoundException
   */
  public RecordReader(final File source) throws FileNotFoundException {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    scanner = new Scanner(source);
    createRecordDelimPat(recordDelimiter);
  }

  /**
   * Constructs a new RecordReader that produces values scanned from the
   * specified file.
   * @param source		nicht null
   * @param charsetName	nicht null, nicht leer
   * @throws FileNotFoundException
   * 						wenn File nicht da ...
   */
  public RecordReader(final File source, final String charsetName) throws FileNotFoundException {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("charsetName", charsetName);
    scanner = new Scanner(source, charsetName);
    createRecordDelimPat(recordDelimiter);
  }

  /**
   * Constructs a new RecordReader that produces values scanned from
   * {@link System#in}
   *
   *
   */
  public RecordReader() {
    scanner = new Scanner(System.in);
    createRecordDelimPat(recordDelimiter);
  }

  public static RecordReader getConsoleReader() {
    return new RecordReader(System.in);
  }

  /**
   * Constructs a new RecordReader that produces values scanned from the
   * specified input stream.
   *
   * @param source	nicht null
   */
  public RecordReader(final InputStream source) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    scanner = new Scanner(source);
    createRecordDelimPat(recordDelimiter);
  }

  /**
   * Constructs a new RecordReader that produces values scanned from the
   * specified input stream.
   * @param source		nicht null
   * @param charsetName	nicht null, nicht leer
   */
  public RecordReader(final InputStream source, final String charsetName) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("charsetName", charsetName);
    scanner = new Scanner(source, charsetName);
    createRecordDelimPat(recordDelimiter);
  }

  /**
   * Constructs a new RecordReader that produces values scanned from the
   * specified source.
   * @param source	nicht null
   */
  public RecordReader(final Readable source) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    scanner = new Scanner(source);
    createRecordDelimPat(recordDelimiter);
  }

  /**
   * Constructs a new RecordReader that produces values scanned from the
   * specified file.
   * @param sourceFile	nicht null
   * @throws FileNotFoundException wenn sourcefile nicht existiert
   */
  public RecordReader(final String sourceFile) throws FileNotFoundException {
    RangeCheckUtils.assertReferenceParamNotNull("source", sourceFile);
    final File file = new File(sourceFile);
    scanner = new Scanner(file);
    createRecordDelimPat(recordDelimiter);
  }

  /**
   * Konstruiert einen Reader, der source ausliest.
   *
   * @param source	nicht null
   * @return			neuen RecordReader
   */
  public static RecordReader newReader(final String source) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    final StringReader reader = new StringReader(source);
    return new RecordReader(reader);
  }

  /**
   * Changes the recordDelimiter to set.
   *
   * @param aRecordDelimiter may be null.
   */
  public final void setRecordDelimiter(final String aRecordDelimiter) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("aRecordDelimiter", aRecordDelimiter);
    createRecordDelimPat(aRecordDelimiter);
    recordDelimiter = aRecordDelimiter;
  }

  /**
   * Erzeugt die Patterns recordDelPat, scannerDelimiterPat
   * für Record,  und Chunk .
   *
   * @param aRecordDelimiter	nicht null.
   */
  private void createRecordDelimPat(final String aRecordDelimiter) {
    RangeCheckUtils.assertReferenceParamNotNull("aRecordDelimiter", aRecordDelimiter);
    /*
     * (?=foo) ist lookahead:
     * Die RegEx-Maschine vergewissert sich, dass ab der
     * aktuellen Stelle "foo" steht, rückt aber nicht weiter.
     *
     * Das bedeutet in unserem Fall, dass auch recordDelimiterPat
     * als zum Datensatz gehörig angesehen wird.
     */
    recordDelimiterPat = Pattern.compile("(?=(" + aRecordDelimiter + "))");
    scanner.useDelimiter(recordDelimiterPat);
  }

  /**
   * Ändert den Zeilentrenner. Regulärer Ausdruck als String.
   * Wird an den Parser durchgereicht.
   *
   * @param lineDelimiter the line delimiter to set. Not null, not empty.
   */
  public final void setLineDelimiter(final String lineDelimiter) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("lineDelimiter", lineDelimiter);
    parser.setLineDelimiter(lineDelimiter);
  }

  /**
   * Ändert die Default-Tag-Datenbank (Wird verwendet, wenn der Satztyp
   * nicht sicher erkannt werden kann). Wird an den Parser durchgereicht.
   *
   * @param tagDB nicht null
   */
  public final void setDefaultTagDB(final TagDB tagDB) {
    RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);
    parser.setDefaultTagDB(tagDB);
  }

  /**
   * Erzwinge Nutzung der Datenbank, die durch
   * {@link #setDefaultTagDB(TagDB)} gesetzt
   * wurde.
   *
   * @param useIt wenn true, wird die Default-
   *              DB genutzt
   */
  public void useDefaultDB(final boolean useIt) {
    parser.useDefaultDB(useIt);
  }

  /**
   * Ändert den Exeption Handler. Wird an den Parser durchgereicht.
   * @param handler nicht null
   */
  public final void setHandler(final ExceptionHandler handler) {
    RangeCheckUtils.assertReferenceParamNotNull("handler", handler);
    parser.setHandler(handler);
  }

  /**
   * Überlese beim Parsen von Pica+-Daten die Felder, die in
   * MARC21 üblich sind, die aber nicht zum Pica-Format gehören. Wird
   * an den Parser durchgereicht.
   *
   * @param ignoreMARC	beliebig, wenn true, werden MARC-Felder
   * 						überlesen. false ist die Standardeinstellung
   * 						für IBW-Downloads.
   */
  public void setIgnoreMARC(final boolean ignoreMARC) {
    parser.setIgnoreMARC(ignoreMARC);
  }

  /**
   *
   * @param stream
   */
  public void setSource(final InputStream stream) {
    scanner = new Scanner(stream);
    scanner.useDelimiter(recordDelimiterPat);
  }

  public void setSource(final InputStream inputStream, final String charset) {
    scanner = new Scanner(inputStream, charset);
    scanner.useDelimiter(recordDelimiterPat);

  }

  /**
   *
   * @param stream
   * @throws FileNotFoundException
   */
  public void setSource(final String filename) throws FileNotFoundException {
    setSource(new File(filename));
  }

  /**
   *
   * @param file
   * @throws FileNotFoundException
   */
  public void setSource(final File file) throws FileNotFoundException {
    scanner = new Scanner(file);
    scanner.useDelimiter(recordDelimiterPat);
  }

  /**
   * Empfohlene Einstellungen von GZip-Files:
   *
   *  <br>
   * <br> RecordDelimiter: \n
   * <br>	LineDelimiter: RS
   * <br>	IgnoreMARC: true
   * <br>	Handler: IgnoringHandler
   * <br>	DefaultTagDB: BibTagDB
   * <br> DefaultTagDB wird nicht erzwungen
   */
  public final void gzipSettings() {
    setRecordDelimiter("\n");
    setLineDelimiter(Constants.RS);
    setIgnoreMARC(true);
    setHandler(new IgnoringHandler()); // bei so großen Files nötig
    setDefaultTagDB(BibTagDB.getDB());
  }

  /**
   * Empfohlene Standard-Einstellungen:
   *  <br>
   * <br> RecordDelimiter: "SET: "
   * <br>	LineDelimiter: "(\r)?\n"
   * <br>	IgnoreMARC: false
   * <br>	Handler: WrappingHandler
   * <br>	DefaultTagDB: GNDTagDB
   * <br> DefaultTagDB wird nicht erzwungen
   */
  public final void defaultSettings() {
    setRecordDelimiter("SET: ");
    setLineDelimiter("(\r)?\n");
    setIgnoreMARC(false);
    setHandler(new WrappingHandler());
    setDefaultTagDB(GNDTagDB.getDB());
  }

  /**
   * Empfohlene Einstellungen für Datashop-Downloads:
   *  <br>
   * <br> RecordDelimiter: GS
   * <br>	LineDelimiter: RS
   * <br>	IgnoreMARC: false
   * <br>	Handler: WrappingHandler
   * <br>	DefaultTagDB: GNDTagDB
   * <br> DefaultTagDB wird nicht erzwungen
   */
  public final void datashopSettings() {
    setRecordDelimiter(Constants.GS);
    setLineDelimiter(Constants.RS);
    setIgnoreMARC(false);
    setHandler(new WrappingHandler());
    setDefaultTagDB(GNDTagDB.getDB());
  }

  /**
   *
   * @param file
   * @param charset
   * @throws FileNotFoundException
   */
  public void setSource(final File file, final String charset) throws FileNotFoundException {
    scanner = new Scanner(file, charset);
    scanner.useDelimiter(recordDelimiterPat);
  }

  /**
   * Filter, der VOR dem Parsen entscheidet, ob der Datensatz überhaupt
   * benötigt wird. Für große Datenmengen sinnvoll, da das Parsen
   * des Datensatzes etwa 95% der Zeit benötigt. Durch sinnvolles
   * Vorfiltern kann die Bearbeitungszeit auf 10% reduziert werden.
   *
   * Standard ist: keine leeren Strings.
   *
   * @param streamFilter	Prädikat, das Strings akzeptiert, nicht null.
   */
  public final void setStreamFilter(final Predicate<String> streamFilter) {
    RangeCheckUtils.assertReferenceParamNotNull("streamFilter", streamFilter);
    this.streamFilter = streamFilter;
  }

  /**
   *
   * Ist ein Datensatz als Nächstes da?
   *
   * @return	true, wenn als Nächstes ein Datensatz anliegt.
   */
  @Override
  public final boolean hasNext() {
    if (nextChunk == null) {
      // solange nachladen, bis der nächste Record dem Muster entspricht:
      while (scanner.hasNext()) {
        final String next = scanner.next();

        // wenn Logging, dann Länge von next addieren:
        if (logBytes) {
          int length = 0;
          try {
            final byte[] utf8Bytes = next.getBytes("UTF-8");
            length = utf8Bytes.length;
          } catch (final UnsupportedEncodingException e) {
            // nix
          }
          bytesRead += length;
        }

        if (streamFilter.test(next)) {
          nextChunk = next;
          break;
        }
      }
    }
    return nextChunk != null;
  }

  /**
   *
   * @return nächsten Datensatz. Wenn keiner vorhanden, wird eine
   * 			{@link InputMismatchException} geworfen.
   */
  @Override
  public final Record next() {
    if (hasNext()) {
      final String s = nextChunk;
      nextChunk = null;
      return parser.parse(s);
    } else
      throw new NoSuchElementException("Kein Record mehr da");
  }

  @Override
  protected void finalize() throws Throwable {
    MyFileUtils.safeClose(scanner);
    super.finalize();
  }

  public Stream<Record> stream() {
    return Streams.getStreamFromIterable(this);
  }

  public static void main2(final String[] args) throws IOException {
    final RecordReader reader = getMatchingReader("D:/Normdaten/DNBtitel_Stichprobe.dat.gz");

    final Predicate<String> titleFilter = new ContainsTag("1131", BibTagDB.getDB());
    reader.setStreamFilter(titleFilter);

    reader.forEach(rec ->
    {
      System.out.println(RecordUtils.getContentsOfAllSubfields(rec, "1131", 'a'));
      System.out.println("-----------");
    });
  }

  public static void main1(final String[] args) throws IOException {
    final RecordReader reader = getMatchingReader("D:/Normdaten/DNBtitel_Stichprobe.dat.gz");
    final Predicate<String> titleFilter = new ContainsTag("1131", BibTagDB.getDB());
    reader.setStreamFilter(titleFilter);

    while (reader.hasNext()) {
      final Record record = reader.next();
      System.out.println(RecordUtils.getContentsOfAllSubfields(record, "1131", 'a'));
      System.out.println("-----------");
    }

  }

  public static void main(final String[] args) throws IOException {

    final RecordReader reader =
      RecordReader.getMatchingReader("D:/Normdaten/DNBtitel_Stichprobe.dat.gz");

    final Stream<Record> stream = reader.stream();
    //@formatter:off
        stream
            .filter(rec -> RecordUtils.getDatatype(rec).equals("Oaf"))
            .filter(rec -> StringUtils.equals(BibRecUtils.getYearOfPublicationString(rec),"2013"))
            .map(Record::getId)
            .forEach(System.out::println);
        //@formatter:on
  }

  /**
   * @param filename Dateiname
   * @return          neuen Reader für GZIP,
   *                  Einstellungen für den Standardfall
   * @throws IOException  wenn nicht gefunden
   *
   */
  public static RecordReader gzipReader(final String filename) throws IOException {
    final RecordReader reader = new RecordReader();
    final GZIPInputStream in = MyFileUtils.getGZipInputStream(filename);
    reader.setSource(in);
    reader.gzipSettings();
    return reader;
  }

  /**
   * @param filename  Dateiname
   * @return          neuen Reader, auch GZIP, wenn nötig;
   *                  Einstellungen für den jeweiligen Standardfall
   * @throws IOException  wenn nicht gefunden
   *
   */
  public static RecordReader getMatchingReader(final String filename) throws IOException {
    final RecordReader reader = new RecordReader(filename);
    if (MyFileUtils.isGZipped(filename)) {
      reader.gzipSettings();
    }
    final InputStream inputStream = MyFileUtils.getMatchingInputStream(filename);
    reader.setSource(inputStream);
    return reader;
  }

  /* (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  /* (non-Javadoc)
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    MyFileUtils.safeClose(scanner);

  }

}
