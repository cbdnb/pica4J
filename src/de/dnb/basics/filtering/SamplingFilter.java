package de.dnb.basics.filtering;

import java.util.function.Predicate;

/**
 * Filter für Stichproben. Die Probe wird um den Faktor n reduziert.
 * Es wird nur jeder n-te Wert akzeptiert.
 * @author Christian_2
 *
 */
public class SamplingFilter implements Predicate<String> {

  private int i = 0;

  /**
   *
   * @param n	Der Kompressionsfaktor, n > 0
   */
  public SamplingFilter(final int n) {
    if (n <= 0)
      throw new IllegalArgumentException("n > 0 verletzt.");
    this.n = n;
  }

  private final int n;

  @Override
  public final boolean test(final String element) {
    i++;
    if (i == n) {
      i = 0;
      return true;
    }
    return false;
  }

}
