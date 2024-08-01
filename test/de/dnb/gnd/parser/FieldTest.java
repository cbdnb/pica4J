package de.dnb.gnd.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.GNDTagDB;

public class FieldTest {

  Line line1, line2, line3;
  Field field1, field2, field3;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    line1 = LineParser.parse("500 !123456789!line1", GNDTagDB.getDB(), false);
    line2 = LineParser.parse("500 !123456789!line2", GNDTagDB.getDB(), false);
    line3 = LineParser.parse("500 !123456789!", GNDTagDB.getDB(), false);
    field1 = new Field(line1);
    field2 = new Field(line2);
    field3 = new Field(line3);
  }

  @Test
  public void testHashCode() {
    assertEquals(field1.hashCode(), field2.hashCode());
    assertEquals(field1.hashCode(), field3.hashCode());
    assertEquals(field2.hashCode(), field3.hashCode());
  }

  @Test
  public void testAdd() {

    try {

      field1.addWithoutDuplicates(line1);
      Collection<Line> lines = field1.getLines();
      assertTrue(lines.size() == 1);
      Line lineInField = field1.getLines().iterator().next();
      assertEquals(lineInField, line1);
      assertTrue(lineInField == line1);

      field1.addWithoutDuplicates(line2);
      lines = field1.getLines();
      assertTrue(lines.size() == 1);
      lineInField = field1.getLines().iterator().next();
      assertEquals(lineInField, line1);
      assertTrue(lineInField == line2);

      // nicht expandierte Zeile
      field2.addWithoutDuplicates(line3);
      lines = field1.getLines();
      assertTrue(lines.size() == 1);
      lineInField = field1.getLines().iterator().next();
      assertEquals(lineInField, line2);
      assertTrue(lineInField == line2);

      field3.addWithoutDuplicates(line1);
      lines = field3.getLines();
      assertTrue(lines.size() == 1);
      lineInField = field3.getLines().iterator().next();
      assertEquals(lineInField, line3);
      assertTrue(lineInField == line1);

      field3.add(line1);
      lines = field3.getLines();
      assertTrue(lines.size() == 2);

      try {
        line1 = LineParser.parse("675 a", GNDTagDB.getDB(), false);
        final Field field = new Field(line1);
        line2 = LineParser.parse("675 b", GNDTagDB.getDB(), false);
        field.add(line2);
        fail();
      } catch (final IllFormattedLineException e) {
        fail();
      }

      try {
        line1 = LineParser.parse("675 a", GNDTagDB.getDB(), false);
        final Field field = new Field(line1);
        line2 = LineParser.parse("675 b", GNDTagDB.getDB(), false);
        field.addWithoutDuplicates(line2);
        fail();
      } catch (final IllFormattedLineException e) {
        fail();
      }

    } catch (final OperationNotSupportedException e) {
    }

  }

  @Test
  public void testEqualsObject() {
    assertEquals(field1, field2);
    assertEquals(field1, field3);
    assertEquals(field2, field3);
  }

  @Test
  public void testClone() {
    final Field field = field1.clone();
    assertEquals(field, field1);
    assertEquals(field.hashCode(), field1.hashCode());
    assertTrue(field != field1);
    assertTrue(field.lines != field1.lines);
    try {
      final Line line = LineParser.parse("500 !123456780!qwe", GNDTagDB.getDB(), false);
      field.add(line);
      // wg GNDTag
      assertEquals(field.hashCode(), field1.hashCode());
    } catch (final Exception e) {
      fail();
    }
  }

}
