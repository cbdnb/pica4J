/**
 *
 */
package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Ein {@link PriorityQueue} mit begrenzter Kapazität. Ist die Kapazität erreicht,
 * wird das kleinste Element entfernt.
 *
 * @author baumann
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> {

  private final int capacity;

  /**
   *
   */
  private static final long serialVersionUID = 2198971073757081029L;

  /**
   * @param capacity  the capacity for this priority queue
   * @param comparator  the comparator that will be used to order this priority queue.
   *                    If null, the natural ordering of the elements will be used.
   */
  public BoundedPriorityQueue(final int capacity, final Comparator<? super E> comparator) {
    super(capacity + 1, comparator);
    this.capacity = capacity;
  }

  /**
   * @param capacity  the capacity for this priority queue
   */
  public BoundedPriorityQueue(final int capacity) {
    super(capacity + 1);
    this.capacity = capacity;
  }

  @Override
  public boolean add(final E e) {
    return offer(e);
  }

  @Override
  public boolean offer(final E e) {
    if (size() < capacity)
      return super.offer(e);
    // also ist die Schlange voll:
    boolean le; // kleiner oder gleich dem Kopf
    final E head = this.peek();
    final Comparator<? super E> comparator = comparator();
    if (comparator == null) {
      // also natürliche Ordnung:
      final Comparable e1 = (Comparable) e;
      le = e1.compareTo(head) <= 0;
    } else {
      le = comparator.compare(e, head) <= 0;
    }
    if (le)
      return false;
    // also ist es größer als das kleinste Element der Schlange:
    poll();
    return super.offer(e);

  }

  /**
   *
   * @return  beginnend mit dem kleinsten Element
   */
  public List<E> reverseOrdering() {
    final ArrayList<E> list = new ArrayList<>(this);
    Collections.sort(list, comparator());
    return list;
  }

  /**
   *
   * @return beginnend mit dem größten Element
   */
  public List<E> ordered() {
    final List<E> list = reverseOrdering();
    Collections.reverse(list);
    return list;
  }

  @Override
  public String toString() {
    return ordered().toString();
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {

  }

}
