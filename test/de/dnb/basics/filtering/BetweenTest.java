/**
 *
 */
package de.dnb.basics.filtering;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.dnb.basics.filtering.Between.Type;

/**
 * @author baumann
 *
 */
public class BetweenTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testTypes() {
    Between<Integer> between = new Between<Integer>(0, 5, Type.HALF_OPEN);
    assertFalse(between.test(-1));
    assertTrue(between.test(0));
    assertTrue(between.test(1));
    assertTrue(between.test(4));
    assertFalse(between.test(5));
    assertFalse(between.test(6));

    between = new Between<Integer>(0, 5, Type.OPEN);
    assertFalse(between.test(-1));
    assertFalse(between.test(0));
    assertTrue(between.test(1));
    assertTrue(between.test(4));
    assertFalse(between.test(5));
    assertFalse(between.test(6));

    between = new Between<Integer>(0, 5, Type.CLOSED);
    assertFalse(between.test(-1));
    assertTrue(between.test(0));
    assertTrue(between.test(1));
    assertTrue(between.test(4));
    assertTrue(between.test(5));
    assertFalse(between.test(6));

  }

}
