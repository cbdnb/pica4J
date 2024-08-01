package de.dnb.basics.filtering;

import java.util.function.Predicate;

/**
 * Filterkriterium GLEICH basierend auf equals().
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 *
 * @param <T>   irgendwas.
 */
public class Equals<T> implements Predicate<T> {

	private final T acceptedValue;

	public Equals(final T acceptedValue) {
		RangeCheckUtils.assertReferenceParamNotNull("acceptedValue",
			acceptedValue);
		this.acceptedValue = acceptedValue;
	}

	@Override
    public final boolean test(final T object) {
		return acceptedValue.equals(object);
	}
}
