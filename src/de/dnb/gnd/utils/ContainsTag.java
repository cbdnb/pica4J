package de.dnb.gnd.utils;

import java.util.function.Predicate;

import de.dnb.basics.Constants;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;

/**
 * Filterkriterium ENTHÄLT basierend auf String.contains(). Verwendet
 * für Datenabzüge im gzip-Format. Vorsicht: Die Datensätze werden
 * durch \n getrennt. Das bedeutet, der erste Tag wird nicht durch RS
 * eingeleitet. Es wird angenommen, dass 001A immer der erste Tag ist.
 *
 * @author Christian Baumann
 *
 *
 */
public class ContainsTag implements Predicate<String> {

  private final String necessarySubstring;

  /**
   * Akzeptiert nur die Datensätze, die eine bestimmte Zeile (Feld)
   * im Pica+-Format enthalten. Die Feldnummer wird im
   * Pica3-Format übergeben.
   *
   * @param field nicht null, im Pica3-Format
   * @param db    Die verwendete Datenbank
   */
  public ContainsTag(final String field, final TagDB db) {
    final Tag tag = db.getPica3(field);
    final String picaPlus = tag.picaPlus;
    necessarySubstring = makeNecessarySubstring(picaPlus, makeTag(picaPlus));
  }

  /**
   *
   * @return  Manche RSWK-Ketten sind falsch aufgebaut. Es fehlt z.B. beim DBSM
   *          die 5100. Das wird durch diesen Filter berücksichtigt.
   */
  public static ContainsTag getContainsRSWK() {
    final String necessarySubstring = Constants.RS + "041A";
    return new ContainsTag(necessarySubstring);
  }

  /**
   *
   * @param picaPlus      Tag
   * @param tagPlusRest   rest
   * @return              notwendig im Datensatz enthaltenen
   *                      Teilstring. wird von RS eingeleitet, außer
   *                      wenn Tag==001A (Erstes Element).
   */
  private String makeNecessarySubstring(final String picaPlus, final String tagPlusRest) {
    String nSS;
    if (picaPlus.equals("001A")) {
      nSS = tagPlusRest;
    } else {
      nSS = Constants.RS + tagPlusRest;
    }
    return nSS;
  }

  /**
   *
   * @param picaPlus  Tag
   * @return          Tag + US
   */
  private String makeTag(final String picaPlus) {
    return picaPlus + " " + Constants.US;
  }

  /**
   * Akzeptiert nur die Datensätze, die eine Bestimmte Zeile (Feld)
   * enthalten.
   *
   * @param field nicht null, im Pica3-Format
   * @param indicator Indikator, mit dem beginnen soll
   * @param prefix    nicht null, damit beginnt das Feld des Datensatzes
   * @param db    Die verwendete Datenbank
   */
  public ContainsTag(
    final String field,
    final char indicator,
    final String prefix,
    final TagDB db) {
    final Tag tag = db.getPica3(field);
    final String picaPlus = tag.picaPlus;
    final String tagPlusRest = makeTag(picaPlus) + indicator + prefix;
    necessarySubstring = makeNecessarySubstring(picaPlus, tagPlusRest);
  }

  @Override
  public final boolean test(final String recordStr) {
    return recordStr.contains(necessarySubstring);
  }

  /**
   *
   */
  private ContainsTag(final String necessarySubstring) {
    this.necessarySubstring = necessarySubstring;

  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ContainsTag [necessarySubstring=" + necessarySubstring + "]";
  }

}
