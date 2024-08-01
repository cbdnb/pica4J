/**
 *
 */
package de.dnb.gnd.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;

/**
 * @author baumann
 *
 *         Komprimiert mehrere Downloaddateien in ein gzip-File.
 *
 */
public class Komprimierer extends DownloadWorker {

  /**
   * @throws IOException
   *
   */
  private Komprimierer() {
    super();
  }

  /**
   *
   * @param inputFolder
   *            Ordner der Download-Dateien
   * @param filePrefix
   *            gemeinsames Präfix aller Download-Dateien
   * @param gzipFileName
   *            Zu beschreibende Datei im selben Ordner
   * @throws IOException
   *             Wenn die zu lesenden Dateien nicht existieren
   */
  public static
    void
    komprimiere(final String inputFolder, final String filePrefix, final String gzipFileName)
      throws IOException {
    final Komprimierer komprimierer = new Komprimierer();
    komprimierer.setInputFolder(inputFolder);
    komprimierer.setFilePrefix(filePrefix);
    komprimierer.out = FileUtils.getGZipPrintStream(gzipFileName);
    komprimierer.processAllFiles();
    FileUtils.safeClose(komprimierer.out);

  }

  private PrintStream out;

  @Override
  protected void processRecord(final Record record) {
    final String recS = toGZip(record);
    out.println(recS);
    System.err.println(recS);
  }

  /**
   * @param record  nicht null
   * @return        Datensatz im GZip-Format
   */
  public static String toGZip(final Record record) {
    Objects.requireNonNull(record);
    return RecordUtils.toPica(record, Format.PICA_PLUS, true, Constants.RS, Constants.MARC_SUB_SEP);
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {

    komprimiere("D:/Analysen/baumann/Musik", "sag.txt", "D:/Analysen/baumann/Musik/sag.gzip");

  }

}
