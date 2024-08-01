package de.dnb.basics.filtering;

import java.util.function.Predicate;

/**
 * Filterkriterium GRÖSSER basierend auf Comparable.
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 */
public class Greater<T extends Object & Comparable<T>> implements
    Predicate<T> {
	private final T lowerBound;

	public Greater(final T lowerBound) {
		RangeCheckUtils.assertReferenceParamNotNull("lowerBound",
			lowerBound);

		this.lowerBound = lowerBound;
	}

	@Override
    public final boolean test(final T object) {
		return lowerBound.compareTo(object) < 0;
	}
}
