package de.dnb.gnd.utils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.collections.CollectionUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.TimeUtils;
import de.dnb.gnd.exceptions.ExceptionHandler;
import de.dnb.gnd.exceptions.IgnoringHandler;
import de.dnb.gnd.exceptions.WrappingHandler;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.ItemParser;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.RecordUtils.STANDORT_DNB;

public final class BibRecUtils {

	//@formatter:on

	private BibRecUtils() {
	}

	/**
	 * Sichert zu, dass record nicht null ist und mithilfe der GND-Tag-Datenbank
	 * aufgebaut worden ist (also kein Titeldatensatz ist). Andernfalls wird eine
	 * {@link IllegalArgumentException} geworfen.
	 *
	 *
	 * @param record nicht null
	 */
	public static void assertBibRecord(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		if (!RecordUtils.isBibliographic(record)) {
			throw new IllegalArgumentException(
					"Datensatz mit idn " + record.getId() + "\n" + record.getRawData() + "\nist kein Titeldatensatz");
		}
	}

	private static final BibTagDB TAG_DB = BibTagDB.getDB();

	/**
	 * Liest einen Datensatz von der Standardeingabe. Nimmt an, dass Titeldaten
	 * vorliegen. Gibt Fehler aus.
	 *
	 * @return neuen Datensatz.
	 */
	public static Record readFromConsole() {
		return RecordUtils.readFromConsole(TAG_DB, new WrappingHandler());
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage. Fehler werden ignoriert.
	 * 
	 * @return neuen Datensatz oder null.
	 */
	public static Record readFromClipIgnoring() {
		return RecordUtils.readFromClip(TAG_DB, null, true);
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage. Fehler werden ignoriert.
	 * 
	 * @param fileName nicht null, nicht leer
	 * @return neuen Datensatz oder null.
	 * @throws FileNotFoundException wenn nicht da
	 */
	public static Record readFromFileIgnoring(final String fileName) throws FileNotFoundException {
		return RecordUtils.readFromFile(fileName, TAG_DB, true);
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage. Gibt Fehler aus.
	 * 
	 * @return neuen Datensatz oder null.
	 */
	public static Record readFromClip() {
		return RecordUtils.readFromClip(TAG_DB, new WrappingHandler(), false);
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage.
	 * 
	 * @param handler beliebig
	 * @return neuen Datensatz oder null.
	 */
	public static Record readFromClip(final ExceptionHandler handler) {
		return RecordUtils.readFromClip(TAG_DB, handler, false);
	}

	/**
	 * Liest einen Datensatz von der Standardeingabe. Nimmt an, dass Titeldaten
	 * vorliegen. Ignoriert Fehler.
	 *
	 * @return neuen Datensatz.
	 */
	public static Record readFromConsoleIgnoring() {
		return RecordUtils.readFromConsole(TAG_DB);
	}

	public static boolean hasIgnorableIndicator(final Record record) {
		assertBibRecord(record);
		for (final Line line : record) {
			final Tag tag = line.getTag();
			if (!tag.hasIgnorableIndicator())
				continue;
			final Indicator indicator = tag.getIgnorableIndicator();
			if (SubfieldUtils.containsIndicator(line, indicator))
				return true;
		}
		return false;
	}

	/**
	 * Gibt das Anfangssegment.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getHeadersSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getHeaders());
	}

	/**
	 * Segment Personennamen. 30XX.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getPersonalNameSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getPersonalNameSegment());
	}

	/**
	 * Segment Körperschaftsnamen.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getCoporateNameSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getCoporateNameSegment());
	}

	/**
	 * Segment Sachtitel.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getTitleSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getTitleSegment());
	}

	/**
	 * Segment Segment Veröff.-Vermerk, Umfang, Beilagen.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getEditionSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getEditionSegment());
	}

	/**
	 * Segment Übergeordnete Gesamtheiten / Sekundärausgaben.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getSeriesStatementSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getSeriesStatementSegment());
	}

	/**
	 * Segment Fußnoten.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getNoteSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getNoteSegment());
	}

	/**
	 * Segment Titelverknüpfungen.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getTitleLinkSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getTitleLinkSegment());
	}

	/**
	 * Segment Feldgruppen für Nicht-Standard-NEE.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getAddedEntryFieldsSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getAddedEntryFieldsSegment());
	}

	/**
	 * Segment Sonstige Angaben.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getOtherInformationSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getOtherInformationSegment());
	}

	/**
	 * Segment Bearbeiterzeichen.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getResponsibilitySegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getResponsibilitySegment());
	}

	/**
	 * Segment Sacherschließung.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getSubjectAccessSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getSubjectAccessSegment());
	}

	/**
	 * Segment Exemplardaten.
	 *
	 * @param record nicht null
	 * @return nicht null; neue, modifizierbare Liste
	 */
	public static ArrayList<Line> getHoldingsSegment(final Record record) {
		assertBibRecord(record);
		return RecordUtils.getLines(record, TAG_DB.getHoldingsSegment());
	}

	/**
	 * Entfernt Exemplardaten.
	 *
	 * @param record nicht null
	 *
	 */
	public static void removeHoldingsSegment(final Record record) {
		assertBibRecord(record);
		RecordUtils.removeTags(record, TAG_DB.getHoldingsSegment());
	}

	/**
	 *
	 * @param record nicht null
	 * @return Titel (aus Feld 4000 $a) oder null
	 */
	public static String getMainTitle(final Record record) {
		final Line titleLine = getMainTitleLine(record);
		if (titleLine == null)
			return null;
		final String dollarA = SubfieldUtils.getContentOfFirstSubfield(titleLine, 'a');
		if (dollarA != null)
			return dollarA;
		return SubfieldUtils.getContentOfFirstSubfield(titleLine, '8');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Titel (aus Feld 4000 $a) + Zusätze ($d), getrennt durch "_:_" oder
	 *         null
	 */
	public static String getHaupttitelUndZusatz(final Record record) {
		final Line titleLine = getMainTitleLine(record);
		if (titleLine == null)
			return null;
		final String dollarA = SubfieldUtils.getContentOfFirstSubfield(titleLine, 'a');
		if (dollarA == null)
			return null;
		final List<String> zusaetze = SubfieldUtils.getContentsOfSubfields(titleLine, 'd');
		zusaetze.add(0, dollarA);
		return StringUtils.concatenate(" : ", zusaetze);
	}

	/**
	 *
	 * @param record nicht null
	 * @return Teil Titel (aus Feld 4004) oder null. Unicode-Composition.
	 */
	public static String getTitelDesTeils(final Record record) {
		final Line titleLine = RecordUtils.getTheOnlyLine(record, "4004");
		if (titleLine == null)
			return null;
		else {
			return RecordUtils.toPicaWithoutTag(titleLine);
		}
	}

	/**
	 *
	 * @param record nicht null
	 * @return Titel aus 4000 + Titel des Teils aus 4004, durch '. ' getrennt oder
	 *         null. Unicode-Composition.
	 */
	public static String getVollstaendigenTitel(final Record record) {
		final String haupt = getMainTitle(record);
		if (haupt == null)
			return null;
		final String neben = getTitelDesTeils(record);
		if (neben == null)
			return haupt;
		else
			return haupt + ". " + neben;
	}

	/**
	 *
	 * @param record nicht null
	 * @return (Titel / Verantwortlichkeitsangabe) oder null
	 */
	public static String getResponsibilityAndTitle(final Record record) {
		final Line titleLine = getMainTitleLine(record);
		if (titleLine == null)
			return null;
		else {
			String title = SubfieldUtils.getContentOfFirstSubfield(titleLine, 'a');
			if (title == null)
				title = SubfieldUtils.getContentOfFirstSubfield(titleLine, '8');
			if (title == null)
				return null;

			final String auth = SubfieldUtils.getContentOfFirstSubfield(titleLine, 'h');
			if (auth != null)
				title += " / " + auth;

			return title;
		}
	}

	/**
	 *
	 * @param record nicht null
	 * @return Zeile 4000
	 */
	public static Line getMainTitleLine(final Record record) {
		return RecordUtils.getTheOnlyLine(record, "4000");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Zeilen der Status-Angaben (Datensatzkennzeichnungen, 0599). Das
	 *         können in seltenen Fällen (ZDB) mehrere sein; nicht null
	 */
	public static List<Line> getStatusLines(final Record record) {
		return RecordUtils.getLines(record, "0599");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Status-Angaben der 0599-Felder. Das können in seltenen Fällen (ZDB)
	 *         mehrere sein; nicht null, nicht leer. Wenn keine Statuszeile
	 *         vorhanden ist, wird {0} zurückgegeben.
	 */
	public static List<Character> getStatuses(final Record record) {
		final List<Line> lines0599 = getStatusLines(record);
		if (lines0599.isEmpty())
			return Arrays.asList((char) 0);
		return FilterUtils.mapNullFiltered(lines0599,
				line -> StringUtils.charAt(SubfieldUtils.getContentOfFirstSubfield(line, 'b'), 0));
	}

	/**
	 *
	 * @param record nicht null
	 * @return Zeile der Code-Angaben (0600), enthält u.a. die Reihen (ra, rh ...)
	 *         oder null
	 */
	public static Line getCodeLine(final Record record) {
		return RecordUtils.getTheOnlyLine(record, "0600");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Code-Angaben aus 0600, enthält u.a. die Reihen (ra, rh ...) oder
	 *         leer, wenn keine Code-Angaben. Liste ist modifizierbar
	 */
	public static List<String> getCodes(final Record record) {
		final Line line = getCodeLine(record);
		if (line == null)
			return new ArrayList<>();
		else
			return SubfieldUtils.getContentsOfSubfields(line, 'a');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Reihe A?
	 */
	public static boolean isRA(final Record record) {
		return getCodes(record).contains("ra");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Reihe B?
	 */
	public static boolean isRB(final Record record) {
		return getCodes(record).contains("rb");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Reihe H?
	 */
	public static boolean isRH(final Record record) {
		return getCodes(record).contains("rh");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Reihe O?
	 */
	public static boolean isRO(final Record record) {
		return getCodes(record).contains("ro");
	}

	/**
	 * Die wichtigsten Reihen.
	 *
	 * @author baumann
	 *
	 */
	public enum REIHE {
		A("Reihe A"), B("Reihe B"), H("Reihe H"), O("Reihe O");

		private String myName;

		private REIHE(final String name) {
			myName = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return myName;
		}

		/**
		 *
		 * @return Die Elemente
		 */
		public static EnumSet<REIHE> enumSet() {
			return EnumSet.allOf(REIHE.class);
		}

	}

	/**
	 *
	 * @param record nicht null
	 * @return Die Reihe A, B, H, O (in genau dieser Reihenfolge) oder null aus 0600
	 */
	public static REIHE getReihe(final Record record) {
		if (isRA(record))
			return REIHE.A;
		if (isRB(record))
			return REIHE.B;
		if (isRH(record))
			return REIHE.H;
		if (isRO(record))
			return REIHE.O;
		return null;
	}

	/**
	 * Macht aus Titeldaten einen Kurztitel.
	 *
	 * @param record nicht null.
	 * @return nicht null?
	 */
	public static String createShortTitle(final Record record) {
		assertBibRecord(record);
		String title;
		String creator;

		title = RecordUtils.getContentOfSubfield(record, "4000", 'a');
		// wohl Af:
		if (title == null) {
			final String unselbständig = RecordUtils.getContentOfSubfield(record, "4004", 'a');
			title = RecordUtils.getContentOfSubfield(record, "4000", '8');
			if (title != null) {
				if (unselbständig != null)
					title = title + "; " + unselbständig;
			} else
				// titel immer noch null, daher letzte Rettung
				title = unselbständig;
		}

		creator = RecordUtils.getContentOfSubfield(record, "3000", '8');
		if (creator == null)
			creator = RecordUtils.getContentOfSubfield(record, "3100", '8');
		if (creator != null) {
			if (creator.contains("$l"))
				creator = creator.replace("$l", " <") + ">";
			if (creator.contains("$g"))
				creator = creator.replace("$g", " <") + ">";
			if (creator.contains("$b")) {
				// $gKiel$bBibliothek -> <Kiel, Bibliothek>:
				if (creator.contains("<"))
					creator = creator.replace("$b", ", ");
				else
					creator = creator.replace("$b", " <") + ">";
			}
			creator = creator.replace("$c", " ");
			title = creator + " : " + title;
			// hier darf man @ entfernen, da der Autor (hoffentlich) keine
			// Artikel ... enthält und an erster Stelle steht:
			title = title.replace("@", "");
		}
		// Für anonyme Werke kein @ entfernen, da Excel falsch sortiert und
		// dann die betreffenden Titel nicht händisch gefunden werden:

		// Bei der Expansion [Tp1] etc. entfernen:
		title = title.replaceFirst(" \\[T[pnb].\\]", "");
		return title;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Sprachen des Textes aus Feld 1500 /1 .. /1 .. nicht null,
	 *         modifizierbare Liste
	 */
	public static List<String> getLanguagesOfText(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Line line = RecordUtils.getTheOnlyLine(record, "1500");
		if (line == null)
			return Collections.emptyList();
		else
			return SubfieldUtils.getContentsOfSubfields(line, 'a');
	}

	/**
	 *
	 * @param record nicht null
	 * @return vierstelliges Erscheinungsjahr aus Feld 1100 $a oder null
	 */
	public static String getYearOfPublicationString(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Line line = RecordUtils.getTheOnlyLine(record, "1100");
		if (line == null)
			return null;
		else
			return SubfieldUtils.getContentOfFirstSubfield(line, 'a');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Erscheinungsjahr aus Feld 1100 $a oder null
	 */
	public static Integer getYearOfPublication(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final String yearStr = getYearOfPublicationString(record);
		if (yearStr == null)
			return null;
		try {
			return Integer.parseInt(yearStr);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Umfangsangabe.
	 *
	 * @param record nicht null
	 * @return Inhalt von 4060 $a (Umfangsangabe, Anzahl der physischen Einheiten
	 *         und/oder spezifische Materialbenennung). null, wenn nicht genau eine
	 *         Zeile 4060 oder wenn kein $a vorhanden.
	 */
	public static String getSimpleExtent(final Record record) {
		return RecordUtils.getContentOfSubfield(record, "4060", 'a');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Lieferungsnummern (Feld ist wiederholbar) der Deutschen
	 *         Nationalbibliografie in der Form <br>
	 *         <code>JJ,RHH(,ZZZZ)</code><br>
	 *         nicht null
	 */
	public static Collection<String> getWVNumberStrings(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return RecordUtils.getContentsOfFirstSubfields(record, "2105", '0');
	}

	/**
	 *
	 * @param record nicht null
	 * @return enthält das Feld 2105 (WV).
	 */
	public static boolean istAngezeigt(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return RecordUtils.containsField(record, "2105");
	}

	/**
	 *
	 * @param record nicht null
	 * @return gewinnt aus 2105 Sammlung von WV-Datensätzen, die Jahr, Nummer und
	 *         Reihe enthalten, nicht null eventuell leer
	 */
	public static Collection<WV> getWVs(final Record record) {
		final Collection<String> wvSs = getWVNumberStrings(record);
		return FilterUtils.mapNullFiltered(wvSs, WV::create);
	}

	/**
	 *
	 * @param record nicht null
	 * @return gewinnt aus 2105 die Jahre der wöchentlichen Verzeichnisse, also die
	 *         Jahre, in denen die Publikation angezeigt wurde; nicht null,
	 *         eventuell leer
	 */
	public static Collection<Integer> getWVYears(final Record record) {
		final Collection<WV> wvs = getWVs(record);
		final Set<Integer> set = wvs.stream().map(WV::getYear).collect(Collectors.toSet());
		return set;
	}

	/**
	 *
	 * @param record nicht null
	 * @return gewinnt aus 2105 die Jahre der wöchentlichen Verzeichnisse, also die
	 *         Jahre, in denen die Publikation angezeigt wurde; nicht null,
	 *         eventuell leer
	 */
	public static boolean isPHeft(final Record record) {
		final Collection<WV> wvs = getWVs(record);
		return wvs.stream().anyMatch(WV::isPHeft);

	}

	/**
	 * Liefert alle Signaturen.
	 *
	 * @param record nicht null.
	 *
	 * @return nicht null.
	 */
	public static Collection<String> getShelfMarks(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return RecordUtils.getContentsOfFirstSubfields(record, "7100", 'a');
	}

	/**
	 * Liefert die DNB-Signatur. Hier wird angenommen, dass die erste Signatur der
	 * Liste die relevante ist - es kann natürlich auch die Leipziger Signatur sein.
	 * Daher muss durch geeignete Massnahmen (etwa Kommando "s d") sichergestellt
	 * sein, dass nur die gewünschte Signatur im Datensatz enthalten ist.
	 * 
	 * @param record nicht null
	 * @return null, wenn keine gefunden
	 */
	public static String getDNBShelfMark(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Collection<String> shMarks = getShelfMarks(record);

		final Iterator<String> iterator = shMarks.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		} else
			return null;

	}

	public static Collection<String> getIDsOfHoldingOrganisations(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return RecordUtils.getContentsOfFirstSubfields(record, "4800", '9');
	}

	/**
	 *
	 * @param record nicht null
	 * @return ist Druckschrift (0500 beginnt mit 'A')
	 */
	public static boolean isPrintedPublication(final Record record) {
		if (RecordUtils.isAuthority(record))
			return false;
		return getPhysikalischeForm(record) == 'A';
	}

	/**
	 *
	 * Liefert die IDN des übergeordneten Buches. Entweder aus der 4000 bei
	 * Af-Sätzen oder aus 4160, 4140, 4241 (in dieser Reihenfolge).
	 *
	 * @param record nicht null
	 * @return idn oder null. null, wenn nicht vorhanden oder 4000 oder 4160, 4140
	 *         oder 4241 fehlerhaft oder mehrfach vorhanden
	 */
	public static String getBroaderTitleIDN(final Record record) {
		String broader = RecordUtils.getContentOfSubfield(record, "4000", '9');
		if (broader != null) {
			return broader;
		}
		broader = RecordUtils.getContentOfSubfield(record, "4160", '9');
		if (broader != null) {
			return broader;
		}
		broader = RecordUtils.getContentOfSubfield(record, "4140", '9');
		if (broader != null) {
			return broader;
		}
		broader = RecordUtils.getContentOfSubfield(record, "4241", '9');
		if (broader != null) {
			return broader;
		}

		return null;

	}

	/**
	 *
	 * Liefert die IDNs der übergeordneten Sätze. Aus der 4000, 4140, 4160, 4180,
	 * 4181, 4182 (in dieser Reihenfolge).
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer
	 */
	public static List<String> getBroaderTitleIDNs(final Record record) {
		return RecordUtils.getContentsOfFirstSubfield(record, '9', "4000", "4140", "4160", "4180", "4181", "4182");
	}

	/**
	 *
	 * @param record nicht null
	 * @return IDNs der Art des Inhalts aus 1131 (z.Z. 7.2.1.3 RDA)
	 */
	public static List<String> getNatureOfContentIDs(final Record record) {
		return RecordUtils.getContentsOfAllSubfields(record, "1131", '9');
	}

	/**
	 *
	 * @param record nicht null
	 * @return IDNs der ZIELGRUPPE aus 1133 (z.Z. 7.7.1.3 RDA)
	 */
	public static List<String> getIntendedAudienceIDs(final Record record) {
		return RecordUtils.getContentsOfAllSubfields(record, "1133", '9');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Alle ISBNs, auch die ungültigen und die der Sekundärausgaben. Nicht
	 *         null, eventuell leer, modifizierbar
	 */
	public static List<String> getTheISBNs(final Record record) {
		Objects.requireNonNull(record);
		final List<Line> lines = RecordUtils.getLines(record, "2000", "2009", "2015", "2016");
		return SubfieldUtils.getContentsOfFirstSubfields(lines, '0');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die gültigen ISBNs aus Feld 2000. Also ISBN-10 und ISBN-13. Nicht
	 *         null, eventuell leer, modifizierbar
	 */
	public static List<String> getValidISBNs(final Record record) {
		Objects.requireNonNull(record);
		final List<Line> lines = RecordUtils.getLines(record, "2000");
		return SubfieldUtils.getContentsOfFirstSubfields(lines, '0');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die gültigen ISBNs ohne Bindestriche. Nicht null, eventuell leer
	 */
	public static List<String> getValidRawISBNs(final Record record) {
		final List<String> isbns = getValidISBNs(record);
		return FilterUtils.mapNullFiltered(isbns, BibRecUtils::makeRawISBN);
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die längste ISBN, in der Regel die ISBN-13 oder null
	 */
	public static String getBestISBN(final Record record) {
		final List<String> list = getValidISBNs(record);
		if (list.isEmpty())
			return null;
		String bestisb = "";
		for (final String isb : list) {
			if (isb.length() > bestisb.length())
				bestisb = isb;
		}
		return bestisb;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Liste mit Verlagsnamen, eventuell leer, modifizierbar
	 */
	public static List<String> getNamesOfProducers(final Record record) {
		final List<Line> lines4030 = getProducerLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines4030, 'n');
	}

	/**
	 *
	 * @param record nicht null
	 * @return Ersten gelisteten Verlag oder null
	 */
	public static String getNameOfFirstProducer(final Record record) {
		final List<String> prods = getNamesOfProducers(record);
		if (prods.isEmpty())
			return null;
		return prods.get(0);
	}

	/**
	 * @param record nicht null
	 * @return Zeilen mit Veröffentlichungsangaben, nicht null, modifizierbar
	 */
	public static List<Line> getProducerLines(final Record record) {
		final ArrayList<Line> lines4030 = RecordUtils.getLines(record, "4030");
		return lines4030;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die längste ISBN, in der Regel die ISBN-13 ohne Bindestriche - oder
	 *         null
	 */
	public static String getRawISBN(final Record record) {
		final String isbn13 = getBestISBN(record);
		return makeRawISBN(isbn13);
	}

	/**
	 * @param isbn auch null
	 * @return ISBN ohne Bindestriche - oder null
	 */
	public static String makeRawISBN(final String isbn) {
		if (isbn == null)
			return null;
		else {
			final String replaced = isbn.replace("-", "").trim();
			if (replaced.isEmpty())
				return null;
			else
				return replaced;
		}
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip(TAG_DB, new IgnoringHandler(), false);
		System.out.println(istHochschulschrift(record, true));
	}

	/**
	 * @param record nicht null
	 * @return Verlage durch ' / ' getrennt, Orte nach Pica3 durch ' : '. Sonst ""
	 */
	public static String getVerlageUndOrte(final Record record) {
		final List<Line> producerLines = getProducerLines(record);
		return producerLines.stream().map(line -> {
			final List<String> ps = SubfieldUtils.getContentsOfSubfields(line, 'p');
			final String pSt = StringUtils.concatenate(" : ", ps);
			final String n = SubfieldUtils.getContentOfFirstSubfield(line, 'n');
			return pSt + (n == null ? " " : (" : " + n + " "));

		}).collect(Collectors.joining(" / "));
	}

	public static final List<String> charaktereDerHochschulschrift = Arrays.asList("Bachelorarbeit", "Diplomarbeit",
			"Dissertation", "Habilitationsschrift", "Lizenziatsarbeit", "Magisterarbeit", "Masterarbeit");
	public static final List<String> dissHabil =
			Arrays.asList(			
			"Dissertation",
			"Habilitationsschrift"
			);

	/**
	 * Unterschucht die Position 3 in 0500 auf 'd' oder 'g', die Codes in 0600 auf
	 * "rh" und "di" und auf das Vorhandensein der Felder 2215 oder 4204.
	 *
	 * @param record       nicht null
	 * @param nurDissHabil wenn true
	 * @return ob Hochschulschrift (Diss. oder Habilschr.)
	 */
	public static boolean istHochschulschrift(final Record record, boolean nurDissHabil) {
		Objects.requireNonNull(record);
		if (RecordUtils.isAuthority(record))
			return false;
		final List<String> ids1131 = getNatureOfContentIDs(record);
		if (ids1131.contains("041139372")) {
			return true;
		}
		final char char3 = getStatusderBeschreibung(record);
		if (char3 == 'd')
			return true;
		if (char3 == 'g')
			return true;

		final List<String> codes = getCodes(record);
		if (codes.contains("rh"))
			return true;
		if (codes.contains("di"))
			return true;

		if (RecordUtils.containsField(record, "2215"))
			return true;
		
		String charakter = RecordUtils.getContentOfSubfield(record, "4204", 'd');
		if (charakter != null) {
			List<String> erlaubteBegriffe = nurDissHabil ? dissHabil : charaktereDerHochschulschrift;
			if(erlaubteBegriffe.contains(charakter))
				return true;
		}

		return false;
	}

	/**
	 *
	 *
	 * @param record nicht null
	 *
	 * @return false wenn kein Titel, wenn keine Belletristik oder keine 5050
	 *         vorhanden
	 */
	public static boolean istBelletristik(final Record record) {
		Objects.requireNonNull(record);
		if (RecordUtils.isAuthority(record))
			return false;
		if (!SubjectUtils.containsDHS(record)) {
			final String tag = "1131";
			final char indicator = '9';
			final List<String> dollar9 = RecordUtils.getContentsOfFirstSubfield(record, indicator, tag);
			// Fiktionale Darstellung:
			return dollar9.contains("1071854844");
		}
		final Line line = SGUtils.getDHSLine(record);
		final List<String> conts = SubfieldUtils.getContentsOfSubfields(line);
		return conts.contains("B");
	}

	/**
	 *
	 * Ist der Titel ein Kinderbuch (Haupt- oder Nebensachgruppe)
	 *
	 * @param record nicht null
	 *
	 * @return false wenn kein Titel, wenn kein Kinderbuch oder keine 5050 vorhanden
	 */
	public static boolean istKinderbuch(final Record record) {
		Objects.requireNonNull(record);
		if (RecordUtils.isAuthority(record))
			return false;
		if (!SubjectUtils.containsDHS(record))
			return false;
		final Line line = SGUtils.getDHSLine(record);
		final List<String> conts = SubfieldUtils.getContentsOfSubfields(line);
		return conts.contains("K");
	}

	/**
	 * Ist es Übersetzung eines deutschsprachigen Werkes: bis Ende 2003 Reihe G der
	 * DNB, Teil 2, ab 2004 Reihe A.
	 *
	 * @param record nicht null
	 * @return ist Übersetzung eines deutschsprachigen Werkes
	 */
	public static boolean isTranslation(final Record record) {
		final List<String> codeAngaben = getCodes(record);
		return codeAngaben.contains("ru");
	}

	/**
	 * Liefert die Abkürzung des Nachschlagewerkes aus 0604. Zum Tag 0604 muss exakt
	 * eine Zeile gehören, sonst wird null geliefert.
	 *
	 * @param record nicht null
	 * @return Abkürzung oder null
	 */
	public static String getAbkuerzungNSW(final Record record) {
		String abkuerzung;
		abkuerzung = RecordUtils.getContentOfSubfield(record, "0604", 'b');
		return abkuerzung;
	}

	/**
	 * @param record nicht null
	 * @return Pos. 1: Physikalische Form von Feld 0500 oder (char) 0
	 */
	public static char getPhysikalischeForm(final Record record) {
		return RecordUtils.getDatatypeCharacterAt(record, 0);
	}

	/**
	 * @param record nicht null
	 * @return Pos. 4: Zuordnung des Datensatzes von 0500 oder (char) 0
	 */
	public static char getZuordnungDesDatensatzes(final Record record) {
		return RecordUtils.getDatatypeCharacterAt(record, 3);
	}

	/**
	 * @param record nicht null
	 * @return Pos. 3: Status der Beschreibung von 0500 oder (char) 0
	 */
	public static char getStatusderBeschreibung(final Record record) {
		return RecordUtils.getDatatypeCharacterAt(record, 2);
	}

	/**
	 * @param record nicht null
	 * @return Pos. 2: Bibliografische Erscheinungsform von 0500 oder (char) 0
	 */
	public static char getBibliografischeErscheinungsform(final Record record) {
		return RecordUtils.getDatatypeCharacterAt(record, 1);
	}

	/**
	 *
	 * @param record nicht null
	 * @return ist Zeitschrift, Pos. 2 von 0500 ist 'b' oder 'p'
	 */
	public static boolean isMagazine(final Record record) {
		final char c2 = getBibliografischeErscheinungsform(record);
		return c2 == 'b' || c2 == 'p';
	}

	/**
	 *
	 * @param record nicht null
	 * @return ist Online-Publikation = Elektronische Ressource im Fernzugriff
	 */
	public static boolean isOnline(final Record record) {
		return getPhysikalischeForm(record) == 'O';
	}

	/**
	 *
	 * @param record nicht null
	 * @return ist Musikalie, also beginnt 0500 mit 'G' (Tonträger) oder 'M' (Noten)
	 */
	public static boolean istMusikalie(final Record record) {
		final char physikalischeForm = getPhysikalischeForm(record);
		return physikalischeForm == 'G' || physikalischeForm == 'M';
	}

	/**
	 *
	 * @param record nicht null
	 * @return ist Druckschrift, also beginnt 0500 mit 'A'
	 */
	public static boolean istDruckschrift(final Record record) {
		return getPhysikalischeForm(record) == 'A';
	}

	public static final Predicate<Line> isGeschaeftsgangIE = line -> StringUtils
			.equals(SubfieldUtils.getGeschaeftsgang(line), "StatIE");

	public static final Predicate<Line> isGeschaeftsgangZUG = line -> StringUtils
			.equals(SubfieldUtils.getGeschaeftsgang(line), "StatZUG");

	public static final Predicate<Line> isGeschaeftsgangFOE = line -> StringUtils
			.equals(SubfieldUtils.getGeschaeftsgang(line), "StatFOE");

	/**
	 * @param record nicht null
	 * @return IE-Statistikfelder (4821 mit $zStatIE) nicht null
	 */
	public static ArrayList<Line> getIEStatistik(final Record record) {
		final ArrayList<Line> filteredLines = RecordUtils.getLines(record, isGeschaeftsgangIE,
				BibTagDB.getDB().findTag("4821"));
		return filteredLines;
	}

	/**
	 * @param record nicht null
	 * @return Zugangs-Statistikfelder (4821 mit $zStatZUG) nicht null
	 */
	public static ArrayList<Line> getZUGStatistik(final Record record) {
		final ArrayList<Line> filteredLines = RecordUtils.getLines(record, isGeschaeftsgangZUG,
				BibTagDB.getDB().findTag("4821"));
		return filteredLines;
	}

	/**
	 * @param record nicht null
	 * @return FE-Statistikfelder (4821 mit $zStatFOE) nicht null
	 */
	public static ArrayList<Line> getFEStatistik(final Record record) {
		final ArrayList<Line> filteredLines = RecordUtils.getLines(record, isGeschaeftsgangFOE,
				BibTagDB.getDB().findTag("4821"));
		return filteredLines;
	}

	/**
	 *
	 * @param record nicht null
	 * @return alle 4821
	 */
	public static ArrayList<Line> getStatistik(final Record record) {
		return RecordUtils.getLines(record, "4821");
	}

	/**
	 * @param record nicht null
	 * @return Idns der in der Manifestation verkörperten Werke + Werke bei
	 *         Zusammenstellungen (Früher: Einheitssachtitel) aus 3210 und 3211.
	 */
	public static List<String> getWorkIds(final Record record) {
		final List<String> idnsTu = RecordUtils.getContentsOfFirstSubfield(record, '9', "3210", "3211");
		return idnsTu;
	}

	/**
	 * @param record nicht null
	 * @return Idns der in der Manifestation verkörperten Werke + Werke bei
	 *         Zusammenstellungen (Früher: Einheitssachtitel) aus 3210 und 3211.
	 */
	public static List<String> getPersonIds(final Record record) {
		final ArrayList<Line> personalNameSegment = getPersonalNameSegment(record);
		return RecordUtils.extractIdns(personalNameSegment);
	}

	/**
	 * Liefert die Durchlaufzeiten: ZUG -> FOE -> IE. Ist einer der Werte unbekannt,
	 * wird er als null zurückgeliefert. Genauso, wenn der Wert negativ ist.
	 *
	 * @param record nicht null
	 * @return Durchlaufzeiten (FOE - ZUG, IE - FOE, IE - ZUG), nicht null, aber
	 *         eventuell mit null-Werten besetzt.
	 */
	public static Triplett<Long, Long, Long> getDurchlaufzeiten(final Record record) {
		Date dateZUG = null;
		Date dateFOE = null;
		Date dateIE = null;

		dateZUG = getZUGstatDate(record);
		dateFOE = getFOEstatDate(record);
		dateIE = getIEstatDate(record);

		Long durchlaufzeitFOE = dateZUG != null && dateFOE != null ? TimeUtils.getDayDifference(dateZUG, dateFOE)
				: null;
		Long durchlaufzeitIE = dateIE != null && dateFOE != null ? TimeUtils.getDayDifference(dateFOE, dateIE) : null;
		Long durchlaufzeit = dateIE != null && dateZUG != null ? TimeUtils.getDayDifference(dateZUG, dateIE) : null;

		durchlaufzeitFOE = durchlaufzeitFOE == null || durchlaufzeitFOE < 0L ? null : durchlaufzeitFOE;
		durchlaufzeitIE = durchlaufzeitIE == null || durchlaufzeitIE < 0L ? null : durchlaufzeitIE;
		durchlaufzeit = durchlaufzeit == null || durchlaufzeit < 0L ? null : durchlaufzeit;

		final Triplett<Long, Long, Long> durchlaufzeiten = new Triplett<>(durchlaufzeitFOE, durchlaufzeitIE,
				durchlaufzeit);
		return durchlaufzeiten;
	}

	/**
	 * @param record nicht null
	 * @return das erste Datum, an dem die FE den Datensatz angefasst hat oder null
	 */
	public static Date getFOEstatDate(final Record record) {
		final ArrayList<Line> feStatistik = getFEStatistik(record);
		final Date dateFOE = getDateFromStatLines(feStatistik);
		return dateFOE;
	}

	/**
	 * @param record nicht null
	 * @return das erste Datum, an dem die Erwerbung den Datensatz angefasst hat
	 *         oder null
	 */
	public static Date getZUGstatDate(final Record record) {
		final ArrayList<Line> zugStatistik = getZUGStatistik(record);
		return getDateFromStatLines(zugStatistik);
	}

	/**
	 * @param statLines Teilmenge von 4821-Feldern
	 * @return das erste Datum, an dem die entsprechende Abteilung den Datensatz
	 *         angefasst hat oder null
	 */
	public static Date getDateFromStatLines(final Collection<Line> statLines) {
		if (statLines == null || statLines.isEmpty())
			return null;
		final ArrayList<Date> mapNullFiltered = FilterUtils.mapNullFiltered(statLines,
				SubfieldUtils::getDateAusDollarD);
		return mapNullFiltered.isEmpty() ? null : Collections.min(mapNullFiltered);
	}

	/**
	 * @param statLines Teilmenge von 4821-Feldern, auch null
	 * @return die erste Nutzerkennung in der Liste statLines oder null
	 */
	public static String getNutzerkennungFromStatLines(final Collection<Line> statLines) {
		if (statLines == null || statLines.isEmpty())
			return null;
		final ArrayList<String> mapNullFiltered = FilterUtils.mapNullFiltered(statLines,
				SubfieldUtils::getNutzerkennung);
		return mapNullFiltered.isEmpty() ? null : mapNullFiltered.get(0);
	}

	/**
	 *
	 * @param statLines Menge von 4821-Feldern, auch null
	 * @return den ersten Standort der Collection: F, L oder U (anderer Standort)
	 */
	public static STANDORT_DNB getStandort(final Collection<Line> statLines) {
		final String nutzerkennung = getNutzerkennungFromStatLines(statLines);
		return RecordUtils.getStandort(nutzerkennung);
	}

	/**
	 *
	 * @param record nicht null
	 * @return F, L oder U (anderer Standort)
	 */
	public static STANDORT_DNB getStandortZugang(final Record record) {
		return getStandort(getZUGStatistik(record));
	}

	/**
	 * @param record nicht null
	 * @return das erste Datum, an dem die IE den Datensatz angefasst hat oder null
	 */
	public static Date getIEstatDate(final Record record) {
		final ArrayList<Line> ieStatistik = getIEStatistik(record);
		final Date dateIE = getDateFromStatLines(ieStatistik);
		return dateIE;
	}

	private static final Set<String> BIBLIOTHEKEN = new HashSet<>(
			Arrays.asList("009030115", "009013849", "009033645", "020583613"));

	/**
	 * Verwende stattdessen {@link ItemParser#countDNB(Record)}
	 *
	 * @param record nicht null
	 * @return Die Zahl der in Frankfurt und Leipzig (inklusive DBSM und DMA)
	 *         vorhandenen Exemplare aus Feld 4800.
	 */
	@Deprecated
	public static int getCountDNB(final Record record) {
		Objects.requireNonNull(record);
		return (int) RecordUtils.getLines(record, "4800").stream().map(Line::getIdnRelated)
				.filter(BIBLIOTHEKEN::contains).count();
	}

	/**
	 *
	 * @param record nicht null
	 * @return ist Kartenmaterial, Möglichkeiten:
	 *         <li>0500 K..
	 *         <li>0600 kt/rc
	 *         <li>1131 Karte
	 */
	public static boolean isKarte(final Record record) {
		Objects.requireNonNull(record);
		// Standardfall:
		if (getPhysikalischeForm(record) == 'K')
			return true;
		final List<String> codes = getCodes(record);
		// Es gibt auch Reihen (Advz) mit kt/rc in 0600:
		if (!CollectionUtils.intersection(codes, Arrays.asList("kt", "rc")).isEmpty())
			return true;
		// letzter Versuch, 040297837 (Karte) in 1131:
		return getNatureOfContentIDs(record).contains("040297837");
	}

	/**
	 * 
	 * @param record nicht null
	 * @return Die IDNs der Körperschaften in den Feldern 3100, 3110 und 3119
	 */
	public static Collection<String> getAlleKoerperschaftIDsDerFE(Record record) {
		return RecordUtils.getContents(record, "31[01].", '9');
	}

}
