/**
 * 
 */
package de.dnb.basics.marc;

import java.util.Comparator;

/**
 * @author baumann
 *
 */
public final class MarcSubfieldComparator
		implements Comparator<Character> {
	@Override
	public int compare(final Character ch1, final Character ch2) {

		// Zahlen hinter Buchstaben:
		if (!Character.isDigit(ch1) && Character.isDigit(ch2))
			return -1;
		if (Character.isDigit(ch1) && !Character.isDigit(ch2))
			return +1;

		return Character.compare(ch1, ch2);
	}
}