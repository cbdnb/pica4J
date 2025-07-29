package de.dnb.gnd.parser;

import java.io.Serializable;

import de.dnb.basics.filtering.RangeCheckUtils;

public class Indicator implements Serializable{

  /**
	 * 
	 */
	private static final long serialVersionUID = 4670749175214061131L;

  /**
   * zusammen mit postfix null oder nicht null.
   */
  public final String prefix;

  /**
   * zusammen mit prefix null oder nicht null.
   */
  public final String postfix;

  /**
   * null oder von "" verschieden. Wenn das zugehörige Unterfeld
   * wiederholt wird, so erscheint das alternative Präfix.
   * (z.B $S in 4000: zuerst "|" später " |".
   */
  public final String prefixAlt;

  /**
   * Wird das zum Indikator gehörige Unterfeld mit dem alternativen
   * Präfix angeschlossen?
   * <br>
   * Tritt nur bei Titeldaten auf.<br>
   * Beispiel: <br>
   * Feld 5450 (Notation eines Klassifikationsystems (aus Fremddaten)<br>
   * Unterfeld $a
   * 			Notation der in $b beschriebenen Klassifikation,
   * 			weitere Notationen werden mit "?$?" angeschlossen.
   *
   */
  public final boolean isAttaching;

  public final char indicatorChar;
  public final char marcIndicator;
  public final String descGerman;
  public final Repeatability repeatability;
  public final String descEnglish;
  public final char alephChar;

  public static final Indicator NULL_INDICATOR =
    new Indicator((char) 0, "Nullindikator", Repeatability.UNKNOWN, null);

  /**
   * Konstruktor mit Prä- und Postfix.
   *
   * @param prefix		nicht null
   * @param postfix		nicht null
   * @param indicator		beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   */
  public Indicator(
    final String prefix,
    final String postfix,
    final char indicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertReferenceParamNotNull("prefix", prefix);
    RangeCheckUtils.assertReferenceParamNotNull("postfix", postfix);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    this.prefix = prefix;
    prefixAlt = null;
    isAttaching = false;
    this.postfix = postfix;
    indicatorChar = indicator;
    marcIndicator = indicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    alephChar = indicator;
  }

  /**
   * Konstruktor mit Prä- und Postfix.
   *
   * @param prefix		nicht null
   * @param postfix		nicht null
   * @param indicator		beliebig
   * @param marcIndicator beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   */
  public Indicator(
    final String prefix,
    final String postfix,
    final char indicator,
    final char marcIndicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertReferenceParamNotNull("prefix", prefix);
    RangeCheckUtils.assertReferenceParamNotNull("postfix", postfix);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    this.prefix = prefix;
    prefixAlt = null;
    isAttaching = false;
    this.postfix = postfix;
    indicatorChar = indicator;
    this.marcIndicator = marcIndicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    alephChar = indicator;
  }

  /**
   * Konstruktor mit Prä- und Postfix.
   *
   * @param prefix		nicht null
   * @param postfix		nicht null
   * @param indicator		beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   * @param alephChar		beliebig
   */
  public Indicator(
    final String prefix,
    final String postfix,
    final char indicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish,
    final char alephChar) {
    RangeCheckUtils.assertReferenceParamNotNull("prefix", prefix);
    RangeCheckUtils.assertReferenceParamNotNull("postfix", postfix);
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    this.prefix = prefix;
    prefixAlt = null;
    this.postfix = postfix;
    isAttaching = false;
    indicatorChar = indicator;
    marcIndicator = indicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    this.alephChar = alephChar;
  }

  /**
   *
   * Konstruktor. Prä- und Postfix werden automatisch auf null gesetzt.
   *
   * @param indicator		beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   */
  public Indicator(
    final char indicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    prefix = null;
    prefixAlt = null;
    isAttaching = false;
    postfix = null;
    indicatorChar = indicator;
    marcIndicator = indicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    alephChar = indicator;
  }

  /**
   *
   * Konstruktor. Prä- und Postfix werden automatisch auf null gesetzt.
   *
   * @param indicator		beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   */
  public Indicator(
    final char indicator,
    final char marcIndicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    prefix = null;
    prefixAlt = null;
    isAttaching = false;
    postfix = null;
    indicatorChar = indicator;
    this.marcIndicator = marcIndicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    alephChar = indicator;
  }

  /**
   *
   * Konstruktor. Prä- und Postfix werden automatisch auf null gesetzt.
   *
   * @param indicator		beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   * @param alephChar		beliebig
   */
  public Indicator(
    final char indicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish,
    final char alephChar) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    prefix = null;
    prefixAlt = null;
    isAttaching = false;
    postfix = null;
    indicatorChar = indicator;
    marcIndicator = indicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    this.alephChar = alephChar;
  }

  /**
   *
   * Konstruktor. Prä- und Postfix werden automatisch auf null gesetzt.
   *
   * @param indicator		beliebig
   * @param marcIndicator beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   * @param alephChar		beliebig
   */
  public Indicator(
    final char indicator,
    final char marcIndicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish,
    final char alephChar) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);
    prefix = null;
    prefixAlt = null;
    isAttaching = false;
    postfix = null;
    indicatorChar = indicator;
    this.marcIndicator = marcIndicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    this.alephChar = alephChar;
  }

  /**
   *
   * @param prefix
   * @param postfix
   * @param prefixAlt
   * @param isAttaching    weitere werden angeschlossen
   * @param indicatorChar
   * @param descGerman
   * @param repeatability
   * @param descEnglish
   */
  public Indicator(
    final String prefix,
    final String postfix,
    final String prefixAlt,
    final boolean isAttaching,
    final char indicatorChar,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("prefixAlt", prefixAlt);
    this.prefix = prefix;
    this.postfix = postfix;
    this.prefixAlt = prefixAlt;
    this.isAttaching = isAttaching;
    this.indicatorChar = indicatorChar;
    marcIndicator = indicatorChar;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    alephChar = indicatorChar;
  }

  /**
   *
   * @param prefix
   * @param postfix
   * @param prefixAlt
   * @param isAttaching   weitere werden angeschlossen
   * @param indicatorChar
   * @param marcIndicator
   * @param descGerman
   * @param repeatability
   * @param descEnglish
   */
  public Indicator(
    final String prefix,
    final String postfix,
    final String prefixAlt,
    final boolean isAttaching,
    final char indicatorChar,
    final char marcIndicator,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("prefixAlt", prefixAlt);
    this.prefix = prefix;
    this.postfix = postfix;
    this.prefixAlt = prefixAlt;
    this.isAttaching = isAttaching;
    this.indicatorChar = indicatorChar;
    this.marcIndicator = marcIndicator;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    alephChar = indicatorChar;
  }

  @Override
  public int hashCode() {
    return indicatorChar;
  }

  @Override
  public String toString() {
    String s = "";
    if (prefix != null) {
      s += "'" + prefix + "..." + postfix + "' ";
    }
    if (prefixAlt != null) {
      s += "bzw.['" + prefixAlt + "..." + postfix + "'] ";
    }
    return s + "$" + indicatorChar + " " + descGerman + " (" + repeatability + ")";
  }

}
