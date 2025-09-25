package de.dnb.gnd.parser.tag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;

public final class BibTagDB extends TagDB {

	/**
	 * Zeitliche Gültigkeit.
	 */
	public static final Indicator DOLLAR_Z_ZEIT = new Indicator("$z", "", 'z', "Zeitliche Gültigkeit", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_6 = new Indicator("{", "}", '6',
			"GND-IDN (temporär bei maschineller Übernahme)", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_B_CHRON = new Indicator("", "#", 'b',
			"Codierte Angabe für die Art der chronologischen Beziehung (obligatorisch)", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_R = new Indicator("{", "}", 'r',
			"Drucktext (Einleitungstext und Titel; in Altdaten und " + "DNB-ZDB-Sätzen bis zum RDA-Umstieg)", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_L_SCHOEPF = new Indicator("$l", "", 'l', "Geistiger Schöpfer", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_Y_URN = new Indicator("$y", "", 'y', "Identifier related product: URN", NR,
			"");

	/**
	 *
	 */
	public static final Indicator DOLLAR_U_UNSPEZ = new Indicator("$u", "", 'u',
			"Identifier related product: unspezifiziert", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_X_DOI = new Indicator("$x", "", 'x', "Identifier related product: DOI", NR,
			"");

	/**
	 * 'i', "Identifier related product: ISBN".
	 */
	public static final Indicator DOLLAR_I_ISB = new Indicator("$i", "", 'i', "Identifier related product: ISBN", NR,
			"");

	/**
	 * 'o', "Sonstige Identifier für die andere Ausgabe".
	 */
	public static final Indicator DOLLAR_O_ANDERE = new Indicator("$o", "", 'o',
			"Sonstige Identifier für die andere Ausgabe", NR, "");

	/**
	 * 'h', "Physische Beschreibung".
	 */
	public static final Indicator DOLLAR_H_PHYS = new Indicator("$h", "", 'h', "Physische Beschreibung", NR, "");

	/**
	 * 'f', "Datum".
	 */
	public static final Indicator DOLLAR_F_DATUM = new Indicator("$f", "", 'f', "Datum", NR, "");

	/**
	 * 'e', "Verlag".
	 */
	public static final Indicator DOLLAR_E_VERLAG = new Indicator("$e", "", 'e', "Verlag", NR, "");

	/**
	 * 'd', "Ort".
	 */
	public static final Indicator DOLLAR_D_ORT = new Indicator("$d", "", 'd', "Ort", NR, "");

	/**
	 * 't', "Titel".
	 */
	public static final Indicator DOLLAR_TITEL = new Indicator("$t", "", 't', "Titel", NR, "");

	/**
	 * 'n', "Anmerkung".
	 */
	public static final Indicator INDICATOR_N_ANM = new Indicator("$n", "", 'n', "Anmerkung", NR, "");

	/**
	 * 'a', "Beziehungskennzeichnung", manchmal auch "Einleitungstext".
	 */
	public static final Indicator DOLLAR_A_BEZ = new Indicator("", "", 'a', "Beziehungskennzeichnung", NR, "");

	/**
	 * 'y', "Standardidentifier".
	 */
	public static final Indicator DOLLAR_Y_IDENTIF = new Indicator("$y", "", 'y', "Standardidentifier", R, "");

	/**
	 * '4', "Beziehungskennzeichnung (Code)".
	 */
	public static final Indicator DOLLAR_4_CREA = new Indicator("$4", "", '4', "Beziehungskennzeichnung (Code)", R, "");

	/**
	 * 'B', "Beziehungskennzeichnung (Text)".
	 */
	public static final Indicator DOLLAR_B_CREA = new Indicator("$B", "", 'B', "Beziehungskennzeichnung (Text)", R, "");

	/**
	 * Datum der Erstellung.
	 */
	public static final Indicator DOLLAR_D_DATUM = new Indicator("$D", "", 'D', "Datum (JJJJ-MM-TT)", NR, "");

	/**
	 * Konfidenzwert.
	 */
	public static final Indicator DOLLAR_K = new Indicator("$K", "", 'K', "Konfidenzwert (1,000 – 0,000)", NR, "");

	/**
	 * Herkunft.
	 */
	public static final Indicator DOLLAR_H = new Indicator("$H", "", 'H', "Herkunft ", NR, "");

	/**
	 * Erfassungsart.
	 */
	public static final Indicator DOLLAR_E = new Indicator("$E", "", 'E', "Kennzeichnung der Erfassungsart ", NR, "");

	/**
	 * Feldzuordnung.
	 */
	public static final Indicator DOLLAR_T = new Indicator("$T", "", 'T',
			"Feldzuordnung bei nicht-lateinischen Schriftzeichen", NR, "");

	/**
	 * Schriftcode.
	 */
	public static final Indicator DOLLAR_U_GR = new Indicator("$U", "%%", 'U',
			"Schriftcode bei nicht-lateinischen Schriftzeichen  (ISO 15924)", NR, "");

	private static final int MAX_PER_LIBRARY = 20;

	private static BibTagDB db = new BibTagDB();

	public static final List<String> HEADERS = Arrays.asList("0100", "0200", "0210", "0230", "0500", "0598", "0596",
			"0599", "0600", "0601", "0602", "0701", "1100", "1101", "1105", "1109", "1110", "1130", "1131", "1132",
			"1140", "1500", "1698", "1700", "1800", "1805", "2000", "2005", "2006", "2009", "2010", "2011", "2012",
			"2013", "2015", "2016", "2017", "2018", "2019", "2020", "2029", "2035", "2040", "2050", "2051", "2052",
			"2061", "2100", "2105", "2106", "2110", "2111", "2115", "2150", "2185", "2198", "2199", "2200", "2205",
			"2215", "2220", "2225", "2230", "2240", "2241", "2242", "2245", "2246", "2260", "2275");

	/**
	 * Exemplardaten.
	 */
	//@formatter:off
	public static final List<String> HOLDINGS = Arrays.asList("7001",
		"7002", "7099",
		"4800", "4801", "4802", "4803", "4820", "4821",
		"6700", "6710", "6800", "6809",
		"7100", "7101", "7108", "7109",	"7120", "7130", "7131", "7131",
		"7133", "7135", "7136", "7137",	"7138", "7150", "7159", "7800",
		"7900",
		"8000", "8001", "8031",	"8032", "8034", "8100", "8410", "8448",
		"8449", "8465", "8466",	"8467", "8510", "8595", "8598");
	//@formatter:on

	/**
	 * Fussnoten.
	 */
	public static final List<String> NOTES = Arrays.asList("4200", "4201", "4202", "4203", "4204", "4207", "4208",
			"4212", "4213", "4215", "4216", "4219", "4220", "4221", "4222", "4224", "4225", "4226", "4227", "4232",
			"4233", "4234", "4237", "4251");

	/**
	 * Segment Titelverknüpfungen.
	 */
	public static final List<String> TITLE_LINKS = Arrays.asList("4241", "4242", "4243", "4244", "4245", "4251", "4261",
			"4262");

	/**
	 * Segment Sonstige Angaben.
	 */
	public static final List<String> OTHER_INFORMATIONS = Arrays.asList("4260");

	/**
	 * @return the db
	 */
	public static BibTagDB getDB() {
		return db;
	}

	/*
	 * Folgende Pica+-Tags werden nicht erkannt, weil es keine Pica3-Tags dazu gibt.
	 * Eventuell muss man da noch etwas drehen:
	 *
	 * 001@ Suppliercode, Userbits 001A Erfassungskennung 001B Änderungskennung 001D
	 * Statusänderung 001E Kennzeichnung logisch gelöscht 001Q internes Feld 001U
	 * Kennzeichnung UTF8 001X Owner main extension
	 *
	 * (Quelle: https://wiki.d-nb.de/display/ILTIS/GND-Berechtigungen)
	 */

	private BibTagDB() {

		BibliographicTag newBibTag;
		GNDTag newGndTag;
		Tag inheritedTag;
		GNDTag gndBasis;
		Tag bibBasis;

		// ----------- GND-artige

		newGndTag = new DefaultGNDTag("0100", "003@", "Identifikationsnummer des Datensatzes (m)", NR, "", "");
		addTag(newGndTag);
		newGndTag.add(new Indicator('0', "Identifikationsnummer des Datensatzes (m)", NR, ""));

		newGndTag = new DefaultGNDTag("0200", "001A", "Erfassungskennung; Datum der Ersterfassung (m)", NR, "008",
				"Date entered on file");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Erfassungskennung; Datum der Ersterfassung (m)", NR, ""));

		newGndTag = new DefaultGNDTag("0210", "001B", "Änderungskennung; Datum und Uhrzeit der letzten Änderung (m)",
				NR, "005", "Date and Time of Latest Transaction");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Änderungskennung; \":\"; Datum", NR, ""));
		newGndTag.add(new Indicator("  ", "", 't', "Uhrzeit HH:MM:SS", NR, ""));

		newGndTag = new DefaultGNDTag("0230", "001D", "Kennung bei Statusänderung; Datum (m)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Kennung bei Statusänderung; Datum (m)", NR, ""));

		newGndTag = new DefaultGNDTag("0500", "002@", "Bibliografische Gattung/Status", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Bibliografische Gattung/Status", NR, ""));

		newGndTag = new DefaultGNDTag("0501", "002C", "Inhaltstyp", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Inhaltstyp in ausgeschriebener Form", NR, ""));
		newGndTag.add(new Indicator('b', "Inhaltstyp in codierter Form", NR, ""));
		newGndTag.add(DOLLAR_2);
		newGndTag.add(new Indicator('3', "Umschreibung", NR, ""));
		newGndTag.add(new Indicator('X', "Zuordnung", NR, ""));

		newGndTag = new DefaultGNDTag("0502", "002D", "Medientyp", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Medientyp in ausgeschriebener Form", NR, ""));
		newGndTag.add(new Indicator('b', "Medientyp in codierter Form", NR, ""));
		newGndTag.add(DOLLAR_2);
		newGndTag.add(new Indicator('3', "Umschreibung", NR, ""));
		newGndTag.add(new Indicator('X', "Zuordnung", NR, ""));

		newGndTag = new DefaultGNDTag("0503", "002E", "Datenträgertyp", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Datenträgertyp in ausgeschriebener Form", NR, ""));
		newGndTag.add(new Indicator('b', "Datenträgertyp in codierter Form", NR, ""));
		newGndTag.add(DOLLAR_2);
		newGndTag.add(new Indicator('3', "Umschreibung", NR, ""));
		newGndTag.add(new Indicator('X', "Zuordnung", NR, ""));

		newGndTag = new DefaultGNDTag("0550", "002M", "Angaben zur Herkunft des Datensatzes", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Format, aus dem der Datensatz konvertiert wurde", NR, ""));
		newGndTag.add(new Indicator('b', "Schnittstelle, über die der Datensatz geliefert wurde", NR, ""));
		newGndTag.add(new Indicator('c', "Lieferant", NR, ""));
		newGndTag.add(new Indicator('d', "Qualitätslevel des gelieferten Datensatzes", NR, ""));
		newGndTag.add(new Indicator('D', "Datum des Imports", NR, ""));

		newGndTag = new DefaultGNDTag("0551", "002N", "Erschließungslevel", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Erschließungslevel", NR, ""));
		newGndTag.add(new Indicator('b', "„m“ / „i“ (aktuelles Level kommt durch intellektuelle bzw. "
				+ "maschinelle Änderungen am Datensatz zustande)", NR, ""));
		newGndTag.add(new Indicator('D', "Datum der Levelvergabe", NR, ""));

		newGndTag = new DefaultGNDTag("0595", "009L",
				"Geschäftsgangs- und Änderungsinformationen für: \"Liste der fachlichen Nachschlagewerke\"", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Korrekturdatum (JJ-MM-TT)", NR, ""));
		newGndTag.add(new Indicator('b', "Codierung („neu“, „aend“, „aufl“)", NR, ""));

		newGndTag = new DefaultGNDTag("0598", "009N", "Bearbeiterzuordnung bei Netzpublikationen", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Bearbeiter", NR, ""));

		newGndTag = new EnumeratingTag("0600", "017A", "Code-Angaben", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new EnumeratingTag("0601", "017B", "Kennzeichnungsfeld für Nationallizenzen", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new EnumeratingTag("0602", "017C",
				"Kennzeichnung zur Erstellung digitaler Daten (nicht für alle Netzpublikationen)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new EnumeratingTag("0603", "017D", "Code für Materialart", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new DefaultGNDTag("0604", "017E", "Informationen zur \"Liste der fachlichen Nachschlagewerke\"", NR,
				"", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Zuordnung \"ListeNSW\"", R, ""));
		newGndTag.add(new Indicator('b', "Zitierform des NSW", R, ""));
		newGndTag.add(new Indicator('c', "Relevanzkennzeichnung", R, ""));
		newGndTag.add(new Indicator('d', "Rangfolge", R, ""));
		newGndTag.add(new Indicator('e', "Formalgruppe", R, ""));
		newGndTag.add(new Indicator('D', "Datum Ersterfassung (JJ-MM-TT)", R, ""));

		newGndTag = new DefaultGNDTag("1101", "016A", "Materialspezifische Codes für Elektronische Ressourcen", R, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Codierte Angaben", NR, ""));

		newGndTag = new DefaultGNDTag("1105", "016E", "Materialspezifische Codes für Mikroformen", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Codierte Angaben", NR, ""));

		newGndTag = new DefaultGNDTag("1106", "016F",
				"Codierte Angaben zu Werksätzen sowie zum Inhalts- und Interpretenvermerk", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Codierte Angaben", NR, ""));

		newGndTag = new DefaultGNDTag("1108", "001F", "Copyright-Datum, Vertriebsdatum, Herstellungsdatum", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Angabe des Jahres in Sortierform (immer 4 Ziffern)", NR, ""));
		newGndTag.addDefaultFirst(
				new Indicator('b', "Angabe eines abschließenden Jahres in Sortierform (immer 4 Ziffern)", NR, ""));
		newGndTag.addDefaultFirst(new Indicator('n', "Copyrightdatum in Vorlageform", NR, ""));
		newGndTag.addDefaultFirst(new Indicator('o', "Vertriebsdatum in Vorlageform", NR, ""));
		newGndTag.addDefaultFirst(new Indicator('p', "Herstellungsdatum in Vorlageform", NR, ""));

		newGndTag = new DefaultGNDTag("1109", "011B", "Erscheinungsjahr der Sekundärausgabe", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Angabe des Erscheinungsjahres in Sortierform (immer 4 Ziffern)", NR, ""));
		newGndTag.add(new Indicator("-", "", 'b',
				"Angabe eines abschließenden Erscheinungsjahres (bei Zeitschriften und Schriftenreihen)", NR, ""));
		newGndTag.add(new Indicator(" $ ", "", 'n',
				"Erscheinungsjahr in Vorlageform, sofern abweichend von der Angabe in Sortierform", NR, ""));

		newBibTag = new BibliographicTag("1110", "011E", "Entstehungsdatum (§34,35 RNA), sonstige Datumsangaben ", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 's',
				"Entstehungsdatum in Vorlageform, sofern abweichend von der Angabe in Sortierform", NR, ""));
		newBibTag.add(new Indicator("*", "", 'r', "Genaues Datum oder Zeitraum in Sortierform", NR, ""));
		newBibTag.add(new Indicator("$a", "", 'a', "Angabe des Erscheinungsjahres in Sortierform (optional)", NR, ""));
		newBibTag
				.add(new Indicator("$b", "", 'b', "Angabe eines abschließenden Erscheinungsjahres (optional)", NR, ""));
		newBibTag.add(new Indicator("$4", "", '4', "Art des Datums: Codes für Laufzeit, Fundzeitraum", NR, ""));

		newGndTag = new DefaultGNDTag("1130", "013C", "Datenträger", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "Code; weitere Codes werden mit \";\" angefügt", R, ""));
		newGndTag.add(DOLLAR_9);
		newGndTag.add(DOLLAR_8);
		newGndTag.add(new Indicator("$x", "", 'x', "Allgemeine Unterteilung", R, ""));
		newGndTag.add(new Indicator("$y", "", 'y', "Chronologische Unterteilung", R, ""));
		newGndTag.add(new Indicator("$z", "", 'z', "Geografische  Unterteilung", R, ""));
		newGndTag.add(TagDB.DOLLAR_2);

		newBibTag = new BibliographicTag("1131", "013D", "Inhaltstyp", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator("$x", "", 'x', "Allgemeine Unterteilung", R, ""));
		newBibTag.add(new Indicator("$y", "", 'y', "Chronologische Unterteilung", R, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "Geografische  Unterteilung", R, ""));
		newBibTag.add(TagDB.DOLLAR_2);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		newBibTag.addAlternative(
				new Indicator("", "", ";", true, 'a', "Code; weitere Codes werden mit \";\" angefügt", R, ""));
		newBibTag.addAlternative(new Indicator("{", "}", '6', "GND-IDN (temporär bei maschineller Übernahme)", R, ""));
		newBibTag.addAlternative(new Indicator("$x", "", 'x', "Allgemeine Unterteilung", R, ""));
		newBibTag.addAlternative(new Indicator("$y", "", 'y', "Chronologische Unterteilung", R, ""));
		newBibTag.addAlternative(new Indicator("$z", "", 'z', "Geografische  Unterteilung", R, ""));
		newBibTag.addAlternative(TagDB.DOLLAR_2);
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);

		newGndTag = new DefaultGNDTag("1132", "013E", "Formangaben, einschließlich musikalische Ausgabeform", R, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "Code; weitere Codes werden mit \";\" angefügt", R, ""));
		newGndTag.add(DOLLAR_9);
		newGndTag.add(DOLLAR_8);
		newGndTag.add(TagDB.DOLLAR_2);

		newGndTag = new DefaultGNDTag("1133", "013F", "Zielgruppe", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "Term; weitere Terme werden mit \";\" angefügt", R, ""));
		newGndTag.add(DOLLAR_9);
		newGndTag.add(DOLLAR_8);
		newGndTag.add(TagDB.DOLLAR_2);

		newGndTag = new EnumeratingTag("1140", "013H",
				"Veröffentlichungsart und Inhalt bei Zeitschriften/Schriftenreihen (MAB-Codes) ", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", '0', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new DefaultGNDTag("1505", "010E", "Katalogisierungsquelle", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('b', "Katalogisierungssprache", NR, ""));
		newGndTag.add(new Indicator('e', "Beschreibungsfestlegungen", R, ""));

		newGndTag = new DefaultGNDTag("1600", "038A", "Verknüpfung vom Werksatz zum Sammelwerksatz", NR, "", "");
		addTag(newGndTag);
		newGndTag.add(DOLLAR_9);
		newGndTag.add(DOLLAR_8);

		newGndTag = new EnumeratingTag("1800", "018@", "Code für Erscheinungsfrequenz (MAB-Codes)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new DefaultGNDTag("1805", "018A", "Publikationsstatus (MAB-Codes) (verwendet bis 28.02.2007)", NR,
				"", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Publikationsstatus in codierter Form", NR, ""));

		newGndTag = new DefaultGNDTag("2006", "005E", "Einbandart, Preis, Lieferbedingungen, Sonstiges zur ISSN", R, "",
				"");
		addTag(newGndTag);
		newGndTag
				.addDefaultFirst(new Indicator('f', "Einbandart, Lieferbedingungen und/oder Preis, Sonstiges", NR, ""));

		newBibTag = new BibliographicTag("2040", "004K", "Europäische Artikel Nummer (EAN)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", '0', "EAN", NR, ""));
		newBibTag.add(new Indicator("(", ")", 'c', "Kommentar zur EAN", NR, ""));
		newBibTag.add(new Indicator("%", "", 'f', "Einbandart, Lieferbedingungen und/oder Preis Sonstiges", NR, ""));

		bibBasis = newBibTag;
		newBibTag = new BibliographicTag("2041", "004C", "Universal Product Code (UPC)", R, "", "");
		newBibTag.addInherited(bibBasis);

		newGndTag = new DefaultGNDTag("2050", "004U", "Persistent Identifier: URN (Level0)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Persistent Identifier vom Typ \"URN\"", NR, ""));

		newGndTag = new DefaultGNDTag("2051", "004P", "Persistent Identifier: DOI (Level0)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Persistent Identifier vom Typ \"DOI\"", NR, ""));

		newGndTag = new DefaultGNDTag("2052", "004R", "Persistent Identifier: Handle (Level0)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Persistent Identifier vom Typ \"Handle\"", NR, ""));

		newGndTag = new DefaultGNDTag("2054", "004J", "International Standard Recording Code (ISRC)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "International Standard Recording Code (ISRC)", NR, ""));

		newGndTag = new DefaultGNDTag("2055", "004O", "ISBN-A (Actionable ISBN)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "ISBN-A (Actionable ISBN)", NR, ""));

		newGndTag = new DefaultGNDTag("2060", "006G", "DMA-Nummer (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "DMA-Nummer (nur aus Altdatenkonversion)", NR, ""));

		newGndTag = new DefaultGNDTag("2061", "006H", "DBSM-Nummer (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "DBSM-Nummer (nur aus Altdatenkonversion)", NR, ""));

		newGndTag = new DefaultGNDTag("2100", "006T", "Anzeigenummer Neuerscheinungsdienst (m)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Anzeigenummer Neuerscheinungsdienst (m)", NR, ""));

		newGndTag = new DefaultGNDTag("2106", "006W",
				"Anzeigenummer der Deutschen Nationalbibliografie der DDR (m) (nur aus Altdatenkonversion)", NR, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0',
				"Anzeigenummer der Deutschen Nationalbibliografie der DDR (m) (nur aus Altdatenkonversion)", NR, ""));

		newGndTag = new DefaultGNDTag("2110", "006Z", "ZDB-Nummer", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "ZDB-Nummer", NR, ""));

		newGndTag = new DefaultGNDTag("2111", "006S", "ZDB-Nummern umgelenkter Datensätze", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "ZDB-Nummern umgelenkter Datensätze", NR, ""));

		newGndTag = new DefaultGNDTag("2115", "006D",
				"IDN der Deutschen Nationalbibliothek bei DNB-Zeitschriften und DNB-Schriftenreihen in der ZDB für den Erhalt der alten DBN/IDN",
				R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0',
				"IDN der Deutschen Nationalbibliothek bei DNB-Zeitschriften und DNB-Schriftenreihen in der ZDB für den Erhalt der alten DBN/IDN",
				NR, ""));

		newGndTag = new DefaultGNDTag("2150", "006V",
				"Identifikationsnummer des Verzeichnis lieferbarer Bücher (VLB) oder sonstige eindeutige Identifizierung bei Netzpublikationen",
				R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0',
				"Identifikationsnummer des Verzeichnis lieferbarer Bücher (VLB) oder sonstige eindeutige Identifizierung bei Netzpublikationen",
				NR, ""));

		newGndTag = new DefaultGNDTag("2185", "006N", "SWETS-Nummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "SWETS-Nummer", NR, ""));

		newGndTag = new DefaultGNDTag("2198", "006X",
				"Eindeutige Identifizierung der abliefernden Stelle (z. B. IDO [Identifizierung des Verlages aus dem DNB-eigenem DeliveryMangementSystem für Netzpublikationen])",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0',
				"Eindeutige Identifizierung der abliefernden Stelle (z. B. IDO [Identifizierung des Verlages aus dem DNB-eigenem DeliveryMangementSystem für Netzpublikationen])",
				NR, ""));
		newGndTag.add(new Indicator('i', "weitere Identifizierung eines Ablieferes (keine IDO)", NR, ""));

		newGndTag = new DefaultGNDTag("2200", "007C", "CODEN", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "CODEN", NR, ""));

		newGndTag = new DefaultGNDTag("2205", "007F", "Reportnummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Reportnummer", NR, ""));

		newGndTag = new DefaultGNDTag("2215", "007E", "Hochschulschriften-Nummer (m)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Hochschulschriften-Nummer (m)", NR, ""));

		newGndTag = new DefaultGNDTag("2220", "007A", "Postvertriebskennzeichen ", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Postvertriebskennzeichen ", NR, ""));

		newGndTag = new DefaultGNDTag("2225", "007B", "Amtliche Druckschriftennummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Amtliche Druckschriftennummer", NR, ""));

		newGndTag = new DefaultGNDTag("2230", "007D", "Verlags-, Produktions- u. Bestellnummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Verlags-, Produktions- u. Bestellnummer", NR, ""));

		newGndTag = new DefaultGNDTag("2246", "007L",
				"Identifikationsnummer des Datensatzes im Verarbeitungssystem der Deutschen Nationalbibliothek Leipzig (m) (nur aus Altdatenkonversion)",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0',
				"Identifikationsnummer des Datensatzes im Verarbeitungssystem der Deutschen Nationalbibliothek Leipzig (m) (nur aus Altdatenkonversion)",
				NR, ""));

		newGndTag = new DefaultGNDTag("2260", "007M", "Katalogkartennummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Katalogkartennummer", NR, ""));

		newGndTag = new DefaultGNDTag("2275", "007P", "Fingerprint (benutzt im DBSM)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Fingerprint (benutzt im DBSM)", NR, ""));

		newGndTag = new DefaultGNDTag("2320", "004S", "Matrizenstammnummer (Etikett) (Historische Tonträger) ", NR, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Matrizenstammnummer", NR, ""));

		newGndTag = new DefaultGNDTag("2321", "004T", "(Andere) Etikettennummer(n) (Historische Tonträger)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Etikettennummer", NR, ""));

		newGndTag = new DefaultGNDTag("2322", "004V", "Matrizenstammnummer (Spiegel) (Historische Tonträger)", R, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Matrizenstammnummer", NR, ""));

		newGndTag = new DefaultGNDTag("2323", "004W", "(Andere) Spiegelnummer(n) (Historische Tonträger)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Etikettennummer", NR, ""));

		newGndTag = new DefaultGNDTag("2324", "004X", "Mechanisches Copyright (Historische Tonträger)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "mechanisches Copyright", NR, ""));

		newGndTag = new DefaultGNDTag("2325", "004Y", "Seitenzählung (in Sets) (Historischer Tonträger)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Seitenzählung", NR, ""));

		newBibTag = new BibliographicTag("2326", "004Q", "Sonstige Nummern (Historische Tonträger)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Art der Nummer", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "Sonstige Nummer", NR, ""));

		newGndTag = new DefaultGNDTag("4011", "021N", "Zusätze und Verfasserangabe zur gesamten Vorlage", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a',
				"Undifferenzierter Text RAK-gemäße Deskriptionszeichen; keine Differenzierung in Unterfelder.", NR,
				""));

		newGndTag = new DefaultGNDTag("4019", "021Z", "Objektbezeichnung auf Manifestationsebene", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Objektbezeichnung", NR, ""));
		newGndTag.add(new Indicator('B', "Typ der Objektbezeichnung", NR, ""));

		newGndTag = new DefaultGNDTag("4025", "031@", "Erscheinungsverlauf", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Erscheinungsverlauf bei Zeitschriften/Schriftenreihen", NR, ""));

		newGndTag = new DefaultGNDTag("4026", "035E", "Kartographische Materialien: mathematische Angaben", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Maßstab bei Karten", NR, ""));
		newGndTag.add(new Indicator(" / ", "", 'c', "Verfasserangabe", NR, ""));

		newGndTag = new DefaultGNDTag("4068", "033Q",
				"Umfangsangabe der Sekundärausgabe (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)", NR,
				"", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Umfangsangabe der Sekundärausgabe", NR, ""));

		newGndTag = new DefaultGNDTag("4105", "036H",
				"Zugehörigkeit zu einer Sammlung (Verknüpfung zu einem Qd-/ oder V*-Satz)", R, "", "");
		addTag(newGndTag);
		newGndTag.add(TagDB.DOLLAR_9);
		newGndTag.add(TagDB.DOLLAR_8);

		newGndTag = new DefaultGNDTag("4110", "036L",
				"1. gezählte Schriftenreihe der Sekundärausgabe (Vorlageform) (wird als Vorlageform zur Verknüpfungsangabe in 4120 angegeben, falls maschinell generierte Gesamttitelangabe der VF aus dem ver- knüpften Datensatz des Gesamttitels nicht zum richtigen Ergebnis führt) (bei Zeitschriften verwendet bis 01.03.2007)",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Sachtitel (VF) (@), ggf. einschließlich UR-Angaben \"_:_\"", NR, ""));
		newGndTag.add(new Indicator(" // ", "", 'b',
				"KV-Ergänzung zum Sachtitel, ggf. einschließlich UR-Angaben \"_:_\"", NR, ""));
		newGndTag.add(new Indicator(" ; ", "", 'l',
				"Bandangabe (Bezeichnung u. Zählung, Parallelzählung, UR-Angaben). In  diesem Unterfeld wird alles ohne weitere Differenzierung gespeichert, was  bei Erfassung der Vorlageform nach dem ersten auftretenden \"_;_\" steht.",
				NR, ""));

		newGndTag = new DefaultGNDTag("4111", "036L/01", "2. gezählte Schriftenreihe der Sekundärausgabe (Vorlageform)",
				NR, "", "");
		addTag(newGndTag);
		inheritedTag = getPica3("4110");
		newGndTag.addInherited(inheritedTag);

		newGndTag = new DefaultGNDTag("4112", "036L/02", "3. gezählte Schriftenreihe der Sekundärausgabe (Vorlageform)",
				NR, "", "");
		addTag(newGndTag);
		inheritedTag = getPica3("4110");
		newGndTag.addInherited(inheritedTag);

		newGndTag = new DefaultGNDTag("4119", "036L/09",
				"1. - 3. ungezählte Schriftenreihe der Sekundärausgabe (Vorlageform) (bei Zeitschriften verwendet bis 01.03.2007)",
				R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Sachtitel (VF) (@), ggf. einschließlich UR-Angaben \"  :  \"", NR, ""));
		newGndTag.add(new Indicator(" // ", "", 'b',
				"KV-Ergänzung zum Sachtitel, ggf. einschließlich UR- Angaben \"  :  \"", NR, ""));

		newGndTag = new DefaultGNDTag("4130", "036A", "Vorlageform zur mehrbändigen begrenzten Überordnung in 4140", NR,
				"", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a',
				"Gesamttitelangabe (@) Innerhalb der Gesamttitelangabe werden die RAK- gemäßen Deskriptionszeichen verwendet. Für KV- Ergänzung: \"  //  \".",
				NR, ""));

		newGndTag = new DefaultGNDTag("4150", "036C",
				"Vorlageform zur Überordnung in 4160 (einschl. der Abteilungsangaben)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a',
				"Gesamttitelangabe (@) Innerhalb der Gesamttitelangabe werden die RAK- gemäßen Deskriptionszeichen verwendet. Für KV- Ergänzung: \"  //  \"",
				NR, ""));

		newGndTag = new DefaultGNDTag("4170", "036E", "1. gezählte Schriftenreihe (Vorlageform)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Sachtitel (VF) (@), ggf. einschließlich UR-Angaben \"  :  \"", NR, ""));
		newGndTag.add(new Indicator(" // ", "", 'b',
				"KV-Ergänzung zum Sachtitel, ggf. einschließlich UR- Angaben \"  :  \"", NR, ""));
		newGndTag.add(new Indicator(" ; ", "", 'l',
				"Bandangabe (Bezeichnung u. Zählung, Parallelzählung, UR-Angaben). In diesem Unterfeld wird alles ohne wei- tere Differenzierung gespeichert, was bei Erfassung der Vorlageform nach dem ersten auftretenden \"  ;  \" steht.",
				NR, ""));

		newGndTag = new DefaultGNDTag("4171", "036E/01", "2. gezählte Schriftenreihe (Vorlageform)", NR, "", "");
		addTag(newGndTag);
		inheritedTag = getPica3("4170");
		newGndTag.addInherited(inheritedTag);

		newGndTag = new DefaultGNDTag("4172", "036E/02",
				"3. gezählte Schriftenreihe (Vorlageform) (bei Zeitschriften verwendet bis 01.03.2007)", NR, "", "");
		addTag(newGndTag);
		inheritedTag = getPica3("4170");
		newGndTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4200", "047C", "Zusätzliche Sucheinstiege", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Stichwörter in abweichender Orthographie (@)", NR, ""));
		newBibTag.add(new Indicator("**", "", 'b', "indexierungsrelevante Titel bzw.Begriffe (@)", NR, ""));

		newGndTag = new DefaultGNDTag("4201", "037A", "Unaufgegliederte Fußnoten", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a',
				"- ohne Unterfeldaufgliederung - Mehrere Fußnoten werden durch \".  -  \" voneinander getrennt.", NR,
				""));

		newGndTag = new DefaultGNDTag("4202", "046N", "Deutsche Übersetzung des Hauptsachtitels", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Deutsche Übersetzung des Hauptsachtitels", NR, ""));

		newBibTag = new BibliographicTag("4203", "047P",
				"Zusammenfassende Register (ab 01.03.2007 verwendet bei Zeitschriften/Schriftenreihen)", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Zusammenfassende Register", NR, ""));

		newGndTag = new DefaultGNDTag("4204", "037C", "Hochschulschriftenvermerk ", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Hochschulschriftenvermerk ", NR, ""));
		newGndTag.add(new Indicator('d', "Charakter der Hochschulschrift", NR, ""));
		newGndTag.add(new Indicator('e', "Institution", NR, ""));
		newGndTag.add(new Indicator('f', "Jahr", NR, ""));
		newGndTag.add(new Indicator('g', "sonstige Angaben", R, ""));

		newGndTag = new DefaultGNDTag("4206", "046U", "Angaben zur Freiwilligen Selbstkontrolle (FSK) und USK", R, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "FSK-Altersangaben", NR, ""));
		newGndTag.add(new Indicator('b', "andere Altersangaben", R, ""));
		newGndTag.add(new Indicator('c', "weitere verbale Angaben zum Jugendschutz", NR, ""));

		newGndTag = new DefaultGNDTag("4208", "020F", "Voraussichtlicher Erscheinungstermin/ JJ.MM.TT oder JJ.MM.00",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Voraussichtlicher Erscheinungstermin/", NR, ""));

		newGndTag = new DefaultGNDTag("4209", "046V", "Künstlerische und technische Angaben (Credits)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Künstlerische und technische Angaben (Credits)", NR, ""));

		newGndTag = new DefaultGNDTag("4214", "037B",
				"Musikalische Form bzw. Besetzung und/oder Sprachangaben (Feld wird mit RDA-Umstieg nicht mehr belegt)",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "-ohne feldinterne Steuerzeichen-", NR, ""));

		newGndTag = new DefaultGNDTag("4217", "046H",
				"Angaben zum Erscheinungsvermerk u. zum Auftraggeber der Produktion", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Angaben zum Erscheinungsvermerk u. zum Auftraggeber der Produktion", NR, ""));

		newGndTag = new DefaultGNDTag("4218", "046I",
				"Angaben zur physischen Beschreibung oder Aufführungsdauer (Bonner Katalog)(Feld wird mit RDA-Umstieg nicht mehr belegt)",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "-ohne feldinterne Steuerzeichen-", NR, ""));

		newGndTag = new DefaultGNDTag("4219", "046J", "Fußnote zum Impressum (verwendet nur im DBSM)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Fußnote zum Impressum (verwendet nur im DBSM)", NR, ""));

		newGndTag = new DefaultGNDTag("4220", "046K",
				"Bandauftragung in Form einer Verweisung auf die Ordnungsblöcke der Stücktitelaufnahme (Nur aus Altdatenkonversion in Satzart Av)",
				NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a',
				"Bandauftragung in Form einer Verweisung auf die Ordnungsblöcke der Stücktitelaufnahme", NR, ""));

		newGndTag = new DefaultGNDTag("4223", "046S",
				"Anmerkungen zu Interpreten, Ausführender, Erzähler, und/oder Präsentator", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Anmerkungen", NR, ""));

		newGndTag = new DefaultGNDTag("4227", "046R", "Herkunftsangaben (verwendet nur im DBSM)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Herkunftsangaben (verwendet nur im DBSM)", NR, ""));

		newGndTag = new DefaultGNDTag("4229", "046T", "Hinweise auf parallele Ausgaben", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Hinweise auf parallele Ausgaben", NR, ""));

		newGndTag = new DefaultGNDTag("4232", "046W", "Redaktionelle Bemerkungen (verwendet nur im DBSM)", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Redaktionelle Bemerkungen (verwendet nur im DBSM)", NR, ""));

		newGndTag = new DefaultGNDTag("4234", "046Y", "Pauschalverweisungen (nur aus Altdatenkonversion) (@{)", R, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Pauschalverweisungen (nur aus Altdatenkonversion) (@{)", NR, ""));

		// wird bibtag:
		newBibTag = new BibliographicTag("4251", "048H/01", "Systemvoraussetzungen für Elektronische Ressourcen", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Systemvoraussetzungen (unstrukturiert)", NR, ""));
		newBibTag.add(new Indicator("=1 ", "", 'c',
				"Systemvoraussetzungen (Prozessor, Speicher) Rechnertyp  Speicheranforderungen", NR, ""));
		newBibTag.add(new Indicator("=2 ", "", 'd',
				"Systemvoraussetzungen (Software) Betriebssystem -   sonstige Software-Voraussetzungen", NR, ""));
		newBibTag.add(new Indicator("=3 ", "", 'e',
				"Systemvoraussetzungen (Hardwarekonfiguration) -  Peripheriegeräte -   sonstige Hardware-Voraussetzungen",
				NR, ""));

		newGndTag = new DefaultGNDTag("4276", "039Y", "Verknüpfung zu weiteren Bezugswerken, Quellennachweis", R, "",
				"");
		addTag(newGndTag);
		newGndTag.add(TagDB.DOLLAR_9);
		newGndTag.add(TagDB.DOLLAR_8);
		newGndTag.add(new Indicator('c', "Titelangaben (wenn keine Verknüpfung)", NR, ""));
		newGndTag.add(new Indicator('4', "Art des Bezugswerks", NR, ""));
		newGndTag.add(new Indicator('v', "Bemerkung", NR, ""));

		newBibTag = new BibliographicTag("4702", "047D", "Erwerbungsspezifische Bezüge: Signaturen", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("%", "", 'p', "Bemerkung", NR, ""));
		newBibTag.add(
				new Indicator("|", "|", 'S', "|k| Medienkombination,|w| Signatur früherer/weiterer Ausgaben", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "Signatur", NR, ""));

		newBibTag = new BibliographicTag("4703", "047E",
				"Feld zur Kennzeichnung von Zweifelsfällen auf der bibliographischen Ebene (nur aus Altdatenkonversion) (m)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a',
				"Codierung für Anfrage unl Code für unleserlichen Scan  anf Code für inhaltliche Anfrage", NR, ""));
		newBibTag.add(new Indicator("*", "", 'c',
				"Anfragetext; ein Anfragetext sollte nur im Zusammenhang mit Code 'anf' vorkommen", NR, ""));

		newGndTag = new DefaultGNDTag("4704", "047J", "Erschließungszustand", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Benennung des Erschließungszustands ", NR, ""));

		newGndTag = new DefaultGNDTag("4705", "047K", "Abstract zur Titelaufnahme", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Abstract", NR, ""));

		newGndTag = new DefaultGNDTag("4710", "047S", "Anzahl der Exemplare insgesamt", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Anzahl (Angabe 4stellig, linksbündig mit \"0\" aufgefüllt)", NR, ""));

		newBibTag = new BibliographicTag("4713", "047V", "Angaben zu Open Access, Lizenzen und Rechten", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Herkunft der Angabe", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Benennung des Rechts (verbal)", NR, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Benennung des Rechts (Code)", NR, ""));
		newBibTag.add(new Indicator("$g", "", 'g', "Grundlage des Rechts, Rechtsnorm", NR, ""));
		newBibTag.add(new Indicator("$o", "", 'o', "Open-Access-Markierung", NR, ""));
		newBibTag.add(new Indicator("$u", "", 'u', "URL zu Lizenzbestimmungen", NR, ""));
		newBibTag.add(TagDB.DOLLAR_9);
		newBibTag.add(TagDB.DOLLAR_8);
		newBibTag.add(new Indicator("$r", "", 'r', "Freitext Rechteinhaber (wenn keine Verknüpfung)", NR, ""));
		newBibTag.add(new Indicator("$t", "", 't', "Gültigkeitsterritorium (ISO-Code)", NR, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "Gültigkeitszeitraum", NR, ""));
		newBibTag.add(new Indicator("$v", "", 'v', "Bemerkung (Rechtsgegenstand, Rechtsgrund etc.)", NR, ""));

		newGndTag = new DefaultGNDTag("4725", "047U", "Aktueller Lieferbarkeitsstatus im Projekt Vergriffene Werke", NR,
				"", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Lieferbarkeitsstatus", NR, ""));
		newGndTag.add(new Indicator('0', "Nummer des gefundenen Titels im Lieferbarkeitsverzeichnis (z.B. VLB- Nummer)",
				NR, ""));
		newGndTag.add(new Indicator('d', "Datum der Ermittlung des Lieferbarkeitsstatus (JJJJMMTT)", NR, ""));
		newGndTag.add(new Indicator('z', "Identifier im Zentralen Verzeichnis Digitalisierter Drucke (ZVDD)", NR, ""));

		bibBasis = newGndTag;
		newGndTag = new DefaultGNDTag("4726", "047W", "Bisherige Lieferbarkeitsstatus im Projekt Vergriffene Werke", R,
				"", "");
		addTag(newGndTag);
		newGndTag.addInherited(bibBasis);

		newGndTag = new DefaultGNDTag("4730", "047Z", "Maßnahme Kataloganreicherung", NR, "", "");
		addTag(newGndTag);
		newGndTag.add(new Indicator('c', "Projektcode", NR, ""));
		newGndTag.add(new Indicator('e', "Ergebnis", NR, ""));
		newGndTag.add(new Indicator('z', "Art der Maßnahme", NR, ""));
		newGndTag.add(DOLLAR_D_DATUM);
		newGndTag.add(new Indicator('K', "Kommentar", NR, ""));

		newGndTag = new DefaultGNDTag("5051", "045D", "Kennzeichen maschineller Erschließungsprozesse", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Kennzeichen maschinelle Sachgruppe", NR, ""));
		newGndTag.add(new Indicator('K', "Konfiguration maschinelle Sachgruppe", NR, ""));
		newGndTag.add(new Indicator('b', "Kennzeichen maschinelle Beschlagwortung", NR, ""));
		newGndTag.add(new Indicator('L', "Konfiguration maschinelle Beschlagwortung", NR, ""));
		newGndTag.add(new Indicator('c', "Kennzeichen maschinelle DDC-Notation", NR, ""));
		newGndTag.add(new Indicator('M', "Konfiguration maschinelle Vergabe DDC-Notation", NR, ""));

		newGndTag = new DefaultGNDTag("5052", "045C", "2. + 3.maschinell vergebene Sachgruppen", NR, "", "");
		addTag(newGndTag);
		newGndTag.add(new Indicator('f', "2. vergebene maschinelle Sachgruppe", NR, ""));
		newGndTag.add(new Indicator('F', "Konfidenzwert zur 2. vergebenen maschinellen Sachgruppe", NR, ""));
		newGndTag.add(new Indicator('g', "3. vergebene maschinelle Sachgruppe", NR, ""));
		newGndTag.add(new Indicator('G', "Konfidenzwert zur 3. vergebenen maschinellen Sachgruppe", NR, ""));
		newGndTag.add(new Indicator('D', "Datum", NR, ""));

		newGndTag = new EnumeratingTag("5056", "045T", "SSG-Angaben", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new EnumeratingTag("5057", "045W", "Online-Contents-Ausschnittskennung", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'a', "weitere Codes werden mit \";\" angeschlossen", R, ""));

		newGndTag = new EnumeratingTag("5080", "045U", "Systematik der katalogisierenden Institution", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator(";", "", 'e',
				"DDC-Sachgruppen; mehrere Systematikstellen werden mit \";\" angeschlossen", R, ""));

		newGndTag = new DefaultGNDTag("5301", "045Q/01", "1. Verknüpfung zur Basisklassifikation", NR, "", "");
		addTag(newGndTag);
		newGndTag.add(TagDB.DOLLAR_9);
		newGndTag.add(TagDB.DOLLAR_8);
		gndBasis = (GNDTag) getPica3("5301");
		for (int i = 2; i <= 9; i++) {
			newGndTag = new DefaultGNDTag("530" + i, "045Q/0" + i, i + ". Verknüpfung zur Basisklassifikation", NR, "",
					"");
			addTag(newGndTag);
			newGndTag.addInherited(gndBasis);
		}

		newBibTag = new BibliographicTag("5310", "045V", "1. Verknüpfung zur SMM-Klassifikation", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Freitext", NR, ""));

		bibBasis = getPica3("5310");
		for (int i = 1; i <= 9; i++) {
			newBibTag = new BibliographicTag("531" + i, "045V/0" + i, (i + 1) + ". Verknüpfung zur SMM-Klassifikation",
					R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		// newGndTag =
		// new DefaultGNDTag("5310", "045V",
		// "1. Verknüpfung zur SMM-Klassifikation", NR, "", "");
		// addTag(newGndTag);
		// newGndTag.add(TagDB.DOLLAR_9);
		// newGndTag.add(TagDB.DOLLAR_8);

		// gndBasis = (GNDTag) getPica3("5310");
		// for (int i = 1; i <= 9; i++) {
		// newGndTag =
		// new DefaultGNDTag("531" + i, "045V/0" + i, (i + 1)
		// + ". Verknüpfung zur SMM-Klassifikation", NR, "", "");
		// addTag(newGndTag);
		// newGndTag.addInherited(gndBasis);
		// }

		newGndTag = new DefaultGNDTag("5320", "045P", "Verknüpfung zu weiteren Klassifikationen", R, "", "");
		addTag(newGndTag);
		newGndTag.add(TagDB.DOLLAR_9);
		newGndTag.add(TagDB.DOLLAR_8);

		newGndTag = new DefaultGNDTag("5401", "045F/01", "1. DDC-Notation: Grundnotation", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Grundnotation", NR, ""));

		gndBasis = (GNDTag) getPica3("5401");
		for (int i = 1; i <= 4; i++) {
			final char c = (char) ('F' + i);
			newGndTag = new DefaultGNDTag("54" + i + "1", "045" + c + "/01", (i + 1) + ". DDC-Notation: Grundnotation",
					R, "", "");
			addTag(newGndTag);
			newGndTag.addInherited(gndBasis);
		}

		newGndTag = new DefaultGNDTag("5402", "045F/02", "1. DDC-Notation: Notationen anderer Haupttafeln", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Notationen anderer Haupttafeln", NR, ""));

		gndBasis = (GNDTag) getPica3("5402");
		for (int i = 1; i <= 4; i++) {
			final char c = (char) ('F' + i);
			newGndTag = new DefaultGNDTag("54" + i + "2", "045" + c + "/02",
					(i + 1) + ". DDC-Notation: Notationen anderer Haupttafeln", R, "", "");
			addTag(newGndTag);
			newGndTag.addInherited(gndBasis);
		}

		newGndTag = new DefaultGNDTag("5404", "045F/04", "1. DDC-Notation: Notationen aus einer Anhängetafel", R, "",
				"");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Notation aus einer Anhängetafel", NR, ""));

		gndBasis = (GNDTag) getPica3("5404");
		for (int i = 1; i <= 4; i++) {
			final char c = (char) ('F' + i);
			newGndTag = new DefaultGNDTag("54" + i + "4", "045" + c + "/04",
					(i + 1) + ". DDC-Notation: Notationen aus einer Anhängetafel", R, "", "");
			addTag(newGndTag);
			newGndTag.addInherited(gndBasis);
		}

		// --------------------

		newBibTag = new BibliographicTag("0596", "009M", "Datensatzkennzeichnung für Mahnung", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", " : ", 'a', "Datum (JJ-MM-TT)", NR, ""));
		newBibTag.add(new Indicator("", "", 'b', "Mahnstatus", NR, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "ILN", NR, ""));

		newBibTag = new BibliographicTag("0599", "009@", "Datensatzkennzeichnungen", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", " : ", 'a', "Datum (JJ-MM-TT)", NR, ""));
		newBibTag.add(new Indicator("", "", 'b', "Selektionsschlüssel", NR, ""));
		newBibTag.addAlternative(new Indicator("", " : ", 'a', "Datum (JJ-MM-TT) (m)", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'b', "Änderungscodierung", NR, ""));
		newBibTag.addAlternative(DOLLAR_9);
		newBibTag.addAlternative(DOLLAR_8);

		newBibTag = new BibliographicTag("0701", "008@",
				"Signatur und exemplarspezifische Angaben bei zentraler Erwerbung / Katalogisierung", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("/", "/", 'a', "Exemplarspezifischer Selektionsschlüssel (m)", NR, ""));
		newBibTag.add(new Indicator("", "", ";", true, 'b', "Signatur (Zeitschriften) / Akzessionsnummer (Monografien) "
				+ "weitere Signaturen / Akzessionsnummern werden mit \";\" angeschlossen", NR, ""));
		newBibTag.add(new Indicator("((", "))", 'f',
				"Erläuterungen / Kommentare zur Signatur / Akzessionsnummer oder zum Bestand", R, ""));
		newBibTag.add(new Indicator("((", "))", 'g',
				"Erläuterungen / Kommentare zur Signatur / Akzessionsnummer oder zum Bestand", R, ""));
		newBibTag.add(new Indicator("[[", "]]", 'h', "Bestandverlauf bei Zeitschriften/Schriftenreihen", NR, ""));
		newBibTag.add(new Indicator("@", "@", 'k', "Kommentar zum Bestand (8034)", NR, ""));
		newBibTag.add(new Indicator("**", "", 'c', "Zugangsart (pz, ge, ka, ta, pa)", NR, ""));
		newBibTag.add(new Indicator("%", "", 'i', "Angaben zu Zugriffsrechten", NR, ""));
		newBibTag.add(new Indicator("{", "}", 'e', "Registrierungsnummer bei Elektronischen Ressourcen", R, ""));
		newBibTag.add(new Indicator("#", "", 'z', "ILN des erfassenden Standorts (m)", NR, ""));

		newGndTag = new DefaultGNDTag("1100", "011@", "Erscheinungsjahr", NR, "", "Date of publication");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('a', "Angabe des Erscheinungsjahres in Sortierform (immer 4 Ziffern)", NR, ""));
		newGndTag.add(new Indicator('b', "Angabe eines abschließenden Jahres in Sortierform", NR, ""));
		newGndTag.add(new Indicator('n',
				"Erscheinungsjahr in Vorlageform, sofern abweichend von der Angabe in Sortierform", NR, ""));
		newGndTag.add(new Indicator('r',
				"Datum des Originals in Sortierform (immer 4 Ziffern) (nur bei Reproduktionen)", NR, ""));

		newBibTag = new BibliographicTag("1698", "038L", "Markierung für Match & Merge-Verfahren", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("+", "+", 'a', "Angabe des Match-&-Merge-Kontingentes", NR, ""));
		newBibTag.add(new Indicator("*", "*", 'b', "Status der Prüfung", NR, ""));
		newBibTag.add(new Indicator("#", "#", 'x', "interne Protokollierung", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);

		newBibTag = new BibliographicTag("1500", "010@",
				"Code(s) für Sprache(n) des Textes und des Originals nach DIN 2335 (ISO 639-2, 3 Kleinbuchstaben)", NR,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("/1", "", 'a', "Sprache des Textes", R, ""));
		newBibTag.add(new Indicator("/3", "", 'c', "Sprache des Originals", R, ""));

		newBibTag = new BibliographicTag("1700", "019@",
				"Code für Erscheinungsland nach DIN EN 23166 (ISO 3166) (2 Großbuchstaben) Angabe erfolgt ab 01.03.2007 bei allen Materialien, auch wenn nur ein deutscher Erscheinungsort genannt ist.",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("/1", "", 'a', "Erscheinungsland", R, ""));

		newBibTag = new BibliographicTag("2000", "004A", "Erste und weitere richtige ISBN", R, "020", "");
		addTag(newBibTag);
		newBibTag.add(
				new Indicator("", "*", '0', '9', "ISBN (mit Bindestrichen); ohne die Zeichenfolge \"ISBN¬\"", NR, ""));
		newBibTag.add(new Indicator("(", ")", 'c', "Kommentar zur ISBN", NR, ""));
		newBibTag.add(
				new Indicator("", "", 'f', 'c', "Einbandart, Lieferbedingungen und/oder Preis, Sonstiges", NR, ""));

		newBibTag = new BibliographicTag("2005", "005I", "Autorisierte ISSN des nationalen ISSN-Zentrums der DNB", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "*", '0', "(Autorisierte) ISSN (mit Bindestrichen)", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Key title", NR, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Qualifier", NR, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Key title abbreviation (@)", NR, ""));
		newBibTag.add(new Indicator("$d", "", 'd',
				"Qualifier Key title abbreviation (mehrere Qualifier getrennt durch \"._\")", NR, ""));
		newBibTag.add(new Indicator("$t", "", 't', "zeitliche Gültigkeit", NR, ""));
		newBibTag.add(new Indicator("p", "", 'p', "Exportcode", NR, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "gelöschte ISSN", R, ""));
		newBibTag.add(new Indicator("$l", "", 'l', "ISSN-L", NR, ""));
		newBibTag.add(new Indicator("$m", "", 'm', "gelöschte ISSN-L", R, ""));

		newBibTag = new BibliographicTag("2009", "004D", "Formal falsche ISBN", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2000");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2010", "005A", "ISSN der Vorlage", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "*", '0', "ISSN (mit Bindestrichen); ohne die Zeichenfolge \"ISSN¬\"", NR, ""));
		newBibTag.add(new Indicator("(", ")", 'c', "Kommentar zur ISSN (verwendet bis 01.03.2007)", NR, ""));
		newBibTag.add(new Indicator("", "", 'f',
				"Einbandart, Lieferbedingungen und/oder Preis, Sonstiges (verwendet bis 01.03.2007)", NR, ""));
		newBibTag.add(new Indicator("$l", "", 'l', "ISSN-L", NR, ""));
		newBibTag.add(new Indicator("$m", "", 'm', "gelöschte ISSN-L", R, ""));

		newBibTag = new BibliographicTag("2011", "005J",
				"ISSN des ersten Gesamttitels der Sekundärausgabe (wird nicht mehr genutzt)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2012", "005K",
				"ISSN des zweiten Gesamttitels der Sekundärausgabe (wird nicht mehr genutzt)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2013", "005P", "ISSNs paralleler Ausgaben", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Indikator für ISSN", NR, ""));
		newBibTag.add(new Indicator("", "*", '0', "ISSN (mit Bindestrichen); ohne die Zeichenfolge \"ISSN \"", NR, ""));

		newBibTag = new BibliographicTag("2014", "005L", "gelöschte ISSN", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "*", '0', "ISSN (mit Bindestrichen); ohne die Zeichenfolge \"ISSN¬\"", NR, ""));
		newBibTag.add(new Indicator("(", ")", 'c', "Kommentar zur ISSN", NR, ""));
		newBibTag.add(new Indicator("", "", 'f',
				"Einbandart, Lieferbedingungen und/oder Preis, Sonstiges " + "(verwendet bis 01.03.2007)", NR, ""));

		newBibTag = new BibliographicTag("2015", "004G", "Erste und weitere richtige ISBN der Sekundärausgabe", R, "",
				"");
		addTag(newBibTag);
		inheritedTag = getPica3("2000");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2016", "004H", "Formal falsche ISBN der Sekundärausgabe", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2000");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2017", "005G", "Erste und weitere richtige ISSN der Sekundärausgabe", R, "",
				"");
		addTag(newBibTag);
		inheritedTag = getPica3("2010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2018", "005H", "Formal falsche ISSN der Sekundärausgabe", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2019", "005B", "Formal falsche ISSN", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2020", "004F", "Erste und weitere richtige ISMN", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "*", '0', "ISMN (mit Bindestrichen); ohne die Zeichenfolge \"ISMN¬\"", NR, ""));
		newBibTag.add(new Indicator("(", ")", 'c', "Kommentar zur ISMN", NR, ""));
		newBibTag.add(new Indicator("", "", 'f', "Einbandart, Lieferbedingungen und/oder Preis, Sonstiges", NR, ""));

		newBibTag = new BibliographicTag("2029", "004I", "Formal falsche ISMN", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("2020");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("2035", "007R", "Bibliografischer Nachweis alter Drucke", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Herkunft des Nachweises", NR, ""));
		newBibTag.add(new Indicator("#", "#", 'x', "Sortierform", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "Nummer des Nachweises", NR, ""));

		newBibTag = new BibliographicTag("2105", "006U",
				"Lieferungsnummer der Deutschen Nationalbibliografie und/oder Pseudoheftnummer (m)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Steuerungscode (n)", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "JJ,RHH", NR, ""));

		newBibTag = new BibliographicTag("2199", "006Y", "Sonstige Standardnummern", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Herkunftscode / Art der Nummer", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "Standardnummer", NR, ""));

		newBibTag = new BibliographicTag("2240", "007G",
				"Regionale Identifikationsnummer der erstkatalogisierenden Institution (m)", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", ":", 'a', "Verbund-Kürzel", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "ID-Nummer", NR, ""));

		newBibTag = new BibliographicTag("2241", "007H", "Regionale Identifikationsnummern", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Bibliotheksverbundskürzel (ein Zeichen)", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "ID-Nummer", NR, ""));

		newBibTag = new BibliographicTag("2242", "007I", "Überregionale Identifikationsnummern", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Institutionskürzel (ein Zeichen)", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "ID-Nummer", NR, ""));

		newBibTag = new BibliographicTag("2245", "007J", "Nummer der falschen Aufnahme (nur aus Altdatenkonversion)", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", '0', "Nummer", NR, ""));
		newBibTag.add(new Indicator(" (", ")", 'z', "Abschlusstext", NR, ""));

		newBibTag = new BibliographicTag("2300", "004E", "Label, Verlags-, Produktions- u. Bestellnummer", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'x', "Sortierform der Bestellnummer", NR, ""));
		newBibTag.add(new Indicator("", "@", 'l', "Label", NR, ""));
		newBibTag.add(new Indicator("", "*", '0', "Nummer (in Altdaten Label und Nummer)", NR, ""));
		newBibTag
				.add(new Indicator("(", ")", 'c', "Kommentar zur Nummer (Historische Tonträger: Preisklasse)", NR, ""));
		newBibTag.add(new Indicator("", "", 'f', "Preis und/oder Lieferbedingungen, Sonstiges", NR, ""));

		newBibTag.addAlternative(new Indicator("#", "#", 'x', "Sortierform der Bestellnummer", NR, ""));
		newBibTag.addAlternative(DOLLAR_9);
		newBibTag.addAlternative(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "*", '0', "Nummer (in Altdaten Label und Nummer)", NR, ""));
		newBibTag.addAlternative(
				new Indicator("(", ")", 'c', "Kommentar zur Nummer (Historische Tonträger: Preisklasse)", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'f', "Preis und/oder Lieferbedingungen, Sonstiges", NR, ""));

		newBibTag = new BibliographicTag("2305", "004L",
				"Label, Verlags-, Produktions- u. Bestellnummer in Vorlageform", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "@", 'l', "Label", NR, ""));
		newBibTag.add(new Indicator("", "*", '0', "Nummer (in Altdaten Label und Nummer)", NR, ""));
		newBibTag
				.add(new Indicator("(", ")", 'c', "Kommentar zur Nummer (Historische Tonträger: Preisklasse)", NR, ""));
		newBibTag.add(new Indicator("", "", 'f', "Preis und/oder Lieferbedingungen, Sonstiges", NR, ""));

		bibBasis = newBibTag;
		newBibTag = new BibliographicTag("2310", "004M", "Vertriebsnummer", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		newBibTag = new BibliographicTag("2315", "004N", "Vertriebsnummer  in Vorlageform", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		// OK???
		newBibTag = new BibliographicTag("2330", "004Z",
				"Erwerbungsspezifische Einzelnummern (für unselbständige Teile)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Code", NR, ""));
		newBibTag.add(new Indicator("", "", '0', "Nummer", R, ""));
		newBibTag.add(new Indicator("%", "", 'p', "Bemerkung", R, ""));

		newBibTag = new BiblioPersonTag("3000", "028A", "Person, Familie - 1. geistiger Schöpfer", R, "100",
				"MAIN ENTRY--PERSONAL NAME");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("|", "|", 'S', "maschinell verknüpft, ein Zeichen: m", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(DOLLAR_6);
		newBibTag.add(DOLLAR_B_CREA);
		newBibTag.add(DOLLAR_4_CREA);
		newBibTag.add(DOLLAR_Y_IDENTIF);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		// -
		newBibTag.addAlternative(new Indicator("@", "", '5', "Persönlicher Name", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'a', "Familienname", NR, ""));
		newBibTag.addAlternative(new Indicator(", ", "", 'd', '!', "Vorname(n)", NR, ""));
		newBibTag.addAlternative(new Indicator(" /", "", 'c', "Präfix, dem Vornamen nachgestellt", NR, ""));
		newBibTag.addAlternative(new Indicator(" <", ">", 'l', "Ordnungshilfe", NR, ""));
		newBibTag.addAlternative(DOLLAR_B_CREA);
		newBibTag.addAlternative(DOLLAR_4_CREA);
		newBibTag.addAlternative(DOLLAR_Y_IDENTIF);
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);

		// 3001-3009 gibt es alle nicht mehr!
		newBibTag = new BiblioPersonTag("3001", "028B/01", "2. Verfasser", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3000");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BiblioPersonTag("3002", "028B/02", "3. Verfasser", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BiblioPersonTag("3003", "028B/03", "4. Verfasser (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BiblioPersonTag("3004", "028B/04", "5. Verfasser (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		// Diese Lücke ist korrekt!

		newBibTag = new BiblioPersonTag("3009", "028B/09", "weitere Verfasser (nur genutzt vom Deutschen Exilarchiv)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BiblioPersonTag("3010", "028C",
				"Person, Familie - weitere geistige Schöpfer, Sonstige und Mitwirkende ", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		// 3011-3018 nicht mehr nötig?
		for (int i = 2; i <= 9; i++) {
			newBibTag = new BiblioPersonTag("301" + (i - 1), "028C/0" + (i - 1), i + ". sonstige beteiligte Person", NR,
					"", "");
			addTag(newBibTag);
			newBibTag.addInherited(inheritedTag);
		}

		newBibTag = new BiblioPersonTag("3019", "028C/09", "Person, Familie - aus Fremddaten", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		// 3020-3099 entfallen!
		newBibTag = new BiblioPersonTag("3020", "028Q", "1.beteiligte Person Briefwechsel", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3010");
		newBibTag.addInherited(inheritedTag);

		bibBasis = getPica3("3010");
		for (int i = 2; i <= 9; i++) {
			newBibTag = new BiblioPersonTag("302" + (i - 1), "028Q/0" + (i - 1), i + ". beteiligte Person Briefwechsel",
					NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BiblioPersonTag("3029", "028Q/09", "weitere beteiligte Personen Briefwechsel", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BiblioPersonTag("3030", "028P", "1. Adressat", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3010");
		newBibTag.addInherited(inheritedTag);

		bibBasis = getPica3("3010");
		for (int i = 2; i <= 9; i++) {
			newBibTag = new BiblioPersonTag("303" + (i - 1), "028P/0" + (i - 1), i + ". Adressat", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BiblioPersonTag("3039", "028P/09", "weitere Adressaten", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3010");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BiblioPersonTag("3040", "028F", "1. gefeierte Person", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3000");
		newBibTag.addInherited(inheritedTag);

		bibBasis = getPica3("3000");
		for (int i = 2; i <= 3; i++) {
			newBibTag = new BiblioPersonTag("304" + (i - 1), "028F/0" + (i - 1), i + ". gefeierte Person", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3000");
		newBibTag = new BiblioPersonTag("3050", "028D", "1. Interpret", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		for (int i = 2; i <= 10; i++) {
			newBibTag = new BiblioPersonTag("305" + (i - 1), "028D/0" + (i - 1), i + ". Interpret", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		// 3060-3099 entfallen
		newBibTag = new BiblioPersonTag("3060", "028E", "11. Interpret", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		for (int i = 2; i <= 9; i++) {
			newBibTag = new BiblioPersonTag("306" + (i - 1), "028E/0" + (i - 1), "1" + i + ". Interpret", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BiblioPersonTag("3069", "028E/09", "weitere Interpreten", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		newBibTag = new BiblioPersonTag("3070", "028M", "1. sonstige beteiligte Person mit zweiteiliger NE", NR, "",
				"");
		addTag(newBibTag);
		inheritedTag = getPica3("3010");
		newBibTag.addInherited(inheritedTag);

		bibBasis = getPica3("3010");
		for (int i = 2; i <= 3; i++) {
			newBibTag = new BiblioPersonTag("307" + (i - 1), "028M/0" + (i - 1),
					i + ". sonstige beteiligte Person mit zweiteiliger NE", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BiblioPersonTag("3090", "028Z",
				"Verweisungsformen zu unverknüpften Personennamen (nur in Altdaten mit Materialart A*o)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("+30XX+", "", 'C',
				"Verbindungscode zu 30XX: Feldnummer des Feldes, in dem sich die Ansetzungsform befindet", NR, ""));
		newBibTag.add(new Indicator("@", "", '5', "Persönlicher Name", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Familienname", NR, ""));
		newBibTag.add(new Indicator(", ", "", 'd', "Vorname(n)", NR, ""));
		newBibTag.add(new Indicator(" <", ">", 'l', "Ordnungshilfe", NR, ""));
		newBibTag.add(new Indicator("[", "]", 'B', "Erläuterung", NR, ""));

		newBibTag = new BibliographicTag("3100", "029A", "Körperschaft, Konferenz - 1. geistiger Schöpfer", R, "110",
				"MAIN ENTRY--CORPORATE NAME");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("|", "|", 'S', "maschinell verknüpft", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(DOLLAR_6);
		newBibTag.add(DOLLAR_B_CREA);
		newBibTag.add(DOLLAR_4_CREA);
		newBibTag.add(DOLLAR_Y_IDENTIF);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		// -
		newBibTag.addAlternative(new Indicator("", "", 'a', "Name der Körperschaft (@{)", NR, ""));
		newBibTag.addAlternative(new Indicator(" <", ">", 'c', "Ordnungshilfe zur Hauptkörperschaft", NR, ""));
		newBibTag.addAlternative(new Indicator(" / ", "", 'b', "Abteilung(en) (@{)", R, ""));
		newBibTag.addAlternative(new Indicator(" <", ">", 'x', "Ordnungshilfe zu(r) Abteilung(en)", R, ""));
		newBibTag.addAlternative(DOLLAR_B_CREA);
		newBibTag.addAlternative(DOLLAR_4_CREA);
		newBibTag.addAlternative(DOLLAR_Y_IDENTIF);
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);

		// diese entfallen!
		bibBasis = getPica3("3100");
		for (int i = 2; i <= 5; i++) {
			newBibTag = new BibliographicTag("310" + (i - 1), "029A/0" + (i - 1), i + ". Primärkörperschaft", NR, "",
					"");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("3110", "029F",
				"Körperschaft, Konferenz - weitere geistige Schöpfer, Sonstige und Mitwirkende", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		// 3111-3118 entfallen!
		for (int i = 2; i <= 9; i++) {
			newBibTag = new BibliographicTag("311" + (i - 1), "029F/0" + (i - 1), i + ". Sekundärkörperschaft", NR, "",
					"");
			addTag(newBibTag);
			newBibTag.addInherited(inheritedTag);
		}

		// bleibt!
		newBibTag = new BibliographicTag("3119", "029F/09", "Körperschaft, Konferenz - aus Fremddaten", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		// 3130-3195 entfallen
		newBibTag = new BibliographicTag("3130", "029K", "1. körperschaftlicher Adressat", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		for (int i = 2; i <= 10; i++) {
			newBibTag = new BibliographicTag("313" + (i - 1), "029K/0" + (i - 1), i + ". körperschaftlicher Adressat",
					NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(inheritedTag);
		}

		inheritedTag = getPica3("3100");
		newBibTag = new BibliographicTag("3150", "029E", "1. körperschaftlicher Interpret", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		for (int i = 2; i <= 10; i++) {
			newBibTag = new BibliographicTag("315" + (i - 1), "029E/0" + (i - 1), i + ". körperschaftlicher Interpret",
					NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("3160", "029G", "11. körperschaftlicher Interpret", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		for (int i = 2; i <= 9; i++) {
			newBibTag = new BibliographicTag("316" + (i - 1), "029G/0" + (i - 1),
					"1" + i + ". körperschaftlicher Interpret", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("3169", "029G/09", "weitere körperschaftlicher Interpreten", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(inheritedTag);

		bibBasis = getPica3("3130");
		newBibTag = new BibliographicTag("3170", "029I", "1. Körperschaft mit zweiteiliger NE", NR, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		for (int i = 2; i < 3; i++) {
			newBibTag = new BibliographicTag("317" + (i - 1), "029I/0" + (i - 1),
					i + ". Körperschaft mit zweiteiliger NE", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("3172", "029I/02", "3. Körperschaft mit zweiteiliger NE", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3181", "029H/01",
				"1. Primär-KV der 1. Schriftenreihe (4180) (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3182", "029H/02",
				"2. - n. Primär-KV der 1. Schriftenreihe (4180) (nur aus Altdatenkonversion)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3184", "029H/04",
				"1. Primär-KV der 2. Schriftenreihe (4181) (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3185", "029H/05",
				"2. - n. Primär-KV der 2. Schriftenreihe (4181) (nur aus Altdatenkonversion)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3187", "029H/07",
				"1. Primär-KV der 3. Schriftenreihe (4182) (nur aus Altdatenkonversion)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3188", "029H/08",
				"2. - n. Primär-KV der 3. Schriftenreihe (4182) (nur aus Altdatenkonversion)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("3100");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("3191", "029Q", "Beteiligte Körperschaft Briefwechsel", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Name der Körperschaft (@{)", NR, ""));
		newBibTag.addAlternative(new Indicator(" <", ">", 'c', "Ordnungshilfe zur Hauptkörperschaft", NR, ""));
		newBibTag.addAlternative(new Indicator(" / ", "", 'b', "Abteilung(en) (@{)", R, ""));

		newGndTag = new DefaultGNDTag("3195", "029V", "Verleger in normierter Form", R, "", "");
		addTag(newGndTag);
		newGndTag.add(DOLLAR_9);
		newGndTag.add(DOLLAR_8);

		newBibTag = new BibliographicTag("3199", "027C", "Person oder Körperschaft, die nicht zugeordnet werden kann",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$a", "", 'a', "Name", NR, ""));
		newBibTag.add(DOLLAR_B_CREA);
		newBibTag.add(DOLLAR_4_CREA);
		newBibTag.add(DOLLAR_Y_IDENTIF);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);

		newBibTag = new BibliographicTag("3200", "022S",
				"Sammlungsvermerk oder Formalsachtitel, mit dem nicht die Haupteintragung erfolgt", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "#", 'b', "Codierung für \"Sammlung\" oder", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Formalsachtitel", NR, ""));
		newBibTag.add(
				new Indicator(" <", ">", 'r', "Einheitl. Sprachbezeichnung nach RAK oder Datum bei Verträgen", NR, ""));

		newBibTag = new BibliographicTag("3210", "022A", "In der Manifestation verkörpertes Werk", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("|", "|", 'S',
				"Funktionscode für NE (Unterfeld wird mit RDA-Umstieg nicht mehr belegt)", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator("$k", "", 'k',
				"Auswahl (Belegung auch bei Verknüpfungen möglich) (in Altdaten vor RDA: Fassung, Alternative)", NR,
				""));
		newBibTag.add(new Indicator("$o", "", 'o',
				"Arrangements, Transkriptionen (Belegung auch bei Verknüpfungen möglich)", NR, ""));
		newBibTag.add(new Indicator("$h", "", 'h',
				"Auswahl und/oder Arrangement (Unterfeld kann auch bei Verknüpfungen erfasst werden) (Unterfeld wird mit RDA-Umstieg nicht mehr belegt)",
				NR, ""));

		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Titel", NR, ""));
		newBibTag.addAlternative(new Indicator("$f", "", 'f', "Datum des Werks", NR, ""));
		newBibTag.addAlternative(new Indicator("$g", "", 'g',
				"Form, Ursprungsort oder sonstige Unterscheidende Eigenschaft des Werks", R, ""));
		newBibTag.addAlternative(new Indicator("$m", "", 'm', "Besetzung", R, ""));
		newBibTag.addAlternative(new Indicator("$n", "", 'n',
				"Numerische Bezeichnung eines Musikwerks bzw. Zählung der Unterrreihe", R, ""));
		newBibTag.addAlternative(new Indicator("$p", "", 'p', "Titel der Unterreihe", R, ""));
		newBibTag.addAlternative(new Indicator("$r", "", 'r',
				"Tonart (in Altdaten vor RDA: Einheitl. Sprachbezeichnung nach RAK oder Datum bei Verfassungen oder Ordnungshilfen bei Musikalien u.Tonträgern)",
				NR, ""));
		newBibTag.addAlternative(new Indicator("$s", "", 's', "Version", NR, ""));
		newBibTag.addAlternative(new Indicator("$k", "", 'k',
				"Auswahl (Belegung auch bei Verknüpfungen möglich) (in Altdaten vor RDA: Fassung, Alternative)", NR,
				""));
		newBibTag.addAlternative(new Indicator("$o", "", 'o',
				"Arrangements, Transkriptionen (Belegung auch bei Verknüpfungen möglich)", NR, ""));
		newBibTag.addAlternative(new Indicator("$h", "", 'h',
				"Auswahl und/oder Arrangement (Unterfeld kann auch bei Verknüpfungen erfasst werden) (Unterfeld wird mit RDA-Umstieg nicht mehr belegt)",
				NR, ""));

		bibBasis = newBibTag; // = 3210
		newBibTag = new BibliographicTag("3211", "022A/01",
				"In der Manifestation verkörpertes Werk bei Zusammenstellungen", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		newBibTag = new BibliographicTag("3213", "032W", "Form des Werks", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Form des Werks", NR, ""));
		newBibTag.addAlternative(TagDB.DOLLAR_2);

		newBibTag = new BibliographicTag("3214", "032V", "Sonstige unterscheidende Eigenschaft des Werks", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Sonstige unterscheidende Eigenschaft des Werks", NR, ""));
		newBibTag.addAlternative(TagDB.DOLLAR_U_SM_R);
		newBibTag.addAlternative(TagDB.DOLLAR_2);
		newBibTag.addAlternative(new Indicator("$v", "", 'v', "Bemerkungen", R, ""));

		newBibTag = new BibliographicTag("3215", "032X", "Besetzung", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Besetzung", NR, ""));
		newBibTag.addAlternative(new Indicator("$b", "", 'b', "Soloist", R, ""));
		newBibTag.addAlternative(new Indicator("$d", "", 'd', "Doubling instrument", R, ""));
		newBibTag.addAlternative(new Indicator("$n", "", 'n', "Besetzungsstärke", R, ""));
		newBibTag.addAlternative(new Indicator("$p", "", 'p', "Alternative Besetzung", R, ""));
		newBibTag.addAlternative(new Indicator("$s", "", 's', "Gesamtbesetzungsstärke", R, ""));
		newBibTag.addAlternative(TagDB.DOLLAR_2);
		newBibTag.addAlternative(new Indicator("$v", "", 'v', "Bemerkungen", R, ""));

		newBibTag = new BibliographicTag("3216", "032Y", "Numerische Bezeichnung eines Musikwerks", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", 'a', "Fortlaufende Zählung", NR, ""));
		newBibTag.addAlternative(new Indicator("$b", "", 'b', "Opus-Zählung", R, ""));
		newBibTag.addAlternative(new Indicator("$c", "", 'c', "Thematic index number", R, ""));
		newBibTag.addAlternative(new Indicator("$d", "", 'd', "Thematic index code", NR, ""));
		newBibTag.addAlternative(new Indicator("$e", "", 'e', "Verleger", NR, ""));
		newBibTag.addAlternative(TagDB.DOLLAR_2);

		newBibTag = new BibliographicTag("3217", "032Z", "Tonart", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Tonart des Originals", NR, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Tonart der Fassung", NR, ""));

		newBibTag = new BibliographicTag("3220", "025@", "Ansetzungssachtitel", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("|", "|", 'S',
				"Funktionscode für NE unter dem Ansetzungssachtitel (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)",
				NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Ansetzungssachtitel ohne eckige Klammern (@{)", NR, ""));

		newBibTag = new BibliographicTag("3260", "027A", "Sachtitelformen für Nebeneintragungen", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("|", "|", 'S',
				"|a|  Funktionscode für NE unter dem Sachtitel |b|  Funktionscode für NE mit dem Sachtitel aus 3000-3002, 3100-3102 |c|   Funktionscode für NE unter und mit dem Sachtitel aus 3000-3002,  3100-3102 (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007) Falls kein Funktionscode angegeben ist, gilt |a| als Standardannahme",
				NR, ""));
		newBibTag.add(new Indicator("", "", 'a',
				"Titel (@{)Ggf. durch Deskriptionszeichen getrennte Bestandteile werden nicht auf Unterfelder differenziert.",
				NR, ""));

		newBibTag = new BibliographicTag("3232", "026C", "Normierter Zeitschriftenkurztitel", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Kurztitel nach DIN 1502", NR, ""));

		newBibTag = new BibliographicTag("4000", "021A", "Hauptsachtitel, Zusätze, Parallelsachtitel, Verfasserangabe",
				NR, "245", "TITLE STATEMENT");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("|", "|", " |", false, 'S', "Funktionscode für NE unter dem Hauptsachtitel (bei"
				+ "Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)", R, ""));
		newBibTag.add(new Indicator("", "", 'a', "Hauptsachtitel (@{)", NR, ""));
		newBibTag.add(
				new Indicator(" // ", "", 'e', "Körperschaftl. Ergänzung bzw. Nachstellung zum Hauptsachtitel", R, ""));
		newBibTag.add(new Indicator(" [[", "]]", 'n', 'h', "Allgemeine Materialbenennung", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'd', 'b',
				"Zusatz zum Hauptsachtitel (@) (mehrere Zusätze getrennt durch \"¬;¬\")", R, ""));
		newBibTag.add(new Indicator(" = ", "", 'f', 'b',
				"1.-4. Parallelsachtitel (Vorlageform) (@{) (bei Zeitschriften/Schriftenreihen werden ab dem 01.03.2007 nur 2 Parallelsachtitel vergeben)",
				R, ""));
		newBibTag.add(new Indicator(" / ", "", 'h', 'c', "Verfasserangabe zum Hauptsachtitel", NR, ""));
		newBibTag.add(new Indicator(" ** ", "", 'q',
				"Verfasserangabe des Gesamttitels zur Expansion in Stücktitelaufnahmen Verwendet in den Satzarten *c und *E bei Verfasser- werken, wenn Stücktitel vorkommen.",
				NR, ""));
		// -
		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(new Indicator("#", "#", 'x', "Sortierfähige Bandzählung (m)", NR, ""));
		newBibTag.addAlternative(DOLLAR_9);
		newBibTag.addAlternative(DOLLAR_8);
		// Stimmt es? Versuch, daher $S oben gelöscht. Die Unterfelder sind nur im
		// Gesamtabzug:
		newBibTag.addAlternative(new Indicator("$", "", 'S', "Hauptsachgruppe des übergeordneten Titels", NR, ""));
		newBibTag.addAlternative(new Indicator("$", "", 'B', "Nebensachgruppe des übergeordneten Titels", R, ""));

		newBibTag = new BibliographicTag("4004", "021B", "Zählung und Titel eines unselbständigen Teils", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("*", "*", 'l', "Bandbezeichnung und/oder Zählung in Vorlageform", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Titel des Bandes (@{)", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'd', "Zusatz zum Bandtitel", R, ""));
		newBibTag.add(new Indicator(" = ", "", 'f', "1.-4. Parallelsachtitel (Vorlageform) (@)", R, ""));
		newBibTag.add(new Indicator(" / ", "", 'h', "Verfasserangabe zum Bandtitel", NR, ""));
		newBibTag.addAlternative(new Indicator("{", "}", 'r', "Undifferenzierter Text", NR, ""));

		newBibTag = new BibliographicTag("4005", "021C", "Titel von Unterreihen fortlaufender Sammelwerke", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("*", "*", 'l', "Reihenbezeichnung und/oder -zählung der Unterreihe in Vorlageform",
				NR, ""));
		newBibTag.add(new Indicator("|", "|", " |", false, 'S',
				"Funktionscode für NE unter dem Sachtitel der Unterreihe (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)",
				R, ""));
		newBibTag.add(new Indicator("", "", 'a', "Titel der Unterreihe (@{)", NR, ""));
		newBibTag.add(new Indicator(" // ", "", 'e', "Körperschaftl. Ergänzung zur Unterreihe", R, ""));
		newBibTag.add(new Indicator(" [[", "]]", 'n', "Allgemeine Materialbenennung", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'd', "Zusatz zum Sachtitel der Unterreihe", NR, ""));
		newBibTag.add(new Indicator(" = ", "", 'f', "Parallelsachtitel zur UR(@)", R, ""));
		newBibTag.add(new Indicator(" : ", "", 'd', "Zusatz zum 1.-4. Parallelsachtitel", R, ""));
		newBibTag.add(new Indicator(" / ", "", 'h', "Verfasserangabe zur Unterreihe", NR, ""));
		newBibTag.addAlternative(new Indicator("{", "}", 'r', "Undifferenzierter Text", NR, ""));

		newBibTag = new BibliographicTag("4010", "021M",
				"Titel des 1., auf der Haupttitelseite genannten beigefügten Werkes in Vorlageform", R, "", "");
		addTag(newBibTag);
		newBibTag.add(
				new Indicator("|", "|", 'S', "Funktionscode für NE unter dem Sachtitel des beigef. Werkes", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Sachtitel des beigef. Werkes (@{)", NR, ""));
		newBibTag.add(new Indicator(" // ", "", 'e', "Körperschaftl. Ergänzung zum Titel des beigef. Werkes", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'd', "Zusatz zum Sachtitel des beigef. Werkes", NR, ""));
		newBibTag.add(new Indicator(" = ", "", 'f', "Parallelsachtitel des beigef. Werkes(@)", R, ""));
		newBibTag.add(new Indicator(" // ", "", 'e',
				"Körperschaftl. Ergänzung zum 1.-4. Parallelsachtitel des beigef. Werkes", R, ""));
		newBibTag.add(new Indicator(" / ", "", 'h', "Verfasserangabe des beigef. Werkes", NR, ""));
		newBibTag.addAlternative(new Indicator("{", "}", 'r', "Undifferenzierter Text", NR, ""));

		newBibTag = new BibliographicTag("4020", "032@",
				"Ausgabebezeichnung, Tausender-Angabe (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)", NR,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'g', "Ausgabebezeichnung in Sortierform (m)", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Ausgabebezeichnung, Tsd.-Angabe", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'c', "Verfasserangabe", NR, ""));

		newBibTag = new BibliographicTag("4021", "032B",
				"Reprint-Vermerk (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'g', "Ausgabebezeichnung in Sortierform (m)", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Ausgabebezeichnung in Vorlageform", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'c', "Verfasserangabe", NR, ""));

		newBibTag = new BibliographicTag("4022", "032C",
				"Ausgabebezeichnung der Sekundärausgabe (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'g', "Reprint-Vermerk in Sortierform (m)", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Reprint-Vermerk in Vorlageform", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'c', "Verfasserangabe", NR, ""));

		newGndTag = new DefaultGNDTag("4023", "032D", " Version ", R, "251", "Version Information");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Version", NR, ""));
		newGndTag.add(DOLLAR_U_UNSPEZ);
		newGndTag.add(new Indicator('b', "Bezugssystem", NR, ""));

		newBibTag = new BibliographicTag("4024", "031N",
				"Zählung von fortlaufenden Ressourcen in maschinell interpretierbarer Form", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$d", "", 'd', "Bandzählung", R, ""));
		newBibTag.add(new Indicator("$e", "", 'e', "Heftzählung", R, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Tageszählung", R, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Monatszählung", R, ""));
		newBibTag.add(new Indicator("$j", "", 'j', "Berichtsjahr (Beginn)", R, ""));
		// Endegruppe:
		newBibTag.add(new Indicator("$n", "", 'n', "Bandzählung", R, ""));
		newBibTag.add(new Indicator("$o", "", 'o', "Heftzählung", R, ""));
		newBibTag.add(new Indicator("$l", "", 'l', "Tageszählung", R, ""));
		newBibTag.add(new Indicator("$m", "", 'm', "Monatszählung", R, ""));
		newBibTag.add(new Indicator("$k", "", 'k', "Berichtsjahr (Ende)", R, ""));
		newBibTag.add(new Indicator("$0", "", '0', "Kettung von Beginn- und Endegruppen", R, ""));
		newBibTag.add(new Indicator("$6", "", '6', "Kennzeichnung laufender Bestände", R, ""));

		newBibTag = new BibliographicTag("4028", "037H", "Kartographische Materialien: Geografische Koordinaten", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'A',
				"Indikator; 1. Pos.: a (analog) oder d (dezimal), 2. Pos.:: c "
						+ "(ungenaue Angaben) oder g (genaue Angaben) "
						+ "Pos. 3: x Ring-Typ nicht anwendbar 0 äußerer Ring " + "1 auszuschließender Ring",
				NR, ""));
		newBibTag.add(new Indicator("$d", "", 'd', "Koordinaten – westlichster Längengrad", NR, ""));
		newBibTag.add(new Indicator("$e", "", 'e', "Koordinaten – östlichster Längengrad", NR, ""));
		newBibTag.add(new Indicator("$f", "", 'f', "Koordinaten – nördlichster Breitengrad", NR, ""));
		newBibTag.add(new Indicator("$g", "", 'g', "Koordinaten – südlichster Breitengrad", NR, ""));
		newBibTag.add(new Indicator("$j", "", 'j', "Deklination - nördliche Grenze", NR, ""));
		newBibTag.add(new Indicator("$k", "", 'k', "Deklination - südliche Grenze", NR, ""));
		newBibTag.add(new Indicator("$m", "", 'm', "Rektaszension - östliche Grenze", NR, ""));
		newBibTag.add(new Indicator("$n", "", 'n', "Rektaszension - westliche Grenze", NR, ""));
		newBibTag.add(new Indicator("$p", "", 'p', "Äquinoktium", NR, ""));
		newBibTag.add(new Indicator("$r", "", 'r', "Distanz zur Erde", NR, ""));
		newBibTag.add(new Indicator("$s", "", 's', "G-Ring Breitengrad", NR, ""));
		newBibTag.add(new Indicator("$t", "", 't', "G-Ring Längengrad", NR, ""));
		newBibTag.add(new Indicator("$x", "", 'x', "Anfangsdatum", NR, ""));
		newBibTag.add(new Indicator("$y", "", 'y', "Enddatum", NR, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "Name des extraterrestrischen Körpers", NR, ""));
		newBibTag.add(TagDB.DOLLAR_U_SM_R);
		newBibTag.add(new Indicator("$S", "", 'S', "ISIL der Referenz-Datei", NR, ""));
		newBibTag.add(new Indicator("$0", "", '0', "Identifikationsnummer der Referenz-Datei", NR, ""));
		newBibTag.add(TagDB.DOLLAR_2);
		newBibTag.add(new Indicator("$v", "", 'v', "Bemerkungen", NR, ""));
		newBibTag.add(new Indicator("$3", "", '3', "Koordinaten-Spezifikation", NR, ""));

		newBibTag = new BibliographicTag("4030", "033A", "Veröffentlichungsangabe", R, "264",
				"PUBLICATION, DISTRIBUTION, ETC. (IMPRINT)");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", " ; ", true, 'p', 'a',
				"Verlagsort (@{) weitere Verlagsorte werden mit \" ; \" angeschlossen", R, ""));
		newBibTag.add(new Indicator(" : ", "", 'n', 'b', "Verlagsname(@{)", NR, ""));
		newBibTag.add(new Indicator("$h", "", 'h', 'c', "Datierung", NR, ""));
		newBibTag.add(DOLLAR_Z_ZEIT);
		newBibTag.add(new Indicator(" ***", "", '5', "Identifikationsnummer des Lieferanten", NR, ""));
		newBibTag.add(new Indicator(" %", "", 'm', "Relevanz für die Mahnpräsentation", NR, ""));
		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(DOLLAR_9);
		newBibTag.addAlternative(DOLLAR_8);
		// auch diese?
		newBibTag.addAlternative(new Indicator("$h", "", 'h', 'c', "Datierung", NR, ""));
		newBibTag.addAlternative(DOLLAR_Z_ZEIT);
		newBibTag.addAlternative(new Indicator(" ***", "", '5', "VLB-Identifikationsnummer des Verlags", NR, ""));
		newBibTag.addAlternative(new Indicator(" %", "", 'm', "Relevanz für die Mahnpräsentation", NR, ""));

		newBibTag = new BibliographicTag("4034", "033E", "Vertriebsangabe", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", " ; ", true, 'p', 'a',
				"Vertriebsort (@{) weitere Vertriebsorte werden mit \" ; \" angeschlossen", R, ""));
		newBibTag.add(new Indicator(" : ", "", 'n', 'b', "Vertriebsname(@{)", NR, ""));
		newBibTag.add(new Indicator("$h", "", 'h', "Datierung", NR, ""));
		newBibTag.add(DOLLAR_Z_ZEIT);
		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(DOLLAR_9);
		newBibTag.addAlternative(DOLLAR_8);
		newBibTag.addAlternative(new Indicator("", "", " ; ", true, 'p', 'a',
				"Vertriebsort (@{) weitere Vertriebsorte werden mit \" ; \" angeschlossen", R, ""));
		newBibTag.addAlternative(new Indicator(" : ", "", 'n', "Vertriebsname", NR, ""));
		newBibTag.addAlternative(new Indicator("$h", "", 'h', "Datierung", NR, ""));
		newBibTag.addAlternative(DOLLAR_Z_ZEIT);

		newBibTag = new BibliographicTag("4035", "033B",
				"Angaben zu früheren Verlagsorten und Verlegern bei Zeitschriften/Schriftenreihen", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4030");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4040", "033D", "Verlagsort, Verlag in Ansetzungsform", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", " ; ", true, 'p', 'a',
				"Vertriebsort (@{) weitere Vertriebsorte werden mit \" ; \" angeschlossen", R, ""));
		newBibTag.add(new Indicator(" : ", "", 'n', 'b', "Vertriebsname(@{)", NR, ""));
		newBibTag.addAlternative(DOLLAR_9);
		newBibTag.addAlternative(DOLLAR_8);

		newBibTag = new BibliographicTag("4045", "033C", "Herstellungsangabe", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4030");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4046", "033F", "Entstehungsangabe", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4030");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4048", "033N", "Verlagsort, Verlag der Sekundärausgabe", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4030");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4049", "033O",
				"Herstellungsort, Hersteller der Sekundärausgabe (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)",
				R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4030");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4050", "033H", "Verbreitungsort in normierter Form", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);

		newBibTag = new BibliographicTag("4060", "034D", "Umfangsangabe", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a',
				"Umfangsangabe, Anzahl der physischen Einheiten und/oder spezifische Materialbenennung", NR, ""));
		newBibTag.add(new Indicator(" ((", "))", 'b', "Dateiumfang", NR, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Dateiumfang", NR, ""));
		newBibTag.add(new Indicator("$d", "", 'd', "Dateiumfang", NR, ""));

		newBibTag = new BibliographicTag("4061", "034M", "Sonstige Angaben zum Datenträger und/oder Inhalt", NR, "",
				"");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(
				new Indicator("", "", 'a', "Sonstige physische und technische Angaben, Illustrationsangabe", NR, ""));

		newBibTag = new BibliographicTag("4062", "034I", "Format, Maßangaben und dgl.", R, "300", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', 'c', "Format, Maßangaben und dgl.", NR, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Breite", NR, ""));
		newBibTag.add(new Indicator("$d", "", 'd', "Durchmesser", NR, ""));
		newBibTag.add(new Indicator("$g", "", 'g', "Teilgewicht", NR, ""));
		newBibTag.add(new Indicator("$h", "", 'h', "Höhe", NR, ""));
		newBibTag.add(new Indicator("$k", "", 'k', "Gesamtgewicht", NR, ""));
		newBibTag.add(new Indicator("$t", "", 't', "Tiefe", NR, ""));
		newBibTag.add(new Indicator("$4", "", '4', "Code für Art des Maßes", NR, ""));

		newBibTag = new BibliographicTag("4063", "034K", "Begleitmaterial", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Begleitmaterial", NR, ""));

		newBibTag = new BibliographicTag("4065", "009A",
				"Besitznachweis für die Verfilmungsvorlage (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'b', "Ländercode", NR, ""));
		newBibTag.add(new Indicator("#", "", 'c', "Bibliothek", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'd', "Abteilung", NR, ""));
		newBibTag.add(new Indicator(" <", ">", 'a', "Signatur der Verfilmungsvorlage", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'h', "Angaben zu verfilmten Bänden", NR, ""));

		newBibTag = new BibliographicTag("4066", "009B",
				"Besitznachweis für den Sekundärausgabe-Master (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)",
				R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4065");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4067", "033P",
				"Urheber der Verfilmung (bei Zeitschriften/Schriftenreihen verwendet bis 01.03.2007)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4030");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4070", "031A", "Differenzierte Angaben zur Quelle", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$d", "", 'd', "Bandzählung", NR, ""));
		newBibTag.add(new Indicator("$e", "", 'e', "Heftzählung", NR, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Tageszählung", NR, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Monatszählung", NR, ""));
		newBibTag.add(new Indicator("$j", "", 'j', "Berichtsjahr", NR, ""));
		newBibTag.add(new Indicator("$h", "", 'h', "Seitenangabe", NR, ""));
		newBibTag.add(new Indicator("$i", "", 'i', "Gesamtzahl Artikelseiten", NR, ""));
		newBibTag.add(new Indicator("$y", "", 'y', "modifizierte Anzeigeform", NR, ""));

		newBibTag = new BibliographicTag("4083", "009P", "Adresse im Archiv- oder Multimedia-Bereitstellungssystem", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("##", "##", 'S', "Lizenzindikator; Zugriffsrechte", NR, ""));
		newBibTag.add(new Indicator("", "", '0',
				"File format (HTML, PDF, PF, TXT, WP etc.) in Satzart O "
						+ "oder: Einleitungstext zur URL für den Zugriff im "
						+ "Multimedia- Bereitstellungssystem (alle Satzarten außer O)",
				NR, ""));
		newBibTag.add(new Indicator("=A ", "", 'a', "URL", NR, ""));
		newBibTag.add(new Indicator("=X ", "", 'x', "Interne Bemerkungen", NR, ""));
		newBibTag.add(new Indicator("=Z ", "", 'z', "Allgemeine Bemerkungen", NR, ""));

		newBibTag = new BibliographicTag("4085", "009Q",
				"Elektronische Adresse und Zugriffsart für " + "eine Elektronische Ressource im Fernzugriff", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("*", "*", 'T', "Zugriffsmethode (E-Mail, FTP, Telnet, Dial-up, andere)", NR, ""));
		newBibTag.add(new Indicator("=a ", "", 'a', "Name des Host", R, ""));
		newBibTag.add(new Indicator("=b ", "", 'b', "IP-Zugriffsnummer", NR, ""));
		newBibTag.add(new Indicator("=c ", "", 'c', "Art der Komprimierung", R, ""));
		newBibTag.add(new Indicator("=d ", "", 'd', "Zugriffspfad für eine Datei", R, ""));
		newBibTag.add(new Indicator("=f ", "", 'f', "Elektronischer Name der Datei im Verzeichnis des Host", R, ""));
		newBibTag.add(new Indicator("=g ", "", 'g', "URN (Uniform Resource Name) Nicht mehr in Benutzung, siehe 2050",
				R, ""));
		newBibTag.add(new Indicator("=h ", "", 'h', "Durchführende Stelle einer Anfrage", NR, ""));
		newBibTag.add(new Indicator("=i ", "", 'i', "Anweisung für die Ausführung einer Anfrage", R, ""));
		newBibTag.add(new Indicator("=j ", "", 'j', "Datenübertragungsrate (Bits pro Sekunde)", NR, ""));
		newBibTag.add(new Indicator("=k ", "", 'k', "Passwort", NR, ""));
		newBibTag.add(new Indicator("=l ", "", 'l', "Logon/Login-Angabe", NR, ""));
		newBibTag.add(new Indicator("=m ", "", 'm', "Kontaktperson", R, ""));
		newBibTag.add(new Indicator("=n ", "", 'n', "Ort des Host", NR, ""));
		newBibTag.add(new Indicator("=o ", "", 'o', "Betriebssystem des Host", NR, ""));
		newBibTag.add(new Indicator("=p ", "", 'p', "Port", NR, ""));
		newBibTag.add(new Indicator("=q ", "", 'q', "Elektronischer Dateiformattyp", NR, ""));
		newBibTag.add(new Indicator("=r ", "", 'r', "Einstellungen für die Datenübertragung", NR, ""));
		newBibTag.add(new Indicator("=s ", "", 's', "Größe der Datei", R, ""));
		newBibTag.add(new Indicator("=t ", "", 't', "Unterstützte Terminalemulation", R, ""));
		newBibTag.add(new Indicator("=u ", "", 'u', "URL (Uniform Resource Locator)", R, ""));
		newBibTag.add(new Indicator("=v ", "", 'v', "Betriebszeiten des Host für die gewählte Zugangsart", R, ""));
		newBibTag.add(new Indicator("=w ", "", 'w', "Identifikationsnummer des verknüpften Datensatzes", R, ""));
		newBibTag.add(new Indicator("=x ", "", 'x', "URL-Herkunftskennzeichnung", R, ""));
		newBibTag.add(new Indicator("=z ", "", 'z', "Kennzeichnung kostenfreier Online-Ressourcen", R, ""));
		newBibTag.add(new Indicator("=y ", "", 'y', "Linktext", NR, ""));
		newBibTag.add(new Indicator("=2 ", "", '2', "Zugriffsmethode", NR, ""));
		newBibTag.add(new Indicator("=3 ", "", '3', "Bezugswerk", NR, ""));

		newBibTag = new BibliographicTag("4120", "036M", "1. gezählte Schriftenreihe der Sekundärausgabe (Verknüpfung)",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'x', "Sortierfähige Bandzählung (m) Sonderform \"#...#\" und \"#xxx#\"",
				NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator(" ; ", "", 'l',
				"Vorliegende, für die Nachtragung unter der Schriftenreihe relevante Bandzählung einschließlich UR-Angaben \"  /  \"",
				NR, ""));

		newBibTag = new BibliographicTag("4121", "036M/01",
				"2. gezählte Schriftenreihe der Sekundärausgabe (Verknüpfung)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4120");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4122", "036M/02",
				"3. gezählte Schriftenreihe der Sekundärausgabe (Verknüpfung)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4120");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4140", "036B",
				"Verknüpfung zur ersten (direkt übergeordneten) von zwei Überordnungen", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'x', "Sortierfähige Bandzählung (m)", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator(" ; ", "", 'l',
				"Vorliegende Bandzählung (hier ggf. aus Altdatenkonversion auch Stufungsanga- ben mit \"  =\")", NR,
				""));
		newBibTag.addAlternative(new Indicator("", "", 'a',
				"Gesamttitelangabe Innerhalb der Gesamttitelangabe werden die RAK-gemäßen Deskriptionszeichen verwendet.",
				NR, ""));

		newBibTag = new BibliographicTag("4160", "036D",
				"Verknüpfung zur einzigen bzw. zur zweiten (hierarchisch höchsten) anzugebenden Überordnung (einschließl. der Abteilungsangaben)",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'x',
				"Sortierfähige Bandzählung (m) Die Sonderform \"#  #\" wird aus der vorliegenden Band- zählung \"...\" abgeleitet.",
				NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator("*", "*", 'n', "Zählung der Abteilung", R, ""));
		newBibTag.add(new Indicator("++", "", 'p', "Titel der Abteilung", R, ""));
		newBibTag.add(new Indicator(" ; ", "",
				// Alternative wohl ein Missverständnis:
				// " = ",
				// true,
				'l', "Vorliegende Bandzählung (hier ggf. aus Altdatenkonversion auch Stufungsangaben mit \"  =\")", NR,
				""));
		newBibTag.addAlternative(new Indicator("#", "#", 'x',
				"Sortierfähige Bandzählung (m) Die Sonderform \"#  #\" wird aus der vorliegenden Band- zählung \"...\" abgeleitet.",
				NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'a',
				"Gesamttitelangabe Innerhalb der Gesamttitelangabe werden die RAK- gemäßen Deskriptionszeichen verwendet.",
				NR, ""));

		newBibTag = new BibliographicTag("4180", "036F",
				"1. gezählte Schriftenreihe (Verknüpfung) (bei Zeitschriften verwendet bis 01.03.2007)", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'x',
				"Sortierfähige Bandzählung (m) Sonderformen: \"#...#\" und \"#xxx#\"", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator(" ; ", "", 'l',
				"Vorliegende, für die Nachtragung unter der Schriftenrei- he relevante Bandzählung einschließlich UR-Angaben \"  :  \"",
				NR, ""));
		newBibTag
				.addAlternative(new Indicator("#", "#", 'x', "Sortierfähige Bandzählung Sonderform \"#...#\"", NR, ""));
		newBibTag.addAlternative(
				new Indicator("", "", 'a', "Sachtitel (AF), ggf. einschließlich UR-Angaben \"  /  \" (@{)", NR, ""));
		newBibTag.addAlternative(new Indicator(" ; ", "", 'l',
				"Vorliegende, für die Nachtragung unter der Schriftenrei- he relevante Bandzählung einschließlich UR-Angaben \"  :  \"",
				NR, ""));

		newBibTag = new BibliographicTag("4181", "036F/01", "2. gezählte Schriftenreihe (Verknüpfung)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4180");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4182", "036F/02", "3. gezählte Schriftenreihe (Verknüpfung)", NR, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4180");
		newBibTag.addInherited(inheritedTag);

		newBibTag = new BibliographicTag("4190", "036G", "1. - 3. ungezählte Schriftenreihe (VF)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator(" ", " ++ ", 'c', "einleitender Vortext zur Angabe der ungezählten Schriftenreihe",
				NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Sachtitel (@), ggf. einschließlich UR-Angaben \"  :  \"", NR, ""));
		newBibTag.add(
				new Indicator(" // ", "", 'b', "KV-Ergänzung zum Sachtitel, ggf. einschließlich UR- Angaben", NR, ""));

		newBibTag = new BibliographicTag("4207", "046E", "Angaben zum Inhalt elektronischer Ressourcen", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("{", "}", 'p', "Publikationsart", R, ""));
		newBibTag.add(new Indicator("", "", 'a', "allg. Angaben", NR, ""));

		newBibTag = new BibliographicTag("4212", "046C",
				"Abweichende Titel (Frühere Titel, Titelzusätze, Paralleltitel und abweichende Titel (Kopftitel, Umschlagtitel))",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", ": ", 'b', "Vortext, ggf. mit zeitlicher Gültigkeit", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Abweichender Titel", NR, ""));

		newBibTag = new BibliographicTag("4213", "046D", "Früherer Haupttitel", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", ": ", 'b', "Vortext, ggf. mit zeitlicher Gültigkeit", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Haupttitel (@{)", NR, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "\"e\"  -  erster Haupttitel", NR, ""));
		newBibTag.add(new Indicator("%", "", 'p', "Bemerkungen (Unterfeld wird mit RDA-Umstellung nicht mehr belegt)",
				NR, ""));

		newBibTag = new BibliographicTag("4215", "046F",
				"Vermerke zur Verfasserangabe (Feld wird mit RDA-Umstieg nicht mehr belegt)", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Vermerke zur Verfasserangabe", NR, ""));

		newBibTag = new BibliographicTag("4216", "046G",
				"Reproduktionsvermerk - in Altdaten bis 30.09.2015 (RDA-Umstieg): "
						+ "Anmerkung zur Ausgabebezeichnung und Angaben zur Freiwilligen Selbstkontrolle der "
						+ "Filmwirtschaft",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Angaben/Anmerkung", NR, ""));
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);

		newBibTag = new BibliographicTag("4221", "046L",
				"Angaben über Schrift, Sprache und Vollständigkeit der Vorlage (ab 01.03.2007 verwendet bei Zeitschriften/Schriftenreihen)",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(
				new Indicator("", "", 'a', "Angaben über Schrift, Sprache und Vollständigkeit der Vorlage", NR, ""));

		newBibTag = new BibliographicTag("4222", "046M", "Angaben zum Inhalt", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Inhaltsangaben unaufgegliedert", NR, ""));
		newBibTag.addAlternative(new Indicator("$t", "", 't', "Manifestationstitel", NR, ""));
		newBibTag.addAlternative(new Indicator("$h", "", 'h', "Verantwortlichkeitsangabe", NR, ""));

		newBibTag = new BibliographicTag("4224", "046O",
				"Teilungsvermerk bei fortlaufenden Sammelwerken (ab 01.03.2007 verwendet bei Zeitschriften/Schriftenreihen)",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Teilungsvermerk bei fortlaufenden Sammelwerken", NR, ""));

		newBibTag = new BibliographicTag("4225", "046P", "Angaben zur Zählung von fortlaufenden Ressourcen", NR, "",
				"");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', " ohne feldinterne Steuerzeichen ", NR, ""));

		newBibTag = new BibliographicTag("4226", "046Q",
				"Hinweise auf unselbständig enthaltene Werke (ab 01.03.2007 verwendet bei Zeitschriften/Schriftenreihen)",
				NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "Hinweise auf unselbständig enthaltene Werke", NR, ""));

		newBibTag = new BibliographicTag("4233", "046X", "Bestandserhaltungsmaßnahmen und Archivierungsabsprachen", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("*", "*", '0', "Archivierungsstände bei Elektronischen Ressourcen", NR, ""));
		newBibTag.add(new Indicator("$3", "", '3', "Bestandsangaben", NR, ""));
		newBibTag.add(new Indicator("$a", "", 'a', "Codierung für Bestandsschutz oder Diverses (Altdaten nur in DNB)",
				NR, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Datum der Aktion", NR, ""));
		newBibTag.add(new Indicator("$f", "", 'f', "Kontext/Rechtsgrundlage", R, ""));
		newBibTag.add(new Indicator("$h", "", 'h', "Rechtliche Verantwortung", NR, ""));
		newBibTag.add(new Indicator("$i", "", 'i', "Methode", NR, ""));
		newBibTag.add(new Indicator("$k", "", 'k', "Durchführender Akteur", NR, ""));
		newBibTag.add(new Indicator("$l", "", 'l', "Schadensbilder", NR, ""));
		newBibTag.add(new Indicator("$z", "", 'z', "Anmerkung", NR, ""));
		newBibTag.add(new Indicator("$5", "", '5', "Bibliothek/Institution", NR, ""));

		newBibTag = new BibliographicTag("4237", "037G", "Spezifische Fußnote zur Sekundärausgabe", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(new Indicator("", "", 'a', "einleitende Wendung zur Fußnote der Sekundärausgabe", NR, ""));
		newBibTag.add(new Indicator(" # ", "", 'b', "Angaben zur Sekundärausgabe", NR, ""));

		/*
		 * Die Unterfelder $l und $t sind nicht ganz klar. $t kommt so vor: 4244
		 * f#Vorangegangen ist$tISBN:9783899812350 (wenige Fälle) $l kommt eigentlich
		 * nur bei Personen vor (im pica+-Datenabzug, Expansion der Verlinkung), hier
		 * Beiname.
		 */
		newBibTag = new BibliographicTag("4241", "039B",
				"Beziehung zu einer größeren Einheit \n" + "Angabe in der Beilage "
						+ "(nur bei fortlaufenden Ressourcen) \n" + "oder Angabe im Artikel- oder Heftdatensatz \n"
						+ "(bei Zeitschriftenartikeln, Zeitschriftenheften " + "oder EP-Images)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(DOLLAR_A_BEZ);
		newBibTag.add(INDICATOR_N_ANM);
		//
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		//
		newBibTag.add(DOLLAR_TITEL);
		newBibTag.add(DOLLAR_D_ORT);
		newBibTag.add(DOLLAR_E_VERLAG);
		newBibTag.add(DOLLAR_F_DATUM);
		newBibTag.add(DOLLAR_H_PHYS);
		newBibTag.add(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		// ----Alternative:
		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(DOLLAR_A_BEZ);
		newBibTag.addAlternative(INDICATOR_N_ANM);
		//
		newBibTag.addAlternative(DOLLAR_L_SCHOEPF);
		newBibTag.addAlternative(DOLLAR_TITEL);
		newBibTag.addAlternative(DOLLAR_D_ORT);
		newBibTag.addAlternative(DOLLAR_E_VERLAG);
		newBibTag.addAlternative(DOLLAR_F_DATUM);
		newBibTag.addAlternative(DOLLAR_H_PHYS);
		newBibTag.addAlternative(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		newBibTag.addAlternative(DOLLAR_R);

		// --

		newBibTag = new BibliographicTag("4242", "039C", "Beziehung zu einer kleineren Einheit", R, "", "");
		addTag(newBibTag);
		bibBasis = getPica3("4241");
		newBibTag.addInherited(bibBasis);

		// --
		newBibTag = new BibliographicTag("4243", "039D", "Beziehung auf Manifestationsebene - außer Reproduktionen", R,
				"", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis); // = 4241

		// ---
		newBibTag = new BibliographicTag("4244", "039E", "Vorgänger-Nachfolger-Beziehung auf Werkebene", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(DOLLAR_B_CHRON);
		newBibTag.add(DOLLAR_A_BEZ);
		newBibTag.add(INDICATOR_N_ANM);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(DOLLAR_TITEL);
		newBibTag.add(DOLLAR_D_ORT);
		newBibTag.add(DOLLAR_E_VERLAG);
		newBibTag.add(DOLLAR_F_DATUM);
		newBibTag.add(DOLLAR_H_PHYS);
		newBibTag.add(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		// -Alternative:
		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(DOLLAR_B_CHRON);
		newBibTag.addAlternative(DOLLAR_A_BEZ);
		newBibTag.add(INDICATOR_N_ANM);
		newBibTag.addAlternative(DOLLAR_L_SCHOEPF);
		newBibTag.add(DOLLAR_TITEL);
		newBibTag.add(DOLLAR_D_ORT);
		newBibTag.add(DOLLAR_E_VERLAG);
		newBibTag.add(DOLLAR_F_DATUM);
		newBibTag.add(DOLLAR_H_PHYS);
		newBibTag.add(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		newBibTag.addAlternative(DOLLAR_R);

		newBibTag = new BibliographicTag("4245", "039S", "Teil-Ganzes-Beziehung auf Werkebene", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis); // 4241

		bibBasis = getPica3("4244");
		newBibTag = new BibliographicTag("4246", "039V",
				"Chronologische Verknüpfung / Vorgänger (Nationales ISSN-Zentrum)", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis); // 4244

		newBibTag = new BibliographicTag("4247", "039W",
				"Chronologische Verknüpfung / Nachfolger (Nationales ISSN-Zentrum)"
						+ "(Feld wird nicht mehr verwendet, sobald ISSN-Zentrum in ZDB erfasst)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis); // 4244

		newBibTag = new BibliographicTag("4248", "039X", "Beziehung auf Expressionsebene", R, "", "");
		addTag(newBibTag);
		bibBasis = getPica3("4241");
		newBibTag.addInherited(bibBasis); // 4241

		newBibTag = new BibliographicTag("4249", "039Z", "Andere Beziehung auf Werk- und Expressionsebene sowie "
				+ "beschreibende Beziehungen auf Manifestationsebene", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis); // 4241

		newBibTag = new BibliographicTag("4255", "039H", "Reproduktion - gleiche physische Form", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_T);
		newBibTag.add(DOLLAR_U_GR);
		newBibTag.add(DOLLAR_A_BEZ);
		//
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		//
		newBibTag.add(DOLLAR_TITEL);
		newBibTag.add(DOLLAR_D_ORT);
		newBibTag.add(DOLLAR_E_VERLAG);
		newBibTag.add(DOLLAR_F_DATUM);
		newBibTag.add(DOLLAR_H_PHYS);
		newBibTag.add(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		// ----Alternative:
		newBibTag.addAlternative(DOLLAR_T);
		newBibTag.addAlternative(DOLLAR_U_GR);
		newBibTag.addAlternative(DOLLAR_A_BEZ);
		//
		newBibTag.addAlternative(DOLLAR_L_SCHOEPF);
		newBibTag.addAlternative(DOLLAR_TITEL);
		newBibTag.addAlternative(DOLLAR_D_ORT);
		newBibTag.addAlternative(DOLLAR_E_VERLAG);
		newBibTag.addAlternative(DOLLAR_F_DATUM);
		newBibTag.addAlternative(DOLLAR_H_PHYS);
		newBibTag.addAlternative(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);

		bibBasis = newBibTag; // 4255
		newBibTag = new BibliographicTag("4256", "039I", "Reproduktion - andere physische Form", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		newBibTag = new BibliographicTag("4260", "048G", "Bestandsschutzmaßnahmen ", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Verfilmungsabsicht", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Bibliothek", NR, ""));
		newBibTag.add(new Indicator("$", "$", 'f', "Datum einer geplanten Bestandsschutznahme", NR, ""));

		newBibTag = new BibliographicTag("4261", "039T", "Verknüpfung zum rezensierten Werk", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_A_BEZ);
		newBibTag.add(INDICATOR_N_ANM);
		//
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		//
		newBibTag.add(DOLLAR_TITEL);
		newBibTag.add(DOLLAR_D_ORT);
		newBibTag.add(DOLLAR_E_VERLAG);
		newBibTag.add(DOLLAR_F_DATUM);
		newBibTag.add(DOLLAR_H_PHYS);
		newBibTag.add(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		// ----Alternative:
		newBibTag.addAlternative(DOLLAR_A_BEZ);
		newBibTag.addAlternative(INDICATOR_N_ANM);
		//
		newBibTag.addAlternative(DOLLAR_L_SCHOEPF);
		newBibTag.addAlternative(DOLLAR_TITEL);
		newBibTag.addAlternative(DOLLAR_D_ORT);
		newBibTag.addAlternative(DOLLAR_E_VERLAG);
		newBibTag.addAlternative(DOLLAR_F_DATUM);
		newBibTag.addAlternative(DOLLAR_H_PHYS);
		newBibTag.addAlternative(DOLLAR_O_ANDERE);
		newBibTag.add(DOLLAR_I_ISB);
		newBibTag.add(DOLLAR_X_DOI);
		newBibTag.add(DOLLAR_U_UNSPEZ);
		newBibTag.add(DOLLAR_Y_URN);
		newBibTag.addAlternative(DOLLAR_R);

		bibBasis = newBibTag;
		newBibTag = new BibliographicTag("4262", "039U", "Verknüpfung zum rezensierten Werk", R, "", "");
		addTag(newBibTag);
		newBibTag.addInherited(bibBasis);

		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "0", "07" + x + "A",
					"Typisierung und einleitende Wendung für die " + (x + 1) + ". Feldgruppe", NR, "", "");
			addTag(newBibTag);
			newBibTag.add(new Indicator("|", "|", 'a',
					"Typ und NE-Steuerung: 2 Kleinbuchstaben (Typ) und + oder - als NE-Steuerung", NR, ""));
			newBibTag.add(new Indicator("", "", 'c',
					"Einleitende Wendung für die Feldgruppe, sofern nicht aus Typ ableitbar", NR, ""));
			newBibTag = new BibliographicTag("46" + x + "0", "07" + x + 10 + "M",
					"Typisierung und einleitende Wendung für die " + (x + 11) + ". Feldgruppe", NR, "", "");
			addTag(newBibTag);
			newBibTag.add(new Indicator("|", "|", 'a',
					"Typ und NE-Steuerung: 2 Kleinbuchstaben (Typ) und + oder - als NE-Steuerung", NR, ""));
			newBibTag.add(new Indicator("", "", 'c',
					"Einleitende Wendung für die Feldgruppe, sofern nicht aus Typ ableitbar", NR, ""));
		}

		bibBasis = getPica3("3000");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "1", "07" + x + "B",
					"1.-3. Primärverfasser für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "1", "07" + x + "N",
					"1.-3. Primärverfasser für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3010");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "2", "07" + x + "C",
					"1. -3. sonstige beteiligte Person für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "2", "07" + x + "O",
					"1. -3. sonstige beteiligte Person für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3100");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "3", "07" + x + "D",
					"1. -3. Primärkörperschaft für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "3", "07" + x + "P",
					"1. -3. Primärkörperschaft für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3110");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "4", "07" + x + "E",
					"1. -3. Sekundärkörperschaft für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "4", "07" + x + "Q",
					"1. -3. Sekundärkörperschaft für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3200");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "5", "07" + x + "F",
					"Sammlungsvermerk, Formalsachtitel (nicht HE) für die " + (x + 1) + ". Feldgruppe", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "5", "07" + x + "R",
					"Sammlungsvermerk, Formalsachtitel (nicht HE) für die " + (x + 11) + ". Feldgruppe", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3210");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "6", "07" + x + "G",
					"Einheitssachtitel/Formalsachtitel - HE für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "6", "07" + x + "S",
					"Einheitssachtitel/Formalsachtitel - HE für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3211");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "7", "07" + x + "H",
					"Einheitssachtitel - nicht  HE für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "7", "07" + x + "T",
					"Einheitssachtitel - nicht  HE für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("3220");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "8", "07" + x + "J",
					"Ansetzungssachtitel für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "8", "07" + x + "U",
					"Ansetzungssachtitel für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("4000");
		for (int x = 0; x <= 9; x++) {
			newBibTag = new BibliographicTag("45" + x + "9", "07" + x + "K",
					"Sachtitel für die " + (x + 1) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);

			newBibTag = new BibliographicTag("46" + x + "9", "07" + x + "V",
					"Sachtitel für die " + (x + 11) + ". Feldgruppe", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("4700", "047A",
				"Bearbeiterzeichen, Bemerkungen zur Titelaufnahme (internes Feld)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Herkunftskennung", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Bearbeiterzeichen", NR, ""));
		newBibTag.add(new Indicator("#", "", 'd',
				"Sammel- bzw. Nichtsammelgebietsvermerk Ist generell anzugeben mit der Zeichenfolge 'NSG' oder 'SG' (bei Zeitschriften verwendet bis 01.03.2007)",
				NR, ""));
		newBibTag.add(new Indicator("++", "", 'e',
				"Begründung zum Sammel- bzw. Nichtsammelgebietsvermerk (bei Zeitschriften verwendet bis 01.03.2007)",
				NR, ""));
		newBibTag.add(new Indicator("*", "", 'c', "Bemerkungen zur Titelaufnahme", NR, ""));
		newBibTag.add(new Indicator("****", "", 'f', "Angaben zu alten Verlagsorten bei maschineller Datenübernahme",
				NR, ""));
		newBibTag.add(new Indicator("$g", "", 'g', "Kennzeichnung zur Geschäftsgangssteuerung (Code)", NR, ""));
		newBibTag.add(new Indicator("$h", "", 'h', "Kennzeichnung zur Geschäftsgangssteuerung (Text)", NR, ""));
		// newBibTag.addAlternative(
		// new Indicator("|", "|", 'S', "Herkunftskennung (in Altdaten optional), Codes
		// s.o.", NR, ""));
		// newBibTag.addAlternative(new Indicator("", "", 'a', "Bearbeiterzeichen", NR,
		// ""));
		// newBibTag.addAlternative(new Indicator("*", "", 'c',
		// "Bemerkungen zur Titelaufnahme (In 4700 $c wurden deskriptiv nach \" ## \"
		// auch ggf. die Angaben des frü- heren Feldes 4201 übernommen.
		// Unterfelddifferenzie- rung erfolgt nicht.)",
		// NR, ""));
		// newBibTag.addAlternative(new Indicator("****", "", 'f',
		// "Angaben zu alten Verlagsorten bei maschineller Datenübernahme", NR, ""));

		newBibTag = new BibliographicTag("4701", "047B",
				"Bearbeiterzeichen, Sammel- bzw. Nichtsammelgebietsvermerk, Bemerkungen zur Erwerbung", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Codes wie bei 4700, optional", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Bearbeiterzeichen", NR, ""));
		newBibTag.add(new Indicator("#", "", 'd',
				"Sammel- bzw. Nichtsammelgebietsvermerk Ist generell anzugeben mit der Zeichenfolge 'NSG' oder 'SG' (bei Zeitschriften verwendet bis 01.03.2007)",
				NR, ""));
		newBibTag.add(new Indicator("++", "", 'e',
				"Begründung zum Sammel- bzw. Nichtsammelgebietsvermerk (bei Zeitschriften verwendet bis 01.03.2007)",
				NR, ""));
		newBibTag.add(new Indicator("*", "", 'c', "Bemerkungen zur Erwerbung", NR, ""));

		newBibTag = new BibliographicTag("4711", "047R",
				"Angaben zur Rechteklärung sowie zum urheberrechtlichen Status", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator('j', "Beginn der Schutzfrist", NR, ""));
		newBibTag.add(new Indicator('s', "Status der Rechteklärung", NR, ""));
		newBibTag.add(new Indicator('k', "Kommentar zu $s in strukturierter Form", NR, ""));
		newBibTag.add(new Indicator('f', "Freitext", NR, ""));

		newBibTag = new BibliographicTag("4712", "047T", "Datum der Rechteklärung", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator('D', "Datum der Rechteklärung", NR, ""));
		newBibTag.add(new Indicator('n', "Namenszeichen", NR, ""));
		newBibTag.add(new Indicator('f', "Freitext", NR, ""));

		newBibTag = new BibliographicTag("4715", "047I",
				"Elektronische Adresse für Dateien mit inhaltlichen Beschreibungen zum Dokument", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("=u ", "", 'u', "URL", NR, ""));
		newBibTag.add(new Indicator("=a ", "", 'a', "Sprache", NR, ""));
		newBibTag.add(new Indicator("=b ", "", 'b', "Format (z. B. html, pdf)", NR, ""));
		newBibTag.add(new Indicator("=c ", "", 'c', "Textart (s. ONIX-Codes)", NR, ""));
		newBibTag.add(new Indicator("=d ", "", 'd', "Herkunft (MVB oder DISS)", NR, ""));
		newBibTag.add(new Indicator("=e ", "", 'e',
				"codierte Angaben über URL, z.B.: 1= intern 2= extern 9= Feld soll gelöscht werden (manuelle Vergabe)",
				NR, ""));
		newBibTag.add(new Indicator("=y ", "", 'y', "Linktext", NR, ""));

		newBibTag = new BibliographicTag("4720", "047N", "Angaben aus Fremddaten-Importen", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Herkunftsformat", NR, ""));
		newBibTag.add(new Indicator("[", "]", '2', "Herkunftsfeldname", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Inhalt des Herkunftsfeldes", NR, ""));

		newBibTag = new BibliographicTag("5050", "045E", "Sachgruppen der Deutschen Nationalbibliografie", R, "082",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'e', 'a', "DDC-Haupt-Sachgruppe", NR, ""));
		newBibTag.add(new Indicator(";", "", 'f', "DDC-Neben-Sachgruppe", R, ""));
		newBibTag.add(new Indicator("%", "", 'a', "Hauptsachgruppe ab 1982", NR, ""));
		newBibTag.add(new Indicator("&", "", 'd', "Nebensachgruppe ab 1982", R, ""));
		newBibTag.add(new Indicator("*", "", 'b',
				"Sachgruppen der Deutschen Bibliografie bis 1981 (nur aus Altdatenkonversion)", R, ""));
		newBibTag.add(new Indicator("#", "", 'c',
				"Sachgruppen der Deutschen Nationalbibliografie der DDR (nur aus Altdatenkonversion)", R, ""));
		newBibTag.add(
				new Indicator("+", "", 'm', "maschinell gebildete DDC-Sachgruppe (aus alten DNB- Sachgruppen)", R, ""));
		newBibTag.add(new Indicator("$E", "", 'E',
				"Kennzeichnung der Erfassungsart m = maschinell gebildet p = aus Paralellausgabe übernommen a = aus Ablieferung übernommen i = intellektuell vergeben (Angabe fakultativ)",
				NR, ""));
		newBibTag.add(new Indicator("$H", "", 'H',
				"Herkunft dnb durch die DNB masch. erzeugte Sachgruppe mrc Ablieferung im Format MARC 21 onx Ablieferung im Format ONIX xmp Ablieferung im Format XmetadissPlus wbf Ablieferung über das Webformular",
				NR, ""));
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);

		newBibTag = new BibliographicTag("5100", "041A", "1. bzw. einziges Element der 1. Schlagwortfolge gemäß RSWK",
				NR, "689", '0', '0', "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		newBibTag.addAlternative(new Indicator(":", "", 'a',
				"ggf. Indikator und Blank Schlagworttext (@{) ggf. Blank und Operator und Schlagwortelemente, die sich grundsätzlich nicht in der SWD befinden Für die Volltextspeicherung gilt die Datenstruktur der „oder“-Variante.",
				NR, ""));
		newBibTag.addAlternative(new Indicator("{", "}", '6', "GND-IDN (temporär bei maschineller Übernahme)", NR, ""));
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);

		newBibTag = new BibliographicTag("5101", "041A/01", "2. Element der 1. Schlagwortfolge", NR, "689", '0', '1',
				"");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		newBibTag.addAlternative(new Indicator(":", "", 'a',
				"ggf. Indikator und Blank Schlagworttext (@{) ggf. Blank und Operator und Schlagwortelemente, die sich grundsätzlich nicht in der SWD befinden Für die Volltextspeicherung gilt die Datenstruktur der „oder“-Variante.",
				NR, ""));
		newBibTag.addAlternative(new Indicator("{", "}", '6', "GND-IDN (temporär bei maschineller Übernahme)", NR, ""));
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);

		bibBasis = getPica3("5101");
		newBibTag = new BibliographicTag("5105", "041A/05", "6.-10. Element der 1. Schlagwortfolge", R, "689", '0', '5',
				"");
		addTag(newBibTag);
		marcMap.put(newBibTag.marc + '0' + '6', newBibTag);
		marcMap.put(newBibTag.marc + '0' + '7', newBibTag);
		marcMap.put(newBibTag.marc + '0' + '8', newBibTag);
		marcMap.put(newBibTag.marc + '0' + '9', newBibTag);
		newBibTag.addInherited(bibBasis);

		newBibTag = new BibliographicTag("5108", "041A/08",
				"Vorgegebene(s) Permutationsmuster zur 1. Schlagwortfolge (verwendet bis zum 1.4.2010)", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$", "", 'f', "Permutationsmuster", R, ""));

		newBibTag = new BibliographicTag("5109", "041A/09", "Angaben zur 1. Schlagwortfolge", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("(", ")", 'e', "ISIL der vergebenden Bibliothek", NR, ""));
		newBibTag.add(new Indicator("{", "}", 'r', "ISIL des Verbundes", NR, ""));
		newBibTag.add(new Indicator("|", "", 'g', "Systematiknummer(n) (verwendet bis zum 31.12.2003)", R, ""));
		newBibTag.add(new Indicator("/", "", 'h', "Ländercode(s) (verwendet bis zum 31.12.2003)", R, ""));
		newBibTag.add(new Indicator("[", "]", 'l', "Bemerkungen zur Schlagwortfolge", NR, ""));
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);

		bibBasis = getPica3("5100");
		for (int folge = 2; folge <= 10; folge++) {
			// 1. Elemente auffüllen:
			newBibTag = new BibliographicTag("51" + (folge - 1) + "0", "041A/" + (folge - 1) + "0",
					"1. Element der " + folge + ". Schlagwortfolge", NR, "689", (char) ('0' + folge - 1), '0', "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("5101");
		for (int element = 3; element <= 5; element++) {
			// 1. Folge auffüllen:
			newBibTag = new BibliographicTag("510" + (element - 1), "041A/0" + (element - 1),
					element + ". Element der 1. Schlagwortfolge", NR, "689", '0', (char) ('0' + element - 1), "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		for (int element = 2; element <= 5; element++) {
			// Rest auffüllen:
			for (int folge = 2; folge <= 10; folge++) {
				newBibTag = new BibliographicTag("51" + (folge - 1) + "" + (element - 1),
						"041A/" + (folge - 1) + "" + (element - 1),
						element + ". Element der " + folge + ". Schlagwortfolge", NR, "689", (char) ('0' + folge - 1),
						(char) ('0' + element - 1), "");
				addTag(newBibTag);
				newBibTag.addInherited(bibBasis);
			}
		}

		for (int folge = 2; folge <= 10; folge++) {
			// 1. Elemente auffüllen:
			newBibTag = new BibliographicTag("51" + (folge - 1) + "5", "041A/" + (folge - 1) + "5",
					"6.-10. Element der " + folge + ". Schlagwortfolge", R, "689", (char) ('0' + folge - 1), '5', "");
			addTag(newBibTag);
			marcMap.put(newBibTag.marc + ((char) ('0' + folge - 1)) + '6', newBibTag);
			marcMap.put(newBibTag.marc + ((char) ('0' + folge - 1)) + '7', newBibTag);
			marcMap.put(newBibTag.marc + ((char) ('0' + folge - 1)) + '8', newBibTag);
			marcMap.put(newBibTag.marc + ((char) ('0' + folge - 1)) + '9', newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("5108");
		for (int folge = 2; folge <= 10; folge++) {
			// 1. Elemente auffüllen:
			newBibTag = new BibliographicTag("51" + (folge - 1) + "8", "041A/" + (folge - 1) + "8",
					"Vorgegebene(s) Permutationsmuster zur " + folge + ". Schlagwortfolge", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("5109");
		for (int folge = 2; folge <= 10; folge++) {
			// 1. Elemente auffüllen:
			newBibTag = new BibliographicTag("51" + (folge - 1) + "9", "041A/" + (folge - 1) + "9",
					"Angaben zur " + folge + ". Schlagwortfolge", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("5400", "045F", "1. DDC-Notation: Vollständige Notation", NR, "082", '0', '4',
				"DEWEY DECIMAL CLASSIFICATION NUMBER");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'e', '2', "Angabe der zugrunde liegenden DDC-Ausgabe", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Synthetische Notation", NR, ""));
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);

		newBibTag = new BibliographicTag("5403", "045F/03", "1. DDC-Notation: Notationen aus Hilfstafeln", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("-T1--", "", 'f', "Notation aus Hilfstafel 1", R, ""));
		newBibTag.add(new Indicator("-T2--", "", 'g', "Notation aus Hilfstafel 2", R, ""));
		newBibTag.add(new Indicator("-T3A--", "", 'h', "Notation aus Hilfstafel 3A", NR, ""));
		newBibTag.add(new Indicator("-T3B--", "", 'i', "Notation aus Hilfstafel 3B", NR, ""));
		newBibTag.add(new Indicator("-T3C--", "", 'j', "Notation aus Hilfstafel 3C", NR, ""));
		newBibTag.add(new Indicator("-T4--", "", 'k', "Notation aus Hilfstafel 4", NR, ""));
		newBibTag.add(new Indicator("-T5--", "", 'l', "Notation aus Hilfstafel 5", NR, ""));
		newBibTag.add(new Indicator("-T6--", "", 'm', "Notation aus Hilfstafel 6", NR, ""));

		bibBasis = getPica3("5400");
		for (int notation = 2; notation <= 5; notation++) {
			final char c = (char) ('F' + notation - 1);
			newBibTag = new BibliographicTag("54" + (notation - 1) + "0", "045" + c,
					notation + ". DDC-Notation: Vollständige Notation", NR, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		bibBasis = getPica3("5403");
		for (int notation = 2; notation <= 5; notation++) {
			final char c = (char) ('F' + notation - 1);
			newBibTag = new BibliographicTag("54" + (notation - 1) + "3", "045" + c + "/03",
					notation + ". DDC-Notation: Notationen aus Hilfstafeln", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("5450", "045Z", "Notation eines Klassifikationsystems (aus Fremddaten)", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Name der Klassifikation, evtl. um Jahr/Ausgabe ergänzt", NR, ""));
		newBibTag.add(new Indicator("", "", " $ ", true, 'a', "Notation der in $b beschriebenen Klassifikation, "
				+ "weitere Notationen werden mit \"  $  \" angeschlossen.", R, ""));
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_D_DATUM);

		newGndTag = new DefaultGNDTag("5460", "045X", "Thema-Klassifikation: mainsubject", NR, "", "");
		addTag(newGndTag);
		newGndTag.add(new Indicator('x', "Thema-Text", NR, ""));
		newGndTag.add(new Indicator('v', "Thema-Version", NR, ""));
		newGndTag.add(new Indicator('q', "Thema-Quelle (Sourcename)", NR, ""));
		newGndTag.add(new Indicator('o', "ONIX-Code für Thema-Klasse", NR, ""));
		newGndTag.addDefaultFirst(new Indicator('a', "Thema-Code (Subjects+Qualifier)", NR, ""));
		newGndTag.add(DOLLAR_H);
		newGndTag.add(DOLLAR_E);

		bibBasis = newGndTag;
		newGndTag = new DefaultGNDTag("5461", "045X/01", "Thema-Klassifikation: subjects", R, "", "");
		addTag(newGndTag);
		newGndTag.addInherited(bibBasis);

		newBibTag = new BibliographicTag("5470", "045K", "Automatisch vergebene DDC-Notation", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'e', "Kennzeichnung", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "DDC-Notation", NR, ""));
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);

		newBibTag = new FixOrderTag("5530", "044F", "Schlagwörter aus Altdaten der Deutschen Nationalbibliothek", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("|", "|", 'S', "Code für Personenschlagwort", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Schlagwort (@{)", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'f', "Unterschlagwort", R, ""));
		newBibTag.add(new Indicator("$", "", 't', "Orts- und Länderschlüssel", NR, ""));
		newBibTag.add(new Indicator("|", "", 's', "Systematiknummer(n)", R, ""));
		newBibTag.add(new Indicator("/", "", 'e', "Ländercode(s)", R, ""));
		newBibTag.add(new Indicator("}", "", 'v', "Schlagwortverweisung", NR, ""));
		newBibTag.addAlternative(new Indicator("#", "", 'g', "Hauptschlagwort", NR, ""));
		newBibTag.addAlternative(new Indicator(" / ", "", 'h', "Unterschlagwort", R, ""));

		newBibTag = new BibliographicTag("5540", "044H", "Automatisch vergegebenes Schlagwort", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Kennung für Art des Schlagworts / Quelle", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator("$L", "", 'L', "LCSH-Identifier", NR, ""));
		newBibTag.add(TagDB.DOLLAR_U_SM_R);
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		newBibTag.add(new Indicator("$R", "", 'R', "Relevanzbewertung", NR, ""));
		newBibTag.add(new Indicator("$T", "", 'T', "Datum der Relevanzbewertung", NR, ""));
		// ---
		newBibTag.addAlternative(new Indicator("[", "]", 'b', "Kennung für Art des Schlagworts / Quelle", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'a', "Schlagwort", NR, ""));
		newBibTag.addAlternative(new Indicator("$L", "", 'L', "LCSH-Identifier", NR, ""));
		newBibTag.addAlternative(TagDB.DOLLAR_U_SM_R);
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);
		newBibTag.add(new Indicator("$R", "", 'R', "Relevanzbewertung", NR, ""));
		newBibTag.add(new Indicator("$T", "", 'T', "Datum der Relevanzbewertung", NR, ""));

		newBibTag = new BibliographicTag("5550", "044K", "GND-Schlagwörter (aus Fremddaten)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Kennung für Art des Schlagworts / Quelle", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator("{", "}", '5', "Temporäre Verknüpfungsnummer", NR, ""));
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_K);
		newBibTag.add(DOLLAR_D_DATUM);
		// -
		newBibTag.addAlternative(new Indicator("[", "]", 'b', "Kennung für Art des Schlagworts / Quelle", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", " $ ", true, 'a',
				"SWD-Schlagwort, weitere Schlagwörter werden " + "mit \" $ \" angeschlossen", R, ""));
		newBibTag.addAlternative(new Indicator("{", "}", '5', "Temporäre Verknüpfungsnummer", NR, ""));
		newBibTag.addAlternative(DOLLAR_E);
		newBibTag.addAlternative(DOLLAR_H);
		newBibTag.addAlternative(DOLLAR_K);
		newBibTag.addAlternative(DOLLAR_D_DATUM);

		newBibTag = new BibliographicTag("5560", "044N", "Deskriptoren aus einem Thesaurus", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Name des Thesaurus", NR, ""));
		newBibTag.add(new Indicator("", "*", '0', "ID-Nummer des Deskriptors", NR, ""));
		newBibTag.add(new Indicator("|", "|", 'e', "Entitätenkennzeichen", NR, ""));
		newBibTag.add(new Indicator("", "", " $ ", true, 'a', "Deskriptor des in $b beschriebenen Thesaurus, "
				+ "weitere Deskriptoren werden mit \" $ \" angeschlossen", R, ""));
		newBibTag.add(DOLLAR_E);
		newBibTag.add(DOLLAR_H);
		newBibTag.add(DOLLAR_D_DATUM);
		newBibTag.addAlternative(new Indicator("[", "]", 'b', "Codierte Angabe zum Inhalt", NR, ""));
		newBibTag
				.addAlternative(new Indicator("", "", " $ ", true, 'a', "Deskriptor des in $b beschriebenen Thesaurus, "
						+ "weitere Deskriptoren werden mit \" $ \" angeschlossen", R, ""));

		newBibTag = new BibliographicTag("5585", "044G", "Literarische Gattung", R, "", "");
		addTag(newBibTag);
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator(" *", "", 'p', "Bemerkungen", NR, ""));

		newBibTag = new BibliographicTag("5590", "044P/00", "1. Gestaltungsmerkmal auf bibliografischer Ebene", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Label", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.add(new Indicator(" *", "", 'p', "Bemerkungen", NR, ""));
		newBibTag.addAlternative(new Indicator("[", "]", 'b', "Label", NR, ""));
		newBibTag.addAlternative(new Indicator("(", ")", 'e', "Entität eines freien Schlagworts", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'a', "freies Schlagwort", NR, ""));
		newBibTag.addAlternative(new Indicator(" *", "", 'p', "Bemerkungen", NR, ""));

		bibBasis = getPica3("5590");
		for (int i = 2; i <= 10; i++) {
			newBibTag = new BibliographicTag("559" + (i - 1), "044P/0" + (i - 1),
					i + ". Gestaltungsmerkmal auf bibliografischer Ebene", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		// ------------Exemplardaten: --------------------

		/*
		 * Die Struktur ist ziemlich komplex: "/XX" bezeichnet (in pica+) das XX-te
		 * Exemplar der jeweiligen Bibliothek. Bei jeder Bibliothek wird von neuem bei
		 * 01 beginnend durchnumeriert.
		 *
		 * Bei einigen Tags stimmen die Pica+ überein. Dann wird durch das Unterfeld $x
		 * differenziert. In diesem stehen die letzten beiden Ziffern von Pica3. Es kann
		 * sein, dass mit RDA hier eine grundsätzliche Änderung eintritt.
		 */

		final Indicator ignorable = new Indicator("$x", "", 'x',
				"Interne Feldnumerierung (00) (m); wird im Externformat nicht dargestellt", NR, "");

		newGndTag = new DefaultGNDTag("4800", "247C/XX", "Bibliothekskennzeichen", R, "", "");
		addTag(newGndTag);
		newGndTag.add(TagDB.DOLLAR_9);
		newGndTag.add(TagDB.DOLLAR_8);
		addHoldings(newGndTag);

		newBibTag = new BibliographicTag("4801", "237A/XX", "Exemplarbezogener Kommentar", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Kommentar für interne Zwecke", NR, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Zustand des Exemplars als Code", NR, ""));
		newBibTag.add(new Indicator("((", "))", 'k', "Konvolutmarkierung", NR, ""));
		newBibTag.add(new Indicator("%", "", 'l', "Kommentar zur Fernleihe ", NR, ""));
		addHoldings(newBibTag);

		newGndTag = new DefaultGNDTag("4802", "220B/XX", "Bestandsschutz-Maßnahmen", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Bestandsschutzmaßnahmen Äquivalent zu 4233 $a (Level0)", NR, ""));
		newGndTag.add(new Indicator('b', "Art der Bestandsschutzmaßnahme (3stelliger Code)", NR, ""));
		newGndTag.add(new Indicator('c', "Status der Bestandsschutzmaßnahme (4stelliger Code)", NR, ""));
		newGndTag.add(new Indicator('d', "Projektcode der Bestandsschutzmaßnahme", NR, ""));
		newGndTag.add(new Indicator('e', "Dienstleister der Bestandsschutzmaßnahme", NR, ""));
		newGndTag.add(new Indicator('f', "Auftragsnummer der Bestandsschutzmaßnahme", NR, ""));
		newGndTag.add(new Indicator('g', "Chargenummer der Bestandsschutzmaßnahme", NR, ""));
		newGndTag.add(DOLLAR_D_DATUM);
		addHoldings(newGndTag);

		newBibTag = new BibliographicTag("4803", "237B/XX",
				"Feld zur Kennzeichnung von Zweifelsfällen auf der Exemplarebene (nur aus Altdatenkonversion)", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a',
				"Codierung für Anfrage unl Code für unleserlichen Scan anf Code für inhaltliche Anfrage ev Anfragen zum Erscheinungsverlauf",
				NR, ""));
		newBibTag.add(new Indicator("*", "", 'c',
				"Anfragetext (ein Anfragetext sollte nur im Zusammenhang mit Code 'anf' vorkommen)", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("4820", "220A/XX", "Provienzvermerk in der ZDB", NR, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$a", "", 'a', "Alte Signaturen", NR, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Exemplarspezifische bibliografische Zitate", NR, ""));
		newBibTag.add(new Indicator("$e", "", 'e', "Exemplarhinweise", NR, ""));
		newBibTag.add(new Indicator("$k", "", 'k', "Kaufvermerke", NR, ""));
		newBibTag.add(new Indicator("$m", "", 'm', "Marginalien", NR, ""));
		newBibTag.add(new Indicator("$p", "", 'p', "Provenienz", NR, ""));
		addHoldings(newBibTag);

		newGndTag = new DefaultGNDTag("4821", "220C/XX",
				"Statistik sowie Dokumentation von Geschäftsgängen, " + "Wertentwicklung und Verwendung des Exemplars",
				R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Anzahl", NR, ""));
		newGndTag.add(new Indicator('c', "Aktionscode", NR, ""));
		newGndTag.add(new Indicator('l', "Lieferant", NR, ""));
		newGndTag.add(new Indicator('m', "Materialart", NR, ""));
		newGndTag.add(new Indicator('q', "Quelle der Wertermittlung", NR, ""));
		newGndTag.add(new Indicator('t', "Teil", NR, ""));
		newGndTag.add(new Indicator('w', "Wert (inkl. Währungsangabe)", NR, ""));
		newGndTag.add(new Indicator('z',
				"Geschäftsgangstyp / Zweck " + "(der Wertermittlung, der statistischen Erfassung, …)", NR, ""));
		newGndTag.add(new Indicator('D', "Datum (JJJJ-MM-TT)", NR, ""));
		newGndTag.add(new Indicator('E', "abschließendes Datum (JJJJ-MM-TT)", NR, ""));
		newGndTag.add(new Indicator('K', "Kommentar", NR, ""));
		newGndTag.add(new Indicator('N', "Nutzerkennung", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("4822", "220D/XX", "Paketzugehörigkeit", NR, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Paketzugehörigkeit", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("6700", "245Z/XX", "Lokale Notation", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a',
				"Angabe einer Notation eines Klassfikationssystems hier: Überschriften der Systematik des DEA", NR,
				""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("6710", "245Y/XX", "Aufstellung innerhalb/Zugehörigkeit zu einer Sammlung", R, "",
				"");
		addTag(newGndTag);
		newGndTag.add(TagDB.DOLLAR_9);
		newGndTag.add(TagDB.DOLLAR_8);
		newGndTag.add(new Indicator('l', "Positionierung", NR, ""));
		newGndTag.add(new Indicator('v', "Bemerkung", NR, ""));
		addHoldings(newGndTag);

		newBibTag = new BibliographicTag("6800", "244Z/XX", "1. Gestaltungsmerkmal des Exemplars", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("[", "]", 'b', "Label", NR, ""));
		newBibTag.add(DOLLAR_9);
		newBibTag.add(DOLLAR_8);
		newBibTag.addIgnorable(ignorable);
		newBibTag.addAlternative(new Indicator("[", "]", 'b', "Label", NR, ""));
		newBibTag.addAlternative(new Indicator("(", ")", 'e', "Entität eines freien Schlagworts", NR, ""));
		newBibTag.addAlternative(new Indicator("", "", 'a', "freies Schlagwort", NR, ""));
		newBibTag.addAlternative(ignorable);
		addHoldings(newBibTag);

		newGndTag = new DefaultGNDTag("6819", "244Y", "Objektbezeichnung auf Exemplar-Ebene", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Objektbezeichnung", NR, ""));
		newGndTag.add(new Indicator('B', "Typ der Objektbezeichnung", NR, ""));

		bibBasis = getPica3("6800");
		for (int i = 2; i <= 10; i++) {
			newBibTag = new BibliographicTag("680" + (i - 1), "244Z/XX" + "680" + (i - 1),
					i + ". Gestaltungsmerkmal des Exemplars", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("7001", "208@/01",
				"1. Exemplar eines Standorts (Pflichtexemplar): Datum und exemplarspezifischer Selektionsschlüssel (m, wenn 0701 vorhanden)",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", " : ", 'a', "Datum TT-MM-JJ (m)", NR, ""));
		newBibTag.add(new Indicator("", "", 'b', "Selektionsschlüssel", NR, ""));

		bibBasis = getPica3("7001");
		for (int i = 2; i <= 99; i++) {
			String s;
			if (i < 10) {
				s = "0" + i;
			} else {
				s = "" + i;
			}
			newBibTag = new BibliographicTag("70" + s, "208@/" + s, i
					+ ". Exemplar eines Standorts (Pflichtexemplar): Datum und exemplarspezifischer Selektionsschlüssel (m, wenn 0701 vorhanden)",
					R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("7100", "209A/XX", "Signatur", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Magazin-/Signatur", NR, ""));
		newBibTag.add(new Indicator(" ((", "))", 'c', "Erläuterungen / Kommentare zur Signatur", NR, ""));
		newBibTag.add(new Indicator(" @ ", "", 'd', "Ausleihindikator", NR, ""));
		newBibTag.add(new Indicator(" % ", "", 'l', "Leihverkehrsrelevanz", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("7101", "209A/XX-7101", "2. weitere Signatur", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Signatur", NR, ""));
		newBibTag.add(new Indicator(" ((", "))", 'c', "Erläuterungen / Kommentare zur Signatur", NR, ""));
		newBibTag.add(new Indicator(" @ ", "", 'd', "Ausleihindikator", NR, ""));
		newBibTag.add(new Indicator(" % ", "", 'l', "Leihverkehrsrelevanz", NR, ""));
		newBibTag.add(new Indicator("!!", "!!", 'f', "aktueller Standort", NR, ""));
		newBibTag.add(new Indicator(" ; ", "", 'g', "Signatur am aktuellen Standort / Aufstellungsnotation", NR, ""));
		newBibTag.addIgnorable(ignorable);

		bibBasis = getPica3("7101");
		for (int i = 3; i <= 9; i++) {
			newBibTag = new BibliographicTag("710" + (i - 1), "209A/XX" + "710" + (i - 1),
					i + "9. weitere Magazin-/Grundsignatur", R, "", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new BibliographicTag("7109", "209A/XX-7109", "Aufstellungsnotation", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("!!", "!!", 'f', "(Sonder-)Standort", NR, ""));
		newBibTag.add(new Indicator(" ; ", "", 'g', "(Sonder-)Standortsignatur", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new HoldingsTag("7100", "209A/01", "Mehrzwecktag für Signaturen", R, "", "", "71");
		newBibTag.addInherited(getPica3("7100"));
		newBibTag.addInherited(getPica3("7109"));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7120", "231@/XX", "Bestandsdaten und Lizenzzeiträume in normierter Form", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$d", "", 'd', "Bandzählung (Beginn)", R, ""));
		newBibTag.add(new Indicator("$e", "", 'e', "Heftzählung (Beginn)", R, ""));
		newBibTag.add(new Indicator("$b", "", 'b', "Tageszählung (Beginn)", R, ""));
		newBibTag.add(new Indicator("$c", "", 'c', "Monatszählung (Beginn)", R, ""));
		newBibTag.add(new Indicator("$j", "", 'j', "Berichtsjahr (Beginn)", R, ""));

		newBibTag.add(new Indicator("$n", "", 'n', "Bandzählung (Ende)", R, ""));
		newBibTag.add(new Indicator("$o", "", 'e', "Heftzählung (Ende)", R, ""));
		newBibTag.add(new Indicator("$l", "", 'b', "Tageszählung (Ende)", R, ""));
		newBibTag.add(new Indicator("$m", "", 'c', "Monatszählung (Ende)", R, ""));
		newBibTag.add(new Indicator("$k", "", 'k', "Berichtsjahr (Ende)", R, ""));
		newBibTag.add(new Indicator("$0", "", '0', "Kettung von Beginn- und Endegruppen", R, ""));

		newBibTag.add(new Indicator("$6", "", '6', "Kennzeichnung laufender Bestände", NR, ""));
		newBibTag.add(new Indicator("+Y", "", 'r', "Moving wall: Nur die <Zahl> Jahrgänge sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("-Y", "", 's', "Moving wall: Nur die <Zahl> Jahrgänge sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("+V", "", '3', "Moving wall: Nur die <Zahl> Bände sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("-V", "", '7', "Moving wall: Nur die <Zahl> Bände sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("+M", "", 't', "Moving wall: Nur die <Zahl> Monate sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("-M", "", 'u', "Moving wall: Nur die <Zahl> Monate sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("+D", "", 'z', "Moving wall: Nur die <Zahl> Tage sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("-D", "", 'y', "Moving wall: Nur die <Zahl> Tage sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("+I", "", 'v', "Moving wall: Nur die <Zahl> Hefte sind zugänglich", NR, ""));
		newBibTag.add(new Indicator("-I", "", 'w', "Moving wall: Nur die <Zahl> Hefte sind zugänglich", NR, ""));

		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7130", "209I/XX", "Rechte (außer Urheberrecht)", R, "", "");
		addTag(newBibTag);
		inheritedTag = getPica3("4713");
		newBibTag.addInherited(inheritedTag);
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7131", "209R/XX",
				"Angaben zur Präsentation (Elektronische Adresse für Dateien mit inhaltlichen  Beschreibungen zum Dokument",
				R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("= u ", "", 'u', "URL", NR, ""));
		newBibTag.add(new Indicator("=a ", "", 'a', "Sprache", NR, ""));
		newBibTag.add(new Indicator("=b ", "", 'b', "Format (z. B. html, pdf)", NR, ""));
		newBibTag.add(new Indicator("=c ", "", 'c', "Textart (s. ONIX-Codes)", NR, ""));
		newBibTag.add(new Indicator("=d ", "", 'd', "Herkunft", NR, ""));
		newBibTag.add(new Indicator("=e ", "", 'e',
				"codierte Angaben über URL, z.B.: 1 = intern 2 = extern 9 = Feld soll gelöscht werden (manuelle Vergabe)",
				NR, ""));
		newBibTag.add(new Indicator("=y ", "", 'y', "Linktext", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7133", "209K/XX", "Angaben zu Zugriffsrechten", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a',
				"Codierte Angaben Mögliche Werte: a domain der Zugriff ist nur hausintern möglich b free der Zugriff ist unbeschränkt möglich c blocked der Zugriff ist gar nicht möglich d domain+ der Zugriff ist hausintern und für bestimmte zugelassene andere Nutzer möglich q  der Zugriff ist gesperrt",
				NR, ""));
		newBibTag.add(new Indicator("+", "", 'b', "Angaben zur Anzahl der parallelen Zugriffe", NR, ""));
		newBibTag.add(new Indicator("*", "", 'c', "Kommentar", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7134", "209L/XX", "Codierte Angaben zum Dokument-, Preis und Lizenztyp", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Codierte Angaben", NR, ""));
		newBibTag.add(new Indicator("#", "", 'b', "Codierte Angaben zum Lizenztyp", NR, ""));
		newBibTag.add(new Indicator("+", "", 'c', "Codierte Angaben zum Preistyp", NR, ""));
		newBibTag.add(new Indicator("*", "", 'd', "Kommentar", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7135", "209S/XX",
				"Elektronische Adresse und Zugriffsart für eine Elektronische Ressource im Fernzugriff (Level2)", R, "",
				"");
		addTag(newBibTag);
		inheritedTag = getPica3("4085");
		newBibTag.addInherited(inheritedTag);
		addHoldings(newBibTag);

		newGndTag = new DefaultGNDTag("7136", "204U/XX", "Persistent Identifier: URN (Level2) ", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Persistent Identifier vom Typ \"URN\"", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("7137", "204P/XX", "Persistent Identifier: DOI (Level2) ", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Persistent Identifier vom Typ \"DOI\"", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("7138", "204R/XX", "Persistent Identifier: Handle (Level2) ", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Persistent Identifier vom Typ \"Handle\"", NR, ""));
		addHoldings(newGndTag);

		newBibTag = new BibliographicTag("7140", "231L/XX-7140", "Lizenzzeiträume in normierter Form", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("/v", "", 'd', "Beginngruppe: Bandzählung", NR, ""));
		newBibTag.add(new Indicator("/a", "", 'e', "Beginngruppe: Heftzählung", NR, ""));
		newBibTag.add(new Indicator("/d", "", 'b', "Beginngruppe: Tageszählung", NR, ""));
		newBibTag.add(new Indicator("/m", "", 'c', "Beginngruppe: Monatszählung", NR, ""));
		newBibTag.add(new Indicator("/b", "", 'j', "Beginngruppe:  Berichts-, Erscheinungs-, Lizenzjahr", NR, ""));
		newBibTag.add(new Indicator("/V", "", 'n', "Endegruppe: Bandzählung", NR, ""));
		newBibTag.add(new Indicator("/A", "", 'o', "Endegruppe: Heftzählung", NR, ""));
		newBibTag.add(new Indicator("/D", "", 'l', "Endegruppe: Tageszählung", NR, ""));
		newBibTag.add(new Indicator("/M", "", 'm', "Endegruppe: Monatszählung", NR, ""));
		newBibTag.add(new Indicator("/E", "", 'k', "Endegruppe:  Berichts-, Erscheinungs-, Lizenzjahr", NR, ""));
		newBibTag.add(new Indicator("; ", "", '0', "Endegruppe: Kettung von Beginn- und Endegruppen", NR, ""));
		newBibTag.add(new Indicator("-", "", '6', "Endegruppe: Kennzeichnung laufender Bestände", NR, ""));
		newBibTag.add(new Indicator("+Y", "", 'r',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (nur die <Zahl> " + "Jahrgänge sind zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("-Y", "", 's',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (die <Zahl> " + "Jahrgänge sind nicht zugänglich)",
				NR, ""));
		newBibTag.add(new Indicator("+V", "", '3',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (nur die " + "<Zahl> Bände sind  zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("-V", "", '7',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (die <Zahl>" + "Bände sind nicht zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("+M", "", 't',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl>  (nur die" + "<Zahl> Monate sind  zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("-M", "", 'u',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (die <Zahl> " + "Monate sind nicht zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("+D", "", 'z',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (bur die <Zahl> " + " Tage sind zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("-D", "", 'y',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (die <Zahl> " + "Tage sind nicht zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("+I", "", 'v',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (nur die " + "<Zahl> Hefte sind zugänglich)", NR,
				""));
		newBibTag.add(new Indicator("-I", "", 'w',
				"Kennzeichnung einer \"moving wall\":  <3st. Zahl> (die <Zahl> " + "Hefte sind nicht zugänglich)", NR,
				""));
		newBibTag.addIgnorable(ignorable);

		bibBasis = getPica3("7140");
		for (int i = 1; i <= 9; i++) {
			newBibTag = new BibliographicTag("714" + i, "231L/XX" + "714" + i, "Lizenzzeiträume in normierter Form", R,
					"", "");
			addTag(newBibTag);
			newBibTag.addInherited(bibBasis);
		}

		newBibTag = new HoldingsTag("7140", "231L/01", "Mehrzwecktag für Lizenzzeiträume", R, "", "", "714");
		newBibTag.addIgnorable(ignorable);
		newBibTag.addInherited(getPica3("7140"));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("7150", "209M/XX-7150", "Signatur des anderen Standorts", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Signatur des anderen Standorts", NR, ""));
		newBibTag.add(new Indicator(" ((", "))", 'c', "Erläuterungen / Kommentare zur Signatur", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("7159", "209M/XX-7159", "Aufstellungsnotation des anderen Standorts", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator("!!", "!!", 'f', "Standort", NR, ""));
		newBibTag.add(new Indicator(" ; ", "", 'g', "Aufstellungsnotation", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new HoldingsTag("7150", "209M/01", "Mehrzwecktag für andere Standorte", R, "", "", "715");
		newBibTag.addIgnorable(ignorable);
		newBibTag.addInherited(getPica3("7150"));
		newBibTag.addInherited(getPica3("7159"));
		addHoldings(newBibTag);

		newGndTag = new DefaultGNDTag("7800", "203@/XX", "Exemplar-Identifikationsnummer (EID) (m)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Exemplar-Identifikationsnummer (EID) (m)", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("7900", "201B/XX",
				"Datum und Uhrzeit der Erfassung bzw. letzten Änderung des Exemplardatensatzes (m)", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Datum TT-MM-JJ (m)", NR, ""));
		newGndTag.add(new Indicator(" ", "", 't', "Uhrzeit HH:MM:SS.ZZZ (m)", NR, ""));
		addHoldings(newGndTag);

		/*
		 * Die Sache geht so: 8000-8034 hat den pica+-tag 209B/01 (bzw. bei Wiederholung
		 * 209B/02 ...). Die exakte Pica3-Nummer ergibt sich aus dem Unterfeld $x. In
		 * diesem stehen zwei Ziffern. $xYZ -> 80YZ
		 *
		 */
		newBibTag = new BibliographicTag("8000", "209B/XX", "Erwerbungsart (nicht mehr verwendet seit 1. März 2007)", R,
				"", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Erwerbungsart", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("8001", "209B/XX-8001", "Materialcode auf Exemplarebene", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("$g", "", 'g', "???", NR, ""));
		newBibTag.add(new Indicator("$a", "", 'a', "???", NR, ""));
		newBibTag.add(new Indicator("%", "", 'c', "Angaben von Materialcodes auf Exemplarebene", NR, ""));
		newBibTag.add(new Indicator("{", "}", 'd', "Materialspezifische Codes auf Exemplarebene", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("8031", "209B/XX-8031",
				"einleitender Text zur zusammenfassenden Bestandsangabe", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "einleitender Text zur zusammenfassenden Bestandsangabe", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("8032", "209B/XX-8032", "Zusammenfassende Bestandsangabe", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "#", 'g', "Sortierhilfe zur Sortierung der Exemplardatensätze", NR, ""));
		newBibTag.add(new Indicator("", "", 'a', "Zusammenfassende Bestandsangabe", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("8033", "209B/XX-8033", "Lückenangabe für Druckzwecke (allgemein)", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Lückenangabe für Druckzwecke (allgemein)", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new BibliographicTag("8034", "209B/XX-8034", "Kommentar zum Bestand", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", 'a', "Kommentar zum Bestand", NR, ""));
		newBibTag.addIgnorable(ignorable);

		newBibTag = new HoldingsTag("8000", "209B/01", "Mehrzwecktag 8000 -80034", R, "", "", "80");
		newBibTag.addInherited(getPica3("8000"));
		newBibTag.addInherited(getPica3("8001"));
		newBibTag.addInherited(getPica3("8031"));
		newBibTag.addInherited(getPica3("8032"));
		newBibTag.addInherited(getPica3("8034"));
		addHoldings(newBibTag);

		// --------------------

		newGndTag = new DefaultGNDTag("8100", "209C/XX", "Zugangsnummer / Akzessionsnummer ", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('a', "Angabe einer Zugangs- oder Akzessionsnummer", NR, ""));
		addHoldings(newGndTag);

		newBibTag = new BibliographicTag("8410", "204E/XX",
				"Serien- oder Lizenznummer bei monografischen elektronischen Publikationen", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", '0',
				"Serien- oder Lizenznummer für die Installation von elektronischen Publikationen", NR, ""));
		newBibTag.add(new Indicator("!!", "!!", 'p',
				"Bemerkungen zur Serien- oder Lizenznummer von elektronischen Publikationen", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("8448", "233S/XX", "Verlagsort, Verleger der Sekundärausgabe ", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator(" ; ", "", 'p',
				"Verlagsort(@{) weitere Verlagsorte werden mit \" ; \" angeschlossen", R, ""));
		newBibTag.add(new Indicator(" : ", "", 'n', "Verlag (@{)", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("8449", "233O/XX", "Herstellungsort, Hersteller der Sekundärausgabe", R, "",
				"");
		addTag(newBibTag);
		newBibTag.add(new Indicator(" ; ", "", 'p',
				"Herstellungsort (@{) weitere Herstellungsorte werden mit \" ; \" angeschlossen", R, ""));
		newBibTag.add(new Indicator(" : ", "", 'n', "Hersteller (@{)", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("8465", "233Q/XX",
				"Besitznachweis für die Verfilmungs- oder Digitalisierungsvorlage", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "", 'c', "Bibliothek", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'd', "Abteilung", NR, ""));
		newBibTag.add(new Indicator(" <", ">", 'a', "Signatur der Verfilmungsvorlage / Sonderstandort", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'h', "Angaben zu verfilmten Bänden", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("8466", "233R/XX", "Besitznachweis für den Sekundärausgabe-Master", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("#", "", 'c', "Bibliothek", NR, ""));
		newBibTag.add(new Indicator(" / ", "", 'd', "Abteilung", NR, ""));
		newBibTag.add(new Indicator(" <", ">", 'a', "Signatur des Sekundärausgabe-Masters / Sonderstandort", NR, ""));
		newBibTag.add(new Indicator(" : ", "", 'h', "Angaben zum Umfang", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("8467", "233P/XX",
				"1. bis 2. Urheber / Auftraggeber der Verfilmung oder der Digitalisierung ", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator(" : ", "", 'n',
				"Name des Urhebers / Auftraggebers der Verfilmung oder der Digitalisierung", NR, ""));
		addHoldings(newBibTag);

		newBibTag = new BibliographicTag("8510", "245G/XX", "SSG-Angaben", R, "", "");
		addTag(newBibTag);
		newBibTag.add(new Indicator("", "", " ; ", true, 'a',
				"Sondersammelgebietsnummer weitere Sondersammelgebietsnummern werden mit \" ; \" angeschlossen", R,
				""));
		newBibTag.add(new Indicator("#", "", 'b', "Sondersammelgebietsnotation", R, ""));
		newBibTag.add(new Indicator("%", "", 'c', "ZDB-Prioritätszahl", R, ""));
		newBibTag.add(new Indicator("{", "}", 'd', "Finanzierungsart.", NR, ""));
		addHoldings(newBibTag);

		newGndTag = new DefaultGNDTag("8594", "206L", "Langzeitarchivierung", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Kennzeichen für langzeitarchivierte Daten", NR, ""));

		newGndTag = new DefaultGNDTag("8595", "206W/XX",
				"Nummer der gescannten Katalogkarte der Deutschen Nationalbibliothek Leipzig in Exemplardaten (nur aus Altdatenkonversion)",
				R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Nummer der gescannten Katalogkarte", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("8596", "206X/XX", "Lokaldaten-Identifikationsnummer aus Altdatenkonversion", R,
				"", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(
				new Indicator('0', "ZDB-Lokaldaten-Identifikationsnummer (LID) aus Altdatenkonversion", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("8597", "206Y/XX", "Regionale Identifikationsnummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Regionale Identifikationsnummer", NR, ""));
		addHoldings(newGndTag);

		newGndTag = new DefaultGNDTag("8598", "206Z/XX", "Lokale Identifikationsnummer", R, "", "");
		addTag(newGndTag);
		newGndTag.addDefaultFirst(new Indicator('0', "Lokale Identifikationsnummer", NR, ""));
		addHoldings(newGndTag);

	}

	/**
	 * Erwartet, dass pica+ nicht leer ist und verwendet den Teil vor "/".
	 *
	 * @param tag nicht null
	 */
	private void addHoldings(final Tag tag) {
		final DecimalFormat df = new DecimalFormat("00");
		final String[] parts = tag.picaPlus.split("/");
		final String picaPlusPrefix = parts[0];
		for (int i = 1; i <= MAX_PER_LIBRARY; i++) {
			picaPlusMap.put(picaPlusPrefix + "/" + df.format(i), tag);
			database.put(picaPlusPrefix + "/" + df.format(i), tag);
		}
	}

	@Override
	public Collection<Tag> getUnmodifiables() {
		if (unmodifiables == null) {
			unmodifiables = new HashSet<Tag>();
			unmodifiables.add(findTag("0100"));
			unmodifiables.add(findTag("0200"));
			unmodifiables.add(findTag("0300"));
			unmodifiables.add(findTag("0210"));
			unmodifiables.add(findTag("0230"));

			unmodifiables.add(findTag("2110"));
			unmodifiables.add(findTag("2240"));

			unmodifiables.add(findTag("7001"));
			unmodifiables.add(findTag("4800"));
			unmodifiables.add(findTag("7100"));
			unmodifiables.add(findTag("7120"));
			unmodifiables.add(findTag("7150"));
			unmodifiables.add(findTag("8032"));
			unmodifiables.add(findTag("8100"));

			unmodifiables.add(findTag("7900"));
			unmodifiables.add(findTag("7800"));

		}
		return unmodifiables;
	}

	/**
	 * Alles, was am Anfang steht.
	 *
	 * @return
	 */
	public Collection<Tag> getHeaders() {
		return getTagsBetween("0000", "2275");
	}

	/**
	 * Segment Personennamen: 30XX.
	 *
	 * @return
	 */
	public Collection<Tag> getPersonalNameSegment() {
		return getTagsBetween("3000", "3090");
	}

	/**
	 * Segment Körperschaftsnamen.
	 *
	 * @return
	 */
	public Collection<Tag> getCoporateNameSegment() {
		return getTagsBetween("3090", "3100");
	}

	/**
	 * Segment Sachtitel.
	 *
	 * @return
	 */
	public Collection<Tag> getTitleSegment() {
		return getTagsBetween("3200", "4011");
	}

	/**
	 * Segment Segment Veröff.-Vermerk, Umfang, Beilagen.
	 *
	 * @return
	 */
	public Collection<Tag> getEditionSegment() {
		return getTagsBetween("4020", "4085");
	}

	/**
	 * Segment Übergeordnete Gesamtheiten / Sekundärausgaben.
	 *
	 * @return
	 */
	public Collection<Tag> getSeriesStatementSegment() {
		return getTagsBetween("4105", "4190");
	}

	/**
	 * Segment Fußnoten.
	 *
	 * @return
	 */
	public Collection<Tag> getNoteSegment() {
		return getTags(NOTES);
	}

	/**
	 * Segment Titelverknüpfungen.
	 *
	 * @return
	 */
	public Collection<Tag> getTitleLinkSegment() {
		return getTags(TITLE_LINKS);
	}

	/**
	 * Segment Feldgruppen für Nicht-Standard-NEE.
	 *
	 * @return
	 */
	public Collection<Tag> getAddedEntryFieldsSegment() {
		return getTagsBetween("4500", "4699");
	}

	/**
	 * Segment Sonstige Angaben.
	 *
	 * @return
	 */
	public Collection<Tag> getOtherInformationSegment() {
		return getTags(OTHER_INFORMATIONS);
	}

	/**
	 * Segment Bearbeiterzeichen.
	 *
	 * @return
	 */
	public Collection<Tag> getResponsibilitySegment() {
		return getTagsBetween("4700", "4720");
	}

	/**
	 * Segment Sacherschließung (1131, 1133, 5050 - 5599).
	 *
	 * @return nicht null, modifizierbar
	 */
	public Collection<Tag> getSubjectAccessSegment() {
		final ArrayList<Tag> tags = new ArrayList<>(161);
		tags.addAll(getTags("1131", "1133"));
		tags.addAll(getTagsBetween("5050", "5599"));

		return tags;
	}

	/**
	 * Segment Exemplardaten.
	 *
	 * @return
	 */
	public Collection<Tag> getHoldingsSegment() {
		return getTags(HOLDINGS);
	}

	/**
	 * @param args nix.
	 * @throws IllFormattedLineException
	 */
	public static void main2(final String[] args) throws IllFormattedLineException {
		final BibTagDB dataB = getDB();

		final Collection<Tag> tags = dataB.getSubjectAccessSegment();
		System.out.println(tags.size());
	}

	/**
	 * @param args nix.
	 * @throws IllFormattedLineException
	 */
	public static void main(final String[] args) throws IllFormattedLineException {
		final String s = "51.[^89]";
		final Set<Tag> patt = getDB().findTagPattern(s);
		patt.forEach(System.out::println);
	}

}
