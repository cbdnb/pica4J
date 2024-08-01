package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.dnb.basics.filtering.RangeCheckUtils;

public class BiMapVisitor<T> {

  /**
   * Traversierungsordnung.
   * <li>PREORDER: Zunächst der Knoten, dann die Kinder
   * <li>POSTORDER: Zunächst die Kinder, dann der Knoten.
   *
   * @author baumann
   *
   */
  public static enum Order {
      /**
       *   Zunächst der Knoten, dann die Kinder.
       */
      PREORDER,
      /**
       * Zunächst die Kinder, dann der Knoten.
       */
      POSTORDER
  }

  /**
   * Traversierungsrichtung.
   * <li>KEY2VALUE: Zu den Keys werden rekursiv alle Values gesucht.
   * <li>VALUE2KEY: Zu den Values werden rekursiv alle Keys gesucht.
   *
   * @author baumann
   *
   */
  public static enum Direction {
      /**
       * Zu den Keys werden rekursiv alle Values gesucht.
       */
      KEY2VALUE,
      /**
       * Zu den Values werden rekursiv alle Keys gesucht.
       */
      VALUE2KEY
  }

  private final Consumer<T> consumer;
  private final IBiMap<T, T> bimap;
  private Consumer<Collection<T>> reporterFunction = null;

  /**
   *
   * @param bimap  nicht null
   * @param consumer  Das auszuführende Code-Fragment. Der consumer verarbeitet
   *                  den aktuellen Knoten
   */
  public BiMapVisitor(final IBiMap<T, T> bimap, final Consumer<T> consumer) {
    RangeCheckUtils.assertReferenceParamNotNull("bimap", bimap);
    RangeCheckUtils.assertReferenceParamNotNull("consumer", consumer);
    this.bimap = bimap;
    this.consumer = consumer;
  }

  /**
   * Besucht alle Knoten des Graphen, ausgehend von nodes. Kein Knoten wird zweimal besucht.
   * Nicht vorhandene nodes werden ignoriert.
   *
   * @param treeOrder   {@link Order#POSTORDER} oder {@link Order#PREORDER}
   * @param direction   {@link Direction#KEY2VALUE} oder {@link Direction#VALUE2KEY}
   * @param nodes       nicht null
   */
  public void visitNodesAndChildren(
    final Order treeOrder,
    final Direction direction,
    final Collection<T> nodes) {

    final HashSet<T> visited = new HashSet<>();
    for (final T node : nodes) {
      final LinkedHashSet<T> path = new LinkedHashSet<>();
      visitNodeAndChildren(node, treeOrder, direction, path, visited);
    }

  }

  /**
   * Besucht alle Knoten des Graphen, ausgehend von nodes. Kein Knoten wird zweimal besucht.
   * Nicht vorhandene nodes werden ignoriert.
   *
   * @param treeOrder   {@link Order#POSTORDER} oder {@link Order#PREORDER}
   * @param direction   {@link Direction#KEY2VALUE} oder {@link Direction#VALUE2KEY}
   * @param nodes       auch leer
   */
  public void visitNodesAndChildren(
    final Order treeOrder,
    final Direction direction,
    @SuppressWarnings("unchecked") final T... nodes) {
    visitNodesAndChildren(treeOrder, direction, Arrays.asList(nodes));
  }

  /**
   *
   * @param node        Startknoten, nicht vorhandener Knoten wird ignoriert.
   * @param treeOrder   {@link Order#POSTORDER} oder {@link Order#PREORDER}
   * @param direction   {@link Direction#KEY2VALUE} oder {@link Direction#VALUE2KEY}
   * @param path        Pfad vom Startknoten zum aktuellen Knoten, wird rekursiv weitergereicht
   * @param visited     Schon besuchte Knoten, werden kein zweites Mal besucht
   */
  private void visitNodeAndChildren(
    final T node,
    final Order treeOrder,
    final Direction direction,
    final LinkedHashSet<T> path,
    final Set<T> visited) {

    // Am Anfang?
    if (path.isEmpty()) {
      //    Absicherung, wenn nach nicht Vorhandenem gesucht wird:
      if (direction == Direction.KEY2VALUE && !bimap.containsKey(node))
        return;
      if (direction == Direction.VALUE2KEY && !bimap.containsValue(node))
        return;
    }

    if (path.contains(node)) {
      // Nur dann liegt ein Zyklus vor, wenn auch child im aktuellen
      // Pfad liegt. Es könnten ja auch mehrere Wege zum selben
      // Knoten führen!
      // Da ein Hashset ein Element nur ein einziges Mal enthalten
      // kann, in eine Liste kopieren:
      final ArrayList<T> reportList = new ArrayList<>(path);
      reportList.add(node);
      reportCycle(reportList);
      return;
    }
    if (visited.contains(node)) {
      return;
    }

    visited.add(node);
    path.add(node);
    if (treeOrder == Order.PREORDER) {
      consumer.accept(node);
    }

    // Traverse children:
    final Set<T> children =
      direction == Direction.KEY2VALUE ? bimap.getValueSet(node) : bimap.getKeySet(node);
    children.forEach(child -> visitNodeAndChildren(child, treeOrder, direction, path, visited));

    if (treeOrder == Order.POSTORDER) {
      consumer.accept(node);
    }
    path.remove(node);
  }

  private void reportCycle(final Collection<T> collection) {

    if (reporterFunction != null)
      reporterFunction.accept(collection);
    else
      System.err.println("Zyklus erkannt: " + collection);
  }

  /**
   * Setzt eine Funktion, die Zyklen im Graphen behandelt. Diese Funktion
   * nimmt den Pfad von der Wurzel bis zum aktuellen (falschen) Knoten
   * entgegen.
   *
   * @param reporterFunction  auch null, dann wird System.err.println() genommen.
   *
   */
  public final void setReporterFunction(final Consumer<Collection<T>> reporterFunction) {
    this.reporterFunction = reporterFunction;
  }

  public static void main(final String[] args) {
    final BiMultimap<Integer, Integer> biMultimap = BiMultimap.createListMap();
    //    biMultimap.addAll(1, 2);
    //    biMultimap.addAll(2, 1);
    biMultimap.addAll(1, 2);
    biMultimap.addAll(0, 2);
    biMultimap.addAll(2, 3);
    //    biMultimap.addAll(3, 1);
    final BiMapVisitor<Integer> visitor =
      new BiMapVisitor<>(biMultimap, t -> System.out.println(t));
    visitor.visitNodesAndChildren(Order.PREORDER, Direction.KEY2VALUE, 0, 1, -1);
    System.out.println();
    visitor.visitNodesAndChildren(Order.PREORDER, Direction.KEY2VALUE, -2);
    System.out.println();
    visitor.visitNodesAndChildren(Order.PREORDER, Direction.KEY2VALUE);
  }

}
