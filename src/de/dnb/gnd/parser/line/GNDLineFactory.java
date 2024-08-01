package de.dnb.gnd.parser.line;

import java.util.Set;
import java.util.regex.Matcher;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.TagDB;

public abstract class GNDLineFactory extends LineFactory {

  /**
   *
   * Erzeugt eine abstrakte GNDLineFactory.
   *
   * @param aTag	nicht null.
   */
  protected GNDLineFactory(final GNDTag aTag) {
    super(aTag);
  }

  /**
   * Ob die Zeile relationiert ist ($9).
   */
  protected boolean related;

  @Override
  protected void preprocessContentString() throws IllFormattedLineException {
    createDollar9();
    createFirstDollar();
    replaceDescriptionSigns();
  }

  /**
   * Aus !...! ein $9...$8 erzeugen (Pica3-Format).
   */
  protected final void createDollar9() {
    // unnötig, wenn kein $9 vorhanden (z.B. für Mailbox-Feld)
    final Indicator ind = tag.getIndicator('9');
    if (ind == null)
      return;
    // II.2.a
    final Matcher mID = ID_REL_PAT.matcher(contentStr);
    if (mID.find()) {
      String contentRepl = contentStr.substring(0, mID.start());
      contentRepl += subfieldSeparator + "9";
      contentRepl += mID.group(1);
      contentRepl += subfieldSeparator + "8";
      contentRepl += contentStr.substring(mID.end());
      contentStr = contentRepl;
      related = true;
    }
  }

  /**
   * Das defaultmäßige erste Unterfeld setzen.
   *
   * @throws IllFormattedLineException	Wenn kein solches Unterfeld
   * 										erlaubt ist.
   */
  protected final void createFirstDollar() throws IllFormattedLineException {
    // II.2.b
    if (contentStr.charAt(0) != subfieldSeparator) {
      final Indicator ohne = tag.getDefaultFirst();
      if (ohne == null)
        //@formatter:off
	    		throw new IllFormattedLineException(
	    			"kein -ohne- gefunden");
	    		//@formatter:on
      contentStr = "" + subfieldSeparator + tag.getDefaultFirst().indicatorChar + contentStr;
    }
  }

  /**
   *
   * Für den Fall, dass content im pica3-Format
   * vorliegt. content wird verändert. Content ist schon
   * vorverarbeitet:     *
   *  -   Am Anfang steht ein $ und ein (hoffentlich) gültiger Indikator.
   *  -   !..! ist durch $9..$8 ersetzt worden.
   *
   * Zu überschreiben, wenn ein andres Vorgehen notwendig ist als hier.
   *
   */
  protected void replaceDescriptionSigns() {
    // II.2.c - Standardverfahren für Deskriptionszeichen ersetzen
    final Set<Indicator> descSigns = ((GNDTag) tag).getDescriptionStrings();

    for (final Indicator indicator : descSigns) {
      contentStr =
        contentStr.replaceAll(indicator.prefix, escapedSeparator + indicator.indicatorChar);
    }

  }

  @Override
  protected void setSplittingIndicators() {
    /*
     * Wenn nicht relationiert, so müssen eventuell die
     * optionalen (so vorhanden) berücksichtigt werden:
     */
    splittingIndicators = tag.getIndicatorMap(!related);
  }

  @Override
  protected void splitInSubfields() throws IllFormattedLineException {
    defaultSplitInSubfields();
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final TagDB db = GNDTagDB.getDB();
    final Line line = LineParser.parse("150 Grün, Otto", db, false);
    System.out.println(line);

  }

}
