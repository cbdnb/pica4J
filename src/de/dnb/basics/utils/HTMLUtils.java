package de.dnb.basics.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;

public class HTMLUtils {

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final String table = tableFromCells(
      Arrays.asList(Arrays.asList("1", "2"), Arrays.asList("11", "12")), 0, 10, 0, 16);
    OutputUtils.show(table);

  }

  public static String makeHtml(final String s) {
    return "<html>" + s + "</html>";
  }

  /**
   *
   * @param reference	nicht null
   * @param text		nicht null
   * @return			Hyperlink mit http://. In der LinkAdresse sind die
   * 					Blanks durch %20 ersetzt.
   */
  public static String makeHtmlRef(final String reference, final String text) {
    final String ref = reference.replace(" ", "%20");
    String s;
    s = "<a href=http://" + ref + ">" + text + "</a>";
    return s;
  }

  /**
   *
   * @param reference nicht null
   * @param text      nicht null
   * @return          Hyperlink ohne beginnendes http://.
   *                  In der LinkAdresse sind die Blanks durch %20 ersetzt.
   */
  public static String makeHtmlRefWithoutHTTP(final String reference, final String text) {
    final String ref = reference.replace(" ", "%20");
    String s;
    s = "<a href=" + ref + ">" + text + "</a>";
    return s;
  }

  /**
   * @param s
   * @return
   */
  public static String bold(final String s) {
    return "<b>" + s + "</b>";
  }

  /**
   * @param s
   * @return
   */
  public static String italic(String s) {
    s = "<i>" + s + "</i>";
    return s;
  }

  /**
   * Erzeugt Tabelle.
   *
   * @param cells     Zellen, in {@link #tableLine(String)} erzeugt und
   *                  aneinandergehängt
   * @param border    Dicke des Randes
   * @param spacing   Abstand der Zellen zueinander (horizontal und
   *                  vertikal)
   * @param padding   Abstand des Textes vom Rand
   * @return          Tabelle
   */
  public static
    String
    table(final String cells, final int border, final int spacing, final int padding) {
    return "<table border=\"" + border + "\" cellspacing=\"" + spacing + "\" cellpadding=\""
      + padding + "\" valign=\"top\">" + cells + "</table>";
  }

  /**
   * Erzeugt Tabelle.
   *
   * @param lines     Zeilen, in {@link #tableLine(String)} erzeugt und
   *                  aneinandergehängt
   * @param border    Dicke des Randes
   * @param spacing   Abstand der Zellen zueinander (horizontal und
   *                  vertikal)
   * @param padding   Abstand des Textes vom Rand
   * @return          Tabelle
   */
  public static
    String
    table(final Collection<String> lines, final int border, final int spacing, final int padding) {
    final String table = StringUtils.concatenate("", lines);
    return "<table border=\"" + border + "\" cellspacing=\"" + spacing + "\" cellpadding=\""
      + padding + "\" valign=\"top\">" + table + "</table>";
  }

  /**
   * Erzeugt Tabelle.
   *
   * @param cells     Zeilen aus Zellen (einfache Strings)
   * @param border    Dicke des Randes
   * @param spacing   Abstand der Zellen zueinander (horizontal und
   *                  vertikal)
   * @param padding   Abstand des Textes vom Rand
   * @param fontsize  Zeichengröße
   * @return          Tabelle
   */
  public static String tableFromCells(
    final Collection<Collection<String>> cells,
    final int border,
    final int spacing,
    final int padding,
    final int fontsize) {

    final Collection<String> lines =
      cells.stream().map(line -> tableLine(line, fontsize)).collect(Collectors.toList());
    return table(lines, border, spacing, padding);
  }

  /**
   * Erzeugt Tabelle.
   *
   * @param cells     Zellen, in {@link #tableLine(String)} erzeugt und
   *                  aneinandergehängt
   * @param colgroup  Spaltenbreiten, in {@link #colgroup(int...)}
   *                  erzeugt
   * @param border    Dicke des Randes
   * @param spacing   Abstand der Zellen zueinander (horizontal und
   *                  vertikal)
   * @param padding   Abstand des Textes vom Rand
   * @return          Tabelle
   */
  public static String table(
    final String cells,
    final String colgroup,
    final int border,
    final int spacing,
    final int padding) {
    return "<table border=\"" + border + "\" cellspacing=\"" + spacing + "\" cellpadding=\""
      + padding + "\" valign=\"top\">" + colgroup + cells + "</table>";
  }

  /**
   * Liefert Vorspann, der den Aufbau einer Tabelle erleichtert.
   * @param widths    Weiten den Tabellenspalten
   * @return          Vorspann, kann in
   *                  {@link #table(String, String, int, int, int)}
   *                  verwendet werden
   */
  public static String colgroup(final int... widths) {
    String s = "<colgroup>";
    for (final int i : widths) {
      s += "<col width=\"" + i + "\">";
    }
    s += "</colgroup>";
    return s;
  }

  /**
   * Erzeugt eine Tabellenzeile.
   * @param s Alle mittels {@link #tableCell(String, int)} erzeugten und
   *          zu einem String verbundenen Zellen
   * @return  neue Zeile
   */
  public static String tableLine(final String s) {
    return "<tr>" + s + "</tr>";
  }

  /**
   * Erzeugt eine Tabellenzeile.
   * @param cells zellen
   * @param fontsize Zeichengröße
   * @return  neue Zeile
   */
  public static String tableLine(final Collection<String> cells, final int fontsize) {
    final String line =
      cells.stream().map(cell -> tableCell(cell, fontsize)).collect(Collectors.joining());
    return "<tr>" + line + "</tr>";
  }

  /**
   * Erzeugt eine Zelle einer Tabelle.
   *
   * @param s         Zellinhalt
   * @param fontsize  größe
   * @return          Zelle
   */
  public static String tableCell(final String s, final int fontsize) {
    return "<td style=\"font-family:Arial;font-size:" + fontsize + "pt\">" + s + "</td>";
  }

  /**
   * Erzeugt Überschrift.
   *
   * @param s beliebig
   * @param i größer 0
   * @return  Leeren String, wenn leeres s, sonst Überschrift
   */
  public static String heading(final String s, final int i) {
    if (s == null || s.isEmpty())
      return "";
    return "<h" + i + ">" + s + "</h" + i + ">";
  }

  /**
   * Erzeugt Überschrift.
   *
   * @param s beliebig
   * @param i größer 0
   * @return  Leeren String, wenn leeres s, sonst Überschrift
   */
  public static String rightHeading(final String s, final int i) {
    if (s == null || s.isEmpty())
      return "";
    return "<h" + i + " align=\"right\">" + s + "</h" + i + ">";
  }

  /**
   * Fasst zu einer logischen einheit zusammen.
   * @param s Block
   * @return  Block
   */
  public static String span(final String s) {
    //        return s;
    return "<span style=\"font-family:Arial;font-size:16pt\">" + s + "</span>";
  }

  /**
   * Textabsatz Beginn.
   */
  public static final String HTML_PARAGRAPH_OPEN =
    "\n<p style=\"text-align:left; " + "margin-left:50px; margin-right:50px\">";

  /**
   * Textabsatz Ende.
   */
  public static final String HTML_PARAGRAPH_CLOSE = "</p>";

}
