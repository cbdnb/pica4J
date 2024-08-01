package de.dnb.gnd;

import java.util.Collection;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;

public class TestUtils {

  public static final BibTagDB DB = BibTagDB.getDB();

  public static void remove797(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    RecordUtils.removeTags(record, "003@", "001X", "001U");
  }

  /**
   * Entfernt 003@ 001A 001B und 001D und 001X und 001U
   * @param record
   */
  public static void normalize(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    RecordUtils.removeTags(record, "003@", "001X");
    RecordUtils.removeTags(record, "001A", "001B", "001D", "001U");
    RecordUtils.removeTags(record, "8001");
  }

  public static String removeItemData(String rawdata) {
    final Collection<Tag> itemTags = DB.getHoldingsSegment();
    for (final Tag tag : itemTags) {
      rawdata = StringUtils.removeLinesFromString(rawdata, tag.pica3);
      final String[] picaPP = tag.picaPlus.split("/");
      final String picaP = picaPP[0];
      rawdata = StringUtils.removeLinesFromString(rawdata, picaP);
    }
    return rawdata;

  }

  public static String normalizeRawData(String rawdata) {
    rawdata = StringUtils.removeLinesFromString(rawdata, "001A ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001B ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001D ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001E ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001Q ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001U ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001X ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001@ ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "003@ ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "SET:");
    rawdata = StringUtils.removeLinesFromString(rawdata, "Set ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "Eingabe: ");
    /*
     * die beiden können nicht verarbeitet werden, da nicht in
     * pica3
     */
    rawdata = StringUtils.removeLinesFromString(rawdata, "001U ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "001X ");

    // bei STRG-A miterfasst
    rawdata = StringUtils.removeLinesFromString(rawdata, "CBS");
    rawdata = StringUtils.removeLinesFromString(rawdata, "Vollanzeige");
    rawdata = StringUtils.removeLinesFromString(rawdata, "Review");
    // ???
    rawdata = StringUtils.removeLinesFromString(rawdata, "101@ ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "\\[");
    rawdata = StringUtils.removeLinesFromString(rawdata, "201U/01 ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "E01");
    rawdata = StringUtils.removeLinesFromString(rawdata, "8001 ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "209B/01 ");
    rawdata = StringUtils.removeLinesFromString(rawdata, "042@ ");

    return rawdata;

  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final Record record = BibRecUtils.readFromConsole();
    final String raw = record.getRawData();
    System.out.println(removeItemData(raw));
  }

}
