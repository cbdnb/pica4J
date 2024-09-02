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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.dnb.basics.applicationComponents.MyFileUtils;

/**
 * Implementation of Multimap that uses an LinkedList to store the values for a
 * given key. A HashMap associates each key with an LinkedList of values.
 *
 * This multimap allows duplicate key-value pairs. After adding a new
 * key-value pair equal to an existing key-value pair, the ListMultimap
 * will contain entries for both the new value and the old value.
 * Keys and values may be null.
 *
 * @author Christian_2
 *
 * @param <K>
 * @param <V>
 */
public class ListMultimap<K, V> extends Multimap<K, V> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5034359466647088987L;

    public ListMultimap() {
        super(new HashMap<K, Collection<V>>());
    }

    @Override
    protected Collection<V> getNewValueCollection() {
        return new LinkedList<V>();
    }

    /**
     * Parameterlos wegen Serialisierung.
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public ListMultimap(final String fileName)
        throws IOException,
        ClassNotFoundException {
        this();
        InputStream fileInp = new FileInputStream(fileName);
        final ObjectInputStream objectin = new ObjectInputStream(fileInp);
        @SuppressWarnings("unchecked")
        ListMultimap<K, V> readObject =
            (ListMultimap<K, V>) objectin.readObject();

        MyFileUtils.safeClose(objectin);
        addAll(readObject);
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final Multimap<String, String> multimap = new ListMultimap<>();
        multimap.add("1", "a");
        multimap.add("1", "b");
        multimap.add("1", "c");
        multimap.add("1", "a");
        multimap.add("2", "a");
        multimap.add("2", "b");
        multimap.add("3", "c");
        multimap.add("4");
        multimap.add("1");
        multimap.add("1", null);

        System.out.println(multimap);
        System.out.println();
        for (final Iterator<String> iterator =
            multimap.valuesIterator(); iterator.hasNext();) {
            System.out.println(iterator.next());
        }
        System.out.println(multimap.get("4"));

        try {
            multimap.safe("d:/temp/lmm.out");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("-----");
        try {
            ListMultimap<String, String> lmm =
                new ListMultimap<>("d:/temp/lmm.out");
            System.out.println(lmm);
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
