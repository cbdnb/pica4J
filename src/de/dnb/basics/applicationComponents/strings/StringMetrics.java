/**
 *
 */

package de.dnb.basics.applicationComponents.strings;

import java.io.FileNotFoundException;
import java.util.*;

import de.dnb.basics.collections.BoundedPriorityQueue;
import de.dnb.basics.collections.Multimap;
import de.dnb.basics.collections.SetMultimap;

/**
 * Enthält diverse Stringvergleich-Algorithmen.
 * Siehe: <a href=https://de.wikipedia.org/wiki/Phonetische_Suche>
 * https://de.wikipedia.org/wiki/Phonetische_Suche</a>
 * <br><br>
 * Im Deutschen ist die maximale Editdistance zwischen Singular und irgendeiner
 * Flexionsform eines Substantivs 4. Die zwischen Plural und Flexionsform
 * maximal 3. Daher empfiehlt es sich,
 * {@link StringMetrics#levenshteinOrMax(String, String, double)} mit
 * max = 3/4/5 zu verwenden.
 *
 * @author baumann
 *
 */
public class StringMetrics {

  public static void main1(final String[] args) throws FileNotFoundException {

    final String pathname = "D:/Normdaten/corpora/willi_test.txt";

    final List<String> liste = StringUtils.readWordsFromFile(pathname, true);
    final Set<String> set = new HashSet<>(liste);
    final Thesaurus thesaurus = new Thesaurus(set);
    final List<StringDistance> wordsWithinDistance1 =
      StringMetrics.wordsWithinDistance(thesaurus, 3, "willi", 400);
    System.out.println(wordsWithinDistance1);
    final List<StringDistance> wordsWithinDistance2 =
      StringMetrics.wordsWithinDistance(set, 3.0, "willi", 400, true);
    System.out.println(wordsWithinDistance2);
    System.out
      .println(new HashSet<>(wordsWithinDistance1).equals(new HashSet<>(wordsWithinDistance2)));
  }

  public static void main(final String[] args) {
    final String[][] table =
      StringUtils.readTable("V:/03_FB_EE/14_IE/_intern/00_Arbeitsordner/Baumann/test/sgpl.txt");
    for (final String[] line : table) {
      final String sg = line[0];
      final String pl = line[1];
      //      final String transfSg = soundex(StringUtils.unicodeDecomposition(sg));
      //      final String transfpl = soundex(StringUtils.unicodeDecomposition(pl));
      final String transfSg = colognePhonetic(sg);
      final String transfpl = colognePhonetic(pl);
      System.out.println(StringUtils.concatenateTab(transfSg, transfpl));
    }
  }

  /**
   * Hilfsklasse, die einen Treffer und seinen Abstand zum Suchwort enthält.
   *
   * @author baumann
   *
   */
  public static class StringDistance implements Comparable<StringDistance> {
    private StringDistance(final String word, final double distance) {
      this.word = word;
      this.distance = distance;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(distance);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + ((word == null) ? 0 : word.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final StringDistance other = (StringDistance) obj;
      if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
        return false;
      if (word == null) {
        if (other.word != null)
          return false;
      } else if (!word.equals(other.word))
        return false;
      return true;
    }

    @Override
    public int compareTo(final StringDistance s) {
      return Double.compare(distance, s.distance);
    }

    public static Comparator<StringDistance> getComparator() {
      return (distance1, distance2) -> distance2.compareTo(distance1);
    }

    private final String word;
    private final double distance;

    /**
     * @return the word
     */
    public String getWord() {
      return word;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
      return distance;
    }

    @Override
    public String toString() {
      return "[" + word + ", " + distance + "]";
    }

  }

  /**
   * Sucht in einem Thesaurus die Wörter, die sich in einer
   * vorgegebenen Distanz zum Suchwort befinden. Wird die
   * Levenstein-Distanz verwendet, nenötigt man beim Abstand 4
   * knapp 4 ns pro Thesauruseintrag.
   * <br><br>
   * Tests ergaben, dass ein Thesaurus von 40.000
   * Wörtern in 7,5 ms durchsucht wurde (Vermutlich verbrauchte die
   * Iteration einiges mehr an Zeit). Ein Text von
   * 40.000 Wörtern wurde daher in 5 min verarbeitet. Ein Levenstein-
   * Automat braucht bei diesen Mengen etwa 2/3 mehr Zeit!
   *
   * @param thesaurus     gegebene Wortliste
   * @param maxDistance   zu berücksichtigender Abstand
   * @param searchTerm    Wort, mit dem verglichen wird
   * @param maxResultSize Maximale Trefferlistengröße
   * @param levenshtein   wenn true, wird die Levenstein-Distanz (Ganzzahlen),
   *                      sonst die Jaro-Winkler-Distanz (double zwischen
   *                      0 und 1) verwendet
   * @return              Eine nach Abständen zum Suchwort sortierte
   *                      Trefferliste
   */
  public static List<StringDistance> wordsWithinDistance(
    final Iterable<String> thesaurus,
    final double maxDistance,
    final String searchTerm,
    final int maxResultSize,
    final boolean levenshtein) {

    final BoundedPriorityQueue<StringDistance> prq =
      new BoundedPriorityQueue<>(maxResultSize, StringDistance.getComparator());
    for (final String word : thesaurus) {
      final double distance = levenshtein ? levenshteinOrMax(searchTerm, word, maxDistance)
        : jaroWinklerDistance(word, searchTerm);
      if (distance <= maxDistance) {
        prq.add(new StringDistance(word, distance));
      }
    }
    return prq.ordered();

  }

  /**
   * Sucht in einem Thesaurus die Wörter, die sich in einer
   * vorgegebenen Distanz zum Suchwort befinden. Die Trefferliste
   * ist nach Abständen geordnet.
   * <br><br>
   * Tests ergaben, für eine Suche nach Distanz 2 ms, nach Distanz 4
   * 6 ms pro Suchbegriff benötigt wurden. Ein Text von
   * 40.000 Wörtern wurde daher (Distanz 4) in 3 min verarbeitet.
   * Ein Levenstein-Automat braucht bei diesen Mengen etwa das 3-fache
   * an Zeit.
   * <br>Ab einer Thesaurusgröße von 300.000 ist allerdings der
   * Automat überlegen. (Anfragezeit 4,8 ms gegenüber 7 ms)
   *
   * @param thesaurus     gegebene Wortliste. Zu erzeugen mittels
   *                      {@link Thesaurus#Thesaurus(Iterable)}
   * @param maxDistance   zu berücksichtigender Abstand
   * @param searchTerm    Wort, mit dem verglichen wird
   * @param maxResultSize Maximale Trefferlistengröße
   *
   * @return              Eine nach Abständen zum Suchwort sortierte
   *                      Trefferliste
   */
  public static List<StringDistance> wordsWithinDistance(
    final Thesaurus thesaurus,
    final int maxDistance,
    final String searchTerm,
    final int maxResultSize) {

    final BoundedPriorityQueue<StringDistance> prq =
      new BoundedPriorityQueue<>(maxResultSize, StringDistance.getComparator());
    for (final String word : thesaurus.getKeywords(searchTerm.length() - maxDistance,
      searchTerm.length() + maxDistance)) {
      final double distance = levenshteinOrMax(searchTerm, word, maxDistance);
      if (distance <= maxDistance) {
        prq.add(new StringDistance(word, distance));
      }
    }
    return prq.ordered();
  }

  /**
   * Enthält die Schlüsselwortliste verteilt auf Listen gleicher Wortlänge. Daher
   * kann nach ähnlichen Wörtern schneller gesucht werden, da die Dreiecksungleichung
   * gilt.
   *
   * @author baumann
   *
   */
  public static class Thesaurus {
    public Thesaurus(final Iterable<String> keywords) {
      for (final String keyword : keywords) {
        final int len = keyword.length();
        len2keywords.add(len, keyword);
      }
    }

    public Collection<String> getKeywords(final int len) {
      return len2keywords.getNullSafe(len);
    }

    public Collection<String> getKeywords(final int lenMin, final int lenMax) {
      final ArrayList<String> keywords = new ArrayList<>();
      for (int i = lenMin; i <= lenMax; i++) {
        keywords.addAll(getKeywords(i));
      }
      return keywords;
    }

    private final Multimap<Integer, String> len2keywords = new SetMultimap<Integer, String>();
  }

  /**
   *
   * Siehe: <a href=https://rosettacode.org/wiki/Jaro-Winkler_distance>
   * https://rosettacode.org/wiki/Jaro-Winkler_distance</a>
   * <br>
   * Der Vergleich braucht im Durchschnitt 1940 ns.
   *
   *
   * @param string1 auch null
   * @param string2 auch null
   * @return
   */
  public static double jaroWinklerDistance(String string1, String string2) {
    if (string1 == null)
      string1 = "";
    if (string2 == null)
      string2 = "";
    int len1 = string1.length();
    int len2 = string2.length();
    if (len1 < len2) {
      final String s = string1;
      string1 = string2;
      string2 = s;
      final int tmp = len1;
      len1 = len2;
      len2 = tmp;
    }
    if (len2 == 0)
      return len1 == 0 ? 0.0 : 1.0;
    final int delta = Math.max(1, len1 / 2) - 1;
    final boolean[] flag = new boolean[len2];
    Arrays.fill(flag, false);
    final char[] ch1Match = new char[len1];
    int matches = 0;
    for (int i = 0; i < len1; ++i) {
      final char ch1 = string1.charAt(i);
      for (int j = 0; j < len2; ++j) {
        final char ch2 = string2.charAt(j);
        if (j <= i + delta && j + delta >= i && ch1 == ch2 && !flag[j]) {
          flag[j] = true;
          ch1Match[matches++] = ch1;
          break;
        }
      }
    }
    if (matches == 0)
      return 1.0;
    int transpositions = 0;
    for (int i = 0, j = 0; j < len2; ++j) {
      if (flag[j]) {
        if (string2.charAt(j) != ch1Match[i])
          ++transpositions;
        ++i;
      }
    }
    final double m = matches;
    final double jaro = (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;
    int commonPrefix = 0;
    len2 = Math.min(4, len2);
    for (int i = 0; i < len2; ++i) {
      if (string1.charAt(i) == string2.charAt(i))
        ++commonPrefix;
    }
    return 1.0 - (jaro + commonPrefix * 0.1 * (1.0 - jaro));
  }

  /**
   * Siehe: <a href=https://rosettacode.org/wiki/Jaro_similarity>
   * https://rosettacode.org/wiki/Jaro_similarity</a>
   *
   *
   * <pre>
   * Benötigt im Mittel 1600 ns.
   * </pre>
   *
   *
   * @param s auch null
   * @param t auch null
   * @return
   */
  public static double jaroSimilarity(String s, String t) {
    if (s == null)
      s = "";
    if (t == null)
      t = "";
    final int s_len = s.length();
    final int t_len = t.length();

    if (s_len == 0 && t_len == 0)
      return 1;

    final int match_distance = Integer.max(s_len, t_len) / 2 - 1;

    final boolean[] s_matches = new boolean[s_len];
    final boolean[] t_matches = new boolean[t_len];

    int matches = 0;
    int transpositions = 0;

    for (int i = 0; i < s_len; i++) {
      final int start = Integer.max(0, i - match_distance);
      final int end = Integer.min(i + match_distance + 1, t_len);

      for (int j = start; j < end; j++) {
        if (t_matches[j])
          continue;
        if (s.charAt(i) != t.charAt(j))
          continue;
        s_matches[i] = true;
        t_matches[j] = true;
        matches++;
        break;
      }
    }

    if (matches == 0)
      return 0;

    int k = 0;
    for (int i = 0; i < s_len; i++) {
      if (!s_matches[i])
        continue;
      while (!t_matches[k])
        k++;
      if (s.charAt(i) != t.charAt(k))
        transpositions++;
      k++;
    }

    return (((double) matches / s_len) + ((double) matches / t_len)
      + ((matches - transpositions / 2.0) / matches)) / 3.0;
  }

  /**
   *
   * Siehe: <a href=https://rosettacode.org/wiki/Levenshtein_distance#Java>
   * https://rosettacode.org/wiki/Levenshtein_distance#Java</a>
   * <br><br>
   * Gibt die Levenstein- oder Editdistanz.
   * <br>
   * Benötigt im Mittel 2900 ns.
   *
   * @param a auch null
   * @param b auch null
   * @return  Editdistanz als double, aber eigentlich int
   */
  public static double levenshteinDistance(final String a, final String b) {
    return levenshteinOrMax(a, b, -1);
  }

  /**
   * Siehe: <a href=https://rosettacode.org/wiki/Levenshtein_distance#Java>
   * https://rosettacode.org/wiki/Levenshtein_distance#Java</a>
   * <br><br>
   * Benötigt iom Mittel für
   * <pre>
   * max = 0: 37 ns
   * max = 30:  280 ns
   * max = 70:  500 ns
   * </pre>
   *
   * @param a   auch null
   * @param b   auch null
   * @param max maximal erlaubte Distanz
   * @return    distanz <= max
   */
  public static boolean levenshteinIsLowerThan(final String a, final String b, final int max) {
    return levenshteinOrMax(a, b, max) <= max;
  }

  /**
   * Siehe: <a href=https://rosettacode.org/wiki/Levenshtein_distance#Java>
   * https://rosettacode.org/wiki/Levenshtein_distance#Java</a>
   * <br><br>
   * Gibt die Levenstein- oder Editdistanz. Um der besseren Vergleichbarkeit
   * mit anderen Metriken willen werden die Distanzen als double zurückgegeben,
   * obwohl es eigentlich Ganzzahlen sind.
   * <br><br>
   * Ist max>0, so bricht die Berechnung ab, wenn erkennbar wird, dass die
   * Distanz>max sein muss. Dann wird max+1 zurückgegeben.<br>
   * Bei negativen Werten wird die Distanz vollständig berechnet.
   *
   * Benötigt iom Mittel für
   * <pre>
   * max = 0: 37 ns
   * max = 30:  280 ns
   * max = 70:  500 ns
   * max = -1: 2900 ns
   * </pre>
   *
   * <br><br>
   * @param a   auch null
   * @param b   auch null
   * @param max maximale Distanz, die berechnet wird
   * @return    Distanz oder max+1
   */
  public static double levenshteinOrMax(String a, String b, final double max) {
    if (a == null)
      a = "";
    if (b == null)
      b = "";
    if (a.equals(b))
      return 0;
    int lengtha = a.length();
    int lengthb = b.length();
    if (max >= 0 && Math.abs(lengtha - lengthb) > max)
      return max + 1;
    if (lengtha == 0)
      return lengthb;
    if (lengthb == 0)
      return lengtha;
    if (lengtha < lengthb) {
      // swap length and strings:
      final int tl = lengtha;
      lengtha = lengthb;
      lengthb = tl;
      final String ts = a;
      a = b;
      b = ts;
    }

    final int[] cost = new int[lengthb + 1];
    for (int i = 0; i <= lengthb; i += 1) {
      cost[i] = i;
    }

    for (int i = 1; i <= lengtha; i += 1) {
      cost[0] = i;
      int prv = i - 1;
      int min = prv;
      for (int j = 1; j <= lengthb; j += 1) {
        final int act = prv + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
        cost[j] = Integer.min(Integer.min(1 + (prv = cost[j]), 1 + cost[j - 1]), act);

        //        Collections.min(Arrays.asList(1 + (prv = cost[j]), 1 + cost[j - 1], act));
        if (prv < min)
          min = prv;
      }
      if (max >= 0 && min > max)
        return max + 1;
    }
    if (max >= 0 && cost[lengthb] > max)
      return max + 1;
    return cost[lengthb];
  }

  // Köln:

  // Predefined char arrays for better performance and less GC load
  private static final char[] AEIJOUY = new char[] { 'A', 'E', 'I', 'J', 'O', 'U', 'Y' };
  private static final char[] CSZ = new char[] { 'C', 'S', 'Z' };
  private static final char[] FPVW = new char[] { 'F', 'P', 'V', 'W' };
  private static final char[] GKQ = new char[] { 'G', 'K', 'Q' };
  private static final char[] CKQ = new char[] { 'C', 'K', 'Q' };
  private static final char[] AHKLOQRUX =
    new char[] { 'A', 'H', 'K', 'L', 'O', 'Q', 'R', 'U', 'X' };
  private static final char[] SZ = new char[] { 'S', 'Z' };
  private static final char[] AHKOQUX = new char[] { 'A', 'H', 'K', 'O', 'Q', 'U', 'X' };
  private static final char[] DTX = new char[] { 'D', 'T', 'X' };

  private static final char CHAR_IGNORE = '-'; // is this character to be ignored?

  /**
   * This class is not thread-safe; the field {@link #length} is mutable.
   * However, it is not shared between threads, as it is constructed on demand
   * by the method {@link ColognePhonetic#colognePhonetic(String)}
   */
  private static abstract class CologneBuffer {

    protected final char[] data;

    protected int length = 0;

    public CologneBuffer(final char[] data) {
      this.data = data;
      length = data.length;
    }

    public CologneBuffer(final int buffSize) {
      data = new char[buffSize];
      length = 0;
    }

    protected abstract char[] copyData(int start, final int length);

    public int length() {
      return length;
    }

    @Override
    public String toString() {
      return new String(copyData(0, length));
    }
  }

  private static class CologneOutputBuffer extends CologneBuffer {

    private char lastCode;

    public CologneOutputBuffer(final int buffSize) {
      super(buffSize);
      lastCode = '/'; // impossible value
    }

    /**
     * Stores the next code in the output buffer, keeping track of the previous code.
     * '0' is only stored if it is the first entry.
     * Ignored chars are never stored.
     * If the code is the same as the last code (whether stored or not) it is not stored.
     *
     * @param code the code to store.
     */
    public void put(final char code) {
      if (code != CHAR_IGNORE && lastCode != code && (code != '0' || length == 0)) {
        data[length] = code;
        length++;
      }
      lastCode = code;
    }

    @Override
    protected char[] copyData(final int start, final int length) {
      final char[] newData = new char[length];
      System.arraycopy(data, start, newData, 0, length);
      return newData;
    }
  }

  private static class CologneInputBuffer extends CologneBuffer {

    public CologneInputBuffer(final char[] data) {
      super(data);
    }

    @Override
    protected char[] copyData(final int start, final int length) {
      final char[] newData = new char[length];
      System.arraycopy(data, data.length - this.length + start, newData, 0, length);
      return newData;
    }

    public char getNextChar() {
      return data[getNextPos()];
    }

    protected int getNextPos() {
      return data.length - length;
    }

    public char removeNext() {
      final char ch = getNextChar();
      length--;
      return ch;
    }
  }

  /*
   * Returns whether the array contains the key, or not.
   */
  private static boolean arrayContains(final char[] arr, final char key) {
    for (final char element : arr) {
      if (element == key) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>
   * Implements the <i>Kölner Phonetik</i> algorithm.
   * </p>
   * Siehe: <a href=https://de.wikipedia.org/wiki/K%C3%B6lner_Phonetik>
   * https://de.wikipedia.org/wiki/K%C3%B6lner_Phonetik</a>
   *
   * <p>
   * In contrast to the initial description of the algorithm, this implementation
   * does the encoding in one pass.
   * </p>
   * Benötigt im Mittel 800 ns.
   * <br><br>
   *
   * @param text The source text to encode
   * @return the corresponding encoding according to the <i>K&ouml;lner Phonetik</i> algorithm
   */
  public static String colognePhonetic(final String text) {
    if (text == null) {
      return null;
    }

    final CologneInputBuffer input = new CologneInputBuffer(preprocessCologne(text));
    final CologneOutputBuffer output = new CologneOutputBuffer(input.length() * 2);

    char nextChar;

    char lastChar = CHAR_IGNORE;
    char chr;

    while (input.length() > 0) {
      chr = input.removeNext();

      if (input.length() > 0) {
        nextChar = input.getNextChar();
      } else {
        nextChar = CHAR_IGNORE;
      }

      if (chr < 'A' || chr > 'Z') {
        continue; // ignore unwanted characters
      }

      if (arrayContains(AEIJOUY, chr)) {
        output.put('0');
      } else if (chr == 'B' || (chr == 'P' && nextChar != 'H')) {
        output.put('1');
      } else if ((chr == 'D' || chr == 'T') && !arrayContains(CSZ, nextChar)) {
        output.put('2');
      } else if (arrayContains(FPVW, chr)) {
        output.put('3');
      } else if (arrayContains(GKQ, chr)) {
        output.put('4');
      } else if (chr == 'X' && !arrayContains(CKQ, lastChar)) {
        output.put('4');
        output.put('8');
      } else if (chr == 'S' || chr == 'Z') {
        output.put('8');
      } else if (chr == 'C') {
        if (output.length() == 0) {
          if (arrayContains(AHKLOQRUX, nextChar)) {
            output.put('4');
          } else {
            output.put('8');
          }
        } else {
          if (arrayContains(SZ, lastChar) || !arrayContains(AHKOQUX, nextChar)) {
            output.put('8');
          } else {
            output.put('4');
          }
        }
      } else if (arrayContains(DTX, chr)) {
        output.put('8');
      } else if (chr == 'R') {
        output.put('7');
      } else if (chr == 'L') {
        output.put('5');
      } else if (chr == 'M' || chr == 'N') {
        output.put('6');
      } else if (chr == 'H') {
        output.put(CHAR_IGNORE); // needed by put
      } else {
        // ignored; should not happen
      }

      lastChar = chr;
    }
    return output.toString();
  }

  /**
   * Compares the first encoded string to the second encoded string.
   *
   * @param text1 source text to encode before testing for equality.
   * @param text2 source text to encode before testing for equality.
   * @return {@code true} if the encoding the first string equals the encoding of the second string, {@code false}
   *         otherwise
   */
  public boolean isCologneEncodeEqual(final String text1, final String text2) {
    return colognePhonetic(text1).equals(colognePhonetic(text2));
  }

  /**
   * Converts the string to upper case and replaces Germanic umlaut characters
   * The following characters are mapped:
   * <ul>
   * <li>capital A, umlaut mark</li>
   * <li>capital U, umlaut mark</li>
   * <li>capital O, umlaut mark</li>
   * <li>small sharp s, German</li>
   * </ul>
   */
  private static char[] preprocessCologne(final String text) {
    // This converts German small sharp s (Eszett) to SS
    final char[] chrs = text.toUpperCase(Locale.GERMAN).toCharArray();

    for (int index = 0; index < chrs.length; index++) {
      switch (chrs[index]) {
      case '\u00C4': // capital A, umlaut mark
        chrs[index] = 'A';
        break;
      case '\u00DC': // capital U, umlaut mark
        chrs[index] = 'U';
        break;
      case '\u00D6': // capital O, umlaut mark
        chrs[index] = 'O';
        break;
      default:
        break;
      }
    }
    return chrs;
  }

  // Soundex:

  private static String getSoundexCode(final char c) {
    switch (c) {
    case 'B':
    case 'F':
    case 'P':
    case 'V':
      return "1";
    case 'C':
    case 'G':
    case 'J':
    case 'K':
    case 'Q':
    case 'S':
    case 'X':
    case 'Z':
      return "2";
    case 'D':
    case 'T':
      return "3";
    case 'L':
      return "4";
    case 'M':
    case 'N':
      return "5";
    case 'R':
      return "6";
    default:
      return "";
    }
  }

  /**
   *  Siehe: <a href=https://rosettacode.org/wiki/Soundex>
   * https://rosettacode.org/wiki/Soundex</a>
   * <br>
   * Benötigt im Mittel 14 Mikrosekunden.
   * <br><br>
   *
   * @param s nicht null
   * @return  Soundex
   */
  public static String soundex(final String s) {
    String code, previous, soundex;
    code = s.toUpperCase().charAt(0) + "";

    // EDITED : previous = "7";
    previous = getSoundexCode(s.toUpperCase().charAt(0));

    for (int i = 1; i < s.length(); i++) {
      final String current = getSoundexCode(s.toUpperCase().charAt(i));
      if (current.length() > 0 && !current.equals(previous)) {
        code = code + current;
      }
      previous = current;
    }
    soundex = (code + "0000").substring(0, 4);
    return soundex;
  }
}
