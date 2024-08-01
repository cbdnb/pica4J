package de.dnb.basics.applicationComponents.tuples;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.dnb.basics.filtering.RangeCheckUtils;

public class Pair<A, B> implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 5933551817604546415L;

  /**
   * Standard-Konstruktor.
   *
   * @param first		auch null
   * @param second	auch null
   */
  public Pair(final A first, final B second) {
    this.first = first;
    this.second = second;
  }

  public static <X, Y> Pair<X, Y> getNullPair() {
    return new Pair<X, Y>(null, null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    return result;
  }

  /**
   * @return the first
   */
  public A getFirst() {
    return first;
  }

  /**
   * @return the second
   */
  public B getSecond() {
    return second;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Pair<?, ?> other = (Pair<?, ?>) obj;
    if (first == null) {
      if (other.first != null)
        return false;
    } else if (!first.equals(other.first))
      return false;
    if (second == null) {
      if (other.second != null)
        return false;
    } else if (!second.equals(other.second))
      return false;
    return true;
  }

  /**
   * Umwandlungs-Konstruktor.
   *
   * @param entry	nicht null
   */
  public Pair(final Map.Entry<A, B> entry) {
    RangeCheckUtils.assertReferenceParamNotNull("entry", entry);
    first = entry.getKey();
    second = entry.getValue();
  }

  /**
   * Für häufige Fälle, Umwandlung des Entrysets einer Map in eine Liste
   * von Paaren.
   *
   * @param collection	nicht null.
   * @return				evenutell leer
   */
  public static <
      A, B>
    Collection<Pair<A, B>>
    getPairs(final Collection<Map.Entry<A, B>> collection) {
    RangeCheckUtils.assertReferenceParamNotNull("", collection);
    final List<Pair<A, B>> pairs = new LinkedList<Pair<A, B>>();
    for (final Map.Entry<A, B> entry : collection) {
      pairs.add(new Pair<A, B>(entry));
    }
    return pairs;
  }

  public final A first;

  public final B second;

  @Override
  public final String toString() {
    return "<" + first + ", " + second + ">";
  }
}
