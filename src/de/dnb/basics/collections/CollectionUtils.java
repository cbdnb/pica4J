/**
 *
 */
package de.dnb.basics.collections;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.dnb.basics.applicationComponents.MyFileUtils;

/**
 * @author baumann
 *
 */
public class CollectionUtils {

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final List<Integer> list1 = Arrays.asList(1, 2);
    final List<Integer> list2 = Arrays.asList(2, 3);
    System.out.println(intersection(list1, list2));
    System.out.println(intersection(list1, list2));
    System.out.println(list1);
    System.out.println(list2);
  }

  /**
  *
  * @param collection   Jedes mögliche Objekt, hier speziell eine Collection oder Map
  * @param fileName     Wohin gespeichert werden soll
  * @throws IOException Wenn es irgendwo hapert
  */
  public static void save(final Object collection, final String fileName) throws IOException {
    final FileOutputStream fos = new FileOutputStream(fileName);
    final ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(collection);
    MyFileUtils.safeClose(oos);
  }

  /**
  *
  * @param <K>
  * @param <V>
  * @param fileName
  * @return
  * @throws IOException
  * @throws ClassNotFoundException
  */
  public static <K, V> BiMultimap<K, V> loadBiMultimap(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final BiMultimap<K, V> readObject = (BiMultimap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  /**
   *
   * @param <K>
   * @param <V>
   * @param fileName
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static <K, V> BiMap<K, V> loadBimap(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final BiMap<K, V> readObject = (BiMap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <K, V> HashMap<K, V> loadHashMap(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final HashMap<K, V> readObject = (HashMap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <K, V> LinkedHashMap<K, V> loadLinkedHashMap(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final LinkedHashMap<K, V> readObject = (LinkedHashMap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <K, V> TreeMap<K, V> loadTreeMap(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final TreeMap<K, V> readObject = (TreeMap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  /**
   *
   * @param <K>
   * @param <V>
   * @param fileName
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static <K, V> ListMultimap<K, V> loadListMultimap(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final ListMultimap<K, V> readObject = (ListMultimap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <
      K extends Comparable<K>, V extends Comparable<V>>
    TreeMultimap<K, V>
    loadTreeMultimap(final String fileName) throws IOException, ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final TreeMultimap<K, V> readObject = (TreeMultimap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <
      T>
    HashSet<T>
    loadHashSet(final String fileName) throws IOException, ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final HashSet<T> readObject = (HashSet<T>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <T> LinkedHashSet<T> loadLinkedHashSet(final String fileName)
    throws IOException,
    ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final LinkedHashSet<T> readObject = (LinkedHashSet<T>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  public static <K, V> List<V> getValues(final Map<K, V> map, final Collection<K> keys) {
    final ArrayList<V> values = new ArrayList<>();
    keys.forEach(key ->
    {
      if (map.containsKey(key)) {
        final V generator = map.get(key);
        values.add(generator);
      }
    });
    return values;
  }

  public static <
      T>
    TreeSet<T>
    loadTreeSet(final String fileName) throws IOException, ClassNotFoundException {
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final TreeSet<T> readObject = (TreeSet<T>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    System.err.println("Fertig mit Laden: " + fileName);
    return readObject;
  }

  /**
   * Vereinigung: coll1 ∪ coll2, enthält keine Duplikate! Eine Garantie über
   * die Reihenfolge besteht nicht.
   *
   * @param <T>   Typ
   * @param coll1 auch null
   * @param coll2 auch null
   * @return      nicht null, neue Menge
   */
  public static <T> Set<T> union(final Collection<T> coll1, final Collection<T> coll2) {
    final HashSet<T> set1 = coll1 != null ? new HashSet<>(coll1) : new HashSet<>();
    final HashSet<T> set2 = coll2 != null ? new HashSet<>(coll2) : new HashSet<>();
    if (set1.size() > set2.size()) {
      set1.addAll(set2);
      return set1;
    } else {
      set2.addAll(set1);
      return set2;
    }
  }

  /**
   * Schnittmenge: coll1 ∩ coll2, enthält keine Duplikate! Eine Garantie über
   * die Reihenfolge besteht nicht.
   *
   * @param <T>   Typ
   * @param coll1 auch null
   * @param coll2 auch null
   * @return      Neue Menge, nicht null
   */
  public static <T> Set<T> intersection(final Collection<T> coll1, final Collection<T> coll2) {
    final HashSet<T> set1 = coll1 != null ? new HashSet<>(coll1) : new HashSet<>();
    final HashSet<T> set2 = coll2 != null ? new HashSet<>(coll2) : new HashSet<>();
    if (set2.size() > set1.size()) {
      set1.retainAll(set2);
      return set1;
    } else {
      set2.retainAll(set1);
      return set2;
    }
  }

  /**
   * Die Schnittmenge coll1 ∩ coll2 ist nicht leer.
   *
   * @param <T>   Typ
   * @param coll1 auch null oder leer -> false
   * @param coll2 auch null oder leer -> false
   * @return      coll1 enthält irgend ein Element von coll2
   */
  public static <T> boolean intersects(final Collection<T> coll1, final Collection<T> coll2) {
    if (coll2 == null || coll2.isEmpty() || coll1 == null || coll1.isEmpty())
      return false;
    // coll1/coll2 kann nicht mehr null sein:
    if (coll1.size() < coll2.size())
      return coll1.stream().anyMatch(coll2::contains);
    else
      return coll2.stream().anyMatch(coll1::contains);
  }

  /**
   * Teilmenge: coll1 ⊂ coll2
   *
   * @param <T>   Typ
   * @param coll1 auch null oder leer -> true; die leere Menge ist Teilmenge jeder Menge
   * @param coll2 auch null oder leer -> nur false, wenn auch coll1 leer
   * @return      coll1 enthält irgend ein Element von coll2
   */
  public static <T> boolean isSubsetOf(final Collection<T> coll1, final Collection<T> coll2) {
    if (coll1 == null || coll1.isEmpty())
      return true;
    // coll1 kann nicht mehr null=leer sein:
    if (coll2 == null || coll2.isEmpty())
      return false;
    return coll2.containsAll(coll1);
  }

  /**
   * Asymmetrische Differenz: coll1 \ coll2, enthält keine Duplikate!
   *
   * @param <T>   Typ
   * @param coll1 auch null
   * @param coll2 auch null
   * @return      nicht leer
   */
  public static <T> Set<T> difference(final Collection<T> coll1, final Collection<T> coll2) {
    final LinkedHashSet<T> set1 =
      coll1 != null ? new LinkedHashSet<>(coll1) : new LinkedHashSet<>();
    final LinkedHashSet<T> set2 =
      coll2 != null ? new LinkedHashSet<>(coll2) : new LinkedHashSet<>();
    set1.removeAll(set2);
    return set1;
  }

  /**
   * Symmetrische Differenz: coll1 Δ coll2, enthält keine Duplikate!
   *
   * @param <T>   Typ
   * @param coll1 auch null
   * @param coll2 auch null
   * @return      nicht leer
   */
  public static <
      T>
    Set<T>
    symmetricDifference(final Collection<T> coll1, final Collection<T> coll2) {
    return union(difference(coll1, coll2), difference(coll2, coll1));
  }

  /**
  *
  * @param <T>         Typ
  * @param collection  Beliebig, auch null
  * @param headerSize  Maximalzahl der am Anfang und Ende angezeigten Elemente
  *
  * @return            Verkürzte Ansicht einer Collection in der Art: [1, 2, .. ,4 ,5]
  */
  public static <T> String shortView(final Collection<T> collection, final int headerSize) {
    return shortView(collection, headerSize, headerSize);
  }

  /**
  *
  * @param <T>         Typ
  * @param collection  Beliebig, auch null  *
  *
  * @return            Verkürzte Ansicht einer Collection in der Art: [1, 2, .. ,4 ,5].
  *                     Es werden maximal 2 Elemente am Anfang und Ende angezeigt.
  */
  public static <T> String shortView(final Collection<T> collection) {
    return shortView(collection, 2);
  }

  /**
   *
   * @param <T>         Typ
   * @param collection  Beliebig, auch null
   * @param headerSize  Maximalzahl der am Anfang angezeigten Elemente
   * @param tailSize    Maximalzahl der am Ende angezeigten Elemente
   * @return            Verkürzte Ansicht einer Collection in der Art: [1, 2, .. ,4 ,5]
   */
  public static <
      T>
    String
    shortView(final Collection<T> collection, final int headerSize, final int tailSize) {
    if (collection == null)
      return "[]";
    final int size = collection.size();
    if (size <= headerSize + tailSize)
      return collection.toString();
    final List<T> asList = ListUtils.convertToList(collection);

    String s = "[";
    for (int i = 0; i < headerSize - 1; i++) {
      s += asList.get(i) + ", ";
    }
    s += asList.get(headerSize - 1) + ",";

    if (tailSize != 0) {
      s += " .. ";
      s += "," + asList.get(size - tailSize);
    } else {
      s += " ..";
    }
    for (int i = size - tailSize + 1; i < size; i++) {
      s += " ," + asList.get(i);
    }
    s += "]";
    return s;
  }

  /**
   *
   * @param obj beliebig, auch null
   * @return    true:<br>obj==null, <br>Collection, Array, Map ist leer<br>
   *            CharSequence enthält nur Whitespace<br>
   *            false: sonst
   */
  public static boolean isNullOrEmpty(final Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj.getClass().isArray()) {
      return Array.getLength(obj) == 0;
    }
    if (obj instanceof CharSequence) {
      final CharSequence charseq = (CharSequence) obj;
      return charseq.toString().trim().length() == 0;
    }
    if (obj instanceof Collection) {
      return ((Collection) obj).isEmpty();
    }
    if (obj instanceof Map) {
      return ((Map) obj).isEmpty();
    }

    // else
    return false;
  }
}
