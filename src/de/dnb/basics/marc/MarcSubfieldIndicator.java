package de.dnb.basics.marc;

import de.dnb.basics.filtering.RangeCheckUtils;

public class MarcSubfieldIndicator {

  public final char code;

  public final String descGerman;
  public final Repeatability repeatability;
  public final String descEnglish;

  public final boolean isDollar9;

  public static final MarcSubfieldIndicator NULL_INDICATOR =
    new MarcSubfieldIndicator((char) 0, "Nullindikator", Repeatability.UNKNOWN, null);

  /**
   * 
   * Konstruktor.
   * 
   * @param code		beliebig
   * @param descGerman	beliebig
   * @param repeatability	beliebig
   * @param descEnglish	beliebig
   */
  public MarcSubfieldIndicator(
    final char code,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);

    this.code = code;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    isDollar9 = false;
  }

  /**
   * 
   * Konstruktor.
   * 
   * @param code    beliebig
   * @param descGerman  beliebig
   * @param repeatability beliebig
   * @param descEnglish beliebig
   * @param isDollar9 für DACH-Sonderfelder
   */
  public MarcSubfieldIndicator(
    final char code,
    final String descGerman,
    final Repeatability repeatability,
    final String descEnglish,
    final boolean isDollar9) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("descGerman", descGerman);

    this.code = code;
    this.descGerman = descGerman;
    this.repeatability = repeatability;
    this.descEnglish = descEnglish;
    this.isDollar9 = isDollar9;
  }

  @Override
  public int hashCode() {
    return code;
  }

  @Override
  public String toString() {
    return "$" + code + " " + descGerman + " (" + repeatability + ")";
  }

  /**
   * 
   * @return Gibt den Code (also den char, der bei uns als Indikator
   *          bezeichnet wird). Außer, wenn ein $9 vorliegt. Dann wird der
   *          DACH-Indikator in der Forrm 9:d ausgegeben.
   */
  public String getDACHCode() {
    if (isDollar9)
      return "9:" + code;
    else
      return Character.toString(code);
  }

}
