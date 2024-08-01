/**
 *
 */
package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.dnb.basics.applicationComponents.Streams;
import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * @author baumann
 *
 */
public class CartesianProducts {

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final List<Integer> aList = Arrays.asList(1, 2, 3);
    final List<Integer> bList = Arrays.asList(4, 5, 6);
    //    bList = Arrays.asList();
    // Stream<List<Integer>> product = aList.stream().flatMap(a -> bList
    // .stream().flatMap(b -> Stream.of(Arrays.asList(a, b))));
    final Stream<Pair<Integer, Integer>> product = cartesianPairStream(aList, bList);

    product.forEach(p ->
    {
      System.out.println(p);
    });

    System.out.println("---------");

    final Map<String, Collection<?>> map = new LinkedHashMap<>();
    map.put("A", Arrays.asList("a1", "a2", "a3", "a4"));
    map.put("B", Arrays.asList("b1", "b2", "b3"));
    map.put("C", Arrays.asList("c1", "c2"));
    map.put("D", Arrays.asList());
    cartesianProductStream(map.values()).forEach(System.out::println);

  }

//@formatter:off
  /**
   *
   * @param <T1>
   * @param <T2>
   * @param it1
   * @param it2
   * @return
   */
  public static <T1, T2> Stream<Pair<T1, T2>>
    cartesianPairStream(final Iterable<T1> it1, final Iterable<T2> it2) {
    return Streams.getStreamFromIterable(it1)
      .flatMap(a -> Streams.getStreamFromIterable(it2).map(b -> new Pair<>(a, b)));
  }

  /**
   *
   * @param <T1>
   * @param <T2>
   * @param it1
   * @param it2
   * @return
   */
  public static <T1, T2> Iterable<Pair<T1, T2>>
    cartesianPairs(final Iterable<T1> it1, final Iterable<T2> it2) {
    return cartesianPairStream(it1, it2).collect(Collectors.toList());
  }


//@formatter:on
  /**
   *
   * @param <T>         gemeinsamer Typ, eventuell Object
   * @param collections Zu verkettende Mengen
   * @return            Kartesisches Produkt
   */
  public static <T> Stream<List<T>> cartesianProductStream(
    final Collection<? extends Collection<T>> collections) {
    return combine(new ArrayList<Collection<T>>(collections), Collections.emptyList());
  }

  /**
   * A * B * C * ... = A * (B * C * ...) = {a1} * (B * C ..) + {a2} *(B * C ..) + ..
   *
   * @param <T>
   * @param collections       verbleibende Mengen
   * @param currentTuple      schon erzeugtes Tupel
   * @return
   */
  private static <T> Stream<List<T>> combine(
    final List<? extends Collection<T>> collections,
    final List<T> currentTuple) {
    if (collections.isEmpty())
      return Stream.of(currentTuple);
    else {
      final Collection<T> head = collections.get(0);
      final List<? extends Collection<T>> tail = collections.subList(1, collections.size());
      // Streams aneinanderhängen:
      return head.stream().flatMap(e ->
      {
        final List<T> newTuple = new ArrayList<T>(currentTuple);
        newTuple.add(e);
        return combine(tail, newTuple);
      });
    }
  }
}
