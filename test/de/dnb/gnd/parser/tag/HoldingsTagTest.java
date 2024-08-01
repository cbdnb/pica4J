package de.dnb.gnd.parser.tag;

import static org.junit.Assert.*;

import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.utils.RecordUtils;

public class HoldingsTagTest {

  TagDB db = BibTagDB.getDB();

  @Test
  public void testTag7100() throws IllFormattedLineException {
    Line line = LineParser.parse("7100 PH ZS @ f % k", db, true);
    assertEquals("7100  |$a|:PH ZS |$d|:f |$l|:k", line.toString());
    assertEquals("7100 PH ZS @ f % k", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("7109 !!Ha 4!!", db, true);
    assertEquals("7109  |$f|:Ha 4", line.toString());
    assertEquals("7109 !!Ha 4!!", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209A/03 ƒaPH ZSƒdfƒlkƒx00", db, true);
    assertEquals("7100  |$a|:PH ZS |$d|:f |$l|:k |$x|:00", line.toString());
    assertEquals("7100 PH ZS @ f % k", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209A/01 ƒfHa 4ƒx09", db, true);
    assertEquals("7109  |$f|:Ha 4 |$x|:09", line.toString());
    assertEquals("7109 !!Ha 4!!", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209A/10 ƒfHa 4ƒx09", db, true);
    assertEquals("7109  |$f|:Ha 4 |$x|:09", line.toString());
    assertEquals("7109 !!Ha 4!!", RecordUtils.toPica(line, Format.PICA3, true, '$'));
  }

  @Test
  public void testTag8000() throws IllFormattedLineException {
    Line line = LineParser.parse("8001 %3b{dbfu000auau}", db, true);
    assertEquals("8001  |$c|:3b |$d|:dbfu000auau", line.toString());
    assertEquals("8001 %3b{dbfu000auau}", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209B/01 ƒc3bƒddbfu000auauƒx01", db, true);
    assertEquals("8001  |$c|:3b |$d|:dbfu000auau |$x|:01", line.toString());
    assertEquals("8001 %3b{dbfu000auau}", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209B/02 ƒc3bƒddbfu000auauƒx01", db, true);
    assertEquals("8001  |$c|:3b |$d|:dbfu000auau |$x|:01", line.toString());
    assertEquals("8001 %3b{dbfu000auau}", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209B/10 ƒc3bƒddbfu000auauƒx01", db, true);
    assertEquals("8001  |$c|:3b |$d|:dbfu000auau |$x|:01", line.toString());
    assertEquals("8001 %3b{dbfu000auau}", RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse("8031 - Beil. \"Heimatliebe, Heimatschutz\" zu", db, true);
    assertEquals("8031  |$a|:- Beil. \"Heimatliebe, Heimatschutz\" zu", line.toString());
    assertEquals("8031 - Beil. \"Heimatliebe, Heimatschutz\" zu",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209B/04 ƒa- Beil. \"Heimatliebe, Heimatschutz\" zuƒx31", db, true);
    assertEquals("8031  |$a|:- Beil. \"Heimatliebe, Heimatschutz\" zu |$x|:31", line.toString());
    assertEquals("8031 - Beil. \"Heimatliebe, Heimatschutz\" zu",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser
      .parse("8032 #2#1949(29.Okt.) - 1957(31.Dez.);" + " 1991(2.Jan.) - 2010(31.Dez.)", db, true);
    assertEquals(
      "8032  |$g|:2 |$a|:1949(29.Okt.) - 1957(31.Dez.);" + " 1991(2.Jan.) - 2010(31.Dez.)",
      line.toString());
    assertEquals("8032 #2#1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.)" + " - 2010(31.Dez.)",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse(
      "209B/02 ƒg2ƒa1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.) -" + " 2010(31.Dez.)ƒx32", db,
      true);
    assertEquals(
      "8032  |$g|:2 |$a|:1949(29.Okt.) - 1957(31.Dez.);" + " 1991(2.Jan.) - 2010(31.Dez.) |$x|:32",
      line.toString());
    assertEquals("8032 #2#1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.)" + " - 2010(31.Dez.)",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse(
      "209B/01 ƒg2ƒa1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.) -" + " 2010(31.Dez.)ƒx32", db,
      true);
    assertEquals(
      "8032  |$g|:2 |$a|:1949(29.Okt.) - 1957(31.Dez.);" + " 1991(2.Jan.) - 2010(31.Dez.) |$x|:32",
      line.toString());
    assertEquals("8032 #2#1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.)" + " - 2010(31.Dez.)",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse(
      "209B/10 ƒg2ƒa1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.) -" + " 2010(31.Dez.)ƒx32", db,
      true);
    assertEquals(
      "8032  |$g|:2 |$a|:1949(29.Okt.) - 1957(31.Dez.);" + " 1991(2.Jan.) - 2010(31.Dez.) |$x|:32",
      line.toString());
    assertEquals("8032 #2#1949(29.Okt.) - 1957(31.Dez.); 1991(2.Jan.)" + " - 2010(31.Dez.)",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse("8033 [N=60.1961,216;63.1963,66,189;64.1966," + "50-53;70.1972,132]",
      db, true);
    assertEquals("8033  |$a|:[N=60.1961,216;63.1963,66,189;64.1966," + "50-53;70.1972,132]",
      line.toString());
    assertEquals("8033 [N=60.1961,216;63.1963,66,189;64.1966," + "50-53;70.1972,132]",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse(
      "209B/02 ƒa[N=60.1961,216;63.1963,66,189;64.1966," + "50-53;70.1972,132]ƒx33", db, true);
    assertEquals("8033  |$a|:[N=60.1961,216;63.1963,66,189;64.1966," + "50-53;70.1972,132] |$x|:33",
      line.toString());
    assertEquals("8033 [N=60.1961,216;63.1963,66,189;64.1966," + "50-53;70.1972,132]",
      RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse("8034 Mikrofilm (Masterfilm)", db, true);
    assertEquals("8034  |$a|:Mikrofilm (Masterfilm)", line.toString());
    assertEquals("8034 Mikrofilm (Masterfilm)", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("209B/02 ƒaMikrofilm (Masterfilm)ƒx34", db, true);
    assertEquals("8034  |$a|:Mikrofilm (Masterfilm) |$x|:34", line.toString());
    assertEquals("8034 Mikrofilm (Masterfilm)", RecordUtils.toPica(line, Format.PICA3, true, '$'));

  }

  @Test
  public void testTag7140() throws IllFormattedLineException {
    Line line = LineParser.parse("7140 -M001", db, true);
    assertEquals("7140  |$u|:001", line.toString());
    assertEquals("7140 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("7141 -M001", db, true);
    assertEquals("7141  |$u|:001", line.toString());
    assertEquals("7141 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("7149 -M001", db, true);
    assertEquals("7149  |$u|:001", line.toString());
    assertEquals("7149 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));

    line = LineParser.parse("231L/01 ƒu001ƒx00", db, true);
    assertEquals("7140  |$u|:001 |$x|:00", line.toString());
    assertEquals("7140 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("231L/02 ƒu001ƒx00", db, true);
    assertEquals("7140  |$u|:001 |$x|:00", line.toString());
    assertEquals("7140 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("231L/03 ƒu001ƒx00", db, true);
    assertEquals("7140  |$u|:001 |$x|:00", line.toString());
    assertEquals("7140 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("231L/10 ƒu001ƒx00", db, true);
    assertEquals("7140  |$u|:001 |$x|:00", line.toString());
    assertEquals("7140 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("231L/10 ƒu001ƒx01", db, true);
    assertEquals("7141  |$u|:001 |$x|:01", line.toString());
    assertEquals("7141 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
    line = LineParser.parse("231L/10 ƒu001ƒx09", db, true);
    assertEquals("7149  |$u|:001 |$x|:09", line.toString());
    assertEquals("7149 -M001", RecordUtils.toPica(line, Format.PICA3, true, '$'));
  }

}
