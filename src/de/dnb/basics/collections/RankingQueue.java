/**
 *
 */
package de.dnb.basics.collections;

import java.util.Comparator;

import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * Eine Klasse, um Ranglisten zu erstellen. Werte und Punktzahlen können über
 * {@link #add(Object, Integer)} eingegeben werden. Intern werden sie als
 * {@link Pair} gespeichert, die Rangfolge wird über den 2. Bestandteil
 * des Paars hergestellt.
 * <br> Es können Ranglisten der besten ({@link RankingQueue#createHighestRanksQueue(int)})
 * und der schlechtesten ({@link RankingQueue#createLowestRanksQueue(int)}) erstellt werden.
 *
 * @author baumann
 *
 * @param <T> Typ
 */
public class RankingQueue<T> extends BoundedPriorityQueue<Pair<T, Integer>> {

  /**
   *
   */
  private static final long serialVersionUID = 583110714401743413L;

  /**
   *
   * @param <T> Typ
   * @param max Maximalzahl der Highscorer
   * @return    Eine Queue, das höchstens max Elemente, die mit der höchsten Punktzahl,
   *            enthält. Die Werte und Punktzahl können mittels {@link #add(Object, Integer)}
   *            eingegeben werden. Ausgabe über {@link #ordered()}.
   */
  public static <T> RankingQueue<T> createHighestRanksQueue(final int max) {
    return new RankingQueue<>(max, (p1, p2) -> p1.second - p2.second);
  }

  /**
  *
  * @param <T> Typ
  * @param max Maximalzahl der Low-Performer
  * @return    Eine Queue, das höchstens max Elemente, die mit der niedrigsten Punktzahl
  *            enthält. Die Werte und Punktzahl können mittel {@link #add(Object, Integer)}
  *            eingegeben werden. Ausgabe über {@link #ordered()}.
  */
  public static <T> RankingQueue<T> createLowestRanksQueue(final int max) {
    return new RankingQueue<>(max, (p1, p2) -> p2.second - p1.second);
  }

  /**
   * @param max
   * @param comparator
   */
  private RankingQueue(final int max, final Comparator<? super Pair<T, Integer>> comparator) {
    super(max, comparator);

  }

  /**
   *
   * @param value Wert
   * @param count Anzahl oder Punktzahl
   * @return
   */
  public boolean add(final T value, final Integer count) {
    return super.add(new Pair<T, Integer>(value, count));
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final RankingQueue<Character> records = createHighestRanksQueue(3);
    for (int i = 0; i < 10; i++) {
      records.add((char) (i + 100), i);
    }
    System.out.println(records);
  }

}
