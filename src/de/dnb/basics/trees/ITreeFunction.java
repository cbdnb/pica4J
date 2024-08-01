package de.dnb.basics.trees;

import java.util.Collection;
import java.util.function.Function;

/**
 *
 * Funktion, die einem Knoten die Menge der Kinder zuweist.
 * extends IFunction&lt;T, Collection&lt;T>>.
 *
 * @author baumann
 *
 * @param <T>           Typ des Baums
 */
public interface ITreeFunction<T> extends Function<T, Collection<T>> {

}
