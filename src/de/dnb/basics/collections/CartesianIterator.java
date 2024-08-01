/**
 *
 */
package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator für Cartesische Produkte von Mengen gleichen Typs. Wenn der Typ unterschiedlich ist,
 * muss man Object als Typ nehmen.
 * @author baumann
 *
 * @param <T> gemeinsamer Typ, eventuell Object
 */
public class CartesianIterator<T> implements Iterator<List<T>> {

  private final List<List<T>> lists;
  private int current = 0;
  private final long last;

  public CartesianIterator(final List<List<T>> ll) {
    this.lists = ll;
    long product = 1L;
    for (final List<T> list : lists)
      product *= list.size();
    last = product;
  }

  @Override
  public boolean hasNext() {
    return current != last;
  }

  @Override
  public List<T> next() {
    ++current;
    return get(current - 1, lists);
  }

  @Override
  public void remove() {
    ++current;
  }

  private List<T> get(final int n, final List<List<T>> listOfLists) {
    switch (listOfLists.size()) {
    case 0:
      return new ArrayList<T>(); // no break past return;
    default: {
      final List<T> firstList = listOfLists.get(0);
      final List<T> retList = new ArrayList<T>();
      retList.add(firstList.get(n % firstList.size()));
      retList.addAll(get(n / firstList.size(), listOfLists.subList(1, listOfLists.size())));
      return retList;
    }
    }
  }

  public static void main(final String[] args) {
    final List<Object> lc = Arrays.asList('A', 'B', 'C', 'D');
    final List<Object> lC = Arrays.asList('a', 'b', 'c');
    final List<Object> li = Arrays.asList(1, 2, 3, 4);
    final List<Object> ld = Arrays.asList();
    // sometimes, a generic solution like List <List <String>>
    // might be possible to use - typically, a mixture of types is
    // the common nominator
    final List<List<Object>> llo = new ArrayList<List<Object>>();
    llo.add(lc);
    llo.add(lC);
    llo.add(li);
    //    llo.add(ld);

    // Preparing the List of Lists is some work, but then ...
    final CartesianIterator<Object> ci = new CartesianIterator<Object>(llo);

    ci.forEachRemaining(System.out::println);

  }
}
