/**
 *
 */
package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.dnb.basics.cloneable.CopyObjectUtils;

/**
 * A collection that supports order-independent equality, like Set, but may have duplicate
 * elements. A multiset is also sometimes called a bag.
 * <br><br>
 * Elements of a multiset that are equal to one another are referred to as occurrences
 * of the same single element. The total number of occurrences of an element in a multiset
 * is called the count of that element. Since the count of an element is represented as an long,
 * a multiset  may never contain more than Long.MAX_VALUE occurrences of any one element.
 * <br><br>
 * Multiset refines the specifications of several methods from Collection.
 * It also defines an additional query operation, count(java.lang.Object),
 * which returns the count of an element.
 *
 * @author baumann
 *
 */
public class Multiset<V> implements Collection<V> {

  private final Frequency<V> frequency = new Frequency<>();

  @Override
  public int size() {
    return (int) frequency.getSum();
    //    long i = 0;
    //    for (final V v : frequency) {
    //      i += frequency.get(v);
    //    }
    //    return (int) i;
  }

  @Override
  public boolean isEmpty() {
    return frequency.isEmpty();
  }

  @Override
  public boolean contains(final Object o) {
    return frequency.contains(o);
  }

  @Override
  public Iterator<V> iterator() {
    return toList().iterator();
  }

  public List<V> toList() {
    final ArrayList<V> list = new ArrayList<>(frequency.size());
    elementSet().forEach(key ->
    {
      final long count = frequency.get(key);
      for (int i = 0; i < count; i++) {
        list.add(key);
      }
    });
    return list;

  }

  @Override
  public Object[] toArray() {
    return toList().toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return toList().toArray(a);
  }

  @Override
  public boolean add(final V e) {
    frequency.add(e);
    return true; // lässt sich immer hinzufügen
  }

  /**
   * Ändert auf einfache Weise die Anzahl von element.
   *
   * @param element nicht null
   * @param count positiv
   * @return  alte Anzahl von element
   */
  public long setCount(final V element, final long count) {
    if (count < 0)
      throw new IllegalArgumentException("Anzahl muss größer 0 sein!");
    if (count > 0)
      return frequency.put(element, count);
    else
      return frequency.remove(element);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean remove(final Object o) {
    long count = frequency.get(o);
    if (count == 0) {
      return false;
    }
    // Cast ist ungefährlich, da o vorhanden und damit vom Typ V ist:
    frequency.increment((V) o, -1);
    count = frequency.get(o);
    if (count == 0)
      frequency.remove(o);
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean containsAll(final Collection<?> c) {
    @SuppressWarnings("rawtypes")
    final Frequency intermed = new Frequency();
    intermed.addCollection(c);
    for (final Object object : intermed) {
      final long countColl = intermed.get(object);
      final long count = frequency.get(object);
      if (countColl > count)
        return false;
    }
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends V> c) {
    c.forEach(this::add);
    return true;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    boolean removed = false;
    for (final Object object : c) {
      removed |= remove(object);
    }
    return removed;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean retainAll(final Collection<?> c) {
    final Frequency<V> intermed = CopyObjectUtils.copyObject(frequency);
    final List<?> intermList = new ArrayList<>(c);
    intermList.retainAll(elementSet());
    clear();
    // Das geht, da nur noch Elemente vom Typ V vorhanden sind:
    intermList.forEach(el -> add((V) el));
    return intermed.equals(frequency);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + frequency.hashCode();
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
    final Multiset other = (Multiset) obj;
    return frequency.equals(other.frequency);
  }

  /**
   *
   * @return  die voneinander verschiedenen Elemente
   */
  public Set<V> elementSet() {
    return frequency.keySet();
  }

  /**
   *
   */
  public Multiset() {
    super();
  }

  /**
  *
  */
  public Multiset(final Collection<V> c) {
    this();
    addAll(c);
  }

  /**
  *
  */
  @SafeVarargs
  public Multiset(final V... c) {
    this(Arrays.asList(c));
  }

  @Override
  public void clear() {
    frequency.clear();
  }

  @Override
  public String toString() {
    return toList().toString();
  }

  /**
   *
   * @return
   */
  public String toStringFreq() {
    return frequency.toString();
  }

  public static void main(final String... args) {
    final Multiset<Integer> ints1 = new Multiset<>(1, 2, 3, 2, 1);

    System.out.println(ints1.toStringFreq());
    System.out.println();
    System.out.println(ints1.setCount(1, 3));
    System.out.println();
    System.out.println(ints1.toStringFreq());
    System.out.println("size: " + ints1.size());
    System.out.println(ints1.setCount(1, -1));
    System.out.println();
    System.out.println(ints1.toStringFreq());

  }
}
