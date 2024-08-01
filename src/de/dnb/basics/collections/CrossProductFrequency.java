/**
 *
 */
package de.dnb.basics.collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.dnb.basics.applicationComponents.strings.StringUtils;

/**
 * Zählt die Häufigkeit einer Kombination mehrerer Merkmale. Diese
 * Merkmalskombination kann als Element des Kartesischen Produktes mehrerer
 * Merkmalsmengen aufgefasst werden.
 *
 * Genauer: Es wird die Funktion <br>
 * <code>#(i<sub>1</sub>, i<sub>2</sub>, ..., i<sub>n</sub>),
 * i<sub>k</sub> &isin; M<sub>k</sub></code>
 * <br> nachgebildet
 *
 * @author Christian_2
 *
 */
public class CrossProductFrequency extends Frequency<Collection<? extends Object>>
  implements CrossProduct<Long> {

  /**
   *
   */
  private static final long serialVersionUID = -8347464637639671298L;

  /**
   * Lädt eine Häufigkeitsverteilung.
   *
   * @param fileName                  nicht null
   *                                  die in einer Datei gespeichert war
   * @throws ClassNotFoundException   Wenn nicht in Frequency konvertiert
   *                                  werden kann
   * @throws IOException              sonst
   */
  public CrossProductFrequency(final String fileName) throws ClassNotFoundException, IOException {
    super(fileName);
  }

  /**
   *
   */
  public CrossProductFrequency() {
    super();
  }

  /**
   * Erhöht den Zähler für die Merkmalskombination values um 1.
   * Da die values in eine Liste umgewandelt werden, muss man
   * aufpassen, wenn man auch Collections via {@link Frequency#add(Object)}
   * einfügt: Sets und Listen sind nie gleich, auch wenn sie die gleichen
   * Elemente enthalten!
   *
   * @param values	beliebig
   */
  public final void addValues(final Object... values) {
    final List<Object> v = Arrays.asList(values);
    //		System.err.println("add:" + v);
    Long l = values2long.get(v);
    if (l == null) {
      l = new Long(1);
    } else {
      l++;
    }
    values2long.put(v, l);
  }

  /**
   * Erhöht den Zähler für die Merkmalskombination objects um increment.
   *
   * @param increment	beliebig
   * @param objects	beliebig
   */
  public final void incrementValues(final long increment, final Object... objects) {
    final List<Object> v = Arrays.asList(objects);
    //		System.err.println("inc:" + v);
    Long l = values2long.get(v);
    if (l == null) {

      l = new Long(increment);
    } else {
      l += increment;
    }
    values2long.put(v, l);
  }

  /**
   * Legt einen neuen Schlüssel an,
   * wenn noch nicht vorhanden. Der Wert
   * eines neuen Schlüssels ist 0.
   *
   * @param objects auch null
   */
  public void addKeys(final Object... objects) {
    final List<Object> v = Arrays.asList(objects);
    super.addKey(v);
  }

  /**
   * Liefert eine reduzierte Verteilungsfunktion mit weniger Merkmalen.
   * Über die nicht interessierenden Merkmale wird summiert.
   *
   *
   * @param remainingIndices	Indices der beibehaltene Merkmale, größer 0
   * @return					neue Verteilungsfunktion
   */
  public final CrossProductFrequency getPartialSum(final int... remainingIndices) {
    // Maximum finden:
    int max = Integer.MIN_VALUE;
    for (final int i : remainingIndices) {
      if (i < 0)
        throw new IllegalArgumentException("beizubehaltender Index " + i + " ist negativ");
      max = Math.max(max, i);
    }

    final CrossProductFrequency newFrequency = new CrossProductFrequency();
    final Iterator<Collection<? extends Object>> iterator = valuesIterator();
    for (; iterator.hasNext();) {
      final Object[] nextValues = iterator.next().toArray();
      if (nextValues.length > max) {
        final List<Object> reducedValues = new ArrayList<>();
        for (final int i : remainingIndices) {
          final Object value = nextValues[i];
          reducedValues.add(value);
        }
        newFrequency.incrementValues(getCount(nextValues), reducedValues);
      }
    }
    return newFrequency;

  }

  /**
   *
   * @param values	beliebig
   * @return			Die Häufigkeit der Merkmalskombination values; 0, wenn
   * 					nicht vorhanden.
   */
  public final long getCount(final Object... values) {
    final List<Object> v = Arrays.asList(values);
    return super.get(v);
  }

  @Override
  public String toString() {
    final Set<Entry<Collection<? extends Object>, Long>> set = getEntries();
    String s = "";
    for (final Iterator<Entry<Collection<? extends Object>, Long>> iterator =
      set.iterator(); iterator.hasNext();) {
      final Entry<Collection<? extends Object>, Long> entry = iterator.next();
      final Collection<? extends Object> key = entry.getKey();
      s += StringUtils.concatenateTab(key) + "\t" + entry.getValue();
      if (iterator.hasNext())
        s += "\n";
    }
    return s;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final CrossProductFrequency frequency = new CrossProductFrequency();
    frequency.incrementValues(1);
    frequency.increment(Collections.emptySet(), 1);
    frequency.incrementValues(1, "1", "a");
    frequency.incrementValues(2, "2", "a");
    frequency.incrementValues(3, "1", "b");
    frequency.incrementValues(4, "2", "b");

    System.out.println("empty:\t" + frequency.get(Collections.emptySet()));

    System.out.println(frequency.getDistribution());
    System.out.println("-----------");
    System.out.println("freq1\n" + frequency);
    System.out.println();
    System.out.println("partial(0)\n" + frequency.getPartialSum(0));
    System.out.println("-----------");
    System.out.println("partial(1,0)\n" + frequency.getPartialSum(1, 0));
    System.out.println("-----------");
    System.out.println("Count(\"1\", \"a\")\t" + frequency.getCount("1", "a"));
    System.out.println("Count(\"1\", \"x\")\t" + frequency.getCount("1", "x"));

    final ArrayList<String> list = new ArrayList<>();
    list.add("1");
    list.add("a");
    frequency.increment(list, 1);

    System.out.println("freq1\n" + frequency);

    try {
      frequency.safe("d:/temp/cf.out");
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("-----------");
    try {
      final CrossProductFrequency fr = new CrossProductFrequency("d:/temp/cf.out");
      System.out.println("freq-load\n" + fr);
    } catch (ClassNotFoundException | IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see de.dnb.basics.statistics.CrossProduct#get(java.lang.Object[])
   */
  @Override
  public Long get(final Object... indices) {
    return getCount(indices);
  }

  /* (non-Javadoc)
   * @see de.dnb.basics.statistics.CrossProduct#get(java.util.Collection)
   */
  @Override
  public Long get(final Collection<? extends Object> indices) {
    return super.get(indices);
  }

}
