/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Objects;

import de.dnb.basics.applicationComponents.TernaryLogic;
import de.dnb.basics.utils.NumberUtils;
import de.dnb.basics.utils.TimeUtils;

/**
 * Wöchentliches Verzeichnis. WV-Datensatz kann aus Feld 2105 gewonnen
 * werden, enthält Jahr, WV-Nummer und die Reihe.
 *
 * @author baumann
 *
 */
public class WV {

  /**
   * WV-Jahr.
   */
  private final int year;

  /**
   * Heftnummer.
   */
  private final int number;

  /**
   * Reihe.
   */
  private final char series;

  /**
   * @param year      >0, wird in vierstelliges Jahr umgewandelt
   * @param number    Heftnummer
   * @param series    Reihe (A, B, H ...)
   */
  public WV(final int year, final int number, final char series) {
    super();
    if (year >= 100)
      this.year = year;
    else
      this.year = TimeUtils.make4DigitYear(year);
    this.number = number;
    this.series = series;
  }

  /**
   * Erzeugt aus dem Inhalt von Feld 2105 ein WV-Objekt.
   * Das Feld sieht in der Regel so aus:<br><br>
   *
   * 2105 98,A39,0360
   *
   * <br><br>
   * Daraus wird ein WV <1998, 39, A> erzeugt.
   *
   * @param wvS    nicht null
   *
   */
  public WV(String wvS) {
    Objects.requireNonNull(wvS);
    wvS = wvS.trim();
    final String[] wvFractions = wvS.split("\\,");
    if (wvFractions.length < 2)
      throw new IllegalArgumentException("Fehlende Kommata: " + wvS);
    final String yearS = wvFractions[0];
    if (!NumberUtils.isPositiveArabicInt(yearS))
      throw new IllegalArgumentException("Keine gültiges Jahr: " + yearS);
    year = TimeUtils.make4DigitYear(Integer.parseInt(yearS));

    final String reiheUndHeft = wvFractions[1];
    if (reiheUndHeft.length() < 3)
      throw new IllegalArgumentException("Reihe + Heft ungültig: " + reiheUndHeft);
    series = reiheUndHeft.charAt(0);

    final String heftNR = reiheUndHeft.substring(1, 3);
    if (!NumberUtils.isPositiveArabicInt(heftNR))
      throw new IllegalArgumentException("Keine Heftnummer: " + heftNR);
    number = Integer.parseInt(heftNR);

  }

  /**
   * @return Jahr
   */
  public int getYear() {
    return year;
  }

  /**
   * @return Heftnummer
   */
  public int getNumber() {
    return number;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "WV [Jahr=" + year + ", Heftnummer=" + number + ", Reihe=" + series + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + number;
    result = prime * result + series;
    result = prime * result + year;
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
    final WV other = (WV) obj;
    if (number != other.number)
      return false;
    if (series != other.series)
      return false;
    if (year != other.year)
      return false;
    return true;
  }

  /**
   * @return the Reihe
   */
  public char getSeries() {
    return series;
  }

  public boolean isPHeft() {
    return 'P' == series;
  }

  /**
   *
   * @param s beliebig
   * @return  WV oder null (im Fehlerfall)
   */
  public static WV create(final String s) {
    try {
      return new WV(s);
    } catch (final Exception e) {
      return null;
    }
  }

  /**
   * Ist das andere WV gleichzeitig oder später?
   * @param other nicht null
   * @return      MAYBE, wenn andere Reihe, da dann nicht
   *              klar ist, ob zu einem späteren Zeitpunkt
   *              erschienen. Sonst TRUE, wenn späteres
   *              Jahr oder späteres Heft (bei gleichem
   *              Jahr)
   */
  public TernaryLogic isPreviousTo(final WV other) {
    Objects.requireNonNull(other);
    if (series != other.series)
      return TernaryLogic.MAYBE;
    if (year < other.year)
      return TernaryLogic.TRUE;
    else if (year > other.year)
      return TernaryLogic.FALSE;
    else
      return TernaryLogic.create(number <= other.number);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final WV wv1 = create("10,P01");
    System.out.println(wv1.isPHeft());

  }

}
