package de.dnb.basics.applicationComponents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import de.dnb.basics.applicationComponents.strings.StringUtils;

public class StringUtilsTest {

  @Test
  public void testStringEqualsAndContains() {
    final String s = "ab12";

    assertTrue(StringUtils.stringEquals(null, null));
    assertFalse(StringUtils.stringEquals(s, null));
    assertFalse(StringUtils.stringEquals(null, s));
    assertTrue(StringUtils.stringEquals("", ""));
    assertFalse(StringUtils.stringEquals("a", "b"));
    // zerlegt vs. unzerlegt:
    assertFalse("Charakterstück".equals("Charakterstu" + '\u0308' + "ck"));
    assertTrue(StringUtils.stringEquals("Charakterstück", "Charakterstu" + '\u0308' + "ck"));

    assertFalse(StringUtils.contains(null, null, false));
    assertFalse(StringUtils.contains("a", null, false));
    assertFalse(StringUtils.contains(null, "a", false));
    assertTrue(StringUtils.contains("", "", false));
    assertFalse(StringUtils.contains("a", "b", false));
    // zerlegt vs. unzerlegt:
    assertFalse("Charakterstück".contains("stu" + '\u0308' + "ck"));
    assertTrue(StringUtils.contains("Charakterstück", "stu" + '\u0308' + "ck", false));

  }

  @Test
  public void testGestLongest() {
    List<String> strings = Arrays.asList("");
    String s = StringUtils.getLongestString(strings);
    assertEquals("", s);

    strings = Arrays.asList(null, null);
    s = StringUtils.getLongestString(strings);
    assertEquals(null, s);

    strings = Arrays.asList();
    s = StringUtils.getLongestString(strings);
    assertEquals(null, s);

    strings = Arrays.asList(null, "");
    s = StringUtils.getLongestString(strings);
    assertEquals("", s);

    strings = Arrays.asList("", null, "");
    s = StringUtils.getLongestString(strings);
    assertEquals("", s);

    strings = Arrays.asList("aa", "bb");
    s = StringUtils.getLongestString(strings);
    assertEquals("aa", s);

    strings = Arrays.asList("aa", "b");
    s = StringUtils.getLongestString(strings);
    assertEquals("aa", s);

    strings = Arrays.asList("a", "aa");
    s = StringUtils.getLongestString(strings);
    assertEquals("aa", s);
  }

  @Test
  public void testGestShortest() {
    List<String> strings = Arrays.asList("");
    String s = StringUtils.getShortestString(strings);
    assertEquals("", s);

    strings = Arrays.asList();
    s = StringUtils.getShortestString(strings);
    assertEquals(null, s);

    strings = Arrays.asList(null, null);
    s = StringUtils.getShortestString(strings);
    assertEquals(null, s);

    strings = Arrays.asList(null, "");
    s = StringUtils.getShortestString(strings);
    assertEquals("", s);

    strings = Arrays.asList("", null, "");
    s = StringUtils.getShortestString(strings);
    assertEquals("", s);

    strings = Arrays.asList("aa", "bb");
    s = StringUtils.getShortestString(strings);
    assertEquals("aa", s);

    strings = Arrays.asList("aa", "b");
    s = StringUtils.getShortestString(strings);
    assertEquals("b", s);

    strings = Arrays.asList("a", "aa");
    s = StringUtils.getShortestString(strings);
    assertEquals("a", s);
  }

  @Test
  public void testReplaceByWildcardCharStringArray() {
    String s;

    s = StringUtils.replaceByWildcard('#', "aa", "aa");
    assertEquals("aa", s);

    s = StringUtils.replaceByWildcard('#', "aa", "");
    assertEquals("##", s);

    s = StringUtils.replaceByWildcard('#', "aa", null);
    assertEquals("##", s);

    s = StringUtils.replaceByWildcard('#', "aa", "ab");
    assertEquals("a#", s);

    s = StringUtils.replaceByWildcard('#', "aa", "a");
    assertEquals("a#", s);

    s = StringUtils.replaceByWildcard('#', "a-a", "b-a");
    assertEquals("#-a", s);

    s = StringUtils.replaceByWildcard('#', "01-03-16", "02-03-16", "03-03-16", "04-03-16");
    assertEquals("0#-03-16", s);

    s = StringUtils.replaceByWildcard('#', "01-04-16", "02-03-16", "03-03-16", "04-03-16");
    assertEquals("0#-0#-16", s);

    s = StringUtils.replaceByWildcard('#', "01-03-15", "02-03-16", "03-03-16", "04-03-16");
    assertEquals("0#-03-1#", s);

    try {
      s = StringUtils.replaceByWildcard('#', null);
      fail();
    } catch (final Exception e) {
      //ok
    }

    s = StringUtils.replaceByWildcard('#', null, null);
    assertEquals(null, s);

  }

  @Test
  public void testQueryTrunc() {

    assertFalse(StringUtils.containsTruncated(null, null, false));
    assertFalse(StringUtils.containsTruncated("as", null, false));
    assertFalse(StringUtils.containsTruncated(null, "as", false));
    assertFalse(StringUtils.containsTruncated("", "", false));
    assertFalse(StringUtils.containsTruncated("as", "", false));
    assertTrue(StringUtils.containsTruncated("as", "as", false));
    assertFalse(StringUtils.containsTruncated("ast", "as", false));
    assertTrue(StringUtils.containsTruncated("ast", "as*", false));
    assertFalse(StringUtils.containsTruncated("ast", "s*", false));
    assertTrue(StringUtils.containsTruncated("ast", "*s*", false));
    assertFalse(StringUtils.containsTruncated("ast", "*s", false));
    // Marc-Unicode:
    assertTrue(StringUtils.containsTruncated("Ärger", "Ärger", false));

  }

  @Test
  public void testQueryTruncPredicate() {
    Predicate<String> predicate;
    predicate = StringUtils.getSimpleTruncPredicate(null, false);
    assertFalse(predicate.test(null));

    predicate = StringUtils.getSimpleTruncPredicate(null, false);
    assertFalse(predicate.test("as"));

    predicate = StringUtils.getSimpleTruncPredicate("as", false);
    assertFalse(predicate.test(null));

    predicate = StringUtils.getSimpleTruncPredicate("", false);
    assertFalse(predicate.test(""));

    predicate = StringUtils.getSimpleTruncPredicate("", false);
    assertFalse(predicate.test("as"));

    predicate = StringUtils.getSimpleTruncPredicate("as", false);
    assertTrue(predicate.test("as"));

    predicate = StringUtils.getSimpleTruncPredicate("aS", true);
    assertTrue(predicate.test("as"));

    predicate = StringUtils.getSimpleTruncPredicate("as", false);
    assertFalse(predicate.test("ast"));

    predicate = StringUtils.getSimpleTruncPredicate("as*", false);
    assertTrue(predicate.test("ast"));

    predicate = StringUtils.getSimpleTruncPredicate("s*", false);
    assertFalse(predicate.test("ast"));

    predicate = StringUtils.getSimpleTruncPredicate("*s*", false);
    assertFalse(predicate.test("ast"));

    predicate = StringUtils.getSimpleTruncPredicate("*s", false);
    assertTrue(predicate.test("ast"));

    predicate = StringUtils.getSimpleTruncPredicate("*S", true);
    assertTrue(predicate.test("ast"));

    // Marc-Unicode
    predicate = StringUtils.getSimpleTruncPredicate("Ärger", false);
    assertTrue(predicate.test("Ärger"));

    // Marc-Unicode
    predicate = StringUtils.getSimpleTruncPredicate("ÄrGer", true);
    assertTrue(predicate.test("Ärger"));

    // Marc-Unicode
    predicate = StringUtils.getSimpleTruncPredicate("ärGer", true);
    assertTrue(predicate.test("Ärger"));

    // Marc-Unicode
    predicate = StringUtils.getSimpleTruncPredicate("ÄrGer", true);
    assertTrue(predicate.test("ärger"));

    // andere Zeichen als Leerzeichen:
    predicate = StringUtils.getSimpleTruncPredicate("s", false);
    assertTrue(predicate.test("a s"));

    // andere Zeichen als Leerzeichen:
    predicate = StringUtils.getSimpleTruncPredicate("s", false);
    assertTrue(predicate.test("a,s"));

    // andere Zeichen als Leerzeichen:
    predicate = StringUtils.getSimpleTruncPredicate("s", false);
    assertTrue(predicate.test("a@s"));

    predicate = StringUtils.getSimpleTruncPredicate("s*", false);
    assertTrue(predicate.test("a@sk"));

    // andere Zeichen als Leerzeichen:
    predicate = StringUtils.getSimpleTruncPredicate("s", false);
    assertTrue(predicate.test("s d"));

    predicate = StringUtils.getSimpleTruncPredicate("s*", false);
    assertTrue(predicate.test("sk,d"));
  }

}
