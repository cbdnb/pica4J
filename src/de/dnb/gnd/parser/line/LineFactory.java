package de.dnb.gnd.parser.line;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.IDNUtils;

public abstract class LineFactory {

  protected LineFactory(final Tag aTag) {
    RangeCheckUtils.assertReferenceParamNotNull("aTag", aTag);
    tag = aTag;
    setSubfieldSeparator('$');
  }

  /**
   * !idn!.
   */
  public static final Pattern ID_REL_PAT = Pattern.compile("!(" + IDNUtils.PPN_STR_PLUS_1 + ")!");

  /**
   * Der Tag.
   */
  protected final Tag tag;

  /**
   * Trennzeichen für Unterfelder (in der Regel $ oder ƒ).
   */
  protected char subfieldSeparator;

  /**
   * Die Indikatoren, die zum Zerteilen von contentStr herangezogen
   * werden.
   */
  protected Map<Character, Indicator> splittingIndicators;

  /**
   * Setzt die Indikatoren, die zum Zerteilen von contentStr herangezogen
   * werden. Dazu muss(!) {@link #splittingIndicators} geändert werden.
   */
  protected abstract void setSplittingIndicators();

  /**
   * Für subfieldSeparator = $ wird das zu \$.
   */
  protected String escapedSeparator;
  /**
   * Provisorische Liste von Unterfeldern, die in line
   * eingefügt werden sollen. Diese Unterfelder sind
   * alle garantiert != null und die Indikatoren sind mit
   *  -   tag
   *  -   Wiederholbarkeit
   * vereinbar. Einzig die Korrektheit der Reihenfolge ist
   * nicht garantiert.
   */
  protected List<Subfield> subfieldList = new LinkedList<>();

  protected void setSubfieldSeparator(final char aSubfieldSeparator) {
    subfieldSeparator = aSubfieldSeparator;
    // Besonderheit bei Matcher.replaceAll(): \ und $
    // können zu Fehlern führen (s. Doc). Daher $ escapen!
    if (aSubfieldSeparator == '$')
      escapedSeparator = "\\" + aSubfieldSeparator;
    else
      escapedSeparator = "" + aSubfieldSeparator;
  }

  /**
   * Liefert die Unterfelder, die mit load() geladen wurden. Diese sind
   * korrekt,was ihre Zulässigkeit und Wiederholbarkeit angeht.
   * Nicht garantiert sind die Reihenfolge und die Einzigartigkeit (bei
   * Mengen).
   *
   * @return the subfieldList
   */
  public final List<Subfield> getSubfieldList() {
    return new ArrayList<>(subfieldList);
  }

  /**
   * Wird von aussen erkannt (durch die Form des Tags) und erleichtert das
   * Parsen der Zeile.
   */
  protected Format format;

  /**
   * @return the tag
   */
  public final Tag getTag() {
    return tag;
  }

  /**
   * Inhalt der Zeile.
   */
  protected String contentStr;

  /**
   * Überlese beim Parsen von Pica+-Daten die Felder, die in
   * MARC21 üblich sind, die aber nicht
   * zum Pica-Format gehören.
   */
  protected boolean ignoreMARC;

  /**
   * Spaltet {@link #contentStr} in Unterfelder auf. Hier wird die
   * eigentliche Arbeit getan.
   *
   * @throws IllFormattedLineException	Wenn Unterfelder nicht vorkommen
   * 										dürfen oder die Wiederholbarkeit
   * 										nicht gegeben ist.
   */
  protected abstract void splitInSubfields() throws IllFormattedLineException;

  /**
   * Angebot einer Default-Methode für {@link #splitInSubfields()}:
   * Zerlegt content in Unterfelder. content liegt in der Form
   * $i...$j... vor.
   *
   * @throws IllFormattedLineException Wenn der erste Indikator unbekannt ist
   *                                  oder das Unterfeld anderweitig
   *                                  nicht konstuiert werden kann.
   */
  protected final void defaultSplitInSubfields() throws IllFormattedLineException {
    final int length = contentStr.length();
    int allowedDollar = 0;
    Indicator ind = splittingIndicators.get(contentStr.charAt(1));
    if (ind == null)
      throw new IllFormattedLineException("Erster Indikator unbekannt");
    int nextDollar = contentStr.indexOf(subfieldSeparator, allowedDollar + 2);

    while (nextDollar != -1 && nextDollar < length - 1) {
      final char nextIndChar = contentStr.charAt(nextDollar + 1);
      final Indicator nextInd = splittingIndicators.get(nextIndChar);
      if (nextInd != null) {
        final String subContent = contentStr.substring(allowedDollar + 2, nextDollar);
        createSubfieldAndAddToList(ind, subContent);
        ind = nextInd;
        allowedDollar = nextDollar;
      }
      nextDollar = contentStr.indexOf(subfieldSeparator, nextDollar + 2);
    }
    final String subContent = contentStr.substring(allowedDollar + 2);
    createSubfieldAndAddToList(ind, subContent);
  }

  /**
   * Erzeugungsmethode der Fabrik. In der Form
   * <pre><code>
   * return new XXXLine(getTag(), subfieldList);
   * </code></pre> zu implementieren.
   *
   * @return	eine neue Zeile, nicht null.
   */
  public abstract Line createLine();

  /**
   * Über diese Methode kann die Fabrik mit einem neuen String
   * geladen werden.
   * @param aFormat						pica3 oder pica+, nicht null
   * @param aContentStr					Inhalt der Zeile (ohne Tag),
   * 										nicht null
   * @param ignoreMARC					Überlese beim Parsen von
   * 										Pica+-Daten die Felder, die in
   * 										MARC21 üblich sind, die aber nicht
   * 										zum Pica-Format gehören.
   * @throws IllFormattedLineException	Wenn die Zeile nicht korrekt
   * 										formatiert ist.
   */
  public final void load(final Format aFormat, final String aContentStr, final boolean ignoreMARC)
    throws IllFormattedLineException {
    RangeCheckUtils.assertReferenceParamNotNull("aFormat", aFormat);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("aContent", aContentStr);
    format = aFormat;
    contentStr = aContentStr;
    this.ignoreMARC = ignoreMARC;
    subfieldList.clear();
    try {
      processContent();
    } catch (final IllFormattedLineException e) {
      String message = e.getMessage();
      message =
        "Fehler beim Verabeiten von Zeile " + tag.pica3 + " " + aContentStr + ": " + message;
      throw new IllFormattedLineException(message);
    }
  }

  /**
   * Über diese Methode kann die Fabrik mit einer neuen Zeile
   * geladen werden. Es wird angenommen, dass die Zeile im
   * pica3-Format vorliegt.
   *
   * @param aContentStr					Inhalt der Zeile (ohne tag),
   * 										nicht null
   * @param ignoreMARK
   * @throws IllFormattedLineException	Wenn die Zeile nicht korrekt
   * 										formatiert ist.
   */
  public final void load(final String aContentStr) throws IllFormattedLineException {
    load(Format.PICA3, aContentStr, false);
  }

  /**
   * Über diese Methode kann die Fabrik mit einer neuen Subfield-Liste
   * geladen werden.
   *
   * @param subfields						nicht null
   * @throws IllFormattedLineException	Wenn die Subfelder nicht zum
   * 										Tag oder zur Wiederholbarkeit
   * 										passen.
   */
  public final void load(final Collection<Subfield> subfields) throws IllFormattedLineException {
    RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfields", subfields);
    subfieldList.clear();
    for (final Subfield subfield : subfields) {
      check(subfield.getIndicator());
      subfieldList.add(subfield);
    }
  }

  /**
   * Über diese Methode kann die Fabrik mit einer neuen Subfield-Liste
   * geladen werden.
   *
   * @param subfields						nicht null
   * @throws IllFormattedLineException	Wenn die Subfelder nicht zum
   * 										Tag oder zur Wiederholbarkeit
   * 										passen.
   */
  public final void load(final Subfield... subfields) throws IllFormattedLineException {
    RangeCheckUtils.assertArrayParamNotNullOrEmpty("subfields", subfields);
    load(Arrays.asList(subfields));
  }

  /**
   * Verarbeitet content = Inhalt der Zeile schrittweise.
   * Diese Methode ist eine Template-Methode, die einzelnen
   * Schritte können überschrieben werden.
   *
   * @throws IllFormattedLineException 	wenn die Zeile nicht korrekt
   * 										formatiert ist.
   */
  protected final void processContent() throws IllFormattedLineException {

    if (format == Format.PICA_PLUS) {
      if (ignoreMARC) {
        try {
          load(ParseUtils.simplePicaPlusSplitInSubfields(contentStr, tag));
        } catch (final Exception e) {
          throw new IllFormattedLineException("Fehler in Tag " + tag + " " + contentStr);
        }
        return;
      }
      // existiert, da contentStr garantiert mit Inhalt:
      final char first = contentStr.charAt(0);
      setSubfieldSeparator(first);
    } else {
      setSubfieldSeparator('$');
    }

    // bei pica3 muss noch weiter ersetzt werden:
    if (format == Format.PICA3) {
      preprocessContentString();
    }
    setSplittingIndicators();
    splitInSubfields();
  }

  /**
   * Vorverarbeitung der Zeile (Ersetzen von Deskriptionszeichen ...).
   * Für die Bearbeitung von Titeldaten ist diese Funktion leer.
   * Die Arbeit wird dann in {@link #splitInSubfields()}  geleistet.
   *
   * @throws IllFormattedLineException	Wenn schon hier erkannt werden
   * 										kann, dass die Zeile falsch
   * 										gebildet wurde.
   */
  protected void preprocessContentString() throws IllFormattedLineException {
  }

  /**
   * Überprüft, ob der Indikator zum tag passt und ob er
   * aufgrund der Wiederholbarkeit noch zu subfieldList hinzugefügt
   * werden darf.
   *
   * @param indicator
   * @throws IllFormattedLineException
   */
  private void check(final Indicator indicator) throws IllFormattedLineException {
    // ist der Indikator zum Tag gehörig?
    if (!tag.getAllIndicators().contains(indicator))
      throw new IllFormattedLineException("indikator unbekannt: " + indicator);
    if (indicator.repeatability != Repeatability.NON_REPEATABLE)
      return;
    // also nicht wiederholbar oder unbekannt:
    for (final Subfield subfield : subfieldList) {
      if (subfield.getIndicator() == indicator)
        throw new IllFormattedLineException("indikator doppelt - " + indicator);
    }
    // nicht wiederholbar, aber noch nicht vorhanden:
    return;
  }

  /**
   * Fügt eine weiteres Unterfeld der Liste subfieldList hinzu.
   *
   * Nur im Falle !...!$8... werden
   * leere Unterfelder ignoriert.
   *
   * @param indicator
   * @param subContent
   * @throws IllFormattedLineException  Wenn das Unterfeld nicht konstruiert
   *                                     werden kann.
   */
  protected final
    void
    createSubfieldAndAddToList(final Indicator indicator, final String subContent)
      throws IllFormattedLineException {
    RangeCheckUtils.assertReferenceParamNotNull("subContent", subContent);
    // keine Expansion:
    if (indicator.indicatorChar == '8' && subContent.length() == 0)
      return;

    check(indicator);
    subfieldList.add(new Subfield(indicator, subContent));
  }

}
