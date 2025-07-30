package de.dnb.gnd.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.applicationComponents.tuples.Quadruplett;
import de.dnb.basics.collections.CollectionUtils;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.Tag;

/**
 * Utility-Klasse für Sacherschließungssegment am Titeldatensatz.
 *
 * @author baumann
 *
 */
public final class SubjectUtils {

	private SubjectUtils() {
	}

	public static final BibTagDB TAG_DB = BibTagDB.getDB();

	/**
	 * Die Felder, die RSWK-SWW enthalten können.
	 */
	public static final List<String> RSWK_LINE_TAGS = Arrays.asList("5100", "5101", "5102", "5103", "5104", "5105",
			"5110", "5111", "5112", "5113", "5114", "5115", "5120", "5121", "5122", "5123", "5124", "5125", "5130",
			"5131", "5132", "5133", "5134", "5135", "5140", "5141", "5142", "5143", "5144", "5145", "5150", "5151",
			"5152", "5153", "5154", "5155", "5160", "5161", "5162", "5163", "5164", "5165", "5170", "5171", "5172",
			"5173", "5174", "5175", "5180", "5181", "5182", "5183", "5184", "5185", "5190", "5191", "5192", "5193",
			"5194", "5195");

	public static Collection<Line> getDDCSegment(final Record record) {
		return RecordUtils.getLinesBetween(record, "5400", "5444");
	}

	/**
	 *
	 * @param record nicht null
	 * @param i      i-te DDC, beginnend bei 0
	 * @return nicht null, modifizierbar
	 */
	public static Collection<Line> getDDCSegment(final Record record, final int i) {
		final String pattern = "54" + i + ".";
		return RecordUtils.getLines(record, pattern);
	}

	/**
	 * Angaben zu SW-Ketten in 51X9
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static Collection<Line> get51X9(final Record record) {
		final String pattern = "51.9";
		return RecordUtils.getLines(record, pattern);
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable1Notations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'f');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable2Notations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'g');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable3ANotations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'h');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable3BNotations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'i');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable3CNotations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'j');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable4Notations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'k');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable5Notations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'l');
	}

	/**
	 * Notation ohne Präfix -Tx--.
	 * 
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getTable6Notations(final Record record) {
		final Collection<Line> lines = getAuxiliarTables(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'm');
	}

	/**
	 * Liefert die Hilfstafeln (Tags enden mit "3").
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static ArrayList<Line> getAuxiliarTables(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLines(record, "5403", "5413", "5423", "5433", "5443");
	}

	/**
	 * Liefert die Zeilen mit vollständigen DDC-Notationen (Tags enden mit "0").
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static ArrayList<Line> getCompleteDDCNotationLines(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLines(record, "5400", "5410", "5420", "5430", "5440");
	}

	/**
	 * Liefert die synthetisierte Haupt-DDC-Notation (5400).
	 *
	 * @param record nicht null
	 * @return null, wenn nicht vorhanden oder sonst
	 */
	public static String getMainDDCNotation(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getContentOfSubfield(record, "5400", 'a');
	}

	/**
	 * Gibt die Anzahl der vergebenen vollständigen DDC-Notationen, also in 54n0 die
	 * Zahl n + 1. Wenn der Datensatz bearbeitet wird, kann es sein, dass noch nicht
	 * alle 54nX-Felder nelegt sind. Dann wird die höchste 54nX-Nummer herangezogen.
	 *
	 * @param record nicht null
	 * @return Zahl
	 */
	public static int getNumberOfDDCNotations(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Collection<Line> lines = SubjectUtils.getDDCSegment(record);
		final String max = RecordUtils.getLastTag(lines);
		if (max == null)
			return 0;
		else
			return StringUtils.charAt(max, 2) - '0' + 1;
	}

	/**
	 * Liefert die Zeilen mit DDC-Grundnotationen (Tags enden mit "1").
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static ArrayList<Line> getDDCMainScheduleLines(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLines(record, "5401", "5411", "5421", "5431", "5441");
	}

	/**
	 * Liefert alle DDC-Grundnotationen (Tags enden mit "1").
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getDDCMainScheduleNotations(final Record record) {
		final Collection<Line> lines = getDDCMainScheduleLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'a');
	}

	/**
	 * Liefert die Zeilen mit DDC-Notationen anderer Haupttafeln (Tags enden mit
	 * "2").
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static ArrayList<Line> getDDCOtherScheduleLines(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLines(record, "5402", "5412", "5422", "5432", "5442");
	}

	/**
	 * Liefert alle DDC-Notationen anderer Haupttafeln (Tags enden mit "2").
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getDDCOtherScheduleNotations(final Record record) {
		final Collection<Line> lines = getDDCOtherScheduleLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'a');
	}

	/**
	 * Liefert alle vollständigen DDC-Notationen (Tags enden mit "0").
	 *
	 * @param record nicht null
	 * @return nicht null, modifizierbar
	 */
	public static List<String> getCompleteDDCNotations(final Record record) {
		final Collection<Line> lines = getCompleteDDCNotationLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, 'a');
	}

	/**
	 * Liefert die Zeilen mit Automatisch vergebenen DDC-Notationen (5470). Das
	 * sollte nur eine sein.
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static ArrayList<Line> getAutomaticDDCLines(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLines(record, "5470");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Liste der medizinischen Kurznotationen, also der Felder 5470, die
	 *         [MKN] enthalten. In der Regel sollte das eine Notation != null sein.
	 *         Im Fehlerfall kann davon abgewichen sein.
	 */
	public static Collection<String> getMKNDDCs(final Record record) {
		final ArrayList<Line> lines = getAutomaticDDCLines(record);
	//@formatter:off
        final Collection<String> mkns =
           lines.stream()
                .filter(line
                        -> Objects.equals(
                            SubfieldUtils.getContentOfFirstSubfield(line, 'e'),
                            "MKN"))
                .map(line -> SubfieldUtils.getContentOfFirstSubfield(line, 'a'))
                .collect(Collectors.toSet());
        //@formatter:on
		return mkns;
	}

	/**
	 * Die Zeilen mit den Sachgruppen (5050).
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer
	 */
	public static Collection<Line> getSubjectGroupLines(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLines(record, "5050");
	}

	/**
	 * Die Zeilen mit RSWK-Schlagwörtern MIT Permutationmuster und Angaben zur 1.
	 * Schlagwortfolge.
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer
	 */
	public static List<Line> getAllRSWKLines(final Record record) {
		Objects.requireNonNull(record);
		return RecordUtils.getLinesBetween(record, "5100", "5198");
	}

	/**
	 * Die Zeilen mit RSWK-Schlagwörtern OHNE Permutationmuster und Angaben zur 1.
	 * Schlagwortfolge.
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer
	 */
	public static List<Line> getRSWKLines(final Record record) {
		Objects.requireNonNull(record);
		final String pat = "51.[^89]";
		return RecordUtils.getLines(record, pat);
	}

	/**
	 *
	 * @param line nicht null
	 * @return Zeile ist RSWK: 5100 <= line <=5195, endet mit 0-5
	 */
	public static boolean isRWSK(final Line line) {
		Objects.requireNonNull(line);
		final Tag tag = line.getTag();
		return RSWK_LINE_TAGS.contains(tag.pica3);
	}

	/**
	 *
	 * @param line nicht null
	 * @return das getrimmte Form-SW (aus Unterfeld $a) oder null, wenn kein $a
	 *         vorhanden, wenn $a nicht mit 'f' beginnt oder, wenn ein $9 vorhanden
	 *         ist
	 */
	public static String getFormSW(final Line line) {
		Objects.requireNonNull(line);
		if (!isRWSK(line))
			return null;
		if (SubfieldUtils.containsIndicator(line, '9'))
			return null;
		final String a = SubfieldUtils.getContentOfFirstSubfield(line, 'a');
		if (a == null)
			return null;
		if (!a.startsWith("f"))
			return null;
		return a.substring(1).trim();
	}

	/**
	 *
	 * @param record nicht null
	 * @return Set der getrimmten Form-SWW
	 */
	public static Collection<String> getFormSWW(final Record record) {
		Objects.requireNonNull(record);
		final Collection<String> forms = new HashSet<>();
		getAllRSWKLines(record).forEach(line -> {
			final String f = getFormSW(line);
			if (f != null)
				forms.add(f);
		});
		return forms;
	}

	/**
	 *
	 * @param line nicht null
	 * @return ob Zeile ein Form-SW enthält
	 */
	public static boolean isFormSW(final Line line) {
		return getFormSW(line) != null;
	}

	/**
	 * Die i-te RSWK-Folge OHNE Permutationsmuster und Angaben zur 1.
	 * Schlagwortfolge.
	 *
	 * @param record nicht null
	 * @param seqNr  1-10, Vorsicht: nicht mit 0 beginnend!
	 * @return nicht null, eventuell leer
	 */
	public static List<Line> getRSWKSequence(final Record record, int seqNr) {
		Objects.requireNonNull(record);
		if (seqNr < 1 || seqNr > 10)
			throw new IllegalArgumentException("RSWK-Folge " + seqNr + " gibt es nicht");
		seqNr--;
		return RecordUtils.getLinesBetween(record, "51" + seqNr + "0", "51" + seqNr + "5");
	}

	/**
	 *
	 * @param record nicht null
	 * @return Zahl der RSWK-Folgen
	 */
	public static int getNumberOfRSWKSequenzes(final Record record) {
		Objects.requireNonNull(record);
		final List<Line> lines = getAllRSWKLines(record);
		if (lines.isEmpty())
			return 0;
		final Line lastLine = ListUtils.getLast(lines);
		final Tag lastTag = lastLine.getTag();
		return StringUtils.charAt(lastTag.pica3, 2) - '0' + 1;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Die RSWK-Folgen
	 */
	public static List<List<Line>> getRSWKSequences(final Record record) {
		Objects.requireNonNull(record);
		final int anz = getNumberOfRSWKSequenzes(record);
		final List<List<Line>> seqs = new ArrayList<>(anz);
		for (int i = 1; i <= anz; i++) {
			final List<Line> rswkSequence = getRSWKSequence(record, i);
			seqs.add(rswkSequence);
		}
		return seqs;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Eine Liste der idns der RSWK-Folgen (ohne nullen)
	 */
	public static List<List<String>> getRswkSequencesOfId(final Record record) {
		Objects.requireNonNull(record);
		final int anz = getNumberOfRSWKSequenzes(record);
		final List<List<String>> seqs = new ArrayList<>(anz);
		for (int i = 1; i <= anz; i++) {
			final List<Line> rswkSequence = getRSWKSequence(record, i);
			final List<String> rswkIDs = SubfieldUtils.getContentsOfFirstSubfields(rswkSequence, '9');
			seqs.add(rswkIDs);
		}
		return seqs;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Eine Liste der Inhalte der RSWK-Folgen. Unicode-Composition.
	 */
	public static List<List<String>> getRswkSequencesOfContent(final Record record) {
		Objects.requireNonNull(record);
		final int anz = getNumberOfRSWKSequenzes(record);
		final List<List<String>> seqs = new ArrayList<>(anz);
		for (int i = 1; i <= anz; i++) {
			final List<Line> rswkSequence = getRSWKSequence(record, i);
			final List<String> inhalte = FilterUtils.map(rswkSequence, RecordUtils::toPicaWithoutTag);
			seqs.add(inhalte);
		}
		return seqs;
	}

	/**
	 * Gibt ein Set mit den ids aller Schlagwörter, die am Titel hängen. Enthält
	 * keine nullen.
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer, keine Doppelten, modifizierbar
	 */
	public static Set<String> getRSWKidsSet(final Record record) {
		Objects.requireNonNull(record);
		final Collection<String> rswkIDs = getRSWKids(record);
		return new LinkedHashSet<>(rswkIDs);
	}

	/**
	 * @param record nicht null
	 * @return alle IDNS ohne null, eventuell mit Wiederholungen
	 */
	public static Collection<String> getRSWKids(final Record record) {
		final Collection<Line> rswkLines = getAllRSWKLines(record);
		return SubfieldUtils.getContentsOfFirstSubfields(rswkLines, '9');
	}

	/**
	 * Die Bearbeitungszeit eines Datensatzes.
	 * 
	 * @param record nicht null
	 * @return Die Bearbeitungszeit eines Datensatzes in Minuten:
	 *
	 *         <br>
	 *         <br>
	 *         RSWK und DDC Erschließung: 20 min (Reihe A ohne Sachgruppen B, K,
	 *         S)<br>
	 *
	 *         nur RSWK Erschließung: 15 min (Reihe A der Sachgruppen K, S und B in
	 *         Auswahl)<br>
	 *
	 *         nur DDC Erschließung: 10 min (Reihe B und H ohne Sachgruppen B, K,
	 *         S)<br>
	 *
	 *         Erschließung nur mit der Sachgruppe oder Kurz-DDC: 5 min (Reihe H bei
	 *         der Sachgruppe 610; Veröffentlichungen, die zur Stufe 3 der
	 *         inhaltlichen Erschließung gehören)
	 * 
	 */
	public static int getProcessingTime(final Record record) {
		final TIEFE status = getErschliessungsTiefe(record);
		if (status != null)
			return status.processingTime;
		return 0;

	}

	/**
	 * Erschließungstiefe (Sachgruppe, nur Schlagwörter, nur DDC, alles).
	 *
	 * @author baumann
	 *
	 */
	public static enum TIEFE {
		DHS("nur Sachgruppe", 5), SWW("nur Schlagwörter", 15), DDC("nur DDC", 10), FULL("SWW und DDC", 20);

		/**
		 * in einfachen Worten.
		 */
		public String verbal;

		public int processingTime;

		private TIEFE(final String verbal, final int processingTime) {
			this.verbal = verbal;
			this.processingTime = processingTime;
		}
	};

	/**
	 * Gibt die Erschließungstiefe (SWW, DDC, DHS).
	 *
	 *
	 * @param record nicht null
	 * @return Erschließungsstatus oder null
	 */
	public static TIEFE getErschliessungsTiefe(final Record record) {
		if (containsDDC(record) && containsRSWK(record))
			return TIEFE.FULL;
		if (containsRSWK(record) && !containsDDC(record))
			return TIEFE.SWW;
		if (containsDDC(record) && !containsRSWK(record))
			return TIEFE.DDC;
		if (containsDHS(record))
			return TIEFE.DHS;
		return null;
	}

	/**
	 * Gibt [DHS, DNS (wenn B, K, S), Erschließungstiefe, $E]
	 *
	 *
	 * @param record      nicht null
	 * @param map2Broader idn -> Übergeordneter Titel, auch null
	 * @return Erschließungsstatus oder (null, null, null, null)
	 */
	public static Quadruplett<DDC_SG, DDC_SG, TIEFE, String> getErschliessungsStatus(final Record record,
			final Map<Integer, Quadruplett<DDC_SG, DDC_SG, TIEFE, String>> map2Broader) {
		Objects.requireNonNull(record);
		final Line line = SGUtils.getDHSLine(record);
		if (line == null) {
			// Also war es nix; hilft die Map weiter?
			if (map2Broader == null)
				return Quadruplett.getNullQuad();
			final int idnBroader = IDNUtils.idn2int(BibRecUtils.getBroaderTitleIDN(record));
			final Quadruplett<DDC_SG, DDC_SG, TIEFE, String> broader = map2Broader.get(idnBroader);
			return broader != null ? broader : Quadruplett.getNullQuad();
		}
		final Pair<String, String> dhs = SGUtils.getDhsStringPair(line);
		final Pair<DDC_SG, DDC_SG> dhsPlusDns = SGUtils.getDHSundDNS(line);
		final TIEFE status = getErschliessungsTiefe(record);
		return new Quadruplett<>(dhsPlusDns.first, dhsPlusDns.second, status, dhs.second);
	}

	/**
	 *
	 * @param record nicht null
	 * @return 5050 da
	 */
	public static boolean containsDHS(final Record record) {
		return RecordUtils.containsField(record, "5050");
	}

	/**
	 *
	 * @param record nicht null
	 * @return 5100 da
	 */
	public static boolean containsRSWK(final Record record) {
		return RecordUtils.containsField(record, "5100");
	}

	/**
	 *
	 * Schlagwörter aus Altdaten der Deutschen Nationalbibliothek.
	 *
	 * @param record nicht null
	 * @return 5530 da
	 */
	public static boolean containsOldSWW(final Record record) {
		return RecordUtils.containsField(record, "5530");
	}

	/**
	 * Automatisch vergebene SWW.
	 *
	 * @param record nicht null
	 * @return 5540 da
	 */
	public static boolean containsAutomaticSWW(final Record record) {
		return RecordUtils.containsField(record, "5540");
	}

	/**
	 * SWW aus Fremddaten.
	 *
	 * @param record nicht null
	 * @return 5550 (GND) oder 5560 (andere Deskriptoren) da
	 */
	public static boolean containsExternalSWW(final Record record) {
		return containsExternalGND(record) || containsExternalDescriptor(record);
	}

	/**
	 * GND aus Fremddaten.
	 *
	 * @param record nicht null
	 * @return 5550 (GND) da
	 */
	public static boolean containsExternalGND(final Record record) {
		return RecordUtils.containsField(record, "5550");
	}

	/**
	 * SWW aus Fremddaten.
	 *
	 * @param record nicht null
	 * @return 5560 (andere Deskriptoren) da
	 */
	public static boolean containsExternalDescriptor(final Record record) {
		return RecordUtils.containsField(record, "5560");
	}

	/**
	 * Thema-Klassifikation.
	 *
	 * @param record nicht null
	 * @return 5460 oder 5461 da
	 */
	public static boolean containsThemaClassification(final Record record) {
		return RecordUtils.containsField(record, "5460") || RecordUtils.containsField(record, "5461");
	}

	/**
	 *
	 * @param record nicht null
	 * @return 5400 da
	 */
	public static boolean containsDDC(final Record record) {
		return RecordUtils.containsField(record, "5400");
	}

	/**
	 * Gibt alle DDC-Notationen am Titel (Grundnotationen und synthetisierte); keine
	 * Duplikate, da Set; die Hilfstafeln mit dem korrekten Präfix -TX--.
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static Set<String> getAllDDCNotations(final Record record) {
		Objects.requireNonNull(record);
		final List<Line> lines = new LinkedList<>();
		lines.addAll(getDDCMainScheduleLines(record));
		lines.addAll(getDDCOtherScheduleLines(record));
		lines.addAll(getAuxiliarTables(record));
		final Set<String> ddcs = new LinkedHashSet<>();
		ddcs.addAll(getCompleteDDCNotations(record));
		for (final Line line : lines) {
			ddcs.add(RecordUtils.toPicaWithoutTag(line));
		}
		return ddcs;
	}

	/**
	 * Gibt alle nichtsynthetisierten DDC-Notationen am Titel; keine Duplikate, da
	 * Set; die Hilfstafeln mit dem korrekten Präfix -TX--.
	 *
	 * @param record nicht null
	 * @return nicht null
	 */
	public static Set<String> getAllsimpleDDCNotations(final Record record) {
		Objects.requireNonNull(record);
		final List<Line> lines = new LinkedList<>();
		lines.addAll(getDDCMainScheduleLines(record));
		lines.addAll(getDDCOtherScheduleLines(record));
		lines.addAll(getAuxiliarTables(record));
		final Set<String> ddcs = new LinkedHashSet<>();
		for (final Line line : lines) {
			final String picaWithoutTag = RecordUtils.toPicaWithoutTag(line);
			// trim(), weil es fehlerhafte Daten gibt:
			ddcs.add(picaWithoutTag.trim());
		}
		return ddcs;
	}

	public static void main(final String[] args) throws IllFormattedLineException, ClassNotFoundException, IOException {
		final Record record = RecordUtils.readFromClip();
		System.out.println(containsFremdBK(record));
	}

	/**
	 * Gibt eine Liste der ISILs der Bibliotheken, die für die RSWK-Ketten
	 * verantwortlich sind. Diese stehen in 51X9, $e. Die DNB-IE hat "DE-101".
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer
	 */
	public static List<String> vergebendeBib51X9(final Record record) {
		final Collection<Line> angaben = get51X9(record);
		return SubfieldUtils.getContents(angaben, 'e');
	}

	/**
	 * Gibt eine Liste der ISILs der Verbünde, die für die RSWK-Ketten
	 * verantwortlich sind. Diese stehen in 51X9, $r. Die DNB-IE hat "DE-101".
	 *
	 * @param record nicht null
	 * @return nicht null, eventuell leer
	 */
	public static List<String> verbuende51X9(final Record record) {
		final Collection<Line> angaben = get51X9(record);
		return SubfieldUtils.getContents(angaben, 'r');
	}

	public static boolean containsEigeneBK(Record record) {
		return RecordUtils.containsField(record, "5301");

	}

	public static boolean containsFremdBK(Record record) {
		ArrayList<Line> classlines = RecordUtils.getLines(record, "5450");
		return classlines.stream()
				.anyMatch(line -> "BK".equalsIgnoreCase(SubfieldUtils.getContentOfFirstSubfield(line, 'b')));

	}

}
