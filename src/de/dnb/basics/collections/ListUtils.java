package de.dnb.basics.collections;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

public class ListUtils {

  /**
   * Nullsichere Methode zur Herausgabe eines Listenelements.
   * @param list  auch null
   * @param i     beliebig
   * @return      null, wenn list == null || i < 0 || i >= list.size()
   * @param <T>     beliebig
   */
  public static <T> Optional<T> getElement(final List<? extends T> list, final int i) {
    if (list == null)
      return Optional.empty();
    final int size = list.size();
    if (i < 0)
      return Optional.empty();
    if (i >= size)
      return Optional.empty();
    return Optional.ofNullable(list.get(i));
  }

  /**
   *
   * @param <D> Zieltyp
   * @param <S> Quelltyp
   * @param collection  nicht null
   * @return  Die mehrfach in der Liste enthaltenen Elemente
   */
  public static <D, S extends D> List<D> getMultipleElements(final Collection<S> collection) {
    Objects.requireNonNull(collection);
    final Frequency<S> frequency = toFrequency(collection);
    final List<D> retList = new ArrayList<>();
    frequency.forEach(el ->
    {
      final long count = frequency.get(el);
      if (count > 1)
        retList.add(el);
    });
    return retList;
  }

  /**
   *
   * @param <S>     Typ der Liste
   * @param <T>     Typ des gesuchten Elements
   * @param list    auch null
   * @param element auch null
   * @return        Alle Indizes, deren Listeneinträge mit element übereinstimmen.
   */
  public static <
      S, T extends S>
    List<Integer>
    findMatchingIndices(final List<S> list, final T element) {
    if (list == null)
      return Collections.emptyList();
    final List<Integer> matchingIndices = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      final S listEl = list.get(i);
      if (element == null) {
        if (listEl == null)
          matchingIndices.add(i);
      } else if (element.equals(listEl)) {
        matchingIndices.add(i);
      }
    }
    return matchingIndices;
  }

  /**
  *
  * @param <D> Zieltyp
  * @param <S> Quelltyp
  * @param collection  nicht null
  * @return  zu jedem Element in collection die Häufigkeit
  */
  public static <D, S extends D> Frequency<S> toFrequency(final Collection<S> collection) {
    final Frequency<S> frequency = new Frequency<>();
    collection.forEach(frequency::add);
    return frequency;
  }

  /**
   * Gibt das letzte Element einer Liste.
   *
   * @param list	nicht null.
   * @return		Letztes Element der Liste oder null, wenn die Liste leer.
   */
  public static <T> T getLast(final List<T> list) {
    RangeCheckUtils.assertReferenceParamNotNull("list", list);
    if (list.isEmpty())
      return null;
    final int n = list.size();
    return list.get(n - 1);
  }

  /**
   *
   * @param collection    nicht null, auch leer. Nicht veränderbar.
   * @param <E>           Typ der Elemente
   * @return              AbstractList oder die Collection selbst, wenn sie
   *                      schon eine AbstractList ist
   *
   */
  @SuppressWarnings("unchecked")
  public static <E> AbstractList<E> convertToList(final Collection<? extends E> collection) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", collection);
    if (collection instanceof AbstractList<?>)
      return (AbstractList<E>) collection;
    final AbstractList<E> list = new ArrayList<>(collection);
    return list;
  }

  /**
  *
  * @param collection    nicht null, auch leer. Nicht veränderbar.
  * @param <E>           Typ der Elemente
  * @return              AbstractSet oder die Collection selbst, wenn sie
  *                      schon eine AbstractSet ist
  *
  */
  @SuppressWarnings("unchecked")
  public static <E> AbstractSet<E> convertToSet(final Collection<? extends E> collection) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", collection);
    if (collection instanceof AbstractSet<?>)
      return (AbstractSet<E>) collection;
    final AbstractSet<E> set = new LinkedHashSet<>(collection);
    return set;
  }

  /**
  *
  * @param collection    nicht null, auch leer. Veränderbar.
  * @param <E>           Typ der Elemente
  * @return              AbstractList oder die Collection selbst, wenn sie
  *                      schon eine AbstractList ist
  *
  */
  public static <
      E>
    AbstractList<E>
    convertToModifiableList(final Collection<? extends E> collection) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", collection);
    final AbstractList<E> list = new ArrayList<>(collection);
    return list;
  }

  /**
   * Gibt das letzte Element einer collection.
   *
   * @param collection  nicht null.
   * @return      Letztes Element der collection oder null, wenn die
   * collection leer.
   */
  public static <T> T getLast(final Collection<T> collection) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", collection);
    return getLast(convertToList(collection));
  }

  /**
   * Gibt das i-te Element einer collection.
   *
   * @param collection  nicht null.
   * @param i     index
   * @return      null, wenn list == null || i < 0 || i >= list.size()
   */
  public static <T> Optional<T> get(final Collection<T> collection, final int i) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", collection);
    return getElement(convertToList(collection), i);
  }

  /**
   *
   * @param <T>   Typ
   * @param list  auch null
   * @param from  einschließlich
   * @param to    ausschließlich
   * @return      eventuell mit null aufgefüllte Liste der Größe (to - from), die
   *              genau die Elemente zwischen to und from enthält (oder null).
   *              Daher sind wird das Element bei from auf die Stelle 0 und
   *              Das Element bei to-1 auf die Stelle (to-from-1) abgebildet.
   * @throws      IllegalArgumentException, wenn from>=to
   */
  public static <T> List<T> newSubList(final List<? extends T> list, final int from, final int to) {
    if (from >= to)
      throw new IllegalArgumentException("from muss < to sein");
    final int size = to - from;
    final ArrayList<T> newlist = new ArrayList<>(size);
    for (int i = from; i < to; i++) {
      newlist.add(getElement(list, i).orElse(null));
    }
    return newlist;
  }

  /**
  *
  * @param <T>   Typ
  * @param list  auch null
  * @param from  einschließlich
  * @param to    ausschließlich
  * @return      nicht null.
  *              Eine Teilliste, basierend auf der ursprünglichen Liste. Die
  *              Indizes from und to werden dabei so normiert, dass
  *              <li> from <= to
  *              <li> from und to innerhalb der ursprünglichen Liste liegen. Daher werden
  *              from >= 0 und to <= list.size() gesetzt.
  */
  public static <T> List<T> subList(final List<T> list, int from, int to) {
    if (list == null || list.isEmpty())
      return Collections.emptyList();
    from = Integer.min(from, to);
    to = Integer.max(from, to);
    from = Integer.max(from, 0);
    to = Integer.min(list.size(), to);
    //    System.err.println(from + "..." + to);
    if (to - from < 0)
      return Collections.emptyList();
    return list.subList(from, to);
  }

  /**
   * Entfernt das letzte Element einer Liste. Wenn die Liste leer
   * ist, wird nichts getan.
   *
   * @param list	nicht null.
   *
   */
  public static void removeLast(final List<?> list) {
    RangeCheckUtils.assertReferenceParamNotNull("list", list);
    if (list.isEmpty())
      return;
    final int n = list.size();
    list.remove(n - 1);
  }

  /**
   * Gibt das erste Element einer Liste.
   *
   * @param list	nicht null.
   * @return		Erstes Element der Liste oder null, wenn die Liste leer.
   */
  public static <T> T getFirst(final List<T> list) {
    RangeCheckUtils.assertReferenceParamNotNull("list", list);
    if (list.isEmpty())
      return null;
    return list.get(0);
  }

  /**
   * Gibt das erste Element einer Liste.
   *
   * @param collection  nicht null.
   * @return      Erstes Element der Liste (kann auch null sein)
   *              oder null, wenn die Liste leer.
   */
  public static <T> T getFirst(final Collection<T> collection) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", collection);
    if (collection.isEmpty())
      return null;
    return collection.iterator().next();
  }

  /**
   *
   * Gibt das Maximum einer Collection. Verwende stattdessen {@link Collections#max(Collection)}.
   *
   * @param collection    nicht leer, darf kein null enthalten
   * @param <T>           implementiert {@link Comparable}
   * @return              Maximum oder null, wenn die Liste leer ist
   *
   */
  @Deprecated
  public static <T extends Comparable<T>> T getMax(final Collection<? extends T> collection) {
    T max = null;
    for (final T t : collection) {
      if (max == null)
        max = t;
      else if (t.compareTo(max) > 0)
        max = t;
    }
    return max;
  }

  /**
   *
   * Gibt das Minimum einer Collection.  Verwende stattdessen {@link Collections#min(Collection)}
   *
   * @param collection    nicht leer
   * @param <T>           implementiert {@link Comparable}
   * @return              Maximum
   *
   */
  @Deprecated
  public static <T extends Comparable<T>> T getMin(final Collection<? extends T> collection) {
    RangeCheckUtils.assertCollectionParamNotNullOrEmpty("", collection);
    T max = null;
    for (final T t : collection) {
      if (max == null)
        max = t;
      else if (t.compareTo(max) < 0)
        max = t;
    }
    return max;
  }

  public static int sumIntegers(final Collection<Integer> integers) {
    return FilterUtils.foldLeft(integers, (x, y) -> x + y, 0);
  }

  /**
   *
   * @param numbers   nicht null, kann leer sein
   * @return          Summe der Zahlen als double
   */
  public static Double sum(final Collection<? extends Number> numbers) {
    return FilterUtils.foldLeft(numbers, (x, y) -> x.doubleValue() + y.doubleValue(), 0.0);
  }

  /**
   *
   * @param numbers   nicht null, kann leer sein
   * @return          Standardabweichung, mittlere
   *                  Summe der Quadrate
   */
  public static Double variance(final Collection<? extends Number> numbers) {
    final double sumOfQuares =
      FilterUtils.foldLeft(numbers, (x, y) -> x.doubleValue() + Math.pow(y.doubleValue(), 2), 0.0);
    return sumOfQuares / numbers.size() - Math.pow(average(numbers), 2);
  }

  /**
  *
  * @param numbers   nicht null, kann leer sein
  * @return          Wurzel aus Standardabweichung
  */
  public static Double sigma(final Collection<? extends Number> numbers) {

    return Math.sqrt(variance(numbers));
  }

  /**
   *
   * @param numbers   nicht null, nicht leer
   * @return          Durchschnitt als double
   */
  public static Double average(final Collection<? extends Number> numbers) {
    RangeCheckUtils.assertCollectionParamNotNullOrEmpty("numbers", numbers);
    return sum(numbers) / numbers.size();
  }

  /**
   * Macht aus 2 geordneten Listen wie
   * <br>
   * [0, 1, 3, 5, 7)]<br>
   * [0, 2, 4, 6, 7]
   * <br>
   * etwas wie
   * <br>
   * [0, 1, null, 3, null, 5, null, 7]<br>
   * [0, null, 2, null, 4, null, 6, 7]
   * <br>
   * Die Listen müssen nicht geordnet sein, werden aber vorher sortiert.
   *
   * @param <T> Comparable, Quell- und Zieltyp
   * @param collection1 Typ T
   * @param collection2 Typ T
   * @return  2 Listen vom Typ T. An den Stellen,
   * an denen die Quell-Listen nicht übereinstimmen, wird null eingefügt.
   */
  public static <T extends Comparable<? super T>> Pair<List<T>, List<T>> diff(
    final Collection<T> collection1,
    final Collection<T> collection2) {
    return diff(collection1, collection2, T::compareTo);
  }

  /**
   * Macht aus 2 geordneten Listen wie
   * <br>
   * [0, 1, 3, 5, 7)]<br>
   * [0, 2, 4, 6, 7]
   * <br>
   * etwas wie
   * <br>
   * [0, 1, null, 3, null, 5, null, 7]<br>
   * [0, null, 2, null, 4, null, 6, 7]
   * <br>
   * Die Listen müssen nicht geordnet sein, werden aber vorher sortiert.
   *
   * @param <T> Zieltyp
   * @param <S> Quelltyp
   * @param collection1
   * @param collection2
   * @param comparator
   * @return  2 Listen vom Typ T. An den Stellen,
   * an denen die Quell-Listen nicht übereinstimmen, wird null eingefügt.
   */
  public static <T, S extends T> Pair<List<T>, List<T>> diff(
    final Collection<S> collection1,
    final Collection<S> collection2,
    final Comparator<? super S> comparator) {
    // erst mal sortieren:
    final ArrayList<S> tempList1 = new ArrayList<>(collection1);
    final ArrayList<S> tempList2 = new ArrayList<>(collection2);
    Collections.sort(tempList1, comparator);
    Collections.sort(tempList2, comparator);

    final ArrayList<T> list1 = new ArrayList<>();
    final ArrayList<T> list2 = new ArrayList<>();
    // verarbeiten:
    final Iterator<S> iterator1 = tempList1.iterator();
    final Iterator<S> iterator2 = tempList2.iterator();
    S elem1 = iterator1.hasNext() ? iterator1.next() : null;
    S elem2 = iterator2.hasNext() ? iterator2.next() : null;
    while (true) {
      if (elem1 == null && elem2 == null) { // Ende beider Listen
        break;
      }
      if (elem1 == null) {
        list1.add(null);
        list2.add(elem2);
        elem2 = iterator2.hasNext() ? iterator2.next() : null;
      } else if (elem2 == null) {
        list1.add(elem1);
        list2.add(null);
        elem1 = iterator1.hasNext() ? iterator1.next() : null;
      } else { // beide Listen noch nicht aufgebraucht:
        if (StringUtils.equals(elem1, elem2)) {
          list1.add(elem1);
          list2.add(elem2);
          elem1 = iterator1.hasNext() ? iterator1.next() : null;
          elem2 = iterator2.hasNext() ? iterator2.next() : null;
        } else if (comparator.compare(elem1, elem2) > 0) {
          list1.add(null);
          list2.add(elem2);
          elem2 = iterator2.hasNext() ? iterator2.next() : null;
        } else { // elem1 < elem2
          list1.add(elem1);
          list2.add(null);
          elem1 = iterator1.hasNext() ? iterator1.next() : null;
        }
      }
    }
    return new Pair<List<T>, List<T>>(list1, list2);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final List<Number> list1 = Arrays.asList(1);
    System.out.println(findMatchingIndices(list1, 1.0));
    final Number i = Integer.valueOf(1);
    final Number d = Double.valueOf(1.0);
    System.out.println(i == d);

  }

}
