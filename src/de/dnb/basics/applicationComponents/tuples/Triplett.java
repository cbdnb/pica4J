package de.dnb.basics.applicationComponents.tuples;

import java.io.Serializable;

public class Triplett<S, U, V> implements Serializable {
  /**
   * @return the first
   */
  public S getFirst() {
    return first;
  }

  /**
   * @return the second
   */
  public U getSecond() {
    return second;
  }

  /**
   * @return the third
   */
  public V getThird() {
    return third;
  }

  /**
   *
   */
  private static final long serialVersionUID = 7274487692698230470L;

  public final S first;

  public final U second;

  public final V third;

  public Triplett(final S first, final U second, final V third) {
    super();
    this.first = first;
    this.second = second;
    this.third = third;
  }

  @Override
  public final String toString() {
    return "<" + first + ", " + second + ", " + third + ">";
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public final static Triplett NULL_TRIPLETT = new Triplett(null, null, null);

  @SuppressWarnings("unchecked")
  public static <X, Y, Z> Triplett<X, Y, Z> getNullTriplett() {
    return NULL_TRIPLETT;
  }

}
