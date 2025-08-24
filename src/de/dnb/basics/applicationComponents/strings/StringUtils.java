package de.dnb.basics.applicationComponents.strings;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.applicationComponents.StreamUtils;
import de.dnb.basics.applicationComponents.tuples.Quadruplett;
import de.dnb.basics.filtering.AcceptEverything;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.PrefixPredicate;
import de.dnb.basics.filtering.RangeCheckUtils;

public class StringUtils {

  // --------------------------------------------------------------------
  // Splitten nach versch. Kriterien
  // --------------------------------------------------------------------

  /**
   * \n (Unix), \r\n (Windows) oder
   * \r (Mac)
   */
  private static final String LINE_SEP_PAT = "\n|\r\n|\r";

  private static final Pattern patDollar =
    Pattern.compile(" *\\$ *" + "| *; *" + "| */ *" + "| *, *");

  private static final Pattern patEckigRechts = Pattern.compile("\\]");

  /**
   *  Splittet nach ; / und $.
   * @param s auch null
   * @return  fractions
   */
  public static String[] splitString(final String s) {
    if (s == null)
      return null;
    if (s.length() == 0)
      return null;
    return patDollar.split(s);
  }

  // --------------------------------------------------------------------

  private static final Pattern patSlash = Pattern.compile(" / ");

  /**
   *  Splittet nach /.
   * @param s auch null
   * @return  fractions
   */
  public static String[] splitSlash(final String s) {
    if (s == null)
      return null;
    if (s.length() == 0)
      return null;
    return patSlash.split(s);
  }

  // --------------------------------------------------------------------

  // gibt Teilstring in eckiger Klammer und Teilstring außerhalb zurück
  public static String[] verarbeiteEckigeKlammern(String line) {
    if (line == null)
      return null;
    if (line.length() == 0)
      return null;
    if (line.charAt(0) == '[') {
      line = line.substring(1);
      return patEckigRechts.split(line);
    }
    return null;
  }

  // --------------------------------------------------------------------
  // IDN-Funktionen und Patterns
  // --------------------------------------------------------------------

  /**
   *
   * Zerlegt die Kopfzeile beim pica3-Download in die Inhalte der Felder
   * 001A, 001B, 001D.
   *
   * @param line nicht null
   * @return Tripel aus Eingabe, Änderung und Status oder null
   */
  public static Quadruplett<String, String, String, String> getControlFields(final String line) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    //@formatter:off
		final Pattern controlPat =
			Pattern.compile(
				"Eingabe: (\\S+) Änderung: (\\S+) (\\S*) Status: (\\S+)\\s*");
		//@formatter:on
    final Matcher m = controlPat.matcher(line);
    if (m.matches()) {
      final String eingabe = m.group(1);
      final String aenderung = m.group(2);
      final String time = m.group(3);
      final String status = m.group(4);
      //@formatter:off
            return new Quadruplett<String, String, String, String>(
                    eingabe, aenderung, time, status);
            //@formatter:on
    }
    return null;

  }

  /**
   * Simuliert eine Kopfzeile, wie sie beim Download erzeugt wird.
   * @return
   */
  public static String kopfzeile() {
    return "SET: S1 [14] TTL: 1           " + "PPN: 972620788                            "
      + "SEITE1 .";
  }

  /*
   * Simuliert eine Kopfzeile mit idn, wie sie beim Download erzeugt wird
   */
  public static String kopfzeile(final int nr, final String idn) {
    return "SET: S1 [14] TTL: " + nr + "           " + "PPN: " + idn
      + "                            " + "SEITE1 .";
  }

  // ----------------------------------------------------------------

  // ----------------------------------------------------------------

  private static final Pattern patSonderzeichen = Pattern // ?
    .compile(".*[^@,\\s\\w<>äöüÄÖÜß§\\$%&-].*");

  public static boolean containsSonderzeichen(final String s) {
    return patSonderzeichen.matcher(s).matches();
  }

  // -------------------------------------------------------------------

  private static final Pattern patgezaehlteUnterabteilung = Pattern.compile("(.*-\\d*)");

  public static boolean containsGezUnterabt(final String s) {
    return patgezaehlteUnterabteilung.matcher(s).matches();
  }

  /*
   * Gibt einen Text mit Zeilenumbrüchen in vorgegebener Zeilenlänge zurück.
   * Text darf noch keine Zeilenumbrüche enthalten.
   */
  public static String maximaleZeilenlaenge(final String s, final int len) {
    final StringTokenizer st = new StringTokenizer(s, " ", true);
    String word, ausgabe = "";
    int currentLineLen = 0;

    while (st.hasMoreTokens()) {
      word = st.nextToken();
      final int wordLen = word.length();

      if (currentLineLen + wordLen <= len) {
        ausgabe += word;
        currentLineLen += wordLen;
      } else { // Zeilenumbruch erforderlich
        ausgabe += Constants.LINE_SEPARATOR;
        final boolean firstIsSpace = (word.charAt(0) == ' ');
        ausgabe += (firstIsSpace ? "" : word);
        currentLineLen = firstIsSpace ? 0 : wordLen;
      }
    }
    return ausgabe;
  }

  /*
   * Rückt Folgezeilen eines String um anz Tabs nach rechts.
   */
  public static String folgezeilenEinruecken(final String s, final int anz) {
    final StringBuffer ein = new StringBuffer("");
    for (int i = 0; i < anz; i++) {
      ein.append("\t");
    }
    final String einrueckung = Constants.LINE_SEPARATOR + ein.toString();
    // Ersetze alle "\n" durch "\n\t..."

    // s = s.replaceAll("\n", a);
    return s.replaceAll(Constants.LINE_SEPARATOR, einrueckung);
  }

  /*
   * Rückt Zeilen eines String um anz Tabs nach rechts.
   */
  public static String einruecken(final String s, final int anz) {
    final StringBuffer ein = new StringBuffer("");
    for (int i = 0; i < anz; i++) {
      ein.append("\t");
    }
    final String einrueckung = Constants.LINE_SEPARATOR + ein.toString();
    // Ersetze alle "\n" durch "\n\t..."

    // s = s.replaceAll("\n", a);
    return ein + s.replaceAll(Constants.LINE_SEPARATOR, einrueckung);
  }

  /**
   * 
   * @param string	auch null
   * @return		Den ersten Buchstaben des ersten Wortes groß. Wird nicht getrimmt!
   */
  public static String capitalize(final String string) {
    if (string == null || string.length() == 0)
      return string;

    return new StringBuffer(string.length()).append(Character.toTitleCase(string.charAt(0)))
      .append(string.substring(1)).toString();
  }

  public static String ersterBuchstabeKlein(final String string) {
    if (string == null || string.length() == 0)
      return string;

    return new StringBuffer(string.length()).append(Character.toLowerCase(string.charAt(0)))
      .append(string.substring(1)).toString();
  }

  static final Pattern patYear = Pattern.compile("\\d\\d\\d\\d");

  public static boolean containsYear(final String s) {
    final Matcher m = patYear.matcher(s);
    return m.find();
  }

  /**
   * Entfernt aus input alle Zeilen, die mit prefix anfangen.
   *
   * @param input		nicht null
   * @param prefix	nicht null
   * @return			input ohne Zeilen mit prefix und ohne Leerzeilen
   *					(Zeilen nur mit Whitespace)
   */
  public static String removeLinesFromString(final String input, String prefix) {
    RangeCheckUtils.assertReferenceParamNotNull("input", input);
    RangeCheckUtils.assertReferenceParamNotNull("prefix", prefix);
    prefix = Pattern.quote(prefix);
    String s = input;
    final String regex = "^" + prefix + ".*$";
    final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    final Matcher matcher = pattern.matcher(input);
    s = matcher.replaceAll("");

    // Leerzeilen entfernen:
    s = s.replaceAll("(\\s*" + "\n" + "" + "\\s*)+", Constants.LINE_SEPARATOR);
    return s.trim();

  }

  /**
   * Liefert den Inhalt aller Felder mit tag und aller Unterfelder
   * mit ind.
   * Dafür werden String-Daten im Pica+-Format mit 'ƒ' als Unterfeldtrenner
   * benötigt.
   *
   * @param tag 		nicht null, nicht leer.
   * @param ind		beliebig.
   * @param rawData	nicht null, nicht leer.
   *
   * @return			nicht null.
   */
  public static
    List<String>
    getPicaPlusContents(final String tag, final char ind, final String rawData) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("tag", tag);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("rawData", rawData);
    final List<String> strings = new LinkedList<String>();
    final String linePatStr = "^" + tag + ".*$";
    final Pattern linePat = Pattern.compile(linePatStr, Pattern.MULTILINE);
    final Matcher lineMat = linePat.matcher(rawData);
    while (lineMat.find()) {
      final String line = lineMat.group();
      final String subfieldStr = "ƒ" + ind + "([^ƒ]*)";
      final Pattern subfieldPat = Pattern.compile(subfieldStr, Pattern.MULTILINE);
      final Matcher subMatcher = subfieldPat.matcher(line);
      while (subMatcher.find()) {
        strings.add(subMatcher.group(1));
      }
    }
    return strings;

  }

  /**
   * Liefert den des ersten Feldes mit tag und des ersten Unterfeldes
   * mit ind.
   * Dafür werden String-Daten im Pica+-Format mit 'ƒ' als Unterfeldtrenner
   * benötigt.
   *
   * @param tag 		nicht null, nicht leer.
   * @param ind		beliebig.
   * @param rawData	nicht null, nicht leer.
   *
   * @return			Inhalt des Unterfeldes oder null.
   */
  public static String getPicaPlusContent(final String tag, final char ind, final String rawData) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("tag", tag);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("rawData", rawData);
    final List<String> strings = getPicaPlusContents(tag, ind, rawData);
    if (strings.isEmpty())
      return null;
    else
      return strings.get(0);
  }

  /**
   * Sichere Methode, um ein Zeichen an der Stelle index im String s zu
   * finden.
   *
   * @param s		beliebig.
   * @param index	beliebig, startet mit 0.
   *
   * @return		Zeichen an der Stelle index, 0 in allen anderen Fällen.
   */
  public static char charAt(final CharSequence s, final int index) {
    if (s == null)
      return 0;
    if (index < 0)
      return 0;
    if (index >= s.length())
      return 0;
    return s.charAt(index);
  }

  /**
   *
   * Null-sichere Methode, auf ein Element eines Arrays zuzugreifen.
   *
   * @param array	beliebig, auch null
   * @param index			beliebig
   * @param <V>			Typ
   * @return				Wert an der Stelle i, sonst null
   */
  public static <V> V getArrayElement(final V[] array, final int index) {
    if (array == null)
      return null;
    if (index < 0)
      return null;
    if (index >= array.length)
      return null;
    return array[index];

  }

  /**
   * Sichere Abfrage auf unnötige Strings.
   *
   * @param s beliebig
   * @return  string ist null oder leer
   */
  public static boolean isNullOrEmpty(final String s) {
    return s == null || s.isEmpty();
  }

  /**
   * Sichere Abfrage auf unnötige Strings. Auch mehrfache
   * Whitespaces.
   *
   * @param s beliebig
   * @return  string ist null oder Whitespace
   */
  public static boolean isNullOrWhitespace(final String s) {
    return s == null || s.trim().isEmpty();
  }

  /**
   * Enthält eines der Elemente aus iterable das Präfix?
   *
   * @param iterable	nicht null.
   * @param prefix	nicht null.
   * @return			true, wenn prefix vorkommt.
   */
  public static boolean containsPrefix(final Iterable<String> iterable, final String prefix) {
    RangeCheckUtils.assertReferenceParamNotNull("iterable", iterable);
    RangeCheckUtils.assertStringParamNotNullOrEmpty("prefix", prefix);
    final Predicate<String> predicate = new PrefixPredicate(prefix);
    return FilterUtils.contains(iterable, predicate);
  }

  /**
   * Liest einen String aus der Zwischenablage.
   *
   * @return String oder null, wenn kein String in Zwischenablage.
   */
  public static String readClipboard() {
    Clipboard systemClipboard;
    systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable transferData = systemClipboard.getContents(null);
    if (transferData != null && transferData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      // Inhalt auslesen
      try {
        return (String) transferData.getTransferData(DataFlavor.stringFlavor);
      } catch (final UnsupportedFlavorException e) {
        return null;
      } catch (final IOException e) {
        return null;
      }
    }
    return null;
  }

  /**
   *
   * @param filename  existiert
   * @return          Fileinhalt als String
   * @throws          FileNotFoundException  wenn nicht da
   */
  public static String readIntoString(final String filename) throws FileNotFoundException {
    final InputStream reader = new FileInputStream(filename);
    return StreamUtils.readIntoString(reader);
  }

  /**
   *
   * StandardCharsets.UTF_8 wird angewendet.
   *
   * @param path        Pfad
   * @return            Liste
   * @throws IOException  übliche
   */
  public static List<String> readLinesFromFile(final String path) throws IOException {
    return Files.readAllLines(Paths.get(path));
  }

  /**
   * StandardCharsets.UTF_8 wird angewendet.
   *
   * @param pathname          Pfad
   * @param withoutPunktuaton Wenn true, werden Satzzeichen überlesen
   * @return                  Liste
   * @throws FileNotFoundException  übliche
   */
  public static
    List<String>
    readWordsFromFile(final String pathname, final boolean withoutPunktuaton)
      throws FileNotFoundException {
    final List<String> words = new ArrayList<>();
    final Scanner sc = new Scanner(new File(pathname));
    if (withoutPunktuaton) {
      sc.useDelimiter("[\\p{Punct}\\s«»]+");
    }
    sc.forEachRemaining(word ->
    {
      words.add(word);

    });
    return words;
  }

  /**
   * Liest einen String aus der Zwischenablage und zerlegt ihn in
   * einzelne Zeilen.
   *
   * @return	Liste der Zeilen, nicht null
   */
  public static List<String> readLinesFromClip() {
    final String s = StringUtils.readClipboard();
    if (s == null)
      return Collections.emptyList();
    final String[] list = s.split(LINE_SEP_PAT);
    return Arrays.asList(list);
  }

  /**
   * Macht aus einem String eine Tabelle. \n (Unix), \r\n (Windows) oder
   * \r (Mac) leitet eine neue Zeile, \t eine neue Spalte ein.
   *
   * @param s		auch null
   * @return		eine Tabelle als Array mit 2 Parametern,
   */
  public static String[][] makeTable(final String s) {
    if (s == null)
      return new String[0][0];
    final String[] lines = s.split(LINE_SEP_PAT);
    final String[][] table = new String[lines.length][];
    for (int i = 0; i < lines.length; i++) {
      table[i] = lines[i].split("\t");
    }
    return table;
  }

  public static String[][] transposeTable(final String[][] source) {
    final int rows = source.length;
    int cols = 0;
    // Spaltenzahl ermitteln:
    for (int i = 0; i < rows; i++) {
      final String[] row = source[i];
      final int actualCols = row == null ? 0 : row.length;
      cols = Integer.max(cols, actualCols);
    }
    final String[][] dest = new String[cols][rows];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        dest[j][i] = getCellAt(source, i, j);
      }
    }
    return dest;
  }

  public static <V> String table2String(final V[][] table) {
    if (table == null)
      return "";
    final List<String> retList = new ArrayList<>();
    for (int i = 0; i < table.length; i++) {
      final V[] line = table[i];
      final String concatenated = concatenateTab(line);
      concatenated.replaceAll("(\r|\n)+", "");
      retList.add(concatenated);
    }
    return concatenate(retList);
  }

  /**
   * Macht aus dem Clipboard (kann auch null sein)
   * eine Tabelle. \n leitet eine
   * neue Zeile, \t eine neue Spalte ein.
   *
   *  @return      eine Tabelle als Array mit 2 Parametern,
   */
  public static String[][] readTableFromClip() {
    final String s = readClipboard();
    return makeTable(s);
  }

  /**
   * Macht aus dem Inhalt der Datei (diese muss nicht existieren)
   * eine Tabelle. \n leitet eine
   * neue Zeile, \t eine neue Spalte ein.
   * @param fileName Dateiname
   *
   *  @return      eine Tabelle als Array mit 2 Parametern, eventuell leer
   *
   */
  public static String[][] readTable(final String fileName) {
    String s;
    try {
      s = readIntoString(fileName);
    } catch (final FileNotFoundException e) {
      s = null;
    }
    return makeTable(s);
  }

  /**
   * Null-sichere Methode, ein 2-dimensionales Array auszulesen.
   *
   * @param table		beliebig
   * @param row		beliebig, für echte Tabellen bei 0 beginnend
   * @param column	beliebig, für echte Tabellen bei 0 beginnend
   * @param <V>		Typ
   * @return			Wert oder null
   */
  public static <V> V getCellAt(final V[][] table, final int row, final int column) {
    if (table == null)
      return null;
    if (row < 0 || column < 0 || row >= table.length)
      return null;
    final V[] line = table[row];
    if (column >= line.length)
      return null;
    return line[column];
  }

  /**
   * Null-sichere Methode, eine Excel-Tabelle, gegeben als
   * 2-dimensionales Array auszulesen.
   *
   *@param table		beliebig
   * @param spalte	beliebig, für echte Tabellen bei 'A' beginnend
   * @param zeile		beliebig, für echte Tabellen bei 1 beginnend
   * @param <V>		Typ
   * @return			Wert oder null
   */
  public static <V> V getExcelCellAt(final V[][] table, final char spalte, final int zeile) {
    return getCellAt(table, zeile - 1, spalte - 'A');
  }

  /**
   * Null-sichere Methode, eine Excel-Tabelle, gegeben als
   * 2-dimensionales Array auszulesen.
   *
   *@param table    beliebig
   * @param spalte  beliebig, für echte Tabellen bei 'A' beginnend
   * @param zeile   beliebig, für echte Tabellen bei 1 beginnend
   * @param <V>   Typ
   * @return      Wert oder null
   */
  public static <
      V>
    void
    setExcelCellAt(final V[][] table, final V cell, final char spalte, final int zeile) {
    if (table == null)
      return;
    final int column = spalte - 'A';
    final int row = zeile - 1;
    if (row < 0 || column < 0)
      throw new IllegalArgumentException("Zeile oder Spalte negativ");
    if (zeile > table.length)
      throw new IllegalArgumentException("Diese Zeile gibt es nicht: " + zeile);
    final V[] line = table[row];
    if (column >= line.length)
      throw new IllegalArgumentException(
        "Diese Spalte " + "in Zeile " + zeile + " gibt es nicht: " + spalte);
    line[column] = cell;
  }

  public static String readConsole() {
    final Scanner scanner = new Scanner(System.in);
    final String s = scanner.nextLine();
    MyFileUtils.safeClose(scanner);
    return s;
  }

  /**
   * Schreibt einen String in die Zwischenablage.
   *
   * @param s	nicht null.
   */
  public static void writeToClipboard(final String s) {
    RangeCheckUtils.assertReferenceParamNotNull("s", s);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
  }

  /**
   * @param repeat  number of times to repeat delim
   * @param padChar  character to repeat
   * @return String with repeated character
   */
  public static String padding(final int repeat, final char padChar) {
    if (repeat < 0) {
      throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
    }
    final char[] buf = new char[repeat];
    Arrays.fill(buf, padChar);
    return new String(buf);
  }

  /**
   * @param repeat  number of times to repeat string
   * @param string  string to repeat
   * @return repeated string
   */
  public static String repeat(final int repeat, final String string) {
    if (repeat < 0) {
      throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
    }
    final StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < repeat; i++) {
      buffer.append(string);
    }
    return buffer.toString();
  }

  /**
   * Füllt String links mit padChar auf bis zur gewünschten Länge minLength.
   * @param s				nicht null
   * @param minLength		beliebig
   * @param padChar		beliebig
   * @return	s, wenn s.length()  >minLength; sonst s aufgefüllt.
   */
  public static String leftPadding(final String s, final int minLength, final char padChar) {
    RangeCheckUtils.assertReferenceParamNotNull("s", s);
    final int sLength = s.length();
    if (sLength >= minLength)
      return s;
    else
      return padding(minLength - sLength, padChar) + s;
  }

  /**
   * Füllt String rechts mit padChar auf bis zur gewünschten Länge minLength.
   * @param s             nicht null
   * @param minLength     beliebig
   * @param padChar       beliebig
   * @return  s, wenn s.length()  >minLength; sonst s aufgefüllt.
   */
  public static String rightPadding(final String s, final int minLength, final char padChar) {
    RangeCheckUtils.assertReferenceParamNotNull("s", s);
    final int sLength = length(s);
    if (sLength >= minLength)
      return s;
    else
      return s + padding(minLength - sLength, padChar);
  }

  public static String cut(String s, final int maxChars) {
    if (s.length() <= maxChars)
      return s;
    else {
      s = s.substring(0, maxChars - 2) + "..";
      return s;
    }
  }

  /**
   * Sortiert eine Liste von Strings nach den ersten 3 Zeichen (Tags).
   * @param strings	nicht null.
   */
  public static void tagSort(final List<String> strings) {
    RangeCheckUtils.assertReferenceParamNotNull("strings", strings);
    final Predicate<String> predicate = new Predicate<String>() {
      @Override
      public boolean test(final String string) {
        return string != null && string.length() > 3;
      }
    };
    FilterUtils.filter(strings, predicate);
    final Comparator<String> comparator = new Comparator<String>() {

      @Override
      public int compare(final String o1, final String o2) {
        final String prefix1 = o1.substring(0, 2);
        final String prefix2 = o2.substring(0, 2);
        return prefix1.compareTo(prefix2);
      }
    };
    Collections.sort(strings, comparator);
  }

  /**
   * Macht eine Objekt-Liste zu einem einzigen String mit
   * Zeilenumbrüchen.
   *
   * @param objects	nicht null
   * @return			String mit Umbrüchen, nicht null.
   */
  public static String concatenate(final Iterable<? extends Object> objects) {
    RangeCheckUtils.assertReferenceParamNotNull("strings", objects);
    return concatenate(Constants.LINE_SEPARATOR, objects);
  }

  /**
   * Macht eine Objekt-Liste zu einem einzigen String mit
   * Trennung durch separator.
   *
   * @param objects	nicht null
   * @return			String mit Separator, nicht null.
   */
  public static String concatenate(final String separator, final Object... objects) {
    RangeCheckUtils.assertReferenceParamNotNull("separator", separator);
    RangeCheckUtils.assertArrayParamNotNullOrEmpty("strings", objects);
    return concatenate(separator, Arrays.asList(objects));
  }

  /**
   * Macht eine Objekt-Liste zu einem einzigen String mit
   * Trennung durch TAB.
   *
   * @param objects nicht null
   * @return      String mit Tabulatoren, nicht null.
   */
  public static String concatenateTab(final Object... objects) {
    return concatenate("\t", objects);
  }

  /**
   * Macht eine iterable-Liste zu einem einzigen String mit
   * Trennung durch TAB.
   *
   * @param iterable nicht null
   * @return      String mit Tabulatoren, nicht null.
   */
  public static String concatenateTab(final Iterable<?> iterable) {
    return concatenate("\t", iterable);
  }

  /**
   * Macht eine Objekt-Liste zu einem einzigen String mit
   * Trennung durch separator.
   * @param separator beliebig
   * @param objects	nicht null
   *
   * @return			String mit separator, nicht null.
   */
  public static
    String
    concatenate(final String separator, final Iterable<? extends Object> objects) {
    return concatenate(separator, objects, null);
  }

  static final Function<Object, String> OBJ_TO_STRING = new Function<Object, String>() {
    @Override
    public String apply(final Object obj) {
      if (obj == null)
        return "null";
      return obj.toString();
    }
  };

  /**
   * Macht eine Objekt-Liste zu einem einzigen String mit
   * Trennung durch separator.
   *
   * @param separator		nicht null
   * @param objects		nicht null
   * @param t2String		Umwandlungsfunktion objects -> String; wenn null,
   * 						wird object.toString() genommen.
   * @param <T>			Typ der Objekte
   * @return				Liste, die durch separator getrennt ist
   */
  public static <T> String concatenate(
    final String separator,
    final Iterable<T> objects,
    final Function<? super T, String> t2String) {

    RangeCheckUtils.assertReferenceParamNotNull("separator", separator);
    RangeCheckUtils.assertReferenceParamNotNull("strings", objects);

    Function<? super T, String> function;
    if (t2String == null)
      function = OBJ_TO_STRING;
    else
      function = t2String;

    String s = "";
    for (final Iterator<T> iterator = objects.iterator(); iterator.hasNext();) {
      final T obj = iterator.next();
      s += function.apply(obj);
      if (iterator.hasNext())
        s += separator;
    }
    return s;
  }

  /**
   * Macht eine Objekt-Liste zu einem einzigen String mit
   * Zeilenumbrüchen.
   *
   * @param objects	nicht null
   * @return			String mit Umbrüchen, nicht null.
   */
  public static <T> String concatenate(
    final String separator,
    final Function<? super T, String> t2String,
    final T... objects) {
    RangeCheckUtils.assertReferenceParamNotNull("separator", separator);
    RangeCheckUtils.assertArrayParamNotNullOrEmpty("strings", objects);
    return concatenate(separator, Arrays.asList(objects), t2String);
  }

  /**
   * Ersetzt nicht druckbare Zeichen durch eine Darstellung <<.>>.
   * @param original	nicht null
   * @return			nicht null
   */
  public static String replaceImprintables(final String original) {
    String s = original;
    s = s.replace(Constants.GS, "<<GS>>");
    s = s.replace(Constants.RS, "<<RS>>");
    s = s.replace(Constants.US, "<<US>>");
    return s;
  }

  /**
   * Sichere equals-Methode.
   * @param obj1	beliebig.
   * @param obj2	beliebig.
   * @return	gleich oder ungleich (null == null)
   */
  public static boolean equals(final Object obj1, final Object obj2) {
    if (obj1 == null) {
      return obj2 == null;
    }
    return obj1.equals(obj2);
  }

  /**
   * Entfernt die Zeichen 152 (Start Of String) und 156 (String Terminator).
   *
   * @param txt	nicht null
   * @return		Eingabestring ohne nichtdruckbare Zeichen
   */
  public static String cleanMarc(final String txt) {

    final char c156 = 156;
    final String s156 = Character.toString(c156);

    final char c152 = 152;
    final String s152 = Character.toString(c152);

    String txt1 = txt.replace(s156, "");
    txt1 = txt1.replace(s152, "");
    txt1 = txt1.replaceAll("[\\p{Cntrl}]", "");
    return txt1;
  }

  /**
   * Gibt die Stringlänge, auch für seltsame Unicode-Strings, bei
   * denen der Cursor nach links wandert.
   *
   * @param s auch null
   * @return  Stringlänge
   */
  public static int length(final String s) {
    if (s == null)
      return 0;
    int l = 0;
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      final byte dir = Character.getDirectionality(c);
      //            System.err.println(dir);
      if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT) {
        l--;
      } else if (dir == 8 || dir == 9) {
        //nix
      } else {
        l++;
      }
    }

    return l;
  }

  /**
   * Einfache nullsichere Methode für die Stringlänge.
   *
   * @param s auch null
   * @return  Stringlänge oder 0 für null
   */
  public static int lengthNullSafe(final CharSequence s) {
    if (s == null)
      return 0;
    return s.length();
  }

  /**
   * Zählt die anzahl der Zeichen c im String s.
   *
   * @param s auch null
   * @param c beliebig
   * @return  Anzahl
   */
  public static int countCharacter(final CharSequence s, final char c) {
    if (s == null)
      return 0;
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c)
        count++;
    }
    return count;
  }

  /**
   *
   * @param s         auch null
   * @param pattern   nicht null
   * @return          die Anzahl der Treffer
   */
  public static int countMatches(final String s, final Pattern pattern) {
    Objects.requireNonNull(pattern);
    if (s == null)
      return 0;
    return getMatches(s, pattern).size();
  }

  /**
   *
   * @param s         auch null
   * @param subString nicht null, kann auch Sonderzeichen ($ ...)
   *                  enthalten, diese werden wörtlich genommen
   * @return          die Anzahl der Substrings, dazu
   *                  wird subString "gequoted"
   */
  public static int countMatches(final String s, final String subString) {
    Objects.requireNonNull(subString);
    final String quoted = Pattern.quote(subString);
    final Pattern pattern = Pattern.compile(quoted, Pattern.MULTILINE);
    return countMatches(s, pattern);
  }

  /**
   * Sucht trunkiert, wobei "*" eine Wildcart für Anfang und/oder
   * Ende ist.
   *
   * <pre>
   * containsTruncated(null, null) = false;
   * containsTruncated("as", null) = false;
   * containsTruncated(null, "as") = false;
   * containsTruncated("", "") = false;
   * containsTruncated("as", "") = false;
   * containsTruncated("as", "as") = true;
   * containsTruncated("ast", "as") = false;
   * containsTruncated("ast", "as*") = true;
   * containsTruncated("ast", "s*") = false;
   * containsTruncated("ast", "*s") = false;
   * containsTruncated("ast", "*st") = true;
   * containsTruncated("ast", "*s*") = true;
   * </pre>
   *
   *
   * @param s     wird durchsucht; wenn null, dann false
   * @param query Suchfrage; wenn null, dann false
   * @param caseInsensitive groß/klein ist egal
   * @return      s enthält query
   */
  public static
    boolean
    containsTruncated(final String s, final String query, final boolean caseInsensitive) {
    if (query == null)
      return false;
    if (query.startsWith("*")) {
      if (query.length() >= 2 && query.endsWith("*")) {
        final String substring = query.substring(1, query.length() - 1);
        return contains(s, substring, caseInsensitive);
      } else
        return containsWordBeginTruncated(s, query.substring(1), caseInsensitive);
    }
    if (query.endsWith("*")) {
      final String subSequence = query.substring(0, query.length() - 1);
      return containsWordEndTruncated(s, subSequence, caseInsensitive);
    }
    return containsWord(s, query, caseInsensitive);
  }

  /**
   * Die Patterns für searchTerm werden gespeichert, so dass sie auch auf große
   * Datenmengen angewandt werden können.
   *
   * @param searchTerm  nicht null. Wenn ohne '*', wird wortweise gesucht,
   *                    sonst trunkiert. Vorher wird Unicode noch zerlegt.
   * @param caseInsensitive groß/klein ist egal
   * @return            Prädikat
   */
  public static
    Predicate<String>
    getSimpleTruncPredicate(final String searchTerm, final boolean caseInsensitive) {

    if (searchTerm == null)
      return new AcceptEverything<String>().negate();

    if (searchTerm.startsWith("*")) {
      return s -> contains(s, searchTerm.substring(1), caseInsensitive);
    }

    Pattern searchPattern;
    if (searchTerm.endsWith("*"))
      searchPattern =
        endTruncPattern(searchTerm.substring(0, searchTerm.length() - 1), caseInsensitive);
    else
      searchPattern = containsWortPattern(searchTerm, caseInsensitive);

    return new Predicate<String>() {
      @Override
      public boolean test(String s) {
        if (s == null)
          return false;
        else {
          s = unicodeDecomposition(s);
          final Matcher matcher = searchPattern.matcher(s);
          return matcher.find();
        }
      }
    };

  }

  /**
   * Sucht nach durch Leerzeichen abgegrenzten Teilwörtern im String.
   * Unicode-Decomposition wird bei String und Query vorgenommen.
   *
   * @param s     auch null
   * @param query  auch null
   * @param caseInsensitive groß/klein ist egal
   * @return      ist word in s als Einzelwort enthalten?
   */
  public static boolean containsWord(String s, String query, final boolean caseInsensitive) {
    if (s == null)
      return false;
    if (query == null)
      return false;
    s = unicodeDecomposition(s);
    query = unicodeDecomposition(query);
    final Pattern pattern = containsWortPattern(query, caseInsensitive);
    final Matcher matcher = pattern.matcher(s);
    return matcher.find();
  }

  /**
   *
   * @param   query nicht null, Unicode-Decomposition wird vorgenommen
   * @param   caseInsensitive groß/klein ist egal
   * @return  Ein Pattern. Dieses kann auf einen String angewandt werden.
   *          Ist query in einem String als Einzelwort enthalten?
   */
  public static Pattern containsWortPattern(final String query, final boolean caseInsensitive) {
    String qword = unicodeDecomposition(Pattern.quote(query));
    qword = "(\\s|^|\\p{Punct})" + qword + "(\\s|$|\\p{Punct})";
    int flag = Pattern.MULTILINE;
    if (caseInsensitive) {
      flag += Pattern.CASE_INSENSITIVE;
      flag += Pattern.UNICODE_CASE;
    }
    final Pattern pattern = Pattern.compile(qword, flag);
    return pattern;
  }

  /**
   * Sucht nach mit Leer- oder Sonderzeichen beginnenden Teilwörtern im String.
   * Unicode-Decomposition wird bei String und Query vorgenommen.
   *
   * @param s     auch null -> false
   * @param query  auch null  -> false
   * @param caseInsensitive groß/klein ist egal
   * @return      ist query in s als Präfix eines Einzelwortes enthalten?
   */
  public static
    boolean
    containsWordEndTruncated(String s, final String query, final boolean caseInsensitive) {
    if (s == null)
      return false;
    if (query == null)
      return false;
    s = unicodeDecomposition(s);
    final Pattern pattern = endTruncPattern(query, caseInsensitive);
    final Matcher matcher = pattern.matcher(s);
    return matcher.find();
  }

  /**
   * Sucht nach mit Leer- oder Sonderzeichen endenden Teilwörtern im String.
   * Unicode-Decomposition wird bei String und Query vorgenommen.
   *
   * @param s     auch null -> false
   * @param query  auch null  -> false
   * @param caseInsensitive groß/klein ist egal
   * @return      ist query in s als Präfix eines Einzelwortes enthalten?
   */
  public static
    boolean
    containsWordBeginTruncated(String s, final String query, final boolean caseInsensitive) {
    if (s == null)
      return false;
    if (query == null)
      return false;
    s = unicodeDecomposition(s);
    final Pattern pattern = beginTruncPattern(query, caseInsensitive);
    final Matcher matcher = pattern.matcher(s);
    return matcher.find();
  }

  /**
   * @param query           nicht null. Unicode-Decomposition wird vorgenommen.
   * @param caseInsensitive groß/klein ist egal
   * @return                Pattern, das auf einen String angewandt werden
   *                        kann: Ist query in einem String als Präfix eines
   *                        Einzelwortes enthalten? Endetrunkierung.
   */
  public static Pattern endTruncPattern(final String query, final boolean caseInsensitive) {
    String qword = unicodeDecomposition(Pattern.quote(query));
    qword = "(\\s|^|\\p{Punct})" + qword;
    int flag = Pattern.MULTILINE;
    if (caseInsensitive) {
      flag += Pattern.CASE_INSENSITIVE;
      flag += Pattern.UNICODE_CASE;
    }
    final Pattern pattern = Pattern.compile(qword, flag);
    return pattern;
  }

  /**
   * @param query           nicht null. Unicode-Decomposition wird vorgenommen.
   * @param caseInsensitive groß/klein ist egal
   * @return                Pattern, das auf einen String angewandt werden
   *                        kann: Ist query in einem String als Postfix eines
   *                        Einzelwortes enthalten? Anfangstrunkierung.
   */
  public static Pattern beginTruncPattern(final String query, final boolean caseInsensitive) {
    String qword = unicodeDecomposition(Pattern.quote(query));
    qword = qword + "(\\s|$|\\p{Punct})";
    int flag = Pattern.MULTILINE;
    if (caseInsensitive) {
      flag += Pattern.CASE_INSENSITIVE;
      flag += Pattern.UNICODE_CASE;
    }
    final Pattern pattern = Pattern.compile(qword, flag);
    return pattern;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {

    final Set<String> stpw =
      new HashSet<>(readLinesFromFile("D:/Analysen/baumann/skurriles/stop_words_de_complete.txt"));
    stpw.addAll(readLinesFromFile("D:/Analysen/baumann/skurriles/stop_words_en_complete.txt"));

    final String s = readClipboard();

    System.out.println(stichwortMenge(s, stpw));
    System.out.println(stichwortListe(s, stpw));

  }

  /**
   * Gibt alle Zeichen eines Strings, deren Integerwert und deren
   * Hexcode aus.
   *
   * @param s nicht null
   */
  public static void debugString(final String s) {
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      System.out.println(c + "/" + (int) c + "/0x" + Integer.toHexString(c) + "/Dir: "
        + Character.getDirectionality(c));
    }
  }

  /**
   * Macht aus einer Liste eine Excel-Zelle mit Umbrüchen. Anführungszeichen werden erhalten,
   * da eines durch zwei ersetzt wird (Excel-Besonderheit)
   *
   * @param collection	beliebig
   * @return				Excel-Zelle; "null", wenn collection null oder leer
   */
  public static String makeExcelCellFromCollection(final Collection<?> collection) {
    if (collection == null)
      return "\"null\"";
    if (collection.isEmpty())
      return "\"null\"";
    String concatenated = StringUtils.concatenate("" + (char) 10, collection);
    concatenated = concatenated.replace("\"", "\"\"");
    String s = "\"" + concatenated + "\"";
    // sicherheitshalber:
    s = s.replace("\t", "");
    return s;
  }

  /**
   * Macht aus einem Object eine Excel-Zelle. Wenn eine das Objekt eine
   * Collection ist, dann mit Umbrüchen. Anführungszeichen werden erhalten,
   * da eines durch zwei ersetzt wird (Excel-Besonderheit)
   *
   * @param object        beliebig
   * @return              Excel-Zelle; "null", wenn null.
   */
  public static String makeExcelCell(final Object object) {
    if (object == null)
      return "\"null\"";
    if (object instanceof Collection<?>)
      return makeExcelCellFromCollection((Collection<?>) object);
    String s = object.toString();
    // sicherheitshalber:
    s = s.replace("\t", "");
    s = s.replace("\"", "\"\"");
    s = "\"" + s + "\"";

    return s;
  }

  /**
   * 
   * @param objects	Sammlung von Objekten. Werden mittels #{@link #toString()} umgewandelt. 
   * 					Ausnahme: Wenn eines der Objekte selbst wieder eine Collection ist.
   * @return	tsv-Zeile, die in Excel eingefügt werden kann. Ist eines der Objekte eine Collection,
   * 			so wird es mittels {@link #makeExcelCellFromCollection(Collection)} in einen String
   * 			mit Zeilenumbrüchen umgewandelt.
   */
  public static String makeExcelLine(final Object... objects) {
    final Collection<Object> collection = Arrays.asList(objects);
    return concatenate("\t", collection, StringUtils::makeExcelCell);
  }

  /**
   * 
   * @param collection	Sammlung von Objekten. Werden mittels #{@link #toString()} umgewandelt. 
   * 					Ausnahme: Wenn eines der Objekte selbst wieder eine Collection ist.
   * @return	tsv-Zeile, die in Excel eingefügt werden kann. Ist eines der Objekte eine Collection,
   * 			so wird es mittels {@link #makeExcelCellFromCollection(Collection)} in einen String
   * 			mit Zeilenumbrüchen umgewandelt.
   */
  public static String makeExcelLine(final Collection<?> collection) {
    return concatenate("\t", collection, StringUtils::makeExcelCell);
  }

  /**
   *
   * @param s
   * @param fractionSize
   * @return
   */
  public static
    Collection<String>
    splitStringInEqualFractions(final String s, final int fractionSize) {
    Objects.requireNonNull(s);
    if (fractionSize < 0)
      throw new IllegalArgumentException("fractionSize < 0");
    final Collection<String> strings = new LinkedList<>();
    final StringReader reader = new StringReader(s);
    final char[] buf = new char[fractionSize];
    int count;
    try {
      while ((count = reader.read(buf)) != -1) {
        final String fraction = new String(buf, 0, count);
        strings.add(fraction);
      }
    } catch (final IOException e) {
      // nix
    }
    return strings;
  }

  /**
  * The empty String {@code ""}.
  */
  public static final String EMPTY = "";

  /**
   * Represents a failed index search.
   */
  public static final int INDEX_NOT_FOUND = -1;

  // Difference
  //-----------------------------------------------------------------------
  /**
   * <p>Compares two Strings, and returns the portion where they differ.
   * More precisely, return the remainder of the second String,
   * starting from where it's different from the first. This means that
   * the difference between "abc" and "ab" is the empty
   * String and not "c". </p>
   *
   * <p>For example,
   * {@code difference("i am a machine", "i am a robot") -> "robot"}.</p>
   *
   * <pre>
   * StringUtils.difference(null, null) = null
   * StringUtils.difference("", "") = ""
   * StringUtils.difference("", "abc") = "abc"
   * StringUtils.difference("abc", "") = ""
   * StringUtils.difference("abc", "abc") = ""
   * StringUtils.difference("abc", "ab") = ""
   * StringUtils.difference("ab", "abxyz") = "xyz"
   * StringUtils.difference("abcde", "abxyz") = "xyz"
   * StringUtils.difference("abcde", "xyz") = "xyz"
   * </pre>
   *
   * @param str1  the first String, may be null
   * @param str2  the second String, may be null
   * @return the portion of str2 where it differs from str1; returns the
   * empty String if they are equal
   * @see #indexOfDifference(CharSequence,CharSequence)
   * @since 2.0
   */
  public static String difference(final String str1, final String str2) {
    if (str1 == null) {
      return str2;
    }
    if (str2 == null) {
      return str1;
    }
    final int at = indexOfDifference(str1, str2);
    if (at == INDEX_NOT_FOUND) {
      return EMPTY;
    }
    return str2.substring(at);
  }

  /**
   * <p>Compares two CharSequences, and returns the index at which the
   * CharSequences begin to differ.</p>
   *
   * <p>For example,
   * {@code indexOfDifference("i am a machine", "i am a robot") -> 7}</p>
   *
   * <pre>
   * StringUtils.indexOfDifference(null, null) = -1
   * StringUtils.indexOfDifference("", "") = -1
   * StringUtils.indexOfDifference("", "abc") = 0
   * StringUtils.indexOfDifference("abc", "") = 0
   * StringUtils.indexOfDifference("abc", "abc") = -1
   * StringUtils.indexOfDifference("ab", "abxyz") = 2
   * StringUtils.indexOfDifference("abcde", "abxyz") = 2
   * StringUtils.indexOfDifference("abcde", "xyz") = 0
   * </pre>
   *
   * @param cs1  the first CharSequence, may be null
   * @param cs2  the second CharSequence, may be null
   * @return the index where cs1 and cs2 begin to differ; -1 if they are equal
   */
  public static int indexOfDifference(final CharSequence cs1, final CharSequence cs2) {
    if (cs1 == cs2) {
      return INDEX_NOT_FOUND;
    }
    if (cs1 == null || cs2 == null) {
      return 0;
    }
    int i;
    for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
      if (cs1.charAt(i) != cs2.charAt(i)) {
        break;
      }
    }
    if (i < cs2.length() || i < cs1.length()) {
      return i;
    }
    return INDEX_NOT_FOUND;
  }

  /**
   * <p>Compares all Strings in a List and returns the index at
   * which the Strings begin to differ.</p>
   *
   * <p>For example,
   * <code>indexOfDifference("i am a machine", "i am a robot"})
   *  -&gt; 7</code></p>
   *
   * <pre>
   * StringUtils.indexOfDifference(null) = -1
   * StringUtils.indexOfDifference() = -1
   * StringUtils.indexOfDifference("abc") = -1
   * StringUtils.indexOfDifference(null, null) = -1
   * StringUtils.indexOfDifference("", "") = -1
   * StringUtils.indexOfDifference("", null) = 0
   * StringUtils.indexOfDifference("abc", null, null) = 0
   * StringUtils.indexOfDifference(null, null, "abc") = 0
   * StringUtils.indexOfDifference("", "abc") = 0
   * StringUtils.indexOfDifference("abc", "") = 0
   * StringUtils.indexOfDifference("abc", "abc") = -1
   * StringUtils.indexOfDifference("abc", "a") = 1
   * StringUtils.indexOfDifference("ab", "abxyz") = 2
   * StringUtils.indexOfDifference("abcde", "abxyz") = 2
   * StringUtils.indexOfDifference("abcde", "xyz") = 0
   * StringUtils.indexOfDifference("xyz", "abcde") = 0
   * StringUtils.indexOfDifference("i am a machine", "i am a robot") = 7
   * </pre>
   *
   * @param strings  List of Strings, entries may be null
   * @return the index where the strings begin to differ;
   * -1 if they are all equal
   */
  public static int indexOfDifference(final String... strings) {
    return indexOfDifference(Arrays.asList(strings));
  }

  /**
   * <p>Compares all Strings in a List and returns the index at
   * which the Strings begin to differ.</p>
   *
   * <p>For example,
   * <code>indexOfDifference("i am a machine", "i am a robot"})
   *  -&gt; 7</code></p>
   *
   * <pre>
   * StringUtils.indexOfDifference(null) = -1
   * StringUtils.indexOfDifference() = -1
   * StringUtils.indexOfDifference("abc") = -1
   * StringUtils.indexOfDifference(null, null) = -1
   * StringUtils.indexOfDifference("", "") = -1
   * StringUtils.indexOfDifference("", null) = 0
   * StringUtils.indexOfDifference("abc", null, null) = 0
   * StringUtils.indexOfDifference(null, null, "abc") = 0
   * StringUtils.indexOfDifference("", "abc") = 0
   * StringUtils.indexOfDifference("abc", "") = 0
   * StringUtils.indexOfDifference("abc", "abc") = -1
   * StringUtils.indexOfDifference("abc", "a") = 1
   * StringUtils.indexOfDifference("ab", "abxyz") = 2
   * StringUtils.indexOfDifference("abcde", "abxyz") = 2
   * StringUtils.indexOfDifference("abcde", "xyz") = 0
   * StringUtils.indexOfDifference("xyz", "abcde") = 0
   * StringUtils.indexOfDifference("i am a machine", "i am a robot") = 7
   * </pre>
   *
   * @param strings  List of Strings, entries may be null
   * @return the index where the strings begin to differ;
   * -1 if they are all equal
   */
  public static int indexOfDifference(final List<String> strings) {
    if (strings == null || strings.size() <= 1) {
      return INDEX_NOT_FOUND;
    }
    boolean anyStringNull = false;
    boolean allStringsNull = true;
    final int listLen = strings.size();
    int shortestStrLen = Integer.MAX_VALUE;
    int longestStrLen = 0;

    // find the min and max string lengths; this avoids checking to make
    // sure we are not exceeding the length of the string each time through
    // the bottom loop.
    for (int i = 0; i < listLen; i++) {
      if (strings.get(i) == null) {
        anyStringNull = true;
        shortestStrLen = 0;
      } else {
        allStringsNull = false;
        shortestStrLen = Math.min(strings.get(i).length(), shortestStrLen);
        longestStrLen = Math.max(strings.get(i).length(), longestStrLen);
      }
    }

    // handle lists containing all nulls or all empty strings
    if (allStringsNull || longestStrLen == 0 && !anyStringNull) {
      return INDEX_NOT_FOUND;
    }

    // handle lists containing some nulls or some empty strings
    if (shortestStrLen == 0) {
      return 0;
    }

    // find the position with the first difference across all strings
    int firstDiff = -1;
    for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
      final char comparisonChar = strings.get(0).charAt(stringPos);
      for (int listPos = 1; listPos < listLen; listPos++) {
        if (strings.get(listPos).charAt(stringPos) != comparisonChar) {
          firstDiff = stringPos;
          break;
        }
      }
      if (firstDiff != -1) {
        break;
      }
    }

    if (firstDiff == -1 && shortestStrLen != longestStrLen) {
      // we compared all of the characters up to the length of the
      // shortest string and didn't find a match, but the string lengths
      // vary, so return the length of the shortest string.
      return shortestStrLen;
    }
    return firstDiff;
  }

  /**
   * <p>Compares all Strings in a List and returns the initial sequence of
   * characters that is common to all of them.</p>
   *
   * <p>For example,
   * <code>getCommonPrefix("i am a machine", "i am a robot"}) -&gt;
   * "i am a "</code></p>
   *
   * <pre>
   * StringUtils.getCommonPrefix(null) = ""
   * StringUtils.getCommonPrefix() = ""
   * StringUtils.getCommonPrefix("abc") = "abc"
   * StringUtils.getCommonPrefix(null, null) = ""
   * StringUtils.getCommonPrefix("", "") = ""
   * StringUtils.getCommonPrefix("", null) = ""
   * StringUtils.getCommonPrefix("abc", null, null) = ""
   * StringUtils.getCommonPrefix(null, null, "abc") = ""
   * StringUtils.getCommonPrefix("", "abc") = ""
   * StringUtils.getCommonPrefix("abc", "") = ""
   * StringUtils.getCommonPrefix("abc", "abc") = "abc"
   * StringUtils.getCommonPrefix("abc", "a") = "a"
   * StringUtils.getCommonPrefix("ab", "abxyz") = "ab"
   * StringUtils.getCommonPrefix("abcde", "abxyz") = "ab"
   * StringUtils.getCommonPrefix("abcde", "xyz") = ""
   * StringUtils.getCommonPrefix("xyz", "abcde") = ""
   * StringUtils.getCommonPrefix("i am a machine", "i am a robot"}) =
   * "i am a "
   * </pre>
   *
   * @param strs  List of String objects, may be null, entries may be null
   * @return the initial sequence of characters that are common to all Strings
   * in the List; empty String if the List is null, the elements are all
   * null or if there is no common prefix.
   */
  public static String getCommonPrefix(final List<String> strs) {
    if (strs == null || strs.size() == 0) {
      return EMPTY;
    }
    final int smallestIndexOfDiff = indexOfDifference(strs);
    if (smallestIndexOfDiff == INDEX_NOT_FOUND) {
      // all strings were identical
      if (strs.get(0) == null) {
        return EMPTY;
      }
      return strs.get(0);
    } else if (smallestIndexOfDiff == 0) {
      // there were no common initial characters
      return EMPTY;
    } else {
      // we found a common initial character sequence
      return strs.get(0).substring(0, smallestIndexOfDiff);
    }
  }

  /**
   *
   * Zahlen vor Buchstaben, Umlaute einsortiert.
   *
   * @return Einen Comparator, der Zahlen und Umlaute richtig sortiert
   */
  @SuppressWarnings("unchecked")
  public static Comparator<String> getGermanComparator() {
    final String EXT_RULES =
      "< ' ' < '.'" + "<0<1<2<3<4<5<6<7<8<9<a,A<b,B<c,C<d,D<ð,Ð<e,E<f,F<g,G<h,H<i,I<j"
        + ",J<k,K<l,L<m,M<n,N<o,O<p,P<q,Q<r,R<s, S & SS,ß<t,T& TH, Þ &TH,"
        + "þ <u,U<v,V<w,W<x,X<y,Y<z,Z&AE,Æ&AE,æ&OE,Œ&OE,œ";
    final RuleBasedCollator germanCollator =
      (RuleBasedCollator) Collator.getInstance(Locale.GERMAN);
    RuleBasedCollator extGermanCollator;
    try {
      extGermanCollator = new RuleBasedCollator(germanCollator.getRules() + EXT_RULES);
    } catch (final ParseException e) {
      return null;
    }
    return (Comparator) extGermanCollator;
  }

  /**
   * <p>Compares all Strings in a List and returns the initial sequence of
   * characters that is common to all of them.</p>
   *
   * <p>For example,
   * <code>getCommonPrefix("i am a machine", "i am a robot"}) -&gt;
   * "i am a "</code></p>
   *
   * <pre>
   * StringUtils.getCommonPrefix(null) = ""
   * StringUtils.getCommonPrefix() = ""
   * StringUtils.getCommonPrefix("abc") = "abc"
   * StringUtils.getCommonPrefix(null, null) = ""
   * StringUtils.getCommonPrefix("", "") = ""
   * StringUtils.getCommonPrefix("", null) = ""
   * StringUtils.getCommonPrefix("abc", null, null) = ""
   * StringUtils.getCommonPrefix(null, null, "abc") = ""
   * StringUtils.getCommonPrefix("", "abc") = ""
   * StringUtils.getCommonPrefix("abc", "") = ""
   * StringUtils.getCommonPrefix("abc", "abc") = "abc"
   * StringUtils.getCommonPrefix("abc", "a") = "a"
   * StringUtils.getCommonPrefix("ab", "abxyz") = "ab"
   * StringUtils.getCommonPrefix("abcde", "abxyz") = "ab"
   * StringUtils.getCommonPrefix("abcde", "xyz") = ""
   * StringUtils.getCommonPrefix("xyz", "abcde") = ""
   * StringUtils.getCommonPrefix("i am a machine", "i am a robot"}) =
   * "i am a "
   * </pre>
   *
   * @param strs  List of String objects, entries may be null
   * @return the initial sequence of characters that are common to all Strings
   * in the List; empty String if the List is null, the elements are all
   * null or if there is no common prefix.
   */
  public static String getCommonPrefix(final String... strs) {
    return getCommonPrefix(Arrays.asList(strs));
  }

  // Reversing
  //-----------------------------------------------------------------------
  /**
   * <p>Reverses a String as per {@link StringBuilder#reverse()}.</p>
   *
   * <p>A {@code null} String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.reverse(null)  = null
   * StringUtils.reverse("")    = ""
   * StringUtils.reverse("bat") = "tab"
   * </pre>
   *
   * @param str  the String to reverse, may be null
   * @return the reversed String, {@code null} if null String input
   */
  public static String reverse(final String str) {
    if (str == null) {
      return null;
    }
    return new StringBuilder(str).reverse().toString();
  }

  /**
   * Kehrt eine Liste von Strings um.
   *
   * @param strings   auch null
   * @return          neue Liste oder null
   */
  public static Iterable<String> reverse(final Iterable<String> strings) {
    if (strings == null)
      return null;
    return FilterUtils.map(strings, StringUtils::reverse);
  }

  /**
   *
   * @param stringList   nicht null
   * @return          längsten String aus der Liste strings. null,
   *                  wenn die Liste nur aus nullen besteht, oder, wenn
   *                  sie leer ist
   */
  public static String getLongestString(final List<String> stringList) {
    RangeCheckUtils.assertReferenceParamNotNull("strings", stringList);
    if (stringList.isEmpty())
      return null;
    String longest = stringList.get(0);
    int maxLength = 0;
    for (final String actualString : stringList) {
      final int actualLength = lengthNullSafe(actualString);
      if (actualLength > maxLength) {
        maxLength = actualLength;
        longest = actualString;
      } else if (longest == null) {
        longest = actualString;
      }
    }
    return longest;
  }

  /**
   *
   * @param stringList   nicht null
   * @return          kürzesten String aus der Liste stringsnull,
   *                  wenn die Liste nur aus nullen besteht, oder, wenn
   *                  sie leer ist
   */
  public static String getShortestString(final List<String> stringList) {
    RangeCheckUtils.assertReferenceParamNotNull("strings", stringList);
    if (stringList.isEmpty())
      return null;
    String shortest = stringList.get(0);
    int minLength = lengthNullSafe(shortest);
    for (final String actualString : stringList) {
      final int actualLength = lengthNullSafe(actualString);
      if (actualLength < minLength) {
        minLength = actualLength;
        shortest = actualString;
      } else if (shortest == null) {
        shortest = actualString;
      }
    }
    return shortest;
  }

  /**
   * Gibt einen String, der an den Stellen, an denen die anderen Strings
   * der Liste vom längsten String abweichen, das Wildcard enthält.
   * Kann zur Erstellung von Suchmasken benutzt werden.
   *
   * <pre>
   * StringUtils.replaceByWildcard(["aa", "aa"], '#')  = "aa"
   * StringUtils.replaceByWildcard(["aa", ""], '#')    = "##"
   * StringUtils.replaceByWildcard(["aa", "ab"], '#')    = "a#"
   * StringUtils.replaceByWildcard(["aa", "ba"], '#')    = "#a"
   * </pre>
   *
   * @param stringList   nicht null
   * @param wildcard  Zeichen
   * @return          Längsten String mit Wildcard an den Stellen, wo die
   *                  anderen Strings der Liste abweichen. Null, wenn der
   *                  längste String == null ist
   */
  public static String replaceByWildcard(final List<String> stringList, final char wildcard) {

    final String longest = getLongestString(stringList);
    if (longest == null)
      return null;

    final StringBuffer stringBuffer = new StringBuffer(longest);
    final String shortest = getShortestString(stringList);
    final int minLen = lengthNullSafe(shortest);

    for (int i = 0; i < minLen; i++) {
      final char actualChar = longest.charAt(i);
      for (final CharSequence string : stringList) {
        if (actualChar != string.charAt(i)) {
          stringBuffer.setCharAt(i, wildcard);
        }
      }
    }

    // der Rest kann nur aus Wildcards bestehen:
    for (int i = minLen; i < longest.length(); i++) {
      stringBuffer.setCharAt(i, wildcard);
    }
    return stringBuffer.toString();
  }

  /**
   * Gibt einen String, der an den Stellen, an denen die anderen Strings
   * der Liste vom längsten String abweichen, das Wildcard enthält.
   * Kann zur Erstellung von Suchmasken benutzt werden.
   *
   * <pre>
   * StringUtils.replaceByWildcard(["aa", "aa"], '#')  = "aa"
   * StringUtils.replaceByWildcard(["aa", ""], '#')    = "##"
   * StringUtils.replaceByWildcard(["aa", "ab"], '#')    = "a#"
   * </pre>
   *
   *
   * @param wildcard  Zeichen
   * @param stringList   nicht null
   * @return          Längsten String mit Wildcard an den Stellen, wo die
   *                  anderen Strings der Liste abweichen.  Null, wenn der
   *                  längste String == null ist
   */
  public static String replaceByWildcard(final char wildcard, final String... stringList) {
    return replaceByWildcard(Arrays.asList(stringList), wildcard);

  }

  /**
   * @param s		auch null
   * @return		letztes Zeichen oder 0, wenn s == null
   * 				oder s leer
   */
  public static char lastChar(final String s) {
    if (s == null || s.isEmpty())
      return 0;
    return charAt(s, s.length() - 1);
  }

  /**
   * @param s     auch null
   * @return      vorletztes Zeichen oder 0, wenn s == null
   *              oder s zu kurz
   */
  public static char penultimateChar(final String s) {
    if (s == null || s.isEmpty())
      return 0;
    return charAt(s, s.length() - 2);
  }

  /**
  * <p>Remove the last character from a String.</p>
  *
  * <p>If the String ends in {@code \r\n}, then remove both
  * of them.</p>
  *
  * <pre>
  * StringUtils.chop(null)          = null
  * StringUtils.chop("")            = ""
  * StringUtils.chop("abc \r")      = "abc "
  * StringUtils.chop("abc\n")       = "abc"
  * StringUtils.chop("abc\r\n")     = "abc"
  * StringUtils.chop("abc")         = "ab"
  * StringUtils.chop("abc\nabc")    = "abc\nab"
  * StringUtils.chop("a")           = ""
  * StringUtils.chop("\r")          = ""
  * StringUtils.chop("\n")          = ""
  * StringUtils.chop("\r\n")        = ""
  * </pre>
  *
  * @param str  the String to chop last character from, may be null
  * @return String without last character, {@code null} if null String input
  */
  public static String chop(final String str) {
    if (str == null) {
      return null;
    }
    final int strLen = str.length();
    if (strLen < 2) {
      return EMPTY;
    }
    final int lastIdx = strLen - 1;
    final String ret = str.substring(0, lastIdx);
    final char last = str.charAt(lastIdx);
    if (last == '\n' && ret.charAt(lastIdx - 1) == '\r') {
      return ret.substring(0, lastIdx - 1);
    }
    return ret;
  }

  // Substring
  //-----------------------------------------------------------------------
  /**
   * <p>Gets a substring from the specified String avoiding exceptions.</p>
   *
   * <p>A negative start position can be used to start {@code n}
   * characters from the end of the String.</p>
   *
   * <p>A {@code null} String will return {@code null}.
   * An empty ("") String will return "".</p>
   *
   * <pre>
   * StringUtils.substring(null, *)   = null
   * StringUtils.substring("", *)     = ""
   * StringUtils.substring("abc", 0)  = "abc"
   * StringUtils.substring("abc", 2)  = "c"
   * StringUtils.substring("abc", 4)  = ""
   * StringUtils.substring("abc", -2) = "bc"
   * StringUtils.substring("abc", -4) = "abc"
   * </pre>
   *
   * @param str  the String to get the substring from, may be null
   * @param start  the position to start from, negative means
   *  count back from the end of the String by this many characters
   * @return substring from start position, {@code null} if null String input
   */
  public static String substring(final String str, int start) {
    if (str == null) {
      return null;
    }

    // handle negatives, which means last n characters
    if (start < 0) {
      start = str.length() + start; // remember start is negative
    }

    if (start < 0) {
      start = 0;
    }
    if (start > str.length()) {
      return EMPTY;
    }

    return str.substring(start);
  }

  /**
   * <p>Gets a substring from the specified String avoiding exceptions.</p>
   *
   * <p>A negative start position can be used to start/end {@code n}
   * characters from the end of the String.</p>
   *
   * <p>The returned substring starts with the character in the {@code start}
   * position and ends before the {@code end} position.
   * All position counting is
   * zero-based -- i.e., to start at the beginning of the string use
   * {@code start = 0}. Negative start and end positions can be used to
   * specify offsets relative to the end of the String.</p>
   *
   * <p>If {@code start} is not strictly to the left of {@code end}, ""
   * is returned.</p>
   *
   * <pre>
   * StringUtils.substring(null, *, *)    = null
   * StringUtils.substring("", * ,  *)    = "";
   * StringUtils.substring("abc", 0, 2)   = "ab"
   * StringUtils.substring("abc", 2, 0)   = ""
   * StringUtils.substring("abc", 2, 4)   = "c"
   * StringUtils.substring("abc", 4, 6)   = ""
   * StringUtils.substring("abc", 2, 2)   = ""
   * StringUtils.substring("abc", -2, -1) = "b"
   * StringUtils.substring("abc", -4, 2)  = "ab"
   * </pre>
   *
   * @param str  the String to get the substring from, may be null
   * @param start  the position to start from, negative means
   *  count back from the end of the String by this many characters
   * @param end  the position to end at (exclusive), negative means
   *  count back from the end of the String by this many characters
   * @return substring from start position to end position,
   *  {@code null} if null String input
   */
  public static String substring(final String str, int start, int end) {
    if (str == null) {
      return null;
    }

    // handle negatives
    if (end < 0) {
      end = str.length() + end; // remember end is negative
    }
    if (start < 0) {
      start = str.length() + start; // remember start is negative
    }

    // check length next
    if (end > str.length()) {
      end = str.length();
    }

    // if start is greater than end, return ""
    if (start > end) {
      return EMPTY;
    }

    if (start < 0) {
      start = 0;
    }
    if (end < 0) {
      end = 0;
    }

    return str.substring(start, end);
  }

  /**
   * Zerlegt einen Unicode-String. Im CBS ist zum Beispiel ü
   * zerlegt in <code> ü = u + ̈  </code>. Diese Zerlegung
   * wird für alle Strings durchgeführt.
   *
   * @param s  ein beliebiger Unicode, auch null
   * @return   Unicode in der NFD-Zerlegung (kanonische Zerlegung) oder
   *           null, wenn <code>s == null</code>
   */
  public static String unicodeDecomposition(final CharSequence s) {
    if (s == null)
      return null;
    return Normalizer.normalize(s, Form.NFD);
  }

  /**
   * Normalisiert einen Unicode-String, dass er in einem Editor
   * gesucht werden kann. Im CBS ist zum Beispiel ü
   * zerlegt in <code> ü = u + ̈  </code>. Diese Zerlegung
   * wird wieder rückgängig gemacht.
   *
   * @param s  ein beliebiger Unicode, auch null
   * @return   Unicode in der NFC-Composition (kanonische Komposition) oder
   *           null, wenn <code>s == null</code>
   */
  public static String unicodeComposition(final CharSequence s) {
    if (s == null)
      return null;
    return Normalizer.normalize(s, Form.NFC);
  }

  /**
   *
   * @param s1    auch null
   * @param s2    auch null
   * @return      Die Unicode-Strings s1 und s2 sind gleich
   *              (nach NFD-Zerlegung)
   */
  public static boolean stringEquals(final CharSequence s1, final CharSequence s2) {
    if (s1 == null)
      return s2 == null;
    return unicodeDecomposition(s1).equals(unicodeDecomposition(s2));
  }

  /**
   *
   * <pre>
   * StringUtils.contains(null, *)     = false
   * StringUtils.contains(*, null)     = false
   * StringUtils.contains("", "")      = true
   * StringUtils.contains("abc", "")   = true
   * StringUtils.contains("abc", "a")  = true
   * StringUtils.contains("abc", "z")  = false
   * </pre>
   *
   * @param seq           auch null
   * @param searchSeq     auch null
   * @return              der Unicode-String seq enthält searchSeq
   *                      (nach NFD-Zerlegung)
   */
  public static
    boolean
    contains(final CharSequence seq, final CharSequence searchSeq, final boolean caseInsensitive) {
    if (seq == null || searchSeq == null) {
      return false;
    }
    final String unicodeDecompositionSeq = unicodeDecomposition(seq);
    final String unicodeDecompositionSearch = unicodeDecomposition(searchSeq);
    if (caseInsensitive) {
      return unicodeDecompositionSeq.toLowerCase()
        .contains(unicodeDecompositionSearch.toLowerCase());
    }
    return unicodeDecompositionSeq.contains(unicodeDecompositionSearch);
  }

  /**
   * @param s       auch null
   * @param pattern nicht null
   * @return        alle Teilstrings, auf die Pattern zutrifft
   */
  public static List<String> getMatches(final String s, final Pattern pattern) {
    if (s == null) {
      return Collections.emptyList();
    }
    final List<String> matches = new ArrayList<>();
    final Matcher matcher = pattern.matcher(s);
    while (matcher.find()) {
      matches.add(matcher.group());
    }
    return matches;
  }

  /**
   * @param s
   *            auch null
   * @return Liste der Zeichen (Unicode composed), eventuell leer
   */
  public static List<Character> string2charList(String s) {
    if (s == null)
      return Collections.emptyList();
    s = unicodeComposition(s);
    return s.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
  }

  /**
   *
   * @param s
   *            auch null, dann ""
   * @return Unicode-Composition von s und Ersetzen aller üblichen Akzente.
   */
  public static String removeAccents(String s) {
    if (s == null)
      return "";
    s = unicodeComposition(s);
    s = s.replace("é", "e");
    s = s.replace("è", "e");
    s = s.replace("ê", "e");
    s = s.replace("ë", "e");
    s = s.replace("á", "a");
    s = s.replace("à", "a");
    s = s.replace("â", "a");
    s = s.replace("ô", "o");
    s = s.replace("ý", "y");
    return s;
  }

  /**
   *
   * @param s         auch null
   * @param stopWords auch null
   * @return Entfernt alle Akzente, ersetzt Sonderzeichen durch " " und
   *         liefert eine Menge von Stichwörtern: Alphabetisch sortiert in
   *         Unicode-Composition
   */
  public static Set<String> stichwortMenge(final String s, final Collection<String> stopWords) {
    if (s == null)
      return Collections.emptySet();
    final List<String> arr = stichwortListe(s, stopWords);
    return new TreeSet<>(arr);
  }

  /**
  *
  * @param s         auch null
  * @param stopWords auch null
  * @return Entfernt alle Akzente, ersetzt Sonderzeichen durch " " und
  *         liefert eine Liste von Stichwörtern: In Unicode-Composition
  */
  public static List<String> stichwortListe(String s, final Collection<String> stopWords) {
    if (isNullOrEmpty(s))
      return Collections.emptyList();
    s = s.trim();
    s = s.toLowerCase();
    s = removeAccents(s);
    s = s.replaceAll("[!\"#$%&()*+,./:;<=>?@\\[\\]^_`{|}~†‐-]", " ");
    s = unicodeComposition(s);
    s = s.trim();
    final List<String> stichWW = new ArrayList<>();
    Collections.addAll(stichWW, s.split("\\p{Space}+"));
    if (stopWords != null)
      stichWW.removeAll(stopWords);
    return stichWW;
  }

}
