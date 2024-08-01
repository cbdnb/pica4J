package de.dnb.gnd.parser.tag;

import java.util.Collection;
import java.util.HashSet;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.RecordUtils;

public final class SWDTagDB extends TagDB {

  /**
   * (Bemerkungen).
   */
  public static final Indicator BEM = new Indicator(" *", "", 'b', "Bemerkungen", NR, "Note");

  public static final Indicator DOLLAR_P =
    new Indicator("|p|", "", 'p', "Schlagwort mit Indikator \"p\"alle (@{)", R, "");
  public static final Indicator DOLLAR_G =
    new Indicator("|g|", "", 'g', "Schlagwort mit Indikator \"g\"alle (@{)", R, "");
  public static final Indicator DOLLAR_S =
    new Indicator("|s|", "", 's', "Schlagwort mit Indikator \"s\"alle (@{)", R, "");
  public static final Indicator DOLLAR_C =
    new Indicator("|c|", "", 'c', "Schlagwort mit Indikator \"c\"alle (@{)", R, "");
  public static final Indicator DOLLAR_K =
    new Indicator("|k|", "", 'k', "Schlagwort mit Indikator \"k\"alle (@{)", R, "");
  public static final Indicator DOLLAR_T =
    new Indicator("|t|", "", 't', "Schlagwort mit Indikator \"t\"alle (@{)", R, "");
  public static final Indicator DOLLAR_X =
    new Indicator("|x|", "", 'x', "Schlagwort mit Indikator \"Blank\"alle (@{)", R, "");
  public static final Indicator DOLLAR_U = new Indicator(" / ", "", 'u',
    "Unterschlagwort oder weiteres Schlagwort in einer Schlagwortkette", R, "");

  static SWDTagDB gndTagDB;

  public static final BibliographicTag TAG_800 =
    new BibliographicTag("800", "041A", "Hauptschlagwort", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_801 =
    new BibliographicTag("801", "041B", "1. Unterschlagwort", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_802 =
    new BibliographicTag("802", "041K", "2. Unterschlagwort", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_803 =
    new BibliographicTag("803", "041L", "3. Unterschlagwort", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_804 =
    new BibliographicTag("804", "041M", "4. Unterschlagwort", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_805 =
    new BibliographicTag("805", "041N", "5. Unterschlagwort", NR, "", "Heading - Topical Term");

  public static final BibliographicTag TAG_820 = new BibliographicTag("820", "041D",
    "Alternativform zum Hauptschlagwort", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_821 = new BibliographicTag("821", "041E/01",
    "1. Unterschlagwort zur Alternativform", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_822 = new BibliographicTag("822", "041E/02",
    "2. Unterschlagwort zur Alternativform", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_823 = new BibliographicTag("823", "041E/03",
    "3. Unterschlagwort zur Alternativform", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_824 = new BibliographicTag("824", "041E/04",
    "4. Unterschlagwort zur Alternativform", NR, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_825 = new BibliographicTag("825", "041E/05",
    "5. Unterschlagwort zur Alternativform", NR, "", "Heading - Topical Term");

  public static final BibliographicTag TAG_830 =
    new BibliographicTag("830", "041F", "Äquivalente Bezeichnung", R, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_835 = new BibliographicTag("835", "041T",
    "Deskriptoren aus Fremdthesauri", R, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_836 = new BibliographicTag("836", "041V",
    "Synonyme aus Fremdthesauri", R, "", "Heading - Topical Term");

  public static final BibliographicTag TAG_601 =
    new BibliographicTag("601", "041G", "Pauschalverweisung", NR, "", "");
  public static final BibliographicTag TAG_602 =
    new BibliographicTag("602", "041H", "Pauschalverweisung (Siehe-Verweisung)", NR, "", "");
  public static final BibliographicTag TAG_604 =
    new BibliographicTag("604", "041I", "Pauschalverweisung (Siehe-auch-Verweisung)", NR, "", "");
  public static final BibliographicTag TAG_606 =
    new BibliographicTag("606", "041O", "Zu verknüpfende Deskriptoren", R, "", "");

  public static final BibliographicTag TAG_845 = new BibliographicTag("845", "041S",
    "Übergeordneter Begriff zu Individualbezeichnung", R, "", "Heading - Topical Term");

  public static final BibliographicTag TAG_850 = new BibliographicTag("850", "039C",
    "Übergeordnetes Schlagwort", R, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_860 =
    new BibliographicTag("860", "039D", "Verwandtes Schlagwort", R, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_870 = new BibliographicTag("870", "039E",
    "Schlagwort für eine frühere Benennung", R, "", "Heading - Topical Term");
  public static final BibliographicTag TAG_880 = new BibliographicTag("880", "039F",
    "Schlagwort für eine spätere Benennung", R, "", "Heading - Topical Term");

  public static SWDTagDB getDB() {
    if (gndTagDB == null)
      gndTagDB = new SWDTagDB();
    return gndTagDB;
  }

  /**
   * @param args nix.
   * @throws IllFormattedLineException
   */
  public static void main(final String[] args) {

    final Record record = RecordUtils.readFromClip();
    System.out.println(record);
  }

  private SWDTagDB() {
    GNDTag newTag;
    BibliographicTag newBibTag;

    newTag = new DefaultGNDTag("001", "001A", "Quelle und Datum der Ersterfassung", NR, "008",
      "Date entered on file", null);
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Quelle und Datum (getrennt durch \":\")", NR, ""));

    newTag = new DefaultGNDTag("002", "001B", "Quelle und Datum der letzten Änderung", NR, "005",
      "Date and Time of Latest Transaction", null);
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Quelle und Datum (getrennt durch \":\")", NR, ""));
    newTag.add(new Indicator('t', "Uhrzeit", NR, ""));

    newTag = new DefaultGNDTag("003", "001D", "Quelle und Datum der letzten Statusvergabe", NR,
      "null", "", null);
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', " Quelle und Datum (getrennt durch \":\") ", NR, ""));

    newTag = new DefaultGNDTag("005", "002@", "Satzart", NR, "008; 079",
      "Fixed-Length Data Elements", null);
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Satzart", NR, ""));

    newTag = new DefaultGNDTag("006", "013A", "Pauschalverweisungskennzeichen", NR, "", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Pauschalverweisungskennzeichen", NR, ""));

    newTag = new DefaultGNDTag("010", "008@", "Änderungscodierung", NR, "Leader", "", "682");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('b', "Code", NR, ""));

    newTag = new EnumeratingTag("011", "008A", "Teilbestandskennzeichen", NR, "", "", "098");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator(";", "", 'a', 'q', "Code", R, ""));

    newTag = new EnumeratingTag("012", "008B", "Nutzungskennzeichen", NR, "", "", "096");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, ""));

    newTag = new DefaultGNDTag("014", "008F", "Quelle und Datum der SWD-Ersterfassung (m)", NR, "",
      "", "014");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Quelle und Datum (getrennt durch \":\")", NR, ""));

    newTag = new EnumeratingTag("015", "008D",
      "Code \"als Zusatz zur Vorzugsbezeichnung zugelassen\" ", NR, "", "");
    addTag(newTag);
    newTag.addDefaultFirst(
      new Indicator("/", "", 'a', "B  Berufsbezeichnung als Zusatz für Personennamen", R, ""));

    newTag = new DefaultGNDTag("021", "007Q", "SWD-Nummer (m)", NR, "", "", "021");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "SWD-Nummer (m)", NR, ""));

    newTag = new DefaultGNDTag("026", "007G",
      "Identifikationsnummern umgelenkter Datensätze (nur bei Hinweissätzen)", NR, "", "", "021");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Identifikationsnummern", NR, ""));

    newTag = new DefaultGNDTag("028", "007R", "Angabe einer GKD-Nummer", NR, "", "", "028");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Angabe einer GKD-Nummer", R, ""));

    newTag = new DefaultGNDTag("029", "007S", "Angabe einer PND-Nummer", R, "", "", "029");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Angabe einer PND-Nummer", NR, ""));

    newBibTag = new BibliographicTag("321", "032H",
      "Abkürzungen für Bezeichnungen der Instrumente der E- und U- Musik (nach RAK-Musik Anlage M 4)",
      R, "", "");
    addTag(newBibTag);
    newBibTag
      .add(new Indicator("|", "|", 'S', "Indikator für den musikalischen Bereich", NR, "", 'c'));
    newBibTag.add(new Indicator("", "", ";", true, 'a',
      "Abkürzung; weitere Abk. werden mit \";\" angeschlossen ", R, ""));

    newBibTag = TAG_601;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);

    newBibTag = TAG_602;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);

    newBibTag = TAG_604;
    addTag(newBibTag);
    newBibTag.add(new Indicator("|", "|", 'S', "Invertierung", NR, ""));
    addDefaultSubfields(newBibTag);

    newBibTag = TAG_606;
    addTag(newBibTag);
    newBibTag.add(DOLLAR_8);
    newBibTag.add(DOLLAR_9);
    newBibTag.addAlternative(
      new Indicator("|f|", "", 'f', "Zu verknüpfender Deskriptor mit Indikator \"f\"", NR, ""));
    newBibTag.addAlternative(
      new Indicator("|x|", "", 'x', "Zu verknüpfender Deskriptor mit Indikator \"x\"", NR, ""));
    newBibTag.addAlternative(
      new Indicator("|z|", "", 'z', "Zu verknüpfender Deskriptor mit Indikator \"z\"", NR, ""));
    addDefaultSubfields(newBibTag);

    newTag = new DefaultGNDTag("797", "003@", "Interne Identifikationsnummer PPN", NR, "001; 003",
      "Control Number (Identifier)", null);
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('0', "Interne Identifikationsnummer PPN", NR, ""));

    newBibTag = TAG_800;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);

    newBibTag = TAG_801;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_T);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_802;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_803;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_804;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_805;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = new BibliographicTag("808", "046A", "Erläuterungen zum Schlagwort", R, "", "");
    addTag(newBibTag);
    newBibTag.add(new Indicator("|", "|", 'S',
      "zweistelliger Ind.; 1. Pos.:  a (analog) oder d (dezimal), 2. Pos.: g (genaue) oder c (ungenaue) Angaben",
      NR, "", 'c'));
    newBibTag.add(new Indicator("", "", 'a', "Text gemäß Indikator", NR, ""));

    newBibTag = new BibliographicTag("809", "046Z", "Angaben zu CrissCross", NR, "", "");
    addTag(newBibTag);
    newBibTag.add(new Indicator("|", "|", 'a', "Projektbearbeitung", NR, "", 'c'));
    newBibTag.add(new Indicator("", "", 'b', "Bearbeiterkürzel, Bemerkungen", NR, ""));
    newBibTag.add(new Indicator(" *", "", 'c', "Bearbeitungszustand", NR, ""));

    newTag =
      new EnumeratingTag("810", "042A", "SWD-Systematik", NR, "065", "Other Classification Number");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator(";", "", 'a', "Notation", R, ""));

    newBibTag = new BibliographicTag("811", "042B", "Ländercode nach ISO 3166", NR, "043",
      "Geographic area code");
    addTag(newBibTag);
    newBibTag.add(new Indicator("", "", ";", true, 'a', "Ländercode", R, ""));
    newBibTag.add(BEM);

    newTag = new EnumeratingTag("812", "042C", "Sprachencode nach ISO 639-2/B", NR, "", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, "Language code"));

    newTag = new EnumeratingTag("813", "042D", "Zeitcode nach UDK", NR, "", "");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, "Language code"));

    newTag = new DefaultGNDTag("814", "042E", "Zeitcode", NR, "", "", "814");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator('a', "Zeitcode", NR, ""));

    newTag = new EnumeratingTag("815", "041C", "Entitätencodierung", NR, "", "", "093");
    addTag(newTag);
    newTag.addDefaultFirst(new Indicator(";", "", 'a', "Code", R, ""));

    newBibTag = new BibliographicTag("816", "037G", "DDC-Notation", R, "083",
      "Dewey Decimal Classificaton Number");
    addTag(newBibTag);
    newBibTag.add(new Indicator("", "", 'c', "DDC-Notation", NR, "", 'c'));
    newBibTag.add(new Indicator("#", "#", 'd', "Determiniertheit", NR, ""));
    newBibTag
      .add(new Indicator(" [", "]", 't', "\"gültig seit\" - Zeitstempel (JJJJ-MM-DD)", NR, ""));
    newBibTag.add(
      new Indicator(" {", "}", 'g', "Zeitstempel der letzten Überprüfung (JJJJ-MM-DD)", NR, ""));
    newBibTag.add(BEM);

    newBibTag =
      new BibliographicTag("818", "037I", "Veraltete Notation der Dewey-Dezimalklassifikation", R,
        "083", "Dewey Decimal Classificaton Number");
    addTag(newBibTag);
    newBibTag.add(new Indicator("", "", 'c', "DDC-Notation", NR, "", 'c'));
    newBibTag.add(new Indicator("#", "#", 'd', "Determiniertheit", NR, ""));
    newBibTag
      .add(new Indicator(" [", "]", 't', "\"gültig seit\" - Zeitstempel (JJJJ-MM-DD)", NR, ""));
    newBibTag.add(
      new Indicator(" {", "}", 'g', "Zeitstempel der letzten Überprüfung (JJJJ-MM-DD)", NR, ""));
    newBibTag.add(BEM);

    newBibTag = new BibliographicTag("819", "037H", "Geografische Koordinaten", R, "034",
      "Coded Cartographic Mathematical Data");
    addTag(newBibTag);
    newBibTag.add(
      new Indicator("|", "|", 'S', "Zeitstempel der letzten Überprüfung (JJJJ-MM-DD)", NR, ""));
    newBibTag.add(new Indicator('d', "Koordinaten – westlichster Längengrad", NR, ""));
    newBibTag.add(new Indicator('e', "Koordinaten – östlichster Längengrad", NR, ""));
    newBibTag.add(new Indicator('f', "Koordinaten – westlichster Längengrad", NR, ""));
    newBibTag.add(new Indicator('g', "Koordinaten – nördlichster Längengrad", NR, ""));
    newBibTag.add(new Indicator('2', "Quelle", NR, ""));
    newBibTag.add(BEM);

    newBibTag = TAG_820;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);

    newBibTag = TAG_821;
    addTag(newBibTag);
    newBibTag.add(DOLLAR_T);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_822;
    addTag(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_823;
    addTag(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_824;
    addTag(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_825;
    addTag(newBibTag);
    newBibTag.add(DOLLAR_X);

    newBibTag = TAG_830;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_U);
    newBibTag.add(BEM);

    newBibTag = TAG_835;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(BEM);

    newBibTag = TAG_836;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(BEM);

    newBibTag = TAG_845;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_U);
    newBibTag.add(BEM);

    newBibTag = TAG_850;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_U);
    newBibTag.add(BEM);

    newBibTag = TAG_860;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_U);
    newBibTag.add(BEM);

    newBibTag = new BibliographicTag("861", "039P", "Person als Urheber", R, "861", "");
    addTag(newBibTag);
    newBibTag.add(TagDB.DOLLAR_9);
    newBibTag.add(TagDB.DOLLAR_8);
    newBibTag.add(BEM);

    newBibTag = new BibliographicTag("862", "039Q", "Körperschaft als Urheber", R, "862", "");
    addTag(newBibTag);
    newBibTag.add(TagDB.DOLLAR_9);
    newBibTag.add(TagDB.DOLLAR_8);
    newBibTag.add(BEM);

    newBibTag = TAG_870;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_U);
    newBibTag.add(BEM);

    newBibTag = TAG_880;
    addTag(newBibTag);
    addDefaultSubfields(newBibTag);
    newBibTag.add(DOLLAR_U);
    newBibTag.add(BEM);

    newBibTag = new BibliographicTag("890", "039G",
      "Nummer und bevorzugter Name bzw. bevorzugte Benennung des Zielsatzes bei Aufspaltung von Datensätzen",
      NR, "682", "Deleted Heading Information");
    addTag(newBibTag);
    newBibTag.add(TagDB.DOLLAR_9);
    newBibTag.add(TagDB.DOLLAR_8);
    newBibTag.add(BEM);

    newBibTag = new BibliographicTag("892", "039I",
      "Nummer und bevorzugter Name bzw. bevorzugte Benennung des Zielsatzes bei Umlenkung von Datensätzen",
      NR, "682", "Deleted Heading Information");
    addTag(newBibTag);
    newBibTag.add(TagDB.DOLLAR_9);
    newBibTag.add(TagDB.DOLLAR_8);
    newBibTag.add(BEM);

    newBibTag = new BibliographicTag("901", "047A/01", "Mailbox", R, "912", "");
    addTag(newBibTag);
    newBibTag.add(
      new Indicator("", " // ", 'z', "Datum (JJJJ-MM-TT; wird maschinell besetzt)", NR, "", 'e'));
    newBibTag.add(BEM);
    newBibTag
      .add(new Indicator("", "*", 'b', "Absender- / Empfänger-ISIL (a-isil e-isil)", NR, "", 'e'));
    newBibTag.add(new Indicator("", "", 'a', "Freitext", NR, "", 'e'));

    newBibTag = new BibliographicTag("903", "047A/03", "ISIL", R, "", "");
    addTag(newBibTag);
    newBibTag.add(new Indicator("|e|", "", 'e', "ISIL des Urhebers des Datensatzes", NR, "", 'e'));
    newBibTag.add(new Indicator("|r|", "", 'r', "ISIL der Verbundredaktion", NR, "", 'e'));

  }

  /**
   * Fügt "Peter geht seine Ziegen füttern" an.
   * @param newTag
   */
  private void addDefaultSubfields(final Tag newTag) {
    newTag.add(DOLLAR_C);
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_K);
    newTag.add(DOLLAR_P);
    newTag.add(DOLLAR_S);
    newTag.add(DOLLAR_T);
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
   *
   *  (Quelle: https://wiki.d-nb.de/display/ILTIS/GND-Berechtigungen)
   */

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
