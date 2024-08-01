package de.dnb.basics.marc;

import java.util.Collection;

public final class MARCAuthorityDB extends MarcDB {

  static MARCAuthorityDB marcAuthDB;

  public static MARCAuthorityDB getDB() {
    if (marcAuthDB == null)
      marcAuthDB = new MARCAuthorityDB();
    return marcAuthDB;
  }

  public static final MarcTag TAG_100 = new MarcTag("100", "Person – Bevorzugter Name", NR,
    "Heading - Personal Name", "(Art des Personennamens als Eintragungselement", null);

  public static final MarcTag TAG_110 = new MarcTag("110", "Körperschaft – Bevorzugter Name", NR,
    "Heading - Corporate Name", "(Art des Körperschaftsnamens als Eintragungselement", null);

  public static final MarcTag TAG_111 = new MarcTag("111", "Kongress – Bevorzugter Name", NR,
    "Heading - Meeting Name", "Art des Kongressnamens als Eintragungselement", null);

  public static final MarcTag TAG_130 = new MarcTag("130", "Bevorzugter Titel des Werks", NR,
    "Heading - Uniform Title", null, "Anzahl der zu übergehenden Zeichen");

  /**
   *
   */
  private static final MarcTag TAG_551 = new MarcTag("551", "Geografikum – Beziehung", R,
    "See Also From Tracing - Geographic Name", null, null);
  /**
   *
   */
  private static final MarcTag TAG_550 = new MarcTag("550", "Sachbegriff – Beziehung", R,
    "See Also From Tracing - Topical Term", null, null);
  /**
   *
   */
  private static final MarcTag TAG_530 = new MarcTag("530", "Einheitstitel – Beziehung", R,
    "See Also From Tracing - Uniform Title", null, null);
  /**
   *
   */
  private static final MarcTag TAG_511 = new MarcTag("511", "Kongress – Beziehung", R,
    "See Also From Tracing - Meeting Name", "Art des Kongressnamens als Eintragungselement", null);
  /**
   *
   */
  private static final MarcTag TAG_510 =
    new MarcTag("510", "Körperschaft – Beziehung", R, "See Also From Tracing - Corporate Name",
      "Art des Körperschaftsnamens als Eintragungselement", null);
  /**
   *
   */
  private static final MarcTag TAG_500 = new MarcTag("500", "Person – Beziehung", R,
    "See Also From Tracing - Personal Name", "Art des Personennamens als Eintragungselement", null);

  /**
   *  $9:v
   * Bemerkungen (wie $v). Kein Indikator im eigentlichen Sinne. NR.
   * Gehört zu $9.
   */
  public static final MarcSubfieldIndicator DOLLAR_9V_NR =
    new MarcSubfieldIndicator('v', "Bemerkungen", NR, "", true);

  /**
   * $9:T
   */
  public static final MarcSubfieldIndicator INDICATOR_DDC_T =
    new MarcSubfieldIndicator('t', "Zeitstempel", NR, "", true);
  /**
   * $9:g
   */
  public static final MarcSubfieldIndicator INDICATOR_DDC_G =
    new MarcSubfieldIndicator('g', "Letzte Überprüfung", NR, "", true);
  /**
   *  $9:d
   */
  public static final MarcSubfieldIndicator INDICATOR_DDC_D =
    new MarcSubfieldIndicator('d', "Determiniertheit", NR, "", true);
  /**
   *Ausgabenummer.
   */
  public static final MarcSubfieldIndicator INDICATOR_DDC_2 =
    new MarcSubfieldIndicator('2', "Ausgabenummer", NR, "Edition number");
  /**
   *  $9:L
   * Einziger Indikator für $L
   * (Sprachcode bei nicht-lateinischen Schriftzeichen).
   */
  public static final MarcSubfieldIndicator DOLLAR_9L = new MarcSubfieldIndicator('L',
    "Sprachcode bei nicht-lateinischen Schriftzeichen", NR, "", true);

  /**
   *  $9:Z
   * Einziger Indikator für $Z, NR.
   * (Zeitliche Gültigkeit).
   */
  public static final MarcSubfieldIndicator DOLLAR_9Z =
    new MarcSubfieldIndicator('Z', "Zeitliche Gültigkeit", NR, "", true);

  /**
   *  $9:X
   * Einziger Indikator für $X, NR.
   * (Anzeigerelevanz).
   */
  public static final MarcSubfieldIndicator DOLLAR_9X =
    new MarcSubfieldIndicator('X', "Anzeigerelevanz", NR, "", true);

  /**
   *  $9:Y
   * Indikator für $Y
   * (MO-Relevanz).
   */
  public static final MarcSubfieldIndicator DOLLAR_9Y =
    new MarcSubfieldIndicator('Y', "MO-Relevanz", R, "", true);

  /**
   *  $9:U
   * Einziger Indikator für $U
   * (Schriftcode bei nicht-lateinischen Schriftzeichen).
   */
  public static final MarcSubfieldIndicator DOLLAR_9U = new MarcSubfieldIndicator('U',
    "Schriftcode bei nicht-lateinischen Schriftzeichen", NR, "", true);

  /**
   * Kontrollunterfeld.
   */
  public static final MarcSubfieldIndicator DOLLAR_W =
    new MarcSubfieldIndicator('w', "Kontrollunterfeld", R, "Control subfield");

  public static final MarcSubfieldIndicator DOLLAR_I =
    new MarcSubfieldIndicator('i', "Verweisungsphrase", R, "Relationship information");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_150_A =
    new MarcSubfieldIndicator('a', "Sachbegriff", NR, "Topical term entry element");

  /**
   * Geografikum.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_151_A =
    new MarcSubfieldIndicator('a', "Geografikum", NR, "Geographic name");

  /**
   * Geografische Unterteilung.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_151_Z =
    new MarcSubfieldIndicator('z', "Geografische Unterteilung", R, "Geographic subdivision");

  /**
   * Zeitschlagwort.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_548_A =
    new MarcSubfieldIndicator('a', "Zeitschlagwort", NR, "Chronological term");

  public static final MarcTag TAG_150 = new MarcTag("150", "Sachbegriff – Bevorzugte Benennung", NR,
    "Heading - Topical Term", null, null);

  public static final MarcTag TAG_151 = new MarcTag("151", "Geografikum – Bevorzugter Name", NR,
    "Heading - Geographic Name", null, null);

  public static final MarcTag TAG_400 =
    new MarcTag("400", "Person – Abweichender Name", R, "See From Tracing - Personal Name", "", "");

  public static final MarcTag TAG_410 = new MarcTag("410", "Körperschaft – Abweichender Name", R,
    "See From Tracing - Corporate Name", "", "");

  public static final MarcTag TAG_411 = new MarcTag("411", "Kongress – Abweichender Name", R,
    "See From Tracing - Meeting Name", "", "");

  public static final MarcTag TAG_430 =
    new MarcTag("430", "Titel – Abweichender Name", R, "See From Tracing - Uniform Title", "", "");

  public static final MarcTag TAG_450 = new MarcTag("450", "Sachbegriff – Abweichende Benennung", R,
    "See From Tracing - Topical Term", "", "");

  public static final MarcTag TAG_451 = new MarcTag("451", "Geografikum – Abweichender Name", R,
    "See From Tracing - Geographic Name", "", "");

  public static final MarcTag TAG_548 = new MarcTag("548", "Zeit – Beziehung", R,
    "See Also From Tracing - Chronological Term", null, null);

  /**
   * @param args nix.
   */
  public static void main(final String[] args) {
    final MARCAuthorityDB db = MARCAuthorityDB.getDB();

    db.getTags().forEach(tag ->
    {
      System.out.println(tag);
      tag.getSortedIndicators().forEach(ind -> System.out.println("\t" + ind));
    });

  }

  //@formatter:on

  private MARCAuthorityDB() {
    MarcTag newTag;

    newTag = new MarcTag("024", "GND-Identifier, sonstige Standardnummern", R,
      "Other Standard Identifier", "Art der Standardnummer", null);
    addTag(newTag);
    newTag.add(
      new MarcSubfieldIndicator('a', "Standardnummer oder Code", NR, "Standard number or code"));
    newTag.add(new MarcSubfieldIndicator('0', "Standardnummer oder Code", NR,
      "Authority record control number or standard number"));
    newTag.add(DOLLAR_2);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_NR);

    newTag = new MarcTag("042", "Authentifizierungscode", NR, "AUTHENTICATION CODE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Authentifizierungscode", NR, "Authentication code"));

    newTag = new MarcTag("065", "GND Systematik", R, "Other Classification Number", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Notation", NR, ""));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("075", "Entitätentyp", R, "TYPE OF ENTITY", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('b', "Code für Entitäten", R, ""));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("079", "Normdatenspezifische Codierungen", NR, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Satztyp (Normdaten)", NR, ""));
    newTag.add(new MarcSubfieldIndicator('q', "Teilbestandskennzeichen", R, ""));
    newTag.add(new MarcSubfieldIndicator('u', "Nutzungskennzeichen", R, ""));

    newTag = new MarcTag("083", "DDC-Notation", R, "Dewey Decimal Classificaton Number",
      "Art der Ausgabe", "Quelle der Notation");
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Notationselement", NR, "Classification number element"));
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(INDICATOR_DDC_2);
    // in Webdewey:
    newTag.add(DOLLAR_9);
    newTag.add(INDICATOR_DDC_D);
    newTag.add(INDICATOR_DDC_G);
    newTag.add(INDICATOR_DDC_T);
    newTag.add(DOLLAR_9V_NR);
    newTag.add(DOLLAR_0);

    newTag = new MarcTag("089", "Veraltete DDC-Notation", R, "", null, null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Notationselement", NR, "Classification number element"));
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(INDICATOR_DDC_2);
    //    newTag.add(DOLLAR_9);
    newTag.add(INDICATOR_DDC_D);
    newTag.add(INDICATOR_DDC_G);
    newTag.add(INDICATOR_DDC_T);
    newTag.add(DOLLAR_9V_NR);

    newTag = TAG_100;
    addTag(newTag);
    newTag.add(MarcDB.MarcSubfieldIndicator_100_A);
    newTag.add(MarcDB.MarcSubfieldIndicator_100_B);
    newTag.add(MarcDB.MarcSubfieldIndicator_100_C);
    newTag.add(MarcDB.MarcSubfieldIndicator_100_D);
    newTag.add(
      new MarcSubfieldIndicator('q', "Vollständige Form eines Namens", NR, "Fuller form of name"));
    connectGXYZ(newTag);

    newTag = TAG_110;
    addTag(newTag);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_A);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_B);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_N);
    connectGXYZ(newTag);
    newTag.add(MarcDB.MarcSubfieldIndicator_110_Z);

    newTag = TAG_111;
    addTag(newTag);
    newTag.add(MarcDB.MarcSubfieldIndicator_111_A);
    newTag.add(MarcDB.MarcSubfieldIndicator_111_C);
    newTag.add(MarcSubfieldIndicator_111_D);
    newTag.add(MarcSubfieldIndicator_111_E);
    newTag.add(MarcSubfieldIndicator_111_N);
    connectGXYZ(newTag);

    newTag = TAG_130;
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_130_A);
    connectGXYZ(newTag);

    // Das können auch Werke sein:
    connect1XXTitle(TAG_100);
    connect1XXTitle(TAG_110);
    connect1XXTitle(TAG_111);
    connect1XXTitle(TAG_130);

    newTag = TAG_150;
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_150_A);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_R);
    connectGXYZ(newTag);

    newTag = TAG_151;
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_151_A);
    newTag.add(MarcDB.DOLLAR_X);
    newTag.add(MarcSubfieldIndicator_151_Z);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_R);
    connectGXYZ(newTag);

    newTag = new MarcTag("260", "Zu verknüpfende Schlagwörter in Hinweissätzen", R,
      "Complex See Reference", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Term", R, "Heading referred to"));
    newTag.add(DOLLAR_0);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_R);

    addTag(TAG_336);

    // Seltsam, aber wahr (in Titeldaten ist das Feld 338):
    newTag = new MarcTag("339", "Datenträgertyp", R, "339", "Art des Typs", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Typ-Term", R, "Type Term"));
    newTag.add(new MarcSubfieldIndicator('b', "Typ-Code", NR, "Type Code"));
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("372", "Tätigkeitsbereich", R, "FIELD OF ACTIVITY", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('s', "Zeitliche Gültigkeit Beginn", R, "Start period"));
    newTag.add(MarcDB.DOLLAR_URI);
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_NR);

    newTag = new MarcTag("375", "Geschlechtsangabe", R, "Gender", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Geschlecht", R, "Gender"));
    newTag.add(DOLLAR_2);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_R);

    newTag =
      new MarcTag("377", "Sprachencode nach ISO 639-2/B", NR, "Associated Language", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Code", R, "Language code"));
    newTag.add(DOLLAR_2);

    addTag(TAG_380);
    addTag(TAG_382);
    addTag(TAG_383);
    addTag(TAG_384);

    newTag = TAG_400;
    addTag(newTag);
    newTag.add(MarcDB.DOLLAR_E_RELATOR);

    newTag = TAG_410;
    addTag(newTag);
    newTag.add(MarcDB.DOLLAR_E_RELATOR);

    newTag = TAG_411;
    addTag(newTag);

    newTag = TAG_430;
    addTag(newTag);

    newTag = TAG_450;
    addTag(newTag);

    newTag = TAG_451;
    addTag(newTag);

    newTag = TAG_500;
    addTag(newTag);
    newTag.add(MarcDB.DOLLAR_E_RELATOR);

    newTag = TAG_510;
    addTag(newTag);
    newTag.add(MarcDB.DOLLAR_E_RELATOR);

    newTag = TAG_511;
    addTag(newTag);

    newTag = TAG_530;
    addTag(newTag);

    newTag = TAG_548;
    addTag(newTag);
    newTag.add(MarcSubfieldIndicator_548_A);
    newTag.add(MarcDB.DOLLAR_4);
    newTag.add(DOLLAR_W);
    newTag.add(DOLLAR_I);
    newTag.add(MarcDB.DOLLAR_5);
    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_NR);
    newTag.add(DOLLAR_9X);
    newTag.add(DOLLAR_9Y);
    newTag.add(DOLLAR_9Z);

    newTag = TAG_550;
    addTag(newTag);

    newTag = TAG_551;
    addTag(newTag);

    newTag =
      new MarcTag("667", "Redaktionelle Bemerkungen", R, "Nonpublic General Note", null, null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Redaktionelle Bemerkung", NR, "Nonpublic general note"));
    newTag.add(MarcDB.DOLLAR_5);

    newTag = new MarcTag("670", "Quellenangaben", R, "Source Data Found", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Quelle", NR, "Source citation"));
    newTag.add(new MarcSubfieldIndicator('b', "Erläuternder Text", NR, "Information found"));
    newTag.add(MarcDB.DOLLAR_URI);

    newTag = new MarcTag("672", "Titelangaben", R, "", null, "Nichtsortierende Zeichen");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Titel", NR, "Title"));
    newTag.add(new MarcSubfieldIndicator('b', "Zusätze zum Titel", NR, "Remainder of title"));
    newTag.add(new MarcSubfieldIndicator('f', "Erscheinungsjahr", NR, "Date"));
    newTag.add(new MarcSubfieldIndicator('w', "Datensatzkontrollnummer (ISIL)", R,
      "Bibliographic record control number"));
    newTag.add(DOLLAR_0);

    newTag =
      new MarcTag("675", "Negativ eingesehene Quellen", NR, "Source Data Not Found", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Quellenangabe", R, "Source citation"));

    newTag = new MarcTag("677", "Definitionen", R, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Definition", NR, "Definition"));
    newTag.add(MarcDB.DOLLAR_URI);
    newTag.add(MarcDB.DOLLAR_5);
    newTag.add(new MarcSubfieldIndicator('v', "Bemerkung", NR, "Source of definition"));

    newTag = new MarcTag("678", "Biografische, historische und andere Angaben", R,
      "Biographical or Historical Data", "(Art der Daten", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Kurzer Text", R, "Biographical or historical data"));
    newTag.add(new MarcSubfieldIndicator('b', "Erläuternder Text", NR, "Expansion"));
    newTag.add(MarcDB.DOLLAR_URI);

    newTag = new MarcTag("680", "Benutzungshinweise", R, "Public General Note", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Benutzungshinweis", NR, ""));

    newTag = new MarcTag("682", "Nummer und bevorzugter Name bei Umlenkung von Datensätzen", NR,
      "Deleted Heading Information", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Zu ersetzende Ansetzung", R, "Replacement heading"));
    newTag.add(new MarcSubfieldIndicator('i', "Erläuterung", R, "Explanatory text"));
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_R);

    newTag = new MarcTag("700", "Person – Bevorzugter Name in einem anderen Datenbestand", R,
      "Established Heading Linking Entry - Personal Name", "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('t', "Titel", NR, ""));

    newTag = new MarcTag("710", "Körperschaft – Bevorzugter Name in einem anderen Datenbestand", R,
      "Established Heading Linking Entry - Corporate Name", "Thesaurus", "Thesaurus");
    addTag(newTag);

    newTag = new MarcTag("711", "Kongress – Bevorzugter Name in einem anderen Datenbestand", R,
      "Established Heading Linking Entry - Meeting Name", "Thesaurus", "Thesaurus");
    addTag(newTag);

    newTag = new MarcTag("730", "Einheitstitel – Bevorzugter Name in einem anderen Datenbestand", R,
      "Established Heading Linking Entry - Uniform Title", "Thesaurus", "Thesaurus");
    addTag(newTag);

    newTag = new MarcTag("750", "Sachbegriff – Bevorzugte Benennung in einem anderen Datenbestand",
      R, "Established Heading Linking Entry - Topical Term", "Thesaurus", "Thesaurus");
    addTag(newTag);

    newTag = new MarcTag("751", "Geografikum – Bevorzugter Name in einem anderen Datenbestand", R,
      "Established Heading Linking Entry - Geographic Name", "Thesaurus", "Thesaurus");
    addTag(newTag);

    newTag = new MarcTag("885", "Markierung für das Match-und-Merge-Verfahren  ", R,
      "Matching information", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Angabe des Match-und-Merge-Kontingents", NR,
      "Matching information"));
    newTag.add(new MarcSubfieldIndicator('b', "Match- und Prüfungsstatus", NR,
      "Status of matching and its checking"));
    newTag.add(new MarcSubfieldIndicator('c', "Konfidenzwert", NR, "Confidence value"));
    newTag.add(new MarcSubfieldIndicator('x', "Interne Fußnote", R, "Nonpublic note"));
    newTag.add(new MarcSubfieldIndicator('z', "Fußnote", R, "Public note"));
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);

    newTag = new MarcTag("912", "Mailbox", R, "Mailbox", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('b', "Absender/Empfänger", NR, ""));
    newTag.add(new MarcSubfieldIndicator('z', "Datum", NR, ""));
    newTag.add(new MarcSubfieldIndicator('a', "Mailboxnachricht (Freitext)", NR, ""));

    newTag = new MarcTag("913", "Alte Ansetzungsform", R, "", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('S', "Normdatei", NR, ""));
    newTag.add(new MarcSubfieldIndicator('i', "Indikator", NR, ""));
    newTag.add(new MarcSubfieldIndicator('a', "Ansetzungsform", NR, ""));
    newTag.add(DOLLAR_0);

    newTag = new MarcTag("983", "DNB-DDC-Feld?", R, "", "?", null);
    addTag(newTag);
    newTag.add(DOLLAR_2);

    connect4XX();
    connect5XX();
    connect7XX();

  }

  /**
   * Fügt den Vorzugsbenennungen $g, $x, $y und $z zu.
   *
   * @param newTag  nicht null
   */
  private void connectGXYZ(final MarcTag newTag) {
    newTag.add(MarcDB.DOLLAR_G);
    newTag.add(MarcDB.DOLLAR_X);
    newTag.add(MarcDB.DOLLAR_Y);
    newTag.add(MarcDB.DOLLAR_Z);
  }

  private void connect1XXTitle(final MarcTag newTag) {
    newTag.add(MarcDB.DOLLAR_T);
    newTag.add(MarcSubfieldIndicator_130_F);
    newTag.add(MarcSubfieldIndicator_130_H);
    newTag.add(MarcSubfieldIndicator_130_L);
    newTag.add(MarcSubfieldIndicator_130_M);
    newTag.add(MarcSubfieldIndicator_130_N);
    newTag.add(MarcSubfieldIndicator_130_O);
    newTag.add(MarcSubfieldIndicator_130_P);
    newTag.add(MarcSubfieldIndicator_130_R);
    newTag.add(MarcSubfieldIndicator_130_S);

    //    newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9V_R);

  }

  protected void connect4XX() {
    final MarcTag tag4XX = new MarcTag("4XX", "Dummy", R, "", null, null);
    tag4XX.add(DOLLAR_I);
    tag4XX.add(DOLLAR_W);
    tag4XX.add(MarcDB.DOLLAR_4);
    tag4XX.add(MarcDB.DOLLAR_5);
    //    tag4XX.add(DOLLAR_9);
    tag4XX.add(DOLLAR_9U);
    tag4XX.add(DOLLAR_9L);

    final Collection<MarcTag> tags4XX = getTagsBetween("400", "451");

    for (final MarcTag tag : tags4XX) {
      final String tagStr = "1" + tag.marc.substring(1);
      final MarcTag tag1XX = getTag(tagStr);
      tag.addInherited(tag1XX);
      tag.addInherited(tag4XX);
    }

  }

  protected void connect5XX() {
    final MarcTag tag5XX = new MarcTag("5XX", "Dummy", R, "", null, null);
    tag5XX.add(DOLLAR_0);
    tag5XX.add(MarcDB.DOLLAR_4);
    tag5XX.add(DOLLAR_W);
    tag5XX.add(DOLLAR_I);
    tag5XX.add(MarcDB.DOLLAR_5);
    //    tag5XX.add(DOLLAR_9);
    tag5XX.add(DOLLAR_9X);
    tag5XX.add(DOLLAR_9Y);
    tag5XX.add(DOLLAR_9Z);

    final Collection<MarcTag> tags5XX = getTagsBetween("500", "551");

    for (final MarcTag tag5xx : tags5XX) {
      final String tagStr = "1" + tag5xx.marc.substring(1);
      final MarcTag tag1XX = getTag(tagStr);
      // um 548 auszuschließen:
      if (tag1XX != null) {
        tag5xx.addInherited(tag1XX);
        tag5xx.addInherited(tag5XX);
      }

    }
  }

  protected void connect7XX() {
    final MarcTag tag7XX = new MarcTag("7XX", "Dummy", R, null, null, null);

    tag7XX.add(DOLLAR_0);
    tag7XX.add(DOLLAR_2);
    tag7XX.add(MarcDB.DOLLAR_5);
    // In DDC zugelassen:
    tag7XX.add(DOLLAR_9);
    tag7XX.add(DOLLAR_9U);
    tag7XX.add(DOLLAR_9L);

    final Collection<MarcTag> tags7XX = getTagsBetween("700", "751");

    for (final MarcTag tag : tags7XX) {
      final String tagStr = "1" + tag.marc.substring(1);
      final MarcTag tag1XX = getTag(tagStr);
      // um 797 auszuschliessen:
      if (tag1XX != null) {
        tag.addInherited(tag1XX);
        tag.addInherited(tag7XX);
      }
    }
  }

}
