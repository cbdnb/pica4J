/**
 *
 */
package de.dnb.gnd.utils.mx;

import static org.junit.Assert.*;

import org.junit.Test;

import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * @author baumann
 *
 */
public class MxTest {

	/**
	 * Test method for
	 * {@link de.dnb.gnd.utils.mx.LibraryDB#queryISILAgency(java.lang.String)}.
	 */
	@Test
	public void testQueryISILAgency() {
		final Library lib1 = LibraryDB.queryISILAgency("Dö-101");
		assertEquals(Library.NULL_LIBRARY, lib1);
		final Library lib2 = LibraryDB.queryISILAgency("De-101");

		final Library lib3 = LibraryDB.getLibraryByISIL("DE-101");

		assertEquals(lib2, lib3);
	}

	@Test
	public void testParseLib() {

		Pair<Library, String> pair = LibraryDB.parse("");
		assertEquals(pair, LibraryDB.NULL_PAIR);

		pair = LibraryDB.parse(null);
		assertEquals(pair, LibraryDB.NULL_PAIR);

		// Ausnahmen von DE-...
		pair = LibraryDB.parse("ZDB-LU-100");
		assertNotNull(pair);

		pair = LibraryDB.parse("spio");
		assertNotNull(pair);

		// fehlender Bindestrich an erster Position
		pair = LibraryDB.parse("DE-101-SE-ba");
		Pair<Library, String> pair1 = LibraryDB.parse("DE101-SE-ba");
		assertEquals(pair, pair1);

		// ISIL kleingeschrieben:
		pair1 = LibraryDB.parse("de101-SE-ba");
		assertEquals(pair, pair1);

		// aber: Auch Redaktion klein
		pair1 = LibraryDB.parse("de101-se-ba");
		assertNotEquals(pair, pair1);

	}

	@Test
	public void testMxadressParse() {
		MXAddress mxad = MXAddress.parse("e-xDE‐601-SE-qay");
		MXAddress mxad1 = MXAddress.parse("e‐😀-DE‐601‐---SE-€qay");
		assertEquals(mxad, mxad1);

		mxad1 = MXAddress.parse("e-xDE‐601-SE-qa");
		assertNotEquals(mxad1, mxad);

		mxad1 = MXAddress.parse("e-DE‐601-SE-qay");
		assertEquals(mxad1, mxad);

		mxad1 = MXAddress.parse("e-XDE‐601-SE-qay");
		assertEquals(mxad1, mxad);

		mxad1 = MXAddress.parse("ex-DE‐601-SE-qay");
		assertEquals(mxad1, mxad);

		mxad1 = MXAddress.parse("eX-DE‐601-SE-qay");
		assertEquals(mxad1, mxad);

		mxad = MXAddress.parse("e-DE‐601-SE");
		mxad1 = MXAddress.parse("e-DE‐601-SE-");
		assertEquals(mxad1, mxad);

		mxad = MXAddress.parse("e-DE‐601");
		mxad1 = MXAddress.parse("e-DE‐601-");
		assertEquals(mxad1, mxad);
	}

}
