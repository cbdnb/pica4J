package de.dnb.basics.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * Utility-Klasse zur Anwendung eines Filters auf eine Liste von Werten.
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 */
public final class FilterUtils {

  private FilterUtils() {
    super();
  }

  /**
   * Splittet eine Liste in Teillisten. Jedes Element, das predicate
   * erfüllt führt eine neue Teilliste an. Alle
   * Teillisten sind nicht leer.
   *
   * @param list		Liste, nicht null.
   * @param predicate	Prädikat
   * @return			Liste von Listen.
   */
  public static <E> List<List<E>> split(final List<E> list, final Predicate<? super E> predicate) {
    final List<List<E>> lists = new LinkedList<>();
    List<E> tempList = new LinkedList<>();
    for (final E e : list) {
      if (predicate.test(e)) {
        // == 0 nur beim ersten Element
        if (tempList.isEmpty()) {
          lists.add(tempList);
          tempList = new LinkedList<>();
        }
      }
      tempList.add(e);
    }
    lists.add(tempList);
    return lists;

  }

  /**
   * Liefert eine gefilterte neue Liste. Es werden nur die Elemente
   * berücksichtigt, die filter erfüllen.
   *
   * @param source	nicht null.
   * @param filter	nicht null.
   * @return			Neue Liste, nicht null.
   */
  public static <R, T extends R, S extends T> List<T> newFilteredList(
    final Iterable<S> source,
    final Predicate<R> filter) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    RangeCheckUtils.assertReferenceParamNotNull("filter", filter);
    final List<T> filteredValues = new ArrayList<>();
    source.forEach(s ->
    {
      if (filter.test(s))
        filteredValues.add(s);
    });

    return filteredValues;
  }

  /**
   * Liefert zwei Listen: eine, die predicate erfüllt, und eine zweite,
   * die predicate nicht erfüllt.
   *
   * @param source	Liste, nicht null.
   * @param predicate	nicht null.
   * @return			Paar zweier Listen.
   */
  public static <R, T extends R, S extends T> Pair<List<T>, List<T>> divide(
    final Iterable<S> source,
    final Predicate<R> predicate) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    RangeCheckUtils.assertReferenceParamNotNull("filter", predicate);
    final List<T> accepting = new ArrayList<>();
    final List<T> denying = new ArrayList<>();
    source.forEach(s ->
    {
      if (predicate.test(s))
        accepting.add(s);
      else
        denying.add(s);
    });
    return new Pair<>(accepting, denying);
  }

  /**
   * Liefert zwei Listen: eine, deren Elemente in membersOfFirstList
   * enthalten sind und eine, für die das nicht gilt.
   *
   * @param source                nicht null
   * @param membersOfFirstList    nicht null
   * @param <T>                   Typ
   * @return                      2 Listen, beide nicht null
   */
  @SafeVarargs
  public static <
      T>
    Pair<List<T>, List<T>>
    divide(final Iterable<T> source, final T... membersOfFirstList) {
    final Set<T> set = new HashSet<>(Arrays.asList(membersOfFirstList));
    return divide(source, set::contains);
  }

  /**
   * Entfernt aus einer Collection Elemente, die nicht von filter
   * akzeptiert werden.
   *
   * @param iterable	nicht null, veränderbar.
   * @param filter	nicht null.
   * @param <E>       Typ der Collection
   */
  public static <E> void filter(final Iterable<E> iterable, final Predicate<? super E> filter) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", iterable);
    RangeCheckUtils.assertReferenceParamNotNull("filter", filter);
    for (final Iterator<E> it = iterable.iterator(); it.hasNext();)
      if (!filter.test(it.next()))
        it.remove();
  }

  //@formatter:off
	/**
	 * Findet das erste Element einer iterierbaren Entität, das dem Kriterium
	 * predicate entspricht.
	 *
	 * @param iterable		nicht null.
	 * @param predicate		nicht null.
	 * @return				erstes gefundenes Element, sonst null.
	 */
	public static <E> E find(
			final Iterable<E> iterable,
			final Predicate<? super E> predicate) {
		RangeCheckUtils.assertReferenceParamNotNull(
				"collection", iterable);
		RangeCheckUtils.assertReferenceParamNotNull(
				"predicate", predicate);
		//@formatter:on
    for (final E e : iterable) {
      if (predicate.test(e))
        return e;
    }
    return null;
  }

  /**
   * Findet heraus, ob iterable ein Element enthält, aus das das Prädikat
   * zutrifft.
   * @param iterable	nicht null.
   * @param predicate	nicht null.
   *
   * @return 			true, wenn ein Element gefunden.
   */
  public static <
      E>
    boolean
    contains(final Iterable<E> iterable, final Predicate<? super E> predicate) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", iterable);
    RangeCheckUtils.assertReferenceParamNotNull("predicate", predicate);
    return find(iterable, predicate) != null;
  }

  public static boolean contains(final Iterable<String> iterable, final Pattern pattern) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", iterable);
    RangeCheckUtils.assertReferenceParamNotNull("Regexp", pattern);
    return contains(iterable, s -> pattern.matcher(s).matches());
  }

  public static boolean contains(final Iterable<String> iterable, final String regexp) {
    RangeCheckUtils.assertReferenceParamNotNull("collection", iterable);
    RangeCheckUtils.assertReferenceParamNotNull("Regexp", regexp);
    Pattern pattern;
    try {
      pattern = Pattern.compile(regexp);
    } catch (final Exception e) {
      throw new IllegalArgumentException("contains: '" + regexp + "' falsch gebildet.");
    }
    return contains(iterable, s -> pattern.matcher(s).matches());
  }

  /**
   * Findet den Zeiger auf das erste Element einer iterierbaren Entität,
   * das gleich element ist. Es kommt equals() zum Einsatz.
   *
   * @param iterable		nicht null.
   * @param element		ein zu suchendes Element
   * @return				Zeiger auf das erste gefundenes Element,
   * 						sonst null.
   */
  public static <E> E findPointerTo(final Iterable<E> iterable, final E element) {
    final Predicate<E> equalsPredicate = new Equals<>(element);
    return find(iterable, equalsPredicate);
  }

  /**
   * Findet den Index des ersten Elementes einer Liste, das identisch element ist.
   * Es wird also == verwendet.
   *
   * @param list			nicht null.
   * @param element		nicht null.
   * @return				index des ersten gefundenes Elementes, sonst -1.
   */
  public static <E, F extends E> int indexOfIdentical(final List<E> list, final F element) {
    RangeCheckUtils.assertReferenceParamNotNull("list", list);
    int index = 0;
    for (final E e : list) {
      if (e == element) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Ersetzt das erste Listenelement, das identisch element ist,
   * durch replacement. (Es wird == verwendet).
   *
   * @param list
   * @param element
   * @param replacement
   *
   * @return true, wenn etwas ersetzt werden konnte.
   */
  public static <
      E, F extends E, G extends E>
    boolean
    replaceIdentical(final List<E> list, final F element, final G replacement) {
    RangeCheckUtils.assertReferenceParamNotNull("list", list);
    RangeCheckUtils.assertReferenceParamNotNull("element", element);
    RangeCheckUtils.assertReferenceParamNotNull("replacement", replacement);
    if (element == replacement)
      return false;
    final int index = indexOfIdentical(list, element);
    if (index >= 0) {
      list.set(index, replacement);
      return true;
    }
    return false;
  }

  /**
   * Findet das erste Listenelement, das gleich element ist, und ersetzt
   * es durch replacement. Es wird equals() verwendet.
   *
   * @param list
   * @param element
   * @param replacement
   *
   * @return true, wenn etwas ersetzt wurde.
   */
  public static <
      E, F extends E, G extends E>
    boolean
    replace(final List<E> list, final F element, final G replacement) {
    RangeCheckUtils.assertReferenceParamNotNull("list", list);
    RangeCheckUtils.assertReferenceParamNotNull("element", element);
    RangeCheckUtils.assertReferenceParamNotNull("replacement", replacement);
    //    if (element.equals(replacement))
    //      return false;
    final int index = list.indexOf(element);
    if (index >= 0) {
      list.set(index, replacement);
      return true;
    }
    return false;
  }

  /**
   * Vergleicht 2 Listen, wobei nur die Elemente berücksichtigt werden,
   * die predicate erfüllen.
   *
   * @param iterable1		nicht null
   * @param iterable2		nicht null
   * @param predicate		nicht null
   * @return				true, wenn nach Entfernen der nicht Erfüllenden
   * 						die Elemente der beiden Iterables Stück
   * 						für Stück gleich sind.
   */
  public static <
      E, F extends E, G extends E>
    boolean
    equals(final Iterable<F> iterable1, final Iterable<G> iterable2, final Predicate<E> predicate) {
    RangeCheckUtils.assertReferenceParamNotNull("iterable1", iterable1);
    RangeCheckUtils.assertReferenceParamNotNull("iterable2", iterable2);
    RangeCheckUtils.assertReferenceParamNotNull("predicate", predicate);
    final List<E> list1 = newFilteredList(iterable1, predicate);
    final List<E> list2 = newFilteredList(iterable2, predicate);
    return list1.equals(list2);
  }

  /**
   * Ersetzt ein altes Element der Menge durch ein neues an
   * der Position des alten. Es wird auch ersetzt, wenn gilt
   * element.equals(replacement), da möglicherweise doch noch
   * Unterschiede bestehen (Variable, die nicht in equals() eingehen,
   * aber weitere Information enthalten).
   *
   * @param set			LinkedHashSet, nicht null.
   * @param element		nicht null
   * @param replacement	nicht null
   * @return				true, wenn ersetzt wurde.
   */
  public static <
      E, F extends E, G extends E>
    boolean
    replace(final LinkedHashSet<E> set, final F element, final G replacement) {
    RangeCheckUtils.assertReferenceParamNotNull("set", set);
    RangeCheckUtils.assertReferenceParamNotNull("element", element);
    RangeCheckUtils.assertReferenceParamNotNull("replacement", replacement);
    // nicht nötig:
    if (element == replacement)
      return false;
    final List<E> list = new LinkedList<>(set);
    final boolean replaced = replaceIdentical(list, element, replacement);
    if (replaced) {
      set.clear();
      set.addAll(list);
      return true;
    }
    return false;
  }

  /**
   * Erzeugt eine neue iterierbare Menge, mit function auf jedes Element
   * von from angewandt.
   *
   * @param source		nicht null, kann leer sein.
   * @param function	nicht null.
   * @param <S>		domain.
   * @param <T>		Range.
   *
   * @return			Menge der Werte, kann leer sein, nie null.
   */
  public static <S, T> ArrayList<T> map(
    final Iterable<S> source,
    final Function<? super S, ? extends T> function) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    RangeCheckUtils.assertReferenceParamNotNull("function", function);
    final ArrayList<T> dest = new ArrayList<>();
    source.forEach(s ->
    {
      final T t = function.apply(s);
      dest.add(t);
    });

    return dest;
  }

  /**
   * Erzeugt eine neue iterierbare Menge, mit function auf jedes Element
   * von from angewandt. Entstehende Null-Werte werden gefiltert.
   *
   * @param source		nicht null, kann leer sein.
   * @param function	nicht null.
   * @param <S>		domain.
   * @param <T>		Range.
   *
   * @return			Menge der Werte ohne null. Kann leer sein,
   *                  nie null
   */
  public static <S, T> ArrayList<T> mapNullFiltered(
    final Iterable<S> source,
    final Function<? super S, ? extends T> function) {
    RangeCheckUtils.assertReferenceParamNotNull("source", source);
    RangeCheckUtils.assertReferenceParamNotNull("function", function);

    final ArrayList<T> dest = map(source, function);
    filter(dest, new NullPredicate<T>().negate());
    return dest;
  }

  /**
   * Realisiert die Listenoperation höherer Ordnung left fold.
   * Auch bekannt als: reduce, accumulate, aggregate, compress, oder inject.
   * Berechnet für eine Liste
   * 		(a, b , c ...)
   * den Wert
   * 		f...f(f(f(initial, a), b), c) ...)
   *
   * @param iterable	iterierbare Menge aus Werten, die mit dem
   * 					Definitionsbereich des zweiten Arguments von
   * 					f verträglich sind (Untertypen von X).
   * @param function	Binäre Funktion, deren erstes argument mit
   * 					dem Wertebereichverträglich ist und deren
   * 					zweites Argument ein Supertyp des Listentyps
   * 					ist..
   * @param initial	Anfangswert.
   * @param <X>		Definitionsbereich von f.
   * @param <F>		Wertebereich von f.
   * @return			gefalteten Wert.
   *
   */
  public static <X, F> F foldLeft(
    final Iterable<? extends X> iterable,
    final BiFunction<F, X, ? extends F> function,
    final F initial) {
    F value = initial;
    for (final X x : iterable) {
      value = function.apply(value, x);
    }
    return value;
  }

  public static void main(final String... ar) {
    final List<String> strings = Arrays.asList("a", "b", "c");
    System.out.println(contains(strings, "a|b*"));
  }
}
