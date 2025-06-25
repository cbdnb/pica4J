/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * Ein Comparator für Systematiknummern, wobei auch null und ungültige Strings erlaubt sind.
 * Die lexikographische Sortierung funktioniert nicht, da zum Beispiel 1 -> 2.1 -> 10.4
 *
 * @author baumann
 */
public class SystematikComparator implements Comparator<String> {

  /**
   * Vergleicht zwei Systematiknummern, wobei auch null erlaubt ist. Die ungültigen werden folgend
   * sortiert:
   * <li>".12", "", "11.11.12"
   * <li>null
   */
  @Override
  public int compare(final String o1, final String o2) {

    final List<String> l1 = split(o1);
    final List<String> l2 = split(o2);

    final int compare1 = l1.get(0).compareTo(l2.get(0));
    if (compare1 != 0) {
      return compare1;
    } else {
      final int compare2 = l1.get(1).compareTo(l2.get(1));
      if (compare2 != 0) {
        return compare2;
      } else {
        return l1.get(2).compareTo(l2.get(2));
      }
    }
  }

  /**
   *
   * @param sys auch null
   * @return    eine Liste mit genau 3 Elementen. Für "10.2ab" ist das <"10", "02", "ab">.
   * <br>		Für Ungültige, also etwa "", ".12" oder "11.22.33" ist das <"yy", "yy", "yy">
   * <br>		Für null <"zz", "zz", "zz">
   */
  private List<String> split(final String sys) {
    final List<String> vorNachL = new LinkedList<>();
    String vor;
    String nachZiffer;
    String nachBuchst;
    if (sys == null) {
      vor = "zz";
      nachZiffer = "zz";
      nachBuchst = "zz";
    } else {
      final String[] vorNachArr = sys.split("\\.");
      final int len = vorNachArr.length;
      if (len < 1 || len > 2) { // ungültig
        vor = "yy";
        nachZiffer = "yy";
        nachBuchst = "yy";
      } else { // len == 1 oder 2
        vor = vorNachArr[0];
        if (vor.length() == 1) {
          vor = "0" + vor;
        }
        if (len == 2) {
          final Pair<String, String> nachPair = splitNach(vorNachArr[1]);
          nachZiffer = nachPair.first;
          nachBuchst = nachPair.second;
        } else {
          nachZiffer = "";
          nachBuchst = "";
        }
        
        if (vor.length() == 0) {// ungültig
          vor = "yy";
          nachZiffer = "yy";
          nachBuchst = "yy";
        }
      }
    }
    vorNachL.add(vor);
    vorNachL.add(nachZiffer);
    vorNachL.add(nachBuchst);
    return vorNachL;
  }

  /**
   * @param string von der Form "2ab" (aus "10.2ab")
   * @return  <"02", "ab"> für "2ab", <"11", ""> für "11"
   */
  private Pair<String, String> splitNach(final String nach) {
    int i = 0;
    for (; i < nach.length(); i++) {
      final char c = nach.charAt(i);
      if (!Character.isDigit(c)) {
        break;
      }
    }
    String zahl = nach.substring(0, i);
    if (zahl.length() < 2)
      zahl = '0' + zahl;
    final String buchst = nach.substring(i);
    return new Pair<String, String>(zahl, buchst);
  }

  public static void main(final String[] args) {
    final SystematikComparator comparator = new SystematikComparator();
    System.out.println(comparator.splitNach("2eb"));

    System.out.println(comparator.compare(".1", ".1.1"));
  }
}
