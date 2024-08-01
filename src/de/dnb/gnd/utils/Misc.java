/**
 *
 */
package de.dnb.gnd.utils;

import java.util.ArrayList;
import java.util.List;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.utils.HTMLEntities;
import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordParser;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineComparator;
import de.dnb.gnd.utils.formatter.HTMLFormatter;

/**
 * Für alles, das keinen richtigen Platz hat.
 *
 * @author baumann
 *
 */
public class Misc {

  public static Pair<Record, Record> getOclcCompare(final String oclc) {
    final Pair<String, String> strings = getOclcCompareStrings(oclc);
    final RecordParser parser = new RecordParser();

    final Record rec1 = parser.parse(strings.first);
    final Record rec2 = parser.parse(strings.second);
    return new Pair<>(rec1, rec2);
  }

  public static Pair<String, String> getOclcCompareStrings(final String oclc) {
    final List<String> fracs1 = new ArrayList<>();
    final List<String> fracs2 = new ArrayList<>();
    final String[] lines = oclc.split("\n");
    for (final String line : lines) {
      final int len = StringUtils.lengthNullSafe(line);
      final int mid = len / 2;
      if (StringUtils.charAt(line, mid) == '|') {
        final String left = StringUtils.substring(line, 0, mid - 1);
        final String right = StringUtils.substring(line, mid + 2);
        fracs1.add(left);
        fracs2.add(right);
      }

    }

    final String recStr1 = combineFracs(fracs1);
    final String recStr2 = combineFracs(fracs2);
    return new Pair<>(recStr1, recStr2);

  }

  public static Pair<List<Line>, List<Line>> diff(final Record record1, final Record record2) {
    return ListUtils.diff(record1.getLines(), record2.getLines(), new LineComparator());
  }

  /**
   * @param fracs1
   * @return
   */
  private static String combineFracs(final List<String> fracs) {
    String ret = "";
    String line = "";
    for (String frac : fracs) {
      if (frac.startsWith(" ")) {
        frac = frac.trim();
        line += frac;
      } else {
        // alte Zeile sichern
        ret += "\n" + line;
        line = frac.trim();
      }
    }
    ret = ret.replace(" $", "$");
    ret = ret.replace("\\&#x", "&#x");
    ret = HTMLEntities.unhtmlentities(ret);

    return ret;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final String s = StringUtils.readClipboard();
    final Pair<Record, Record> recs = getOclcCompare(s);
    final Record firstRec = recs.first;
    RecordUtils.removeTagsBetween(firstRec, "0101", "0499");
    RecordUtils.removeTagsBetween(firstRec, "0501", "1129");
    RecordUtils.removeTagsBetween(firstRec, "1139", "2999");

    final Record secondRec = recs.second;
    RecordUtils.removeTagsBetween(secondRec, "0101", "0499");
    RecordUtils.removeTagsBetween(secondRec, "0501", "1129");
    RecordUtils.removeTagsBetween(secondRec, "1139", "2999");

    final Pair<List<Line>, List<Line>> diff = diff(firstRec, secondRec);
    final HTMLFormatter formatter = new HTMLFormatter();
    formatter.setFontsize(16);
    formatter.setBorder(0);
    formatter.setSpacing(-3);
    final String fo1 = formatter.format(diff.first, firstRec.tagDB);
    final String fo2 = formatter.format(diff.second, secondRec.tagDB);
    OutputUtils.show(fo1, 300, 0, 900, 1000);
    OutputUtils.show(fo2, 500, 0, 900, 1000);
  }

}
