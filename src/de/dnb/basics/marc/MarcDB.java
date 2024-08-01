/**
 *
 */
package de.dnb.basics.marc;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.marc4j.marc.Subfield;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * @author baumann
 *
 */
public abstract class MarcDB {

  public static final Repeatability R = Repeatability.REPEATABLE;

  /**
  *
  */
  public static final MarcTag TAG_336 =
    new MarcTag("336", "Inhaltstyp", R, "Content Type", null, null);

  /**
  *
  */
  public static final MarcTag TAG_337 =
    new MarcTag("337", "Medientyp", R, "MEDIA TYPE", null, null);

  /**
   *
   */
  public static final MarcTag TAG_380 =
    new MarcTag("380", "Form des Werks", R, "Form of work", null, null);
  public static final Repeatability NR = Repeatability.NON_REPEATABLE;

  /**
  *
  */
  public static final MarcTag TAG_382 = new MarcTag("382", "Besetzung im Musikbereich", R,
    "Medium of Performance", "Regelung der Anzeigekonstante", null);

  /**
  *
  */
  public static final MarcTag TAG_383 =
    new MarcTag("383", "Numerische Kennzeichnung eines Musikwerks", R,
      "Numeric Designation of Musical Work", null, null);

  /**
   *
   */
  public static final MarcTag TAG_384 =
    new MarcTag("384", "Tonart", NR, "Key", "Art der Tonart", null);

  protected TreeMap<String, MarcTag> marcMap = new TreeMap<>();
  /**
   *Hilfstafelnummer.
   */
  public static final MarcSubfieldIndicator INDICATOR_DDC_Z =
    new MarcSubfieldIndicator('z', "Hilfstafelnummer", NR, "table number");
  /**
   * Nicht eindeutiger Indikator für $g (Zusatz), R.
   */
  public static final MarcSubfieldIndicator DOLLAR_G =
    new MarcSubfieldIndicator('g', "Zusatz", R, "Qualifier");
  /**
   * Indikator für $5, R.
   * (Institution (=ISIL), die Feld in besonderer Art verwendet).
   */
  public static final MarcSubfieldIndicator DOLLAR_5 = new MarcSubfieldIndicator('5',
    "Herkunft(=ISIL der Institution), ", R, "Institution to which field applies");
  /**
   * Einziger Indikator für $t (Feldzuordnung beim Titel).
   */
  public static final MarcSubfieldIndicator DOLLAR_T =
    new MarcSubfieldIndicator('t', "Titel", NR, "");
  /**
   *Field link and sequence number.
   */
  public static final MarcSubfieldIndicator DOLLAR_8 = new MarcSubfieldIndicator('8',
    "Feldverknüpfung und Reihenfolge", R, "Field link and sequence number");
  /**
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_Z =
    new MarcSubfieldIndicator('z', "Geografische Unterteilung", R, "Geographic subdivision");
  /**
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_Y =
    new MarcSubfieldIndicator('y', "Chronologische Unterteilung", R, "Chronological subdivision");
  /**
   * Nicht eindeutuger Indikator für $x (Allgemeine Unterteilung). Gibt es
   * auch noch für 169.
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_X =
    new MarcSubfieldIndicator('x', "Allgemeine Unterteilung", R, "General subdivision");
  /**
   * URI. R.
   */
  public static final MarcSubfieldIndicator DOLLAR_URI =
    new MarcSubfieldIndicator('u', "Uniform Resource Identifier", R, "");

  /**
   * Personenname.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_100_A =
    new MarcSubfieldIndicator('a', "Personenname", NR, "");

  /**
   * Zählung.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_100_B =
    new MarcSubfieldIndicator('b', "Zählung", NR, "Numeration");

  /**
   * Beiname, Gattungsname, Territorium, Titulatur.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_100_C =
    new MarcSubfieldIndicator('c', "Beiname, Gattungsname, Territorium, Titulatur", NR,
      "Titles and other words associated with a name");

  /**
   * Lebensdaten.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_100_D =
    new MarcSubfieldIndicator('d', "Lebensdaten", NR, "Dates associated with a name");

  /**
   * Hauptkörperschaft.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_110_A = new MarcSubfieldIndicator(
    'a', "Hauptkörperschaft", NR, "Corporate name or jurisdiction name as entry element");

  /**
   * Untergeordnete Körperschaft.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_110_B =
    new MarcSubfieldIndicator('b', "Untergeordnete Körperschaft", R, "Subordinate unit");

  /**
   * Zählung.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_110_N =
    new MarcSubfieldIndicator('n', "Zählung", R, "Number of part/section");

  /**
   * Geografische Unterteilung.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_110_Z =
    new MarcSubfieldIndicator('z', "Geografische Unterteilung", R, "Geographic subdivision");

  /**
   * Hauptkongress.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_111_A = new MarcSubfieldIndicator(
    'a', "Hauptkongress", NR, "Meeting name or jurisdiction name as entry element");

  /**
   * Ort.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_111_C =
    new MarcSubfieldIndicator('c', "Ort", NR, "Location of meeting");

  /**
   * Datum.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_111_D =
    new MarcSubfieldIndicator('d', "Datum", NR, "Date of meeting");

  /**
   * Untergeordnete Einheit. In DACH als $b.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_111_E =
    new MarcSubfieldIndicator('e', "Untergeordnete Einheit", R, "Number");

  /**
   * Zählung.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_111_N =
    new MarcSubfieldIndicator('n', "Zählung", R, "Number of meeting");

  /**
   * Titel eines Werks.
   */
  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_A =
    new MarcSubfieldIndicator('a', "Titel eines Werks", NR, "Uniform title");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_F =
    new MarcSubfieldIndicator('f', "Datum eines Werks", R, "Date of a work");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_H =
    new MarcSubfieldIndicator('h', "Inhaltstyp", NR, "Medium");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_L =
    new MarcSubfieldIndicator('l', "Sprache", NR, "Language of a work");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_M = new MarcSubfieldIndicator(
    'm', "Besetzung im Musikbereich", R, "Medium of performance for music");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_N =
    new MarcSubfieldIndicator('n', "Zählung eines Werks, des Teils/der Abteilung eines Werks", R,
      "Number of part/section of a work");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_O = new MarcSubfieldIndicator(
    'o', "Angabe des Musikarrangements", NR, "Arranged statement for music");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_P = new MarcSubfieldIndicator(
    'p', "Titel des Teils/der Abteilung eines Werkes", R, "Name of part/section of a work");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_R =
    new MarcSubfieldIndicator('r', "Tonart", NR, "Key for music");

  public static final MarcSubfieldIndicator MarcSubfieldIndicator_130_S =
    new MarcSubfieldIndicator('s', "Version", NR, "Version");

  /**
   * Indikator für $4 (GND-Code für Beziehungen). NR.
   */
  public static final MarcSubfieldIndicator DOLLAR_4 =
    new MarcSubfieldIndicator('4', "GND-Code für Beziehungen", R, "Relationship code");

  /**
   * Funktionsbezeichnung.
   */
  public static final MarcSubfieldIndicator DOLLAR_E_RELATOR =
    new MarcSubfieldIndicator('e', "Funktionsbezeichnung", R, "Relator term");

  /**
   * Nicht eindeutuger Indikator für $9 (Bemerkungen etc.).
   *
   */
  public static final MarcSubfieldIndicator DOLLAR_9 =
    new MarcSubfieldIndicator('9', "Bemerkungen/Relevanz/Codes", R, "");

  /**
   * $9:A.
   */
  public static final MarcSubfieldIndicator DOLLAR_9A =
    new MarcSubfieldIndicator('A', "Indikator für Darstellung", R, "", true);

  /**
   * $9:v. Bemerkungen (wie $v). Kein Indikator im eigentlichen Sinne, R.
   * Gehört zu $9.
   */
  public static final MarcSubfieldIndicator DOLLAR_9V_R =
    new MarcSubfieldIndicator('v', "Bemerkungen", R, "", true);

  /**
   * $9:r.
   */
  public static final MarcSubfieldIndicator DOLLAR_9R =
    new MarcSubfieldIndicator('r', "ISIL der Verbundredaktion", NR, "", true);

  /*
   * Indikatoren, die überall gleich auftauchen:
   */
  /**
   * IDN, R.
   */
  public static final MarcSubfieldIndicator DOLLAR_0 =
    new MarcSubfieldIndicator('0', "Datensatzkontrollnummer", R, "Record control number");

  /**
   * Code der Quelle. NR.
   */
  public static final MarcSubfieldIndicator DOLLAR_2 =
    new MarcSubfieldIndicator('2', "Code der Quelle", NR, "");

  /**
  *
  */
  public static final MarcSubfieldIndicator DOLLAR_B =
    new MarcSubfieldIndicator('b', "Inhaltstyp-Code", R, "Content type code");

  public static final Repeatability U = Repeatability.UNKNOWN;

  /**
   *
   */
  protected MarcDB() {
    MarcTag newTag;

    newTag = new MarcTag("000", "Satzkennung", NR, "Leader", null, null);
    addTag(newTag);

    newTag = new MarcTag("001", "Kontrollnummer", NR, "CONTROL NUMBER", null, null);
    addTag(newTag);

    newTag =
      new MarcTag("003", "Kontrollnummer-Identifier", NR, "CONTROL NUMBER IDENTIFIER", null, null);
    addTag(newTag);

    newTag = new MarcTag("005", "Datum der letzen Änderung  ", NR,
      "Date and Time of Latest Transaction", null, null);
    addTag(newTag);

    newTag = new MarcTag("008", "Datenelemente mit fester Länge", NR,
      "Fixed-Length Data Elements--GENERAL INFORMATION", null, null);
    addTag(newTag);

    newTag = new MarcTag("034", "Geografische Koordinaten", R,
      "Coded Cartographic Mathematical Data", null, "Art des Ringes");
    addTag(newTag);
    // $a nur Titeldaten:
    newTag.add(new MarcSubfieldIndicator('a', "Kategorie des Maßstabs", NR, "Category of scale"));
    newTag.add(new MarcSubfieldIndicator('d', "Koordinaten - westlichster Längengrad", NR,
      "Coordinates - westernmost longitude"));
    newTag.add(new MarcSubfieldIndicator('e', "Koordinaten - östlichster Längengrad", NR,
      "Coordinates - easternmost longitude"));
    newTag.add(new MarcSubfieldIndicator('f', "Koordinaten - nördlichster Längengrad", NR,
      "Coordinates - northernmost latitude"));
    newTag.add(new MarcSubfieldIndicator('g', "Koordinaten - südlichster Längengrad", NR,
      "Coordinates - southernmost latitude"));
    newTag.add(new MarcSubfieldIndicator('j', "Deklination - nördliche Grenze", NR, ""));
    newTag.add(new MarcSubfieldIndicator('k', "Deklination - südliche Grenze", NR, ""));
    newTag.add(new MarcSubfieldIndicator('m', "Rektaszension - östliche Grenze", NR, ""));
    newTag.add(new MarcSubfieldIndicator('n', "Rektaszension - westliche Grenze", NR, ""));
    newTag.add(new MarcSubfieldIndicator('p', "Äquinoktium", NR, ""));
    newTag.add(new MarcSubfieldIndicator('r', "Distanz zur Erde", NR, ""));
    newTag.add(new MarcSubfieldIndicator('s', "G-Ring Breitengrad", R, ""));
    newTag.add(new MarcSubfieldIndicator('t', "G-Ring Längengrad", R, ""));
    newTag.add(new MarcSubfieldIndicator('x', "Anfangsdatum", NR, ""));
    newTag.add(new MarcSubfieldIndicator('y', "Enddatum", NR, ""));
    newTag.add(new MarcSubfieldIndicator('z', "Name des extraterrestrischen Körpers", NR, ""));
    newTag.add(new MarcSubfieldIndicator('u', "URI der Web-Ressource", NR, ""));
    newTag.add(new MarcSubfieldIndicator('F', "ISIL der Referenz-Datei", NR, ""));
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);
    newTag
      .add(new MarcSubfieldIndicator('3', "Koordinaten-Spezifikation", NR, "Materials specified"));
    // newTag.add(DOLLAR_9);
    newTag.add(DOLLAR_9A);
    newTag.add(DOLLAR_9V_R);

    newTag = new MarcTag("035", "Identifikationsnummern", R, "System Control Number", null, null);
    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "System-Kontrollnummer", NR, "System Control Number"));
    // nur GND
    newTag.add(new MarcSubfieldIndicator('z', "Alte/ungültige Kontrollnummer", R,
      "Canceled/invalid system control number"));
    newTag.add(DOLLAR_8);
    // nur GND
    newTag.add(DOLLAR_9V_R);

    newTag = new MarcTag("040", "Katalogisierungsquelle", NR, "Cataloging Source", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Original-Katalogisierungsstelle", NR,
      "Original cataloging agency"));
    newTag.add(new MarcSubfieldIndicator('b', "Katalogisierungssprache", NR, ""));
    newTag.add(new MarcSubfieldIndicator('c', "Übertragungsstelle", NR, "Transcribing agency"));
    newTag.add(new MarcSubfieldIndicator('d', "Bearbeitungsstelle", R, "Modifying agency"));
    newTag.add(
      new MarcSubfieldIndicator('e', "Beschreibungsfestlegungen", R, "Description conventions"));
    // nur GND
    newTag.add(new MarcSubfieldIndicator('f', "Schlagwort- oder Thesaurusfestlegungen", NR,
      "Subject heading or thesaurus conventions"));
    // newTag.add(DOLLAR_9);
    // nur GND
    newTag.add(DOLLAR_9R);

    newTag = new MarcTag("043", "Ländercode nach ISO 3166", NR, "Geographic Area Code", null, null);
    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('c', "ISO-code", R, "ISO code"));

    newTag = TAG_336;
    // nicht hier, da nur in Auth und Biblio:
    //    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Inhaltstyp-Term", R, "Content type term"));
    newTag.add(DOLLAR_B);
    newTag.add(DOLLAR_2);
    // nur Titel
    newTag
      .add(new MarcSubfieldIndicator('3', "Spezifische Materialangaben", R, "Materials specified"));
    // nur Titel
    newTag.add(DOLLAR_8);

    newTag = TAG_337;
    // nicht hier, da nur in Auth und Biblio:
    //    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Medientyp in Textform", R, "Media type term"));
    newTag.add(new MarcSubfieldIndicator('b', "Code für Medientyp", R, "Media type code"));
    newTag.add(DOLLAR_2);
    newTag
      .add(new MarcSubfieldIndicator('3', "Spezifische Materialangaben", R, "Materials specified"));
    newTag.add(DOLLAR_8);

    newTag = TAG_380;

    //    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Form des Werks", NR, ""));
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);

    newTag = TAG_382;
    // nicht hier, da nur in Auth und Biblio:
    //    addTag(newTag);
    newTag
      .add(new MarcSubfieldIndicator('a', "Darstellungsmedium (Term)", R, "Medium of performance"));
    newTag.add(new MarcSubfieldIndicator('b', "Solist", R, "Soloist"));
    newTag.add(new MarcSubfieldIndicator('d', "Zusätzliches Instrument", R, "Doubling instrument"));
    newTag.add(new MarcSubfieldIndicator('e', "Anzahl der Ensembles derselben Art", R,
      "Number of ensembles of the same type"));
    newTag.add(new MarcSubfieldIndicator('n', "Besetzungsstärke", R,
      "Number of performers of the same medium"));
    newTag.add(new MarcSubfieldIndicator('p', "Alternative Besetzung", R,
      "Alternative medium of performance"));
    newTag.add(
      new MarcSubfieldIndicator('s', "Gesamtbesetzungsstärke", NR, "Total number of performers"));
    newTag.add(new MarcSubfieldIndicator('t', "Gesamtanzahl der Ensembles", NR,
      "Total number of ensembles"));
    newTag.add(new MarcSubfieldIndicator('v', "Bemerkung", R, "Note"));
    newTag.add(DOLLAR_0);
    newTag.add(DOLLAR_2);

    newTag = TAG_383;
    // nicht hier, da nur in Auth und Biblio:
    //    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Fortlaufende Zählung", R, "Serial number"));
    newTag.add(new MarcSubfieldIndicator('b', "Opus-Zählung", R, "Opus number"));
    newTag.add(new MarcSubfieldIndicator('c', "Zählung eines Werkverzeichnisses", R,
      "Thematic index number"));

    newTag = TAG_384;
    // nicht hier, da nur in Auth und Biblio:
    //    addTag(newTag);
    newTag.add(new MarcSubfieldIndicator('a', "Tonart des Werks", NR, "Key"));
  }

  /**
   * @return
   */
  public List<MarcTag> getTags() {
    return new LinkedList<MarcTag>(marcMap.values());

  }

  /**
   * @param newTag
   */
  protected void addTag(final MarcTag newTag) {
    marcMap.put(newTag.marc, newTag);

  }

  /**
   *
   * @param tag
   *            nicht null
   * @param code
   *            beliebig
   * @return Indikator oder null
   */
  public MarcSubfieldIndicator getSubfieldIndicator(final String tag, final char code) {
    final MarcTag marcTag = getTag(tag);
    if (marcTag == null)
      return null;
    return marcTag.getSubfieldIndicator(code);
  }

  /**
   * Extrahiert die Daten, die in DACH relevant sind. Im Falle von $9 wird
   * weiter ausgewertet.
   *
   * @param tag
   *            nicht null
   * @param subfield
   *            nicht null
   * @return Paar aus der Indikatorbeschreibung und den Daten nicht null. Im
   *         Fehlerfalle können eine oder zwei Komponenten null sein.
   */
  public
    Pair<MarcSubfieldIndicator, String>
    getRealData(final String tag, final Subfield subfield) {
    Objects.requireNonNull(tag);
    Objects.requireNonNull(subfield);
    char code = subfield.getCode();
    // data enthält die Daten, oder aber
    // 1. den DACH-Indikator an pos. 0 und 2. danach die Daten
    String data = subfield.getData();
    MarcSubfieldIndicator indicator = getSubfieldIndicator(tag, code);
    // eventuell gibt es doch einen $9-Indikator, obwohl das eine lokale
    // Anwendung
    // ist.
    // Dann wird nichts getan:
    if (code == '9' && StringUtils.charAt(data, 1) == ':') {
      // Umdefinition
      code = StringUtils.charAt(data, 0);
      indicator = getSubfieldIndicator(tag, code);
      // Wegen Unterfeldinhalt wie "t:2015-09-16"
      data = StringUtils.substring(data, 2);
    }
    if (code == '9' && indicator == null) {
      //      // Umdefinition
      //      code = StringUtils.charAt(data, 0);
      //      indicator = getSubfieldIndicator(tag, code);
      //      // Wegen Unterfeldinhalt wie "t:2015-09-16"
      //      data = StringUtils.substring(data, 2);
    }
    return new Pair<MarcSubfieldIndicator, String>(indicator, data);
  }

  /**
   *
   * @param tag
   *            nicht null
   * @return Tag oder null.
   */
  public final MarcTag getTag(final String tag) {
    RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
    return marcMap.get(tag);
  }

  /**
   * Gibt eine Teilmenge der Tags, die zwischen from (einschließlich) und to
   * (einschließlich) liegen.
   *
   * @param from
   *            nicht null, nicht leer.
   * @param to
   *            nicht null, nicht leer.
   * @return nicht null, nicht modifizierbar.
   */
  public final Collection<MarcTag> getTagsBetween(final String from, final String to) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("from", from);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("to", to);
    final NavigableMap<String, MarcTag> subMap = marcMap.subMap(from, true, to, true);
    return Collections.unmodifiableCollection(subMap.values());
  }

}
