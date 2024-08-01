package de.dnb.gnd.parser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;

public class SubfieldTest {

	Subfield subfield1, subfield2;
	String content;
	Indicator indicator1;
	private Indicator indicator2;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testHashCode() throws IllFormattedLineException {
		final GNDTag gNDTag = (GNDTag) GNDTagDB.getDB().findTag("675");
		final Indicator indicator = gNDTag.getIndicator('a', false);
		String content = "q";
		Subfield subfield1, subfield2;
		subfield1 = new Subfield(indicator, content);
		subfield2 = new Subfield(indicator, content);
		assertNotSame(subfield1, subfield2);
		assertEquals(subfield1.hashCode(), subfield2.hashCode());
	}

	@Test
	public final void testSubfieldIndicatorString()
			throws IllFormattedLineException {
		final GNDTag gNDTag = (GNDTag) GNDTagDB.getDB().findTag("675");
		final Indicator indicator = gNDTag.getIndicator('a', false);
		Subfield subfield;

		try {
			subfield = new Subfield(indicator, null);
			fail();
		} catch (IllFormattedLineException e) {
			// OK
		}

//		try {
//			subfield = new Subfield(indicator, "");
//			fail();
//		} catch (IllFormattedLineException e) {
//			// OK
//		}
//
//		try {
//			subfield = new Subfield(indicator, " ");
//			fail();
//		} catch (IllFormattedLineException e) {
//			// OK
//		}

		try {
			Indicator indicator2 = null;
			subfield = new Subfield(indicator2, "a");
			fail();
		} catch (IllFormattedLineException e) {
			// OK
		}

		subfield = new Subfield(indicator, "a");
		assertEquals(" |$a|:a", subfield.toString());
		
		subfield = new Subfield(indicator, "a$$");
		assertEquals(" |$a|:a$", subfield.toString());


//		subfield = new Subfield(indicator, " a ");
//		assertEquals(" |$a|:a", subfield.toString());
	}

	@Test
	public final void testSubfieldTagString()
			throws IllFormattedLineException {
		final GNDTag gNDTag = (GNDTag) GNDTagDB.getDB().findTag("675");
		Subfield sub = new Subfield(gNDTag, "$a a");
		assertEquals(" |$a|: a", sub.toString());

		sub = new Subfield(gNDTag, "$aa");
		assertEquals(" |$a|:a", sub.toString());

		try {
			sub = new Subfield(gNDTag, "$ba");
			fail();
		} catch (IllFormattedLineException e) {
			// OK
		}

//		try {
//			sub = new Subfield(tag, "$a");
//			fail();
//		} catch (IllFormattedLineException e) {
//			// OK
//		}

//		try {
//			sub = new Subfield(tag, "$a  ");
//			fail();
//		} catch (IllFormattedLineException e) {
//			// OK
//		}

	}

	@Test
	public final void testEqualsObject() throws IllFormattedLineException {

		indicator1 = GNDTagDB.getDB().findIndicator("150", 'v');
		indicator2 = GNDTagDB.getDB().findIndicator("130", 'v');
		content = "a";
		subfield1 = new Subfield(indicator1, content);
		subfield2 = new Subfield(indicator2, content);
		assertEquals(subfield1, subfield2);
		subfield2 = new Subfield(indicator2, "b");
		assertNotSame(subfield1, subfield2);
	}

}
