package de.dnb.gnd.utils;

import static de.dnb.gnd.utils.RecordUtils.getLines;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.IntParser;
import de.dnb.basics.utils.TimeUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Field;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.mx.Library;
import de.dnb.gnd.utils.mx.LibraryDB;

/**
 * Enthält spezielle Hilfsfunktionen.
 *
 * @author baumann
 *
 */
public final class GNDUtils {

	public static final String explicitSubfieldSep = "\n\t$";

	public static final String alephSubfieldSep = "$$";

	public static final String alephTagSubfieldSep = "   L ";

	public static final String explicitIndicatorContentSep = "  ";

	public static final String alephIndicatorContentSep = "";

	private static final GNDTagDB TAG_DB = GNDTagDB.getDB();

	private GNDUtils() {
		super();
	}

	/**
	 * Sichert zu, dass record nicht null ist und mithilfe der GND-Tag-Datenbank
	 * aufgebaut worden ist (also kein Titeldatensatz ist). Andernfalls wird eine
	 * {@link IllegalArgumentException} geworfen.
	 *
	 *
	 * @param record nicht null
	 */
	public static void assertGNDRecord(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		if (!RecordUtils.isAuthority(record)) {
			throw new IllegalArgumentException(
					"Datensatz mit idn " + record.getId() + "\n" + record.getRawData() + "\nist kein Normdatensatz");
		}
	}

	/**
	 * Liefert die Hauptansetzung.
	 *
	 * @param record nicht null.
	 * @return Hauptansetzung
	 * @throws IllegalStateException wenn Zahl der 1xx != 1. Auch bei Titeldaten.
	 */
	public static Line getHeading(final Record record) {
		final List<Line> lines = getLines1XX(record);
		if (lines.size() == 1)
			return lines.get(0);
		else
			throw new IllegalStateException("Anzahl der 1XX ungleich 1");
	}

	/**
	 * Liefert die 1xx-Felder.
	 *
	 * @param record nicht null.
	 * @return nicht null, modifizierbar. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getLines1XX(final Record record) {
		final Collection<Tag> gNDTags = TAG_DB.getTag1XX();
		return getLines(record, gNDTags);
	}

	/**
	 * Liefert die 4xx-Felder.
	 *
	 * @param record nicht null, GND
	 * @return nicht null, modifizierbar. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getLines4XX(final Record record) {
		final Collection<Tag> gNDTags = TAG_DB.getTag4XX();
		return getLines(record, gNDTags);
	}

	/**
	 *
	 * @param record nicht null, GND
	 * @return enthält Verweisungen
	 */
	public static boolean contains4XX(final Record record) {
		return !getLines4XX(record).isEmpty();
	}

	/**
	 * Liefert alle 5xx-Felder.
	 *
	 * @param record nicht null.
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getLines5XX(final Record record) {
		final Collection<Tag> gNDTags = GNDTagDB.getDB().getTag5XX();
		return getLines(record, gNDTags);
	}

	/**
	 * Liefert die 7xx-Felder.
	 *
	 * @param record nicht null.
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getLines7XX(final Record record) {
		final Collection<Tag> gNDTags = GNDTagDB.getDB().getTag7XX();
		return getLines(record, gNDTags);
	}

	/**
	 * Liefert die 5xx-Felder, außer 548, da das nicht relationiert ist.
	 *
	 * @param record nicht null.
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getRelatedLines5XX(final Record record) {
		final Collection<Tag> gNDTags = GNDTagDB.getDB().getRelatedTag5XX();
		return getLines(record, gNDTags);
	}

	/**
	 *
	 * @param record nicht null
	 * @return 190-Felder
	 */
	public static List<Line> getLinesTc2GND(final Record record) {
		return RecordUtils.getLines(record, "190");
	}

	/**
	 *
	 * @param record nicht null
	 * @return 7XX-Felder mit $2 lcsh
	 */
	public static List<Line> getLinesTc2LCSH(final Record record) {
		final ArrayList<Line> lines7xx = RecordUtils.getLines(record, "7..");
		final List<Line> linesLCSH = RecordUtils.filter(lines7xx,
				line -> "lcsh".equals(SubfieldUtils.getContentOfFirstSubfield(line, '2')));
		return linesLCSH;
	}

	private static final List<Character> LCSH_LABEL_INDS = StringUtils.string2charList("abcdgmnoprstlP");

	/**
	 * Die Inhalte der Subfelder mit Indikator abcdgmnoprstlP werden mittels ' / '
	 * aneinandergehängt.
	 *
	 * @param record nicht null
	 * @return (id ohne Blank, name) der LCSH-Links in Tc-Datensätzen.
	 */
	public static Set<Pair<String, String>> getLCSH_ID_Label(final Record record) {
		return getLinesTc2LCSH(record).stream().map(line -> {
			String lcshIdentifier = SubfieldUtils.getContentOfFirstSubfield(line, '0');
			lcshIdentifier = lcshIdentifier == null ? null : lcshIdentifier.replaceAll(" +", "");
			final List<Subfield> subs = SubfieldUtils.getSubfields(line, LCSH_LABEL_INDS);
			final List<String> conts = SubfieldUtils.getContentsOfSubfields(subs);
			final String lcshLabel = StringUtils.concatenate(" / ", conts);
			return new Pair<String, String>(lcshIdentifier, lcshLabel);
		}).collect(Collectors.toSet());
	}

	private static final List<Character> GND_LABEL_INDS = StringUtils.string2charList("cgkpst9");

	/**
	 * Tc-Sätze (Crosskonkordanz) haben in 190 eine Relation zu einem oder mehreren
	 * GND-SWW. Manchmal ist keine Relation enthalten, sondern etwas anderes, meist
	 * ein Zeitschlagwort. Zeitschlagwörter stehen seltsamerweise im Unterfeld $s.
	 *
	 * @param record nicht null
	 * @return idns der relationierten GND-SWW in 190 oder die mittels ' / '
	 *         konkatenierten Inhalte der Unterfelder c, g, k, p, s, t, 9.
	 */
	public static List<String> getTc_IDNs_Labels(final Record record) {
		return getLinesTc2GND(record).stream().map(line -> {
			if (line.isRelated()) {
				return line.getIdnRelated();
			}
			final List<Subfield> subs = SubfieldUtils.getSubfields(line, GND_LABEL_INDS);
			final List<String> conts = SubfieldUtils.getContentsOfSubfields(subs);
			return StringUtils.concatenate(" / ", conts);

		}).collect(Collectors.toList());
	}

	/**
	 * Gibt alle Zeilen, deren $4 mit prefix anfängt.
	 * 
	 * @param lines  nicht null
	 * @param prefix nicht null
	 * @return nicht null
	 */
	public static List<Line> getLinesWithDollar4(final List<Line> lines, final String prefix) {
		final Dollar4Predicate predicate = new Dollar4Predicate(prefix);
		return RecordUtils.filter(lines, predicate);
	}

	/**
	 * Gibt alle Oberbegriffe, also alle 5XX-Zeilen, deren $4 "ob.." oder "adue"
	 * lautet.
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static List<Line> getOBB(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final List<Line> obb = getRelatedLines5XX(record);
		final List<Line> linesOB = getLinesWithDollar4(obb, "ob");
		linesOB.addAll(getLinesWithDollar4(obb, "adue"));
		return linesOB;
	}

	/**
	 * Gibt alle IDNs der Oberbegriffe, also der 5XX-Zeilen, deren $4 "ob.." oder
	 * "adue" lautet.
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static List<Integer> getOBBidns(final Record record) {
		return getOBB(record).stream().map(line -> SubfieldUtils.getContentOfFirstSubfield(line, '9'))
				.filter(IDNUtils::isKorrektePPN).map(IDNUtils::ppn2int).collect(Collectors.toList());
	}
	
	/**
	 * Gibt alle IDNs der relationierten Begriffe, also der 5XX-Zeilen.
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static List<Integer> getRelIdns(final Record record) {
		return getRelatedLines5XX(record).stream().map(line -> SubfieldUtils.getContentOfFirstSubfield(line, '9'))
				.filter(IDNUtils::isKorrektePPN).map(IDNUtils::ppn2int).collect(Collectors.toList());
	}

	/**
	 * Gibt die alte(n) Normdatei(en) eines Datensatzes (z.Z. swd, gkd, pnd, est).
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer, modifizierbar. Leer auch bei Titeldaten.
	 */
	public static List<String> getOriginalAuthorityFiles(final Record record) {
		final ArrayList<Line> lines = getOriginalHeadingLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'S');
	}

	/**
	 * Gibt die alte(n) Ansetzungen eines Datensatzes aus 913 $a.
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer, modifizierbar. Leer auch bei Titeldaten.
	 */
	public static List<String> getOriginalHeadings(final Record record) {
		final ArrayList<Line> lines = getOriginalHeadingLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'a');
	}

	/**
	 * Gibt die Mailbox-Zeilen, die in der 901 enthalten sind.
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getMXLines(final Record record) {
		final Field field901 = RecordUtils.getFieldGivenAsString(record, "901");
		if (field901 != null)
			return new ArrayList<Line>(field901.getLines());
		else
			return new ArrayList<Line>();
	}

	public static boolean containsMX(final Record record) {
		return !getMXLines(record).isEmpty();
	}

	/**
	 * Gibt die 913-Zeilen, die die alten Ansetzungen (nebst anderem) enthalten.
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getOriginalHeadingLines(final Record record) {
		final Field field913 = RecordUtils.getFieldGivenAsString(record, "913");
		if (field913 != null)
			return new ArrayList<Line>(field913.getLines());
		else
			return new ArrayList<Line>();
	}

	/**
	 * Gibt die 083-Zeilen, die die gültigen DDC-Nummern enthalten.
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getValidDDCLines(final Record record) {
		final Field field083 = RecordUtils.getFieldGivenAsString(record, "083");
		if (field083 != null)
			return new ArrayList<Line>(field083.getLines());
		else
			return new ArrayList<Line>();
	}

	/**
	 * Gibt die 089-Zeilen, die die veralteten DDC-Nummern enthalten.
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getDeprecatedDDCLines(final Record record) {
		final Field field089 = RecordUtils.getFieldGivenAsString(record, "089");
		if (field089 != null)
			return new ArrayList<Line>(field089.getLines());
		else
			return new ArrayList<Line>();
	}

	/**
	 * Gibt alle DDC-Nummern (gültig oder ungültig).
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer. Leer auch bei Titeldaten.
	 */
	public static ArrayList<Line> getAllDDCLines(final Record record) {
		final ArrayList<Line> lines = getValidDDCLines(record);
		lines.addAll(getDeprecatedDDCLines(record));
		return lines;
	}

	/**
	 * Gibt die gültigen DDC-Nummern eines GND-Datensatzes.
	 *
	 * @param record nicht null.
	 * @return Liste, eventuell leer, modifizierbar. Leer auch bei Titeldaten.
	 */
	public static List<String> getValidDDCNumbers(final Record record) {
		final ArrayList<Line> lines = getValidDDCLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'c');
	}

	/**
	 * Gibt die maximale Determiniertheit aller DDC-Notationen (im Feld 083) des
	 * Normdatensatzes. Gibt es kein Feld 083, wird null zurückgegeben.
	 * Determiniertheiten, die nicht geparst werden können, werden als 0 angesetzt.
	 *
	 *
	 * @param record nicht null
	 * @return Die Maximale Determiniertheit oder null
	 */
	public static Integer getMaxDet(final Record record) {
		final ArrayList<Line> lines = getValidDDCLines(record);
		final ArrayList<String> dets = FilterUtils.map(lines, GNDUtils::getDDCDeterminacy);
		final IntParser intParser = new IntParser(0);
		final ArrayList<Integer> ints = FilterUtils.map(dets, intParser);
		return ListUtils.getMax(ints);

	}

	/**
	 * Gibt die DDC-Nummer einer DDC-Zeile.
	 *
	 * @param line nicht null.
	 * @return DDC-Nummer oder null
	 */
	public static String getDDCNumber(final Line line) {
		return SubfieldUtils.getContentOfFirstSubfield(line, 'c');
	}

	/**
	 * Gibt die Determiniertheit einer DDC-Zeile.
	 *
	 * @param line nicht null.
	 * @return DDC-Determiniertheit oder null
	 */
	public static String getDDCDeterminacy(final Line line) {
		return SubfieldUtils.getContentOfFirstSubfield(line, 'd');
	}

	/**
	 * Gibt zu einem Datensatz alle (gültigen und ungültigen) Paare (DDC,
	 * Determiniertheit).
	 *
	 * @param record nicht null
	 * @return Liste von Paaren, nicht null, eventuell leer. Die Paare (DDC,
	 *         Determiniertheit) enthalten eventuell an einer Stelle eine null
	 */
	public static List<Pair<String, String>> getAllDDCNumbersAndDet(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final List<Pair<String, String>> pairs = new LinkedList<>();
		final ArrayList<Line> ddcs = getAllDDCLines(record);
		for (final Line line : ddcs) {
			final String ddc = getDDCNumber(line);
			final String det = getDDCDeterminacy(line);
			final Pair<String, String> pair = new Pair<String, String>(ddc, det);
			pairs.add(pair);
		}
		return pairs;
	}

	/**
	 * splitted Feldinhalt in Information + Kommentar.
	 * 
	 * @param line nicht null.
	 * @return Paar aus a) Information(en) und b) Kommentar(en) als Strings.
	 */
	public static Pair<List<Subfield>, List<String>> splitCommentAsString(final Line line) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		final Pair<List<Subfield>, List<Subfield>> pair = splitCommentsFrom(line);
		return new Pair<List<Subfield>, List<String>>(pair.first,
				FilterUtils.map(pair.second, SubfieldUtils.FUNCTION_SUBFIELD_TO_CONTENT));
	}

	/**
	 * splitted Feldinhalt in Information + Kommentar.
	 * 
	 * @param line nicht null.
	 * @return Paar aus a) Information(en) und b) Kommentar(en).
	 */
	public static Pair<List<Subfield>, List<Subfield>> splitCommentsFrom(final Line line) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		final List<Subfield> subfields = line.getSubfields();
		return FilterUtils.divide(subfields, subfield -> subfield.getIndicator().indicatorChar != 'v');
	}

	/**
	 * Liefert den ersten Kommentar.
	 * 
	 * @param line nicht null
	 * @return ersten Kommentar oder null.
	 */
	public static String getFirstComment(final Line line) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		final Subfield dollarv = SubfieldUtils.getFirstSubfield(line, 'v');
		final String comment = (dollarv == null) ? null : dollarv.getContent();
		return comment;
	}

	/**
	 * Entfernt Kommentare aus Zeile und liefert Liste der restlichen Unterfelder.
	 *
	 * @param line nicht null
	 * @return nicht null, eventuell leer.
	 */
	public static List<Subfield> removeComments(final Line line) {
		return SubfieldUtils.removeSubfields(line, 'v');
	}

	/**
	 * Entfernt Kommentare aus Zeile und liefert eine neue Zeile.
	 *
	 * @param line nicht null
	 * @return nicht null, eventuell leer.
	 */
	public static Line removeCommentsFromLine(final Line line) {
		final List<Subfield> subfields = removeComments(line);
		final Tag tag = line.getTag();
		try {
			final Line newLine = LineParser.parse(tag, subfields);
			return newLine;
		} catch (final IllFormattedLineException e) {
		}
		return line;

	}

	/**
	 * Entfernt $4 und $v aus Zeile und liefert eine neue Zeile.
	 *
	 * @param line nicht null
	 * @return nicht null, eventuell leer.
	 */
	public static Line remove_4_v(final Line line) {
		final List<Subfield> subfields = SubfieldUtils.removeSubfields(line, 'v', '4');
		final Tag tag = line.getTag();
		try {
			final Line newLine = LineParser.parse(tag, subfields);
			return newLine;
		} catch (final IllFormattedLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;

	}

	/**
	 * Liefert Pos. 2: Satztyp.
	 * <li>"p" – Person
	 * <li>"b" - Körperschaft
	 * <li>"f" - Kongress
	 * <li>"u" - Werk
	 * <li>"g" - Geografikum
	 * <li>"s" - Sachbegriff
	 * <li>"c" - Crisscross
	 * 
	 * @param record nicht null
	 * @return Satztyp oder 0, wenn nicht enthalten. 0 auch bei Titeldaten.
	 */
	public static char getRecordType(final Record record) {
		return RecordUtils.getDatatypeCharacterAt(record, 1);
	}

	/**
	 * Liefert Satztypen:
	 * <li>"p" – Person
	 * <li>"b" - Körperschaft
	 * <li>"f" - Kongress
	 * <li>"u" - Werk
	 * <li>"g" - Geografikum
	 * <li>"s" - Sachbegriff
	 * <li>"c" - Crisscross
	 *
	 * @return Satztypen
	 */
	public static Collection<Character> getDataTypes() {
		final Collection<String> alldataTypes = Constants.SATZ_TYPEN.keySet();
		return alldataTypes.stream().filter(s -> s.length() == 2).map(s -> s.charAt(1)).collect(Collectors.toList());
	}

	/**
	 * Gibt Pos.3: Katalogisierungslevel von Feld 005.
	 * 
	 * @param record nicht null
	 * @return 1-7, 0 für 'z'; -48, wenn nicht enthalten
	 */
	public static int getLevel(final Record record) {
		final char levelChar = RecordUtils.getDatatypeCharacterAt(record, 2);
		if (levelChar == 'z')
			return 0;
		int level = levelChar - ('0');
		// wenn getDatatype ... den Wert 0 liefert, wird level -'0' = -48:
		if (level < 0)
			level = -level;
		return level;
	}

	/**
	 * Ist das ein Hinweissatz? Pos.4: Code für Hinweissatz: „e“
	 *
	 * @param record nicht null
	 * @return true, wenn ein Hinweissatz.
	 */
	public static boolean isUseCombination(final Record record) {
		return (RecordUtils.getDatatypeCharacterAt(record, 3) == 'e');
	}

	/**
	 * Die relationierten Datensätze eines Hinweissatzes (Feld 260).
	 *
	 * @param record nicht null
	 * @return nicht null, modofizierbar
	 */
	public static List<Line> getHinweisLines(final Record record) {
		return RecordUtils.getLines(record, "260");
	}

	/**
	 * Ist der GND-Datensatz gültig?
	 *
	 * @param record nicht null
	 * @return Datensatz enthält keine 010 (Änderungscodierung). Nicht mehr gültige
	 *         Datensätze enthalten eine 010.
	 */
	public static boolean isValid(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return !RecordUtils.containsField(record, "010");
	}

	/**
	 * Gibt die Entitätencodierung(en).
	 * 
	 * @param record nicht null. *
	 * @return nicht null. Eventuell leer, wenn Titeldaten.
	 */
	public static List<String> getEntityTypes(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "008");
	}

	/**
	 * Enthält der Datensatz überhaupt Entitätencodierungen?
	 * 
	 * @param record nicht null
	 * @return	false, wenn keine vorhanden, was aber auch an Titeldaten liegen kann.
	 */
	public static boolean containsEntityTypes(final Record record) {
		return !getEntityTypes(record).isEmpty();
	}

	/**
	 *
	 * @param record     nicht null
	 * @param entityCode
	 * @return Enthält der Datensatz Entitätencodierung entityCode? false auch bei Titeldaten.
	 */
	public static boolean containsEntityType(final Record record, final String entityCode) {
		final List<String> ents = getEntityTypes(record);
		return ents.contains(entityCode);
	}

	/**
	 * Gibt die GND-URI(s).
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getURIs(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "006");
	}

	public static String getAktuelleURI(final Record record) {
		final String nid = getNID(record);
		return makeURI(nid);
	}

	/**
	 * @param nid nid
	 * @return URI
	 */
	public static String makeURI(final String nid) {
		return "http://d-nb.info/gnd/" + nid;
	}

	/**
	 * Liefert zu einem Tag den Grundtag (z.B. 500 -> 100).
	 * 
	 * @param tag nicht null
	 * @return auch null, wenn nichts vorhanden
	 */
	public static Tag getNamingRelevantTag(final Tag tag) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		final StringBuffer buffer = new StringBuffer(tag.pica3);
		buffer.setCharAt(0, '1');
		return TAG_DB.findTag(buffer.toString());
	}

	/**
	 * Extrahiert aus $8 oder aus den vorhandenen Unterfeldern die Ansetzungsform
	 * des relationierten Datensatzes, die in der $8 angezeigt würde. Wenn zu dem
	 * Tag kein 1XX-Tag exisistiert, wird eine {@link IllegalArgumentException}
	 * geworfen.
	 *
	 * @param line nicht null
	 * @return Name oder null. Unicode-Composition.
	 */
	public static String getNameOfRelatedRecord(final Line line) {
		Objects.requireNonNull(line);
		// erster Versuch:
		String name = SubfieldUtils.getContentOfFirstSubfield(line, '8');
		if (name == null) {
			// Vielleicht ist die Quelle der Datenabzug:
			final List<Subfield> subfields = SubfieldUtils.getNamingRelevantSubfields(line);
			final Tag tag = getNamingRelevantTag(line.getTag());
			if (tag == null)
				throw new IllegalArgumentException("Zeile hat kein korrespondierendes 1XX");
			if (!subfields.isEmpty())
				name = RecordUtils.toPicaWithoutTag(tag, subfields);
		}
		return name;
	}

	/**
	 * Gibt den Namen des Datensatzes, wie er in $8 angezeigt würde. Ohne
	 * irrelevante Unterfelder wie $v, $5, Wirft eine {@link IllegalStateException},
	 * wenn z.B. mehrere 1XX-Zeilen.
	 *
	 * @param record nicht null
	 * @return nicht name oder "", Unicode-Composition.
	 */
	public static String getNameOfRecord(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		String name;
		if (WorkUtils.isWork(record))
			name = WorkUtils.getExpansionTitle(record);
		else {
			final Line line1XX = getHeading(record);
			final List<Subfield> subs = line1XX.getSubfields();
			final List<Subfield> relevant = SubfieldUtils.getRelevanteUnterfelder(subs);
			name = RecordUtils.toPicaWithoutTag(line1XX.getTag(), relevant, Format.PICA3, false, '$');
		}
		// Führendes $ entfernen:
		if (name.startsWith("" + Constants.DOLLAR) || name.startsWith("" + Constants.FLORIN))
			name = name.substring(2);
		return name;
	}

	/**
	 * Gibt die Teilbestandskennzeichen (011).
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getTbs(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "011");
	}

	/**
	 *
	 * @param record nicht null
	 * @return 012 enthält "s"
	 */
	public static boolean isTeilbestandIE(final Record record) {
		return getTbs(record).contains("s");
	}

	/**
	 * Gibt die Nutzungskennzeichen (012).
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getNutzungskennzeichen(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "012");
	}

	/**
	 *
	 * @param record nicht null
	 * @return 012 enthält "w" (seltsam, aber wahr!)
	 */
	public static boolean isNutzungIE(final Record record) {
		return getNutzungskennzeichen(record).contains("w");
	}

	/**
	 * Test auf $4dats.
	 */
	final static Predicate<Line> predicateErstellung = new Predicate<Line>() {
		String dollar4 = "dats";

		@Override
		public boolean test(final Line line) {
			return testDollar4(line, dollar4);
		}
	};

	/**
	 *
	 * Test auf $4datb.
	 *
	 */
	final static Predicate<Line> predicateBestehen = new Predicate<Line>() {
		String dollar4 = "datb";

		@Override
		public boolean test(final Line line) {
			return testDollar4(line, dollar4);
		}
	};

	/**
	 * Test auf $4datf.
	 */
	final static Predicate<Line> predicateFundjahr = new Predicate<Line>() {
		String dollar4 = "datf";

		@Override
		public boolean test(final Line line) {
			return testDollar4(line, dollar4);
		}
	};

	public static double toDecimal(final int degrees, final int minutes, final int seconds) {
		return degrees + minutes / 60.0 + seconds / 3600.0;
	}

	/**
	 * Gibt die im Tg-Satz enthaltenen Koordinaten. Wenn database != null, wird
	 * zunächst darin gesucht, dann im Datensatz. Danach wird die Datenbank nach
	 * relationierten Datensätzen(vorg, nach, adue, obpa, orta) durchsucht.
	 *
	 *
	 * @param record   nicht null
	 * @param database Abbildung idn->Koordinaten, auch null
	 * @return Mittelpunktskoordinaten (Mittelwert) oder null
	 */
	public static Point2D getCenterPointCoordinates(final Record record, final Map<String, Point2D> database) {
		if (database != null) {
			final Point2D found = database.get(record.getId());
			if (found != null) {
				return found;
			} else {
				// 1. Versuch: im Datensatz selbst suchen:
				final Point2D koo = getCenterPointCoordinates(record);
				if (koo != null)
					return koo;
				// 2. Versuch: Vorgänger und Nachfolger durchsuchen, hintendran Oberbegriffe.
				final List<Line> lines = RecordUtils.getLinesWithSubfield(record, "551", '4', "vorg|nach");
				lines.addAll(RecordUtils.getLinesWithSubfield(record, "551", '4', "obpa"));
				lines.addAll(RecordUtils.getLinesWithSubfield(record, "551", '4', "adue"));
				// gefährlich?:
				lines.addAll(RecordUtils.getLinesWithSubfield(record, "551", '4', "orta"));
				final List<String> idns = FilterUtils.mapNullFiltered(lines, Line::getIdnRelated);
				for (final String idn : idns) {
					if (database.containsKey(idn)) {
						return database.get(idn);
					}
				}
			}

		}
		return getCenterPointCoordinates(record);

	}

	/**
	 *
	 * @param record nicht null
	 *
	 * @return Mittelpunktskoordinaten (Mittelwert) oder null
	 */
	public static Point2D getCenterPointCoordinates(final Record record) {
		boolean isDecimal = true;
		if (getRecordType(record) != 'g')
			return null;
		List<Line> lines = RecordUtils.getLinesWithSubfield(record, "034", 'A', "d..");
		if (lines.isEmpty()) {
			lines = RecordUtils.getLinesWithSubfield(record, "034", 'A', "a..");
			isDecimal = false;
		}
		if (lines.isEmpty())
			return null;
		// Hoffentlich nur eine:
		final Line line = lines.get(0);
		final String x1s = SubfieldUtils.getContentOfFirstSubfield(line, 'd');
		final String x2s = SubfieldUtils.getContentOfFirstSubfield(line, 'e');
		final String y1s = SubfieldUtils.getContentOfFirstSubfield(line, 'f');
		final String y2s = SubfieldUtils.getContentOfFirstSubfield(line, 'g');
		final Double x1 = isDecimal ? parseDecimal(x1s) : analog2decimal(x1s);
		final Double x2 = isDecimal ? parseDecimal(x2s) : analog2decimal(x2s);
		final Double y1 = isDecimal ? parseDecimal(y1s) : analog2decimal(y1s);
		final Double y2 = isDecimal ? parseDecimal(y2s) : analog2decimal(y2s);
		// Shortcut, könnte man natürlich noch genauer analysieren:
		if (x1 == null || x2 == null || y1 == null || y2 == null)
			return null;

		final double x = (x1 + x2) / 2.0;
		final double y = (y1 + y2) / 2.0;
		return new Point2D.Double(x, y);
	}

	/**
	 * Das Feld 548 enthält Zeitpunkte oder Intervalle. Ungenaue Angaben (aus $d)
	 * werden berücksichtigt, wenn keine anderen vorliegen.
	 *
	 * @param line548 nicht null
	 * @return Nicht null!<br>
	 *         (Ein abgeschlossenes Intervall, Anfang ist unsicher, Ende ist
	 *         unsicher) <br>
	 *         Das Intervall ist nie null. Wenn nicht geparst werden kann, wird das
	 *         maximale Intervall angenommen! <br>
	 *         Für Zeitpunkte t, das Intervall [t, t], sofern t tagesgenau ist,
	 *         ansonsten [1.1.t, 31.12.t]. Änhlich wird für alle nicht tagesgenauen
	 *         Werte verfahren. <br>
	 *         Für Intervalle gilt: Ist einer der Werte nicht vorhanden (=null), so
	 *         wird das minimale/maximale Datum verwendet. <br>
	 *         Anfang/Ende sind unsicher, wenn ein 'X' vorkommt, oder wenn die Daten
	 *         aus $d stammen
	 */
	public static Triplett<Between<LocalDate>, Boolean, Boolean> getDaten(final Line line548) {
		Objects.requireNonNull(line548);
		final String subAnf = SubfieldUtils.getContentOfFirstSubfield(line548, 'a');
		final String subEnd = SubfieldUtils.getContentOfFirstSubfield(line548, 'b');
		final String subPunkt = SubfieldUtils.getContentOfFirstSubfield(line548, 'c');
		final String subUngefaehr = SubfieldUtils.getContentOfFirstSubfield(line548, 'd');
		LocalDate datAnf = TimeUtils.localDateFrom548(subAnf, false);
		final LocalDate datEnd = TimeUtils.localDateFrom548(subEnd, true);
		final LocalDate datPunkt = TimeUtils.localDateFrom548(subPunkt, false);
		final Between<LocalDate> ungefaehrInterv = TimeUtils.get548dInterval(subUngefaehr);
		boolean anfangUnsicher;
		boolean endeUnsicher;
		Between<LocalDate> daten;
		// A---Punkte:
		if (datPunkt != null) {
			final LocalDate datcend = TimeUtils.localDateFrom548(subPunkt, true);
			anfangUnsicher = StringUtils.contains(subPunkt, "X", true);
			endeUnsicher = anfangUnsicher;
			daten = new Between<LocalDate>(datPunkt, datcend);
			// B---Ungefähr:
		} else if (ungefaehrInterv != null) {
			anfangUnsicher = true;
			endeUnsicher = true;
			daten = ungefaehrInterv;
		} else {
			// C---Intervalle:
			anfangUnsicher = StringUtils.contains(subAnf, "X", true);
			endeUnsicher = StringUtils.contains(subEnd, "X", true);
			if (datAnf != null) {
				if (datEnd != null)
					daten = Between.getOrdered(datAnf, datEnd);
				else
					daten = new Between<LocalDate>(datAnf, LocalDate.MAX);
			} else {// data = null
				datAnf = LocalDate.MIN;
				if (datEnd != null)
					daten = new Between<LocalDate>(datAnf, datEnd);
				else
					daten = new Between<LocalDate>(datAnf, LocalDate.MAX);
			}
		}
		return new Triplett<Between<LocalDate>, Boolean, Boolean>(daten, anfangUnsicher, endeUnsicher);

	}

	/**
	 * Liefert die Zeile mit der GND-Systematik.
	 *
	 * @param record nicht null.
	 * @return Zeile mit Systematik oder null. null auch bei Titeldaten.
	 */
	public static Line getGNDClassificationLine(final Record record) {
		return RecordUtils.getTheOnlyLine(record, "065");
	}

	/**
	 *
	 * @param record nicht null
	 * @return enthält GND-Systematik? False auch bei Titeldaten.
	 */
	public static boolean containsGNDClassification(final Record record) {
		return getGNDClassificationLine(record) != null;
	}

	/**
	 * Gibt die GND-Systematik-Nummern.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getGNDClassifications(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "065");
	}

	/**
	 *
	 * @param record nicht null
	 * @return erste Systematik oder null
	 */
	public static String getFirstGNDClassification(final Record record) {
		final List<String> classif = getGNDClassifications(record);
		return ListUtils.getFirst(classif);
	}

	/**
	 * Enthält der Datensatz die Systematiknummer?
	 * 
	 * @param record         nicht null.
	 * @param classification beleibig.
	 * @return true, wenn eine der Systematiknummern mit classification
	 *         übereinstimmt. false auch bei Titeldaten.
	 */
	public static boolean containsGNDClassification(final Record record, final String classification) {
		final List<String> gndClasses = getGNDClassifications(record);
		return gndClasses.contains(classification);
	}

	/**
	 * Enthält der Datensatz eine der Systematiknummern?
	 * 
	 * @param record          nicht null.
	 * @param classifications beliebig.
	 * @return true, wenn eine der Systematiknummern des Datensatzes mit einer der
	 *         classifications übereinstimmt. False auch bei Titeldaten.
	 */
	public static boolean containsGNDClassifications(final Record record, final String... classifications) {
		for (final String classification : classifications) {
			if (containsGNDClassification(record, classification))
				return true;
		}
		return false;
	}

	/**
	 * Enthält der Datensatz die Systematiknummer trunkiert?
	 * 
	 * @param record          nicht null.
	 * @param classifications nicht null.
	 * @return true, wenn eine der Systematiknummern mit einer der classifications
	 *         beginnt. false auch bei Titeldaten.
	 */
	public static boolean containsGNDClassificationsTrunk(final Record record, final String... classifications) {
		final List<String> gndClasses = getGNDClassifications(record);
		return Arrays.asList(classifications).stream()
				.anyMatch(classification -> StringUtils.containsPrefix(gndClasses, classification));
	}

	/**
	 * Gibt die Ländercodes.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getCountryCodes(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "043");
	}

	/**
	 * 
	 * @param record nicht null
	 * @return	Ländercode enthalten? false auch bei Titeldaten.
	 */
	public static boolean containsCountryCode(final Record record) {
		return !getCountryCodes(record).isEmpty();
	}

	/**
	 *
	 * @param record nicht null, GND-Datensatz
	 * @return Enthält Feld 010 (Änderungscodierung). false auch bei Titeldaten.
	 */
	public static boolean containsChangeCode(final Record record) {
		return RecordUtils.containsField(record, "010");
	}

	/**
	 * Gibt die Redaktionellen Bemerkungen aus 667.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getNonpublicGeneralNotes(final Record record) {
		return RecordUtils.getContentsOfFirstSubfields(record, "667", 'a');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die Isil des Urhebers aus 903 $e oder null
	 */
	public static String getIsilUrheber(final Record record) {
		final List<String> list = RecordUtils.getContentsOfFirstSubfields(record, "903", 'e');
		if (list.isEmpty())
			return null;
		else
			return list.get(0);
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die Bibliothek des Urhebers aus 903 $e
	 */
	public static Library getUrheber(final Record record) {
		final String isil = getIsilUrheber(record);
		if (isil == null)
			return null;
		final Library library = LibraryDB.getLibraryByISIL(isil);
		if (library != null)
			return library;
		else
			return new Library("?", "?", isil, "?");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die Isil der Verbundredaktion aus 903 $r oder null
	 */
	public static String getIsilVerbund(final Record record) {
		final List<String> list = RecordUtils.getContentsOfFirstSubfields(record, "903", 'r');
		if (list.isEmpty())
			return null;
		else
			return list.get(0);
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die Verbundredaktion aus 903 $r
	 */
	public static Library getVerbund(final Record record) {
		final String isil = getIsilVerbund(record);
		if (isil == null)
			return null;
		final Library library = LibraryDB.getLibraryByISIL(isil);
		if (library != null)
			return library;
		else
			return new Library("?", "?", isil, "?");
	}

	/**
	 * Gibt die Benutzungshinweise aus 680.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getPublicGeneralNotes(final Record record) {
		return RecordUtils.getContentsOfFirstSubfields(record, "680", 'a');
	}

	/**
	 * Gibt die Quellenangaben aus 670 ohne erläuternde Texte und URIs.
	 *
	 * @param record nicht null.
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getSourcesDataFound(final Record record) {
		return RecordUtils.getContentsOfFirstSubfields(record, "670", 'a');
	}

	/**
	 * Gibt die Quellenangaben in Feld 670.
	 *
	 * @param record nicht null. *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<Line> getSourceLines(final Record record) {		
		return RecordUtils.getLines(record, "670");
	}

	/**
	 * 
	 * @param record nicht null
	 * @return	Quellenangaben? false auch bei Titeldaten.
	 */
	public static boolean containsSource(final Record record) {
		return !getSourceLines(record).isEmpty();
	}

	/**
	 * Gibt die Negativ eingesehene Quellen.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getSourceDataNotFound(final Record record) {
		return RecordUtils.getContentsOfSubfields(record, "675");
	}

	/**
	 * Gibt die Definitionen aus 677.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<String> getDefinitions(final Record record) {
		return RecordUtils.getContentsOfFirstSubfields(record, "677", 'a');
	}

	/**
	 * Gibt die Bemerkungen aus 667, 670, 672, 675, 677, 678 und 680.
	 * 
	 * @param record nicht null.
	 *
	 * @return nicht null. Leer auch bei Titeldaten.
	 */
	public static List<Line> getBemerkungsFelder(final Record record) {
		return RecordUtils.getLines(record, "667", "670", "672", "675", "677", "678", "680");
	}

	/**
	 * Gibt die Bemerkungen aus 667, 670, 672, 675, 677, 678 und 680 als String im
	 * {@link Format#PICA3}.
	 *
	 * @param record nicht null.
	 *
	 * @return nicht null.
	 */
	public static List<String> getBemerkungen(final Record record) {
		final List<Line> felder = getBemerkungsFelder(record);
		return FilterUtils.map(felder, feld -> RecordUtils.toPica(feld, Format.PICA3, false, '$'));
	}

	/**
	 * Gibt die gnd-Idn (nid, Feld 035).
	 *
	 * @param record nicht null
	 * @return Nummer oder null
	 */
	public static String getSystemControlNumber(final Record record) {
		return RecordUtils.getContentOfSubfield(record, "035", '0');
	}

	/**
	 * Liefert alle aktuellen und ehemaligen Normnummern (insbesondere IDN und NID)
	 * aus den Feldern: <br>
	 * <br>
	 * "023", "024", "028", "035", "039", "913", "797", "982", "983"
	 *
	 * @param record nicht null
	 *
	 * @return Alle Normnummern, nicht null, modifizierbar
	 */
	public static Set<String> getAlleNormnummer(final Record record) {
		Objects.requireNonNull(record);
		final LinkedHashSet<String> normnummern = new LinkedHashSet<>(RecordUtils.getContentsOfFirstSubfield(record,
				'0', "023", "024", "028", "035", "039", "913", "797", "982", "983"));
		normnummern.add(record.getId());
		return normnummern;
	}

	/**
	 * Gibt die gnd-Idn (Feld 035).
	 *
	 * @param record nicht null
	 * @return Nummer oder null
	 */
	public static String getNID(final Record record) {
		return getSystemControlNumber(record);
	}

	public static String toAleph(final Subfield subfield, final boolean explicit) {
		RangeCheckUtils.assertReferenceParamNotNull("subfield", subfield);
		String content = subfield.getContent();
		content = makeAlephStopword(content);
		return toAleph(subfield.getIndicator().alephChar, content, getAlephSubfieldDescription(subfield.getIndicator()),
				explicit);
	}

	/**
	 * Umgibt nichtsortierende Artikel am Anfang mit <<>>.
	 *
	 * @param content
	 * @return
	 */
	public static String makeAlephStopword(String content) {
		if (content.contains(" @")) {
			content = "<<" + content.replace(" @", ">> ");
		}
		return content;
	}

	/**
	 *
	 * @param indicator
	 * @param subContent
	 * @param subfieldDescription
	 * @param explicit
	 * @return
	 */
	public static String toAleph(final char indicator, final String subContent, final String subfieldDescription,
			final boolean explicit) {
		String s;
		if (explicit)
			s = explicitSubfieldSep;
		else
			s = alephSubfieldSep;
		s += indicator;
		if (explicit)
			s += explicitIndicatorContentSep;
		else
			s += alephIndicatorContentSep;
		s += subContent;
		if (explicit)
			s += "\t\t" + subfieldDescription;
		return s;
	}

	public static String toAleph(final Tag tag, final Collection<Subfield> subfields, final boolean explicit) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
		String s = toAlephPrefix(tag.aleph, getAlephTagDescription(tag), explicit);
		for (final Subfield subfield : subfields) {
			s += toAleph(subfield, explicit);
		}
		return s;
	}

	public static String toAleph(final Line line, final boolean explicit) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		return toAleph(line.getTag(), line.getSubfields(), explicit);
	}

	/**
	 * Findet auf irgendeine Weise zu einer idn die GND-Nummer (übers WEB?).
	 *
	 * @author baumann
	 *
	 */
	public static class GNDNumberFinder {
		public String find(final String idn) {
			return idn;
		}
	}

	public static final String DOLLAR_9_PHRASE = "(DE-588)";

	private static GNDNumberFinder defaultFinder = new GNDNumberFinder();

	/**
	 *
	 * Default-Aleph-Darstellung eines relationierten Feldes. Das Feld muss
	 * relationierte idn und Expansion enthalten.
	 * 
	 * @param line     nicht null
	 * @param finder   wenn null, dann wird die idn als GND-Nummer verwendet.
	 * @param explicit TODO
	 * @return Alephdarstellung oder leeren String.
	 */
	public static String toAlephRelationField(final Line line, final GNDNumberFinder finder, final boolean explicit) {
		return toAlephRelationField(line.getTag(), line.getSubfields(), finder, explicit);
	}

	public static String getAlephSubfieldDescription(final Indicator indicator) {
		return "(" + indicator.descGerman + ")";
	}

	public static String getAlephTagDescription(final Tag tag) {
		return " --- " + tag.german;
	}

	/**
	 *
	 * Default-Aleph-Darstellung eines relationierten Feldes. Das Feld muss
	 * relationierte idn und Expansion enthalten.
	 * 
	 * @param tag       nicht null
	 * @param subfields nicht null
	 * @param finder    wenn null, dann wird die idn als GND-Nummer verwendet.
	 * @param explicit  TODO
	 * @return Alephdarstellung oder leeren String.
	 */
	public static String toAlephRelationField(final Tag tag, final Collection<Subfield> subfields,
			GNDNumberFinder finder, final boolean explicit) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
		final Subfield dollar8 = SubfieldUtils.getFirstSubfield(subfields, '8');
		final Subfield dollar9 = SubfieldUtils.getFirstSubfield(subfields, '9');
		if (dollar9 == null)
			return "";
		if (finder == null)
			finder = defaultFinder;
		final List<Subfield> subList = SubfieldUtils.removeSubfieldsFromCollection(subfields, '8', '9');

		String s = toAlephPrefix(tag.aleph, getAlephTagDescription(tag), explicit);
		final char indChar = getAlephIndicator(tag.aleph);
		if (dollar8 != null) {
			String dollar8Content = dollar8.getContent();
			dollar8Content = dollar8Content.replace(" <", "\n\t$h ");
			dollar8Content = dollar8Content.replace(">", "");
			dollar8Content = makeAlephStopword(dollar8Content);
			s += toAleph(indChar, dollar8Content, getAlephSubfieldDescription(TagDB.DOLLAR_8), explicit);
		}
		for (final Subfield subfield : subList) {
			s += toAleph(subfield, explicit);
		}
		final String relID = dollar9.getContent();
		final String gndID = finder.find(relID);
		s += toAleph('9', DOLLAR_9_PHRASE + gndID, getAlephSubfieldDescription(TagDB.DOLLAR_9), explicit);
		return s;
	}

	/**
	 * Liefert den Anfang einer Zeile im Aleph-Format.
	 *
	 *
	 */
	private static String toAlephPrefix(final String alephTag, final String description, final boolean explicit) {
		String s = alephTag;
		if (explicit)
			s += description;
		else
			s += alephTagSubfieldSep;
		return s;
	}

	/**
	 * Gibt zu einem relationierten Tag den ersten Indikator.
	 *
	 * @param alephTag nicht null.
	 * @return früheren Indikator bei Relationen
	 */
	public static char getAlephIndicator(final String alephTag) {
		RangeCheckUtils.assertReferenceParamNotNull("alephTag", alephTag);
		char indChar;
		if (alephTag.equals("500"))
			indChar = 'p';
		else if (alephTag.equals("510"))
			indChar = 'k';
		else if (alephTag.equals("511"))
			indChar = 'e';
		else if (alephTag.equals("530"))
			indChar = 't';
		else if (alephTag.equals("550"))
			indChar = 's';
		else if (alephTag.equals("551"))
			indChar = 'g';
		else
			indChar = 'a';
		return indChar;
	}

	public static String toAleph(final Record record, final GNDNumberFinder finder, final boolean explicit) {
		assertGNDRecord(record);
		if (record.tagDB != TAG_DB)
			throw new IllegalArgumentException("Kein GND-Datensatz");

		final Line authorLine = WorkUtils.getAuthorLine(record);
		String authorName = null;
		if (authorLine != null) {
			authorName = SubfieldUtils.getContentOfFirstSubfield(authorLine, '8');
			if (authorName == null) // keine Expansion dabei
				authorName = "-Name nicht ermittelbar-";
		}

		final List<String> lines = new LinkedList<String>();
		for (final Line line : record) {
			final Tag tag = line.getTag();
			final String tagStr = tag.pica3;
			if ((tagStr.equals("130") || tagStr.equals("430")) && authorName != null) {
				// Sonderbehandlung für Werktitel:
				String newLine;
				newLine = toAlephPrefix(tagStr.charAt(0) + "00", getAlephTagDescription(tag), explicit);
				newLine += toAleph('p', authorName, "(Autor/Komponist)", explicit);

				final List<Subfield> subs = line.getSubfields();
				for (final Subfield subfield : subs) {
					newLine += toAleph(subfield, explicit);
				}
				lines.add(newLine);
				continue;
			}
			if (line.getTag().aleph != null) {
				if (SubfieldUtils.containsIndicator(line, '9')) {
					lines.add(toAlephRelationField(line, finder, explicit));
				} else {
					lines.add(toAleph(line, explicit));
				}
			} else {
				// Sonderbehandlung für einzelne Tags, die auf mehrere
				// Aleph-Tags aufgeteilt werden.
			}
		}
		StringUtils.tagSort(lines);
		return StringUtils.concatenate(lines);
	}

	/**
	 * Liest einen Datensatz von der Standardeingabe. Nimmt an, dass Normdaten
	 * vorliegen.
	 *
	 * @return neuen Datensatz.
	 */
	public static Record readFromConsole() {
		return RecordUtils.readFromConsole(TAG_DB);
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage. Fehler werden ignoriert.
	 * 
	 * @return neuen Datensatz oder null, wenn Clipboard nicht in String
	 *         konvertierbar.
	 */
	public static Record readFromClip() {
		return RecordUtils.readFromClip(TAG_DB);
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public static void main2(final String[] args)
			throws IllFormattedLineException, OperationNotSupportedException, IOException {
		final RecordReader reader = RecordReader.getMatchingReader(Constants.Tg);
		reader.forEach(record -> {
			final List<Line> lines = RecordUtils.getLinesWithSubfield(record, "034", 'A', "d..");
			lines.addAll(RecordUtils.getLinesWithSubfield(record, "034", 'A', "a.."));
			if (lines.size() == 1 || lines.size() > 2)
				System.out.println(record.getId());
		});
	}

	public static void main(final String[] args)
			throws IllFormattedLineException, OperationNotSupportedException, IOException {
		final Record record = RecordUtils.readFromClip();
		System.out.println(getRelIdns(record));
	}

	/**
	 * @param record nicht null
	 * @return 1XX ohne irrelevante Unterfelder im Pica-Format und
	 *         Unicode-normalisiert (Unlaute zusammengezogen)
	 */
	public static String getSimpleName(final Record record) {
		final Line line = getHeading(record);
		final List<Subfield> subfields = SubfieldUtils.getNamingRelevantSubfields(line);
		final String simpleName = RecordUtils.toPicaWithoutTag(line.getTag(), subfields, Format.PICA3, false, '$');
		return StringUtils.unicodeComposition(simpleName);
	}

	private static final String ANALOG_REGEXP = "([A-Z])\\s+(\\d\\d\\d)\\s+(\\d\\d)\\s+(\\d\\d)";
	private static final Pattern ANALOG_PAT = Pattern.compile(ANALOG_REGEXP);

	/**
	 * @param analog Format: W 058 22 38
	 * @return Gradangabe -58.377229 oder null!
	 */
	public static Double analog2decimal(String analog) {
		analog = analog.trim();
		final Matcher matcher = ANALOG_PAT.matcher(analog);
		if (!matcher.matches())
			return null;
		final String sign = matcher.group(1);
		final int deg = Integer.parseInt(matcher.group(2));
		final int min = Integer.parseInt(matcher.group(3));
		final int sec = Integer.parseInt(matcher.group(4));
		double decimal = toDecimal(deg, min, sec);
		if (sign.equals("W") || sign.equals("S"))
			decimal = -decimal;
		return decimal;
	}

	private static final String DECIMAL_REGEXP = "([A-Z])\\s*(\\d\\d\\d\\.\\d+)";
	private static final Pattern DECIMAL_PAT = Pattern.compile(DECIMAL_REGEXP);

	/**
	 * @param analog Format: W058.377229
	 * @return Gradangabe -58.377229 oder null!
	 */
	public static Double parseDecimal(String analog) {
		analog = analog.trim();
		final Matcher matcher = DECIMAL_PAT.matcher(analog);
		if (!matcher.matches())
			return null;
		final String sign = matcher.group(1);

		double value = Double.parseDouble(matcher.group(2));
		if (sign.equals("W") || sign.equals("S"))
			value = -value;
		return value;
	}

	/**
	 * @param line
	 * @param dollar4
	 * @return
	 */
	public static boolean testDollar4(final Line line, final String dollar4) {
		if (SubfieldUtils.containsIndicator(line, '4')) {
			final Subfield subfield4 = SubfieldUtils.getFirstSubfield(line, '4');
			final String subCont = subfield4.getContent();
			return subCont.equals(dollar4);
		} else
			return false;
	}

	/**
	 * Gibt alle instantiellen Sach-Oberbegriffe, also alle 550-Zeilen, deren $4
	 * "obin" lautet.
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar.
	 */
	public static List<Line> getInstantielleSachOBB(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final List<Line> obb = RecordUtils.getLines(record, "550");
		final List<Line> linesOB = getLinesWithDollar4(obb, "obin");
		return linesOB;
	}

	/**
	 * Gibt alle instantiellen Sach-Oberbegriffe, also alle 550-Zeilen, deren $4
	 * "obin" lautet.
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar.
	 */
	public static List<Line> getGenerischeSachOBB(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final List<Line> obb = RecordUtils.getLines(record, "550");
		final List<Line> linesOB = getLinesWithDollar4(obb, "obge");
		return linesOB;
	}

	/**
	 * Tc-Sätze (Crosskonkordanz) haben in 190 eine Relation zu einem oder mehreren
	 * GND-SWW.
	 *
	 * @param record nicht null
	 * @return idns der relationierten GND-SWW in 190
	 */
	public static List<String> getCrossConcordanceIDNs(final Record record) {
		return RecordUtils.getContentsOfFirstSubfields(record, "190", '9');
	}

	/**
	 * Die ids aus dem MACS-Projekt.
	 *
	 * @param record nicht null
	 * @return mids, nicht null, modifizierbar
	 */
	public static List<String> getMACSids(final Record record) {
		return RecordUtils.getContentsOfFirstSubfield(record, '0', "030");
	}

	/**
	 * Tk-Sätze haben als Namensfeld die 153.
	 *
	 * @param record nicht null
	 * @return Nummer oder null
	 */
	public static String getClassificationNumber(final Record record) {
		return RecordUtils.getContentOfSubfield(record, "153", 'a');
	}

	/**
	 * Tk-Sätze haben als Namensfeld die 153.
	 *
	 * @param record nicht null
	 * @return Nummer oder null
	 */
	public static String getClassificationName(final Record record) {
		return RecordUtils.getContentOfSubfield(record, "153", 'j');
	}

	/**
	 * 
	 * @param record nicht null
	 * @return Ts1, Tc, Tu1e ....
	 */
	public static String getBBG(final Record record) {
		return RecordUtils.getDatatype(record);
	}

	/**
	 * 
	 * @param record nicht null
	 * @return Alle Vorgänger und Nachfolger (vorg|nach) im Feld 510.
	 */
	public static Collection<Line> getPrePostLines(Record record) {
		return RecordUtils.getLinesWithSubfield(record, "510", '4', "vorg|nach");
	}

	/**
	 * 
	 * @param record nicht null
	 * @return alle idns von Körperschaften, die mit record als Vorgänger und
	 *         Nachfolger verbunden sind.
	 */
	public static Collection<Integer> getVorgNachf(Record record) {
		Collection<Line> lines = getPrePostLines(record);
		return RecordUtils.extractIDNints(lines);
	}

	/**
	 * Wird ein Name, der $g enthält in ein Unterfeld eingesetzt, also z.B. "Bezirk
	 * Biel$gBern", so darf nicht "Bezirk Biel (Bern)" eingesetzt werden, sondern es
	 * muss heißen: "Bezirk Biel, Bern". Allerdings werden zunächst nur $a und $g
	 * berücksichtigt. $x nicht!
	 * 
	 * @param record nicht null
	 * @return Name eines Geogrfikums, der in ein Unterfeld eingesetzt werden kann.
	 */
	public static String geoNameFuerUnterfeld(final Record record) {
		String name;
		final Line headingline = getHeading(record);
		name = SubfieldUtils.getContentOfFirstSubfield(headingline, 'a');
		final String dollarg = SubfieldUtils.getContentOfFirstSubfield(headingline, 'g');
		if (dollarg != null)
			name += ", " + dollarg;
		return StringUtils.unicodeComposition(name);

	}
}
