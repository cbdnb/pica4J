package de.dnb.basics.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;

public class NumberUtils {

  private static Pattern romanPattern = null;

  private static String simpleRoman = "[MDCLXVI]+";

  private static Pattern simpleRomanPattern;

  /**
   *
   * @return  Pattern, das erkennt, ob ein String nur aus römischen
   * Zahlzeichen besteht.
   */
  public static Pattern getSimpleRomanPattern() {
    if (simpleRomanPattern == null) {
      simpleRomanPattern = Pattern.compile(simpleRoman);
    }
    return simpleRomanPattern;
  }

  /**
   *
   * @return  Mindestens eine röm. Ziffer, vorher ein non-word oder
   *          Zeilenanfang, hinterher non-word oder Zeilenende
   */
  public static Pattern getRomanPattern() {
    if (romanPattern == null) {
      String s = "([MDCLXVI]+)";
      // Mindestens eine röm. Ziffer, vorher ein non-word oder
      // Zeilenanfang, hinterher non-word oder Zeilenende:
      s = "(^|\\W)" + s + "($|\\W)";
      romanPattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    }
    return romanPattern;
  }

  private static Pattern arabicIntPattern = null;

  /**
   *
   * @return  Pattern für arabische = natürliche Zahl
   */
  public static Pattern getArabicIntPattern() {
    if (arabicIntPattern == null) {
      final String s = "\\d+";
      arabicIntPattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    }
    return arabicIntPattern;
  }

  /**
   *
   * @param s auch null
   * @return  s repäsentiert eine arabische = natürliche Zahl
   */
  public static boolean isPositiveArabicInt(final String s) {
    if (s == null)
      return false;
    final Pattern intP = getArabicIntPattern();
    final Matcher m = intP.matcher(s);
    return m.matches();
  }

  final static String floatS = "[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?";

  private static Pattern floatPattern = null;

  public static Pattern getfloatPattern() {
    if (floatPattern == null) {
      floatPattern = Pattern.compile(floatS, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    }
    return floatPattern;
  }

  /**
   *
   * @param s nicht null
   * @return  s repäsentiert eine rationale Zahl
   */
  public static boolean isFloat(final String s) {
    Objects.requireNonNull(s);
    final Pattern floatP = getfloatPattern();
    final Matcher m = floatP.matcher(s);
    return m.matches();
  }

  /**
   *
   * @param s auch null
   * @return  alle arabische = natürliche Zahlen, nicht null
   */
  public static Collection<String> getArabicIntMatches(final String s) {
    final Pattern pattern = getArabicIntPattern();
    final List<String> matches = StringUtils.getMatches(s, pattern);
    return matches;
  }

  /**
   *
   * @param s auch leer
   * @return      erste Zahl als String
   */
  public static Optional<String> getFirstArabicIntAsString(final String s) {
    final Collection<String> ints = getArabicIntMatches(s);
    return ints.stream().findFirst();
  }

  /**
  *
  * @param s auch null
  * @return  enthält arabische = natürliche Zahlen, nicht null
  */
  public static boolean containsArabicInts(final String s) {
    return !getArabicIntMatches(s).isEmpty();
  }

  /**
   * Liefert die römischen Zahlen im String.
   * @param s auch null
   * @return  nicht null.
   */
  public static Collection<String> getRomanMatches(final String s) {
    if (s == null)
      return Collections.emptyList();
    final List<String> matches = new ArrayList<>();
    final Pattern pattern = getRomanPattern();
    final Matcher matcher = pattern.matcher(s);
    while (matcher.find()) {
      final String cand = matcher.group(2);
      if (isRoman(cand))
        matches.add(cand);
    }
    return matches;
  }

  public static Collection<Integer> getRomanInts(final String s) {
    final Collection<String> matches = getRomanMatches(s);
    return FilterUtils.map(matches, NumberUtils::romanToInt);
  }

  /**
   *
   * @param s auch null
   * @return  erste arabische = natürliche Zahl
   */
  public static Optional<Integer> getFirstArabicInt(final String s) {
    final Collection<Integer> ints = getArabicInts(s);
    return ints.stream().findFirst();
  }

  /**
   * Liefert die arabische = natürliche Zahlen im String.
   * @param s auch null
   * @return  nicht null
   */
  public static Collection<Integer> getArabicInts(final String s) {
    final Collection<String> matches = getArabicIntMatches(s);
    return FilterUtils.map(matches, Integer::parseInt);
  }

  /**
   * Liefert alle natürlichen Zahlen (römisch, arabisch) im String.
   * @param s auch null
   * @return  nicht null
   */
  public static Collection<Integer> getAllInts(final String s) {
    final Collection<Integer> allints = getArabicInts(s);
    allints.addAll(getRomanInts(s));
    return allints;
  }

  public static int romanToInt(final char letter) {
    switch (letter) {
    case 'M':
      return 1000;
    case 'D':
      return 500;
    case 'C':
      return 100;
    case 'L':
      return 50;
    case 'X':
      return 10;
    case 'V':
      return 5;
    case 'I':
      return 1;
    default:
      return 0;
    }
  }

  public static int romanToInt(final String roman) {
    int result = 0;
    final String uRoman = roman.toUpperCase(); //case-insensitive
    for (int i = 0; i < uRoman.length() - 1; i++) {//loop over all but the last character
      //if this character has a lower value than the next character
      if (romanToInt(uRoman.charAt(i)) < romanToInt(uRoman.charAt(i + 1))) {
        //subtract it
        result -= romanToInt(uRoman.charAt(i));
      } else {
        //add it
        result += romanToInt(uRoman.charAt(i));
      }
    }
    //decode the last character, which is always added
    result += romanToInt(uRoman.charAt(uRoman.length() - 1));
    return result;
  }

  /**
   * Prüft eine röm. Zahl auf Korrektheit. Doppelte 5-er werden
   * abgewiesen. Ein zweimaliger Anstieg der Wertigkeit (IXC) wird
   * abgewiesern. Ein Zeichen, das vor einem höherwertigen steht,
   * darf nur einmal vorkommen (also nicht: XCIX).
   * @param s auch null
   * @return  ist korrekt?
   */
  public static boolean isRoman(final String s) {
    if (s == null)
      return false;
    final String uRoman = s.toUpperCase();
    final Pattern pattern = getSimpleRomanPattern();
    final Matcher matcher = pattern.matcher(uRoman);
    if (!matcher.matches())
      return false;

    // also nur Römische Zahlzeichen.
    // 5-er dürfen nur einmal vorkommen:
    if (StringUtils.countCharacter(uRoman, 'D') > 1)
      return false;
    if (StringUtils.countCharacter(uRoman, 'L') > 1)
      return false;
    if (StringUtils.countCharacter(uRoman, 'V') > 1)
      return false;
    boolean ascending = false;
    for (int i = 0; i < uRoman.length() - 1; i++) {
      final char currentChar = uRoman.charAt(i);
      final char nextChar = uRoman.charAt(i + 1);
      //if this character has a lower value than the next character
      if (romanToInt(currentChar) < romanToInt(nextChar)) {
        // darf nicht zweimal ansteigen:
        if (ascending)
          return false;
        else
          ascending = true;
        // darf dann nur einmal vorkommen:
        if (StringUtils.countCharacter(uRoman, currentChar) > 1)
          return false;
      } else
        ascending = false;
    }
    return true;
  }

  /**
   * Wandelt ein Array von int in eine veränderbare Integer-Liste
   * um.
   *
   * @param intArray  auch null
   * @return          nicht null
   */
  public static List<Integer> toList(final int[] intArray) {
    if (intArray == null)
      return Collections.emptyList();
    final int length = intArray.length;
    final ArrayList<Integer> integers = new ArrayList<>(length);
    for (final int integer : intArray) {
      integers.add(integer);
    }
    return integers;
  }

  /**
   *
   * @param intColl  auch null, darf keine null enthalten
   * @return          nicht null
   */
  public static int[] toArray(final Collection<Integer> intColl) {
    if (intColl == null)
      return new int[0];
    final int[] ret = new int[intColl.size()];
    int i = 0;
    for (final Integer e : intColl)
      ret[i++] = e;
    return ret;
  }

  /**
   *
   * @param intColl          auch null, darf null enthalten
   * @param defaultValue  wird als Ersatz für null verwendet
   * @return              null oder Array
   */
  public static int[] toArray(final Collection<Integer> intColl, final int defaultValue) {
    if (intColl == null)
      return null;
    final int[] ret = new int[intColl.size()];
    int i = 0;
    for (final Integer e : intColl) {
      if (e == null)
        ret[i++] = defaultValue;
      else
        ret[i++] = e;
    }
    return ret;
  }

  public static void main(final String[] args) {
    final int[] inta = {};//{1,2,3};
    System.out.println(toList(inta));

    final List<Integer> li = new LinkedList<>();
    li.add(1);
    li.add(null);
    System.out.println(toArray(li, 0));

  }
}
