/**
 *
 */
package de.dnb.basics.utils;

import java.time.LocalDate;

import static de.dnb.basics.utils.TimeUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author baumann
 *
 */
public class TimeUtilsTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.dnb.basics.utils.TimeUtils#temporalAccesorFrom548(java.lang.String, boolean)}.
   */
  @Test
  public void testTemporalAccesorFrom548() {
    //    fail("Not yet implemented");
  }

  /**
   * Test method for {@link de.dnb.basics.utils.TimeUtils#localDateFrom548(java.lang.String, boolean)}.
   */
  @Test
  public void testLocalDateFrom548() {
    final LocalDate jahr1anf = LocalDate.of(1, 1, 1);
    final LocalDate jahr1ende = LocalDate.of(1, 12, 31);

    final LocalDate jahr1vanf = LocalDate.of(0, 1, 1);
    final LocalDate jahr1vende = LocalDate.of(0, 12, 31);

    // volle Daten
    LocalDate ld = localDateFrom548("01.01.v0001", true);
    assertEquals(jahr1vanf, ld);
    ld = localDateFrom548("01.01.v0001", false);
    assertEquals(jahr1vanf, ld);

    // Jahre (1 v.Chr. ist Schlatjahr):
    ld = localDateFrom548("0001", false);
    assertEquals(jahr1anf, ld);
    ld = localDateFrom548("0001", true);
    assertEquals(jahr1ende, ld);
    ld = localDateFrom548("v0001", false);
    assertEquals(jahr1vanf, ld);
    ld = localDateFrom548("v0001", true);
    assertEquals(jahr1vende, ld);

    // ungenaue Daten:
    ld = localDateFrom548("v000X", true);
    assertEquals(jahr1vende, ld);
    ld = localDateFrom548("000X", false);
    assertEquals(jahr1anf, ld);
    ld = localDateFrom548("v00XX", true);
    assertEquals(jahr1vende, ld);
    ld = localDateFrom548("00XX", false);
    assertEquals(jahr1anf, ld);
    ld = localDateFrom548("v0XXX", true);
    assertEquals(jahr1vende, ld);
    ld = localDateFrom548("0XXX", false);
    assertEquals(jahr1anf, ld);
    //???:
    ld = localDateFrom548("vXXXX", true);
    assertNull(ld);
    //    assertEquals(jahr1vende, ld);
    ld = localDateFrom548("XXXX", false);
    assertNull(ld);
    //    assertEquals(jahr1anf, ld);
    ld = localDateFrom548("v000X", false);
    assertEquals(LocalDate.of(-8, 1, 1), ld);
    ld = localDateFrom548("000X", true);
    assertEquals(LocalDate.of(9, 12, 31), ld);
    ld = localDateFrom548("v00XX", false);
    assertEquals(LocalDate.of(-98, 1, 1), ld);
    ld = localDateFrom548("00XX", true);
    assertEquals(LocalDate.of(99, 12, 31), ld);
    ld = localDateFrom548("v0XXX", false);
    assertEquals(LocalDate.of(-998, 1, 1), ld);
    ld = localDateFrom548("000XXX", true);
    assertEquals(LocalDate.of(999, 12, 31), ld);
    //???:
    ld = localDateFrom548("vXXXX", false);
    assertNull(ld);
    //    assertEquals(LocalDate.of(-9998, 1, 1));
    ld = localDateFrom548("XXXX", true);
    assertNull(ld);
    //    assertEquals(LocalDate.of(9999, 12, 31));

    //Tage, Monate:
    ld = localDateFrom548("XX.XX.0001", true);
    assertEquals(jahr1ende, ld);
    ld = localDateFrom548("XX.1X.0001", true);
    assertEquals(jahr1ende, ld);
    ld = localDateFrom548("XX.12.0001", true);
    assertEquals(jahr1ende, ld);
    ld = localDateFrom548("XX.02.0001", true);
    assertEquals(LocalDate.of(1, 2, 28), ld);
    ld = localDateFrom548("XX.04.0001", true);
    assertEquals(LocalDate.of(1, 4, 30), ld);
    // eigentlich Unsinn:
    ld = localDateFrom548("11.XX.0001", true);
    assertEquals(LocalDate.of(1, 12, 11), ld);
    ld = localDateFrom548("0X.04.0001", true);
    assertEquals(LocalDate.of(1, 4, 9), ld);
    ld = localDateFrom548("1X.04.0001", true);
    assertEquals(LocalDate.of(1, 4, 19), ld);
    ld = localDateFrom548("2X.04.0001", true);
    assertEquals(LocalDate.of(1, 4, 29), ld);
    ld = localDateFrom548("3X.04.0001", true);
    assertEquals(LocalDate.of(1, 4, 30), ld);
    // Da sinnlos:
    ld = localDateFrom548("X0.04.0001", true);
    assertNull(ld);
    ld = localDateFrom548("X1.04.0001", true);
    assertNull(ld);

    //Tage, Monate:
    ld = localDateFrom548("XX.XX.0001", false);
    assertEquals(jahr1anf, ld);
    ld = localDateFrom548("XX.1X.0001", false);
    assertEquals(LocalDate.of(1, 10, 1), ld);
    ld = localDateFrom548("XX.12.0001", false);
    assertEquals(LocalDate.of(1, 12, 1), ld);
    ld = localDateFrom548("XX.02.0001", false);
    assertEquals(LocalDate.of(1, 2, 1), ld);
    ld = localDateFrom548("XX.04.0001", false);
    assertEquals(LocalDate.of(1, 4, 1), ld);
    // eigentlich Unsinn:
    ld = localDateFrom548("11.XX.0001", false);
    assertEquals(LocalDate.of(1, 1, 11), ld);
    ld = localDateFrom548("0X.04.0001", false);
    assertEquals(LocalDate.of(1, 4, 1), ld);
    ld = localDateFrom548("1X.04.0001", false);
    assertEquals(LocalDate.of(1, 4, 10), ld);
    ld = localDateFrom548("2X.04.0001", false);
    assertEquals(LocalDate.of(1, 4, 20), ld);
    ld = localDateFrom548("3X.05.0001", false);
    assertEquals(LocalDate.of(1, 5, 30), ld);
    // Da sinnlos:
    ld = localDateFrom548("X0.04.0001", false);
    assertNull(ld);
    //    ld = localDateFrom548("X1.04.0001", false);
    //    assertNull(ld);

  }

  /**
   * Test method for {@link de.dnb.basics.utils.TimeUtils#get548Interval(java.lang.String)}.
   */
  @Test
  public void testGet548Interval() {
    //    fail("Not yet implemented");
  }

}
