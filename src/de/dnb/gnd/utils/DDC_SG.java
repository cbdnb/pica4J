/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.dnb.basics.filtering.Between;

/**
 * Sachgruppen als Tupel von (Nr, Beschreibung, Intervalle, Referat).
 *
 * @author baumann
 *
 */
public enum DDC_SG implements SG {

  //@formatter:off
	SG_000("000", "Allgemeines, Wissenschaft", "000-003"),
	SG_004("004", "Informatik", "004-006"),
	SG_010("010", "Bibliografien", "010"),
	SG_020("020", "Bibliotheks- und Informationswissenschaft", "020"),
	SG_030("030", "Enzyklopädien", "030"),
	SG_050("050", "Zeitschriften, fortlaufende Sammelwerke", "050"),
	SG_060("060", "Organisationen, Museumswissenschaft", "060"),
	SG_070("070", "Nachrichtenmedien, Journalismus, Verlagswesen", "070"),
	SG_080("080", "Allgemeine Sammelwerke", "080"),
	SG_090("090", "Handschriften, seltene Bücher", "090"),
	SG_100("100", "Philosophie", "100-120, 140, 160-190"),
	SG_130("130", "Parapsychologie, Okkultismus", "130"),
	SG_150("150", "Psychologie", "150"),
	SG_200("200", "Religion, Religionsphilosophie", "200, 210"),
	SG_220("220", "Bibel", "220"),
	SG_230("230", "Theologie, Christentum", "230-280"),
	SG_290("290", "Andere Religionen", "290"),
	SG_300("300", "Sozialwissenschaften, Soziologie, Anthropologie", "300"),
	SG_310("310", "Allgemeine Statistiken", "310"),
	SG_320("320", "Politik", "320"),
	SG_330("330", "Wirtschaft", "330"),
	SG_333_7("333.7", "Natürliche Ressourcen, Energie und Umwelt", "333.7-333.9"),
	SG_340("340", "Recht", "340"),
	SG_350("350", "Öffentliche Verwaltung", "350-354"),
	SG_355("355", "Militär", "355-359"),
	SG_360("360", "Soziale Probleme, Sozialdienste, Versicherungen", "360"),
	SG_370("370", "Erziehung, Schul- und Bildungswesen", "370"),
	SG_380("380", "Handel, Kommunikation, Verkehr", "380"),
	SG_390("390", "Bräuche, Etikette, Folklore", "390"),
	SG_400("400", "Sprache, Linguistik", "400, 410"),
	SG_420("420", "Englisch", "420"),
	SG_430("430", "Deutsch", "430"),
	SG_439("439", "Andere germanische Sprachen", "439"),
	SG_440("440", "Französisch, romanische Sprachen allgemein", "440"),
	SG_450("450", "Italienisch, Rumänisch, Rätoromanisch", "450"),
	SG_460("460", "Spanisch, Portugiesisch", "460"),
	SG_470("470", "Latein", "470"),
	SG_480("480", "Griechisch", "480"),
	SG_490("490", "Andere Sprachen", "490"),
	SG_491_8("491.8", "Slawische Sprachen", "491.7-491.8"),
	SG_500("500", "Naturwissenschaften", "500"),
	SG_510("510", "Mathematik", "510"),
	SG_520("520", "Astronomie, Kartografie", "520"),
	SG_530("530", "Physik", "530"),
	SG_540("540", "Chemie", "540"),
	SG_550("550", "Geowissenschaften", "550"),
	SG_560("560", "Paläontologie", "560"),
	SG_570("570", "Biowissenschaften, Biologie", "570"),
	SG_580("580", "Pflanzen (Botanik)", "580"),
	SG_590("590", "Tiere (Zoologie)", "590"),
	SG_600("600", "Technik", "600"),
	SG_610("610", "Medizin, Gesundheit", "610"),
	SG_620("620", "Ingenieurwissenschaften und Maschinenbau", "620, 621 (außer 621.3 und 621.46), 623, 625.19, 625.2, 629 (außer 629.8)"),
	SG_621_3("621.3", "Elektrotechnik, Elektronik", "621.3, 621.46, 629.8"),
	SG_624("624", "Ingenieurbau und Umwelttechnik", "622, 624-628 (außer 625.19 und 625.2)"),
	SG_630("630", "Landwirtschaft, Veterinärmedizin", "630"),
	SG_640("640", "Hauswirtschaft und Familienleben", "640"),
	SG_650("650", "Management", "650"),
	SG_660("660", "Technische Chemie", "660"),
	SG_670("670", "Industrielle und handwerkliche Fertigung", "670, 680"),
	SG_690("690", "Hausbau, Bauhandwerk", "690"),
	SG_700("700", "Künste, Bildende Kunst allgemein", "700"),
	SG_710("710", "Landschaftsgestaltung, Raumplanung", "710"),
	SG_720("720", "Architektur", "720"),
	SG_730("730", "Plastik, Numismatik, Keramik, Metallkunst", "730"),
	SG_740("740", "Grafik, angewandte Kunst", "740"),
	SG_741_5("741.5", "Comics, Cartoons, Karikaturen", "741.5"),
	SG_750("750", "Malerei", "750"),
	SG_760("760", "Druckgrafik, Drucke", "760"),
	SG_770("770", "Fotografie, Video, Computerkunst", "770"),
	SG_780("780", "Musik", "780"),
	SG_790("790", "Freizeitgestaltung, Darstellende Kunst", "790-790.2"),
	SG_791("791", "Öffentliche Darbietungen, Film, Rundfunk", "791"),
	SG_792("792", "Theater, Tanz", "792"),
	SG_793("793", "Spiel", "793-795"),
	SG_796("796", "Sport", "796-799"),
	SG_800("800", "Literatur, Rhetorik, Literaturwissenschaft", "800"),
	SG_810("810", "Englische Literatur Amerikas", "810"),
	SG_820("820", "Englische Literatur", "820"),
	SG_830("830", "Deutsche Literatur", "830"),
	SG_839("839", "Literatur in anderen germanischen Sprachen", "839"),
	SG_840("840", "Französische Literatur", "840"),
	SG_850("850", "Italienische, rumänische, rätoromanische Literatur", "850"),
	SG_860("860", "Spanische und portugiesische Literatur", "860"),
	SG_870("870", "Lateinische Literatur", "870"),
	SG_880("880", "Griechische Literatur", "880"),
	SG_890("890", "Literatur in anderen Sprachen", "890"),
	SG_891_8("891.8", "Slawische Literatur", "891.7-891.8"),
	SG_900("900", "Geschichte", "900"),
	SG_910("910", "Geografie, Reisen", "910"),
	SG_914_3("914.3", "Geografie, Reisen (Deutschland)", "914.3-914.35"),
	SG_920("920", "Biografie, Genealogie, Heraldik", "920"),
	SG_930("930", "Alte Geschichte, Archäologie", "930"),
	SG_940("940", "Geschichte Europas", "940"),
	SG_943("943", "Geschichte Deutschlands", "943-943.5"),
	SG_950("950", "Geschichte Asiens", "950"),
	SG_960("960", "Geschichte Afrikas", "960"),
	SG_970("970", "Geschichte Nordamerikas", "970"),
	SG_980("980", "Geschichte Südamerikas", "980"),
	SG_990("990", "Geschichte der übrigen Welt", "990"),
	SG_B("B", "Belletristik", ""),
	SG_K("K", "Kinder- und Jugendliteratur", ""),
	SG_S("S", "Schulbücher", "");

	//@formatter:on

  /**
   * Die Felder, die vom Konstruktor befüllte werden:
   */
  private final String dnb_ddc;
  private final String description;
  private final Collection<Between<String>> intervals;
  private final REFERATE referat;
  private final HUNDERTER hunderter;

  /**
   * @return Die DDC-Sachgruppe der DNB (z.B. '530').
   */
  @Override
  public String getDDCString() {
    return dnb_ddc;
  }

  /**
   * @return Die Beschreibung (z.B. 'Geschichte Europas')
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @return die Intervalle, die eine Sachgruppe umfasst, z.B. bei 624
   */
  public Collection<Between<String>> getIntervals() {
    return intervals;
  }

  public REFERATE getReferat() {
    return referat;
  }

  public HUNDERTER getHunderter() {
    return hunderter;
  }

  public enum REFERATE {
      SOCIAL_SCIENCES("Sozialwissenschaften"), HUMANITIES("Geisteswissenschaften"), STM("STM");

    public String name;

    REFERATE(final String name) {
      this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return name;
    }
  }

  public enum HUNDERTER {
    //@formatter:off

    HU_000("000", "Allgemeines, Informatik, Informationswissenschaft"),
    HU_100("100", "Philosophie und Psychologie "),
    HU_200("200", "Religion"),
    HU_300("300", "Sozialwissenschaften"),
    HU_400("400", "Sprache"),
    HU_500("500", "Naturwissenschaften und Mathematik"),
    HU_600("600", "Technik, Medizin, angewandte Wissenschaften"),
    HU_700("700", "Künste und Unterhaltung"),
    HU_800("800", "Literatur"),
    HU_900("900", "Geschichte und Geografie"),
    HU_B("B", "Belletristik"),
    HU_K("K", "Kinder- und Jugendliteratur"),
    HU_S("S", "Schulbücher");


    //@formatter:on
    private final String nummer;
    private final String description;

    HUNDERTER(final String nummer, final String description) {
      this.nummer = nummer;
      this.description = description;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return nummer + ": " + description;
    }

    /**
    *
    * @return Die Hunderter in der richtigen Reihenfolge
    */
    public static Set<HUNDERTER> enumSet() {
      return EnumSet.allOf(HUNDERTER.class);
    }

  }

  private HUNDERTER getHunderterP(final String nummer) {
    for (final HUNDERTER hunderter : HUNDERTER.enumSet()) {
      if (hunderter.nummer.equals(nummer))
        return hunderter;
    }
    return null;
  }

  public HUNDERTER getHunderter(final String dhs) {

    final char first = dhs.charAt(0);
    if (Character.isDigit(first))
      return getHunderterP(first + "00");

    if (first == 'B')
      return HUNDERTER.HU_B;
    if (first == 'S')
      return HUNDERTER.HU_S;
    if (first == 'K')
      return HUNDERTER.HU_K;
    return null;
  }

  /**
   * STM- Fächer.
   */
  private static Collection<String> stmStr = null;
  /**
   * Sozialwissenschaften.
   */
  private static Collection<String> socStr = null;
  /**
   * Geisteswissenschaften.
   */
  private static Collection<String> humStr = null;

  private static void fillHumanities() {
    if (humStr == null) {
      humStr = Arrays.asList("000", "010", "020", "030", "050", "060", "070", "080", "090",

        "100", "130",

        "200", "220", "230", "290",

        "390",

        "400", "420", "430", "439", "440", "450", "460", "470", "480", "490", "491.8",

        "700", "710", "720", "730", "740", "741.5", "750", "760", "770", "780", "790", "791", "792",
        "793", "796",

        "800", "810", "820", "830", "839", "840", "850", "860", "870", "880", "890", "891.8",

        "900", "920", "930", "940", "943", "950", "960", "970", "980", "990",

        "B", "K");
    }
  }

  private static void fillSoc() {
    if (socStr == null) {
      socStr = Arrays.asList("150",

        "300", "310", "320", "330", "333.7", "340", "350", "355", "360", "370", "380",

        "640", "650",

        "910", "914.3",

        "S");
    }
  }

  private static void fillStm() {
    if (stmStr == null) {
      stmStr = Arrays.asList("004",

        "500", "510", "520", "530", "540", "550", "560", "570", "580", "590",

        "600", "610", "620", "621.3", "624", "630", "660", "670", "690"

      );
    }
  }

  private static boolean isHum(final String ddc) {
    fillHumanities();
    return humStr.contains(ddc);
  }

  private static boolean isSoc(final String ddc) {
    fillSoc();
    return socStr.contains(ddc);
  }

  private static boolean isStm(final String ddc) {
    fillStm();
    return stmStr.contains(ddc);
  }

  /**
   *
   * @return  eine veränderbare, sortierbare Liste
   */
  public static List<DDC_SG> getSTM() {
    fillStm();
    return stmStr.stream().map(DDC_SG::getSG).collect(Collectors.toList());
  }

  /**
   * @param ddc          die Nummer der DNB
   * @param descriptionS Beschreibung
   * @param intervalStr  Intervalle
   */
  DDC_SG(final String ddc, final String descriptionS, final String intervalStr) {
    dnb_ddc = ddc;
    description = descriptionS;
    intervals = getDDCIntervalls(intervalStr);
    if (isHum(ddc))
      referat = REFERATE.HUMANITIES;
    else if (isSoc(ddc))
      referat = REFERATE.SOCIAL_SCIENCES;
    else if (isStm(ddc))
      referat = REFERATE.STM;
    else
      throw new IllegalStateException("kein Referat?");

    hunderter = getHunderter(ddc);
  }

  /* (non-Javadoc)
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return "<" + dnb_ddc + ", " + description + ", " + referat + ">";
  }

  public final static String END = "z";
  private final static String numberPat = "\\d\\d\\d(\\.\\d+)?";
  private static Pattern singlePattern;
  private final static String spanPat = "(" + numberPat + ")-(" + numberPat + ")";
  private static Pattern spanPattern;

  /**
   * @param intervalStr   verbale Beschreibung der Intervalle
   * @return              zur SG gehörige Intervalle in der Form
   *                      [<622 .. 622z>, <624 .. 628z>]
   */
  private Collection<Between<String>> getDDCIntervalls(final String intervalStr) {
    final Collection<Between<String>> betweens = new LinkedList<>();
    final String removed = removeAusser(intervalStr);
    final String[] strings = removed.split(", ");
    for (final String string : strings) {
      final Between<String> between = getDDCIntervall(string);
      if (between != null)
        betweens.add(between);
    }
    return betweens;
  }

  /**
   * Entfernt die Phrase, die durch "(außer .. )" die Ausnahmen kennzeichnet.
   *
   * @param s Phrase
   * @return  Phrase ohne Ausnahmen (diese Ausnahmen folgen in
   *          der Aufzählung der Intervalle ohnehin noch)
   */
  private static String removeAusser(final String s) {
    final String stripped = s.replaceAll(" \\(außer [^)]*\\)", "");
    return stripped;
  }

  /**
   * Macht
   *
   * a) aus einer einzelnen Nummer ein Intervall, das mit der Nummer beginnt
   * und mit dem nächsten Hunderter endet b) aus einem Bereich (###-###) das
   * entsprechende Intervall
   *
   * @param s in der Form 624-628 oder 741.5s
   * @return  Intervall
   */
  private static Between<String> getDDCIntervall(final String s) {
    String low;
    String high;

    if (singlePattern == null)
      singlePattern = Pattern.compile(numberPat);
    final Matcher singeleMatcher = singlePattern.matcher(s);
    if (singeleMatcher.matches()) {
      low = s;
      high = makeUpperBound(s);
      return new Between<String>(low, high);
    }

    if (spanPattern == null)
      spanPattern = Pattern.compile(spanPat);
    final Matcher spanMatcher = spanPattern.matcher(s);
    if (spanMatcher.matches()) {
      low = spanMatcher.group(1);
      // 3, da in der Klammer noch Klammern stehen!
      high = makeUpperBound(spanMatcher.group(3));
      return new Between<String>(low, high);
    }
    return null;
  }

  /**
   * Macht eine obere Schranke durch (eventuelles) Entfernen der letzten 0 und
   * Anhängen des Ende-Zeichens. Letzteres, um halboffene Intervalle zu
   * erzeugen.
   *
   * @param s in der Form 741.5
   * @return  741.5z
   */
  private static String makeUpperBound(final String s) {
    return s.replaceFirst("0$", "") + END;
  }

  /**
   *
   * @return Die Sachgruppen in der richtigen Reihenfolge
   */
  public static EnumSet<DDC_SG> enumSet() {
    return EnumSet.allOf(DDC_SG.class);
  }

  /**
   * einfache Datenstruktur, um Strings statt enum
   * verwenden zu können.
   */
  private static Map<String, DDC_SG> ddc2enum = null;

  /**
   * Vorsicht: Die Sachgruppen des DMA werden zur Zeit alle auf 780
   * gemapt!!!
   *
   * @param ddc	auch null
   * @return		Sachgruppe (SG), null, wenn nichts gefunden
   */
  public static DDC_SG getSG(final String ddc) {
    // Shortcut für Musik:
    if (ddc != null && ddc.startsWith("78"))
      return SG_780;
    if (ddc2enum == null) {
      ddc2enum = new TreeMap<>();
      enumSet().forEach(sg ->
      {
        ddc2enum.put(sg.dnb_ddc, sg);
      });
    }
    return ddc2enum.get(ddc);
  }

  public static void main(final String[] args) {

    System.out.println(SG_333_7.getHunderter());

  }

}
