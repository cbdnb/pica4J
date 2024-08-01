/**
 *
 */
package de.dnb.gnd.utils.mx;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Field;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineFactory;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.GNDUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;

/**
 * @author baumann
 *
 */
public class LibraryUtils {

  /**
   * nur statische Methoden.
   */
  private LibraryUtils() {
  }

  /**
   *
   * @param libRecord  Bibliotheks-Normdaten-Record
   * @return        ISIL oder null
   */
  public static String getIsil(final Record libRecord) {
    GNDUtils.assertGNDRecord(libRecord);
    return RecordUtils.getContentOfSubfield(libRecord, "092", 'e');
  }

  /**
   * Sucht im Unterfeld 807 $c. Wenn da nichts steht, im Unterfeld 805 $d.
  *
  * @param libRecord  Bibliotheks-Normdaten-Record
  * @return        Zugehörigen Verbund oder null
  */
  public static String getVerbund(final Record libRecord) {
    GNDUtils.assertGNDRecord(libRecord);
    final String verbund = RecordUtils.getContentOfSubfield(libRecord, "807", 'c');
    if (verbund != null)
      return verbund;
    else
      return RecordUtils.getContentOfSubfield(libRecord, "805", 'd');
  }

  /**
  *
  * @param libRecord  Bibliotheks-Normdaten-Record
  * @return        Voller Bibliotheksname oder null
  */
  public static String getLongName(final Record libRecord) {
    GNDUtils.assertGNDRecord(libRecord);
    return RecordUtils.getContentOfSubfield(libRecord, "110", 'a');
  }

  /**
   * Nimmt die 410-Felder und schaut nach, ob der vorgegebene code in einem
   * der $4-Unterfelder enthalten ist.
   *
   * @param record  Normdaten-Record
   * @param code a = weiterer Name, b Abkürzung, c = Kurzname, d = Englische Anzeigeform
   * @return        Alternativer Name oder null
   */
  public static String getAlternativeName(final Record record, final String code) {
    GNDUtils.assertGNDRecord(record);
    final Field field410 = RecordUtils.getFieldGivenAsString(record, "410");
    String shortN = null;
    for (final Line line : field410) {
      final String name = SubfieldUtils.getContentOfFirstSubfield(line, 'a');
      final String codeL = SubfieldUtils.getContentOfFirstSubfield(line, '4');
      if (StringUtils.equals(codeL, code)) {
        shortN = name;
        break;
      }
    }
    return shortN;
  }

  /**
   *
   * @param libRecord  Bibliotheks-Normdaten-Record
   * @return        Abkürzung oder null
   */
  public static String getAbbreviatedName(final Record libRecord) {
    return getAlternativeName(libRecord, "b");
  }

  /**
   *
   * @param libRecord  Bibliotheks-Normdaten-Record
   * @return        Kurzname oder null
   */
  public static String getShortName(final Record libRecord) {
    return getAlternativeName(libRecord, "c");
  }

  /**
   *
   * @param libRecord  Bibliotheks-Normdaten-Record
   * @return  die Abkürzung des Namens. Wenn keine gefunden wird, den Kurznamen
   */
  public static String getBestShortName(final Record libRecord) {
    final String name = getAbbreviatedName(libRecord);
    if (name != null)
      return name;
    else
      return getShortName(libRecord);
  }

  /**
   * @param args  bla
   */
  public static void main(final String[] args) {

    final String s = StringUtils.readClipboard();
    final JSONObject object = new JSONObject(s);

    final Record record = parse(object);
    System.out.println(record);

  }

  /**
   * @param jsonObject      nicht null
   * @return                Die Daten, die im OBjekt "data" enthalten sind.
   *                        Die Berliner ändern den Pfad alle Nas' lang. Zur Zeit
   *                        ist er: member[0].("data")
   * @throws JSONException  Wenns nicht klappt
   */
  public static JSONObject getData(final JSONObject jsonObject) {
    JSONObject data;
    try {
      final JSONArray arr = jsonObject.getJSONArray("member");
      final JSONObject o = (JSONObject) arr.get(0);
      data = o.getJSONObject("data");
    } catch (final JSONException | ClassCastException e) {
      return null;
    }
    return data;
  }

  private static final boolean DEBUG = false;

  /**
   *  Macht aus JSON-Daten einen PICA-Datensatz.
   *
   * @param jsonAuthority  nicht null
   *
   * @return        Pica-Normdaten-Record oder null
   */
  public static Record parse(final JSONObject jsonAuthority) {

    final JSONObject dataObj = getData(jsonAuthority);
    if (dataObj == null)
      return null;

    // idn raten, steht im Feld 003@:
    String idn = null;
    try {
      final JSONArray idnField = dataObj.getJSONArray("003@");
      final JSONArray idnField0 = idnField.getJSONArray(0);
      final JSONArray idnField1 = idnField0.getJSONArray(0);
      idn = idnField1.getString(0);
    } catch (final JSONException e1) {
      // nix
    }
    final GNDTagDB tagDB = GNDTagDB.getDB();
    final Record record = new Record(idn, tagDB);

    // ist sicher, da als JSONObject erkannt:
    final String[] picaTags = JSONObject.getNames(dataObj);
    for (final String tagStr : picaTags) {
      final Tag tag = tagDB.findTag(tagStr);
      if (tag == null) {
        if (DEBUG)
          System.err.println("Kein Tag zu: " + tagStr);
        continue;
      }
      final LineFactory factory = tag.getLineFactory();

      JSONArray field = null;
      try {
        field = dataObj.getJSONArray(tagStr);
      } catch (final JSONException e1) {
        if (DEBUG)
          e1.printStackTrace();
        continue;
      }
      final int fieldLength = field.length();
      for (int i = 0; i < fieldLength; i++) {

        JSONArray jsonLine = null;
        try {
          jsonLine = field.getJSONArray(i);
        } catch (final JSONException e1) {
          if (DEBUG)
            e1.printStackTrace();
          continue; // for (int i = 0; i < fieldLength;...)
        }
        final int subFieldCount = jsonLine.length();
        final List<Subfield> subfields = new ArrayList<>(subFieldCount);
        for (int j = 0; j < subFieldCount; j++) {
          Object jsonSubfieldObj;
          try {
            jsonSubfieldObj = jsonLine.get(j);
          } catch (final JSONException e1) {
            if (DEBUG)
              e1.printStackTrace();
            continue; // for (int j = 0; j < subFieldCount;...)
          }
          String indcatorStr = "0";
          String content = "";
          if (jsonSubfieldObj instanceof JSONObject) {
            final JSONObject jsonSubfield = (JSONObject) jsonSubfieldObj;
            indcatorStr = jsonSubfield.keySet().iterator().next();
            content = jsonSubfield.getString(indcatorStr);

          } else if (jsonSubfieldObj instanceof JSONArray) {
            final JSONArray jsonSubfield = (JSONArray) jsonSubfieldObj;
            content = jsonSubfield.getString(0);
          } else {
            if (DEBUG)
              System.err.println("Unbekanntes Unterfeld: " + jsonSubfieldObj);
          }
          final char ind = indcatorStr.charAt(0);
          final Indicator indicator = tag.getIndicator(ind);
          if (indicator == null) {
            if (DEBUG)
              System.err
                .println("Unbekannter Indikator: " + ind + "\n" + tag + "\n" + jsonAuthority);
            continue;
          }
          Subfield subfield = null;
          try {
            subfield = new Subfield(indicator, content);
          } catch (final IllFormattedLineException e) {
            if (DEBUG) {
              e.printStackTrace();
              System.err.println(StringUtils.concatenate(" / ", tag, indicator, content));
            }
            continue;
          }
          subfields.add(subfield);
        }

        try {
          factory.load(subfields);
          final Line line = factory.createLine();
          record.add(line);
        } catch (OperationNotSupportedException | IllFormattedLineException
          | IllegalArgumentException e) {
          if (DEBUG)
            e.printStackTrace();
        }
      }
    }
    return record;
  }

}
