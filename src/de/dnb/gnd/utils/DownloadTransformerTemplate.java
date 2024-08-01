package de.dnb.gnd.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.Tag;

public abstract class DownloadTransformerTemplate extends DownloadWorker {

	private Format format = Format.PICA3;
	private boolean expanded = false;

	// Default-Werte
	private String recordSeparator = "\n\t\n\n";

	protected Tag TAG_797 = defaultTagDB.findTag("797");

	@Override
	protected void processRecord(final Record oldRecord) {
		RangeCheckUtils
			.assertReferenceParamNotNull("oldRecord", oldRecord);
		String id = oldRecord.getId();
		// clone, um alten sicher aufzuheben:
		Record newRecord = oldRecord.clone();
		newRecord = transform(newRecord);

		/*
		 * Daten werden nicht abgespeichert, wenn
		 * 	- keine Änderung erfogte
		 * 	- finalAccept() nicht akzeptiert.
		 */
		//@formatter:off
			if (!equals(newRecord, oldRecord) 
					&& finalAccept(newRecord, oldRecord)) {
				//@formatter:on
			addId(newRecord, id);
			RecordUtils.removeTags(newRecord, "001", "002", "003");
			save(newRecord, format, expanded);
		} else {
			log(oldRecord);
		}
	}

	/**
	 * Logt die nicht abgespeicherten Datensätze. Kann
	 * überschrieben werden.
	 * 
	 * @param record
	 */
	protected void log(final Record record) {
		System.err.println(record + "\n\n---\n\n");
	}

	/**
	 * Vergleicht alten und neuen Datensatz. Kann zur Not auch 
	 * überschrieben werden.
	 * 
	 * @param newRecord
	 * @param oldRecord
	 * @return
	 */
	protected
		boolean
		equals(final Record newRecord, final Record oldRecord) {
		RangeCheckUtils
			.assertReferenceParamNotNull("oldRecord", oldRecord);
		return oldRecord.equals(newRecord);
	}

	/**
	 * Fügt idn hinzu, wenn nötig. Kann zur Not auch überschrieben
	 * werden.
	 * 
	 * @param record	nicht null
	 * @param id		nicht null, nicht leer.
	 */
	protected void addId(Record record, final String id) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		if (RecordUtils.containsField(record, TAG_797))
			return; // erledigt
		RangeCheckUtils.assertStringParamNotNullOrEmpty("id", id);
		String idnew = IDNUtils.extractPPNfromFirstLine(id);
		if (!idnew.equals(id))
			throw new IllegalArgumentException("id nicht zuläsig");

		try {
			Line newLine =
				LineParser.parse("797 " + id, defaultTagDB, false);
			record.add(newLine);
		} catch (IllFormattedLineException e) {
			// ist OK
		} catch (OperationNotSupportedException e) {
			// schon OK
		}

	}

	private void save(
		final Record record,
		final Format format,
		final boolean expanded) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("format", format);
		this.outputStream.append(recordSeparator);
		// \n ist von IT verlangt:
		String recString =
			RecordUtils.toPica(record, format, expanded, "\n", '$');
		this.outputStream.append(recString);

	}

	/**
	 * 
	 * Nimmt cloneRec, eine Kopie des alten Datensatzes, 
	 * und erzeugt einen neuen Datensatz. Der Clone darf verändert
	 * und dann zurückgegeben werden - es kann aber auch ein anderer
	 * Datensatz erzeugt und zurückgegeben werden.
	 * 
	 * @param clonedRecord	Zu verändernder Datensatz, nicht null.
	 * @return				clonedRecord oder einen neuen Datensatz mit 
	 * 						gleicher idn wie der alte.
	 */
	protected abstract Record transform(Record clonedRecord);

	/**
	 * Wenn es noch Besonderheiten gibt, die das Einspielen der Daten
	 * verhindern sollen.
	 * 
	 * @param newRecord		neuer Datensatz
	 * @param oldRecord		aufgehobener alter Datensatz
	 * 
	 * @return		ob der Datensatz akzeptiert wird.
	 */
	protected boolean finalAccept(
		final Record newRecord,
		final Record oldRecord) {
		return true;
	}

	//-------------------------------
	// Öffentliche
	//--------------------------------

	public final void setRecordSeparator(final String recordSeparator) {
		RangeCheckUtils.assertStringParamNotNullOrEmpty("recordSeparator",
			recordSeparator);
		this.recordSeparator = recordSeparator;
	}

	/**
	 * Setzt die Ausgabedatei neu.
	 * @param file	Neue Ausgabedatei, wenn null, erfolgt keine Änderung.
	 */
	public final void setOutputFile(final File file) {
		if (file == null)
			return;
		try {
			this.outputStream = new PrintStream(file, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final void setFormat(final Format format) {
		RangeCheckUtils.assertReferenceParamNotNull("format", format);
		this.format = format;
	}

	public final void setExpanded(final boolean expanded) {
		this.expanded = expanded;
	}

	@Override
	protected void finalize() throws Throwable {
		FileUtils.safeClose(outputStream);
		// als nicht mehr benutzbar markieren:
		outputStream = null;
		super.finalize();
	}

}
