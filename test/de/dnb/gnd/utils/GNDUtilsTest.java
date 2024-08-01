package de.dnb.gnd.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.dnb.basics.Constants;
import de.dnb.gnd.TestUtils;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordParser;
import de.dnb.gnd.parser.RecordReader;

public class GNDUtilsTest {

  @Test
  public void testRemoveExpansion() {

  }

  @Test
  public void testSubfields2String() {

  }

  @Test
  public void testLine2String() {

  }

  @Test
  public void testRecord2StringFromFile() throws IOException {
    final RecordReader readerP3 = new RecordReader(new File("documents/GNDBeispiel_ber.txt"));
    final RecordReader readerPP = new RecordReader(new File("documents/GNDBeispiel_P_ber.txt"));

    while (readerPP.hasNext()) {
      final Record recordPPlus = readerPP.next();
      final Record recordP3 = readerP3.next();

      TestUtils.normalize(recordPPlus);
      String rawdataPP = recordPPlus.getRawData();
      rawdataPP = TestUtils.normalizeRawData(rawdataPP);

      TestUtils.normalize(recordP3);
      String rawdata3 = recordP3.getRawData();
      rawdata3 = TestUtils.normalizeRawData(rawdata3);

      final String p32p3 = RecordUtils.toPica(recordP3);
      final String p32pp = RecordUtils.toPica(recordP3, Format.PICA_PLUS, true,
        Constants.LINE_SEPARATOR, Constants.FLORIN);
      final String pp2pp = RecordUtils.toPica(recordPPlus, Format.PICA_PLUS, true,
        Constants.LINE_SEPARATOR, Constants.FLORIN);
      final String pp2p3 = RecordUtils.toPica(recordPPlus);

      assertEquals(rawdata3, p32p3);
      assertEquals(rawdata3, pp2p3);
      assertEquals(rawdataPP, pp2pp);
      assertEquals(rawdataPP, p32pp);

    }

  }

  private final RecordParser parser = new RecordParser();

  private void assertPicaTransf(final String pica3, final String picaPlus) {
    Record record;
    String pica3generated;
    String picaPlusGenerated;

    final boolean expanded = true;
    // pica3 -> pica3
    record = parser.parse(pica3);
    pica3generated = RecordUtils.toPica(record);
    assertEquals(pica3, pica3generated);
    // pica3 -> pica+
    record = parser.parse(pica3);
    picaPlusGenerated = RecordUtils.toPica(record, Format.PICA_PLUS, expanded,
      Constants.LINE_SEPARATOR, Constants.DOLLAR);
    assertEquals(picaPlus, picaPlusGenerated);
    // pica+ -> pica+
    record = parser.parse(picaPlus);
    picaPlusGenerated = RecordUtils.toPica(record, Format.PICA_PLUS, expanded,
      Constants.LINE_SEPARATOR, Constants.DOLLAR);
    assertEquals(picaPlus, picaPlusGenerated);
    // pica+ -> pica3
    record = parser.parse(picaPlus);
    pica3generated = RecordUtils.toPica(record);
    assertEquals(pica3generated, pica3);
  }

  @Test
  public void testRecord2String() {

    final String[][] data = { { "005 Tu1", "002@ $0Tu1" },

      { "797 1026406420", "003@ $01026406420" },

      { "006 http://d-nb.info/gnd/1026406420", "003U $ahttp://d-nb.info/gnd/1026406420" },

      { "008 pxl;szz", "004B $apxl$aszz" },

      { "011 f;s", "008A $af$as" }, { "012 v;w", "008B $av$aw" },

      { "043 XA-DE-BW;XA-DE-BY", "042B $aXA-DE-BW$aXA-DE-BY" },

      { "065 2.1;3.5a", "042A $a2.1$a3.5a" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" },
      { "797 1026406420", "003@ $01026406420" }, { "797 1026406420", "003@ $01026406420" } };

    for (final String[] picas : data) {
      assertPicaTransf(picas[0], picas[1]);
    }

  }

  @Test
  public void testToStringTagCollectionOfSubfield() {

  }

  @Test
  public void testToStringLine() {

  }

}
