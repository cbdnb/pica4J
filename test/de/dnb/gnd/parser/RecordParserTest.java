package de.dnb.gnd.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.Test;

import de.dnb.gnd.TestUtils;
import de.dnb.gnd.exceptions.WrappingHandler;

public class RecordParserTest {

	@Test
	public void testCreateRecordStringStringExceptionHandler() {
		final RecordParser parser = new RecordParser();
		parser.setHandler(new WrappingHandler());
		try {
			final Record record =
				parser.parse("028@ ƒdMadonna LouiseƒaCicconeƒaCiccone");

			fail();
		} catch (final RuntimeException e) {
			// OK
		}

	}

	@Test
	public void testGetNextRecord() throws IOException {
		String s = "005 Ts1" + "\n" + "008 saz" + '\n' + "011 s";
		RecordReader recordReader = RecordReader.newReader(s);
		recordReader.setRecordDelimiter("005 ");
		Record record = recordReader.next();
		assertTrue(record != null);
		assertTrue(record.getFields().size() == 3);
		try {
			record = recordReader.next();
			fail();
		} catch (final NoSuchElementException e) {
			// nix
		}

		s =
			"005 Ts1" + "\n" + "008 saz" + '\n' + "011 s" + "005 Ts1" + "\n"
				+ "\n008 saq" + '\n' + "011 m";
		recordReader = RecordReader.newReader(s);
		recordReader.setRecordDelimiter("005 ");
		record = recordReader.next();
		record = recordReader.next();
		assertTrue(record != null);
		try {
			record = recordReader.next();
			fail();
		} catch (final NoSuchElementException e) {
			// nix
		}

	}

	@Test
	public void testTextFiles() throws IOException {
		RecordReader readerP3;
		RecordReader readerPPlus;
		readerP3 = new RecordReader(new File("documents/GNDBeispiel.txt"));
		readerPPlus = new RecordReader(new File("documents/GNDBeispiel_P.txt"));

		while (readerP3.hasNext()) {
			Record recordPPlus;
			final Record recordP3 = readerP3.next();
			recordPPlus = readerPPlus.next();
			TestUtils.remove797(recordPPlus);
			assertEquals(recordP3, recordPPlus);
		}

	}
}
