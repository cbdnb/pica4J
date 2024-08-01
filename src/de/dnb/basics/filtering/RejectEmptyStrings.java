package de.dnb.basics.filtering;

import java.util.function.Predicate;

public class RejectEmptyStrings implements Predicate<String> {

	@Override
    public boolean test(final String string) {
		return !string.trim().isEmpty();
	}

}
