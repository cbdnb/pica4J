package de.dnb.basics.filtering;

import java.util.function.Predicate;

public class AcceptEverything<T> implements Predicate<T> {

	@Override
    public boolean test(final T element) {
		return true;
	}

}
