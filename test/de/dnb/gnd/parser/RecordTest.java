package de.dnb.gnd.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import javax.naming.OperationNotSupportedException;

import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

public class RecordTest {

  @Test
  public void testIterator() throws IllFormattedLineException, OperationNotSupportedException {
    final Record record = new Record("12345678", GNDTagDB.getDB());
    final String s = record.toString();

    // Zeile hinzufügen:
    final Tag gNDTag = GNDTagDB.getDB().findTag("008");
    final Line line = LineParser.parse(gNDTag, Format.PICA3, "saz", false);
    record.add(line);
    for (final Line line1 : record) {
      assertEquals(line, line1);
    }

    // Zeile wieder entfernen:
    record.remove(line);
    assertEquals(record.toString(), "IDN: 12345678");

  }

  @Test
  public void testClone() throws Exception {
    final Record recordOld = new Record("12345678", GNDTagDB.getDB());
    Line line1, line2;

    line1 = LineParser.parse("008 saz", GNDTagDB.getDB(), false);
    line2 = LineParser.parse("150 Berg", GNDTagDB.getDB(), false);

    recordOld.add(line1);
    final Record recordNew = recordOld.clone();

    assertEquals(recordOld, recordNew);
    assertTrue(recordNew != recordOld);

    assertTrue(recordNew.fieldMap != recordOld.fieldMap);
    assertEquals(recordOld.fieldMap, recordNew.fieldMap);

    final GNDTag tag008 = (GNDTag) GNDTagDB.getDB().findTag("008");
    final Field fieldOld = recordOld.fieldMap.get(tag008);
    final Field fieldNew = recordNew.fieldMap.get(tag008);
    assertTrue(fieldNew != fieldOld);
    assertEquals(fieldOld, fieldNew);

    recordNew.add(line2);
    assertTrue(!recordNew.equals(recordOld));
  }

}
