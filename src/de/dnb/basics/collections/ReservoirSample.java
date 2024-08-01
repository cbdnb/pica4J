package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * Nimmt aus einem Datenstrom vom Typ E ein Sample der Größe size.
 * Implementierung siehe:
 * <a href="https://xlinux.nist.gov/dads/HTML/reservoirSampling.html">
 *  https://xlinux.nist.gov/dads/HTML/reservoirSampling.html</a>
 *
 * @author baumann
 *
 * @param <E>   Typ der gesammelten Objekte
 */
public final class ReservoirSample<E> implements Collection<E> {

  private final ArrayList<E> reservoir;

  private final Random random;

  /**
   * Nummer des nächsten einzufügenden Items.
   */
  private int i = 0;

  private final int maxSize;

  /**
   *
   * @param size  größer 0
   */
  public ReservoirSample(final int size) {
    if (size <= 0)
      throw new IllegalArgumentException("Sample-Größe muss größer als 0 sein");
    this.maxSize = size;
    this.reservoir = new ArrayList<>(maxSize);
    random = new Random();
  }

  @Override
  public int size() {
    return reservoir.size();
  }

  @Override
  public boolean isEmpty() {
    return reservoir.isEmpty();
  }

  @Override
  public boolean contains(final Object o) {
    return reservoir.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return reservoir.iterator();
  }

  @Override
  public Object[] toArray() {
    return reservoir.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return reservoir.toArray(a);
  }

  @Override
  public boolean add(final E e) {
    i++;
    if (i <= maxSize)
      return reservoir.add(e);
    final int index = random.nextInt(i);
    if (index < maxSize) {
      reservoir.set(index, e);
      return true;
    } else
      return false;
  }

  @Override
  public boolean remove(final Object o) {
    return reservoir.remove(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return reservoir.containsAll(c);
  }

  @Override
  public boolean addAll(final Collection<? extends E> c) {
    boolean changed = false;
    for (final E e : c) {
      final boolean change = add(e);
      changed = change || changed;
    }
    return changed;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new IllegalAccessError("removeAll() darf nicht aufgerufen werden");
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new IllegalAccessError("retainAll() darf nicht aufgerufen werden");
  }

  @Override
  public void clear() {
    reservoir.clear();
    i = 0;
  }

  @Override
  public String toString() {
    return reservoir.toString();
  }

  public static void main(final String[] args) {

    final ReservoirSample<Integer> integers = new ReservoirSample<>(1100);
    final Frequency<Integer> frequency = new Frequency<Integer>();
    for (int i = 0; i < 11000000; i++) {
      integers.add(i % 11);
    }
    frequency.addCollection(integers);
    System.out.println(ListUtils.average(integers));
    System.out.println(ListUtils.variance(integers));
    System.out.println(frequency);
  }
}
