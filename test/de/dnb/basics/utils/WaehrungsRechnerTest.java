/**
 *
 */
package de.dnb.basics.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author baumann
 *
 */
public class WaehrungsRechnerTest {

  WaehrungsRechner rechner = new WaehrungsRechner();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for
   * {@link de.dnb.basics.utils.WaehrungsRechner#findePreis(java.lang.String)}.
   */
  @Test
  public void testFindePreisString() {

    double preis = rechner.findePreis("EUR 26.95");
    assertEquals(preis, 26.95, 0);

    preis = rechner.findePreis("Festeinband : EUR 68.90");
    assertEquals(preis, 68.9, 0);

    preis = rechner.findePreis(": EUR 56.95 (DE), EUR 57.70 (AT), CHF 65.00 (freier Preis)");
    assertEquals(preis, 56.95, 0);

    preis = rechner.findePreis("geb. : DM 148.00");
    assertEquals(75.67, preis, .1);

    preis = rechner.findePreis("kart. : L 25.00, $ 45.00");
    assertEquals(preis, 42.59, 5);

    preis = rechner.findePreis(": Ptas 260.00");
    assertEquals(preis, 1.56, .1);

    preis = rechner.findePreis("hbk. : GBP 120.00");
    assertEquals(preis, 138, 15);

    preis = rechner.findePreis("Pp. : £ 19.99");
    assertEquals(preis, 23, 5);

    preis = rechner.findePreis("geb. : $A 30.00");
    assertEquals(preis, 18, 5);

    preis = rechner.findePreis("geb. : Ft 104.00 (Köt. 1 u. 2)");
    assertEquals(preis, 0.2707, .1);

    preis = rechner.findePreis("JPY 3800");
    assertEquals(preis, 24, 5);

    preis = rechner.findePreis("YEN 3000");
    assertEquals(preis, 18, 2);

    preis = rechner.findePreis("Gewebe in Schuber : Y 9800.00");
    assertEquals(preis, 61, 10);

    preis = rechner.findePreis("ffr 3.90");
    assertEquals(preis, 0.59, .1);

    preis = rechner.findePreis("geb. : sfr 115.00");
    assertEquals(preis, 121, 12);

    preis = rechner.findePreis("bfr 85.00");
    assertEquals(preis, 2, .2);

    preis = rechner.findePreis(": Kčs 16.00");
    assertEquals(preis, 0.648, .1);

    preis = rechner.findePreis("*: nkr 38.00");
    assertEquals(preis, 3, .5);

    preis = rechner.findePreis(": hfl 65.00");
    assertEquals(preis, 29, .5);

    preis = rechner.findePreis("Kcs 21.00");
    assertEquals(preis, 0.85, .2);

    preis = rechner.findePreis("kart. : Kč 44.00");
    assertEquals(preis, 1.78, .2);

    preis = rechner.findePreis("*: zł 100.00");
    assertEquals(preis, 22, 2);

    preis = rechner.findePreis("kart. : RMB Y 26.00");
    assertEquals(preis, 3.36, .8);

    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);

    preis = rechner.findePreis("Fmk 33.-");
    assertEquals(preis, 5.5, .1);

    preis = rechner.findePreis("kart. : TL 14.00");
    assertEquals(preis, 0.47, .2);

    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
    preis = rechner.findePreis("Gewebe : Yuan 13.80");
    assertEquals(preis, 1.78, .2);
  }

}
