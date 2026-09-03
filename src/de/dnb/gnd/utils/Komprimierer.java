/**
 *
 */
package de.dnb.gnd.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Objects;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.line.LineParser;

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
	 * @param inputFolder  Ordner der Download-Dateien
	 * @param filePrefix   gemeinsames Präfix aller Download-Dateien
	 * @param gzipFileName Zu beschreibende Datei im selben Ordner
	 * @throws IOException Wenn die zu lesenden Dateien nicht existieren
	 */
	public static void komprimiere(final String inputFolder, final String filePrefix, final String gzipFileName)
			throws IOException {
		final Komprimierer komprimierer = new Komprimierer();
		komprimierer.setInputFolder(inputFolder);
		komprimierer.setFilePrefix(filePrefix);
		komprimierer.out = MyFileUtils.getGZipPrintStream(gzipFileName);
		komprimierer.processAllFiles();
		MyFileUtils.safeClose(komprimierer.out);
	}

	private PrintStream out;

	@Override
	protected void processRecord(final Record record) {
		final String recS = toGZip(record);
		out.println(recS);
		System.err.println(recS);
	}

	/**
	 * @param record nicht null
	 * @return Datensatz im GZip-Format, Unicode-Composition!
	 */
	public static String toGZip(final Record record) {
		Objects.requireNonNull(record);
		try {
			record.add(LineParser.parse("001U ƒ0utf8", record.tagDB, false));

		} catch (OperationNotSupportedException | IllegalArgumentException | IllFormattedLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			record.add(LineParser.parse("001X ƒ00", record.tagDB, false));
		} catch (OperationNotSupportedException | IllegalArgumentException | IllFormattedLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			record.add(LineParser.parse("003@ ƒ0" + record.getId(), record.tagDB, false));
		} catch (OperationNotSupportedException | IllegalArgumentException | IllFormattedLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return RecordUtils.toPica(record, Format.PICA_PLUS, true, Constants.RS, Constants.MARC_SUB_SEP);
	}

	/**
	 *
	 * @param records      nicht null
	 * @param gzipFileName nicht null, wird ggf. um ".dat.gz" ergänzt
	 * @throws IOException wenn die Datei nicht geschrieben werden kann
	 */
	public static void toGZip(final Collection<Record> records, String gzipFileName) throws IOException {
		if (!gzipFileName.endsWith(".dat.gz")) {
			gzipFileName += ".dat.gz";
		}
		final PrintStream outFile = MyFileUtils.getGZipPrintStream(gzipFileName);
		records.forEach(record -> {
			final String recS = toGZip(record);
			outFile.print(recS + Constants.RS + Constants.LF);
		});
		MyFileUtils.safeClose(outFile);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {

		komprimiere("D:/Analysen/baumann/Musik", "sag.txt", "D:/Analysen/baumann/Musik/sag.gzip");

	}

}
