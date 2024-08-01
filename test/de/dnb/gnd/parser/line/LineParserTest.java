package de.dnb.gnd.parser.line;

import static org.junit.Assert.*;

import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

public class LineParserTest {

  @Test
  public void testEnumLine() throws IllFormattedLineException {
    String lineStr;
    Line line;
    String expected;
    lineStr = "065 16.2;2.1;2.1";
    expected = "065  |$a|:16.2 |$a|:2.1";
    line = LineParser.parse(lineStr, GNDTagDB.getDB(), false);
    assertEquals(expected, line.toString());
  }

  @Test
  public void testPersonLine() throws IllFormattedLineException {
    Tag gNDTag = GNDTagDB.getDB().findTag("400");
    String content = "Bingen, Hildegard$cvon";
    Line line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    String expected = "400  |$a|:Bingen |$d|:Hildegard |$c|:von";
    assertEquals(expected, line.toString());

    content = "$PHildegardis$lBingensis";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "400  |$P|:Hildegardis |$l|:Bingensis";
    assertEquals(expected, line.toString());

    content = "$PHildegard$lHeilige, 1098-1179$vSWB-AK";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "400  |$P|:Hildegard |$l|:Heilige, 1098-1179 |$v|:SWB-AK";
    assertEquals(expected, line.toString());

    content = "$PHildegard$lHeilige, 1098-1179$vSWB-AK";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "400  |$P|:Hildegard |$l|:Heilige, 1098-1179 |$v|:SWB-AK";
    assertEquals(expected, line.toString());

    content = "$PElisabeth$nI.$lEngland, Königin";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "400  |$P|:Elisabeth |$n|:I. |$l|:England, Königin";
    assertEquals(expected, line.toString());

    gNDTag = GNDTagDB.getDB().findTag("500");
    content = "!119601699!Aesculapius$4beza$vVD-16 Mitverf.";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "500  |$9|:119601699 |$8|:Aesculapius |$4|:beza |$v|:VD-16 Mitverf.";
    assertEquals(expected, line.toString());

    content = "!119601699!$4beza$vVD-16 Mitverf.";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "500  |$9|:119601699 |$4|:beza |$v|:VD-16 Mitverf.";
    assertEquals(expected, line.toString());

    content = "Gebweiler, Hieronymus$4beza$vVD-16 Mitverf.";
    line = LineParser.parse(gNDTag, Format.PICA3, content, false);
    expected = "500  |$a|:Gebweiler |$d|:Hieronymus |$4|:beza |$v|:VD-16 Mitverf.";
    assertEquals(expected, line.toString());

  }

}
