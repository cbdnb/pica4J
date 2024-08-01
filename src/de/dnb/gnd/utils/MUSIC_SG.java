/**
 *
 */
package de.dnb.gnd.utils;

import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.Between.Type;

/**
 * Musik-Sachgruppen als Tupel von (Nr, Beschreibung).
 *
 * @author baumann
 *
 */
public enum MUSIC_SG implements SG {
  //@formatter:off
  MSG_780("780", "Musik allgemein"),
  MSG_780_7("780.7", "Unterrichtswerke"),
  MSG_780_9("780.9", "Musik allgemein mit zeitlichem, regionalem oder biografischem Bezug"),

  MSG_781("781", "Musik nach Gattungen"),
  MSG_781_54("781.54", "Hintergrund- und Stimmungsmusik"),
  MSG_781_542("781.542", "Filmmusik"),
  MSG_781_556("781.556", "Ballettmusik"),
  MSG_781_62("781.62", "Volksmusik"),
  MSG_781_64("781.64", "Unterhaltungsmusik"),
  MSG_781_642("781.642", "Countrymusik"),
  MSG_781_643("781.643", "Blues und Soul"),
  MSG_781_646("781.646", "Reggae"),
  MSG_781_648("781.648", "Electronica"),
  MSG_781_649("781.649", "Rap"),
  MSG_781_65("781.65", "Jazz"),
  MSG_781_66("781.66", "Rockmusik"),
  MSG_781_68("781.68", "Klassische Musik"),

  MSG_782("782", "Vokalmusik"),
  MSG_782_1("782.1", "Musikalische Bühnenwerke"),
  MSG_782_22("782.22", "Geistliche Vokalmusik"),
  MSG_782_25("782.25", "Geistliche Lieder"),
  MSG_782_4("782.4", "Weltliche Vokalmusik"),
  MSG_782_5("782.5", "Chormusik für gemischte Stimmen"),
  MSG_782_6("782.6", "Chormusik für Frauenstimmen"),
  MSG_782_7("782.7", "Chormusik für Kinder- und Jugendstimmen"),
  MSG_782_8("782.8", "Chormusik für Männerstimmen"),

  MSG_783("783", "Vokalmusik für Einzelstimmen"),

  MSG_784("784", "Orchestermusik"),
  MSG_784_23("784.23", "Orchester mit Soloinstrument(en)"),
  MSG_784_8("784.8", "Blasorchester"),

  MSG_785("785", "Kammermusik"),
  MSG_785_12("785.12", "Duos"),
  MSG_785_13("785.13", "Trios"),
  MSG_785_14("785.14", "Quartette"),
  MSG_785_15("785.15", "Quintette und mehr"),

  MSG_786("786", "Tasten- und Schlaginstrumente, mechanische und elektronische Instrumente"),
  MSG_786_2("786.2", "Klavier"),
  MSG_786_4("786.4", "Cembalo"),
  MSG_786_5("786.5", "Orgel"),
  MSG_786_6("786.6", "mechanische Instrumente"),
  MSG_786_7("786.7", "elektronische Instrumente"),
  MSG_786_8("786.8", "Schlaginstrumente"),

  MSG_787("787", "Saiteninstrumente"),
  MSG_787_2("787.2", "Violine"),
  MSG_787_3("787.3", "Viola"),
  MSG_787_4("787.4", "Violoncello"),
  MSG_787_5("787.5", "Kontrabass"),
  MSG_787_6("787.6", "andere Streichinstrumente"),
  MSG_787_8("787.8", "Zupfinstrumente"),
  MSG_787_83("787.83", "Laute"),
  MSG_787_84("787.84", "Mandoline"),
  MSG_787_87("787.87", "Gitarre"),
  MSG_787_9("787.9", "Harfe"),

  MSG_788("788", "Blasinstrumente"),
  MSG_788_3("788.3", "Flöten"),
  MSG_788_32("788.32", "Querflöte"),
  MSG_788_36("788.36", "Blockflöte"),
  MSG_788_4("788.4", "Rohrblattinstrumente"),
  MSG_788_52("788.52", "Oboe"),
  MSG_788_58("788.58", "Fagott"),
  MSG_788_62("788.62", "Klarinette"),
  MSG_788_7("788.7", "Saxophon"),
  MSG_788_8("788.8", "Akkordeon, Mundharmonika"),
  MSG_788_92("788.92", "Trompete"),
  MSG_788_93("788.93", "Posaune"),
  MSG_788_94("788.94", "Horn"),
  MSG_788_96("788.96", "Kornett"),
  MSG_788_97("788.97", "Flügelhorn"),
  MSG_788_98("788.98", "Tuba"),
  MSG_788_99("788.99", "andere Blechblasinstrumente");


  //@formatter:on

  /**
   * Die Felder, die vom Konstruktor befüllte werden:
   */
  private final String musicDDC;
  private final String description;

  /**
   * @return Die DDC-Sachgruppe der DNB (z.B. '783').
   */
  @Override
  public String getDDCString() {
    return musicDDC;
  }

  /**
   * @return Die Beschreibung (z.B. 'Vokalmusik für Einzelstimmen')
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Konstruktor.
   *
   * @param ddc          die Nummer der DNB
   * @param descriptionS Beschreibung
   */
  MUSIC_SG(final String ddc, final String descriptionS) {
    musicDDC = ddc;
    description = descriptionS;
  }

  /* (non-Javadoc)
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return "<" + musicDDC + ", " + description + ">";
  }

  /**
   * einfache Datenstruktur, um Strings statt enum
   * verwenden zu können.
   */
  private static TreeMap<String, MUSIC_SG> ddc2enum = null;

  static {
    ddc2enum = new TreeMap<>();
    enumSet().forEach(sg ->
    {
      ddc2enum.put(sg.musicDDC, sg);
    });
  }

  /**
   *
   * @return Die Sachgruppen in der richtigen Reihenfolge
   */
  public static Set<MUSIC_SG> enumSet() {
    return EnumSet.allOf(MUSIC_SG.class);
  }

  /**
   * zur Überprüfung, ob zur Musik gehörig:
   */
  private static Between<Float> musicNumbers = new Between<>(780f, 790f, Type.HALF_OPEN);

  /**
   * Übergeordnete Musik-Sachgruppe zur DDC-Nummer im Bereich der Sachgruppe.
   *
   * @param ddc	auch null
   * @return		Sachgruppe (SG); null, wenn nichts gefunden oder im falschen Bereich.
   *            ddc muss als Float formatiert sein, sonst wird null zurückgegeben.
   */
  public static MUSIC_SG getBestSG(final String ddc) {
    try {
      if (!musicNumbers.test(Float.parseFloat(ddc)))
        return null;
    } catch (NumberFormatException | NullPointerException e) {
      return null;
    }

    final Entry<String, MUSIC_SG> floorEntry = ddc2enum.floorEntry(ddc);
    if (floorEntry == null)
      return null;
    return floorEntry.getValue();
  }

  /**
   *  Musik-Sachgruppe als enum.
   *
   * @param sgString auch null
   * @return    Sachgruppe (SG); null, wenn nicht vorhanden.
   */
  public static MUSIC_SG getSG(final String sgString) {
    if (sgString == null)
      return null;
    return ddc2enum.get(sgString);
  }

  public static void main(final String[] args) {
    System.out.println(getBestSG("781.01"));
    System.out.println(getSG("782.25"));
  }

}
