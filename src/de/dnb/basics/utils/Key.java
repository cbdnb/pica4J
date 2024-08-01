package de.dnb.basics.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dnb.basics.collections.CollectionUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

public class Key {

  /**
   *
   */
  private static final String TON2 = " Ton";

  private String tonBuchstabe = null; // String wg. "cis"

  private String tonGeschlecht = null;

  private String modusNr = null; // in der Regel == "1." ...

  private String modus = null; // in der Regel Ton

  @Override
  public final String toString() {

    // Fallunterscheidungen:-----------

    // einfachster Fall zuerst
    if (modusNr != null) {
      return modusNr + modus;
    }

    // Nur Tonbuchstabe
    if (tonBuchstabe != null && tonGeschlecht == null)
      return tonBuchstabe;

    // nur Tongeschlecht
    if (tonBuchstabe == null && tonGeschlecht != null) {
      if (istTongeschlechtAlt(tonGeschlecht))
        return tonGeschlecht;
    }

    //beides
    return tonBuchstabe + "-" + tonGeschlecht;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final Key key = new Key("2. Ton");
    System.out.println(key);
  }

  private static List<String> tonBuchstaben = Arrays.asList(

    "C", "Cis", "Des", "D",

    "Dis", "E", "Es", "F", "Fis", "Ges", "G", "Gis", "As", "A", "B", "H",

    "Ceses", "Ces", "Deses", "Eses", "Fes", "Feses", "Asas", "Heses",

    "Cisis", "Disis", "Eis", "Fisis", "Gisis", "Ais", "Aisis", "His");

  private static List<String> tonGeschlechterAltDur = Arrays.asList(

    "Phrygisch", "Hypophrygisch",

    "Lydisch", "Hypolydisch",

    "Mixolydisch", "Hypomixolydisch",

    "Ionisch", "Hypoionisch");

  private static List<String> tonGeschlechterAltMoll =
    Arrays.asList("Dorisch", "Hypodorisch", "äolisch", "Hypoäolisch");

  public static Collection<String> getKeyNames() {
    return tonBuchstaben;
  }

  private static final Set<String> tongeschlechterAlt =
    CollectionUtils.union(tonGeschlechterAltDur, tonGeschlechterAltMoll);

  public static Collection<String> getModeNames() {
    final List<String> list = new LinkedList<String>();
    list.add("Dur");
    list.add("Moll");
    list.addAll(tongeschlechterAlt);

    return list;
  }

  public static boolean istTongeschlechtAlt(final String geschlecht) {
    if (geschlecht == null)
      return false;
    return tongeschlechterAlt.contains(geschlecht);
  }

  public static boolean istDurig(final String geschlecht) {
    if (geschlecht == null)
      return false;
    if (geschlecht.equals("Dur"))
      return true;
    return tonGeschlechterAltDur.contains(geschlecht);
  }

  public static boolean istMollig(final String geschlecht) {
    if (geschlecht == null)
      return false;
    if (geschlecht.equals("Moll"))
      return true;
    return tonGeschlechterAltMoll.contains(geschlecht);
  }

  public static boolean istTonbuchstabe(final String b) {
    if (b == null)
      return false;
    for (final String buchstabe : tonBuchstaben) {
      if (buchstabe.equalsIgnoreCase(b))
        return true;
    }
    return false;
  }

  private static IllegalArgumentException keyException =
    new IllegalArgumentException("Tonart falsch");

  static final String tonPS = "(1?\\d\\. )(Ton)";
  static final Pattern tonP = Pattern.compile(tonPS);
  static final String bindeStr = "(\\w+)\\-(\\w+)";
  static final Pattern bindePattern = Pattern.compile(bindeStr);

  /*
   *  Der Konstruktor nimmt an, dass kein Rest mehr folgt, die Tonart also
   *  das letzte Glied in der Kette ist.
   */
  public Key(final String keyStr) {

    RangeCheckUtils.assertStringParamNotNullOrEmpty("Tonart-String keyStr leer oder null", keyStr);

    if (istTonbuchstabe(keyStr)) {
      tonBuchstabe = keyStr;
      return;
    }
    if (istTongeschlechtAlt(keyStr)) {
      tonGeschlecht = keyStr;
      return;
    }

    // "12. Ton" erkennen
    final Matcher tonM = tonP.matcher(keyStr);
    if (tonM.matches()) {
      modusNr = tonM.group(1);
      modus = tonM.group(2);
      return;
    }

    // Also ist es eine Art "C-Dur"

    final Matcher bindeMatcher = bindePattern.matcher(keyStr);
    if (bindeMatcher.matches()) {

      tonBuchstabe = bindeMatcher.group(1);
      tonGeschlecht = bindeMatcher.group(2);
      if (istDurig(tonGeschlecht))
        if (Character.isUpperCase(tonBuchstabe.charAt(0)))
          if (istTonbuchstabe(tonBuchstabe))
            return;

      if (istMollig(tonGeschlecht))
        if (Character.isLowerCase(tonBuchstabe.charAt(0)))
          if (istTonbuchstabe(tonBuchstabe))
            return;
    }
    throw keyException;
  } // Konstruktor

}
