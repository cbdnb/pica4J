/**
 * 
 */
package de.dnb.basics.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Ordnet der Kombination mehrerer Merkmale einen Wert zu. Diese
 * Merkmalskombination kann als Element des Kartesischen Produktes mehrerer
 * Merkmalsmengen aufgefasst werden.
 *
 * Genauer: Es wird eine Funktion <br>
 * <code>f(i<sub>1</sub>, i<sub>2</sub>, ..., i<sub>n</sub>) &isin; V,
 * i<sub>k</sub> &isin; M<sub>k</sub></code>
 * <br> nachgebildet
 *
 * @param <V>	Typ, auf den abgebildet wird
 *
 * @author Christian_2
 *
 */
public class CrossProductMap<V>
    implements Iterable<Collection<? extends Object>>, CrossProduct<V> {

    private Map<Collection<? extends Object>, V> data = new HashMap<>();

    /**
     * 
     * @param value    Wert
     * @param indices  Kombination mehrerer Merkmale
     */
    public final
        void
        put(final V value, final Collection<? extends Object> indices) {
        data.put(indices, value);
    }

    /**
     * 
     * @param value    Wert
     * @param indices  Kombination mehrerer Merkmale
     */
    public final void putValues(final V value, final Object... indices) {
        put(value, Arrays.asList(indices));
    }

    /**
     * 
     * @param indices  Kombination mehrerer Merkmale
     * @return         zugehöriges Objekt oder null
     */
    @Override
    public final V get(final Collection<? extends Object> indices) {
        return data.get(indices);
    }

    /**
     * 
     * @param indices  Kombination mehrerer Merkmale
     * @return         zugehöriges Objekt oder null
     */
    @Override
    public V get(final Object... indices) {
        return data.get(Arrays.asList(indices));
    }

    /**
     * 
     * @return Funktionswerte als Menge von {@link Entry}.
     */
    public final Set<Entry<Collection<? extends Object>, V>> getEntries() {
        return data.entrySet();
    }

    @Override
    public String toString() {
        Set<Entry<Collection<? extends Object>, V>> set = getEntries();
        String s = "";
        for (Iterator<Entry<Collection<? extends Object>, V>> iterator =
            set.iterator(); iterator.hasNext();) {
            Entry<Collection<? extends Object>, V> entry = iterator.next();
            s += entry.getKey() + "\t" + entry.getValue();
            if (iterator.hasNext())
                s += "\n";
        }
        return s;
    }

    @Override
    public final Iterator<Collection<? extends Object>> iterator() {
        return data.keySet().iterator();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        CrossProductMap<Integer> map = new CrossProductMap<>();

        map.putValues(1, 'a', 'a');
        map.putValues(2, 'a', 'b');
        map.putValues(3, 'b', 'a');
        map.putValues(4, 'b', 'b');

        System.out.println(map.get('a', 'b'));

        ArrayList<Character> characters = new ArrayList<>();
        characters.add('a');
        characters.add('c');
        map.put(0, characters);
        System.out.println(map.get(characters));

        System.out.println(map);
        System.out.println();

        Collection<Character> list = new LinkedList<>();
        list.add('a');
        list.add('a');
        map.put(-1, list);

        map.put(9, null);

        System.out.println(map);

        for (Collection<? extends Object> collection : map) {
            System.out.println("" + collection + map.get(collection));
        }

    }

}
