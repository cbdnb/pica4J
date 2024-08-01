/**
 *
 */
package de.dnb.gnd.utils;

import static org.junit.Assert.*;
import static de.dnb.gnd.utils.IDNUtils.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author baumann
 *
 */
public class IDNUtilsTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#extractIDNs(String)}.
   */
  @Test
  public void testExtractIDNs() {
    String s = "000000108wert118696424yxc04072560XX";
    List<String> idns = extractIDNs(s);
    assertEquals(Arrays.asList("000000108", "118696424", "04072560X"), idns);

    s = "10-3wert118696424yxc4072560-1";
    idns = extractPPNs(s);
    assertEquals(Arrays.asList("118696424"), idns);

    s = "10-3wert118696424yxc4072560-1";
    idns = extractIDNs(s);
    assertEquals(Arrays.asList("10-3", "118696424", "4072560-1"), idns);
  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#int2Pruefziffer(int)}.
   */
  @Test
  public void testInt2Pruefziffer() {
    final int i = 10;
    final char pz = int2Pruefziffer(i);
    assertEquals('X', pz);
  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#getPPNPruefziffer(java.lang.CharSequence)}.
   */
  @Test
  public void testGetPPNPruefzifferCharSequence() {
    final String s = "00000010";
    assertEquals('8', getPPNPruefziffer(s));
  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#getSWDPruefziffer(java.lang.CharSequence)}.
   */
  @Test
  public void testGetSWDPruefzifferCharSequence() {
    final String s = "00000010";
    assertEquals('3', getSWDPruefziffer(s));
  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#ppn2long(java.lang.String)} and
   * {@link de.dnb.gnd.utils.IDNUtils#swd2long(java.lang.String)}
   */
  @Test
  public void testPpn2long() {
    String ppn = "000000108";
    assertEquals(10, ppn2long(ppn));
    ppn = "000000109";
    assertEquals(-1, ppn2long(ppn));
    ppn = "";
    assertEquals(-1, ppn2long(ppn));
    ppn = null;
    assertEquals(-1, ppn2long(ppn));

    String swd = "10-3";
    assertEquals(10, swd2long(swd));
    swd = "10-4";
    assertEquals(-1, swd2long(swd));
    swd = "";
    assertEquals(-1, swd2long(swd));
    swd = null;
    assertEquals(-1, swd2long(swd));

  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#long2PPN(long)}.
   */
  @Test
  public void testLong2PPN() {
    final int i = 1;
    assertEquals("000000019", long2PPN(i));
    assertNull(long2PPN(0));
    assertNull(long2PPN(-1));
  }

  /**
   * Test method for {@link de.dnb.gnd.utils.IDNUtils#long2SWD(long)}.
   */
  @Test
  public void testLong2SWD() {
    final int i = 1;
    assertEquals("1-2", long2SWD(i));
    assertNull(long2SWD(0));
    assertNull(long2SWD(-1));
  }

}
