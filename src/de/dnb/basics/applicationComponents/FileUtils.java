package de.dnb.basics.applicationComponents;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Objects;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * Diverse kleine Hilfsmethoden für die Dateiverarbeitung.
 *
 * @author Michael Inden, Christian Baumann
 *
 * Copyright 2011 by Michael Inden
 */
public final class FileUtils {
  public static final String XML_ENDING = ".xml";
  public static final String PDF_ENDING = ".pdf";
  public static final String BMP_ENDING = ".bmp";
  public static final String ZIP_ENDING = ".zip";

  private FileUtils() {
    // no ctor
  }

  /**
   * this method ensures that the complete path for the passed file exist.
   * this means that any non existent directories are created.
   * <p>
   * if the passed file is a directory, this directory is created too
   * </p>
   * <p>
   * if it's a file, the direcories to the parent are created
   * </p>
   *
   * @param destinationFilePath
   *            the file path to be created
   */
  public static void ensurePathExists(final String destinationFilePath) {
    if (destinationFilePath == null)
      //@formatter:off
            throw new IllegalArgumentException(
                   "Passed File Path must not be null!");
            //@formatter:on
    ensurePathExists(new File(destinationFilePath));
  }

  /**
   * this method ensures that the complete path for the passed file exist.
   * this means that any non existent directories are created.
   * <p>
   * if the passed file is a directory, this directory is created too
   * </p>
   * <p>
   * if it's a file, the direcories to the parent are created
   * </p>
   *
   * @param destinationFile
   *            the file path to be created
   */
  public static void ensurePathExists(final File destinationFile) {
    if (destinationFile == null)
      throw new IllegalArgumentException("Passed File must not be null!");

    if (destinationFile.isDirectory()) {
      destinationFile.mkdirs();
    } else {
      if (destinationFile.getParentFile() != null) {
        ensurePathExists(destinationFile.getParentFile());
      } else {
        // Aufruf mit Root-Ordner => nichts zu tun
      }
    }
  }

  /**
   * Diese Methode löscht ein Verzeichnis inklusive aller darin enthaltener
   * Verzeichnisse und Dateien.
   *
   * @param aDirectory ein Verzeichnis.
   * @return  true, wenn gelöscht werden konnte.
   */
  public static boolean deleteDir(final String aDirectory) {
    if (aDirectory == null)
      //@formatter:off
            throw new IllegalArgumentException(
                    "Passed File Path must not be null!");
        //@formatter:on
    return delete(new File(aDirectory));
  }

  /**
   * Diese Methode löscht ein Verzeichnis inklusive aller darin enthaltener
   * Verzeichnisse und Dateien.
   *
   * @param aDirectory Verzeichnis.
   * @return true, wenn Verzeichnis gelöscht werden konnte.
   */
  public static boolean deleteDir(final File aDirectory) {
    return delete(aDirectory);
  }

  private static boolean delete(final File aFile) {
    if (aFile == null)
      throw new IllegalArgumentException("Passed File must not be null!");

    if (aFile.exists()) {
      if (aFile.isDirectory()) {
        final File[] content = getAllMatchingFiles(aFile, ACCEPT_ALL);
        for (int i = 0; i < content.length; i++) {
          delete(content[i]);
        }
      }
      return aFile.delete();
    }

    return false;
  }

  // Sammlung praktischer FileFilter
  public static final java.io.FileFilter ACCEPT_ALL = file -> true;

  public static final java.io.FileFilter FILES_ONLY = file -> file.isFile();

  public static final java.io.FileFilter DIRS_ONLY = file -> file.isDirectory();

  /**
   * Diese Methode stellt einen schmalen Wrapper um file.list() dar, damit
   * nicht null zurückgelifert wird!
   *
   * Sammelt alle laut FileFilter passenden Files im übergebenen Verzeichnis
   * inputdir
   *
   * @param inputdir		nicht null.
   * @param fileFilter	auch null.
   * @return 				nicht null, sondern leeres Array, auch wenn
   * 						inputdir kein Verzeichnis ist.
   *
   */
  public static
    File[]
    getAllMatchingFiles(final File inputdir, final java.io.FileFilter fileFilter) {
    RangeCheckUtils.assertReferenceParamNotNull("inputdir", inputdir);
    final File[] files = inputdir.listFiles(fileFilter);

    // listFiles may return null
    if (files != null) {
      return files;
    }

    return new File[0];
  }

  /**
   * converts a windows file name into a ftp or unix / file name.
   *
   * @param parentDirName
   *            the parentDirName to be converted
   * @return a windows file name converted into a ftp or unix / file name.
   */
  public static String convertToFtpName(final String parentDirName) {
    // convert to ftp name
    if (File.separatorChar != '/') {
      return parentDirName.replace(File.separatorChar, '/');
    }
    return parentDirName;
  }

  /**
   * close a Closeable and ignore IOException.
   * @param closeable	may be null.
   */
  public static void safeClose(final Closeable closeable) {
    try {
      if (closeable != null)
        closeable.close();
    } catch (final IOException e) {
      // ignore
    }
  }

  /**
   * Hängt Extension an Filenamen an, wenn nötig.
   *
   * @param filename		nicht null
   * @param fileExtension	nicht null
   * @return				Korrekten Filenamen mit Extension.
   */
  public static
    String
    appendFileExtensionIfNecessary(final String filename, final String fileExtension) {
    checkFileNameAndExtension(filename, fileExtension);
    if (filename.toLowerCase().endsWith(fileExtension.toLowerCase()))
      return filename;
    return filename + fileExtension;
  }

  /**
   * Entfernt Extension von Filenamen, wenn nötig.
   *
   * @param filename		nicht null
   * @param fileExtension	nicht null
   * @return				Korrekten Filenamen mit Extension.
   */
  public static
    String
    removeFileExtensionIfExisting(final String filename, final String fileExtension) {
    checkFileNameAndExtension(filename, fileExtension);

    if (filename.toLowerCase().endsWith(fileExtension.toLowerCase()))
      return filename.substring(0, filename.length() - fileExtension.length());

    return filename;
  }

  /**
   *
   *
   * @param filename      beliebig
   * @param fileExtension beliebig
   *
   * @throws IllegalArgumentException wenn eines
   *          der Argumente null
   */
  private static void checkFileNameAndExtension(final String filename, final String fileExtension) {
    if (filename == null || fileExtension == null)
      //@formatter:off
            throw new IllegalArgumentException(
                    "Parameters 'filename' and 'fileExtension' "
                    + "must not be null!");
        //@formatter:on
  }

  /**
   * Kopiert byteweise.
   *
   * @param fromFile		nicht null, lesbar.
   * @param toFile		nicht null, beschreibbar.
   * @throws IOException	wenn Fehler beim Öffnen und Kopieren enststehen.
   */
  public static void copy(final File fromFile, final File toFile) throws IOException {
    //@formatter:off
        if (fromFile == null)
            throw new IllegalArgumentException(
                    "parameter 'fromFile' must not be null!");

        if (toFile == null)
            throw new IllegalArgumentException(
                    "Passed 'toFile' must not be null!");

        if (!fromFile.isFile() || !fromFile.exists() || !fromFile.canRead())
            throw new IllegalArgumentException(
                    "Passed 'fromFile' must exist and be a readable file!");

        if (toFile.exists() && toFile.isFile() && !toFile.canWrite())
            throw new IllegalArgumentException(
                    "Passed 'toFile' must be a writable file!");
        //@formatter:on
    final InputStream is = new FileInputStream(fromFile);
    final OutputStream os = new FileOutputStream(toFile);
    try {
      StreamUtils.copyBuffered(is, os);
    } finally {
      StreamUtils.safeClose(is);
      StreamUtils.safeClose(os);
    }
  }

  /**
    * Checks if an input stream is gzipped.
    *
    * @param inputStream nicht null
    * @return
    */
  public static boolean isGZipped(InputStream inputStream) {
    RangeCheckUtils.assertReferenceParamNotNull("inputStream", inputStream);
    if (!inputStream.markSupported()) {
      inputStream = new BufferedInputStream(inputStream);
    }
    inputStream.mark(2);
    int magic = 0;
    try {
      magic = inputStream.read() & 0xff | ((inputStream.read() << 8) & 0xff00);
      inputStream.reset();
    } catch (final IOException e) {
      e.printStackTrace(System.err);
      return false;
    }
    return magic == GZIPInputStream.GZIP_MAGIC;
  }

  /**
   * Checks if a file is gzipped.
   *
   * @param file	nicht null
   * @return  Datei ist GZIP
   */
  public static boolean isGZipped(final File file) {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    int magic = 0;
    try {
      final RandomAccessFile raf = new RandomAccessFile(file, "r");
      magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
      raf.close();
    } catch (final Throwable e) {
      //
    }
    return magic == GZIPInputStream.GZIP_MAGIC;
  }

  /**
   * Checks if a file is gzipped.
   *
   * @param filename  nicht null
   * @return  Datei ist GZIP
   */
  public static boolean isGZipped(final String filename) {
    RangeCheckUtils.assertReferenceParamNotNull("filename", filename);
    final File file = new File(filename);
    return isGZipped(file);
  }

  /**
   * Gibt zu einer gzip-Datei einen Reader. gzip-Archive enthalten nur
   * eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				Buffered Reader.
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static BufferedReader getGZipReader(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    final GZIPInputStream in = getGZipInputStream(file);
    final InputStreamReader inputStreamReader = new InputStreamReader(in);
    final BufferedReader reader = new BufferedReader(inputStreamReader);
    return reader;
  }

  /**
   * Gibt zu einer gzip-Datei einen Reader. gzip-Archive enthalten nur
   * eine Datei, die verpackt ist.
   *
   * @param fileName		nicht null
   * @return				Buffered Reader.
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static BufferedReader getGZipReader(final String fileName) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("fileName", fileName);
    final File file = new File(fileName);
    return getGZipReader(file);
  }

  /**
   * Gibt den passenden Inputstream zur Datei. Wenn eine gzip-Datei
   * vorliegt, ist das der GZIPInputStream, ansonsten der
   * FileInputStream.
   *
   * @param file			nicht null
   * @return				Stream
   * @throws IOException	wenn Datei nicht gelesen werden kann
   */
  public static InputStream getMatchingInputStream(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    if (isGZipped(file))
      return getGZipInputStream(file);
    else
      return new FileInputStream(file);
  }

  /**
   * Gibt den passenden Inputstream zur Datei. Wenn eine gzip-Datei
   * vorliegt, ist das der GZIPInputStream, ansonsten der
   * FileInputStream.
   *
   * @param file			nicht null
   * @return				Stream
   * @throws IOException	wenn Datei nicht gelesen werden kann
   */
  public static InputStream getMatchingInputStream(final String file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    return getMatchingInputStream(new File(file));
  }

  /**
   * Gibt zu einer gzip-Datei einen GZIPInputStream. gzip-Archive enthalten
   * nur eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				GZInputStream
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static GZIPInputStream getGZipInputStream(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    final FileInputStream inputStream = new FileInputStream(file);
    final GZIPInputStream in = new GZIPInputStream(inputStream);
    return in;
  }

  /**
   * Gibt zu einer gzip-Datei einen GZIPInputStream. gzip-Archive enthalten
   * nur eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				GZIPOutputStream
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static GZIPInputStream getGZipInputStream(final String file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    return getGZipInputStream(new File(file));
  }

  /**
   * Gibt zu einer gzip-Datei einen Reader. gzip-Archive enthalten nur
   * eine Datei, die verpackt ist.
   *
   * @param fileName		nicht null
   * @return				Buffered Reader.
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static BufferedWriter getGZipWriter(final String fileName) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("fileName", fileName);
    final File file = new File(fileName);
    return getGZipWriter(file);
  }

  /**
   * Gibt zu einer gzip-Datei einen Writer. gzip-Archive enthalten nur
   * eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				Buffered Writer.
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static BufferedWriter getGZipWriter(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    final GZIPOutputStream out = getGZipOutputStream(file);
    final OutputStreamWriter outputStreamReader = new OutputStreamWriter(out);
    final BufferedWriter writer = new BufferedWriter(outputStreamReader);
    return writer;
  }

  /**
   * Gibt zu einer gzip-Datei einen GZIPInputStream. gzip-Archive enthalten
   * nur eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				GZIPOutputStream
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static GZIPOutputStream getGZipOutputStream(final String file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    return getGZipOutputStream(new File(file));
  }

  /**
   * Gibt zu einer gzip-Datei einen GZIPInputStream. gzip-Archive enthalten
   * nur eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				GZInputStream
   * @throws IOException	wenn keine GZip-Datei oder ein IO-Fehler.
   */
  public static GZIPOutputStream getGZipOutputStream(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    final FileOutputStream outputStream = new FileOutputStream(file);
    final GZIPOutputStream out = new GZIPOutputStream(outputStream);
    return out;
  }

  /**
   * Gibt zu einer gzip-Datei einen PrintStream. gzip-Archive enthalten nur
   * eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				PrintStream
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static PrintStream getGZipPrintStream(final File file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    final GZIPOutputStream out = getGZipOutputStream(file);
    final PrintStream printStream = new PrintStream(out);
    return printStream;
  }

  /**
   * Liest eine Datei in eine vorgegebene String-Collection.
   *
   * @param file				nicht null
   * @param stringCollection	nicht null
   * @throws FileNotFoundException bei Dateiproblemen
   *
   */
  public static
    void
    readFileIntoCollection(final String file, final Collection<String> stringCollection)
      throws FileNotFoundException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    RangeCheckUtils.assertReferenceParamNotNull("stringCollection", stringCollection);
    final Scanner scanner = new Scanner(new File(file));
    while (scanner.hasNext()) {
      stringCollection.add(scanner.nextLine());
    }
    safeClose(scanner);
  }

  /**
   * Gibt zu einer gzip-Datei einen PrintStream. gzip-Archive enthalten nur
   * eine Datei, die verpackt ist.
   *
   * @param file			nicht null
   * @return				PrintStream
   * @throws IOException	wenn keine Zip-Datei oder ein IO-Fehler.
   */
  public static PrintStream getGZipPrintStream(final String file) throws IOException {
    RangeCheckUtils.assertReferenceParamNotNull("file", file);
    return getGZipPrintStream(new File(file));
  }

  public static void main(final String... strings) throws IOException {
    final File ff = new File("D:/temp");
    System.out.println(ff.isDirectory());
    System.out.println(ff.getParentFile());
    System.out.println(File.separator);

    ensurePathExists(ff);
  }

  /**
   *
   * @param classObj  nicht null
   * @return  null, wenn nicht gefunden wurde, sonst: das Verzeichnis,
   *          in dem die Anwendung läuft. Vor
   *          allem wichtig bei .jar; Vorsicht: das ist NICHT das
   *          Verzeichnis der .class-Datei, sondern in der Regel ein
   *          darüberliegendes (RTFM).
   */
  public static String getExecutionDirectory(final Class<?> classObj) {
    RangeCheckUtils.assertReferenceParamNotNull("class", classObj);
    final CodeSource codeSource = classObj.getProtectionDomain().getCodeSource();
    if (codeSource == null)
      return null;
    try {
      final File dir = new File(codeSource.getLocation().toURI().getPath());
      String path = dir.getPath();
      if (path.endsWith(".jar")) {
        path = dir.getParent();
      }
      return path;
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   *
   * @return  Das Verzeichnis, in dem sich FileUtils befindet. Vor
   *          allem wichtig bei .jar; Vorsicht: das ist NICHT das
   *          Verzeichnis der .class-Datei!
   */
  public static String getExecutionDirectory() {
    return getExecutionDirectory(FileUtils.class);

  }

  /**
   *
   * @param fileName  nicht null
   * @param append    soll angehängt (append==true) oder überschrieben werden?
   *
   * @return          {@link PrintWriter} in den mit println() geschrieben werden kann
   * @throws IOException
   *                  Wenn File nicht erzeugt werden kann
   */
  public static PrintWriter oeffneAusgabeDatei(final String fileName, final boolean append)
    throws IOException {
    Objects.requireNonNull(fileName);
    return new PrintWriter(new FileWriter(fileName, append), true);
  }

  /**
  *
  * @param fileName  nicht null
  * @param append    soll angehängt (append==true) oder überschrieben werden?
  *
  * @return          {@link PrintWriter} in den mit println() geschrieben werden kann
  * @throws IOException
  *                  Wenn File nicht erzeugt werden kann
  */
  public static PrintWriter outputFile(final String fileName, final boolean append)
    throws IOException {
    return oeffneAusgabeDatei(fileName, append);
  }

}
