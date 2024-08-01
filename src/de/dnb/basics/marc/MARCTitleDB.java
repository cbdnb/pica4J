package de.dnb.basics.marc;

public final class MARCTitleDB extends MarcDB {

  /**
   *
   */
  private static final MarcTag TAG_082 = new MarcTag("082", "DDC-Notation", NR,
    "DEWEY DECIMAL CLASSIFICATION NUMBER", "Art der Ausgabe", "Quelle der Notation");

  /**
   *
   */
  private static final MarcTag TAG_770 = new MarcTag("770", "Beilage/Sonderheft", R,
    "SUPPLEMENT/SPECIAL ISSUE ENTRY", "Fußnotenregelung", "Regelung der Anzeigekonstante");

  /**
   *
   */
  private static final MARCAuthorityDB AUTH_DB = MARCAuthorityDB.getDB();

  /**
   *
   */
  private static final MarcSubfieldIndicator MarcSubfieldIndicator_111_J =
    new MarcSubfieldIndicator('j', "Funktionsbezeichnung", R, "Relator term");

  public static final MarcTag TAG_100 = new MarcTag("100", "Haupteintragung - Personenname", NR,
    "MAIN ENTRY--PERSONAL NAME", "(Art des Personennamens als Eintragungselement", null);

  public static final MarcTag TAG_110 = new MarcTag("110", "Haupteintragung - Körperschaftsname",
    NR, "MAIN ENTRY--CORPORATE NAME", "(Art des Körperschaftsnamens als Eintragungselement", null);

  public static final MarcTag TAG_111 = new MarcTag("111", "Haupteintragung - Kongressname", NR,
    "MAIN ENTRY--MEETING NAME", "Art des Kongressnamens als Eintragungselement", null);

  public static final MarcTag TAG_130 = new MarcTag("130", "Haupteintragung - Einheitstitel", NR,
    "MAIN ENTRY--UNIFORM TITLE", null, "Anzahl der zu übergehenden Zeichen");

  /**
   *  $9:X
   * Einziger Indikator für $X, R.
   * (Anzeigerelevanz).
   */
  public static final MarcSubfieldIndicator DOLLAR_9XR =
    new MarcSubfieldIndicator('X', "Anzeigerelevanz", R, "", true);

  static MARCTitleDB marcTitleDB;

  /**
   * Nicht eindeutiger Indikator für $g (Zusatz), R.
   */
  public static final MarcSubfieldIndicator DOLLAR_G =
    new MarcSubfieldIndicator('g', "Sonstige Informationen", R, "Miscellaneous information");

  public static MARCTitleDB getDB() {
    if (marcTitleDB == null)
      marcTitleDB = new MARCTitleDB();
    return marcTitleDB;
  }

  /**
   * @param args nix.
   */
  public static void main(final String[] args) {
    final MARCTitleDB db = MARCTitleDB.getDB();

    db.getTags().forEach(tag ->
    {
      System.out.println(tag);
      tag.getSortedIndicators().forEach(ind -> System.out.println("\t" + ind));
    });

  }

  //@formatter:on

  private MARCTitleDB() {
    MarcTag newTag;

    newTag = new MarcTag("007", "Feld mit fester Länge zur physischen Beschreibung", R,
      "PHYSICAL DESCRIPTION FIXED FIELD", null, null);
    addTag(newTag);

    newTag = new MarcTag("015", "Nummer der Nationalbibliografie", R,
      "NATIONAL BIBLIOGRAPHY NUMBER", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Nummer der Nationalbibliografie", R,
      "National bibliography number"));
    newTag.add(new MarcSubfieldIndicator('z', "Gelöschte/Ungültige Nummer der Nationalbibliografie",
      R, "Canceled/Invalid national national bibliography number"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("016", "Kontrollnummer der nationalbibliografischen Agentur", R,
      "NATIONAL BIBLIOGRAPHIC AGENCY CONTROL NUMBER", "Nationalbibliografische Agentur", null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Datensatzkontrollnummer", NR, "Record control number"));
    newTag.add(new MarcSubfieldIndicator('z', "Gelöschte/Ungültige Datensatzkontrollnummer", R,
      "Canceled/Invalid record control number"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("020", "Internationale Standardbuchnummer", R,
      "INTERNATIONAL STANDARD BOOK NUMBER", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Internationale Standardbuchnummer", NR,
      "International Standard Book number"));
    newTag.add(new MarcSubfieldIndicator('c', "Bezugsbedingungen", NR, "Terms of availability"));
    newTag
      .add(new MarcSubfieldIndicator('z', "Gelöschte/Ungültige ISBN", R, "Canceled/Invalid ISBN"));
    newTag.add(new MarcSubfieldIndicator('9', "ISBN mit Bindestrichen", R, ""));

    newTag = new MarcTag("022", "Internationale Standardseriennummer", R,
      "INTERNATIONAL STANDARD SERIAL NUMBER", "Level von internationalem Interesse", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Internationale Standardseriennummer", NR,
      "International Standard Serial Number"));
    newTag.add(new MarcSubfieldIndicator('l', "ISSN-L", NR, "ISSN-L"));
    newTag.add(new MarcSubfieldIndicator('m', "Gelöschte ISSN-L", NR, "Canceled ISSN-L"));
    newTag.add(new MarcSubfieldIndicator('y', "Falsche ISSN", NR, "Incorrect ISSN"));
    newTag.add(new MarcSubfieldIndicator('z', "Gelöschte ISSN", R, "Canceled ISSN"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("024", "Anderer Standard-Identifier", R, "Other Standard Identifier",
      "Art der Standardnummer", "Abweichungsindikator");
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Standardnummer oder Code", NR, "Standard number or code"));
    newTag.add(new MarcSubfieldIndicator('c', "Bezugsbedingungen", NR, "Terms of availability"));
    newTag.add(new MarcSubfieldIndicator('z', "Gelöschte(r)/ungültige(r) Standardnummer oder Code",
      NR, "Canceled/invalid standard number or code"));
    newTag.add(DOLLAR_2);
    newTag.add(new MarcSubfieldIndicator('9', "Standardnummer (mit Bindestrichen)", NR, ""));

    newTag = new MarcTag("028", "Verlegernummer", R, "PUBLISHER NUMBER", "Art der Verlegernummer",
      "Fußnoten-/Nebeneintragungsregelung");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Verlegernummer", NR, "Publisher number"));
    newTag.add(new MarcSubfieldIndicator('b', "Quelle", NR, "Source"));

    newTag = new MarcTag("030", "CODEN", R, "CODEN DESIGNATION", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "CODEN", NR, "CODEN"));

    newTag = new MarcTag("041", "Sprachcode", R, "LANGUAGE CODE", "Übersetzungshinweis",
      "Quelle des Codes");
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Sprachcode des Textes/der Tonspur oder des separaten Titels",
        NR, "Language code of text/sound track or separate title"));
    newTag.add(new MarcSubfieldIndicator('h',
      "(Sprachcode der Original- und/oder Zwischenübersetzung des Textes", NR,
      "Language code of original and/or intermediate translations of text"));
    newTag.add(MarcDB.DOLLAR_8);

    newTag = new MarcTag("044", "Ländercode der veröffentlichenden/herstellenden Stelle", NR,
      "COUNTRY OF PUBLISHING/PRODUCING ENTITY CODE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('c', "ISO-code", R, "ISO code"));

    newTag = TAG_082;
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Notation", R, "Classification number"));
    newTag.add(new MarcSubfieldIndicator('q', "Vergabestelle", NR, "Assigning agency"));
    newTag.add(MARCAuthorityDB.INDICATOR_DDC_2);
    newTag.add(DOLLAR_8);

    newTag = new MarcTag("083", "Zusätzliche DDC-Notation", R,
      "ADDITIONAL DEWEY DECIMAL CLASSIFICATION NUMBER", "Art der Ausgabe", "Quelle der Notation");
    addTag(newTag);
    newTag.addInherited(TAG_082);

    newTag = new MarcTag("084", "Andere Notation", R, "OTHER CLASSIFICATION NUMBER", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Notation", R, "Classification number"));
    newTag.add(new MarcSubfieldIndicator('q', "Vergabestelle", R, "Assigning agency"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("085", "Synthetische Notation und ihre Bestandteile", R,
      "SYNTHESIZED CLASSIFICATION NUMBER COMPONENTS", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('b', "Grundnotation", R, "Base number"));
    newTag.add(new MarcSubfieldIndicator('s', "Aus Haupt- oder Hilfstafeln angehängte Ziffern", R,
      "Digits added from classification number in schedule or external table"));
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);

    newTag = new MarcTag("090", "Weitere Codierungen", NR, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Papierzustand", NR, ""));
    newTag.add(new MarcSubfieldIndicator('i',
      "Angaben der Freiwilligen Selbstkontrollen der Filmwirtschaft", NR, ""));
    newTag.add(new MarcSubfieldIndicator('n', "Veröffentlichungsart und Inhalt", R, ""));

    newTag = TAG_100;
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_100_A);
    newTag.add(MarcSubfieldIndicator_100_B);
    newTag.add(MarcSubfieldIndicator_100_C);
    newTag.add(MarcSubfieldIndicator_100_D);
    newTag.add(DOLLAR_E_RELATOR);
    connect1XX(newTag);

    newTag = TAG_110;
    addTag(newTag);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_A);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_B);
    newTag.add(DOLLAR_E_RELATOR);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_N);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_Z);
    connect1XX(newTag);

    newTag = TAG_111;
    addTag(newTag);
    newTag.add(MarcDB.MarcSubfieldIndicator_111_A);
    newTag.add(MarcDB.MarcSubfieldIndicator_111_C);
    newTag.add(MarcSubfieldIndicator_111_D);
    newTag.add(MarcSubfieldIndicator_111_E);
    newTag.add(MarcSubfieldIndicator_111_J);
    newTag.add(MarcSubfieldIndicator_111_N);
    connect1XX(newTag);

    newTag = TAG_130;
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_130_A);
    newTag.add(MarcSubfieldIndicator_130_F);
    newTag.add(DOLLAR_G);
    newTag
      .add(new MarcSubfieldIndicator('k', "Formbestandteil der Ansetzung", R, "Form subheading"));
    newTag.add(MarcSubfieldIndicator_130_M);
    newTag.add(MarcSubfieldIndicator_130_N);
    newTag.add(MarcSubfieldIndicator_130_O);
    newTag.add(MarcSubfieldIndicator_130_P);
    newTag.add(MarcSubfieldIndicator_130_R);
    newTag.add(MarcSubfieldIndicator_130_S);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_9XR);

    newTag = new MarcTag("210", "Kurztitel", R, "ABBREVIATED TITLE",
      "Nebeneintragung unter dem Titel", "Art");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Kurztitel", NR, "Abbreviated title"));
    newTag.add(new MarcSubfieldIndicator('b', "Nähere Angabe", NR, "Qualifying information"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("222", "Key-Title", R, "KEY TITLE", null, "Nichtsortierende Zeichen");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Key-Title", NR, "Key title"));
    newTag.add(new MarcSubfieldIndicator('b', "Nähere Angabe", NR, "Qualifying information"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("240", "Einheitstitel", R, "UNIFORM TITLE",
      "Gedruckter oder angezeigter Einheitstitel", "Nichtsortierende Zeichen");
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_130_A);
    newTag.addInherited(TAG_130);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("245", "Titelangabe", NR, "TITLE STATEMENT",
      "Nebeneintragung unter dem Titel", "Nichtsortierende Zeichen");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Titel", NR, "Title"));
    newTag.add(new MarcSubfieldIndicator('b', "Zusatz zum Titel", NR, "Remainder of title"));
    newTag.add(new MarcSubfieldIndicator('c', "Verfasserangabe etc.", NR,
      "Statement of responsibility, etc."));
    newTag.add(new MarcSubfieldIndicator('h', "Medium", NR, "Medium"));
    newTag.add(new MarcSubfieldIndicator('n', "Zählung eines Teils/einer Abteilung eines Werkes",
      NR, "Number of part/section of a work"));
    newTag.add(new MarcSubfieldIndicator('p', "Titel eines Teils/einer Abteilung eines Werkes", NR,
      "Name of part/section of a work"));

    newTag = new MarcTag("246", "Titelvarianten", R, "VARYING FORM OF TITLE",
      "Fußnote/Nebeneintragung", "Art des Titels");
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Hauptsachtitel/Kurztitel", NR, "Title proper/short title"));
    newTag.add(new MarcSubfieldIndicator('b', "Zusatz zum Titel", NR, "Remainder of title"));
    newTag.add(new MarcSubfieldIndicator('i', "Anzeigetext", NR, "Display text"));

    newTag = new MarcTag("250", "Ausgabebezeichnung", R, "EDITION STATEMENT", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Ausgabebezeichnung", NR, "Edition statement"));
    newTag.add(new MarcSubfieldIndicator('b', "Zusatz zur Ausgabebezeichnung", NR,
      "Remainder of Edition statement"));

    newTag = new MarcTag("255", "Kartografische mathematische Daten", R,
      "CARTOGRAPHIC MATHEMATICAL DATA", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Maßstabsangabe", NR, "Statement of scale"));

    newTag = new MarcTag("259", "Ausgabebezeichnung in normierter Form", NR, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Ausgabebezeichnung in normierter Form", NR, ""));

    newTag = new MarcTag("260", "Publikation, Vertrieb usw. (Erscheinungsvermerk) - benutze 264!",
      R, "PUBLICATION, DISTRIBUTION, ETC. (IMPRINT)", "Geplantes Erscheinungsdatum", null);
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Verlagsorte", R, "Place of publication, distribution, etc"));
    newTag
      .add(new MarcSubfieldIndicator('b', "Verleger", R, "Name of publisher, distributor, etc."));
    newTag.add(
      new MarcSubfieldIndicator('c', "Datierung", R, "Date of publication, distribution, etc"));
    newTag.add(new MarcSubfieldIndicator('e', "Druckort(e)", R, "Place of manufacture"));
    newTag.add(new MarcSubfieldIndicator('f', "Drucker", R, "Manufacturer"));

    newTag =
      new MarcTag("264", "Entstehungs-, Verlags-, Vertriebs-, Herstellungs- und Copyright-Angabe",
        R, "PRODUCTION, PUBLICATION, DISTRIBUTION, MANUFACTURE, AND COPYRIGHT NOTICE",
        "Abfolge der Angaben", "Zuordnung zur Entität");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Entstehungs-, Verlags-, Vertriebs-, Herstellungsort",
      R, "Place of production, publication, distribution, manufacture"));
    newTag.add(new MarcSubfieldIndicator('b', "Erzeuger-, Verlags-, Vertriebs-, Herstellername", R,
      "Name of producer, publisher, distributor, manufacturer"));
    newTag.add(new MarcSubfieldIndicator('c',
      "Entstehungs-, Verlags-, Vertriebs-, Herstellungs- oder Copyrightdatum", R,
      "Date of production, publication, distribution, manufacture, or copyright notice"));

    newTag = new MarcTag("300", "Physische Beschreibung", R, "PHYSICAL DESCRIPTION", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Umfang", R, "Extent"));
    newTag.add(
      new MarcSubfieldIndicator('b', "Andere physische Details", NR, "Other physical details"));
    newTag.add(new MarcSubfieldIndicator('c', "Maße", R, "Dimensions"));
    newTag.add(new MarcSubfieldIndicator('e', "Begleitmaterial", R, "Accompanying material"));

    addTag(TAG_336);
    addTag(TAG_337);

    newTag = new MarcTag("338", "Datenträgertyp", R, "CARRIER TYPE", null, null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Datenträgertyp in Textform", R, "Carrier type term"));
    newTag.add(new MarcSubfieldIndicator('b', "Code für Datenträgertyp", R, "Carrier type code"));
    newTag.add(DOLLAR_2);
    newTag
      .add(new MarcSubfieldIndicator('3', "Spezifische Materialangaben", R, "Materials specified"));
    newTag.add(DOLLAR_8);

    newTag =
      new MarcTag("348", "Musikalische Ausgabeform", R, "Format of Notated Music", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Begriff für die musikalische Ausgabeform", R,
      "Format of notated music term"));
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("362", "Erscheinungsdaten und/oder Bandbezeichnung", R,
      "DATES OF PUBLICATION AND/OR SEQUENTIAL DESIGNATION", "Datumsformat", null);
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Datumsangaben der Veröffentlichung und/oder Bandbezeichnung",
        NR, "Dates of publication and/or sequential designation"));

    newTag = new MarcTag("363", "Datum und Zählung in normalisierter Form", R,
      "NORMALIZED DATE AND SEQUENTIAL DESIGNATION", "Beginn-/Endekennzeichnung",
      "Status des Erscheinens");
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Erste Ebene der Zählung", NR, "First level of enumeration"));
    newTag.add(new MarcSubfieldIndicator('b', "Zweite Ebene der Zählung", NR,
      "Second level of enumeration"));
    newTag.add(new MarcSubfieldIndicator('i', "Erste Ebene der Chronologie", NR,
      "First level of chronology"));
    newTag.add(new MarcSubfieldIndicator('j', "Zweite Ebene der Chronologie", NR,
      "Second level of chronology"));
    newTag.add(new MarcSubfieldIndicator('k', "Dritte Ebene der Chronologie", NR,
      "Third level of chronology"));
    newTag.add(new MarcSubfieldIndicator('u', "Erste Ebene der Bezeichnung", NR,
      "First level textual designation"));
    newTag.add(new MarcSubfieldIndicator('z', "Öffentliche Anmerkung", NR, "Public note"));
    newTag.add(DOLLAR_8);

    newTag = new MarcTag("365", "Handelspreis", R, "TRADE PRICE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('b', "Betrag", NR, "Price amount"));

    addTag(TAG_380);
    addTag(TAG_382);
    addTag(TAG_383);
    addTag(TAG_384);

    newTag = new MarcTag("490", "Gesamttitelangabe", R, "SERIES STATEMENT",
      "Spezifiziert, ob auf den Gesamttitel verwiesen wird", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Gesamttitelangabe", R, "Series statement"));
    newTag.add(new MarcSubfieldIndicator('v', "Bandnummer/Bandbezeichnung", R,
      "Volume number/sequential designation"));
    newTag
      .add(new MarcSubfieldIndicator('3', "Spezifische Materialangaben", R, "Materials specified"));

    newTag = new MarcTag("500", "Allgemeine Fußnote", R, "GENERAL NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Allgemeine Fußnote", NR,
      "Place of publication, distribution, etc"));

    newTag = new MarcTag("501", "Fußnote zu enthaltenen Werken", R, "WITH NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu enthaltenen Werken", NR, "With note"));

    newTag = new MarcTag("502", "Dissertationsvermerk", R, "DISSERTATION NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Dissertationsvermerk", NR, "Dissertation note"));
    newTag.add(new MarcSubfieldIndicator('b', "Art des Abschlusses", NR, "Degree type"));
    newTag.add(
      new MarcSubfieldIndicator('c', "Name der Institution, die den akademischen Grad verleiht", NR,
        "Name of granting institution"));
    newTag.add(new MarcSubfieldIndicator('d', "Graduierungsjahr", NR, "Year degree granted"));
    newTag.add(DOLLAR_G);
    newTag.add(new MarcSubfieldIndicator('o', "Hochschulschriften-Identifier", NR,
      "Dissertation identifier"));

    newTag =
      new MarcTag("505", "Fußnote zu strukturierten Inhaltsangaben", R, "FORMATTED CONTENTS NOTE",
        "Regelung der Anzeigekonstante", "(Level der Inhaltsangabenbezeichnung");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu strukturierten Inhaltsangaben", NR,
      "Formatted contents note"));
    newTag.add(new MarcSubfieldIndicator('r', "Verfasserangabe", R, "Statement of responsibility"));
    newTag.add(DOLLAR_G);
    newTag.add(new MarcSubfieldIndicator('t', "Titel", R, "Title"));

    newTag = new MarcTag("506", "Zugangsbestimmungen", R, "Restrictions on Access Note",
      "Benutzungsbeschränkung", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Zugangsbestimmungen", NR, "Terms governing access"));
    newTag.add(
      new MarcSubfieldIndicator('c', "Physische Benutzbarkeit", NR, "Physical access provisions"));
    newTag.add(new MarcSubfieldIndicator('d', "Autorisierter Benutzer", NR, "Authorized users"));
    newTag.add(DOLLAR_URI);

    newTag = new MarcTag("508", "Fußnote zu Vor- und Nachspann", R,
      "CREATION/PRODUCTION CREDITS NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu Vor- und Nachspann", NR,
      "Creation/production credits note"));

    newTag = new MarcTag("511", "Fußnote zu Mitwirkenden oder Interpreten", R,
      "PARTICIPANT OR PERFORMER NOTE", "Regelung der Anzeigekonstante", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu Mitwirkenden oder Interpreten", NR,
      "Participant or performer note"));

    newTag = new MarcTag("515", "Fußnote zu Besonderheiten der Zählung", R,
      "NUMBERING PECULIARITIES NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu Besonderheiten der Zählung", NR,
      "Numbering peculiarities note"));

    newTag = new MarcTag("516", "Fußnote zur Art der Datei oder der Daten", R,
      "TYPE OF COMPUTER FILE OR DATA NOTE", "Regelung der Anzeigekonstante", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zur Art der Datei oder der Daten", NR,
      "Type of computer file or data note"));

    newTag = new MarcTag("518", "Fußnote zu Datum/Zeit und Ort eines Ereignisses", R,
      "DATE/TIME AND PLACE OF AN EVENT NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu Datum/Zeit und Ort eines Ereignisses", NR,
      "Date/time and place of an event note"));

    newTag = new MarcTag("520", "Fußnote zu Zusammenfassungen usw.", R, "SUMMARY, ETC.",
      "Regelung der Anzeigekonstante", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu Zusammenfassungen usw.", NR,
      "Summary, etc. note"));

    newTag = new MarcTag("521", "Fußnote zur Zielgruppe", R, "TARGET AUDIENCE NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zur Zielgruppe", R, "Target audience note"));

    newTag = new MarcTag("530", "Fußnote zu zusätzlich erhältlichen Publikationsformen", R,
      "ADDITIONAL PHYSICAL FORM AVAILABLE NOTE", null, null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Fußnote zu zusätzlich erhältlichen Publikationsformen",
        NR, "Additional physical form available note"));

    newTag = new MarcTag("533", "Fußnote zur Reproduktion", R, "REPRODUCTION NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Art der Reproduktion", NR, "Type of reproduction"));
    newTag.add(new MarcSubfieldIndicator('b', "Ort der Reproduktion", R, "Place of reproduction"));
    newTag.add(new MarcSubfieldIndicator('c', "Verantwortliche Stelle der Reproduktion", R,
      "Agency responsible for reproduction"));
    newTag
      .add(new MarcSubfieldIndicator('d', "Datum der Reproduktion", NR, "Date of reproduction"));
    newTag.add(new MarcSubfieldIndicator('e', "Physische Beschreibung der Reproduktion", NR,
      "Physical description of reproduction"));
    newTag.add(new MarcSubfieldIndicator('f', "Gesamttitelangabe der Reproduktion", R,
      "Series statement of reproduction"));
    newTag.add(
      new MarcSubfieldIndicator('n', "(Fußnote der Reproduktion", R, "Note about reproduction"));
    newTag.add(new MarcSubfieldIndicator('7', "Datenelemente mit fester Länge der Reproduktion", NR,
      "Fixed-length data elements of reproduction"));

    newTag = new MarcTag("535", "Fußnote zum Standort von Originalen und Duplikaten", R,
      "LOCATION OF ORIGINALS/DUPLICATES NOTE", "Zusätzliche Angaben über den Besitzer", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Besitzer", NR, "Custodian"));
    newTag.add(new MarcSubfieldIndicator('c', "Land", NR, "Country"));
    newTag.add(new MarcSubfieldIndicator('g', "Code des Aufbewahrungsortes", NR,
      "Repository location code"));

    newTag = new MarcTag("538", "Fußnote zu Systemdetails (-besonderheiten)", R,
      "SYSTEM DETAILS NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zu Systemvoraussetzungen", NR,
      "System details note"));

    newTag = new MarcTag("546", "Fußnote zur Sprache", R, "LANGUAGE NOTE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zur Sprache", NR, "Language note"));

    newTag = new MarcTag("550", "Fußnote zur herausgebenden Körperschaft", R, "ISSUING BODY NOTE",
      null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fußnote zur herausgebenden Körperschaft", NR,
      "Issuing body note"));

    newTag = new MarcTag("555", "Fußnote zum kumulierenden Register/Fundstellennachweis", R,
      "CUMULATIVE INDEX/FINDING AIDS NOTE", "Regelung der Anzeigekonstante", null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Fußnote zum kumulierenden Register/Fundstellennachweis",
        NR, "Cumulative index/finding aids note"));

    newTag = new MarcTag("583",
      "Fußnote zum Bearbeitungsvermerk, Angaben zu Bestandserhaltungsmaßnahmen und "
        + "Archivierungsabsprachen",
      R, "ACTION NOTE", "Vertraulichkeit", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Bearbeitung, Aktion", NR, "Action"));
    newTag.add(new MarcSubfieldIndicator('c', "Zeit/Datum der Bearbeitung, Datum der Aktion", NR,
      "Time/date of action"));
    newTag.add(new MarcSubfieldIndicator('f', "Autorisierende Stelle, Kontext/Rechtsgrundlage", R,
      "Authorization"));
    newTag.add(new MarcSubfieldIndicator('h', "Zuständigkeit, Kontext/Rechtliche Verantwortung", NR,
      "Jurisdiction"));
    newTag.add(new MarcSubfieldIndicator('i', "Bearbeitungsmethode", NR, "Method of action"));
    newTag.add(
      new MarcSubfieldIndicator('k', "Beauftragter, durchführender Akteur", R, "Action agent"));
    newTag.add(new MarcSubfieldIndicator('l', "Status, Schadensbild", R, "Status"));
    newTag.add(new MarcSubfieldIndicator('z', "Allgemeine Bemerkungen", NR, "Public note"));
    newTag.add(DOLLAR_2);
    newTag.add(new MarcSubfieldIndicator('3',
      "Spezifische Materialangaben, Bestandsangaben, Signatur", NR, "Materials specified"));
    newTag.add(DOLLAR_5);

    newTag = new MarcTag("591", "Bemerkungen zur Titelaufnahme", NR, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fussnote", NR, "Fussnote"));

    newTag = new MarcTag("600", "Nebeneintragung unter einem Schlagwort - Personenname", R,
      "SUBJECT ADDED ENTRY--PERSONAL NAME", "Art des Personennamens als Eintragungselement",
      "Thesaurus");
    addTag(newTag);
    newTag.add(DOLLAR_Z);
    newTag.addInherited(AUTH_DB.getTag("100"));
    connect6XX(newTag);

    newTag = new MarcTag("610", "Nebeneintragung unter einem Schlagwort - Körperschaftsname", R,
      "SUBJECT ADDED ENTRY--CORPORATE NAME", "Art des Körperschaftsnamens als Eintragungselement",
      "Thesaurus");
    addTag(newTag);
    newTag.addInherited(AUTH_DB.getTag("110"));
    connect6XX(newTag);

    newTag = new MarcTag("611", "Nebeneintragung unter einem Schlagwort - Kongressname", R,
      "SUBJECT ADDED ENTRY--MEETING NAME", "Art des Kongressnamens als Eintragungselement",
      "Thesaurus");
    addTag(newTag);
    newTag.addInherited(AUTH_DB.getTag("111"));
    connect6XX(newTag);

    newTag = new MarcTag("630", "Nebeneintragung unter einem Schlagwort - Einheitstitel", R,
      "SUBJECT ADDED ENTRY--UNIFORM TITLE", "Nichtsortierende Zeichen", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(AUTH_DB.getTag("130"));
    connect6XX(newTag);

    newTag = new MarcTag("648", "Nebeneintragung unter einem Schlagwort - Zeitschlagwort", R,
      "SUBJECT ADDED ENTRY--CHRONOLOGICAL TERM", null, "Thesaurus");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Zeitschlagwort", NR, "Chronological term"));
    newTag.add(new MarcSubfieldIndicator('v', "Genre-Form", R, "Form subdivision"));
    newTag.add(DOLLAR_X);
    newTag.add(MarcDB.DOLLAR_Y);
    newTag.add(DOLLAR_Z);
    connect6XX(newTag);

    newTag = new MarcTag("650", "Nebeneintragung unter einem Schlagwort - Sachschlagwort", R,
      "SUBJECT ADDED ENTRY--TOPICAL TERM", "Level des Schlagwortes", "Thesaurus");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a',
      "Sachschlagwort oder geografischer Name als Eintragungselement", NR,
      "Topical term or geographic name as entry element"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_X);
    connect6XX(newTag);

    newTag = new MarcTag("651", "Nebeneintragung unter einem Schlagwort - Geografischer Name", R,
      "SUBJECT ADDED ENTRY--GEOGRAPHIC NAME", null, "Thesaurus");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Geografischer Name", NR, "Geographic name"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_X);
    newTag.add(DOLLAR_Z);
    connect6XX(newTag);

    newTag = new MarcTag("653", "Indexierungsterm - nicht normiert", R, "INDEX TERM--UNCONTROLLED",
      "Level des Indexierungsterms", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Nicht-normierter Term", R, "Uncontrolled term"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_X);
    connect6XX(newTag);

    newTag = new MarcTag("655", "Nebeneintragung unter einem Schlagwort -  Genre/Formschlagwort", R,
      "SUBJECT ADDED ENTRY--GENRE/FORM", "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Genre/Form oder fokussierter Term", NR,
      "Genre/form data or focus term"));
    newTag.add(DOLLAR_X);
    newTag.add(DOLLAR_Y);
    newTag.add(DOLLAR_Z);
    connect6XX(newTag);

    newTag =
      new MarcTag("689", "RSWK-Kette", R, "", "Nummer der RSWK-Kette", "Nummer des Kettengliedes");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Schlagwort", NR, ""));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_T);
    newTag.add(MarcSubfieldIndicator_100_B);
    newTag.add(MarcSubfieldIndicator_100_C);
    newTag.add(MarcSubfieldIndicator_100_D);
    newTag.add(MarcSubfieldIndicator_110_N);
    newTag.add(MarcSubfieldIndicator_110_Z);
    newTag.add(MarcSubfieldIndicator_111_E);
    newTag.add(MarcSubfieldIndicator_111_J);
    newTag.add(MarcSubfieldIndicator_130_F);
    newTag.add(MarcSubfieldIndicator_130_H);
    newTag.add(MarcSubfieldIndicator_130_L);
    newTag.add(MarcSubfieldIndicator_130_M);
    newTag.add(MarcSubfieldIndicator_130_O);
    newTag.add(MarcSubfieldIndicator_130_P);
    newTag.add(MarcSubfieldIndicator_130_R);
    newTag.add(MarcSubfieldIndicator_130_S);
    newTag.add(new MarcSubfieldIndicator('A', "Indikator des Kettengliedes", R, ""));
    newTag.add(new MarcSubfieldIndicator('B', "Permutationsmuster", R, ""));
    newTag.add(new MarcSubfieldIndicator('C', "Bemerkungen", R, ""));
    newTag.add(new MarcSubfieldIndicator('D', "Repräsentation der MARC-Feldnummer", R, ""));
    newTag.add(DOLLAR_X);
    newTag.add(DOLLAR_Y);
    newTag.add(DOLLAR_Z);
    connect6XX(newTag);
    newTag.add(DOLLAR_5);

    newTag = new MarcTag("700", "Nebeneintragung - Personenname", R, "ADDED ENTRY--PERSONAL NAME",
      "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(TAG_100);

    newTag = new MarcTag("710", "Nebeneintragung - Körperschaftsname", R,
      "ADDED ENTRY--CORPORATE NAME", "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_110_A);
    newTag.addInherited(TAG_110);
    connect7XX(newTag);

    newTag = new MarcTag("711", "Nebeneintragung - Kongressname", R, "ADDED ENTRY--MEETING NAME",
      "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_111_A);
    newTag.add(MarcSubfieldIndicator_111_J);
    newTag.addInherited(TAG_111);
    connect7XX(newTag);

    newTag = new MarcTag("730", "Nebeneintragung - Einheitstitel", R, "ADDED ENTRY--UNIFORM TITLE",
      "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_130_A);
    newTag.addInherited(TAG_130);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("751", "Nebeneintragung - Geografischer Name", R,
      "ADDED ENTRY--GEOGRAPHIC NAME", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Geografischer Name", R, "Geographic name"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_4);

    newTag = TAG_770;
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Haupteintragung", NR, "Main entry heading"));
    newTag.add(new MarcSubfieldIndicator('b', "Ausgabe", NR, "Edition"));
    newTag.add(new MarcSubfieldIndicator('d', "Ort, Verlag und Erscheinungsjahr", NR,
      "Place, publisher, and date of publication"));
    newTag
      .add(new MarcSubfieldIndicator('h', "Physische Beschreibung", NR, "Physical description"));
    newTag.add(new MarcSubfieldIndicator('i', "Anzeigetext", NR, "Display text"));
    newTag.add(new MarcSubfieldIndicator('n', "Fußnote", R, "Note"));
    newTag.add(new MarcSubfieldIndicator('o', "Anderer Identifier", R, "Other item identifier"));
    newTag.add(new MarcSubfieldIndicator('t', "Titel", NR, "Title"));
    newTag
      .add(new MarcSubfieldIndicator('w', "Datensatzkontrollnummer", NR, "Record control number"));
    newTag.add(new MarcSubfieldIndicator('x', "Internationale Standardseriennummer", NR,
      "International Standard Serial Number"));
    newTag.add(new MarcSubfieldIndicator('z', "Internationale Standardbuchnummer", NR,
      "International Standard Book Number"));

    newTag = new MarcTag("772", "Übergeordnete Einheit der Beilage", R, "SUPPLEMENT PARENT ENTRY",
      "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("773", "Übergeordnete Einheit", R, "HOST ITEM ENTRY", "Fußnotenregelung",
      "Regelung der Anzeigekonstante");
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('g', "Verknüpfungsangaben", R, "Relationship information"));
    newTag.add(
      new MarcSubfieldIndicator('q', "Zählung und erste Seite", NR, "Enumeration and first page"));
    newTag.add(new MarcSubfieldIndicator('7', "Kontrollunterfeld", NR, "Control subfield"));
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("775", "Andere Ausgabe", R, "OTHER EDITION ENTRY", "Fußnotenregelung",
      "Regelung der Anzeigekonstante");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("776", "Andere physische Form", R, "ADDITIONAL PHYSICAL FORM ENTRY",
      "Art der Ansetzung", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("777", "Erschienen mit", R, "ISSUED WITH ENTRY", "Art der Ansetzung",
      "Thesaurus");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("780", "Vorgänger", R, "PRECEDING ENTRY", "Fußnotenregelung",
      "Art der Verknüpfung");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("785", "Nachfolger", R, "SUCCEEDING ENTRY", "Fußnotenregelung",
      "Art der Verknüpfung");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("787", "Nichtspezifische Beziehung", R, "NONSPECIFIC RELATIONSHIP ENTRY",
      "Fußnotenregelung", "Art der Verknüpfung");
    addTag(newTag);
    newTag.addInherited(TAG_770);

    newTag = new MarcTag("800", "Nebeneintragung unter dem Gesamttitel - Personenname", R,
      "SERIES ADDED ENTRY--PERSONAL NAME", "Art des Personennamens als Eintragungselement", null);
    addTag(newTag);
    newTag.addInherited(TAG_100);
    newTag.add(new MarcSubfieldIndicator('v', "Band/Erscheinungsverlauf", NR,
      "Volume/sequential designation"));
    newTag.add(new MarcSubfieldIndicator('w', "Kontrollnummer des Datensatzes", NR,
      "Bibliographic record control number"));
    newTag.add(DOLLAR_4);
    newTag.add(new MarcSubfieldIndicator('7', "Verknüpfung", NR, "Control subfield"));
    newTag.add(new MarcSubfieldIndicator('9', "Sortierzählung", NR, ""));
    connect7XX(newTag);

    newTag = new MarcTag("810", "Nebeneintragung unter dem Gesamttitel - Körperschaftsname", R,
      "SERIES ADDED ENTRY--CORPORATE NAME", "Art des Körperschaftsnames als Eintragungselement",
      null);
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_110_A);
    newTag.addInherited(TAG_110);
    newTag.add(new MarcSubfieldIndicator('v', "Band/Erscheinungsverlauf", NR,
      "Volume/sequential designation"));
    newTag.add(new MarcSubfieldIndicator('w', "Kontrollnummer des Datensatzes", NR,
      "Bibliographic record control number"));
    newTag.add(DOLLAR_4);
    newTag.add(new MarcSubfieldIndicator('7', "Verknüpfung", NR, "Control subfield"));
    newTag.add(new MarcSubfieldIndicator('9', "Sortierzählung", NR, ""));
    connect7XX(newTag);

    newTag = new MarcTag("811", "Nebeneintragung unter dem Gesamttitel - Kongressname", R,
      "SERIES ADDED ENTRY--MEETING NAME", "Art des Kongressnames als Eintragungselement", null);
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_111_A);
    newTag.add(MarcSubfieldIndicator_111_J);
    newTag.addInherited(TAG_111);
    newTag.add(new MarcSubfieldIndicator('v', "Band/Erscheinungsverlauf", NR,
      "Volume/sequential designation"));
    newTag.add(new MarcSubfieldIndicator('w', "Kontrollnummer des Datensatzes", NR,
      "Bibliographic record control number"));
    newTag.add(DOLLAR_4);
    newTag.add(new MarcSubfieldIndicator('7', "Verknüpfung", NR, "Control subfield"));
    newTag.add(new MarcSubfieldIndicator('9', "Sortierzählung", NR, ""));
    connect7XX(newTag);

    newTag = new MarcTag("830", "Nebeneintragung unter dem Gesamttitel - Einheitstitel", R,
      "ADDITIONAL PHYSICAL FORM ENTRY", null, "Nichtsortierende Zeichen");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Einheitstitel", NR, "Uniform title"));
    newTag.add(MarcSubfieldIndicator_130_P);
    newTag.add(DOLLAR_T);
    newTag.add(new MarcSubfieldIndicator('v', "Band/Erscheinungsverlauf", NR,
      "Volume/sequential designation"));
    newTag.add(new MarcSubfieldIndicator('w', "Kontrollnummer des Datensatzes", NR,
      "Bibliographic record control number"));
    newTag.add(DOLLAR_4);
    newTag.add(new MarcSubfieldIndicator('7', "Verknüpfung", NR, "Control subfield"));
    newTag.add(new MarcSubfieldIndicator('9', "Sortierzählung", NR, ""));

    newTag = new MarcTag("850", "Besitzende Institution", NR, "HOLDING INSTITUTION", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Besitzende Institution", R, "Holding institution"));

    newTag = new MarcTag("856", "Elektronische Adresse und Zugriff", R,
      "ELECTRONIC LOCATION AND ACCESS", "Zugriffsart", "Beziehung");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Host Name", R, "Host name"));
    newTag.add(new MarcSubfieldIndicator('b', "Zugriffsnummer", R, "Access number"));
    newTag.add(
      new MarcSubfieldIndicator('c', "(Angaben zur Komprimierung", R, "Compression information"));
    newTag.add(new MarcSubfieldIndicator('d', "Pfad", R, "Path"));
    newTag.add(new MarcSubfieldIndicator('f', "Elektronischer Name", R, "Electronic name"));
    newTag
      .add(new MarcSubfieldIndicator('h', "Bearbeiter der Anfrage", NR, "Processor of request"));
    newTag.add(new MarcSubfieldIndicator('i', "Anweisung", R, "Instruction"));
    newTag.add(new MarcSubfieldIndicator('j', "Bits pro Sekunde", NR, "Bits per second"));
    newTag.add(new MarcSubfieldIndicator('k', "Passwort", NR, "Password"));
    newTag.add(new MarcSubfieldIndicator('l', "Login", NR, "Logon"));
    newTag.add(new MarcSubfieldIndicator('m', "Kontaktstelle für Hilfe beim Zugriff", R,
      "Contact for access assistance"));
    newTag.add(new MarcSubfieldIndicator('n', "Name des Standortes des Hosts", NR,
      "Name of location of host"));
    newTag.add(new MarcSubfieldIndicator('o', "Betriebssystem", R, "Operating system"));
    newTag.add(new MarcSubfieldIndicator('p', "Port", NR, "Port"));
    newTag
      .add(new MarcSubfieldIndicator('q', "Elektronisches Format", NR, "Electronic format type"));
    newTag.add(new MarcSubfieldIndicator('r', "Einstellungen", NR, "Settings"));
    newTag.add(new MarcSubfieldIndicator('s', "Dateigröße", R, "File size"));
    newTag.add(new MarcSubfieldIndicator('t', "Terminalemulation", R, "Terminal emulation"));
    newTag.add(DOLLAR_URI);
    newTag
      .add(new MarcSubfieldIndicator('v', "Zugriffszeiten", R, "Hours access method available"));
    newTag
      .add(new MarcSubfieldIndicator('w', "Datensatzkontrollnummer", R, "Record control number"));
    newTag.add(new MarcSubfieldIndicator('x', "Interne Anmerkungen", R, "Nonpublic note"));
    newTag.add(new MarcSubfieldIndicator('y', "Verknüpfungstext", R, "Link text"));
    newTag.add(new MarcSubfieldIndicator('z', "Allgemeine Anmerkungen", R, "Public note"));
    newTag.add(new MarcSubfieldIndicator('2', "Zugriffsart", R, "Access method"));
    newTag.add(
      new MarcSubfieldIndicator('3', "Spezifische Materialangaben", NR, "Materials specified"));

    newTag = new MarcTag("883", "Herkunft von maschinell generierten Metadaten", R,
      "Machine-generated Metadata Provenance", "Art und Weise der maschinellen Vergabe", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Generierungsprozess", NR, "Generation process"));
    newTag.add(new MarcSubfieldIndicator('c', "Konfidenzwert", NR, "Confidence value"));
    newTag.add(new MarcSubfieldIndicator('d', "Generierungsdatum", NR, "Generation date"));
    newTag.add(new MarcSubfieldIndicator('q', "Generierungsagentur", NR, "Generation agency"));
    newTag.add(DOLLAR_8);

    newTag = new MarcTag("889", "Angaben zum umgelenkten Datensatz", R, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('w', "Kontrollnummer des Zielsatzes", R,
      "Replacement bibliographic record control number"));

    newTag = new MarcTag("902", "Naxos-spezifischer Tag", R, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Naxos-Nummer", R, ""));

    newTag = new MarcTag("924", "Bestandsinformationen", R, "", "", "");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Lokale IDN des Bestandsdatensatzes", NR, ""));
    newTag.add(
      new MarcSubfieldIndicator('b', "ISIL als Kennzeichen der besitzenden Institution", NR, ""));
    newTag.add(new MarcSubfieldIndicator('c', "Leihverkehrsregion", NR, ""));
    newTag.add(new MarcSubfieldIndicator('d', "Fernleihindikator", NR, ""));
    newTag.add(new MarcSubfieldIndicator('e',
      "(Vertragsrechtliche) Einschränkungen bei der Fernleihe", R, ""));
    newTag.add(new MarcSubfieldIndicator('f', "Kommentar zum Fernleihindikator", R, ""));
    newTag.add(new MarcSubfieldIndicator('g', "Signatur", R, ""));
    newTag.add(new MarcSubfieldIndicator('h', "Sonderstandort / Abteilung", R, ""));
    newTag.add(new MarcSubfieldIndicator('i', "Sonderstandort-Signatur", R, ""));
    newTag.add(new MarcSubfieldIndicator('j', "Kommentar(e) zur Signatur", R, ""));
    newTag.add(new MarcSubfieldIndicator('k',
      "Elektronische Adresse für eine Computerdatei im Fernzugriff, Uniform Resource Identifier", R,
      ""));
    newTag.add(
      new MarcSubfieldIndicator('m', "Normierte Bestandsangaben, Bandzählung (Beginn)", R, ""));
    newTag.add(
      new MarcSubfieldIndicator('o', "Normierte Bestandsangaben, Tageszählung (Beginn)", R, ""));
    newTag.add(
      new MarcSubfieldIndicator('p', "Normierte Bestandsangaben, Monatszählung (Beginn)", R, ""));
    newTag.add(new MarcSubfieldIndicator('q', "Normierte Bestandsangaben, Jahr (Beginn)", R, ""));
    newTag
      .add(new MarcSubfieldIndicator('r', "Normierte Bestandsangaben, Bandzählung (Ende)", R, ""));
    newTag
      .add(new MarcSubfieldIndicator('s', "Normierte Bestandsangaben, Heftzählung (Ende)", R, ""));
    newTag
      .add(new MarcSubfieldIndicator('t', "Normierte Bestandsangaben, Tageszählung (Ende)", R, ""));
    newTag.add(
      new MarcSubfieldIndicator('u', "Normierte Bestandsangaben, Monatszählung (Ende)", R, ""));
    newTag.add(new MarcSubfieldIndicator('v', "Normierte Bestandsangaben, Jahr (Ende)", R, ""));
    newTag.add(new MarcSubfieldIndicator('w', "Normierte Bestandsangaben, Kettung", R, ""));
    newTag.add(new MarcSubfieldIndicator('x',
      "Normierte Bestandsangaben, Kennzeichnung \"laufender Bestand\"", NR, ""));
    newTag.add(new MarcSubfieldIndicator('y',
      "Aufbewahrungs- und Verfügbarkeitszeitraum, Moving Wall", NR, ""));
    newTag.add(new MarcSubfieldIndicator('z', "Zusammenfassende Bestandsangaben", NR, ""));
    newTag.add(
      new MarcSubfieldIndicator('9', "Sigel als Kennzeichen der besitzenden Institution", NR, ""));

    newTag = new MarcTag("925", "Weitere DNB-Codierungen", R, "", "", "");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Code je nach Indikatoren", NR, ""));

    newTag = new MarcTag("926", "Thema-Klassifikation", R, "Thema-Klassifikation",
      "(Level der Thema-Klassifikation", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Thema-Code (Subjects + Qualifier)", NR,
      "Thema-Code (Subjects + Qualifier)"));
    newTag.add(new MarcSubfieldIndicator('o', "ONIX-Code für Thema-Klasse", NR,
      "ONIX-Code für Thema-Klasse"));
    newTag.add(
      new MarcSubfieldIndicator('q', "Thema-Quelle (Sourcename)", NR, "Thema-Quelle (Sourcename)"));
    newTag.add(new MarcSubfieldIndicator('v', "Thema-Version", NR, "Thema-Version"));
    newTag.add(new MarcSubfieldIndicator('x', "Thema-Text", NR, "Thema-Text"));

  }

  /**
   * @param newTag
   */
  private void connect6XX(final MarcTag newTag) {
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
  }

  /**
   * e,t,0,2,4.
   * @param newTag
   */
  private void connect7XX(final MarcTag newTag) {
    newTag.add(DOLLAR_E_RELATOR);
    newTag.add(DOLLAR_T);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_4);
  }

  /**
   * g,0,2,4,r.
   * @param newTag
   */
  private void connect1XX(final MarcTag newTag) {
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_4);
    newTag.add(DOLLAR_9XR);
  }

}
