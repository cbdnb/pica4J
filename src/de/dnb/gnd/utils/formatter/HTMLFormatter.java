package de.dnb.gnd.utils.formatter;

import java.util.Collection;
import java.util.List;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.utils.HTMLEntities;
import de.dnb.basics.utils.HTMLUtils;
import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;

public class HTMLFormatter extends Pica3Formatter {

  private static final int HEADING_SIZE = 3;

  /**
   * Standard-Breite des Datensatzes.
   */
  public static final int DEF_WIDTH = 200;

  /**
   * Standard-Breite des Tags.
   */
  public static final int DEF_TAG_WIDTH = 5;

  /**
   * Breite des Datensatzes.
   */
  private int width = DEF_WIDTH;

  /**
   * Breite des Tags.
   */
  private int tagWidth = DEF_TAG_WIDTH;

  /**
   * Non-breaking Space.
   */
  public static final String NBSP = "&nbsp;";

  private HTMLHeadingBuilder htmlHeadingBuilder = null;

  /**
   * Ändert den HTMLTitleBuilder. Damit können die ausgegebene Titelzeilen
   * angepasst werden. Wenn der HTMLTitleBuilder null ist, werden nur Titel
   * und idn ausgegeben.
   *
   * @param htmlTitleBuilder  auch null
   */
  public void setHtmlTitleBuilder(final HTMLHeadingBuilder htmlTitleBuilder) {
    htmlHeadingBuilder = htmlTitleBuilder;
  }

  /**
   * Breite des Datensatzes.
   */
  public void setWidth(final int width) {
    this.width = width;
  }

  /**
   * Breite des Tags.
   */
  public void setTagWidth(final int tagWidth) {
    this.tagWidth = tagWidth;
  }

  public HTMLFormatter() {
    // geschütztes Leerzeichen
    tagPost = StringUtils.repeat(2, NBSP);
  }

  private boolean boldLine;

  private int fontsize = 10;

  private int border = 0;

  private int spacing = 0;

  private int padding = 2;

  public void setBorder(final int border) {
    this.border = border;
  }

  public void setSpacing(final int spacing) {
    this.spacing = spacing;
  }

  public void setPadding(final int padding) {
    this.padding = padding;
  }

  public void setFontsize(final int fontsize) {
    this.fontsize = fontsize;
  }

  public static void main(final String[] args) {
    final Record record = BibRecUtils.readFromClip();
    final HTMLFormatter formatter = new HTMLFormatter();
    formatter.setFontsize(16);
    formatter.setBorder(0);
    formatter.setSpacing(-3);
    final List<Line> lines = record.getLines();
    lines.add(10, null);
    lines.add(10, null);
    lines.add(10, null);
    final String txt = formatter.format(lines, record.tagDB);
    //        System.out.println(txt);
    OutputUtils.show(txt);

  }

  private String makeHeadings(final Record record) {
    if (htmlHeadingBuilder != null)
      return htmlHeadingBuilder.getHeading(record);
    final String idn = "idn: " + record.getId();
    final String title = RecordUtils.getTitle(record);
    return HTMLUtils.heading(title, HEADING_SIZE) + HTMLUtils.heading(idn, HEADING_SIZE);
  }

  @Override
  public String format(final Record record) {
    final String cell1 = HTMLUtils.tableCell(StringUtils.repeat(tagWidth, NBSP), fontsize);
    final String cell2 = HTMLUtils.tableCell(StringUtils.repeat(width - tagWidth, NBSP), fontsize);
    final String firstline = cell1 + cell2;

    final String html = makeHeadings(record) + HTMLUtils.table(HTMLUtils.tableLine(firstline) +
    // super.format(record) ruft über format(Line) Code in
    // dieser Klasse auf:
      super.format(record), border, spacing, padding);
    return html;
  }

  @Override
  public String format(final Line line) {
    if (line == null) {
      String s = HTMLUtils.tableCell("", fontsize);
      s += HTMLUtils.tableCell("", fontsize);
      return HTMLUtils.tableLine(s);
    }
    actualTag = line.getTag();
    boldLine = false;
    final String pica3 = actualTag.pica3;
    if (tagDB == RecordUtils.BIB_TAG_DB) {
      if (pica3.equals("4000"))
        boldLine = true;
    } else if (tagDB == RecordUtils.AUTH_TAG_DB) {
      if (pica3.startsWith("1") && pica3.length() == 3)
        boldLine = true;
      else {
        if (SubfieldUtils.containsIndicator(line, '4')) {
          final Subfield subfield4 = SubfieldUtils.getFirstSubfield(line, '4');
          final String subCont = subfield4.getContent();
          if (subCont.equals("aut1") || subCont.equals("kom1") || subCont.equals("kue1"))
            boldLine = true;
        }
      }
    }

    String s = HTMLUtils.tableCell(formatTag(), fontsize);
    final Collection<Subfield> subfields = line.getSubfields(format);
    s += HTMLUtils.tableCell(format(subfields), fontsize);
    return HTMLUtils.tableLine(s);
  }

  @Override
  public String formatTag() {
    if (boldLine)
      return HTMLUtils.bold(super.formatTag());
    else
      return super.formatTag();
  }

  @Override
  public String format(final Collection<Subfield> subfields) {
    String subsString = super.format(subfields);
    /*
     * Ziemlich primitiv, ist aber das Verhalten, das die Win-IBW
     * auch liefert: es werden einfach $ und das darauf folgende
     * Zeichen farblich hervorgehoben.
     */
    subsString = subsString.replaceAll("(\\$.)", "<font color=\"#CC3300\"><b>$1</b></font>");

    if (boldLine)
      return HTMLUtils.bold(subsString);
    else
      return subsString;
  }

  @Override
  protected String format(final Subfield subfield) {
    String s = super.format(subfield);
    s = HTMLEntities.allCharacters(s);

    final char c = actualIndicator.indicatorChar;
    if (c == '8') {
      s = HTMLUtils.italic(s);
    } else if (c == '9') {
      final String id = subfield.getContent();
      s = HTMLUtils.makeHtmlRef(id, "!" + id + "!");
    } else if (c == 'u') {
      final String dollarU = subfield.getContent();
      s = HTMLUtils.makeHtmlRef(dollarU, "$u" + dollarU);
    }

    if (actualTag.isIgnorable(actualIndicator))
      return "";

    return s;
  }

}
