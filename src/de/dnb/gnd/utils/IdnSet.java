/**
 *
 */
package de.dnb.gnd.utils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.utils.NumberUtils;
import de.dnb.basics.utils.TimeUtils;

/**
 * Eine Datenstruktur, um Integer-Idns -also Idns ohne Prüfziffer-
 * effizient zu speichern. Dazu wird ein {@link BitSet} benutzt.
 * Da der Speicherbedarf für typische Idns recht hoch ist (ca. 16 MB),
 * lohnt es sich aus diesem Grund, IdnSet erst ab etwa 280.000 Idns diese Datenstruktur zu nutzen.
 * <br>
 * Für kleinere Mengen kann es sinnvoller sein, ein {@link HashSet} von Integers zu
 * verwenden. Zur sicheren Manipulation verwende die Methoden von {@link IDNUtils},
 * etwa {@link IDNUtils#add(Collection, String)}. Und: diese Datenstruktur
 * etwas weniger Zeit!
 *
 * @author baumann
 *
 *
 *
 */
public class IdnSet implements Set<Integer>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -5455671371759547063L;

  private BitSet bitSet;

  private transient BitSet temporary;

  @Override
  public int size() {
    return bitSet.cardinality();
  }

  @Override
  public boolean isEmpty() {
    return bitSet.isEmpty();
  }

  @Override
  public boolean contains(final Object o) {
    if (o instanceof Integer) {
      final Integer i = (Integer) o;
      return contains(i);
    }
    if (o instanceof String) {
      final String s = (String) o;
      return contains(s);
    }
    return false;
  }

  public boolean contains(final Integer i) {
    if (i < 0)
      return false;
    return bitSet.get(i);
  }

  public boolean contains(final String s) {
    final int i = IDNUtils.idn2int(s);
    return contains(i);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      int next = -1;

      @Override
      public Integer next() {
        next = bitSet.nextSetBit(next + 1);
        return next;
      }

      @Override
      public boolean hasNext() {
        return bitSet.nextSetBit(next + 1) != -1;
      }
    };
  }

  @Override
  public Object[] toArray() {
    return toArray(new Object[0]);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    final int size = size();
    if (a.length < size) {
      final T[] newInstance = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
      a = newInstance;
    }

    final Iterator<Integer> it = iterator();
    for (int i = 0; i < size; i++)
      a[i] = (T) it.next();

    if (a.length > size)
      a[size] = null;
    return a;
  }

  @Override
  public boolean add(final Integer i) {
    if (i == null || i < 0)
      return false;
    final boolean contains = contains(i);
    if (!contains)
      bitSet.set(i);
    return !contains;
  }

  /**
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   *
   * @param idn beliebig
   * @return  this geändert
   */
  public boolean add(final String idn) {
    final int i = IDNUtils.idn2int(idn);
    return add(i);
  }

  public boolean addObject(final Object o) {
    if (o instanceof Integer) {
      final Integer i = (Integer) o;
      return add(i);
    }
    if (o instanceof String) {
      final String s = (String) o;
      return add(s);
    }
    return false;
  }

  @Override
  public boolean remove(final Object o) {
    if (o instanceof Integer) {
      final Integer i = (Integer) o;
      return remove(i);
    }
    if (o instanceof String) {
      final String s = (String) o;
      return remove(s);
    }
    return false;
  }

  public boolean remove(final Integer i) {
    if (i == null || i < 0)
      return false;
    final boolean contains = contains(i);
    if (contains)
      bitSet.set(i, false);
    return contains;
  }

  /**
   * Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   *
   * @param s beliebig
   * @return  this geändert
   */
  public boolean remove(final String s) {
    final int i = IDNUtils.idn2int(s);
    return remove(i);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    for (final Object e : c)
      if (!contains(e))
        return false;
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends Integer> c) {
    boolean modified = false;
    for (final Integer e : c)
      if (add(e))
        modified = true;
    return modified;
  }

  /**
   * Vereinigung. Funktioniert sowohl mit 4093770-7 als auch mit 118696424.
   *
   * @param idns nicht null
   * @return  ob this geändert wurde
   */
  public boolean addAllIdns(final Collection<String> idns) {
    boolean modified = false;
    for (final String idn : idns)
      if (add(idn))
        modified = true;
    return modified;
  }

  /**
   * Vereinigung.
   *
   * @param other nicht null
   * @return  ob this geändert wurde
   */
  public boolean addAll(final IdnSet other) {
    temporary = (BitSet) bitSet.clone();
    final BitSet otherBitset = other.bitSet;
    bitSet.or(otherBitset);
    final boolean equals = temporary.equals(otherBitset);
    temporary = null;
    return !equals;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    Objects.requireNonNull(c);
    final IdnSet tempSet = new IdnSet();

    for (final Object o : c) {
      if (contains(o)) {
        tempSet.addObject(o);
      }
    }
    final boolean unmodified = bitSet.equals(tempSet.bitSet);
    if (!unmodified)
      bitSet = tempSet.bitSet;
    return unmodified;
  }

  /**
   * Schnitt.
   *
   * @param other nicht null
   * @return  ob this geändert wurde
   */
  public boolean retainAll(final IdnSet other) {
    temporary = (BitSet) bitSet.clone();
    final BitSet otherBitset = other.bitSet;
    bitSet.and(otherBitset);
    final boolean equals = temporary.equals(otherBitset);
    temporary = null;
    return !equals;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    Objects.requireNonNull(c);
    boolean modified = false;
    for (final Object o : c) {
      if (contains(o)) {
        remove(o);
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Mengendifferenz.
   *
   * @param other nicht null
   * @return  ob this geändert wurde
   */
  public boolean removeAll(final IdnSet other) {
    temporary = (BitSet) bitSet.clone();
    final BitSet otherBitset = other.bitSet;
    bitSet.andNot(otherBitset);
    final boolean equals = temporary.equals(otherBitset);
    temporary = null;
    return !equals;
  }

  @Override
  public String toString() {
    final int max = 3;
    String retV = "IdnSet [";
    final int size = size();
    if (size < 2 * max + 1) {
      retV += StringUtils.concatenate(", ", this);

    } else {
      int next = -1;
      for (int i = 0; i < max; i++) {
        next = bitSet.nextSetBit(next + 1);
        retV += next + ", ";
      }
      next = size + 1;
      String s = "";
      for (int i = 0; i < max; i++) {
        next = bitSet.previousSetBit(next - 1);
        s = ", " + next + s;
      }
      retV = retV + "..." + s;
    }
    return retV + "]";
  }

  @Override
  public void clear() {
    bitSet.clear();
  }

  public static void main(final String[] args) {
    final Runtime rt = Runtime.getRuntime();
    System.out.println(("mem: " + (rt.totalMemory() - rt.freeMemory()) / 1_000_000));
    TimeUtils.startStopWatch();

    //    final List<String> list1 = Arrays.asList("1136084800", "1136083383");
    //    final List<String> list2 = Arrays.asList("122591700X", "1136084851", "4019294-5");

    final IdnSet set = new IdnSet();
    set.add("1136084800");
    for (int i = 0; i < 280_000; i++) {
      set.add(i);
    }
    System.out.println(("mem: " + (rt.totalMemory() - rt.freeMemory()) / 1_000_000));
    System.out.println("Einfügen set1: " + TimeUtils.delta_t_millis());
    TimeUtils.startStopWatch();
    final Set<Integer> integers = new HashSet<>();
    for (int i = 0; i < 280_000; i++) {
      IDNUtils.add(integers, i);
    }
    System.out.println(("mem: " + (rt.totalMemory() - rt.freeMemory()) / 1_000_000));
    System.out.println("Einfügen integers1: " + TimeUtils.delta_t_millis());

    //------------

    final IdnSet set2 = new IdnSet();
    for (int i = 100_000; i < 380_000; i++) {
      set2.add(i);
    }

    System.out.println(TimeUtils.delta_t_millis());

    final Set<Integer> integers2 = new HashSet<>();
    for (int i = 100_000; i < 380_000; i++) {
      integers2.add(i);
    }
    TimeUtils.startStopWatch();
    set.removeAll(set2);
    System.out.println("Vereinigung sets: " + TimeUtils.delta_t_millis());

    TimeUtils.startStopWatch();
    integers.removeAll(integers2);
    System.out.println("Vereinigung integers: " + TimeUtils.delta_t_millis());

  }

  /**
   *
   */
  public IdnSet() {
    bitSet = new BitSet();
  }

  /**
   * Copy-Konstruktor.
   *
   * @param other nicht null
   */
  public IdnSet(final IdnSet other) {
    bitSet = (BitSet) other.bitSet.clone();
  }

  /**
   *
   * @param s               auch null
   * @param mitPruefziffer  es werden nur die Treffer mit korrekter Prüfziffer genommen,
   *                        andernfalls alles, was wie eine positive Zahl aussieht
   */
  public IdnSet(final String s, final boolean mitPruefziffer) {
    this();
    Collection<Integer> ints;
    if (mitPruefziffer) {
      ints = IDNUtils.extractIDNsasInts(s);
    } else {
      ints = NumberUtils.getAllInts(s);
    }
    addAll(ints);
  }
}
