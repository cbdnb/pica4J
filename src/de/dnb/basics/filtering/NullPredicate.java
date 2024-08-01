package de.dnb.basics.filtering;

import java.util.function.Predicate;

/**
 * Testet, ob ein übergebenes Element null ist.
 *
 * @author baumann
 *
 * @param <T>   Typ
 */
public class NullPredicate<T> implements Predicate<T> {

	@Override
    public final boolean test(final T element) {
		return element == null;
	}

}
