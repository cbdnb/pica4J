/**
 *
 */
package de.dnb.basics.collections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import de.dnb.basics.applicationComponents.MyFileUtils;

/**
 * Implementation of Multimap that uses an LinkedHashSet to store the values for a
 * given key. A SetMultimap associates each key with an LinkedHashSet of values.
 *
 * <p>This multimap allows no duplicate key-value pairs.
 *  After adding a new
 * key-value pair equal to an existing key-value pair, the SetMultimap
 * will contain entries only for the old value.
 * Keys and values are allowed to be null.
 *
 * @author Christian_2
 *
 * @param <K> keys
 * @param <V> values
 */
public class SetMultimap<K, V> extends Multimap<K, V> implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -8099249521841526145L;

  @Override
  protected Collection<V> getNewValueCollection() {
    return new LinkedHashSet<>();
  }

  /**
   * Basiert auf {@link LinkedHashMap}
   */
  public SetMultimap() {
    super(new LinkedHashMap<>());
  }

  /**
   * Parameterlos wegen Serialisierung.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public SetMultimap(final String fileName) throws IOException, ClassNotFoundException {
    this();
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final SetMultimap<K, V> readObject = (SetMultimap<K, V>) objectin.readObject();
    MyFileUtils.safeClose(objectin);
    addAll(readObject);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final SetMultimap<Integer, Integer> multimap = new SetMultimap<>();
    multimap.addAll(1, 2, 3, 4, 2);
    multimap.addAll(null, 2, 3, null, 2);
    System.out.println(multimap);
  }

}
