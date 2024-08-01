package de.dnb.basics.filtering;

import java.util.function.Predicate;

/**
 * Filterkriterium KLEINER GLEICH basierend auf Comparable.
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 */
public class LowerOrEqual<T extends Object & Comparable<T>> implements
    Predicate<T> {
	private final T acceptedValue;

	public LowerOrEqual(final T acceptedValue) {
		RangeCheckUtils.assertReferenceParamNotNull("acceptedValue",
			acceptedValue);

		this.acceptedValue = acceptedValue;
	}

	@Override
    public final boolean test(final T object) {
		return acceptedValue.compareTo(object) >= 0;
	}
}
