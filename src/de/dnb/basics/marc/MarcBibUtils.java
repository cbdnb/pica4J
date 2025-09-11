/**
 *
 */
package de.dnb.basics.marc;

import java.util.Optional;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.utils.NumberUtils;
import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.utils.DDC_SG;
import de.dnb.gnd.utils.SGUtils;

/**
 * Für bibliographische Datensätze.
 *
 * @author baumann
 *
 */
public final class MarcBibUtils {

  private MarcBibUtils() {
  }

  /**
   *
   * @param record    nicht null
   * @return          den Titel aus 245
   */
  public static String getTitle(final Record record) {
    final DataField field245 = (DataField) record.getVariableField("245");
    if (field245 == null)
      return null;
    final String title = MarcUtils.getContent(record, "245", 'a');
    final String remainder = MarcUtils.getContent(record, "245", 'b');
    final String responsibility = MarcUtils.getContent(record, "245", 'c');
    String total = title;
    if (remainder != null && !remainder.isEmpty())
      total += " : " + remainder;
    if (responsibility != null && !responsibility.isEmpty())
      total += " / " + responsibility;
    return total;
  }

  /**
   *
   * @param record    nicht null
   * @return          die erste Sachgrupppe aus 082 oder null
   */
  public static String getSachgruppe(final Record record) {
    final String sg = MarcUtils.getContent(record, "082", 'a');
    final DDC_SG ddc = SGUtils.ddc2sg(sg);
    if (ddc == null)
      return null;
    return ddc.getDDCString();
  }

  /**
   *
   * @param record    nicht null
   * @return          die erste ISBN (in der Regel ISBN-13)
   *                  ohne Bindestriche oder null
   */
  public static String getISBN(final Record record) {
    final String tag = "020";
    final String sg = MarcUtils.getContent(record, tag, 'a');
    return sg;
  }

  /**
  *
  * @param record    nicht null
  * @return          den Verlagsnamen oder null
  */
  public static String getNameOfProducer(final Record record) {
    final String tag = "264";
    final String sg = MarcUtils.getContent(record, tag, 'b');
    return sg;
  }

  /**
  *
  * @param record    nicht null
  * @return          das Publikationsjahr (hoffentlich)
  *                    oder null
  */
  public static String getDateOfProduction(final Record record) {
    final String tag = "264";
    final String sg = MarcUtils.getContent(record, tag, 'c');
    if (sg == null)
      return null;
    final Optional<String> y = NumberUtils.getFirstArabicIntAsString(sg);
    return y.orElse(null);
  }

  /**
  *
  * @param record  nicht null
  * @return        den Typ des Datensatzes für Titeldaten
  */
  public static String getArtDesTiteldatensatzes(final Record record) {
    final Leader leader = record.getLeader();
    final char type = leader.getTypeOfRecord();
    String s = "?";
    switch (type) {
    case 'a':
      s = "Sprachmaterialien";
      break;
    case 'c':
      s = "Noten";
      break;
    case 'e':
      s = "Kartografische Materialien";
      break;
    case 'g':
      s = "Projiziertes Medium";
      break;
    case 'i':
      s = "Nicht-musikalische Tonaufnahme";
      break;
    case 'j':
      s = "Musikaufnahme";
      break;
    case 'o':
      s = "Medienkombination";
      break;
    case 't':
      s = "Handschriftliche Sprachmaterialien";
      break;

    default:
      break;
    }
    return s;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final String idn = StringUtils.readClipboard();
    final Record record = PortalUtils.getMarcRecord(idn);
    System.out.println(getNameOfProducer(record));
    System.out.println(getDateOfProduction(record));
  }

}
