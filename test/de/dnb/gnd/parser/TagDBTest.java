package de.dnb.gnd.parser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

public class TagDBTest {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public final void testFindTag() {
		String tagStr;
		GNDTag gNDTag;

		tagStr = "150";
		gNDTag = (GNDTag) GNDTagDB.getDB().findTag(tagStr);
		assertNotNull(gNDTag);

		tagStr = "15";
		gNDTag = (GNDTag) GNDTagDB.getDB().findTag(tagStr);
		assertNull(gNDTag);

		tagStr = "";
		try {
			gNDTag = (GNDTag) GNDTagDB.getDB().findTag(tagStr);
		} catch (IllegalArgumentException e1) {
			// OK
		}

		tagStr = null;
		try {
			gNDTag = (GNDTag) GNDTagDB.getDB().findTag(tagStr);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

	}

	@Test
	public final void testGetIndicator() {
		String tag;
		char indC;

		Indicator indicator;
		tag = "150";
		indC = 'v';
		indicator = GNDTagDB.getDB().findIndicator(tag, indC);
		assertNotNull(indicator);

		tag = "15";
		indC = 'v';
		indicator = GNDTagDB.getDB().findIndicator(tag, indC);
		assertNull(indicator);

		tag = "150";
		indC = '!';
		indicator = GNDTagDB.getDB().findIndicator(tag, indC);
		assertNull(indicator);

		tag = null;
		indC = 'v';
		try {
			indicator = GNDTagDB.getDB().findIndicator(tag, indC);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		tag = "";
		indC = 'v';
		try {
			indicator = GNDTagDB.getDB().findIndicator(tag, indC);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

		tag = "150";
		indC = 0;
		try {
			indicator = GNDTagDB.getDB().findIndicator(tag, indC);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	@Test
	public final void testParseTag() {
		System.err.println("TagDBTest: Sollen in TagDB.parseTag() "
			+ "Blanks entfernt werden oder nicht (s. Dokumentation dort)");
		Triplett<Tag, String, String> triplett;
		String line;

		line = "150 aaa";
		triplett = GNDTagDB.getDB().parseTag(line);
		assertNotNull(triplett);

		line = "041A ƒaPhysik";
		triplett = GNDTagDB.getDB().parseTag(line);
		assertNotNull(triplett);

		line = "159 aaa";
		triplett = GNDTagDB.getDB().parseTag(line);
		assertNull(triplett);

		line = "";
		triplett = GNDTagDB.getDB().parseTag(line);
		assertNull(triplett);

		try {
			line = null;
			triplett = GNDTagDB.getDB().parseTag(line);
			fail();
		} catch (IllegalArgumentException e) {
			// OK
		}

	}

}
