/**
 *
 */
package de.dnb.basics.applicationComponents;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author baumann
 *
 */
public class LogiParserTest {

  /**
   * Test method for {@link de.dnb.basics.applicationComponents.LogiParser#getPredicate(java.lang.String, boolean)}.
   */
  @Test
  public void testGetPredicate() {
    assertFalse(LogiParser.getPredicate(null, true).test(null));
    assertFalse(LogiParser.getPredicate(null, true).test("ab"));
    assertFalse(LogiParser.getPredicate("ab", true).test(null));
    assertTrue(LogiParser.getPredicate("", true).test(null));

    assertTrue(LogiParser.getPredicate("a", true).test("a b"));
    assertTrue(LogiParser.getPredicate("(a)", true).test("a b"));
    assertTrue(LogiParser.getPredicate("((a))", true).test("a b"));

    assertFalse(LogiParser.getPredicate("a", true).test("ab bc"));

    assertTrue(LogiParser.getPredicate("ab* and a*", true).test("abc"));
    assertFalse(LogiParser.getPredicate("ab* and a", true).test("abc"));
    assertTrue(LogiParser.getPredicate("ab* and a", true).test("abc a"));
    assertFalse(LogiParser.getPredicate("ab* and ab", true).test("abc a"));

    assertTrue(LogiParser.getPredicate("ab*  a*", true).test("abc"));
    assertFalse(LogiParser.getPredicate("ab*  a", true).test("abc"));
    assertTrue(LogiParser.getPredicate("ab*  a", true).test("abc a"));
    assertFalse(LogiParser.getPredicate("ab*  ab", true).test("abc a"));

    assertTrue(LogiParser.getPredicate("a and b c", true).test("a b c"));
    assertTrue(LogiParser.getPredicate("a (b c)", true).test("a b c"));
    assertTrue(LogiParser.getPredicate("(a b) c", true).test("a b c"));

    assertTrue(LogiParser.getPredicate("ab* or ac", true).test("abc"));
    assertFalse(LogiParser.getPredicate("ab or ac", true).test("abc"));
    assertTrue(LogiParser.getPredicate("ab or ac", true).test("abc ac"));

    assertTrue(LogiParser.getPredicate("not a", true).test("abc ac"));
    assertFalse(LogiParser.getPredicate("not a", true).test("abc a"));
    assertTrue(LogiParser.getPredicate("not a b", true).test("abc b"));
    assertTrue(LogiParser.getPredicate("a not b", true).test("abc a"));

    assertTrue(LogiParser.getPredicate("not(a b)", true).test("abc a"));
    assertFalse(LogiParser.getPredicate("not( a b)", true).test("abc a b"));

    assertTrue(LogiParser.getPredicate("a (b or c)", true).test("a b"));
    assertFalse(LogiParser.getPredicate("a (b or c)", true).test("a bc"));

    // Unicode: Bei Ärger ist 'Ä' aus 2 Zeichen zusammengesetzt (vgl. debugString())
    assertTrue(LogiParser.getPredicate("Ärge*", true).test("Ärger"));
    assertTrue(LogiParser.getPredicate("Ärger", true).test("Ärger"));

    try {
      LogiParser.getPredicate("and", true);
      fail();
    } catch (final Exception e) {
      // OK
    }

    try {
      LogiParser.getPredicate("or", true);
      fail();
    } catch (final Exception e) {
      // OK
    }

    try {
      LogiParser.getPredicate("not", true);
      fail();
    } catch (final Exception e) {
      // OK
    }

    try {
      LogiParser.getPredicate("a and", true);
      fail();
    } catch (final Exception e) {
      // OK
    }

    try {
      LogiParser.getPredicate("a or", true);
      fail();
    } catch (final Exception e) {
      // OK
    }

    try {
      LogiParser.getPredicate("a or (", true);
      fail();
    } catch (final Exception e) {
      // OK
    }

    try {
      LogiParser.getPredicate("a or )", true);
      fail();
    } catch (final Exception e) {
      //
    }

  }

}
