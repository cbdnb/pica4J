package de.dnb.gnd.parser.line;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.tag.BiblioPersonTag;

public class BiblioPersonLineFactory extends BibLineFactory {

	public BiblioPersonLineFactory(final BiblioPersonTag tag) {
		super(tag);
	}

	@Override
	public final Line createLine() {
		return new BiblioPersonLine((BiblioPersonTag) getTag(), subfieldList);
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException 
	 */
	public static void main(String[] args) throws IllFormattedLineException {

	}

}
