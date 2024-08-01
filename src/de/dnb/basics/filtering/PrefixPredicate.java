package de.dnb.basics.filtering;

import java.util.function.Predicate;

/**
 * Akzeptiert, wenn das zu überprüfende Element mit prefix beginnt.
 * Ist das zu überprüfende Element null, so wird false
 * zurückgegeben.
 *
 * @author baumann
 *
 */
public class PrefixPredicate implements Predicate<String> {

	/**
	 * Konstruktor.
	 * @param aPrefix nicht null.
	 */
	public PrefixPredicate(final String aPrefix) {
		RangeCheckUtils.assertStringParamNotNullOrEmpty("prefix", aPrefix);
		prefix = aPrefix;
	}

	private final String prefix;

	@Override
    public final boolean test(final String element) {
		if (element == null)
			return false;
		else
			return element.startsWith(prefix);
	}

}
