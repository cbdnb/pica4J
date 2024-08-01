package de.dnb.gnd;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dnb.basics.Constants;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;

public class ComparePicaTest {

  private static final TagDB TAG_DB = BibTagDB.getDB();

  static Record pica3R;
  static Record picaPlusFlorinR;
  static Record picaPlusDollarR;

  static String rawP3;
  static String rawPD;
  static String rawPF;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    final BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
    final String s = "";
    String input;

    System.out.println(
      "Bitte Pica3 (Normaldarstellung) in Zwischenablage " + "kopieren und hier Enter drücken");
    input = is.readLine();
    pica3R = BibRecUtils.readFromClip();

    System.out.println(
      "Bitte Pica+ (mittels \"s p\") in Zwischenablage " + "kopieren und hier Enter drücken");
    input = is.readLine();
    picaPlusFlorinR = BibRecUtils.readFromClip();

    System.out.println(
      "Bitte Pica+ (mittels Skript) in Zwischenablage " + "kopieren und hier Enter drücken");
    input = is.readLine();
    picaPlusDollarR = BibRecUtils.readFromClip();

    TestUtils.normalize(pica3R);
    TestUtils.normalize(picaPlusDollarR);
    TestUtils.normalize(picaPlusFlorinR);

    BibRecUtils.removeHoldingsSegment(pica3R);
    BibRecUtils.removeHoldingsSegment(picaPlusDollarR);
    BibRecUtils.removeHoldingsSegment(picaPlusFlorinR);

    rawP3 = TestUtils.removeItemData(TestUtils.normalizeRawData(pica3R.getRawData()));
    rawPD = TestUtils.removeItemData(TestUtils.normalizeRawData(picaPlusDollarR.getRawData()));
    rawPF = TestUtils.removeItemData(TestUtils.normalizeRawData(picaPlusFlorinR.getRawData()));
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public final void testRaw() {

    assertEquals(rawP3,
      RecordUtils.toPica(pica3R, Format.PICA3, true, Constants.LINE_SEPARATOR, Constants.DOLLAR));
    assertEquals(rawP3, RecordUtils.toPica(picaPlusDollarR, Format.PICA3, true,
      Constants.LINE_SEPARATOR, Constants.DOLLAR));
    assertEquals(rawP3, RecordUtils.toPica(picaPlusFlorinR, Format.PICA3, true,
      Constants.LINE_SEPARATOR, Constants.DOLLAR));

    assertEquals(rawPF, RecordUtils.toPica(pica3R, Format.PICA_PLUS, true, Constants.LINE_SEPARATOR,
      Constants.FLORIN));
    assertEquals(rawPF, RecordUtils.toPica(picaPlusDollarR, Format.PICA_PLUS, true,
      Constants.LINE_SEPARATOR, Constants.FLORIN));
    assertEquals(rawPF, RecordUtils.toPica(picaPlusFlorinR, Format.PICA_PLUS, true,
      Constants.LINE_SEPARATOR, Constants.FLORIN));

    assertEquals(rawPD, RecordUtils.toPica(pica3R, Format.PICA_PLUS, true, Constants.LINE_SEPARATOR,
      Constants.DOLLAR));
    assertEquals(rawPD, RecordUtils.toPica(picaPlusDollarR, Format.PICA_PLUS, true,
      Constants.LINE_SEPARATOR, Constants.DOLLAR));
    assertEquals(rawPD, RecordUtils.toPica(picaPlusFlorinR, Format.PICA_PLUS, true,
      Constants.LINE_SEPARATOR, Constants.DOLLAR));

  }

  @Test
  public final void test() {
    System.err.println();
    System.err.println(pica3R);
    System.err.println("------");
    System.err.println(picaPlusFlorinR);
    System.err.println("------");
    System.err.println(picaPlusDollarR);
  }

  @Test
  public final void testEquals() {
    assertEquals(pica3R, picaPlusDollarR);
    assertEquals(pica3R, picaPlusFlorinR);
  }
}
