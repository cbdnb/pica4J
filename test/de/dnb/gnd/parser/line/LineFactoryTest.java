package de.dnb.gnd.parser.line;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

public class LineFactoryTest {

  LineFactory factory450;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testLoad() throws IllFormattedLineException {

    final Tag tag450 = GNDTagDB.getDB().findTag("450");
    final Tag tag550 = GNDTagDB.getDB().findTag("550");
    factory450 = tag450.getLineFactory();
    final LineFactory factory550 = tag550.getLineFactory();
    Line line = LineParser.parse("150 aa$ggg$vvv", GNDTagDB.getDB(), false);
    List<Subfield> subfields = line.getSubfields();
    factory450.load(subfields);
    line = factory450.createLine();
    factory550.load(subfields);
    line = factory550.createLine();

    line = LineParser.parse("151 aa$ggg$vvv", GNDTagDB.getDB(), false);
    subfields = line.getSubfields();
    try {
      factory450.load(subfields);
      fail();
    } catch (final IllFormattedLineException e) {
      // OK
    }

  }

}
