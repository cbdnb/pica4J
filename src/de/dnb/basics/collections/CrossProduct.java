/**
 * 
 */
package de.dnb.basics.collections;

import java.util.Collection;

/**
 * Zusammenfassend (und etwas ungenau) für alle Abbildungen
 *
 * <br>
 * <code>(i<sub>1</sub>, i<sub>2</sub>, ..., i<sub>n</sub>) &rarr; V</code>.
 * <br>
 *
 * @param <V>   Typ, auf den abgebildet wird
 *
 * @author baumann
 *
 *
 */
public interface CrossProduct<V> {

    /**
     *
     * @param indices  Kombination mehrerer Merkmale
     * @return         zugehöriges Objekt oder null
     */
    V get(Object... indices);

    /**
     *
     * @param indices  Kombination mehrerer Merkmale
     * @return         zugehöriges Objekt oder null
     */
    V get(Collection<? extends Object> indices);

}
