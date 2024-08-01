/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * @author baumann
 *
 */
public class IDNUtils {

  /**
   *
   */
  private static final int MIN_LEN = 9;

  /**
   * mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X.
   */
  public static final String PPN_STR = "\\d{7,11}[Xx]?";

  /**
   * ein Zeichen (optional, wird, wenn vorhanden ausgewertet)
   * gefolgt von mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X.
   */
  public static final String PPN_STR_PLUS_1 = ".?" + PPN_STR;

  /**
   * "PPN:"gefolgt von Blanks und indestens 7 -11 Zahlen,
   *  eventuell gefolgt von x oder X.
   */
  public static final String PPN_STR_DOW = "PPN:? *(" + PPN_STR + ")";

  /**
   *  Mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X.
   */
  public static final Pattern PPN_PAT = Pattern.compile(PPN_STR);

  /**
   * ein Zeichen (optional, wird, wenn vorhanden ausgewertet)
   * gefolgt von mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X.
   */
  public static final Pattern PPN_PLUS_1_PAT = Pattern.compile(PPN_STR_PLUS_1);

  /**
   *  Mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X.
   */
  public static final Pattern PPN_DOW_PAT = Pattern.compile(PPN_STR_DOW);

  /**
   * die SWD-Nummern wie 2015732-0 oder ZDB-Nummern wie 2268640-X.
   */
  public static final String SWD_STR = "\\d{1,11}\\-(\\d|X|x)";

  /**
   *  die SWD-Nummern wie 2015732-0 oder ZDB-Nummern wie 2268640-X.
   */
  public static final Pattern SWD_IDN_PAT = Pattern.compile(SWD_STR, Pattern.MULTILINE);

  /**
   * SWD-IDNs (also mit '-') und PPNs.
   * <br><br>
   * \d{1,11}\-(\d|X|x)|\d{7,11}[Xx]?
   */
  public static final String IDN_STR = "(" + SWD_STR + ")|(" + PPN_STR + ")";

  /**
   * SWD-IDNs (also mit '-') und PPNs.
   * <br><br>
   * \d{1,11}\-(\d|X|x)|\d{7,11}[Xx]?
   */
  public static final Pattern IDN_PAT = Pattern.compile(IDN_STR, Pattern.MULTILINE);

  /**
   * Liefer die erste erkannte SWD-idn, PPN oder einen Leerstring.
   *
   * @param cs
   *            beliebig
   *
   * @return Gültige IDN oder Leeren String
   */
  public static String extractIDN(final CharSequence cs) {
    final Matcher mSWD = SWD_IDN_PAT.matcher(cs);
    final Matcher mGND = PPN_PAT.matcher(cs);
    String idn = "";
    if (mSWD.find()) {
      idn = mSWD.group();
    }
    String idn2 = "";
    if (mGND.find()) {
      idn2 = mGND.group();
    }
    if (idn.length() > idn2.length())
      return idn;
    else
      return idn2;
  }

  /**
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   *
   * @param s auch null
   * @return  alle gültigen idns
   */
  public static List<String> extractIDNs(final String s) {
    final List<String> list = StringUtils.getMatches(s, IDN_PAT);
    FilterUtils.filter(list, IDNUtils::isKorrekteIDN);
    return list;
  }

  /**
   *
   * @param s auch null. Die IDNs müssen als Strings mit Prüfziffer (SWD oder
   *          Standard-ppn) gegeben sein.
   * @return  idns als Zahlen.
   */
  public static List<Integer> extractIDNsasInts(final String s) {
    final Collection<String> matches = StringUtils.getMatches(s, IDN_PAT);
    return idns2ints(matches);
  }

  /**
   * Enthält keine -1
   *
   * @param idns  nicht null
   * @return      nicht null
   */
  public static List<Integer> idns2ints(final Collection<String> idns) {
    return idns.stream().filter(IDNUtils::isKorrekteIDN).map(IDNUtils::idn2int)
      .collect(Collectors.toList());
  }

  /**
   * Enthält keine null
   *
   * @param idns  beliebig
   * @return      nicht null, enthält zugehörige ppns mit angehängter Prüfziffer,
   *              die mindestens 9 Zeichen lang sind.
   */
  public static List<String> ints2ppns(final Collection<Integer> idns) {
    if (idns == null)
      return Collections.emptyList();
    return idns.stream().map(IDNUtils::int2PPN).filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Enthält keine -1
   *
   * @param ppns  nicht null
   * @return      nicht null
   */
  public static List<Integer> ppns2ints(final Collection<String> ppns) {
    return ppns.stream().map(IDNUtils::ppn2int).filter(ppnI -> ppnI != -1)
      .collect(Collectors.toList());
  }

  /**
   * Vom Typ 118696424.
   *
   * @param s auch null. Die PPNs können durch fast beliebige Teilstrings getrennt sein.
   * Vorsicht, wenn ein X vorkommt! Ein 000000108X wird nicht als 000000108 extrahiert, da als
   * Prüfziffer ein X, das aber falsch ist, angenommen wird.
   *
   * @return  alle gültigen ppns
   */
  public static List<String> extractPPNs(final String s) {
    final List<String> list = StringUtils.getMatches(s, PPN_PAT);
    FilterUtils.filter(list, IDNUtils::isKorrektePPN);
    return list;
  }

  /**
   * Liefert die erkannte idn oder null.
   * Extrahiert aus einem eingelesenen Download oder der Kopie aus der IBW
   * die IDN. dazu wird die Zeichenfolge PPN ausgewertet.
   *
   * @param dow
   *            Kopfzeile eines neuen Datensatzes
   *
   * @return Gültige IDN oder null
   */
  public static String extractPPNFromDownload(final CharSequence dow) {
    final Matcher mIDN = PPN_DOW_PAT.matcher(dow);
    if (mIDN.find()) {
      return mIDN.group(1);
    }
    return null;
  }

  /**
   * Liefer die erkannte idn oder null.
   * Extrahiert aus der eingelesenen Kopfzeile die IDN. Führende
   * Leerzeichen und -zeilen werden ignoriert.
   *
   * @param recordStr
   *            Kopfzeile eines neuen Datensatzes
   *
   * @return Gültige IDN oder null
   */
  public static String extractPPNfromFirstLine(final CharSequence recordStr) {
    RangeCheckUtils.assertReferenceParamNotNull("line", recordStr);
    final String lineTrimmed = recordStr.toString().trim();
    final String[] lines = lineTrimmed.split("\n");
    final String firstLine = lines[0];
    final Matcher mIDN = PPN_PLUS_1_PAT.matcher(firstLine);
    if (mIDN.find()) {
      String idn = mIDN.group();
      final char first = idn.charAt(0);
      if (first == Constants.MARC_SUB_SEP) {
        idn = idn.substring(2);
      } else if (!Character.isDigit(first)) {
        idn = idn.substring(1);
      }
      return idn;
    }
    return extractPPNFromDownload(recordStr);
  }

  /**
   * @param pz  1-10
   * @return    1-9 oder X
   */
  public static char int2Pruefziffer(final int pz) {
    if (pz == 10)
      return 'X';
    else
      return (char) (pz + '0');
  }

  /**
   * @param idnArray nicht null
   * @return  SUMME a_i * (n-i); a_i == Einträge, n == Arraylänge
   */
  public static int elferSumme(final int[] idnArray) {
    int summe = 0;
    final int length = idnArray.length;
    for (int i = 0; i < length; i++) {
      summe += (i + 2) * idnArray[length - i - 1];
    }
    return summe % 11;
  }

  /**
   * Gibt die Prüfziffer einer IDN nach
   * <a href=https://wiki.dnb.de/pages/viewpage.action?pageId=48139522>
   * https://wiki.dnb.de/pages/viewpage.action?pageId=48139522</a>.
   *
   * @param idnOhnePruefz  nicht null, besteht nur aus Zahlen
   * @return  Prüfziffer
   */
  public static char getPPNPruefziffer(final CharSequence idnOhnePruefz) {
    final int[] intArray = toIntArray(idnOhnePruefz);
    return getPPNPruefziffer(intArray);
  }

  /**
   * Gibt die Prüfziffer einer IDN nach
   * <a href=https://wiki.dnb.de/pages/viewpage.action?pageId=48139522>
   * https://wiki.dnb.de/pages/viewpage.action?pageId=48139522</a>.
   *
   * @param idnArray  nicht null
   * @return  Prüfziffer
   */
  public static char getPPNPruefziffer(final int[] idnArray) {
    final int elferSumme = elferSumme(idnArray);
    final int pz = (11 - elferSumme) % 11;
    return int2Pruefziffer(pz);
  }

  /**
   * Gibt die Prüfziffer einer SWD- oder ZDB-IDN nach
   * <a href=https://wiki.dnb.de/pages/viewpage.action?pageId=48139522>
   * https://wiki.dnb.de/pages/viewpage.action?pageId=48139522</a>.
   *
   * @param idnOhnePruefz  nicht null, besteht nur aus Zahlen
   * @return  Prüfziffer
   */
  public static char getSWDPruefziffer(final CharSequence idnOhnePruefz) {
    final int[] intArray = toIntArray(idnOhnePruefz);
    return getSWDPruefziffer(intArray);
  }

  /**
   * Gibt die SWD- oder ZDB-Prüfziffer einer IDN nach
   * <a href=https://wiki.dnb.de/pages/viewpage.action?pageId=48139522>
   * https://wiki.dnb.de/pages/viewpage.action?pageId=48139522</a>.
   *
   * @param idnArray  nicht null
   * @return  Prüfziffer
   */
  public static char getSWDPruefziffer(final int[] idnArray) {
    final int elferSumme = elferSumme(idnArray);
    return int2Pruefziffer(elferSumme);
  }

  /**
   * @param cs  nicht leer, nicht null
   * @return    nimmt an, dass nur Ziffern vorliegen und wandelt in ein Array um. Kann
   *            schiefgehen, wenn die Voraussetzung nicht erfüllt ist.
   */
  public static int[] toIntArray(final CharSequence cs) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("idn", cs);
    return cs.chars().map(i -> i - '0').toArray();
  }

  /**
   *
   * @param idn beliebig
   * @return  idn als int ohne Prüfziffer. -1, wenn ungültig
   */
  public static long ppn2long(final String idn) {
    if (StringUtils.isNullOrWhitespace(idn)) {
      return -1;
    }
    if (isKorrektePPN(idn)) {
      final Pair<String, Character> pair = splitPPN(idn);
      return Long.parseLong(pair.first);
    } else {
      return -1;
    }
  }

  /**
  *
  * @param idn beliebig
  * @return  idn als int ohne Prüfziffer. -1, wenn ungültig
  */
  public static long swd2long(final String idn) {
    if (StringUtils.isNullOrWhitespace(idn)) {
      return -1;
    }
    if (isKorrekteSWDidn(idn)) {
      final Pair<String, Character> pair = splitSWDid(idn);
      return Long.parseLong(pair.first);
    } else {
      return -1;
    }
  }

  /**
   *
   *@param ppn beliebig
   * @return  idn als int ohne Prüfziffer. -1, wenn ungültig oder null
   * @throws NumberFormatException wenn idn größer als 2^31-1. Das kann irgendwann einmal
   *                               in Zukunft passieren. Dann verwende {@link #ppn2long(String)}
   */
  public static int ppn2int(final String ppn) throws NumberFormatException {
    if (StringUtils.isNullOrWhitespace(ppn)) {
      return -1;
    }
    if (isKorrektePPN(ppn)) {
      final Pair<String, Character> pair = splitPPN(ppn);
      return Integer.parseInt(pair.first);
    } else {
      return -1;
    }
  }

  /**
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
  *
  * @param idn beliebig
  * @return  idn als int ohne Prüfziffer. -1, wenn ungültig
  * @throws NumberFormatException wenn idn größer als 2^31-1. Das kann irgendwann einmal
  *                               in Zukunft passieren. Dann verwende {@link #ppn2long(String)}
  */
  public static int idn2int(final String idn) throws NumberFormatException {
    if (StringUtils.isNullOrWhitespace(idn)) {
      return -1;
    }
    if (isKorrekteSWDidn(idn)) {
      final Pair<String, Character> pair = splitSWDid(idn);
      return Integer.parseInt(pair.first);
    } else
      return ppn2int(idn);
  }

  /**
  *
  * @param idn > 0, sonst wird null zurückgegeben, auch null
  * @return  zugehörige ppn mit angehängter Prüfziffer und mindestens 9 Zeichen lang
  */
  public static String int2PPN(final Integer idn) {
    if (idn == null)
      return null;
    return long2PPN(idn);
  }

  /**
   *
   * @param idn > 0, sonst wird null zurückgegeben
   * @return  zugehörige ppn mit angehängter Prüfziffer und mindestens 9 Zeichen lang
   */
  public static String long2PPN(final long idn) {
    if (idn <= 0)
      return null;
    String myIDN = Long.toString(idn);
    final char pruefz = getPPNPruefziffer(myIDN);
    myIDN += pruefz;
    myIDN = StringUtils.leftPadding(myIDN, MIN_LEN, '0');
    return myIDN;
  }

  /**
  *
  * @param idn > 0, sonst wird null zurückgegeben
  * @return  zugehörige SWD-nid mit angehängter Prüfziffer. Im Extremfall nur 3 Zeichen lang.
  *           Beispiel 1-2 in idn 000000019 (Conference of Non-Nuclear Weapon States)
  */
  public static String long2SWD(final long idn) {
    if (idn <= 0)
      return null;
    String myIDN = Long.toString(idn);
    final char pruefz = getSWDPruefziffer(myIDN);
    myIDN += "-" + pruefz;
    return myIDN;
  }

  /**
   * Für ppns vom Typ 118696424.
   *
   * @param ppn beliebig
   * @return  ob korrekt
   */
  public static boolean isKorrektePPN(final String ppn) {
    if (StringUtils.isNullOrWhitespace(ppn)) {
      return false;
    }
    final Matcher idnMatcher = PPN_PAT.matcher(ppn);
    if (!idnMatcher.matches()) {
      return false;
    }
    return hatKorrektePPNPruefziffer(ppn);
  }

  /**
   * Für idns vom Typ 4093770-7.
   *
   * @param idn beliebig
   * @return    ist SWD- oder ZDB-IDN
   */
  public static boolean isKorrekteSWDidn(final String idn) {
    if (StringUtils.isNullOrWhitespace(idn)) {
      return false;
    }
    final Matcher idnMatcher = SWD_IDN_PAT.matcher(idn);
    if (!idnMatcher.matches()) {
      return false;
    }
    return hatKorrekteSWDPruefziffer(idn);
  }

  /**
  * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
  *
  * @param idn beliebig
  * @return    ist PPN, SWD- oder ZDB-IDN
  */
  public static boolean isKorrekteIDN(final String idn) {
    return isKorrektePPN(idn) || isKorrekteSWDidn(idn);
  }

  /**
   *
   * @param idn beliebig
   *
   * @return  letztes Zeichen ist korrekte Prüfziffer
   */
  public static boolean hatKorrektePPNPruefziffer(final String idn) {
    final Pair<String, Character> pair = splitPPN(idn);
    if (pair == null)
      return false;
    final char mypruef = getPPNPruefziffer(pair.first);
    return pair.second == mypruef;
  }

  /**
  *
  * @param idn beliebig
  *
  * @return  letztes Zeichen ist korrekte SWD- oder ZDB-Prüfziffer
  */
  public static boolean hatKorrekteSWDPruefziffer(final String idn) {
    final Pair<String, Character> pair = splitSWDid(idn);
    if (pair == null)
      return false;
    final char mypruef = getSWDPruefziffer(pair.first);
    return pair.second == mypruef;
  }

  /**
   * Hilfsfunktion, um Collections kontrolliert manipulieren zu können.
   *
   * @param c   nicht null
   * @param idn Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   * @return  c geändert
   */
  public static boolean add(final Collection<Integer> c, final Integer idn) {
    if (idn == null || idn < 0)
      return false;
    return c.add(idn);
  }

  /**
   * Hilfsfunktion, um Collections kontrolliert manipulieren zu können.
   *
   * @param c   nicht null
   * @param idn beliebig, funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   * @return  c geändert
   */
  public static boolean add(final Collection<Integer> c, final String idn) {
    final int i = IDNUtils.idn2int(idn);
    return add(c, i);
  }

  /**
   * Hilfsfunktion, um Collections kontrolliert manipulieren zu können.
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   *
   * @param idn beliebig
   * @return  c geändert
   */
  public static boolean remove(final Collection<Integer> c, final String idn) {
    final int i = IDNUtils.ppn2int(idn);
    return c.remove(i);
  }

  /**
   * Hilfsfunktion, um Collections kontrolliert manipulieren zu können.
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   * <br>Differenz
   *
   * @param idns1 nicht null
   * @param idns2 nicht null, kann auch fehlerhafte idns enthalten, diese werden ignoriert
   * @return  idns1 geändert
   */
  public static boolean removeAll(final Collection<Integer> idns1, final Collection<String> idns2) {
    final List<Integer> is = idns2ints(idns2);
    return idns1.removeAll(is);
  }

  /**
   * Hilfsfunktion, um Collections kontrolliert manipulieren zu können.
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   * <br>Schnitt
   *
   * @param idns1 nicht null
   * @param idns2 nicht null, kann auch fehlerhafte idns enthalten, diese werden ignoriert
   * @return  idns1 geändert
   */
  public static boolean retainAll(final Collection<Integer> idns1, final Collection<String> idns2) {
    final List<Integer> is = idns2ints(idns2);
    return idns1.retainAll(is);
  }

  /**
   * Hilfsfunktion, um Collections kontrolliert manipulieren zu können.
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   * <br>Vereinigung.
   *
   * @param idns1 nicht null
   * @param idns2 nicht null, kann auch fehlerhafte idns enthalten, diese werden ignoriert
   * @return  idns1 geändert
   */
  public static boolean addAll(final Collection<Integer> idns1, final Collection<String> idns2) {
    final List<Integer> is = idns2ints(idns2);
    return idns1.addAll(is);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final int i = 10;
    System.out.println(int2PPN(i));
    System.out.println(long2SWD(i));
  }

  /**
   *
   * @param ppn beliebig
   * @return  (ppn ohne Prüfziffer, Prüfziffer) oder null
   */
  public static Pair<String, Character> splitPPN(String ppn) {
    if (ppn == null)
      return null;
    ppn = ppn.trim();
    if (StringUtils.length(ppn) < 2)
      return null;
    final int lastIndex = ppn.length() - 1;
    final char pruef = ppn.charAt(lastIndex);
    final String idnOhnePruef = ppn.substring(0, lastIndex);
    return new Pair<>(idnOhnePruef, pruef);
  }

  /**
  * Vom Typ 4093770-7.
  *
  * @param idn swd-idn beliebig
  * @return  (idn ohne Prüfziffer, Prüfziffer) oder null
  */
  public static Pair<String, Character> splitSWDid(String idn) {
    if (idn == null)
      return null;
    idn = idn.trim();
    if (StringUtils.length(idn) < 3)
      return null;
    final int bindestrich = idn.length() - 2;
    if (idn.charAt(bindestrich) != '-')
      return null;
    final char pruef = idn.charAt(bindestrich + 1);
    final String idnOhnePruef = idn.substring(0, bindestrich);
    return new Pair<>(idnOhnePruef, pruef);
  }

}
