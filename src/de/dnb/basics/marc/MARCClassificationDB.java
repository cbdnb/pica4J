package de.dnb.basics.marc;

public final class MARCClassificationDB extends MarcDB {

  /**
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_X_ANDERE = new MarcSubfieldIndicator('x',
    "Andere Klassifikationsnummer", NR, "Other classification number");

  /**
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_R_WURZEL =
    new MarcSubfieldIndicator('r', "Wurzel-Nummer", R, "Root number");

  /**
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_N_GEGENBEISP = new MarcSubfieldIndicator('n',
    "Beispiel einer falschen Nummer", R, "Negative example class number");

  /**
   *
   */
  private static final MarcSubfieldIndicator DOLLAR_F_FACETT =
    new MarcSubfieldIndicator('f', "Facettendesignator", R, "Facet designator");

  /**
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_E_BEISP =
    new MarcSubfieldIndicator('e', "Nummer des Beispiels", R, "Example class number");

  /**
   *
   */
  private static final MarcSubfieldIndicator DOLLAR_B_BASE =
    new MarcSubfieldIndicator('b', "Basisnummer", NR, "Base number");

  /**
   *
   */
  private static final MarcSubfieldIndicator DOLLAR_D_DATUM =
    new MarcSubfieldIndicator('d', "Datum", NR, "Date of implementation at authoritative agency");

  /**
   * Benennung.
   */
  private static final MarcSubfieldIndicator DOLLAR_J =
    new MarcSubfieldIndicator('j', "Benennung", NR, "Caption ");

  /**
   * Thema.
   */
  private static final MarcSubfieldIndicator INDICATOR_253_T =
    new MarcSubfieldIndicator('t', "Thema", R, "Topic");

  /**
   *Erläuternder Text.
   */
  private static final MarcSubfieldIndicator DOLLAR_I =
    new MarcSubfieldIndicator('i', "Erläuternder Text", R, "Explanatory text");

  /**
   * Ende der Spanne.
   */
  private static final MarcSubfieldIndicator INDICATOR_153_C = new MarcSubfieldIndicator('c',
    "Ende der Spanne", R, "Classification number--ending number of span");

  /**
   * Einzelne Nummer oder Beginn der Spanne.
   */
  private static final MarcSubfieldIndicator INDICATOR_153_A =
    new MarcSubfieldIndicator('a', "Einzelne Nummer oder Beginn der Spanne", R,
      "Classification number--single number or beginning number of span");

  /**
   * Table sequence number.
   */
  private static final MarcSubfieldIndicator INDICATOR_Y_SEQUENZ = new MarcSubfieldIndicator('y',
    "Table sequence number", R, "Table sequence number for internal subarrangement or add table");
  static MARCClassificationDB marcAuthDB;

  public static MARCClassificationDB getDB() {
    if (marcAuthDB == null)
      marcAuthDB = new MARCClassificationDB();
    return marcAuthDB;
  }

  private final MARCAuthorityDB authorityDB = MARCAuthorityDB.getDB();

  /**
   * @param args nix.
   */
  public static void main(final String[] args) {
    final MARCClassificationDB db = MARCClassificationDB.getDB();
    db.getTags().forEach(tag ->
    {
      System.out.println(tag);
      tag.getSortedIndicators().forEach(ind -> System.out.println("\t" + ind));
    });

  }

  //@formatter:on

  private MARCClassificationDB() {
    MarcTag newTag;

    newTag = new MarcTag("042", "Authentifizierungscode", NR, "AUTHENTICATION CODE", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Authentifizierungscode", NR, "Authentication code"));

    newTag = new MarcTag("084", "Klassifikatonsschema und -ausgabe", NR,
      "Classificaton Scheme and Edition", "Art der Ausgabe", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Code für das Klassifikationsschema", NR,
      "Classification scheme code"));
    newTag.add(new MarcSubfieldIndicator('b', "Titel der Ausgabe", NR, "Edition title"));
    newTag.add(new MarcSubfieldIndicator('c', "Identifier der Ausgabe", NR, "Edition identifier"));
    newTag.add(
      new MarcSubfieldIndicator('d', "Identifier der Quelle", NR, "Source edition identifier"));
    newTag.add(new MarcSubfieldIndicator('e', "Sprachencode", NR, "Language code"));
    newTag.add(new MarcSubfieldIndicator('f', "Authorization", NR, "Authorization"));
    newTag.add(new MarcSubfieldIndicator('n', "Variations", NR, "Variations"));
    newTag.add(new MarcSubfieldIndicator('q', "Assigning agency", NR, "Assigning agency"));
    newTag.add(DOLLAR_8);

    newTag = new MarcTag("153", "Klassifikatonsnummer", NR, "Classification Number", null, null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('e', "Hierarchie--Einzelne Nummer oder Beginn der Spanne",
      R, "Classification number hierarchy--single number or beginning number of span"));
    newTag.add(new MarcSubfieldIndicator('f', "Hierarchie--Ende der Spanne", R,
      "Classification number hierarchy--ending number of span"));
    newTag.add(DOLLAR_G);
    newTag.add(new MarcSubfieldIndicator('h', "Benennung der Hierarchie", R, "Caption hierarchy"));
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_J);
    newTag.add(DOLLAR_X);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag =
      new MarcTag("159", "Andere Klassifikatonsnummer?", NR, "Classification Number", null, null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_J);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("194", "Noch eine andereKlassifikatonsnummer", NR, "Classification Number",
      null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('h', "Benennung der Hierarchie", R, "Caption hierarchy"));
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("253", "Siehe-Verweisung oder Notationsspanne", R, "Complex See Reference",
      "Typ der Verweisung", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "Datum?", NR,
      "Date of implementation at authoritative agency?"));
    newTag.add(new MarcSubfieldIndicator('e', "Verweisung--Einzelne Nummer oder Beginn der Spanne",
      R, "See Reference--single number or beginning number of span"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("353", "Siehe-auch-Verweisung oder Notationsspanne", R,
      "Complex See Also Reference", null, null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(new MarcSubfieldIndicator('b', "???", R, "???"));
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "Datum?", NR,
      "Date of implementation at authoritative agency?"));
    newTag.add(new MarcSubfieldIndicator('e', "Verweisung--Einzelne Nummer oder Beginn der Spanne",
      R, "See Reference--single number or beginning number of span"));
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("453", "Verweisung von ungültiger Nummer", R, "Invalid Number Tracing",
      "Quelle", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_J);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("559", "DNB-Verweisung???", R, "???", null, null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag
      .add(new MarcSubfieldIndicator('e', "Verweisung--Einzelne Nummer oder Beginn der Spanne???",
        R, "See Reference--single number or beginning number of span???"));
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_J);
    newTag.add(DOLLAR_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("673", "Segmentierte Klassifikationsnummer", R,
      "Segmented Classification Number", null, null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag =
      new MarcTag("674", "Segmentierungsanweisung", R, "Segmentation Instruction", null, null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("680", "Verwendungshinweis", R, "Scope Note", "", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(new MarcSubfieldIndicator('b', "Basisnummer???", NR, "Base number???"));
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "Datum?", NR,
      "Date of implementation at authoritative agency?"));
    newTag.add(new MarcSubfieldIndicator('e', "???", NR, "???"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(DOLLAR_X);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("683", "Anweisung für die Notationssynthese", R,
      "Application Instruction Note", "", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(new MarcSubfieldIndicator('b', "Basisnummer???", NR, "Base number???"));
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "Zerlegte Nummer??", R, "Divided like number"));
    newTag.add(new MarcSubfieldIndicator('e', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('f', "Hierarchie--Ende der Spanne?", R,
      "Classification number hierarchy--ending number of span?"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(new MarcSubfieldIndicator('n', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('r', "Wurzel-Nummer???", R, "Root number???"));
    newTag.add(DOLLAR_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag =
      new MarcTag("684", "Auxiliary Instruction Note", R, "Auxiliary Instruction Note", "", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('e', "???", NR, "???"));
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_J);
    newTag.add(DOLLAR_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("685", "Fußnote zur Anwendungsgeschichte", R, "History Note",
      "Resultat der Änderung", "Typ der Information");
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(new MarcSubfieldIndicator('b', "Vorherige Nummer oder Beginn der Spanne", R,
      "Previous number-single number or beginning number of span"));
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_D_DATUM);
    newTag.add(new MarcSubfieldIndicator('e', "Datum der lokalen Implementierung", NR,
      "Local implementation date"));
    newTag.add(
      new MarcSubfieldIndicator('f', "Datum der Publikation", NR, "Title and publication date"));
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(DOLLAR_X);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("689", "Fußnote ?", R, "", "Resultat der Änderung", "Typ der Information");
    addTag(newTag);
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_2);
    newTag.add(MarcDB.DOLLAR_9);

    newTag =
      new MarcTag("694", "Fußnote ??", R, "", "Resultat der Änderung", "Typ der Information");
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('e', "Datum der lokalen Implementierung", NR,
      "Local implementation date"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(new MarcSubfieldIndicator('n', "???", NR, ""));
    newTag.add(INDICATOR_253_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(MarcDB.DOLLAR_9);

    newTag =
      new MarcTag("695", "Fußnote ???", R, "", "Resultat der Änderung", "Typ der Information");
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(DOLLAR_I);
    newTag.add(MarcDB.DOLLAR_9);

    newTag =
      new MarcTag("698", "Fußnote ???", R, "", "Resultat der Änderung", "Typ der Information");
    addTag(newTag);
    newTag.add(INDICATOR_153_A);

    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_D_DATUM);
    newTag.add(new MarcSubfieldIndicator('e', "Datum der lokalen Implementierung?", NR,
      "Local implementation date"));
    newTag.add(
      new MarcSubfieldIndicator('f', "Datum der Publikation?", NR, "Title and publication date"));
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(new MarcSubfieldIndicator('u', "???", R, ""));
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_2);
    newTag.add(DOLLAR_8);
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("700", "Person – Indexeintrag", R, "Index Term - Personal Name",
      "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(authorityDB.getTag("100"));
    connet7XX(newTag);

    newTag = new MarcTag("710", "Körperschaft – Indexeintrag", R, "Index Term - Corporate Name",
      "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(authorityDB.getTag("110"));
    connet7XX(newTag);

    newTag = new MarcTag("711", "Kongress – Indexeintrag", R, "Index Term - Meeting Name",
      "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(authorityDB.getTag("111"));
    connet7XX(newTag);

    newTag = new MarcTag("730", "Einheitstitel – Indexeintrag", R, "Index Term - Uniform Title",
      "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(authorityDB.getTag("130"));
    connet7XX(newTag);

    newTag = new MarcTag("748", "Zeit – Indexeintrag", R, "Index Term - Chronological", "Thesaurus",
      "Thesaurus");
    addTag(newTag);
    newTag.add(MARCAuthorityDB.MarcSubfieldIndicator_548_A);
    newTag.add(DOLLAR_X);
    newTag.add(MARCAuthorityDB.MarcSubfieldIndicator_151_Z);
    connet7XX(newTag);

    newTag = new MarcTag("750", "Sachbegriff – Indexeintrag", R, "Index Term - Topical Term",
      "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(authorityDB.getTag("150"));
    connet7XX(newTag);

    newTag = new MarcTag("751", "Geografikum – Indexeintrag", R, "Index Term - Geographic Name",
      "Thesaurus", "Thesaurus");
    addTag(newTag);
    newTag.addInherited(authorityDB.getTag("151"));
    connet7XX(newTag);

    newTag =
      new MarcTag("761", "Anhängeanweisung", NR, "Add or Divide Like Instructions", null, "Typ");
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(DOLLAR_B_BASE);
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "Zerlegte Nummer", R, "Divided like number"));
    newTag.add(DOLLAR_E_BEISP);
    newTag.add(DOLLAR_F_FACETT);
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_N_GEGENBEISP);
    newTag.add(DOLLAR_R_WURZEL);
    newTag.add(INDICATOR_253_T);
    newTag.add(DOLLAR_X_ANDERE);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(DOLLAR_9);

    newTag = new MarcTag("763", "Interne Anhängetafel", R,
      "Internal Subarrangement or Add Table Entry", "Validität", "Typ");
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(DOLLAR_B_BASE);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_X_ANDERE);

    newTag = new MarcTag("765", "Komponenten der synthetisierten Nummer", R,
      "Synthesized Number Components", "Field of number analyzed", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(new MarcSubfieldIndicator('b', "Basisnummer", R, "Base number"));
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_F_FACETT);
    newTag.add(DOLLAR_R_WURZEL);
    newTag.add(new MarcSubfieldIndicator('s',
      "Digits added from classification number in schedule or external table", R,
      "Digits added from classification number in schedule or external table"));
    newTag.add(new MarcSubfieldIndicator('t', "Angehängte Ziffern", R,
      "Digits added from internal subarrangement or add table"));
    newTag.add(new MarcSubfieldIndicator('u', "Analysierte Nummer", R, "Number being analyzed"));
    newTag.add(new MarcSubfieldIndicator('v',
      "Number in internal subarrangement or add table where instructions are found", R,
      "Number in internal subarrangement or add table where instructions are found"));
    newTag.add(
      new MarcSubfieldIndicator('w', "Table identification-Internal subarrangement or add table", R,
        "Table identification-Internal subarrangement or add table"));
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(DOLLAR_9);

    newTag = new MarcTag("767", "???", R, "???", "Type der Anweisung?", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(new MarcSubfieldIndicator('d', "???", R, "???"));
    newTag.add(DOLLAR_E_BEISP);
    newTag.add(DOLLAR_G);
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_N_GEGENBEISP);
    newTag.add(INDICATOR_253_T);
    newTag.add(new MarcSubfieldIndicator('x', "Ausnahme der Vorzugsreihenfolge", R,
      "Exception to table of preference"));
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(DOLLAR_9);

    newTag = new MarcTag("768", "Anweisungen über Vorzugsreihenfolge", R,
      "Citation and Preference Order Instructions", "Type der Anweisung?", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_E_BEISP);
    newTag.add(DOLLAR_I);
    newTag.add(DOLLAR_J);
    newTag.add(DOLLAR_N_GEGENBEISP);
    newTag.add(INDICATOR_253_T);
    newTag.add(new MarcSubfieldIndicator('x', "Ausnahme der Vorzugsreihenfolge", R,
      "Exception to table of preference"));
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(DOLLAR_9);

    newTag = new MarcTag("769", "???", R, "???", "Type der Anweisung?", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(INDICATOR_Y_SEQUENZ);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(DOLLAR_9);

    newTag = new MarcTag("795", "???", R, "???", "Type der Anweisung?", null);
    addTag(newTag);
    newTag.add(INDICATOR_153_A);
    newTag.add(INDICATOR_153_C);
    newTag.add(DOLLAR_I);
    newTag.add(INDICATOR_253_T);
    newTag.add(MarcDB.INDICATOR_DDC_Z);
    newTag.add(DOLLAR_8);
    newTag.add(DOLLAR_9);

    newTag = new MarcTag("883", "Computergenerierte Provenienz", R,
      "Machine-generated Metadata Provenance", "Methode", null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Prozess", NR, "Generation process"));
    newTag.add(new MarcSubfieldIndicator('c', "Konfidenzwert", NR, " Confidence value"));
    newTag.add(DOLLAR_D_DATUM);
    newTag.add(new MarcSubfieldIndicator('q', "Agentur", NR, "Generation agency"));
    newTag.add(new MarcSubfieldIndicator('x', "Ende der Gültigkeit", NR, "Validity end date"));
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("953", "???", NR, "", null, "");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('c', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('d', "???", NR, "???"));
    newTag.add(MarcDB.DOLLAR_9);

    newTag = new MarcTag("990", "Interner Hinweis", NR, "", null, "");
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('d', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('i', "???", NR, "???"));
    newTag.add(new MarcSubfieldIndicator('t', "???", NR, "???"));
    newTag.add(MarcDB.DOLLAR_9);

  }

  /**
   * @param newTag
   */
  private void connet7XX(final MarcTag newTag) {
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag.add(MarcDB.DOLLAR_9);
  }

}
