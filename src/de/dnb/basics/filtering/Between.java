package de.dnb.basics.filtering;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Filterkriterium geschlossenes/offenes/halboffenes Intervall. Verschiedene Intervalle sind über
 * ihre Untergrenze miteinander vergleichbar. Ist die Untergrenze gleich,
 * so kommt das kleinere Intervall vor dem größeren (Vergleich der
 * Obergrenze).
 *
 * @param <T>                   Typ, der {@link Comparable} implementiert
 * @author Christian Baumann
 *
 */
public class Between<T extends Object & Comparable<? super T>>
  implements Predicate<T>, Comparable<Between<T>> {

  public final T lowerBound;
  public final T higherBound;
  private Type type = Type.CLOSED;

  public enum Type {
      OPEN, CLOSED, HALF_OPEN
  }

  /**
  * @param lowerBound  nicht null
  * @param higherBound nicht null
  * @param type        offen ({@link Type#OPEN}), abgeschlossen ({@link Type#CLOSED}) oder
  *                    halb-offen ({@link Type#HALF_OPEN}) (untere Grenze im Intervall enthalten)
  *
  * @throws IllegalArgumentException wenn lowerBound größer als higherBound
  */
  public Between(final T lowerBound, final T higherBound, final Type type) {

    RangeCheckUtils.assertReferenceParamNotNull("lowerBound", lowerBound);
    RangeCheckUtils.assertReferenceParamNotNull("higherBound", higherBound);

    if (lowerBound.compareTo(higherBound) > 0)
      throw new IllegalArgumentException(
        "lowerBound " + lowerBound + " must be <= higherBound " + higherBound);

    this.lowerBound = lowerBound;
    this.higherBound = higherBound;
    this.type = type;
  }

  /**
   * Standardfall abgeschlossenes Intervall. Für die anderen Intervalltypen verwende
   * {@link Between#Between(Object, Object, Type)}
   *
   * @param lowerBound  nicht null
   * @param higherBound nicht null
   * @param type        offen, abgeschlossen, halb-offen (untere Grenze im Intervall enthalten)
   *
   * @throws IllegalArgumentException wenn lowerBound größer als higherBound
   */
  public Between(final T lowerBound, final T higherBound) {
    this(lowerBound, higherBound, Type.CLOSED);
  }

  /**
   *
   * @param <T>         extends Object & Comparable<? super T
   * @param lowerBound  nicht null, auch größer als higherBound
   * @param higherBound nicht null, auch kleiner als lowerBound
   * @return            immer ein korrekt angeordnetes Between
   */
  public static <T extends Object & Comparable<? super T>> Between<T> getOrdered(
    final T lowerBound,
    final T higherBound) {
    RangeCheckUtils.assertReferenceParamNotNull("lowerBound", lowerBound);
    RangeCheckUtils.assertReferenceParamNotNull("higherBound", higherBound);
    if (!(lowerBound.compareTo(higherBound) <= 0)) {
      return new Between<T>(higherBound, lowerBound);
    } else
      return new Between<T>(lowerBound, higherBound);

  }

  @Override
  public final boolean test(final T object) {
    if (object == null)
      return false;
    if (type == Type.CLOSED)
      return lowerBound.compareTo(object) <= 0 && higherBound.compareTo(object) >= 0;
    else if (type == Type.OPEN)
      return lowerBound.compareTo(object) < 0 && higherBound.compareTo(object) > 0;
    else
      return lowerBound.compareTo(object) <= 0 && higherBound.compareTo(object) > 0;
  }

  @Override
  public final int compareTo(final Between<T> other) {
    int ret = lowerBound.compareTo(other.lowerBound);
    if (ret == 0)
      ret = higherBound.compareTo(other.higherBound);
    return ret;
  }

  /**
   *
   * @param other auch null
   * @return  other enthalten?
   */
  public final boolean contains(final Between<T> other) {
    if (other == null)
      return false;
    return lowerBound.compareTo(other.lowerBound) <= 0
      && higherBound.compareTo(other.higherBound) >= 0;
  }

  /**
   * Funktioniert zur Zeit nur mit abgeschlossenen Intervallen!
   *
   * @param other auch null
   * @return    Durchschnitt nicht leer?
   */
  public final boolean intersects(final Between<T> other) {
    if (other == null)
      return false;
    return test(other.lowerBound) || other.test(lowerBound);
  }

  /**
   * Funktioniert zur Zeit nur mit abgeschlossenen Intervallen!
   *
   * @param other abgeschlossenes Intervall
   * @return      Schnitt als abgeschlossenes Intervall
   */
  public final Between<T> getIntersection(final Between<T> other) {
    final T otherLower = other.lowerBound;
    final T otherHigher = other.higherBound;
    if (test(otherLower)) {
      return new Between<T>(otherLower, Collections.min(Arrays.asList(higherBound, otherHigher)));
    }
    if (other.test(lowerBound)) {
      return new Between<T>(lowerBound, Collections.min(Arrays.asList(higherBound, otherHigher)));
    }
    return null;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((higherBound == null) ? 0 : higherBound.hashCode());
    result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "<" + lowerBound + " .. " + higherBound + ">";
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Between<?> other = (Between<?>) obj;
    if (higherBound == null) {
      if (other.higherBound != null)
        return false;
    } else if (!higherBound.equals(other.higherBound))
      return false;
    if (lowerBound == null) {
      if (other.lowerBound != null)
        return false;
    } else if (!lowerBound.equals(other.lowerBound))
      return false;
    return true;
  }

  public static void main(final String[] args) {

    final Between<Integer> between2 = new Between<Integer>(0, 5, Type.HALF_OPEN);
    System.out.println(between2.test(-1));
    System.out.println(between2.test(0));
    System.out.println(between2.test(1));
    System.out.println(between2.test(4));
    System.out.println(between2.test(5));
    System.out.println(between2.test(6));

  }

}
