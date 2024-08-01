package de.dnb.gnd.parser.line;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotSame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.utils.RecordUtils;

public class LineTest {

	Line line1;
	Line line2;

	GNDTag gNDTag;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gNDTag = (GNDTag) GNDTagDB.getDB().findTag("675");
		line1 = LineParser.parse(gNDTag, Format.PICA3, "a", false);
		line2 = LineParser.parse(gNDTag, Format.PICA3, "b", false);
	}

	@Test
	public void testAddLine() {
		Line line;

		// Wiederholungszeichen
		try {
			line = line1.add(line2);
			assertEquals("675  |$a|:a |$a|:b", line.toString());
		} catch (OperationNotSupportedException e) {
			fail();
		}

		// Personen
		try {
			line1 = LineParser.parse("100 a, b", GNDTagDB.getDB(), false);
			line2 = LineParser.parse("100 c, d", GNDTagDB.getDB(), false);
			line = line1.add(line2);
			fail();
		} catch (IllFormattedLineException e) {
			fail();
		} catch (OperationNotSupportedException e) {
			// ok
		}

		// Aufzählungen
		try {
			line1 = LineParser.parse("008 saz", GNDTagDB.getDB(), false);
			line2 = LineParser.parse("008 siz", GNDTagDB.getDB(), false);
			line = line1.add(line2);
			Line expected =
				LineParser.parse("008 saz;siz", GNDTagDB.getDB(), false);
			assertEquals(expected, line);
		} catch (IllFormattedLineException e) {
			fail();
		} catch (OperationNotSupportedException e) {
			fail();
		}
	}

	@Test
	public void testEqualsAndHashcode() throws IllFormattedLineException {

		line1 =
			LineParser
				.parse("500 !123456789!qwe", GNDTagDB.getDB(), false);
		String toString = line1.toString();
		line2 =
			LineParser
				.parse("500 !123456789!asd", GNDTagDB.getDB(), false);
		Line line3 =
			LineParser.parse("500 !123456789!", GNDTagDB.getDB(), false);
		assertEquals(line1, line2);
		assertEquals(line1, line3);
		assertEquals(line3, line3);
		assertEquals(line1.hashCode(), line2.hashCode());
		assertEquals(line1.hashCode(), line3.hashCode());
		assertEquals(line3.hashCode(), line3.hashCode());
		// nach all den Operationen unverändert?
		assertEquals(toString, line1.toString());

		line1 = LineParser.parse("008 a;b", GNDTagDB.getDB(), false);
		line2 = LineParser.parse("008 b;a", GNDTagDB.getDB(), false);
		assertEquals(line1, line2);
		assertEquals(line1.hashCode(), line2.hashCode());
		line2 = LineParser.parse("008 b;c", GNDTagDB.getDB(), false);
		assertNotSame(line1, line2);
		assertNotSame(line1, line3);
		assertNotSame(line1.hashCode(), line2.hashCode());

		line1 =
			LineParser.parse(
				"100 $PSchmidt$lFamilie, Oberstein, Idar-Oberstein",
				GNDTagDB.getDB(), false);
		line2 =
			LineParser.parse(
				"100 $PSchmidt$lFamilie, Oberstein, Idar-Oberstein",
				GNDTagDB.getDB(), false);
		assertEquals(line1, line2);
		line2 =
			LineParser.parse(
				"100 $PSchmid$lFamilie, Oberstein, Idar-Oberstein",
				GNDTagDB.getDB(), false);
		assertNotSame(line1, line2);
	}

	@Test
	public void testExpansion() throws Exception {
		assertTrue(!line1.isRelated());
		assertTrue(!line1.isExpanded());

		line1 =
			LineParser.parse("500 !123456789!", GNDTagDB.getDB(), false);
		assertTrue(line1.isRelated());
		assertTrue(!line1.isExpanded());
		assertEquals("123456789", line1.getIdnRelated());
		assertEquals(null, line1.getExpansion());

		line1 =
			LineParser
				.parse("500 !123456789!qwe", GNDTagDB.getDB(), false);
		assertTrue(line1.isRelated());
		assertTrue(line1.isExpanded());
		assertEquals("123456789", line1.getIdnRelated());
		assertEquals("qwe", line1.getExpansion());

		line1 = LineParser.parse("500 qwe", GNDTagDB.getDB(), false);
		assertTrue(!line1.isRelated());
		assertTrue(!line1.isExpanded());
		assertEquals(null, line1.getIdnRelated());
		assertEquals(null, line1.getExpansion());

		line1 =
			LineParser.parse("550 !123456789!", GNDTagDB.getDB(), false);
		assertTrue(line1.isRelated());
		assertTrue(!line1.isExpanded());
		assertEquals("123456789", line1.getIdnRelated());
		assertEquals(null, line1.getExpansion());

		line1 =
			LineParser
				.parse("550 !123456789!qwe", GNDTagDB.getDB(), false);
		assertTrue(line1.isRelated());
		assertTrue(line1.isExpanded());
		assertEquals("123456789", line1.getIdnRelated());
		assertEquals("qwe", line1.getExpansion());

		line1 = LineParser.parse("550 qwe", GNDTagDB.getDB(), false);
		assertTrue(!line1.isRelated());
		assertTrue(!line1.isExpanded());
		assertEquals(null, line1.getIdnRelated());
		assertEquals(null, line1.getExpansion());

	}

	@Test
	public void testGetSubfields() throws IllFormattedLineException {
		line1 =
			LineParser.parse("550 !123456789!qwe$4obin", GNDTagDB.getDB(),
				false);
		Subfield sub1, sub2, sub3;
		gNDTag = (GNDTag) GNDTagDB.getDB().findTag("550");
		sub1 = new Subfield(gNDTag, "$9123456789");
		sub2 = new Subfield(gNDTag, "$8qwe");
		sub3 = new Subfield(gNDTag, "$4obin");
		List<Subfield> subs =
			new LinkedList<Subfield>(Arrays.asList(sub1, sub2, sub3));
		assertEquals(line1.getSubfields(), subs);
		assertEquals(3, line1.getSubfields().size());
		line1 =
			LineParser.parse("550 !123456789!$4obin", GNDTagDB.getDB(),
				false);
		subs = new LinkedList<Subfield>(Arrays.asList(sub1, sub3));
		assertEquals(line1.getSubfields(), subs);
		assertEquals(2, line1.getSubfields().size());
	}

	@Test
	public void testGNDPersonLine() throws IllFormattedLineException {
		line1 =
			LineParser.parse("100 $PLudwigAugust$cvon$vKomm1$vKomm2",
				GNDTagDB.getDB(), false);
		assertEquals("$PLudwigAugust$cvon$vKomm1$vKomm2",
			RecordUtils.toPicaWithoutTag(line1));
		assertEquals("028A $PLudwigAugust$cvon$vKomm1$vKomm2",
			RecordUtils.toPica(line1, Format.PICA_PLUS, true, '$'));
		line1 =
			LineParser.parse("028A $PLudwigAugust$cvon$vKomm1$vKomm2",
				GNDTagDB.getDB(), false);
		assertEquals("$PLudwigAugust$cvon$vKomm1$vKomm2",
			RecordUtils.toPicaWithoutTag(line1));
		assertEquals("028A $PLudwigAugust$cvon$vKomm1$vKomm2",
			RecordUtils.toPica(line1, Format.PICA_PLUS, true, '$'));

		line1 =
			LineParser.parse("028A $PLudwigAugust$cvon$vKomm1$vKomm2",
				GNDTagDB.getDB(), false);
		assertEquals("$PLudwigAugust$cvon$vKomm1$vKomm2",
			RecordUtils.toPicaWithoutTag(line1));
		assertEquals("028A $PLudwigAugust$cvon$vKomm1$vKomm2",
			RecordUtils.toPica(line1, Format.PICA_PLUS, true, '$'));
	}

}
