package de.dnb.basics.collections;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import de.dnb.basics.applicationComponents.FileUtils;

/**
 * Multimap, deren Index eine Kombination mehrerer Merkmale ist.
 * @author baumann
 *
 * @param <V>   Typ
 */
public class CrossProductMultimap<V> extends ListMultimap<Collection<? extends Object>, V>
  implements CrossProduct<Iterable<V>> {

  /**
   * 
   * @param value    Wert
   * @param indices  Kombination mehrerer Merkmale
   */
  public final void addValue(final V value, final Object... indices) {
    add(Arrays.asList(indices), value);
  }

  /* (non-Javadoc)
   * @see de.dnb.basics.statistics.CrossProduct#get(java.lang.Object)
   */
  @Override
  public Iterable<V> get(final Object... indices) {
    return get(Arrays.asList(indices));
  }

  /**
   * 
   * @param indices  Kombination mehrerer Merkmale
   * @return         zugehöriges Objekt oder null
   */
  public Iterable<V> getNullSafe(final Object... indices) {
    final Collection<V> kValues = get(Arrays.asList(indices));
    if (kValues != null)
      return kValues;
    else
      return Collections.emptyList();
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1425991503169087186L;

  /**
   * Parameterlos wegen Serialisierung.
   * @throws IOException 
   * @throws ClassNotFoundException 
   */
  public CrossProductMultimap(final String fileName) throws IOException, ClassNotFoundException {
    this();
    final InputStream fileInp = new FileInputStream(fileName);
    final ObjectInputStream objectin = new ObjectInputStream(fileInp);
    @SuppressWarnings("unchecked")
    final CrossProductMultimap<V> readObject = (CrossProductMultimap<V>) objectin.readObject();

    FileUtils.safeClose(objectin);
    addAll(readObject);
  }

  /**
   * 
   */
  public CrossProductMultimap() {
    super();
  }

  public static void main(final String[] args) {
    final CrossProductMultimap<String> multimap = new CrossProductMultimap<>();
    multimap.addValue("D");
    multimap.addValue("b");
    multimap.addValue("c");
    multimap.addValue("e", "1", "x");
    multimap.addValue("A", "1", "x");
    multimap.addValue("B", "x", "1");

    System.out.println(multimap.toString());

    try {
      multimap.safe("d:/temp/cpm.out");
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("----------");

    try {
      final CrossProductMultimap<String> cpm = new CrossProductMultimap<>("d:/temp/cpm.out");
      System.out.println(cpm);
    } catch (ClassNotFoundException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
