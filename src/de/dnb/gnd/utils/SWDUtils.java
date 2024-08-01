package de.dnb.gnd.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.gnd.exceptions.WrappingHandler;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordParser;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.utils.formatter.Pica3Formatter;

public class SWDUtils {

  public static boolean containsChronologicalRelations(final Record record) {
    return RecordUtils.containsFields(record, "870", "880");
  }

  /**
   * Gibt den Inhalt der Zeile als Liste über alle Unterfelder (ohne
   * Kommentar):
   *
   * @param line  nicht null
   * @return      nicht null, evtl. leer, veränderbar
   */
  public static List<String> getContentOfSubfields(final Line line) {
    return SubfieldUtils.getContentsOfSubfields(getRelevantSubfields(line));
  }

  public static List<Subfield> getRelevantSubfields(final Line line) {
    return SubfieldUtils.removeSubfields(line, 'b');
  }

  /**
   *
   * @param record    nicht null
   * @return          nicht null
   */
  public static List<Line> getNamingRelevantLines(final Record record) {
    return RecordUtils.getLines(record, "800", "801", "802", "803", "804", "805");
  }

  static Function<Line, String> line2PicaWithoutTag = line -> getPica3WithoutTag(line);

  static Function<Line, String> line2Pica = line -> getPica3(line);

  /**
   *
   * @param record    nicht null
   * @param indicator Soll der Indikator (|s|, |p|, ...) eingeschlossen werden?
   * @return          null, wenn nicht vorhanden
   */
  public static String getName(final Record record, final boolean indicator) {
    final List<Line> lines = getNamingRelevantLines(record);
    if (lines.isEmpty())
      return null;

    List<String> names = FilterUtils.mapNullFiltered(lines, line2PicaWithoutTag);
    if (!indicator) {
      names = FilterUtils.map(names, str -> str.replaceFirst("^\\|.\\|", ""));
    }
    return StringUtils.concatenate(" / ", names);

  }

  /**
   * Gibt den Inhalt der Zeile als Inhalt des ersten Unterfeldes (in der
   * Annahme, dass nur eines existiert).
   *
   * @param line  nicht null
   * @return      auch null, wenn Liste leer
   */
  public static String getContentOfLine(final Line line) {
    final List<String> conts = getContentOfSubfields(line);
    return ListUtils.getFirst(conts);
  }

  public static String getPica3WithoutTag(final Line line) {
    final Pica3Formatter formatter = new Pica3Formatter();
    return formatter.formatWithoutTag(line);
  }

  public static String getPica3(final Line line) {
    final Pica3Formatter formatter = new Pica3Formatter();
    return formatter.format(line);
  }

  public static List<String> getPica3WithoutTag(final Record record, final String... tags) {
    final ArrayList<Line> lines = RecordUtils.getLines(record, tags);
    return FilterUtils.mapNullFiltered(lines, line2PicaWithoutTag);
  }

  public static List<String> getPica3(final Record record, final String... tags) {
    final ArrayList<Line> lines = RecordUtils.getLines(record, tags);
    return FilterUtils.mapNullFiltered(lines, line2Pica);
  }

  /**
   * Gibt die Entitätencodierung(en).
   * @param record    nicht null.
   *
   * @return          nicht null.
   */
  public static List<String> getEntityTypes(final Record record) {
    return RecordUtils.getContentsOfSubfields(record, "815");
  }

  public static String getDefinition(final Record record) {
    final List<Line> lines = RecordUtils.getLines(record, "808");
    String def = null;
    for (final Line line : lines) {
      final String ind = SubfieldUtils.getContentOfFirstSubfield(line, 'S');
      if (ind.equals("a"))
        def = SubfieldUtils.getContentOfFirstSubfield(line, 'a');
    }
    return def;
  }

  public static void main(final String[] args) {
    final String string = StringUtils.readClipboard();
    if (string == null)
      return;
    final RecordParser parser = new RecordParser();
    parser.setHandler(new WrappingHandler());
    final Record record = parser.parse(string);
    System.out.println(getDefinition(record));
    System.out.println(record.getId());
    System.out.println(record);
  }

}
