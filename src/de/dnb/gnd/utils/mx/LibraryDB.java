/**
 *
 */

package de.dnb.gnd.utils.mx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

import de.dnb.basics.Misc;
import de.dnb.basics.applicationComponents.LFUCachedFunction;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.tries.TST;
import de.dnb.basics.tries.Trie;
import de.dnb.gnd.parser.Record;

/**
 * @author baumann
 *
 */
public class LibraryDB {

	public static final Pair<Library, String> NULL_PAIR;

	private static final Map<String, Library> ISIL_2_LIB;

	private static final Map<String, Library> KURZ_2_LIB;

	/**
	 * Enthält die ISIL + '-':
	 *
	 * "DE-1-" -> Stabi Berlin "DE-101-" -> DNB
	 *
	 * Damit kann das Präfix gesucht werden und bei Eingabe von "DE-10" wird nicht
	 * die Stabi ausgegeben. Das "-" dient damit sowohl als interner Separator als
	 * auch als Endezeichen.
	 */
	private static Trie<Library> ISIL_2_LIBRARY_TRIE = new TST<>();

	static {
		NULL_PAIR = new Pair<>(Library.NULL_LIBRARY, "");
		ISIL_2_LIB = new TreeMap<>();
		KURZ_2_LIB = new HashMap<>();
		loadLibraries();
	}

	/**
	 * Sucht die längste passende ISIL.
	 *
	 * @param mx etwa in der Form DE-603-FE (der erste '-' darf fehlen)
	 * @return etwas wie (HeBIS, FE) oder {@link Pair#getNullPair()}
	 */
	public static Pair<Library, String> parseByISILAgency(final String mx) {
		if (StringUtils.isNullOrEmpty(mx)) {
			return NULL_PAIR;
		}

		final List<String> list = Mailbox.splitRawAddress(mx);
		final int length = list.size();
		for (int i = length; i >= 1; i--) {

			final List<String> subList = list.subList(0, i);
			final String query = StringUtils.concatenate("-", subList);
			final Library library = queryISILAgency(query);
			if (library != null) {
				final String rest = StringUtils.concatenate("-", list.subList(i, length));
				return new Pair<Library, String>(library, rest);
			}
		}
		return NULL_PAIR;
	}

	static LFUCachedFunction<String, Library> queryAgencyLFU = new LFUCachedFunction<String, Library>(50) {

		@Override
		protected Library calculate(final String isil) {
			return queryAgency(isil);
		}
	};

	/**
	 * Sucht über die ISIL bei <a>
	 * https://sigel.staatsbibliothek-berlin.de/startseite/</a>.
	 *
	 * @param isil auch null, eventuell fehlerhaft (fehlender '-')
	 * @return Bibliothek oder {@link Library#getNullLibrary()} Die zugehörige
	 *         Verbundbibliothek wird als DEFAULT angenommen. Wenn im Json-Datensatz
	 *         keine ISIL erkannt wird, wird eine Library mit normalisierter isil
	 *         zurückgegeben
	 */
	private static Library queryAgency(String isil) {
		isil = normalize(isil);
		if (isil == null) {
			return Library.getNullLibrary();
		}

		final String query = "https://sigel.staatsbibliothek-berlin.de/api/org/" + isil + ".jsonld";
		final String jsonS = Misc.getWebsite(query);

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonS);
		} catch (final Exception e) {
			return Library.getNullLibrary();
		}

		final Record record = LibraryUtils.parse(jsonObject);

		if (record == null) {
			return Library.getNullLibrary();
		}
		final String longN = LibraryUtils.getLongName(record);
		String isilKorr = LibraryUtils.getIsil(record);
		if (isilKorr == null) {
			isilKorr = isil;
		}
		final String shortN = LibraryUtils.getBestShortName(record);
		final String verbund = LibraryUtils.getVerbund(record);

		final Library library = new Library("XXXX", shortN, isilKorr, longN);
		if (!StringUtils.isNullOrEmpty(verbund)) {
			final Library verbLib = getLibraryByShortName(verbund);
			if (verbLib != null) {
				final String isilverb = verbLib.isil;
				library.addRedaktion(RedaktionsTyp.DEFAULT, isilverb);
			}
		}
		return library;
	}

	/**
	 * die Differenz der Stringlänge nach Normalisierung.
	 */
	private static int normalizationDelta = 0;

	/**
	 * Versucht ISILs wie DE101 zu korrigieren, indem ein "-" eingeschoben wird. Das
	 * Resultat ist dann DE-101. Notationen wie ZDB-LU-100 werden gleichfalls
	 * erkannt und gegebenenfalls korrigiert.
	 *
	 * @param mxAdress beliebig, aber getrimmt
	 * @return Versuchsweise korrigierte ISIL oder <code>null</code>
	 */
	private static String normalize(String mxAdress) {
		if (mxAdress == null) {
			return null;
		}
		mxAdress = mxAdress.toUpperCase();

		normalizationDelta = 0;
		if (mxAdress.length() < 3) {
			return null;
		}
		if ("spio".equalsIgnoreCase(mxAdress)) {
			return mxAdress;
		} else if ("pseu".equalsIgnoreCase(mxAdress)) {
			return mxAdress;
		}
		int dashPos = 2;
		// Wenn z.B. ZDB-LU-100:
		if (mxAdress.startsWith("ZDB")) {
			dashPos = 3;
		}

		final char charAtDashpos = StringUtils.charAt(mxAdress, dashPos);

		if (charAtDashpos != '-') {

			mxAdress = mxAdress.substring(0, dashPos) + "-" + mxAdress.substring(dashPos);
			normalizationDelta = 1;
		}

		return mxAdress;

	}

	public static void main(final String... args) throws IOException {

//		loadLibrariesExtern();
//		System.out.println(serialize());

		final Library lib1 = ISIL_2_LIB.get("DE-101c");
		System.out.println(lib1.toStringLang());

	}

	public static void main1(final String... args) throws IOException {
		final Pair<Library, String> db = parseDB("spio");
		System.out.println(db.first.toStringLang());
	}

	public static Collection<Library> getLibraries() {
		return ISIL_2_LIB.values();
	}

	public static List<Library> getLibrariesSortName() {
		final List<Library> libraries = new ArrayList<>(getLibraries());
		final Comparator<Library> c = (a, b) -> a.nameKurz.compareToIgnoreCase(b.nameKurz);
		Collections.sort(libraries, c);
		return libraries;
	}

	/**
	 * Sucht in der internen Datenbank oder ersatzweise bei der Agentur (Stabi
	 * Berlin)
	 *
	 * @param isil nicht null, nicht leer
	 * @return Die zugehörige Bibliothek oder null
	 */
	public static Library getLibraryByISIL(final String isil) {
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("isil", isil);
		Library library = ISIL_2_LIB.get(isil);
		if (library == null) {
			library = queryISILAgency(isil);
		}
		return library;
	}

	/**
	 * Parst den Anfang der Mailboxadresse (z.B. DE-101-...) und sucht nach nach der
	 * ISIL. Die ISIL besteht aus 2 Buchstaben Ländercode + "-" +
	 * Institutionenkennzeichen. Der Bindestrich darf fehlen. Die Mailboxadresse
	 * muss aus der ISIL allein oder aus ISIL + "-" + Rest bestehen. Das irrtümliche
	 * Fehlen des Rests wird akzeptiert. <br>
	 * <br>
	 * Wird in der internen Datenbank nichts gefunden, wird an der Datenbank der
	 * Deutschen ISIL-Agentur
	 * <a>https://sigel.staatsbibliothek-berlin.de/startseite/</a> gesucht. <br>
	 * <br>
	 * Ansonsten wird null zurückgegeben.
	 *
	 *
	 * @param isilPlusRest auch null, auch Kleinbuchstaben werden akzeptiert. Kann
	 *                     ungetrimmt sein.
	 * @return (bibliothek, Rest). {@link Pair#getNullPair()} wenn nichts gefunden
	 *         oder ein Fehler beim Rest aufgetreten ist.
	 */
	public static Pair<Library, String> parse(String isilPlusRest) {

		if (isilPlusRest == null) {
			return NULL_PAIR;
		}
		isilPlusRest = isilPlusRest.trim();
		final Pair<Library, String> pair = parseDB(isilPlusRest);

		if (pair != null) {
			return pair;
		} else {
			return parseByISILAgency(isilPlusRest);
		}

	}

	/**
	 * Parst in der internen DB den Anfang der Mailboxadresse (z.B. DE-101-...) und
	 * sucht nach nach der ISIL. Der String muss aus der ISIL allein oder aus ISIL +
	 * "-" + Rest bestehen. Das irrtümliche Fehlen des Rests wird akzeptiert. <br>
	 * Sonderfälle sind spio und pseu. <br>
	 * Ansonsten wird {@link this#NULL_PAIR} zurückgegeben.
	 *
	 *
	 * @param isilPlusRest auch null, auch Kleinbuchstaben werden akzeptiert. Muss
	 *                     dann aber getrimmt sein.
	 * @return (bibliothek, Rest). {@link this#NULL_PAIR} wenn nichts gefunden oder
	 *         ein Fehler beim Rest aufgetreten ist.
	 */
	public static Pair<Library, String> parseDB(final String isilPlusRest) {

		final Library lib = getLibOfLongestPrefix(isilPlusRest);

		if (lib == Library.NULL_LIBRARY) {
			return NULL_PAIR;
		}

		final String isil = lib.isil;
		final int beginIndex = isil.length() - normalizationDelta;
		String rest = isilPlusRest.substring(beginIndex);

		if (!rest.isEmpty()) {
			rest = rest.substring(1);
		}

		return new Pair<Library, String>(lib, rest);
	}

	/**
	 *
	 * @param mx auch null, auch Kleinbuchstaben oder fehlerhafte ISILs (DE1 anstatt
	 *           DE-1) werden akzeptiert
	 * @return Typ oder {@link Library#getNullLibrary()}
	 */
	public static Library getLibOfLongestPrefix(String mx) {
		mx = normalize(mx);
		if (mx == null) {
			return Library.getNullLibrary();
		}
		final Library valueOfLongestPrefix = ISIL_2_LIBRARY_TRIE.getValueOfLongestPrefix(mx.toUpperCase() + "-");
		return valueOfLongestPrefix == null ? Library.NULL_LIBRARY : valueOfLongestPrefix;
	}

	/**
	 * @param kurz auch null
	 * @return Die zugehörige Bibliothek oder null
	 */
	public static Library getLibraryByShortName(final String kurz) {
		return KURZ_2_LIB.get(kurz);
	}

	public static String serialize() {
		final Collection<Library> values = ISIL_2_LIB.values();
		String s = "";

		s += "ISIL_2_LIB.clear();\n";
		s += "KURZ_2_LIB.clear();\n";
		s += "ISIL_2_LIBRARY_TRIE.clear();\n\n";

		s += "Library bibliothek;\n";

		for (final Library library : values) {
			s += library.serialize();
			s += "\n";

			s += "ISIL_2_LIB.put(bibliothek.isil, bibliothek);\n";
			s += "KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);\n";
			s += "ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + \"-\", bibliothek);\n\n";

		}
		return s;
	}

	/**
	 * Zum Laden über Code, der aus serialize() gewonnen wird.
	 */
	private static void loadLibraries() {
		ISIL_2_LIB.clear();
		KURZ_2_LIB.clear();
		ISIL_2_LIBRARY_TRIE.clear();

		Library bibliothek;
		bibliothek = new Library("XXX", "Stadtarchiv Attnang-Puchheim", "AT-41703AR", "Stadtarchiv Attnang-Puchheim");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Stadtarchiv Schwaz", "AT-70926AR", "Stadtarchiv Schwaz");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Stadtbücherei Dornbirn", "AT-80301001BUE", "Stadtbücherei Dornbirn");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Amt der Stadt Feldkirch Stadtbib.", "AT-80404STB",
				"Amt der Stadt Feldkirch Stadtbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Bücherei Sulz-Röthis", "AT-80420001BUE", "Bücherei Sulz-Röthis");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Provinz der Gesellschaft Jesu Archiv", "AT-AASI-AR",
				"Österreichische Provinz der Gesellschaft Jesu Archiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "BA für Agrarwirtschaft Bib.", "AT-AGWI",
				"Bundesanstalt für Agrarwirtschaft Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Kammer für Arbeiter und Angestellte für Wien AK Bib. Wien für Sozialwissenschaften", "AT-AKW",
				"Kammer für Arbeiter und Angestellte für Wien AK Bibliothek Wien für Sozialwissenschaften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Diözese Gurk Archiv der Diözese Gurk", "AT-ARCHDZSGURK",
				"Diözese Gurk Archiv der Diözese Gurk");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Bib. der Verlage", "AT-BDV", "Bibliothek der Verlage");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Bundesinstitut für Erwachsenenbildung St. Wolfgang Bib.", "AT-BIFEB",
				"Bundesinstitut für Erwachsenenbildung St. Wolfgang Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Bundeskanzleramt Administrative Bib. des Bundes", "AT-BKA",
				"Bundeskanzleramt Administrative Bibliothek des Bundes");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Burgenländisches L-Arch.", "AT-BLA", "Burgenländisches Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "BM für Europa, Integration und Äusseres Aussenpolitische Bib.", "AT-BMA",
				"Bundesministerium für Europa, Integration und Äusseres Aussenpolitische Bibliothek (Amtsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "BM für Bildung und Frauen Schulbuch- und Schulschriftensammlung", "AT-BMUK",
				"Bundesministerium für Bildung und Frauen Schulbuch- und Schulschriftensammlung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Compass-Verlag GmbH Compass-Archiv", "AT-COMPASS",
				"Compass-Verlag GmbH Compass-Archiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Diözese Gurk Diözesanbib.", "AT-DBGK", "Diözese Gurk Diözesanbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Erzdiözese Salzburg Diözesanbib.", "AT-DBSBG",
				"Erzdiözese Salzburg Diözesanbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Don Juan Archiv Wien", "AT-DJARCH", "Don Juan Archiv Wien");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Diözese Linz Private Päd. HS Bib.", "AT-DLIHS-B",
				"Diözese Linz Private Pädagogische Hochschule Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Donau-Uni Krems Bib.", "AT-DUK-B", "Donau-Universität Krems Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Diözese St. Pölten Diözesanarchiv", "AT-DZSASTP",
				"Diözese St. Pölten Diözesanarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Erzdiözese Wien Kirchliche Päd. HS Wien/Krems Campus-Bibliotheken Bib.",
				"AT-EDW-PH",
				"Erzdiözese Wien Kirchliche Pädagogische Hochschule Wien/Krems Campus-Bibliotheken Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Tiroler Landesmuseum Ferdinandeum Bib.", "AT-FERD",
				"Tiroler Landesmuseum Ferdinandeum Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Burgenland GmbH Bib.", "AT-FHB",
				"Fachhochschule Burgenland GmbH Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Burgenland GmbH | StO Eisenstadt Bib.", "AT-FHB-E",
				"Fachhochschule Burgenland GmbH | Standort Eisenstadt Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Burgenland GmbH | StO Pinkafeld Bib.", "AT-FHB-P",
				"Fachhochschule Burgenland GmbH | Standort Pinkafeld Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH des BFI Wien Bib.", "AT-FHBFIW", "Fachhochschule des BFI Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Campus Wien Bib. und Mediathek", "AT-FHCW",
				"Fachhochschule Campus Wien Bibliothek und Mediathek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Joanneum GmbH Bib.", "AT-FHJ", "Fachhochschule Joanneum GmbH Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Joanneum GmbH Bib. StO Bad Gleichenberg", "AT-FHJ-BG",
				"Fachhochschule Joanneum GmbH Bibliothek Standort Bad Gleichenberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Joanneum GmbH Bib. StO Graz", "AT-FHJ-G",
				"Fachhochschule Joanneum GmbH Bibliothek Standort Graz");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Joanneum GmbH Bib. StO Graz-Ost", "AT-FHJ-GO",
				"Fachhochschule Joanneum GmbH Bibliothek Standort Graz-Ost");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Joanneum GmbH Bib. StO Kapfenberg", "AT-FHJ-K",
				"Fachhochschule Joanneum GmbH Bibliothek Standort Kapfenberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Kärnten FH-Bib.", "AT-FHK",
				"Fachhochschule Kärnten Fachhochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Kärnten FH-Bib. StO Feldkirchen i. K.", "AT-FHK-FE",
				"Fachhochschule Kärnten Fachhochschulbibliothek Standort Feldkirchen i. K.");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Kärnten FH-Bib. StO Klagenfurt", "AT-FHK-KL",
				"Fachhochschule Kärnten Fachhochschulbibliothek Standort Klagenfurt");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Kärnten FH-Bib. StO Spittal a. d. Drau", "AT-FHK-SP",
				"Fachhochschule Kärnten Fachhochschulbibliothek Standort Spittal a. d. Drau");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Kärnten FH-Bib. StO Villach - St. Magdalen", "AT-FHK-VI",
				"Fachhochschule Kärnten Fachhochschulbibliothek Standort Villach - St. Magdalen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Salzburg Bib.", "AT-FHS", "Fachhochschule Salzburg Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Salzburg | StO Kuchl Bib.", "AT-FHS-K",
				"Fachhochschule Salzburg | Standort Kuchl Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Salzburg | StO Urstein Bib.", "AT-FHS-U",
				"Fachhochschule Salzburg | Standort Urstein Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH St. Pölten Bib.", "AT-FHSTP", "Fachhochschule St. Pölten Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "FH Vorarlberg Bib.", "AT-FHV", "Fachhochschule Vorarlberg Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "CAMPUS 02 FH der Wirtschaft Bib.", "AT-FHWG",
				"CAMPUS 02 Fachhochschule der Wirtschaft Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Frauensolidarität - Bib. und Dokumentationsstelle Frauen und \"Dritte Welt\"",
				"AT-FRSO", "Frauensolidarität - Bibliothek und Dokumentationsstelle Frauen und \"Dritte Welt\"");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Gesellschaft der Ärzte in Wien Bib.", "AT-GDAeW",
				"Gesellschaft der Ärzte in Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Geologische BA Bib.", "AT-GEOL", "Geologische Bundesanstalt Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Grüne Bildungswerkstatt Grünes Archiv Österreich", "AT-GRUeARCH",
				"Grüne Bildungswerkstatt Grünes Archiv Österreich");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Albertina Bib.", "AT-GSA", "Albertina Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Katholische Privatuni Linz Bib.", "AT-HBTL",
				"Katholische Privatuniversität Linz Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Heeresgeschichtliches Mus Bib.", "AT-HGMW",
				"Heeresgeschichtliches Museum Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Inst. für Höhere Studien Bib.", "AT-IHS",
				"Institut für Höhere Studien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Max Perutz Library", "AT-IMP", "Max Perutz Library");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Öster. Geschichtsfor", "AT-IOeGF",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Österreichische Geschichtsfor");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Institute of Science and Technology Austria Bib.", "AT-ISTA",
				"Institute of Science and Technology Austria Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Jüdisches Mus der Stadt Wien Bib.", "AT-JMW",
				"Jüdisches Museum der Stadt Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "KDZ - Zentrum für Verwaltungsforschung Bib.", "AT-KDZ",
				"KDZ - Zentrum für Verwaltungsforschung Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Kunsthist. Mus Bib.", "AT-KHMW-BIB", "Kunsthistorisches Museum Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Kunsthist. Mus Münzkabinett", "AT-KHMW-MK",
				"Kunsthistorisches Museum Münzkabinett");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Kärntner L-Arch.", "AT-KLA", "Kärntner Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Musik und Kunst - Privatuni der Stadt Wien Bib.", "AT-KWPU",
				"Musik und Kunst - Privatuniversität der Stadt Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Burgenländische Landesbib.", "AT-LBB", "Burgenländische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2107", "Graz Steiermaerk. LB", "AT-LBST", "Steiermärkische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-NOELB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Landeskonservatorium für Vorarlberg", "AT-LKONSV",
				"Landeskonservatorium für Vorarlberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Landesmuseum für Kärnten Bib.", "AT-LMK",
				"Landesmuseum für Kärnten Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Oberöster. Landesmuseen Bib.", "AT-LMO-BIB",
				"Oberösterreichische Landesmuseen Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Landesschulrat für Kärnten Amtsbib.", "AT-LSRK",
				"Landesschulrat für Kärnten Amtsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Mus für angewandte Kunst Bib.", "AT-MAKW",
				"Österreichisches Museum für angewandte Kunst Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Internationale Stiftung Mozarteum Bibliotheca Mozartiana", "AT-MOZ",
				"Internationale Stiftung Mozarteum Bibliotheca Mozartiana");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Naturhistorisches Mus Wien", "AT-NMW", "Naturhistorisches Museum Wien");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Naturhistorisches Mus Wien Zoologische Hauptbib.", "AT-NMW-Z",
				"Naturhistorisches Museum Wien Zoologische Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Amt der Niederösterreichischen Landesreg. Abt. NÖ L-Arch. und NÖ Inst. für Landesku", "AT-NOeLA",
				"Amt der Niederösterreichischen Landesregierung Abt. NÖ Landesarchiv und NÖ Institut für Landesku");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1402", "OENDV", "AT-NOeLB", "Niederösterreichische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-NOELB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("9005", "ÖBV", "AT-OBV", "Österreichischer Bibliothekenverbund");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2108", "OBVSG", "AT-OBVSG", "OBVSG");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Oberöster. L-Arch.", "AT-OOeLA", "Oberösterreichisches Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Oberöster. L-Arch. Bib.", "AT-OOeLA-B",
				"Oberösterreichisches Landesarchiv Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Oberöster. Landesbib.", "AT-OOeLB", "Oberösterreichische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Archäologisches Inst. Bib. Wien", "AT-OeAI",
				"Österreichisches Archäologisches Institut Bibliothek Wien");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Akad. der Wissenschaften", "AT-OeAW",
				"Österreichische Akademie der Wissenschaften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Öster. Akad. der Wissenschaften Bib., Archiv, Sammlungen: Information & Service (", "AT-OeAW-BA",
				"Österreichische Akademie der Wissenschaften Bibliothek, Archiv, Sammlungen: Information & Service (");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Nationalbib.", "AT-OeNB", "Österreichische Nationalbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Nationalbib. Sammlung für Plansprachen und Esperanto-Mus",
				"AT-OeNB-ESP", "Österreichische Nationalbibliothek Sammlung für Plansprachen und Esperanto-Museum");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Nationalbib. Sammlung von Handschriften und alten Drucken",
				"AT-OeNB-HSAD", "Österreichische Nationalbibliothek Sammlung von Handschriften und alten Drucken");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Nationalbib. Literaturarchiv", "AT-OeNB-LIT",
				"Österreichische Nationalbibliothek Literaturarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Staatsarchiv", "AT-OeSTA", "Österreichisches Staatsarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Staatsarchiv Bib.", "AT-OeSTA-BIB",
				"Österreichisches Staatsarchiv Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Österreichischer Verbundkatalog für Nachlässe, Autographen, Handschriften",
				"AT-OeVKNAH", "Österreichischer Verbundkatalog für Nachlässe, Autographen, Handschriften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Parlamentsdirektion Parlamentsbib.", "AT-PARL",
				"Parlamentsdirektion Parlamentsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Landesschulrat für Niederösterreich Bundesstaatliche päd. Bib.", "AT-PBN",
				"Landesschulrat für Niederösterreich Bundesstaatliche pädagogische Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Kärnten Bib.", "AT-PHK", "Pädagogische Hochschule Kärnten Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Niederösterreich Bib.", "AT-PHNOe",
				"Pädagogische Hochschule Niederösterreich Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Oberösterreich Bib.", "AT-PHOOe",
				"Pädagogische Hochschule Oberösterreich Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Salzburg Bib.", "AT-PHS",
				"Pädagogische Hochschule Salzburg Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Steiermark Studienbib.", "AT-PHST",
				"Pädagogische Hochschule Steiermark Studienbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Tirol Bib.", "AT-PHT", "Pädagogische Hochschule Tirol Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Vorarlberg Bib.", "AT-PHV",
				"Pädagogische Hochschule Vorarlberg Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Päd. HS Wien Bib.", "AT-PHW", "Pädagogische Hochschule Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Paracelsus Med. Privatuni Bib.", "AT-PMUS",
				"Paracelsus Medizinische Privatuniversität Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Schottenstift - Benediktinerabtei Unserer Lieben Frau zu den Schotten Bib., Archiv, Sammlungen",
				"AT-SCHOTTEN",
				"Schottenstift - Benediktinerabtei Unserer Lieben Frau zu den Schotten Bibliothek, Archiv, Sammlungen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Sigmund Freud Privatuni Wien Bib.", "AT-SFU",
				"Sigmund Freud Privatuniversität Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Salzburger L-Arch.", "AT-SLA", "Salzburger Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Stadtgemeinde Amstetten Stadtarchiv", "AT-STAAMST",
				"Stadtgemeinde Amstetten Stadtarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Stadtarchiv Graz", "AT-STARG", "Stadtarchiv Graz");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Stadtarchiv Salzburg", "AT-STARSBG", "Stadtarchiv Salzburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Benediktinerstift Admont Bib.", "AT-STFTADMONT",
				"Benediktinerstift Admont Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Zisterzienserstift Heiligenkreuz Bib.", "AT-STFTHLGKR-BIB",
				"Zisterzienserstift Heiligenkreuz Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Stichwort: Archiv der Frauen- und Lesbenbewegung, Bib. - Dokumentation - Multimedia", "AT-STICHWORT",
				"Stichwort: Archiv der Frauen- und Lesbenbewegung, Bibliothek - Dokumentation - Multimedia");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Steiermärkisches L-Arch.", "AT-STLA", "Steiermärkisches Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Steiermärkisches L-Arch. Bib.", "AT-STLA-B",
				"Steiermärkisches Landesarchiv Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Benediktiner Erzabtei St. Peter in Salzburg Bib., Archiv und Musikalienarchiv",
				"AT-STPET", "Benediktiner Erzabtei St. Peter in Salzburg Bibliothek, Archiv und Musikalienarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Amt der Tiroler Landesreg. Landesamtsbib.", "AT-TAB",
				"Amt der Tiroler Landesregierung Landesamtsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Tiroler L-Arch.", "AT-TLA", "Tiroler Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Techn. Mus Wien", "AT-TMW", "Technisches Museum Wien");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Techn. Mus Wien Archiv", "AT-TMW-AR", "Technisches Museum Wien Archiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Techn. Mus Wien Bib.", "AT-TMW-BIB", "Technisches Museum Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Techn. Mus Wien Öster. Mediathek", "AT-TMW-OeMTH",
				"Technisches Museum Wien Österreichische Mediathek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Graz Archiv und Dokumentation", "AT-TUG-AR",
				"Technische Universität Graz Archiv und Dokumentation");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für MuK Graz Universitätsarchiv", "AT-UAKUG",
				"Universität für Musik und darstellende Kunst Graz Universitätsarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Akad. der bildenden Künste Wien Universitätsbib.", "AT-UBABW",
				"Akademie der bildenden Künste Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für angewandte Kunst Wien Universitätsbib.", "AT-UBAW",
				"Universität für angewandte Kunst Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für Bodenkultur Universitätsbib.", "AT-UBBW",
				"Universität für Bodenkultur Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für Bodenkultur Universitätsbib. Fachbib. für Lebensmittel- und Biotechn",
				"AT-UBBW-BIO",
				"Universität für Bodenkultur Universitätsbibliothek Fachbibliothek für Lebensmittel- und Biotechn");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für Bodenkultur Universitätsbib. Hauptbib.", "AT-UBBW-HB",
				"Universität für Bodenkultur Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für Bodenkultur Universitätsbib. Fachbib. SOWIRE", "AT-UBBW-WIR",
				"Universität für Bodenkultur Universitätsbibliothek Fachbibliothek SOWIRE");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für Bodenkultur Universitätsbib. Fachbib. für Wald, Natur und Technik",
				"AT-UBBW-WNT",
				"Universität für Bodenkultur Universitätsbibliothek Fachbibliothek für Wald, Natur und Technik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Graz Universitätsbib.", "AT-UBG",
				"Universität Graz Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Graz Universitätsbib. Depotbib.", "AT-UBG-DEPO",
				"Universität Graz Universitätsbibliothek Depotbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Graz Universitätsbib. Hauptbib.", "AT-UBG-HB",
				"Universität Graz Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Graz Universitätsbib. Fachbib. für Mathematik", "AT-UBG-SH",
				"Universität Graz Universitätsbibliothek Fachbibliothek für Mathematik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Kunstuni Linz. Uni für künstlerische und industrielle Gestaltung Universitätsbi", "AT-UBGL",
				"Kunstuniversität Linz. Universität für künstlerische und industrielle Gestaltung Universitätsbi");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Innsbruck Universitätsbib.", "AT-UBI",
				"Universität Innsbruck Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Innsbruck Universitäts- und Landesbib. Tirol Hauptbib.", "AT-UBI-HB",
				"Universität Innsbruck Universitäts- und Landesbibliothek Tirol Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Klagenfurt Universitätsbib.", "AT-UBK",
				"Universität Klagenfurt Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Linz Universitätsbib.", "AT-UBL",
				"Universität Linz Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Linz Universitätsbib. Hauptbib.", "AT-UBL-HB",
				"Universität Linz Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für MuK Graz Universitätsbib.", "AT-UBMG",
				"Universität für Musik und darstellende Kunst Graz Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für MuK Graz Universitätsbib.", "AT-UBMG-HB",
				"Universität für Musik und darstellende Kunst Graz Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Mozarteum Salzburg Universitätsbib.", "AT-UBMS",
				"Universität Mozarteum Salzburg Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Mozarteum Salzburg Universitätsbib. Hauptbib.", "AT-UBMS-HB",
				"Universität Mozarteum Salzburg Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Mozarteum Salzburg Universitätsbib. Abt. Musikerziehung", "AT-UBMS-MUS",
				"Universität Mozarteum Salzburg Universitätsbibliothek Abt. Musikerziehung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Mozarteum Salzburg Orff-Inst.", "AT-UBMS-ORFF",
				"Universität Mozarteum Salzburg Orff-Institut");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Graz Bib.", "AT-UBMUG", "Medizinische Universität Graz Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Montanuni Leoben Universitätsbib.", "AT-UBMUL",
				"Montanuniversität Leoben Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Montanuni Leoben Universitätsbib. Fachbib. für Geowissenschaften",
				"AT-UBMUL-FBG", "Montanuniversität Leoben Universitätsbibliothek Fachbibliothek für Geowissenschaften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Montanuni Leoben Universitätsbib. Hauptbib.", "AT-UBMUL-HB",
				"Montanuniversität Leoben Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Montanuni Leoben Universitätsbib. Rohstoff- und Werkstoffzentrum - Bib.",
				"AT-UBMUL-RWZ",
				"Montanuniversität Leoben Universitätsbibliothek Rohstoff- und Werkstoffzentrum - Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Wien Universitätsbib.", "AT-UBMUW",
				"Medizinische Universität Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Wien Universitätsbib. Wissenschaftliche Bib. im Allgemeinen K",
				"AT-UBMUW-100",
				"Medizinische Universität Wien Universitätsbibliothek Wissenschaftliche Bibliothek im Allgemeinen K");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Wien Universitätsbib. Zweigbib. Zahnmedizin", "AT-UBMUW-360",
				"Medizinische Universität Wien Universitätsbibliothek Zweigbibliothek Zahnmedizin");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Wien Universitätsbib. Zweigbib. Krebsforschung", "AT-UBMUW-400",
				"Medizinische Universität Wien Universitätsbibliothek Zweigbibliothek Krebsforschung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Wien Universitätsbib. Zweigbib. Hirnforschung", "AT-UBMUW-500",
				"Medizinische Universität Wien Universitätsbibliothek Zweigbibliothek Hirnforschung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Med. Uni Wien Universitätsbib. Zweigbib. Geschichte der Medizin",
				"AT-UBMUW-900",
				"Medizinische Universität Wien Universitätsbibliothek Zweigbibliothek Geschichte der Medizin");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für MuK Wien Universitätsbib.", "AT-UBMW",
				"Universität für Musik und darstellende Kunst Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Salzburg Universitätsbib.", "AT-UBS",
				"Universität Salzburg Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Graz Universitätsbib.", "AT-UBTUG",
				"Technische Universität Graz Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Graz Universitätsbib. Fachbib. für Chemie I", "AT-UBTUG-FBCH1",
				"Technische Universität Graz Universitätsbibliothek Fachbibliothek für Chemie I");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Graz Universitätsbib. Fachbib. für Geodäsie und Mathematik",
				"AT-UBTUG-FBGM",
				"Technische Universität Graz Universitätsbibliothek Fachbibliothek für Geodäsie und Mathematik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Graz Universitätsbib. Hauptbib.", "AT-UBTUG-HB",
				"Technische Universität Graz Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Wien Universitätsbib.", "AT-UBTUW",
				"Technische Universität Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Wien Universitätsbib. Fachbib. Chemie und Maschinenbau CheMaB",
				"AT-UBTUW-CH",
				"Technische Universität Wien Universitätsbibliothek Fachbibliothek Chemie und Maschinenbau CheMaB");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Wien Universitätsbib. Hauptbib.", "AT-UBTUW-HB",
				"Technische Universität Wien Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "TU Wien Universitätsbib. Fachbib. für Mathematik und Physik", "AT-UBTUW-MATH",
				"Technische Universität Wien Universitätsbibliothek Fachbibliothek für Mathematik und Physik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Veterinärmed. Uni Wien Universitätsbib.", "AT-UBVUW",
				"Veterinärmedizinische Universität Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Veterinärmed. Uni Wien Universitätsbib. Hauptbib.", "AT-UBVUW-UB",
				"Veterinärmedizinische Universität Wien Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien", "AT-UBW", "Universität Wien Bibliotheks- und Archivwesen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Katholische und Evangelische T", "AT-UBW-001",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Katholische und Evangelische T");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Hauptbib.", "AT-UBW-002",
				"Universität Wien Bibliotheks- und Archivwesen Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Rechtswissenschaften", "AT-UBW-005",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Rechtswissenschaften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Soziologie und Politikwissensc", "AT-UBW-007",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Soziologie und Politikwissensc");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Wirtschaftswissenschaften und", "AT-UBW-008",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Wirtschaftswissenschaften und");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Öster. Zentralbib. für Physik & Fach", "AT-UBW-071",
				"Universität Wien Bibliotheks- und Archivwesen Österreichische Zentralbibliothek für Physik & Fach");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Botanik", "AT-UBW-073",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Botanik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Anglistik und Amerikanistik", "AT-UBW-074",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Anglistik und Amerikanistik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Geographie und Regionalforschu", "AT-UBW-075",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Geographie und Regionalforschu");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Geschichtswissenschaften", "AT-UBW-078",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Geschichtswissenschaften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Südasien-, Tibet- und Buddhis", "AT-UBW-079",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Südasien-, Tibet- und Buddhis");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Alte Geschichte", "AT-UBW-081",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Alte Geschichte");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Byzantinistik und Neogräzist", "AT-UBW-085",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Byzantinistik und Neogräzist");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Translationswissenschaft", "AT-UBW-086",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Translationswissenschaft");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Biologie", "AT-UBW-087",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Biologie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Ostasienwissenschaften", "AT-UBW-088",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Ostasienwissenschaften");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Judaistik", "AT-UBW-089",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Judaistik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Klassische Philologie, Mittel-", "AT-UBW-090",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Klassische Philologie, Mittel-");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Osteuropäische Geschichte und", "AT-UBW-097",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Osteuropäische Geschichte und");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Bildungswissenschaft, Sprachwi", "AT-UBW-098",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Bildungswissenschaft, Sprachwi");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Publizistik- und Kommunikation", "AT-UBW-100",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Publizistik- und Kommunikation");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Romanistik", "AT-UBW-102",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Romanistik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Theater-, Film- und Medienwiss", "AT-UBW-106",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Theater-, Film- und Medienwiss");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Astronomie", "AT-UBW-107",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Astronomie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Kultur- und Sozialanthropologi", "AT-UBW-113",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Kultur- und Sozialanthropologi");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Europäische Ethnologie", "AT-UBW-114",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Europäische Ethnologie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Zeitgeschichte", "AT-UBW-116",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Zeitgeschichte");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Kunstgeschichte", "AT-UBW-117",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Kunstgeschichte");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Universitätsbib. Fachbib. für Mathematik, Statistik und Informatik",
				"AT-UBW-118",
				"Universität Wien Universitätsbibliothek Fachbibliothek für Mathematik, Statistik und Informatik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Musikwissenschaft", "AT-UBW-120",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Musikwissenschaft");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Philosophie", "AT-UBW-128",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Philosophie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Psychologie", "AT-UBW-132",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Psychologie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Sportwissenschaft", "AT-UBW-134",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Sportwissenschaft");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Finno-Ugristik", "AT-UBW-137",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Finno-Ugristik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Germanistik, Nederlandistik un", "AT-UBW-145",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Germanistik, Nederlandistik un");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Afrikawissenschaften und Orien", "AT-UBW-146",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Afrikawissenschaften und Orien");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Archäologien und Numismatik", "AT-UBW-155",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Archäologien und Numismatik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Erdwissenschaften und Meteorol", "AT-UBW-160",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Erdwissenschaften und Meteorol");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni Wien Fachbereichsbib. Pharmazie und Ernährungswisse", "AT-UBW-161",
				"Universität Wien Bibliotheks- und Archivwesen Fachbereichsbibliothek Pharmazie und Ernährungswisse");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wirtschaftsuni Wien Universitätsbib.", "AT-UBWW",
				"Wirtschaftsuniversität Wien Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Uni für Bodenkultur Universitätsbib. Fachbereich Landschaftsplanung",
				"AT-UBWW-874", "Universität für Bodenkultur Universitätsbibliothek Fachbereich Landschaftsplanung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wirtschaftsuni Wien Universitätsbib. Hauptbib.", "AT-UBWW-HB",
				"Wirtschaftsuniversität Wien Universitätsbibliothek Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Verbund für Bildung und Kultur", "AT-VBK", "Verbund für Bildung und Kultur");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Verein zur Förderung der Informationswissenschaft", "AT-VFI",
				"Verein zur Förderung der Informationswissenschaft");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Öster. Mus für Volkskunde Bib.", "AT-VKW",
				"Österreichisches Museum für Volkskunde Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Vorarlberger L-Arch.", "AT-VLA", "Vorarlberger Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Amt der Vorarlberger Landesreg. Amtsbib.", "AT-VLAB",
				"Amt der Vorarlberger Landesregierung Amtsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Vorarlberger Landesbib.", "AT-VLB", "Vorarlberger Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Franz Michael Felder-Archiv", "AT-VLB-FMFA", "Franz Michael Felder-Archiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Mus für Völkerkunde Wien Bib.", "AT-VMW",
				"Museum für Völkerkunde Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wiener Wiesenthal Inst. für Holocaust-Studien (VWI) Bib.", "AT-VWIHS",
				"Wiener Wiesenthal Institut für Holocaust-Studien (VWI) Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wienbib. im Rathaus", "AT-WBR", "Wienbibliothek im Rathaus");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX",
				"Dr.-Wilfried-Haslauer-Bib. (Forschungsinstitut für Politisch-Historische Studien)", "AT-WHBIB",
				"Dr.-Wilfried-Haslauer-Bibliothek (Forschungsinstitut für Politisch-Historische Studien)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wirtschaftskammer Wien Bib.", "AT-WHK", "Wirtschaftskammer Wien Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Zisterzienserstift Wilhering Musikarchiv", "AT-WIL-ARM",
				"Zisterzienserstift Wilhering Musikarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wiener Stadt- und L-Arch.", "AT-WSTLA", "Wiener Stadt- und Landesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wiener Stadt- und L-Arch. Archivbib.", "AT-WSTLA-B",
				"Wiener Stadt- und Landesarchiv Archivbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Wirtschaftsuni Wien Universitätsarchiv", "AT-WUW-AR",
				"Wirtschaftsuniversität Wien Universitätsarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXX", "Zentrum für Erinnerungskultur und Geschichtsforschung (ZEG) Archiv", "AT-ZEG",
				"Zentrum für Erinnerungskultur und Geschichtsforschung (ZEG) Archiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "AT-OBV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1600", "Bern NB", "CH-000001-5", "Schweizerische Nationalbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000001-5");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2102", "Thurgau Kantonsbibl.", "CH-000086-2", "Kantonsbibliothek Thurgau");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000001-5");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1775", "Zuerich SIK", "CH-000479-X", "Zuerich SIK");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000006-1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2103", "Eisenbibliothek Schlatt", "CH-000728-7",
				"Eisenbibliothek - Stiftung der Georg Fischer AG");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1769", "Bern HLS Schweiz", "CH-001499-7",
				"Historisches Lexikon der Schweiz (HLS), Zentralredaktion");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000001-5");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1771", "Bern Foto CH", "CH-001651-4", "Bern Foto CH");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000001-5");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1770", "Bern DoDIS", "CH-001661-0", "Bern DoDIS");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000001-5");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2104", "NB, e-codices", "CH-001667-5", "NB, e-codices");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000001-5");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1868", "Zuerich, GTA", "CH-001803-8", "Zuerich, GTA");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-000006-1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1885", "Schweizer NB", "CH-001821-6", "Schweizer NB");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-001821-6");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("35433", "Aargauer Kantonsbibliothek", "CH-AaAKBLR",
				"Aargauer Kantonsbibliothek. GND-Lokalredaktion");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-AaAKBLR");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1776", "Lokalredaktion UB Basel, f ", "CH-BaUGNF", "Basel UB Portraitsammlung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-BaUGNF");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "Lokalredaktion HAN", "CH-BaUGNH", "Basel UB Portraitsammlung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-BaUGNH");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "Lokalredaktion UB Basel, s", "CH-BaUGNS", "Basel UB Portraitsammlung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-BaUGNS");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "Lokalredaktion UB Basel Verbund", "CH-BaUGNV", "Basel UB Portraitsammlung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-BaUGNV");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41359", "UB Bern", "CH-BeUB", "Universitätsbibliothek Bern");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-BeUB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41680", "Kantonsbibliothek Graubünden", "CH-ChKBLR",
				"Kantonsbibliothek Graubünden. GND-Lokalredaktion");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ChKBLR");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1610", "IDS-SE", "CH-IDSGNDS", "IDS-SE");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-IDSGNDS");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("8478", "Luzern ZHB", "CH-LnZHB", "Zentral- und Hochschulbibliothek Luzern");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-LnZHB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41679", "Kantonsbibliothek Vadiana", "CH-SgKBLR",
				"Kantonsbibliothek Vadiana. GND Lokalredaktion");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-SgKBLR");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41405", "UB St. Gallen", "CH-SgUB", "Universität St. Gallen, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-SgUB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41411", "Lokalredaktion ETH Zürich, f", "CH-ZuETHB",
				"ETH Zürich, ETH-Bibliothek, Redaktion Normdatei");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ZuETHB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41417", "Lokalredaktion NEBIS VZ, f", "CH-ZuETHN",
				"ETH-Bibliothek, NEBIS-Verbundzentrale");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ZuETHN");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41432", "Lokalredaktion Uni Zürich, f", "CH-ZuUH",
				"Universität Zürich, Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ZuUH");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41421", "Lokalredaktion ZB Zürich, f", "CH-ZuZBF",
				"Zentralbibliothek Zürich, Formalerschliessung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ZuZBF");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("41456", "ZB Zürich, s", "CH-ZuZBS", "Zentralbibliothek Zürich, Sacherschliessung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ZuZBS");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0001", "Berlin SBB", "DE-1",
				"Staatsbibliothek zu Berlin - Preußischer Kulturbesitz, Haus Unter den Linden");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-1");
		bibliothek.addRedaktion(RedaktionsTyp.GKD, "DE-600");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0100", "Stuttgart-Hoh. KIM", "DE-100",
				"Kommunikations-, Informations- und Medienzentrum der Universität Hohenheim");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1923", "DNB", "DE-101", "Deutsche Nationalbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-101");
		bibliothek.addRedaktion(RedaktionsTyp.GKD, "DE-600");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1015", "Darmstadt Ev. HS", "DE-1015",
				"Evangelische Hochschule Darmstadt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0104", "Clausthal-Z. UB", "DE-104", "Universitätsbibliothek Clausthal");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-600");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0105", "Freiberg TU BA", "DE-105",
				"Technische Universität Bergakademie Freiberg, Bibliothek 'Georgius Agricola'");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2008", "Bonn FIZ Bundeswehr", "DE-1073",
				"Fachinformationszentrum der Bundeswehr (FIZBw) - Ausleihservice Hardthöhe");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-605");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1999", "Berlin BGK", "DE-109",
				"Zentral- und Landesbibliothek Berlin, Haus Amerika-Gedenkbibliothek und Haus Berliner Stadtbibliothek sowie: Senatsbibliothek Berlin");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-609");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1158", "Guestrow FH oeff. Verw.", "DE-1158",
				"Fachhochschule für öffentliche Verwaltung, Polizei und Rechtspflege des Landes Mecklenburg-Vorpommern, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0012", "Muenchen BSB", "DE-12", "Bayerische Staatsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.GKD, "DE-604");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0125", "Zwickau RSB", "DE-125", "Ratsschulbibliothek, Wissenschaftliche Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0014", "Dresden SLUB", "DE-14",
				"Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0015", "Leipzig UB", "DE-15", "Universitätsbibliothek Leipzig");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0016", "Heidelberg UB", "DE-16", "Universitätsbibliothek Heidelberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0016", "Heidelberg HSbibliographie", "DE-16-250",
				"HeiBIB - Die Heidelberger Universitätsbibliographie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0016", "Heidelberg UB, SAI, SE", "DE-16-77",
				"Centre for Asian and Transcultural Studies (CATS), Abteilung Südasien");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0017", "Darmstadt ULB", "DE-17",
				"TU Darmstadt, Universitäts- und Landesbibliothek - Stadtmitte");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-17");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0018", "Hamburg SUB", "DE-18",
				"Staats- und Universitätsbibliothek Hamburg Carl von Ossietzky");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.SE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.SPRACH, "DE-18");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2148", "Hamburg IFSH", "DE-18-226",
				"Institut für Friedensforschung und Sicherheitspolitik an der Universität Hamburg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1838", "Hamburg HS Musik", "DE-18-258",
				"Hochschule für Musik und Theater, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2130", "Hamburg HfBK", "DE-18-26", "Hochschule für Bildende Künste, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0018", "Hamburg HAW", "DE-18-302",
				"Hochschule für Angewandte Wissenschaften Hamburg, Hochschulinformations- und Bibliotheksservice (HIBS), Fachbibliothek Technik, Wirtschaft, Informatik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0018", "Hamburg SUB Zentr. Normd.", "DE-18-312",
				"Zentrale Stelle für Normdaten der Fachbibliotheken der Universität Hamburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-18");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0018", "Hamburg UKE", "DE-18-64",
				"Universitätsklinikum Hamburg-Eppendorf, Ärztliche Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-18");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0180", "Mannheim UB", "DE-180", "Universitätsbibliothek Mannheim");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1834", "Mannheim MZES", "DE-180-4-2",
				"Mannheimer Zentrum für Europäische Sozialforschung, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2175", "Leipzig IFL GZB", "DE-185",
				"Leibniz-Institut für Länderkunde, Geographische Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1866", "Bochum HSG", "DE-1866", "Hochschule für Gesundheit, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1869", "Nuertingen HKT-Bibl.", "DE-1869",
				"Hochschule für Wirtschaft und Umwelt Nürtingen-Geislingen, Bibliothek Campus Hauber");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1871", "Hamm ZfW", "DE-1871", "Zentrum für Wissensmanagement, Bibliothek Hamm");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0188", "Berlin UB FU", "DE-188",
				"Freie Universität Berlin, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-602");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0019", "Muenchen UB", "DE-19", "Universitätsbibliothek der LMU München");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-19");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1926", "Lippstadt ZfW", "DE-1926",
				"Zentrum für Wissensmanagement, Bibliothek Lippstadt");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1705", "Koblenz Bundesarchiv", "DE-1958", "Bundesarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-604");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2100", "HG Kreisarchiv", "DE-1962", "Kreisarchiv des Hochtaunuskreises");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1971", "FFM Weltkulturen Mus.", "DE-1971", "Weltkulturen Museum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1973", "FFM STAEDELSCHULE", "DE-1973",
				"Staatliche Hochschule für Bildende Künste, Städelschule, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1975", "FFM MUS. MOD. KUNST", "DE-1975", "Museum für Moderne Kunst, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1978", "FFM MUS. KOMMUNIKATION", "DE-1978",
				"Museum für Kommunikation Frankfurt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1979", "FFM JUED. MUS.", "DE-1979",
				"Jüdisches Museum der Stadt Frankfurt am Main, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0020", "Wuerzburg UB", "DE-20", "Universitätsbibliothek Würzburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-20");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0204", "Berlin IAI", "DE-204",
				"Ibero-Amerikanisches Institut Preußischer Kulturbesitz, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-204");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0206", "Kiel ZBW", "DE-206",
				"ZBW - Leibniz-Informationszentrum Wirtschaft, Standort Kiel");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0208", "Karlsruhe BGH", "DE-208", "Bibliothek des Bundesgerichtshofs");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0021", "Tuebingen UB", "DE-21",
				"Universitätsbibliothek der Eberhard Karls Universität");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1832", "Tübingen Brechtbau-Bib", "DE-21-108", "Brechtbau-Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1821", "Tuebingen JS", "DE-21-24", "Juristisches Seminar, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0212", "Stuttgart Inst. Auslandsbez", "DE-212",
				"Institut für Auslandsbeziehungen, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2166", "Rottweil Kreisarchiv", "DE-2156",
				"Landratsamt Rottweil, Stabsbereich Archiv, Kultur, Tourismus");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2173", "Halle/S IWH", "DE-2173",
				"Leibniz-Institut für Wirtschaftsforschung Halle, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2101", "MPI Aesthetik", "DE-2177",
				"Max-Planck-Institut für empirische Ästhetik, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2112", "Frankfurt/M CIBEDO", "DE-2287",
				"Christlich-Islamische Begegnungs- und Dokumentationsstelle, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0023", "Wolfenbuettel HAB", "DE-23", "Herzog August Bibliothek Wolfenbüttel");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-12");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("4048", "Witzenhausen Arch Jugendbew", "DE-2355",
				"Archiv der deutschen Jugendbewegung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2133", "Mainz Stadtarchiv Bibliothek", "DE-2397", "Stadtarchiv Mainz, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0024", "Stuttgart Wuertt. LB", "DE-24", "Württembergische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2155", "Bad Oldesloe Kreisarchiv", "DE-2410", "Kreisarchiv Stormarn");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2144", "Bonn BICC", "DE-2421",
				"Internationales Konversionszentrum Bonn (BICC), Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2138", "HSC", "DE-2489",
				"Handschriftencensus (HSC) - Kompetenzzentrum Deutschsprachige Handschriften des Mittelalters");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2152", "DHI Rom", "DE-2491", "Deutsches Historisches Institut in Rom, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0025", "Freiburg UB", "DE-25", "Universitätsbibliothek Freiburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2165", "Frankfurt/M Fritz Bauer Inst.", "DE-2508",
				"Bibliothek des Fritz Bauer Instituts zur Geschichte und Wirkung des Holocaust");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0253", "Braunschweig FAL", "DE-253",
				"Thünen-Institut, Bundesforschungsinstitut für Ländliche Räume, Wald und Fischerei, Zentrum für Informationsmanagement");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0255", "Muenchen ZIKG", "DE-255", "Zentralinstitut für Kunstgeschichte, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-612");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0026", "Giessen UB", "DE-26", "Universitätsbibliothek Gießen");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-26");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0027", "Jena THULB", "DE-27", "Thüringer Universitäts- und Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0028", "Rostock UB", "DE-28", "Universitätsbibliothek Rostock");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-28");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0281", "Berlin Bundestag Bibliothek", "DE-281", "Deutscher Bundestag, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-281");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0289", "Ulm UB (KIZ)", "DE-289",
				"Universität Ulm, Kommunikations- und Informationszentrum, Bibliotheksservices");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0029", "Erlangen UB", "DE-29",
				"Universitätsbibliothek Erlangen-Nürnberg, Hauptbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-29");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0291", "Saarbruecken UuLB", "DE-291",
				"Saarländische Universitäts- und Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0296", "Singen Hegau-B", "DE-296", "Hegau-Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0003", "Halle ULB", "DE-3",
				"Universitäts- und Landesbibliothek Sachsen-Anhalt / Zentrale");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.SPRACH, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0030", "Frankfurt/M StuUB", "DE-30",
				"Universitätsbibliothek J. C. Senckenberg, Zentralbibliothek (ZB)");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-30");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0031", "Karlsruhe BLB", "DE-31", "Badische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0032", "Weimar HAAB", "DE-32",
				"Klassik Stiftung Weimar / Herzogin Anna Amalia Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0033", "Schwerin LBMV", "DE-33",
				"Landesbibliothek Mecklenburg-Vorpommern Günther Uecker im Landesamt für Kultur und Denkmalpflege");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0034", "Kassel GHB", "DE-34",
				"Universitätsbibliothek Kassel, Landesbibliothek und Murhardsche Bibliothek der Stadt Kassel");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-34");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0035", "Hannover NLB", "DE-35",
				"Gottfried Wilhelm Leibniz Bibliothek - Niedersächsische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0352", "Konstanz UB", "DE-352",
				"Universität Konstanz, Kommunikations-, Informations-, Medienzentrum (KIM)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0354", "Hannover Med. HS", "DE-354", "Medizinische Hochschule Hannover, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0355", "Regensburg UB", "DE-355", "Universitätsbibliothek Regensburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-355");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0036", "Mainz StBi", "DE-36", "Wissenschaftliche Stadtbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-36");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0038", "Köln UuSTB", "DE-38",
				"Universitäts- und Stadtbibliothek Köln, Hauptabteilung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-605");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0384", "Augsburg UB", "DE-384", "Universitätsbibliothek Augsburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-384");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0004", "Marburg UB", "DE-4", "Universität Marburg, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-4");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0043", "Wiesbaden LB", "DE-43",
				"Hochschul- und Landesbibliothek RheinMain, Rheinstraße");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-43");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0045", "Oldenburg LB", "DE-45", "Landesbibliothek Oldenburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0046", "Bremen SuUB", "DE-46", "Staats- und Universitätsbibliothek Bremen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0460", "Darmstadt Staatsarchiv", "DE-460",
				"Hessisches Staatsarchiv Darmstadt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0468", "Wuppertal UB", "DE-468", "Universitätsbibliothek Wuppertal");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-605");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0473", "Bamberg UB", "DE-473", "Universitätsbibliothek Bamberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-473");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2123", "Bremen StB", "DE-478", "Stadtbibliothek Bremen, Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0048", "Luebeck StB", "DE-48", "Bibliothek der Hansestadt Lübeck");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0517", "Potsdam UB", "DE-517", "Universität Potsdam, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0521", "Frankfurt/O UB", "DE-521",
				"Europa-Universität Viadrina, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-521");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0522", "Brandenburg FH", "DE-522",
				"Technische Hochschule Brandenburg, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-521");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0523", "Berlin HTW", "DE-523",
				"Hochschule für Technik und Wirtschaft Berlin, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-521");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0526", "Wildau TH", "DE-526",
				"Technische Hochschule Wildau [FH], Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-521");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0540", "Dresden HfBK FE", "DE-540",
				"Hochschule für Bildende Künste Dresden, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0547", "Erfurt UB", "DE-547",
				"Universitätsbibliothek Erfurt / Forschungsbibliothek Gotha, Universitätsbibliothek Erfurt");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0056", "Braunschweig StBi", "DE-56", "Stadtbibliothek Braunschweig");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0563", "Rostock HS Musik", "DE-563",
				"Hochschule für Musik und Theater Rostock, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1500", "SWB", "DE-576", "Bibliotheksservice-Zentrum Baden-Württemberg (BSZ)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1510", "BSZ Baden-Wuerttemberg - PND", "DE-576-2",
				"früher: Konstanz; Bibliotheksservice-Zentrum Baden-Württemberg, Autorenredaktion");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2125", "Konstanz BSZ/MusIS", "DE-576-3",
				"Bibliotheksservice-Zentrum Baden-Württemberg (BSZ) / MusIS");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576-3");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1704", "Bonn GESIS", "DE-587b",
				"früher: GESIS  - Leibniz-Institut für Sozialwissenschaften,  Bibliothek Bonn");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-587b");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0059", "Chemnitz STB", "DE-59", "Stadt Chemnitz, Kulturbetrieb, Stadtbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1995", "ZDB", "DE-600", "Zeitschriftendatenbank (ZDB)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-600");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("7777", "GBV", "DE-601", "Verbundzentrale des GBV (VZG)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "KOBV", "DE-602", "Kooperativer Bibliotheksverbund Berlin-Brandenburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-602");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0600", "HeBIS", "DE-603", "Hessisches BibliotheksInformationsSystem HeBIS");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "BVB", "DE-604", "Bibliotheksverbund Bayern");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-604");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0500", "HBZ", "DE-605",
				"hbz - Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-605");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2174", "Berlin VöBB", "DE-609",
				"Verbund der  Öffentlichen Bibliotheken Berlins - VÖBB");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-609");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0103", "SBB / ZKA", "DE-611",
				"Staatsbibliothek zu Berlin - Preußischer Kulturbesitz, Kalliope-Verbund");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-611");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1702", "Neuendetttelsau VThK (THB)", "DE-613",
				"Virtueller Katalog Theologie und Kirche");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-613");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0614", "Muenster Int.Port.Westf.Gesch", "DE-614",
				"Internet-Portal \"Westfälische Geschichte\"");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-614");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0631", "Stuttgart LABI BAW", "DE-631",
				"Landesbibliographie Baden-Württemberg, Zentralredaktion");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2105", "ExLibris", "DE-632", "Ex Libris (Deutschland) GmbH");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-632");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1758", "Brandenburgische Technische Universität Cottbus - Senftenberg", "DE-634",
				"Brandenburgische Technische Universität Cottbus - Senftenberg, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-521");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0066", "Fulda HLB", "DE-66",
				"Hochschul- und Landesbibliothek Fulda, Standort Heinrich-von-Bibra-Platz");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-66");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0068", "Kiel LB", "DE-68", "Schleswig-Holsteinische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0007", "Goettingen SUB", "DE-7",
				"Niedersächsische Staats- und Universitätsbibliothek Göttingen");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0007", "GOETTINGEN MATH. INST.", "DE-7-003",
				"Universität Göttingen, Mathematisches Institut, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0007", "Goettingen UB Bibl. d. Jur. Sem.", "DE-7-037",
				"Universität Göttingen, Zentralbibliotheken der Juristischen Fakultät");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0007", "Goettingen Sem. Dt. Phil.", "DE-7-052",
				"Universität Göttingen, Seminar für Deutsche Philologie, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1774", "Goettingen Sanskrit Woerterbuch", "DE-7-911",
				"Akademie der Wissenschaften zu Göttingen, Sanskrit-Wörterbuch, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0700", "Osnabrueck UB", "DE-700", "Universitätsbibliothek Osnabrück");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-700");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0703", "Bayreuth UB", "DE-703", "Universitätsbibliothek Bayreuth");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-703");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0705", "Hamburg UniBW (HSU)", "DE-705",
				"Helmut-Schmidt-Universität, Universität der Bundeswehr Hamburg, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0706", "Muenchen UniBW", "DE-706",
				"Universität der Bundeswehr München, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-706");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0715", "Oldenburg BIS", "DE-715",
				"Bibliotheks-und Informationssystem der Carl von Ossietzky Universität Oldenburg (BIS)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0739", "Passau UB", "DE-739", "Universitätsbibliothek Passau");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-739");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0747", "Weingarten HSB", "DE-747", "Hochschulbibliothek Weingarten");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0075", "Nuernberg StB", "DE-75", "Stadtbibliothek im Bildungscampus Nürnberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-12");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2167", "Karlsruhe HSB", "DE-751", "Hochschulbibliothek Karlsruhe (PH)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0755", "Emden/Leer FH", "DE-755", "Hochschule Emden/Leer, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0077", "Mainz UB", "DE-77", "Universität Mainz, Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-77");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0008", "Kiel UB", "DE-8", "Universitätsbibliothek Kiel, Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE_VD_17, "DE-12");
		bibliothek.addRedaktion(RedaktionsTyp.SPRACH, "DE-8");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0824", "Eichstaett UB", "DE-824", "Universitätsbibliothek Eichstätt-Ingolstadt");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-824");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0083", "Berlin, UB TU", "DE-83", "TU Berlin, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-384");
		bibliothek.addRedaktion(RedaktionsTyp.SE, "DE-188");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-83");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0830", "Hamburg UB TU", "DE-830",
				"Technische Universität Hamburg, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0832", "Koeln FHB, SE", "DE-832", "Technische Hochschule Köln, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-832");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0834", "Braunschweig HBK", "DE-834",
				"Hochschule für Bildende Künste Braunschweig, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0839", "WHV Jade Hochschule", "DE-839",
				"Jade Hochschule Wilhelmshaven/Oldenburg/Elsfleth, Campus Wilhelmshaven, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0084", "Braunschweig UB TU", "DE-84", "Universitätsbibliothek Braunschweig");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0089", "Hannover TIB/UB", "DE-89",
				"Technische Informationsbibliothek (TIB) / Leibniz-Informationszentrum Technik und Naturwissenschaften und Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0897", "Oldenburg Jade-Hochschule", "DE-897",
				"Jade Hochschule Wilhelmshaven/Oldenburg/Elsfleth, Campus Oldenburg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0897", "Elsfleth Jade HS", "DE-897-1",
				"Jade Hochschule Wilhelmshaven/Oldenburg/Elsfleth, Campus Elsfleth, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0009", "Greifswald UB", "DE-9", "Universitätsbibliothek Greifswald");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0090", "Karlsruhe UB (KIT)", "DE-90",
				"Karlsruher Institut für Technologie, KIT-Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0091", "Muenchen TU", "DE-91",
				"Technische Universität München, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-91");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0916", "Wolfenbuettel FH", "DE-916",
				"Ostfalia Hochschule für angewandte Wissenschaften, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0929", "Koblenz LB", "DE-929",
				"Landesbibliothekszentrum Rheinland-Pfalz / Rheinische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-605");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0093", "Stuttgart UB", "DE-93", "Universitätsbibliothek Stuttgart");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0095", "Hannover TierHS", "DE-95",
				"Stiftung Tierärztliche Hochschule Hannover, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0955", "Rottenburg HS Forstw", "DE-955",
				"Hochschule für Forstwirtschaft Rottenburg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1749", "Osnabrueck FH Teilb. Musik", "DE-959-2",
				"früher: Hochschule Osnabrück, Bibliothek, Teilbibliothek Musik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0960", "Hannover FHB", "DE-960", "Bibliothek der Hochschule Hannover");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0969", "Wiesbaden FHB", "DE-969", "Wiesbaden FHB");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0974", "Giessen-Friedberg FHB", "DE-974",
				"Technische Hochschule Mittelhessen, Hochschulbibliothek Gießen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0984", "Schwäbisch Gmünd HS Gestalt.", "DE-984",
				"Hochschule für Gestaltung Schwäbisch Gmünd, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0996", "Hannover HMTM/FMG", "DE-996",
				"Hochschule für Musik, Theater und Medien Hannover, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-1");
		bibliothek.addRedaktion(RedaktionsTyp.GKD, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1844", "Aurich LandschaftsB", "DE-Au3", "Landschaftsbibliothek Aurich");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1748", "Berlin PTB", "DE-B108",
				"Physikalisch-Technische Bundesanstalt, Institut Berlin, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1830", "Berlin KB", "DE-B11",
				"Staatliche Museen zu Berlin, Preußischer Kulturbesitz, Kunstbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.SE, "DE-1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1718", "Berlin BfR", "DE-B12", "Bundesinstitut für Risikobewertung, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-B12");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1781", "Muencheberg Senckenberg DEI", "DE-B15",
				"Senckenberg Deutsches Entomologisches Institut, Entomologische Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2145", "Bonn DIE", "DE-B1503",
				"Deutsches Institut für Entwicklungspolitik, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1847", "MPI Bildungsforsch.", "DE-B1532",
				"Max-Planck-Institut für Bildungsforschung, Bibliothek und wissenschaftliche Information");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1727", "Berlin Dt. Inst.f.Menschenr.,FE", "DE-B1547",
				"Deutsches Institut für Menschenrechte, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1715", "Berlin Stiftung Aufarbeitung", "DE-B1548",
				"Stiftung zur Aufarbeitung der SED-Diktatur, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-B1548");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1711", "Berlin DZA", "DE-B1562",
				"Deutsches Zentrum für Altersfragen e.V. (DZA), Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2143", "Berlin SWP", "DE-B1567",
				"Stiftung Wissenschaft und Politik (SWP), Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1723", "Berlin Hertie School", "DE-B1570",
				"Hertie School, Library and Information Services");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1777", "Berlin Wikimedia Deutschland", "DE-B1592",
				"Wikimedia Deutschland e.V., Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-B1592");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1753", "Berlin UdK", "DE-B170",
				"Universität der Künste Berlin, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-384");
		bibliothek.addRedaktion(RedaktionsTyp.SE, "DE-188");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2114", "Berlin AA", "DE-B19",
				"Auswärtiges Amt, Referat 116, Bibliothek, Informationsvermittlung");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1799", "Heidelberg MPIL", "DE-B208",
				"Max-Planck-Institut für ausländisches öffentliches Recht und Völkerrecht, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1823", "Hamburg MPI Priv.", "DE-B212",
				"Max-Planck-Institut für ausländisches und internationales Privatrecht, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2170", "Berlin MPI Wiss.-Gesch.", "DE-B2226",
				"Max-Planck-Institut für Wissenschaftsgeschichte, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1845", "Berlin AdW B", "DE-B4",
				"Berlin-Brandenburgische Akademie der Wissenschaften, Akademiebibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1846", "Berlin AdW GRA", "DE-B4-556",
				"Berlin, Berlin-Brandenburgische Akademie der Wissenschaften, Akademiebibliothek, Teilbibliothek Griechisch-römische Altertumskunde");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1828", "Berlin GSTA", "DE-B41",
				"Geheimes Staatsarchiv Preußischer Kulturbesitz, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.SE, "DE-1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("3032", "Berlin BBF/DIPF", "DE-B478",
				"BBF | Bibliothek für Bildungsgeschichtliche Forschung in Berlin");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1829", "Berlin SIM", "DE-B763",
				"Staatliches Institut für Musikforschung - Preußischer Kulturbesitz, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-601");
		bibliothek.addRedaktion(RedaktionsTyp.SE, "DE-1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1806", "Leipzig BVerwG", "DE-B791", "Bundesverwaltungsgericht, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2153", "Bautzen Sorbisches Inst.", "DE-Bn1",
				"Sorbisches Institut e. V., Serbski institut z. t., Sorbische Zentralbibliothek, Serbska centralna biblioteka");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2106", "Berlin Bundesrat", "DE-Bo151", "Bundesrat, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2141", "Boppard Ev. Archivstelle", "DE-Bpd1", "Evangelische Archivstelle Boppard");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1779", "Bremen Forschungsst. Osteuropa", "DE-Bre12",
				"Forschungsstelle Osteuropa an der Universität Bremen - Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2135", "Bremen ZMT", "DE-Bre14",
				"Leibniz-Zentrum für Marine Tropenforschung (ZMT) GmbH, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1703", "Bremen Stiftung FLF / DaSinD", "DE-Bre15",
				"Stiftung Frauen-Literatur-Forschung e.V.");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Bre15");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2120", "Bremen Seeverkehrswirtschaft", "DE-Bre2",
				"Institut für Seeverkehrswirtschaft und Logistik, ISL InfoCenter/Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2124", "Bremen Ueberseemuseum", "DE-Bre3", "Übersee-Museum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1745", "Bremen La.ki.Bibl.", "DE-Bre4", "Landeskirchliche Bibliothek Bremen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1706", "Braunschweig PTB", "DE-Bs68",
				"Physikalisch-Technische Bundesanstalt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1707", "Braunschweig GEI", "DE-Bs78",
				"Georg-Eckert-Institut - Leibniz-Institut für internationale Schulbuchforschung, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1862", "Bremerhaven A.-Wegener-Inst.", "DE-Bv2",
				"Alfred-Wegener-Institut Helmholtz-Zentrum für Polar- und Meeresforschung, (AWI), Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1742", "Chemnitz UB", "DE-Ch1",
				"Technische Universität Chemnitz, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1758", "Cottbus BTU, SE", "DE-Co1", "Cottbus BTU");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-188");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1787", "Dreseden LFA Sachsen", "DE-D115",
				"Landesamt für Archäologie Sachsen, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1788", "Dresden HFM", "DE-D117",
				"Hochschule für Musik 'Carl Maria von Weber', Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1789", "Dresden SKD", "DE-D13",
				"Staatliche Kunstsammlungen Dresden, Kunstbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1751", "Dresden ISGV SAEBI", "DE-D174a",
				"Institut für Sächsische Geschichte und Volkskunde, Projekt \" Sächsische Biografie\"");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-D174a");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1836", "Dresden HAIT", "DE-D264",
				"Hannah-Arendt-Institut für Totalitarismusforschung e.V., Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1884", "FFM Rat f. Formgebung", "DE-Ds108",
				"Rat für Formgebung - German Design Council, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1831", "Darmstadt HLM", "DE-Ds82", "Hessisches Landesmuseum Darmstadt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2139", "Duesseldorf ArchBiblEvKirche", "DE-Due72",
				"Archiv der Evangelischen Kirche im Rheinland, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1759", "Eberswalde FH", "DE-Eb1",
				"Hochschule für nachhaltige Entwicklung Eberswalde, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-521");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1840", "Erfurt TFM", "DE-Ef32", "Thüringer Finanzministerium, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1729", "Emden JALB", "DE-Em2", "Johannes a Lasco Bibliothek Große Kirche Emden");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1837", "Esslingen Stadtarchiv", "DE-Ess4", "Stadtarchiv Esslingen, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1782", "Frankfurt Staedel Museum", "DE-F10", "Städel Museum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2142", "Berlin DGAP", "DE-F131",
				"Informationszentrum der Deutschen Gesellschaft für Auswärtige Politik e.V.");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1879", "FFM Kunsthandwerksmus.", "DE-F146",
				"Museum für Angewandte Kunst, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1876", "FFM Stadtgeschichte", "DE-F186",
				"Institut für Stadtgeschichte Frankfurt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2146", "Frankfurt HSFK", "DE-F197",
				"Hessische Stiftung Friedens- und Konfliktforschung, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1883", "FFM Hist. Mus.", "DE-F207", "Historisches Museum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1881", "FFM DAM", "DE-F219", "Deutsches Architekturmuseum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1878", "FFM ARCHAEOL. MUS.", "DE-F225",
				"Archäologisches Museum Frankfurt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1877", "FFM FDH/FGM", "DE-F25",
				"Freies Deutsches Hochstift / Frankfurter Goethe-Museum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("3032", "Frankfurt/M DIPF", "DE-F43",
				"DIPF | Leibniz-Institut für Bildungsforschung und Bildungsinformation, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1870", "Frankfurt/M HFMDK", "DE-F78",
				"Hochschule für Musik und Darstellende Kunst, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1794", "Freiburg ABI", "DE-Frei119",
				"Arnold-Bergstraesser-Institut für kulturwissenschaftliche Forschung, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1795", "Freiburg Augustiner-Museum", "DE-Frei123",
				"Städtische Museen Freiburg, Augustinermuseum/Museum für Neue Kunst, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2168", "Freiburg PH", "DE-Frei129",
				"Bibliothek der Pädagogischen Hochschule Freiburg/Breisgau");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2115", "Freiburg Staatsarchiv", "DE-Frei145",
				"Landesarchiv Baden-Württemberg, Abteilung Staatsarchiv Freiburg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1791", "Freiburg Caritas Bibl.", "DE-Frei26",
				"Deutscher Caritasverband e.V., Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1790", "Freiburg MFO", "DE-Frei3c",
				"Mathematisches Forschungsinstitut Oberwolfach gGmbH, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1864", "Freiburg HS Musik Bibl.", "DE-Frei50",
				"Hochschule für Musik Freiburg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1792", "Freiburg MPI CC", "DE-Frei85",
				"Max-Planck-Institut für ausländisches und internationales Strafrecht, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1793", "Freiburg DVA", "DE-Frei99", "Freiburg DVA");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1873", "Fulda Priesterseminar", "DE-Ful2",
				"Bibliothek des Bischöflichen Priesterseminars");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1756", "Gotha Schlossmuseum", "DE-G16",
				"Stiftung Schloss Friedenstein Gotha, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.FE, "DE-576");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1712", "Gatersleben IPK", "DE-Gat1",
				"Leibniz-Institut für Pflanzengenetik und Kulturpflanzenforschung (IPK), Wissenschaftliche Bibliothek / Dokumentation");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Gat1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1835", "Goerlitz OLB", "DE-Gl2",
				"Oberlausitzische Bibliothek der Wissenschaften bei den Görlitzer Sammlungen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1746", "Goettingen Otto-Hahn-Bibl.", "DE-Goe116",
				"Otto-Hahn-Bibliothek des Max-Planck-Instituts für biophysikalische Chemie");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1730", "Goettingen GWDG", "DE-Goe168",
				"Gesellschaft für wissenschaftliche Datenverarbeitung mbH Göttingen (GWDG), Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1773", "Goettingen Germ. Sacra", "DE-Goe172",
				"Akademie der Wissenschaften zu Göttingen, Germania Sacra");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1849", "Hamburger Kunsthalle", "DE-H13", "Hamburger Kunsthalle, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1855", "Hamburg, Bibliothek Geowiss.", "DE-H144",
				"Behörde für Stadtentwicklung und Wohnen / Behörde für Umwelt und Energie - Bibliothek Stadtentwicklung, Umwelt und Geologie: Sondersammlung des Geologischen Landesamtes (GLA)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1856", "Hamburg Mus. Völkerkunde", "DE-H16", "Museum am Rothenbaum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("4046", "Hamburg Bamt Seeschifffahrt", "DE-H2",
				"Bundesamt für Seeschifffahrt und Hydrographie, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1850", "Hamburg Ärztl. Verein", "DE-H20",
				"Ärztekammer Hamburg, Bibliothek des Ärztlichen Vereins");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2117", "Hamburg ZB Behoerden", "DE-H216",
				"Zentrale Bibliothek Behörden Hamburger Straße");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1714", "Hamburg MKG", "DE-H22",
				"Gerd Bucerius Bibliothek im Museum für Kunst und Gewerbe");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2147", "Hamburg GIGA", "DE-H221",
				"GIGA German Institute of Global and Area Studies; Informationszentrum - Fachbibliotheken Afrika, Nahost und Lateinamerika");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2154", "Hamburg Zeitgeschichte", "DE-H250",
				"Forschungsstelle für Zeitgeschichte in Hamburg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1851", "Museum Arbeit Hamburg", "DE-H353",
				"Stiftung Historische Museen Hamburg, Museum der Arbeit, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1732", "Hamburg Bucerius Law School", "DE-H360",
				"Bucerius Law School, Hochschule für Rechtswissenschaft, \" Hengeler Mueller\"-Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1852", "Mus. Hamburg. Geschichte", "DE-H77",
				"Stiftung Historische Museen Hamburg, Museum für Hamburgische Geschichte, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1863", "Hamburg Nordkirchenbibliothek", "DE-H99",
				"Nordkirchenbibliothek im Bibliotheks- und Medienzentrum der Nordkirche");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2119", "Halle/S Dt Akad Naturfor", "DE-Ha2",
				"Deutsche Akademie der Naturforscher Leopoldina, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1768", "Halle/S Marienbibliothek", "DE-Ha32", "Marienbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1763", "Halle Franckesche Stiftungen", "DE-Ha33",
				"Franckesche Stiftungen, Studienzentrum August Hermann Francke, Archiv und Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1798", "Heidelberg PH", "DE-He76",
				"Bibliothek der Pädagogischen Hochschule Heidelberg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1839", "Hildesheim UB", "DE-Hil2", "Universitätsbibliothek Hildesheim");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1724", "Hildesheim Nieders. LRH", "DE-Hil5",
				"Niedersächsischer Landesrechnungshof, Bibliothek und Informationsdienste");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1717", "Hildesheim Dombibliothek", "DE-Hil6", "Dombibliothek Hildesheim");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1848", "Center for World Music", "DE-Hil8", "Center for World Music, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1783", "Bad Homburg Stadtarchiv", "DE-Hog1",
				"Stadtarchiv Bad Homburg vor der Höhe, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1827", "Hannover Ev.Luth.Landesk.", "DE-Hv111",
				"Evangelisch-Lutherisches Landeskirchenamt, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1719", "Hannover LT Nie", "DE-Hv14", "Niedersächsischer Landtag, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1733", "Ilmenau UB", "DE-Ilm1", "Universitätsbibliothek Ilmenau");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1721", "Jena FLI", "DE-J120",
				"Friedrich-Loeffler-Institut, Bundesforschungsinstitut für Tiergesundheit, Bibliothek Jena");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-J120");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1735", "Jena Thuering. OLG", "DE-J153", "Justizzentrum Jena, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1734", "Jena FH", "DE-J59",
				"Ernst-Abbe-Hochschule Jena, Wissenschaftliche Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1804", "Karlsruhe Kunsthalle", "DE-Ka23", "Staatliche Kunsthalle, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2109", "Karlsruhe Badisches Landesmus.", "DE-Ka23a",
				"Badisches Landesmuseum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1801", "Karlsruhe BVerfG", "DE-Ka26", "Bundesverfassungsgericht, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1720", "Karlsruhe BFI", "DE-Ka51",
				"Max Rubner-Institut, Bundesforschungsinstitut für Ernährung und Lebensmittel, Standort Karlsruhe, Bibliothek, Information und Dokumentation");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Ka51");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1802", "Karlsruhe HfM", "DE-Ka84", "Hochschule für Musik Karlsruhe, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1803", "Karlsruhe ZKM", "DE-Ka88",
				"Zentrum für Kunst und Medien Karlsruhe / Staatliche Hochschule für Gestaltung, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1796", "Kehl HS", "DE-Kh1",
				"Hochschule für öffentliche Verwaltung Kehl, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1757", "Kiel IFM-GEOMAR", "DE-Ki109",
				"Helmholtz-Zentrum für Ozeanforschung Kiel (GEOMAR), Bibliothek Westufer");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1713", "Kiel FH", "DE-Ki95", "Fachhochschule Kiel, Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1857", "Kassel MHK", "DE-Ks14", "Museumslandschaft Hessen Kassel, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1875", "FFM Frau und Musik", "DE-Ks15", "Archiv Frau und Musik");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1824", "Kassel documenta Archiv", "DE-Ks17", "documenta archiv, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2134", "Koethen HSB HSA", "DE-Kt1", "Hochschule Anhalt , Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1808", "Leipzig HMT", "DE-L152",
				"Hochschule für Musik und Theater 'Felix Mendelssohn Bartholdy', Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1807", "Leipzig HTWK", "DE-L189",
				"Hochschule für Technik, Wirtschaft und Kultur, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2140", "Leipzig Museum Voelkerkunde", "DE-L228",
				"Staatliche Kunstsammlungen Dresden, Museum für Völkerkunde Leipzig, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1805", "Leipzig Bach-Archiv", "DE-L326", "Bach-Archiv, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1818", "Ludwigsburg DFI", "DE-Lg3",
				"Deutsch-Französisches Institut, Frankreich-Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2151", "StadtALB", "DE-Lg4", "Stadtarchiv Ludwigsburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1861", "Loerrach Duale Hochschule BW", "DE-Loer2",
				"Duale Hochschule Baden-Württemberg Lörrach, Zentralbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1874", "Luebeck MusikHS", "DE-Lue12", "Musikhochschule Lübeck, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("3110", "Lueneburg UB", "DE-Luen4",
				"Leuphana Universität Lüneburg, Medien- und Informationszentrum, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1822", "München Goethe-Zentr.", "DE-M504",
				"Goethe-Institut e. V. Zentrale, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1772", "Muenchen BMLO", "DE-M512", "Bayerisches Musiker-Lexikon Online");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-12");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1736", "Magdeburg UB", "DE-Ma9",
				"Otto-von-Guericke-Universität, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1764", "Marbach DLA", "DE-Mar1", "Deutsches Literaturarchiv Marbach, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1728", "Marburg Hess Landesamt", "DE-Mb107",
				"Hessisches Landesamt für geschichtliche Landeskunde, Landesgeschichtliches Informationssystem Hessen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Mb107");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1750", "Marburg FSt Personalschriften FE", "DE-Mb108",
				"Forschungsstelle für Personalschriften an der Philipps-Universität Marburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Mb108");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1752", "Marburg Ev. HS Tabor", "DE-Mb109",
				"Evangelische Hochschule Tabor, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Mb109");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1765", "Marburg DFG-Projekt Thenom", "DE-Mb111",
				"DFG-Projekt Thenom (Thesaurus Nominum Auctorum et Mortuorum) an der Philipps-Universität Marburg, FB 06, Sozial- u. Wirtschaftsgeschichte");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Mb111");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1766", "Marburg Foto", "DE-Mb112",
				"Deutsches Dokumentationszentrum für Kunstgeschichte - Bildarchiv Foto Marburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Mb112");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1809", "Mannheim MUHO", "DE-Mh31",
				"Staatliche Hochschule für Musik und Darstellende Kunst Mannheim, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1810", "Mannheim TECHNOSEUM", "DE-Mh34",
				"TECHNOSEUM Landesmuseum für Technik und Arbeit, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1859", "Mannheim IDS WGL", "DE-Mh39",
				"Leibniz-Institut für Deutsche Sprache (IDS), Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2110", "Mannheim Stadtarchiv", "DE-Mh40", "MARCHIVUM");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1785", "Mittweida HS", "DE-Mit1", "Hochschule Mittweida (FH), Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1784", "Mainz NHM Naturhistor. Museum", "DE-Mz119",
				"Naturhistorisches Museum und Landessammlung für Naturkunde Mainz, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2164", "Mainz Martinus-Bibl.", "DE-Mz2",
				"Martinus-Bibliothek, Wissenschaftliche Diözesanbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1825", "Nuernberg GNM", "DE-N1", "Germanisches Nationalmuseum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1882", "OF Klingspor-Mus.", "DE-Of2", "Klingspor-Museum, Bibliothek und Archiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1797", "Offenburg HS", "DE-Ofb1",
				"Hochschule Offenburg, University of Applied Sciences, Bibliothek Campus Offenburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1737", "Oldenburg Ev.Luth. Oberk.", "DE-Old3",
				"Evangelisch-Lutherischer Oberkirchenrat, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1767", "Bad Koesen Landesschule Pforte", "DE-Pf1",
				"Landesschule Pforte, Archiv und Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-Pf1");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1762", "Potsdam TFA", "DE-Po84", "Theodor-Fontane-Archiv Universität Potsdam");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-101");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1854", "Dummerstorf Inst Nutztierbiol.", "DE-R48",
				"Leibniz-Institut für Nutztierbiologie, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1819", "Rottenburg DioezesanB", "DE-Rot2", "Diözesanbibliothek Rottenburg");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1833", "Reutlingen HSB", "DE-Rt2", "Hochschulbibliothek Reutlingen (Lernzentrum)");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1520", "Reutlingen EKZ", "DE-Rt5", "ekz.bibliotheksservice GmbH");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1865", "Saarbruecken Stift. Kulturbesitz", "DE-Sa27",
				"Stiftung Saarländischer Kulturbesitz, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2116", "Salem KrsB", "DE-Sam1",
				"Landratsamt Bodenseekreis, Kulturamt, Kreisbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1738", "Schmalkalden FH", "DE-Shm2",
				"Hochschule  Schmalkalden, Cellarius Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1820", "Sigmaringen STA", "DE-Sig1",
				"Landesarchiv Baden-Württemberg, Abteilung Staatsarchiv Sigmaringen, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1826", "Speyer DHV", "DE-Sp3",
				"Deutsche Universität für Verwaltungswissenschaften Speyer, Universitätsbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2172", "Stade Landsch.-Verb.", "DE-Sta5",
				"Landschaftsverband der ehemaligen Herzogtümer Bremen und Verden, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1814", "Stuttgart MH", "DE-Stg111",
				"Staatliche Hochschule für Musik und Darstellende Kunst Stuttgart, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1811", "Stuttgart LKZB", "DE-Stg117",
				"Landeskirchliche Zentralbibliothek - Bibliothek des Evangelischen Oberkirchenrats");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1817", "Stuttgart WABW", "DE-Stg256",
				"Wirtschaftsarchiv Baden-Württemberg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1812", "Stuttgart Haus Birkach", "DE-Stg257",
				"Evangelisches Bildungszentrum Haus Birkach, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1860", "Stuttgart Landesmuseum", "DE-Stg266",
				"Landesmuseum Württemberg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1813", "Stuttgart HDG", "DE-Stg269",
				"Haus der Geschichte Baden-Württemberg, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1815", "Stuttgart Stadtarchiv", "DE-Stg277", "Stadtarchiv Stuttgart, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2150", "Stuttgart Landesarchiv A1u2", "DE-Stg285",
				"Landesarchiv Baden-Württemberg - Abteilungen 1 und 2, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1841", "Stuttgart Lindenmuseum", "DE-Stg5", "Linden-Museum Stuttgart, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1816", "Trossingen MH", "DE-Trs1",
				"Staatliche Hochschule für Musik Trossingen, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1858", "Tuebingen IxTheo", "DE-Tue135",
				"Index theologicus der Universitätsbibliothek Tübingen");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1739", "Vechta UB", "DE-Va1", "Universitätsbibliothek Vechta");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("4025", "Koeln HBZ Elektron. Zsn", "DE-WWW2",
				"früher: Köln; Elektronische Zeitschriften Nordrhein-Westfalens und aus Teilen von Rheinland-Pfalz");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-605");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1853", "Rostock Inst.Ostseefor.", "DE-Wa1",
				"Leibniz-Institut für Ostseeforschung Warnemünde, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2118", "Wernigerode Harzbuecherei", "DE-We21", "Harzbücherei Wernigerode");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1780", "Wiesbaden Hess. Hauptstaatsarch.", "DE-Wi1",
				"Hessisches Hauptstaatsarchiv, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-603");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1880", "FFM DIF", "DE-Wi17", "DFF - Deutsches Filminstitut & Filmmuseum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1716", "Frankfurt DIF", "DE-Wi17FP",
				"DFF - Deutsches Filminstitut & Filmmuseum / filmportal.de");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-101");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1740", "Weimar UB", "DE-Wim2", "UB Weimar");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1867", "Weimar Hauptstaatsarchiv", "DE-Wim6", "Hauptstaatsarchiv Weimar, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1843", "Weimar TLDA", "DE-Wim7",
				"Landesamt für Denkmalpflege und Archäologie, Dienststelle Weimar mit Museum für Ur- und Frühgeschichte Thüringens, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1722", "Weimar HS Musik", "DE-Wim8",
				"Hochschule für Musik Franz Liszt, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1725", "Wismar FH", "DE-Wis1",
				"Hochschule Wismar, University of Applied Sciences: Technology, Business and Design, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("2113", "Weil/Rhein vdmb", "DE-Wlr1", "Vitra Design Museum, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0255", "Rom Bibl. Hertziana", "DE-Y2",
				"Bibliotheca Hertziana - Max-Planck-Institut für Kunstgeschichte");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-612");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0255", "Florenz KHI", "DE-Y3",
				"Kunsthistorisches Institut in Florenz, Max-Planck-Institut, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-612");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("0255", "Paris DFK", "DE-Y7", "Deutsches Forum für Kunstgeschichte, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-612");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1778", "Beirut Orient-Institut", "DE-Y9", "Orient-Institut Beirut");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-601");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1842", "Zittau/Goerlitz HS", "DE-Zi4",
				"Hochschule Zittau / Görlitz, Hochschulbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("1786", "Zwickau HS", "DE-Zwi2", "Westsächsische Hochschule Zwickau, Bibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-576");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("39245", "Liechtensteinische Landesbibliothek", "LI-VaLIL",
				"Liechtensteinische Landesbibliothek");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "LI-VaLIL FLLB");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = Library.PSEU;
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = Library.SPIO;
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "SLSP", "CH-ZuSLS", "Swiss Library Service Platform");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "CH-ZuSLS");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

		bibliothek = new Library("XXXX", "DMA", "DE-101c", "Deutsche Nationalbibliothek, Deutsches Musikarchiv");
		bibliothek.addRedaktion(RedaktionsTyp.DEFAULT, "DE-101");
		ISIL_2_LIB.put(bibliothek.isil, bibliothek);
		KURZ_2_LIB.put(bibliothek.nameKurz, bibliothek);
		ISIL_2_LIBRARY_TRIE.put(bibliothek.isil.toUpperCase() + "-", bibliothek);

	}

	/**
	 * @param lib
	 */
	private static void loadMaps(final Library lib) {
		ISIL_2_LIB.put(lib.isil, lib);
		KURZ_2_LIB.put(lib.nameKurz, lib);
		ISIL_2_LIBRARY_TRIE.put(lib.isil.toUpperCase() + "-", lib);
	};

	/**
	 * Zum Laden aus Liste.txt, wenn sich was geändert hat.
	 *
	 * @throws IOException
	 */
	static void loadLibrariesExtern() throws IOException {

		ISIL_2_LIB.clear();
		KURZ_2_LIB.clear();
		ISIL_2_LIBRARY_TRIE.clear();

		final Path path = Paths.get("src/de/dnb/gnd/utils/mx/liste.txt");
		final List<String> lines = Files.readAllLines(path);

		final int URH = 0;
		final int NAM_KU = 1;
		final int ISIL_BIB = 2;
		final int RED_TYP = 03;
		final int ISIL_VERB = 04;
		final int NAM_LA = 05;
		for (final String line : lines) {
			if (line.isEmpty()) {
				continue;
			}
			final String[] lineArr = line.split("\t");
			final String urheberk = StringUtils.getArrayElement(lineArr, URH);
			final String naKu = StringUtils.getArrayElement(lineArr, NAM_KU);
			final String isilBiB = StringUtils.getArrayElement(lineArr, ISIL_BIB);
			final String redTyp = StringUtils.getArrayElement(lineArr, RED_TYP);
			final String isilVerb = StringUtils.getArrayElement(lineArr, ISIL_VERB);
			final String naLa = StringUtils.getArrayElement(lineArr, NAM_LA);

			Library bibliothek = LibraryDB.ISIL_2_LIB.get(isilBiB);
			if (bibliothek == null) {
				bibliothek = new Library(urheberk, naKu, isilBiB, naLa);
				ISIL_2_LIB.put(isilBiB, bibliothek);
				KURZ_2_LIB.put(naKu, bibliothek);
				ISIL_2_LIBRARY_TRIE.put(isilBiB.toUpperCase() + "-", bibliothek);
			}
			RedaktionsTyp typ;
			typ = RedaktionsTyp.getTyp(redTyp);
			if (typ == null) {
				throw new NullPointerException("Typ unbekannt: " + redTyp + ", ISIL:" + isilBiB);
			}
			bibliothek.addRedaktion(typ, isilVerb);

		}
		;
	}

	/**
	 * @param isil auch null
	 * @return {@link Library} oder {@link Library#getNullLibrary()}
	 */
	public static Library queryISILAgency(final String isil) {
		return queryAgencyLFU.apply(isil);
	}

}
