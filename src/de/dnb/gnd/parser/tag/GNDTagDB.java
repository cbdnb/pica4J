package de.dnb.gnd.parser.tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.utils.RecordUtils;

public final class GNDTagDB extends TagDB {

	/**
	 * Relationierte Notation.
	 */
	public static final DefaultGNDTag TAG_553 = new DefaultGNDTag("553", "045C", "Relationierte Notation", R, "553",
			"Valid Number Tracing");

	/**
	 * Allgemeine Kommentare in einer Klassifikation.
	 */
	public static final DefaultGNDTag TAG_900 = new DefaultGNDTag("900", "047A", "Allgemeine Kommentare", R, "", "");

	/**
	 * Synonyme Umschreibung für die Notation.
	 */
	public static final DefaultGNDTag TAG_453 = new DefaultGNDTag("453", "044F",
			"Synonyme Umschreibung für die Notation", R, "453", "");

	/**
	 * Zeitliche Gültigkeit.
	 */
	public static final Indicator INDICATOR_Z = new Indicator('Z', "Zeitliche Gültigkeit", NR, "");

	/**
	 *
	 */
	public static final Indicator DOLLAR_b = new Indicator('b', "Code", NR, "Content type code");

	public static final Indicator INDICATOR_153_J = new Indicator('j',
			"Umschreibung für die Notation, Klassenbenennung", NR, "Caption");

	public static final Indicator INDICATOR_153_B = new Indicator('b', "Hilfstafelnummer", R, "Table identification");

	public static final Indicator INDICATOR_153_A = new Indicator('a', "Notation", NR, "Classification Number");

	public static final DefaultGNDTag TAG_153 = new DefaultGNDTag("153", "045A",
			"Notation von Klassifikation oder Bestandsgliederung", NR, "153", "Heading - Classification Number");

	/*
	 * Indikatoren, die überall gleich auftauchen:
	 */
	//@formatter:off
	/**
	 *  Einziger Indikator für $4 (GND-Code für Beziehungen).
	 */
	public static final Indicator DOLLAR_4 = new Indicator('4', "GND-Code für Beziehungen", NR, "Relationship code");

	/**
	 * Einziger Indikator für $5
	 * (Institution (=ISIL), die Feld in besonderer Art verwendet).
	 */
	public static final Indicator DOLLAR_5 = new Indicator('5', "Institution (=ISIL), "
	  + "die die mit dem Datenfeld getroffene Aussage verantwortet.", R, "Institution to which field applies");

	/**
	 * Nicht eindeutiger Indikator für $g (Zusatz). Gibt es auch noch für
	 * Koordinaten und DDC-Zeitstempel.
	 */
	public static final Indicator DOLLAR_G = new Indicator('g', "Zusatz", R, "Qualifier", 'h');

	/**
	 * Einziger Indikator für $L
	 * (Sprachcode bei nicht-lateinischen Schriftzeichen).
	 */
	public static final Indicator DOLLAR_L = new Indicator('L', "Sprachcode bei "
	  + "nicht-lateinischen Schriftzeichen", NR, "");

	/**
	 * Einziger Indikator für $t
	 * (Feldzuordnung beim Titel).
	 */
	public static final Indicator DOLLAR_T_GROSS = new Indicator('t', "Titel, "
	  + "Weitere Unterfelder analog GND-PICA 130", NR, "");

	/**
	 * Einziger Indikator für $T
	 * (Feldzuordnung bei nicht- lateinischen Schriftzeichen).
	 */
	public static final Indicator DOLLAR_T = new Indicator('T', "Feldzuordnung bei "
	  + "nicht-lateinischen Schriftzeichen", NR, "");

	/**
	 * Einziger Indikator für $U
	 * (Schriftcode bei nicht-lateinischen Schriftzeichen).
	 */
	public static final Indicator DOLLAR_U_GR = new Indicator('U', "Schriftcode bei "
	  + "nicht-lateinischen Schriftzeichen", NR, "");

	/**
	 * Einziger Indikator für nicht wiederholbares $v (Bemerkungen).
	 */
	public static final Indicator DOLLAR_V_NR = new Indicator('v', "Bemerkungen", NR, "Note");

	/**
	 * Einziger Indikator für wiederholbares $v (Bemerkungen, Regelwerk).
	 */
	public static final Indicator DOLLAR_V_R = new Indicator('v', "Bemerkungen, Regelwerk", R, "Note");

	/**
	 * Nicht eindeutuger Indikator für $x (Allgemeine Unterteilung).
	 * Gibt es auch noch für 169.
	 *
	 */
	public static final Indicator DOLLAR_X = new Indicator('x', "Allgemeine Unterteilung", R, "General subdivision");

	static GNDTagDB gndTagDB;

	/**
	 * Nachname.
	 */
	public static final Indicator INDICATOR_100_A = new Indicator('a', "Nachname", NR, "");

	/**
	 * Nachgestelltes Präfix.
	 */
	public static final Indicator INDICATOR_100_C = new Indicator('c', '!',
		"Nachgestelltes Präfix", NR, "");

	/**
	 * Vorname.
	 */
	public static final Indicator INDICATOR_100_D = new Indicator(", ", "",
		'd', '!', "Vorname", NR, "");

	/**
	 * Titulatur.
	 */
	public static final Indicator INDICATOR_100_L = new Indicator('l', 'c',
		"Beiname, Gattungsname, Territorium, Titulatur", NR, "");

	/**
	 * Numeration.
	 */
	public static final Indicator INDICATOR_100_N = new Indicator('n', 'b',
		"Zählung", NR, "Numeration");

	/**
	 * Persönlicher Name.
	 */
	public static final Indicator INDICATOR_100_P = new Indicator('P',
		"Persönlicher Name", NR, "");

	public static final Indicator INDICATOR_110_A = new Indicator('a', "Hauptkörperschaft", NR, "Corporate "
	  + "name or jurisdiction name as entry element");

	public static final Indicator INDICATOR_110_B = new Indicator('b',
		"Untergeordnete Körperschaft", R, "Subordinate unit");

	public static final Indicator INDICATOR_110_N = new Indicator('n',
		"Zählung", R, "Number of part/section");

	public static final Indicator INDICATOR_111_A = new Indicator('a', "Hauptkongress", NR, "Meeting "
	  + "name or jurisdiction name as entry element");



	public static final Indicator INDICATOR_111_B = new Indicator('b',
		"Untergeordnete Einheit", R, "Number");

	public static final Indicator INDICATOR_111_C = new Indicator('c', "Ort",
		NR, "Location of meeting");

	public static final Indicator INDICATOR_111_D = new Indicator('d', "Datum",
		NR, "Date of meeting");

	public static final Indicator INDICATOR_111_N = new Indicator('n',
		"Zählung", R, "Number of meeting");

	public static final Indicator INDICATOR_130_A = new Indicator('a', 'a', "Titel "
	  + "eines Werks", NR, "Uniform title", 't');

	public static final Indicator INDICATOR_130_F = new Indicator('f',
		"Datum eines Werks", R, "Date of a work");

	public static final Indicator INDICATOR_130_H = new Indicator('h',
        "Inhaltstyp", NR, "Medium");

	public static final Indicator INDICATOR_130_L = new Indicator('l',
        "Sprache", NR, "Language of a work");

	public static final Indicator INDICATOR_130_M = new Indicator('m',
		"Besetzung im Musikbereich", R, "Medium of performance for music");

	public static final Indicator INDICATOR_130_N = new Indicator('n',
		"Zählung eines Werks, des Teils/der Abteilung eines Werks", R,
		"Number of part/section of a work");

	public static final Indicator INDICATOR_130_O = new Indicator('o',
		"Angabe des Musikarrangements", NR, "Arranged statement for music");

	public static final Indicator INDICATOR_130_P = new Indicator('p',
		"Titel des Teils/der Abteilung eines Werkes", R,
		"Name of part/section of a work", 'u');

	public static final Indicator INDICATOR_130_R = new Indicator('r',
		"Tonart", R, "Key for music");

	public static final Indicator INDICATOR_130_S = new Indicator('s',
		"Version", NR, "Version");

	public static final Indicator INDICATOR_150_A = new Indicator('a', "Sachbegriff", NR, "Topical "
	  + "term entry element");

	public static final Indicator INDICATOR_151_A = new Indicator('a', "Geografikum", NR, "Geographic "
	  + "name entry element");

	public static final Indicator INDICATOR_151_Z = new Indicator('z',
		"Geografische Unterteilung", R, "Geographic subdivision");

	public static final Indicator INDICATOR_548_A = new Indicator('a', "Beginn einer Zeitspanne", NR, "");

	public static final Indicator INDICATOR_548_B = new Indicator('b', "Ende einer Zeitspanne", NR, "");

	public static final Indicator INDICATOR_548_C = new Indicator('c', "Zeitpunkt", NR, "");

	public static final Indicator INDICATOR_548_D = new Indicator('d', "Ungefähre Zeitangabe", NR, "");

	public static final GNDPersonTag TAG_100 = new GNDPersonTag("100", "028A",
		"Person – Bevorzugter Name", NR, "100", "Heading - Personal Name");

	public static final DefaultGNDTag TAG_110 = new DefaultGNDTag("110",
		"029A", "Körperschaft – Bevorzugter Name", NR, "110",
		"Heading - Corporate Name");

	public static final DefaultGNDTag TAG_111 = new DefaultGNDTag("111",
		"030A", "Kongress – Bevorzugter Name", NR, "111",
		"Heading - Meeting Name");

	public static final DefaultGNDTag TAG_130 = new DefaultGNDTag("130",
		"022A", "Bevorzugter Titel des Werks", NR, "130",
		"Heading - Uniform Title");

	public static final DefaultGNDTag TAG_150 = new DefaultGNDTag("150",
		"041A", "Sachbegriff – Bevorzugte Benennung", NR, "150",
		"Heading - Topical Term");

	public static final DefaultGNDTag TAG_151 = new DefaultGNDTag("151",
		"065A", "Geografikum – Bevorzugter Name", NR, "151",
		"Heading - Geographic Name");

	public static final GNDPersonTag TAG_400 = new GNDPersonTag("400", "028@",
		"Person – Abweichender Name", R, "400",
		"See From Tracing - Personal Name");

	public static final DefaultGNDTag TAG_410 = new DefaultGNDTag("410",
		"029@", "Körperschaft – Abweichender Name", R, "410",
		"See From Tracing - Corporate Name");

	public static final DefaultGNDTag TAG_411 = new DefaultGNDTag("411",
		"030@", "Kongress – Abweichender Name", R, "411",
		"See From Tracing - Meeting Name");

	public static final DefaultGNDTag TAG_430 = new DefaultGNDTag("430",
		"022@", "Titel – Abweichender Name", R, "430",
		"See From Tracing - Uniform Title");

	public static final DefaultGNDTag TAG_450 = new DefaultGNDTag("450",
		"041@", "Sachbegriff – Abweichende Benennung", R, "450",
		"See From Tracing - Topical Term");

	public static final DefaultGNDTag TAG_451 = new DefaultGNDTag("451",
		"065@", "Geografikum – Abweichender Name", R, "451",
		"See From Tracing - Geographic Name");

	public static final DefaultGNDTag TAG_548 = new DefaultGNDTag("548",
		"060R", "Zeit – Beziehung", R, "548",
		"See Also From Tracing - Chronological Term");

	public static GNDTagDB getDB() {
		if(gndTagDB == null) {
			gndTagDB = new GNDTagDB();
		}
		return gndTagDB;
	}

	/**
	 * @param args nix.
	 */
	public static void main(final String[] args) {
		final de.dnb.gnd.parser.Record record = RecordUtils.readFromClip();
		System.out.println(record);
	}

	//@formatter:on

	/**
	 * Hilfsgröße, in der die (Relations-)Tags zwischen 500 und 600 gespeichert
	 * sind.
	 */
	private final HashSet<Tag> tagsRelated = new LinkedHashSet<>();

	private GNDTagDB() {
		GNDTag newTag;
	//@formatter:off

		newTag =
			new DefaultGNDTag(
					"001",
					"001A",
					"Quelle und Datum der Ersterfassung",
					NR,
					"008",
					"Date entered on file",
					null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', "Quelle und Datum (getrennt durch \":\")", NR, ""));

		newTag =
			new DefaultGNDTag(
					"002",
					"001B",
					"Quelle und Datum der letzten Änderung",
					NR,
					"005",
					"Date and Time of Latest Transaction",
					null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', "Quelle und Datum (getrennt durch \":\")", NR, ""));
		newTag.add(new Indicator('t', "Uhrzeit", NR, ""));

		newTag =
			new DefaultGNDTag(
					"003",
					"001D",
					"Quelle und Datum der letzten Statusvergabe",
					NR,
					"null",
					"",
					null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', " Quelle und Datum (getrennt durch \":\") ", NR, ""));

		newTag =
			new DefaultGNDTag(
					"005",
					"002@",
					"Satzart",
					NR,
					"008; 079",
					"Fixed-Length Data Elements",
					null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', "Satzart", NR, ""));

		newTag =
			new DefaultGNDTag(
					"006",
					"003U",
					"GND-Identifier",
					R,
					"024",
					"Other Standard Identifier");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "GND-URI", NR, ""));
		// später hinzugefügt:
		newTag.add(new Indicator('z', "nicht mehr gültige URI", R, ""));
		newTag.add(DOLLAR_V_NR);

		newTag = new EnumeratingTag("008", "004B", "Entitätencodierung", NR, "", "", "093");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, ""));

		newTag = new DefaultGNDTag("00A", "001X", "Internes Systemfeld", NR, "", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Internes Systemfeld", NR, ""));

    newTag = new DefaultGNDTag("00U", "001U", "Unicode-Kennzeichen", NR, "", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Unicode-Kennzeichen", NR, ""));

		newTag =
				new DefaultGNDTag(
						"009",
						"009B",
						"Hierarchie-Indikator",
						NR,
						"",
						"");
			addTag(newTag);
			newTag.addDefaultFirst(new Indicator('a', "Hierarchie-Indikator", NR, ""));

		newTag =
			new DefaultGNDTag("010", "008@", "Änderungscodierung", NR, "Leader", "", "682");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Code", NR, ""));

		newTag =
			new EnumeratingTag("011", "008A", "Teilbestandskennzeichen", NR, "", "", "098");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a','q', "Code", R, ""));

		newTag = new EnumeratingTag("012", "008B", "Nutzungskennzeichen", NR, "", "", "096");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, ""));

		newTag = new DefaultGNDTag("023", "007W", "SWD-Nr. im GKD-Satz", R, "null", "", null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', "SWD-Nummer", NR, ""));

		newTag =
			new DefaultGNDTag(
					"024",
					"006Y",
					"Sonstige Standardnummern",
					R,
					"024",
					"Other Standard Identifier",
					null);
		addTag(newTag);
		// $S darf nicht eingegeben werden (30.11.2012)
//		newTag.add(new Indicator('S', "Quelle/Code der Standardnummer", NR, ""));
		newTag.add(new Indicator(": ",   "", 'S', "Quelle/Code der Standardnummer", NR, ""));
		newTag.addDefaultFirst(new Indicator('0', "Nummer/Code", NR, ""));
		newTag.add(DOLLAR_V_NR);
		newTag.add(DOLLAR_5);

		newTag = new DefaultGNDTag("028", "007R", "GKD-Nr. im SWD-Satz", R, "null", "", null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', "GKD-Nummer", NR, ""));

		newTag = new DefaultGNDTag("030", "007C", "MACS-Identifikationsnummer", R, "null", "", null);
        addTag(newTag);
        newTag.addDefaultFirst(new Indicator('0', "MACS-Identifikationsnummer", NR, ""));

		newTag =
			new DefaultGNDTag(
					"034",
					"037H",
					"Geografische Koordinaten",
					R,
					"034",
					"Coded Cartographic Mathematical Data");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('A', "Indikator", NR, ""));
		newTag.add(new Indicator('d', "Koordinaten - westlichster Längengrad", NR, "Coordinates - westernmost longitude"));
		newTag.add(new Indicator('e', "Koordinaten - östlichster Längengrad", NR, "Coordinates - easternmost longitude"));
		newTag.add(new Indicator('f', "Koordinaten - nördlichster Längengrad", NR, "Coordinates - northernmost latitude"));
		newTag.add(new Indicator('g', "Koordinaten - südlichster Längengrad", NR, "Coordinates - southernmost latitude"));
		newTag.add(new Indicator("$j", "", 'j',
            "Deklination - nördliche Grenze", NR, ""));
		newTag.add(new Indicator("$k", "", 'k',
            "Deklination - südliche Grenze", NR, ""));
    newTag.add(new Indicator("$m", "", 'm',
        "Rektaszension - östliche Grenze", NR, ""));
    newTag.add(new Indicator("$n", "", 'n',
        "Rektaszension - westliche Grenze", NR, ""));
    newTag.add(new Indicator("$p", "", 'p', "Äquinoktium", NR, ""));
    newTag.add(new Indicator("$r", "", 'r', "Distanz zur Erde", NR, ""));
    newTag
        .add(new Indicator("$s", "", 's', "G-Ring Breitengrad", NR, ""));
    newTag
        .add(new Indicator("$t", "", 't', "G-Ring Längengrad", NR, ""));
    newTag.add(new Indicator("$x", "", 'x', "Anfangsdatum", NR, ""));
    newTag.add(new Indicator("$y", "", 'y', "Enddatum", NR, ""));
    newTag.add(new Indicator("$z", "", 'z',
        "Name des extraterrestrischen Körpers", NR, ""));
    newTag.add(new Indicator("$u", "", 'u', "URI der Web-Ressource", NR,
        ""));
    newTag.add(new Indicator("$S", "", 'S', "ISIL der Referenz-Datei",
        NR, ""));
    newTag.add(new Indicator("$0", "", '0',
        "Identifikationsnummer der Referenz-Datei", NR, ""));
    newTag.add(DOLLAR_2);
    newTag.add(new Indicator("$3", "", '3', "Koordinaten-Spezifikation",
        NR, ""));
		newTag.add(DOLLAR_V_NR);
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"035",
					"007K",
					"GND-Nummer",
					NR,
					"035",
					"System Control Number");
		addTag(newTag);
		// hier erzählt das Dokument "GND-Datenformat" Unfug:
		newTag.addDefaultFirst(new Indicator('a', "Präfix", NR, ""));
		newTag.add(new Indicator("/", "", '0', "GND-Nummer", NR, ""));
		newTag.add(DOLLAR_V_NR);

		newTag =
			new DefaultGNDTag(
					"039",
					"007N",
					"Alte Normnummer",
					R,
					"035",
					"System Control Number", "039");
		addTag(newTag);
		// wie oben!
		newTag.addDefaultFirst(new Indicator('a', "Präfix", NR, ""));
		newTag.add(new Indicator("/", "", '0', "Alte Normnummer", NR, ""));
		newTag.add(DOLLAR_V_NR);

		newTag =
				new DefaultGNDTag(
						"040",
						"010E",
						"Katalogisierungsquelle",
						NR,
						"040",
						"Cataloging Source");
			addTag(newTag);
			newTag.addDefaultFirst(new Indicator('b', "Katalogisierungssprache", NR, ""));
			newTag.add(new Indicator('e', "Beschreibungsfestlegungen", R, ""));
			newTag.add(new Indicator('f', "Schlagwort- oder Thesaurusfestlegungen", NR, ""));

		newTag =
			new EnumeratingTag(
					"043",
					"042B",
					"Ländercode nach ISO 3166",
					NR,
					"043",
					"Geographic Area Code");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', 'c', "Ländercode", R, "Geographic area code"));
		newTag.add(DOLLAR_5);

		newTag =
			new EnumeratingTag(
					"065",
					"042A",
					"GND Systematik",
					NR,
					"065",
					"Other Classification Number");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', "Notation", R, ""));
		newTag.add(DOLLAR_5);

		newTag =
		    new EnumeratingTag(
  		      "069",
  		      "037S",
  		      "Notation des fremden Thesaurus",
  		      NR,
		        "",
		        "");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', "Notation", R, ""));

		newTag =
			new DefaultGNDTag(
					"083",
					"037G",
					"DDC-Notation",
					R,
					"083",
					"Dewey Decimal Classificaton Number");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('c', 'a', "DDC-Notation", NR, "",'c'));
		newTag.add(new Indicator('d', "Determiniertheit", NR, ""));
		newTag.add(new Indicator('t', "Zeitstempel der Notationsvergabe", NR, ""));
		newTag.add(new Indicator('g', "Zeitstempel der letzten Überprüfung", NR, ""));
		newTag.add(DOLLAR_V_NR);

		newTag =
			new DefaultGNDTag("089", "037I", "Veraltete DDC-Notation", R, "089", "", null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('c', 'a', "DDC-Notation", NR, "", 'c'));
		newTag.add(new Indicator('d', "Determiniertheit", NR, ""));
		newTag.add(new Indicator('t', "Zeitstempel für „gültig seit“", NR, ""));
		newTag.add(new Indicator('g', "Zeitstempel für „gültig bis“", NR, ""));
		newTag.add(DOLLAR_V_NR);

		//------- Bibliotheksdatei
		newTag =
	      new DefaultGNDTag("092", "008H", "ZDB-Bibliotheks-kennung (BIK)", NR, "092", "", null);
	    addTag(newTag);
	  newTag.addDefaultFirst(new Indicator('a', "ZDB-Bibliotheks-kennung (BIK)", NR, ""));
	  newTag.add(new Indicator('b', "ID-Nummer der DBS", NR, ""));
	  newTag.add(new Indicator('c', "Regionales Bibliotheks-kennzeichen", NR, ""));
	  newTag.add(new Indicator('d', "Sigel", NR, ""));
	  newTag.add(new Indicator('e', "ISIL", NR, ""));
	  newTag.add(new Indicator('f', "Bibliotheks-ID der EZB", NR, ""));
	  newTag.add(new Indicator('g', "OCLC Registry ID", NR, ""));
	  newTag.add(new Indicator('h', "Weitere ISIL", NR, ""));


		newTag =
			TAG_100;
		addTag(newTag);
		newTag.add(INDICATOR_100_P);
		newTag.addDefaultFirst(INDICATOR_100_A);
		newTag.add(INDICATOR_100_D);
		newTag.add(INDICATOR_100_C);
		newTag.add(INDICATOR_100_N);
		newTag.add(INDICATOR_100_L);
		newTag.add(DOLLAR_X);
		newTag.add(DOLLAR_G);
		newTag.add(DOLLAR_V_R);
		newTag.add(new Indicator('E', "Geburtsjahr", NR, ""));
    newTag.add(new Indicator('G', "Todesjahr", NR, ""));

		newTag =
			TAG_110;
		addTag(newTag);
		newTag.addDefaultFirst(INDICATOR_110_A);
		newTag.add(INDICATOR_110_B);
		newTag.add(INDICATOR_110_N);
		newTag.add(DOLLAR_X);
		newTag.add(DOLLAR_G);
		newTag.add(DOLLAR_V_R);

		newTag =
			TAG_111;
		addTag(newTag);
		newTag.addDefaultFirst(INDICATOR_111_A);
		newTag.add(INDICATOR_111_B);
		newTag.add(INDICATOR_111_N);
		newTag.add(INDICATOR_111_D);
		newTag.add(INDICATOR_111_C);
		newTag.add(DOLLAR_X);
		newTag.add(DOLLAR_G);
		newTag.add(DOLLAR_V_R);

		newTag =
			TAG_130;
		addTag(newTag);
		newTag.addDefaultFirst(INDICATOR_130_A);
		newTag.add(INDICATOR_130_F);
		newTag.add(DOLLAR_G);
		newTag.add(INDICATOR_130_H);
		newTag.add(INDICATOR_130_L);
		newTag.add(INDICATOR_130_M);
		newTag.add(INDICATOR_130_N);
		newTag.add(INDICATOR_130_O);
		newTag.add(INDICATOR_130_P);
		newTag.add(INDICATOR_130_R);
		newTag.add(INDICATOR_130_S);
		newTag.add(DOLLAR_X);
		newTag.add(DOLLAR_V_R);

		newTag =
			new DefaultGNDTag(
					"148",
					"060A",
					"Zeit  - wird nicht verwendet",
					NR,
					"148",
					"Heading - Chronological Term");

		newTag =
			TAG_150;
		addTag(newTag);
		newTag.addDefaultFirst(INDICATOR_150_A);
		newTag.add(DOLLAR_X);
		newTag.add(DOLLAR_G);
		newTag.add(DOLLAR_V_R);

		newTag =
			TAG_151;
		addTag(newTag);
		newTag.addDefaultFirst(INDICATOR_151_A);
		newTag.add(DOLLAR_X);
		newTag.add(INDICATOR_151_Z);
		newTag.add(DOLLAR_G);
		newTag.add(DOLLAR_V_R);

		newTag =
				TAG_153;
			addTag(newTag);
			newTag.addDefaultFirst(INDICATOR_153_A);
			newTag.add(INDICATOR_153_B);
			newTag.add(new Indicator('h', "Übergeordnete Klassenbenennung", NR, "Caption hierarchy"));
			newTag.add(INDICATOR_153_J);
			newTag.add(new Indicator('x', "Notation in Sortierform", NR, ""));
			newTag.add(new Indicator('z', "Zeitdefinition für Epochen", NR, ""));
			newTag.add(DOLLAR_V_R);


		newTag =
			new DefaultGNDTag(
					"169",
					"038L",
					"Markierung für das Match-und-Merge- Verfahren",
					NR,
					"null",
					"");
		addTag(newTag);
		// "GND-Datenformat" ist hier nicht korrekt.
		newTag.add(new Indicator('a', "Angabe des Match-und- Merge-Kontingents", NR, ""));
		newTag.add(new Indicator('b', "Status der Prüfung", NR, ""));
		newTag.add(new Indicator('c', "Kommentar", NR, ""));
		newTag.add(new Indicator('x', "ermittelter Matchwert", NR, ""));
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);

		newTag =
          new DefaultGNDTag(
            "190",
            "039A",
            "Hauptschlagwort der GND",
            R,
            "null",
            "");
    addTag(newTag);
    newTag.add(TagDB.DOLLAR_9);
    newTag.add(TagDB.DOLLAR_8);
    newTag.add(new Indicator('c', "Schlagwort mit Indikator \"c\"", NR, ""));
    newTag.add(new Indicator('g', "Schlagwort mit Indikator \"g\"", NR, ""));
    newTag.add(new Indicator('k', "Schlagwort mit Indikator \"k\"", NR, ""));
    newTag.add(new Indicator('p', "Schlagwort mit Indikator \"p\"", NR, ""));
    newTag.add(new Indicator('s', "Schlagwort mit Indikator \"s\"", NR, ""));
    newTag.add(new Indicator('t', "Schlagwort mit Indikator \"t\"", NR, ""));
    newTag.add(new Indicator('5', "Normnummer (bei maschineller Übernahme; "
                                    + "temporäres Umfeld)", NR, ""));

		newTag =
			new DefaultGNDTag(
					"260",
					"041O",
					"Zu verknüpfende Schlagwörter in Hinweissätzen",
					R,
					"260",
					"Complex See Reference");
		addTag(newTag);
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);
		newTag.addDefaultFirst(new Indicator('a', "Form-, Zeitschlagwort", NR, ""));
		newTag.add(DOLLAR_V_NR);

		newTag =
			new DefaultGNDTag("336", "032L", "Inhaltstyp", R, "336", "Content Type", null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a',
        "Term", NR, "Content type term"));
		newTag.add(DOLLAR_b);
		newTag.add(DOLLAR_5);

		newTag = new DefaultGNDTag("337", "032M", "Medientyp", R, "339", "", null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('b', "Code", NR, ""));
		newTag.add(DOLLAR_5);

		newTag = new DefaultGNDTag("338", "032N", "Datenträgertyp", R, "339", "", null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('b', "Code", NR, ""));
		newTag.add(DOLLAR_5);

	//------- Bibliotheksdatei

		newTag =
	      new DefaultGNDTag("371", "032P", "Adresse, Zugang", R, "371", "", null);
	  addTag(newTag);
	  newTag.addDefaultFirst(new Indicator('a',"Straße und Hausnummer ", NR, ""));
	  newTag.add(new Indicator('b', "Ort", NR, ""));
	  newTag.add(new Indicator('d', "Ländercode (ISO 3166)", NR, ""));
	  newTag.add(new Indicator('e', "Postleitzahl", NR, ""));
	  newTag.add(new Indicator('f', "Bundesland / Provinz", NR, ""));
	  newTag.add(new Indicator('g', "Postfach", NR, ""));
	  newTag.add(new Indicator('h', "Gebäude / Gebäudeteil / Zustellanweisung", NR, ""));
	  newTag.add(new Indicator('i', "Öffnungszeiten (allgemein)", NR, ""));
	  newTag.add(new Indicator('j', "Öffnungszeiten (Anmerkungen)", NR, ""));
	  newTag.add(new Indicator('k', "Geografische Länge", NR, ""));
	  newTag.add(new Indicator('l', "Geografische Breite", NR, ""));
	  newTag.add(new Indicator('n', "Statistische Kennzahl des Ortes", NR, ""));
	  newTag.add(new Indicator('o', "Informationen für Menschen mit Behinderungen", NR, ""));
	  newTag.add(new Indicator('p', "Adresse öffentlich anzeigen?", NR, ""));
	  newTag.add(new Indicator('z', "Bemerkungen zur Anschrift allgemein", NR, ""));
	  newTag.add(new Indicator('2', "Code Adresse", NR, ""));
	  newTag.add(new Indicator('3', "Bezeichnung Adresse", NR, ""));


		newTag =
			new DefaultGNDTag(
					"372",
					"032Q",
					"Tätigkeitsbereich",
					R,
					"372",
					"Field of Activity",
					null);
		addTag(newTag);
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);
		newTag.addDefaultFirst(new Indicator('a', "Sachbegriff", NR, ""));
		newTag.add(new Indicator('w', "Quelle der Information (ggfs. URI)", R, ""));
		newTag.add(new Indicator('Z', "Zeitl. Gültigkeit der Beziehung", NR, ""));
		newTag.add(DOLLAR_V_NR);
		newTag.add(DOLLAR_5);

		newTag =
			new EnumeratingTag("375", "032T", "Geschlechtsangabe", NR, "375", "Gender");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "",'a', "Geschlecht", R, "Gender"));
		newTag.add(DOLLAR_V_NR);
		newTag.add(DOLLAR_5);

		newTag =
			new EnumeratingTag(
					"377",
					"042C",
					"Sprachencode nach ISO 639-2/B",
					NR,
					"377",
					"Associated Language");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, "Language code"));
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag("380", "032W", "Form des Werks", R, "380", "Form of work");
		addTag(newTag);
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);
		newTag.addDefaultFirst(new Indicator('a', "Form des Werks", NR, ""));
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"382",
					"032X",
					"Besetzung im Musikbereich",
					R,
					"382",
					"Medium of Performance");
		addTag(newTag);
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);
		newTag.addDefaultFirst(new Indicator('a', "Darstellungsmedium (Term)", NR, "Medium of performance"));
		newTag.add(new Indicator('e', "Anzahl der Ensembles vom gleichen Typ", NR, "Number of ensembles of the same type"));
		newTag.add(new Indicator('n', "Besetzungsstärke", NR, "Number of performers of the same medium"));
		newTag.add(new Indicator('p', "Alternative Besetzung oder Doubling "
				+ "instruments oder Ad-libitum-Besetzungen ", NR, "Alternative medium of performance"));
		newTag.add(new Indicator('s', "Gesamtbesetzungsstärke", NR, "Total number of performers"));
		newTag.add(new Indicator('t', "Gesamtanzahl der Ensembles", NR, "Total number of ensembles"));
		newTag.add(DOLLAR_V_NR);
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"383",
					"032Y",
					"Numerische Kennzeichnung eines Musikwerks",
					R,
					"383",
					"Numeric Designation of Musical Work");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Fortlaufende Zählung", R, "Serial number"));
		newTag.add(new Indicator('b', "Opus-Zählung", R, "Opus number"));
		newTag.add(new Indicator('c', "Zählung eines Werkverzeichnisses", R, "Thematic index number"));
		newTag.add(DOLLAR_5);

		newTag = new DefaultGNDTag("384", "032Z", "Tonart", NR, "384", "Key");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Tonart des Werks", NR, "Key"));
		newTag.add(new Indicator('b', "Tonart der Fassung", NR, ""));
		newTag.add(DOLLAR_5);

		newTag =
			TAG_400;
		addTag(newTag);
		newTag =
			TAG_410;
		addTag(newTag);
		newTag =
			TAG_411;
		addTag(newTag);
		newTag =
			TAG_430;
		addTag(newTag);
		// wird nicht verwendet:
		newTag =
			new DefaultGNDTag(
					"448",
					"060@",
					"Zeit",
					Repeatability.UNKNOWN,
					"448",
					"See From Tracing - Chronological Term");

		newTag = TAG_450;
		addTag(newTag);
		newTag = TAG_451;
		addTag(newTag);

		newTag = TAG_453;
			addTag(newTag);
			newTag.add(new Indicator('S', "Umschreibungstyp", NR, ""));
			newTag.add(new Indicator('a', "Synonyme Umschreibung für die Notation", NR, ""));
			newTag.add(new Indicator('x', "Registerbegriff, weitere Glieder", R, "General subdivision"));
			newTag.add(DOLLAR_9);
			newTag.add(DOLLAR_8);
			newTag.add(DOLLAR_5);
			newTag.add(DOLLAR_V_NR);
			newTag.add(DOLLAR_L);

		newTag =
			new GNDPersonTag(
					"500",
					"028R",
					"Person – Beziehung",
					R,
					"500",
					"See Also From Tracing - Personal Name");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"510",
					"029R",
					"Körperschaft – Beziehung",
					R,
					"510",
					"See Also From Tracing - Corporate Name");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"511",
					"030R",
					"Kongress – Beziehung",
					R,
					"511",
					"See Also From Tracing - Meeting Name");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"530",
					"022R",
					"Einheitstitel – Beziehung",
					R,
					"530",
					"See Also From Tracing - Uniform Title");
		addTag(newTag);
		newTag =
			TAG_548;
		addTag(newTag);
		newTag.addDefaultFirst(INDICATOR_548_A);
		newTag.add(INDICATOR_548_B);
		newTag.add(INDICATOR_548_C);
		newTag.add(INDICATOR_548_D);
		newTag.add(DOLLAR_V_R);
		newTag.add(new Indicator('X', "Anzeige-Relevanz", NR, ""));
		newTag.add(new Indicator('Y', "MO-Relevanz", R, ""));
		newTag.add(INDICATOR_Z);
		newTag.add(DOLLAR_4);
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"550",
					"041R",
					"Sachbegriff – Beziehung",
					R,
					"550",
					"See Also From Tracing - Topical Term");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"551",
					"065R",
					"Geografikum – Beziehung",
					R,
					"551",
					"See Also From Tracing - Geographic Name");
		addTag(newTag);

		newTag = TAG_553;
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Relationierte Notation (Freitext)", NR, "Classification number"));
		newTag.add(new Indicator('b', "einleitender Text", NR, ""));
		newTag.add(DOLLAR_9);
		newTag.add(DOLLAR_8);
		newTag.add(DOLLAR_4);
		newTag.add(DOLLAR_V_NR);


		newTag =
			new DefaultGNDTag(
					"667",
					"050C",
					"Redaktionelle Bemerkungen",
					R,
					"667",
					"Nonpublic General Note");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Freitext", NR, "Nonpublic general note"));
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"670",
					"050E",
					"Quellenangaben",
					R,
					"670",
					"Source Data Found");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Quelle", NR, "Source citation"));
		newTag.add(new Indicator('b', "Erläuternder Text", NR, "Information found"));
		newTag.add(DOLLAR_U_SM_R);
		newTag.add(DOLLAR_5);

		newTag = new DefaultGNDTag("672", "046G", "Titelangaben", R, "692", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('a', "Titel", NR, ""));
    newTag.add(new Indicator('b', "Zusätze zum Titel", NR, ""));
    newTag.add(new Indicator('f', "Jahr", NR, ""));
    newTag.add(new Indicator('w', "Identifikationsnummer zum Bibliografischen Datensatz", R, ""));
    newTag.add(new Indicator('0', "Verknüpfungsnummer zum Normdatensatz oder Standardnummer", R, ""));
    newTag.add(DOLLAR_5);

		newTag =
			new EnumeratingTag(
					"675",
					"050F",
					"Negativ eingesehene Quellen",
					NR,
					"675",
					"Source Data Not Found");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator(";", "", 'a', "Quelle", R, "Source citation"));
		newTag.add(DOLLAR_5);

		newTag = new DefaultGNDTag("677", "050H", "Definitionen", R, "677", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('a', "Definition", NR, ""));
    newTag.add(DOLLAR_U_SM_R);
    newTag.add(DOLLAR_V_NR);
    newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"678",
					"050G",
					"Biografische, historische und andere Angaben",
					R,
					"678",
					"Biographical or Historical Data");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Kurzer Text", R, "Biographical or historical data"));
		newTag.add(new Indicator('b', "Erläuternder Text", NR, "Expansion"));
		newTag.add(DOLLAR_U_SM_R);
		newTag.add(DOLLAR_5);



		newTag =
			new DefaultGNDTag(
					"680",
					"050D",
					"Benutzungshinweise",
					R,
					"680",
					"Public General Note");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Benutzungshinweis", NR, ""));
		newTag.add(DOLLAR_5);

		newTag =
			new DefaultGNDTag(
					"682",
					"039I",
					"Nummer und bevorzugter Name bzw. bevorzugte Benennung des Zielsatzes bei Umlenkung von Datensätzen",
					NR,
					"682",
					"Deleted Heading Information");
		addTag(newTag);
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);
		newTag.add(DOLLAR_V_NR);

		newTag =
			new DefaultGNDTag(
					"689",
					"039G",
					"Nummer und bevorzugter Name bzw. bevorzugte Benennung des Zielsatzes bei Aufspaltung von Datensätzen",
					NR,
					"682",
					"Deleted Heading Information");
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Code für Art der Aufspaltung", NR, ""));
		newTag.add(TagDB.DOLLAR_9);
		newTag.add(TagDB.DOLLAR_8);
		newTag.add(DOLLAR_V_NR);

		newTag =
			new GNDPersonTag(
					"700",
					"028P",
					"Person – Bevorzugter Name in einem anderen Datenbestand",
					R,
					"700",
					"Established Heading Linking Entry - Personal Name");
		addTag(newTag);
		newTag.add(new Indicator('t', "Titel", NR, ""));

		newTag =
			new DefaultGNDTag(
					"710",
					"029P",
					"Körperschaft – Bevorzugter Name in einem anderen Datenbestand",
					R,
					"710",
					"Established Heading Linking Entry - Corporate Name");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"711",
					"030P",
					"Kongress – Bevorzugter Name in einem anderen Datenbestand",
					R,
					"711",
					"Established Heading Linking Entry - Meeting Name");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"730",
					"022P",
					"Einheitstitel – Bevorzugter Name in einem anderen Datenbestand",
					R,
					"730",
					"Established Heading Linking Entry - Uniform Title");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"750",
					"041P",
					"Sachbegriff – Bevorzugte Benennung in einem anderen Datenbestand",
					R,
					"750",
					"Established Heading Linking Entry - Topical Term");
		addTag(newTag);
		newTag =
			new DefaultGNDTag(
					"751",
					"065P",
					"Geografikum – Bevorzugter Name in einem anderen Datenbestand",
					R,
					"751",
					"Established Heading Linking Entry - Geographic Name");
		addTag(newTag);

		newTag =
				new DefaultGNDTag(
						"753",
						"044H",
						"Klassifikation aus anderen Klassifikationssystemen",
						R,
						"753",
						"");
		newTag.addInherited(getPica3("453"));
		addTag(newTag);

		newTag =
			new DefaultGNDTag(
					"797",
					"003@",
					"Interne Identifikationsnummer PPN",
					NR,
					"001; 003",
					"Control Number (Identifier)",
					null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('0', "Interne Identifikationsnummer PPN", NR, ""));



	//------- Bibliotheksdatei
		newTag =
	      new DefaultGNDTag(
	          "802",
	          "035B",
	          "Kommunikation",
	          R,
	          "802",
	          "",
	          null);
	    addTag(newTag);
	    newTag.addDefaultFirst(new Indicator('a', "Code Kommunikationsbereich", NR, ""));
	    newTag.add(new Indicator('b', "Bezeichnung Kommunikationsbereich", NR, ""));
	    newTag.add(new Indicator('c', "Kommunikation öffentlich anzeigen?", NR, ""));
	    newTag.add(new Indicator('d', "Telefon Ländervorwahl", NR, ""));
	    newTag.add(new Indicator('e', "Telefon Ortsvorwahl", NR, ""));
	    newTag.add(new Indicator('f', "Telefonnummer, ggf. mit Durchwahl", NR, ""));
	    newTag.add(new Indicator('g', "Telefax Ländervorwahl", NR, ""));
	    newTag.add(new Indicator('h', "Telefax Ortsvorwahl", NR, ""));
	    newTag.add(new Indicator('i', "Telefaxnummer", NR, ""));
	    newTag.add(new Indicator('j', "Telex", NR, ""));
	    newTag.add(new Indicator('k', "E-Mail-Adresse", NR, ""));
	    newTag.add(new Indicator('l', "Bemerkungen zu den Kommunikationsverbindungen", NR, ""));

	    newTag =
	        new DefaultGNDTag(
	            "803",
	            "035G",
	            "Schlagwörter der Sammelschwerpunkte",
	            NR,
	            "803",
	            "",
	            null);
	      addTag(newTag);
	      newTag.addDefaultFirst(new Indicator('a', "Schlagwörter der Sammelschwerpunkte", R, ""));

	      newTag =
	          new DefaultGNDTag(
	              "804",
	              "035H",
	              "Betreute Sondersammelgebiete der DFG",
	              NR,
	              "804",
	              "",
	              null);
	      addTag(newTag);
	      newTag.addDefaultFirst(new Indicator('a', "Betreute Sondersammelgebiete der DFG", R, ""));

	      newTag =
	          new DefaultGNDTag(
	              "805",
	              "035E",
	              "Status der Adresse in der ZDB-Bibliotheksdatei",
	              NR,
	              "805",
	              "",
	              null);
	        addTag(newTag);
	        newTag.addDefaultFirst(new Indicator('a', "Status der Adresse in der ZDB-Bibliotheksdatei", NR, ""));
	        newTag.add(new Indicator('b', "Abrufzeichen (z.B. zur Datenweitergabe)", NR, ""));
	        newTag.add(new Indicator('c', "ZDB-Melderkennung", NR, ""));
	        newTag.add(new Indicator('d', "Lieferkategorie Verbundsystem", R, ""));
	        newTag.add(new Indicator('e', "Importkennzeichen der ZDB", NR, ""));
	        newTag.add(new Indicator('f', "Typ der Einrichtung", NR, ""));
	        newTag.add(new Indicator('g', "Unterhaltsträger", NR, ""));
	        newTag.add(new Indicator('h', "Bestandsgrößenklasse", NR, ""));
	        newTag.add(new Indicator('i', "Benutzungsbeschränkungen", NR, ""));
	        newTag.add(new Indicator('j', "ILN anderer Systeme", R, ""));
	        newTag.add(new Indicator('k', "früheres Sigel", NR, ""));
	        newTag.add(new Indicator('l', "Lizenzinformation Bestandsdaten ZDB", NR, ""));

	        newTag =
	            new DefaultGNDTag(
	                "806",
	                "035D",
	                "übernommene Einrichtungen",
	                R,
	                "806",
	                "",
	                null);
	        addTag(newTag);
	        newTag.addDefaultFirst(new Indicator('a', "ehem. Sigel der übernommenen Einrichtung", NR, ""));
	        newTag.add(new Indicator('b', "ehem. ISIL der übernommenen Einrichtung", NR, ""));


	        newTag =
              new DefaultGNDTag(
                  "807",
                  "035I",
                  "Codes und Informationen für die Fernleihe",
                  NR,
                  "807",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Leihverkehrsregion", NR, ""));
          newTag.add(new Indicator('b', "Leihverkehrsart", NR, ""));
          newTag.add(new Indicator('c', "Zuständiges Verbundsystem (AGV)", NR, ""));
          newTag.add(new Indicator('d', "Weitere(s) Verbundsystem(e) - nicht in AGV", NR, ""));
          newTag.add(new Indicator('e', "Fernleihindikator", NR, ""));
          newTag.add(new Indicator('f', "Fernleihindikator Ausland", NR, ""));
          newTag.add(new Indicator('g', "Typ Online-Fernleihe", NR, ""));

          newTag =
              new DefaultGNDTag(
                  "808",
                  "035J",
                  "Allgemeine Anmerkungen zur Fernleihe",
                  NR,
                  "808",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Allgemeine Anmerkungen zur Fernleihe (öffentlich)", NR, ""));
          newTag.add(new Indicator('b', "Allgemeine Anmerkungen zur Fernleihe (intern)", NR, ""));


          newTag =
              new DefaultGNDTag(
                  "809",
                  "035K",
                  "Liefersysteme, Altbestand und Codes",
                  NR,
                  "809",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Dokumentenliefersystem", NR, ""));
          newTag.add(new Indicator('b', "Teilnahme am Altbestandszertifikat", NR, ""));
          newTag.add(new Indicator('c', "Altbestandsgrenze", NR, ""));
          newTag.add(new Indicator('d', "URL Altbestandausstattung", NR, ""));
          newTag.add(new Indicator('e', "Art das Lokalsystems (Protokoll FL-System)", NR, ""));
          newTag.add(new Indicator('f', "Automatisierte FL (Online FL regional)", NR, ""));
          newTag.add(new Indicator('g', "Bestandslücken Verbund-DB", NR, ""));
          newTag.add(new Indicator('h', "Materialcodes ", NR, ""));

          newTag =
              new DefaultGNDTag(
                  "810",
                  "035L",
                  "Fristen und besondere Kontakt-E-Mails für die Fernleihe",
                  R,
                  "810",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Code für Inhalt", NR, ""));
          newTag.add(new Indicator('b', "Wird Service angeboten?", NR, ""));
          newTag.add(new Indicator('c', "Fristlänge", NR, ""));
          newTag.add(new Indicator('d', "Kontakt-E-Mail", NR, ""));


          newTag =
              new DefaultGNDTag(
                  "811",
                  "035M",
                  "Weitere Angaben zur Fernleihe",
                  NR,
                  "811",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Leihfristen", NR, ""));
          newTag.add(new Indicator('b', "Lieferformen", NR, ""));
          newTag.add(new Indicator('c', "Preis Kopie", NR, ""));
          newTag.add(new Indicator('d', "ISIL FL-Abteilung ", NR, ""));
          newTag.add(new Indicator('e', "ISIL Leitbibliothek ", NR, ""));
          newTag.add(new Indicator('f', "Zuständiger ZK", NR, ""));

          newTag =
              new DefaultGNDTag(
                  "812",
                  "035N",
                  "URL-Templates für die Fernleihe",
                  R,
                  "812",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Art des Templates  / Code", NR, ""));
          newTag.add(new Indicator('b', "URL-Template / String mit Parametersyntax", NR, ""));

          newTag =
              new DefaultGNDTag(
                  "813",
                  "035O",
                  "Büchertransport",
                  R,
                  "813",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Typ der Post-/Frachtgutverbindung im Leihverkehr", NR, ""));
          newTag.add(new Indicator('b', "Art der Sendungen für diese Post-/Frachtgutverbindung", NR, ""));
          newTag.add(new Indicator('c', "Bemerkungen zu dieser Post-/Frachtgutverbindung", NR, ""));
          newTag.add(new Indicator('d', "Kontakt E-Mail Transport", NR, ""));


          newTag =
              new DefaultGNDTag(
                  "814",
                  "035P",
                  "Spezielle Felder für Produktsätze (Pakete)",
                  NR,
                  "814",
                  "",
                  null);
          addTag(newTag);
          newTag.addDefaultFirst(new Indicator('a', "Art des Pakets", NR, ""));
          newTag.add(new Indicator('b', "Logodatei", NR, ""));
          newTag.add(new Indicator('c', "Verhandlungsrunde", NR, ""));
          newTag.add(new Indicator('d', "Zeitschriften enthalten?", NR, ""));
          newTag.add(new Indicator('e', "Enthaltene Titel (Anbieter)", NR, ""));
          newTag.add(new Indicator('f', "Enthaltene Titel (ZDB)", NR, ""));
          newTag.add(new Indicator('g', "Anbieter", NR, ""));
          newTag.add(new Indicator('h', "URL im CMS \"Datenbankinfo\"", NR, ""));
          newTag.add(new Indicator('i', "Finanzierungsmodell", NR, ""));

          newTag =
              new DefaultGNDTag(
                  "856",
                  "009Q",
                  "Endnutzer Service-URLs",
                  R,
                  "856",
                  "",
                  null);
          addTag(newTag);
          newTag.add(new Indicator('u', "URL ", NR, ""));
          newTag.add(new Indicator('x', "Art der URL / Text", NR, ""));
          newTag.add(new Indicator('z', "Art der URL / Code", NR, ""));


		newTag = TAG_900;
		addTag(newTag);
		newTag.add(new Indicator('a', "Text", NR, ""));
		newTag.add(DOLLAR_5);

		newTag = new GNDTextTag(
						"901",
						"047A/01",
						"Mailbox",
						R,
						"912",
						"",
						"901");
		addTag(newTag);
		newTag.add(new Indicator('z', "Datum", NR, ""));
		newTag.add(new Indicator('b', "Absender/Empfänger", NR, ""));
		newTag.add(new Indicator('a', "Freitext", NR, ""));

		newTag =
			new DefaultGNDTag(
					"903",
					"047A/03",
					"Katalogisierende Institution",
					R,
					"040",
					"Cataloging Source",
					"903");
		addTag(newTag);
		newTag.add(new Indicator('e', 'a', "ISIL des Urhebers", NR, "", 'e'));
		newTag.add(new Indicator('r', "ISIL der Verbundredaktion", NR, ""));

		newTag =
			new DefaultGNDTag(
					"913",
					"047C",
					"Alte Ansetzungsform",
					R,
					"913",
					"",
					"990");
		addTag(newTag);
		newTag.add(new Indicator('S', "Normdatei (swd, gkd, pnd, est)", NR, ""));
		newTag.add(new Indicator('i', "Indikator", NR, ""));
		newTag.add(new Indicator('a', "Ansetzungsform", NR, ""));
		newTag.add(new Indicator('0', "Normnummer", NR, ""));

		newTag =
			new DefaultGNDTag(
					"980",
					"070A",
					"Sortiername im Deutschen Exilarchiv",
					NR,
					"null",
					"",
					null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Körperschaftsname, Gebietskörperschaftsname, Kongressname", NR, ""));
		newTag.add(new Indicator('n', "Zählung", R, ""));
		newTag.add(new Indicator('d', "Datum", NR, ""));
		newTag.add(new Indicator('c', "Ort", NR, ""));
		newTag.add(new Indicator('b', "Untergeordnete Körperschaft, untergeordnete Einheit eines Kongresses", R, ""));
		newTag.add(DOLLAR_G);
		newTag.add(DOLLAR_4);
		newTag.add(DOLLAR_5);
		newTag.add(DOLLAR_V_R);

		newTag = new DefaultGNDTag(
            "981",
            "070A/01",
            "Relation",
            R,
            "null",
            "",
            null);
		addTag(newTag);
		newTag.addDefaultFirst(new Indicator('a', "Angabe der Relation", NR, ""));
		newTag.add(DOLLAR_2);

		newTag = new DefaultGNDTag(
      "982",
      "070A/02",
      "Lokale Identifier (permanent)",
      R,
      "null",
      "",
      null);
		addTag(newTag);
		newTag.add(new Indicator('S', "ISIL der Referenzdatei", NR, ""));
		newTag.add(new Indicator('0', "Identifier in der Referenzdatei", NR, ""));


    newTag = new DefaultGNDTag(
      "983",
      "070A/03",
      "Lokale Identifier (temporär)",
      R,
      "null",
      "",
      null);
    addTag(newTag);
    newTag.add(new Indicator('S', "ISIL der Referenzdatei", NR, ""));
    newTag.add(new Indicator('0', "Identifier in der Referenzdatei", NR, ""));

		// wiederholbar!
		newTag = new DefaultGNDTag(
						"999",
						"070B/09",
						"Fehlermeldungen",
						R,
						"null",
						"",
						null);
		addTag(newTag);
		newTag.add(new Indicator('a', "Bezug zu der gesuchten Relation", NR, ""));
		newTag.add(new Indicator('b', "Fehlermeldung", NR, ""));

		connect4XX();
		connect5XX();
		connect7XX();

	}

	protected  void connect4XX() {
		final GNDTag tag4XX = new DefaultGNDTag("4XX", "XXX", "Dummy", R, null, null);
		tag4XX.add(DOLLAR_4);
		tag4XX.add(DOLLAR_5);
		tag4XX.add(DOLLAR_L);
		tag4XX.add(DOLLAR_T);
		tag4XX.add(DOLLAR_U_GR);

		final Collection<Tag> tags4XX = getTagsBetween("400", "451");

		for (final Tag tag : tags4XX) {
			final String tagStr = "1" + tag.pica3.substring(1);
			final Tag tag1XX = getPica3(tagStr);
			tag.addInherited(tag1XX);
			tag.addInherited(tag4XX);
		}

	}

	protected  void connect5XX() {
		final GNDTag tag5XX = new DefaultGNDTag("5XX", "XXX", "Dummy", R, null, null);
		tag5XX.add(TagDB.DOLLAR_9);
		tag5XX.add(TagDB.DOLLAR_8);
		tag5XX.add(DOLLAR_4);
		tag5XX.add(DOLLAR_5);
		// DOLLAR_V_R eventuell doppelt:
		tag5XX.add(DOLLAR_V_R);
		tag5XX.add(new Indicator('X', "Anzeige-Relevanz", NR, ""));
		tag5XX.add(new Indicator('Y', "MO-Relevanz", R, ""));
		tag5XX.add(INDICATOR_Z);

		final Collection<Tag> tags5XX = getTagsBetween("500", "551");

		for (final Tag tag5xx : tags5XX) {
			final String tagStr = "1" + tag5xx.pica3.substring(1);
			final GNDTag tag1XX = (GNDTag) getPica3(tagStr);
			tag5xx.addInherited(tag5XX);
			// um 548 auszuschließen:
			if (tag1XX != null) {
				tag5xx.related = tag1XX;
				tagsRelated.add(tag5xx);
			}
		}
	}

	protected  void connect7XX() {
		final GNDTag tag7XX = new DefaultGNDTag("7XX", "XXX", "Dummy", R, null, null);
		tag7XX.add(DOLLAR_T_GROSS);
		tag7XX.add(DOLLAR_U_SM_R);
		tag7XX.add(new Indicator('S', "ISIL der Referenz-Normdatei", NR, ""));
		tag7XX.add(new Indicator('0', "Identifikationsnummer", NR, ""));
		tag7XX.add(DOLLAR_2);
		tag7XX.add(DOLLAR_4);
		tag7XX.add(DOLLAR_5);

		// eventuell doppelt:
		tag7XX.add(DOLLAR_V_R);
		tag7XX.add(DOLLAR_L);
		tag7XX.add(DOLLAR_T);
		tag7XX.add(DOLLAR_U_GR);

		final Collection<Tag> tags7XX = getTagsBetween("700", "751");

		for (final Tag gNDTag : tags7XX) {
			final String tagStr = "1" + gNDTag.pica3.substring(1);
			final Tag tag1XX = getPica3(tagStr);
			// um 797 auszuschliessen:
			if (tag1XX != null) {
				gNDTag.addInherited(tag1XX);
				gNDTag.addInherited(tag7XX);
			}
		}

	}

	/**
	 * 5XX.
	 * @return Alle außer 548.
	 */
	public Collection<Tag> getRelatedTag5XX() {
		return tagsRelated;
	}

	/*
	 * Folgende Pica+-Tags werden nicht erkannt, weil es keine
	 * Pica3-Tags dazu gibt. Eventuell muss man da noch etwas drehen:
	 *
	 * 001@		Suppliercode, Userbits
	 * 001A		Erfassungskennung
	 * 001B 	Änderungskennung
	 * 001D 	Statusänderung
	 * 001E		Kennzeichnung logisch gelöscht
	 * 001Q		internes Feld
	 * 001U		Kennzeichnung UTF8
	 * 001X		Owner main extension
	 * 042@   Angaben zum (Offline)-Datenimport
	 *
	 *  (Quelle: https://wiki.d-nb.de/display/ILTIS/GND-Berechtigungen)
	 */

	/**
	 *
	 * @return nur die bevorzugten Benennungen. Nicht null, nicht modifizierbar.
	 */
	public Collection<Tag> getTag1XX() {
		return getTagsBetween("100", "153");
	}



	/**
   *
   * @return nur die abweichenden Benennungen.
   *          Nicht null, nicht modifizierbar.
   */
	public Collection<Tag> getTag4XX() {
		return getTagsBetween("400", "451");
	}

	/**
	 * 5XX.
	 * @return Alle 5xx. Nicht null, nicht modifizierbar.
	 */
	public Collection<Tag> getTag5XX() {
		return getTagsBetween("500", "551");
	}

	/**
	 *
	 * @return Nicht null, nicht modifizierbar.
	 */
	public Collection<Tag> getTag7XX() {
		return getTagsBetween("700", "751");
	}

	//@formatter:on

	@Override
	public Collection<Tag> getUnmodifiables() {
		if (unmodifiables == null) {
			unmodifiables = new HashSet<Tag>();
			unmodifiables.add(findTag("001"));
			unmodifiables.add(findTag("002"));
			unmodifiables.add(findTag("003"));
			unmodifiables.add(findTag("006"));
			unmodifiables.add(findTag("035"));
			unmodifiables.add(findTag("039"));
			unmodifiables.add(findTag("913"));
		}
		return unmodifiables;
	}

}
