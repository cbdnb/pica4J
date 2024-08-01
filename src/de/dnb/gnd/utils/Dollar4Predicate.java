package de.dnb.gnd.utils;

import java.util.function.Predicate;

import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;

/**
 * Akzeptiert Zeilen, die ein $4 haben, das mit prefix anfängt.
 * @author baumann
 *
 */
public class Dollar4Predicate implements Predicate<Line> {

	private final String prefix;

	public Dollar4Predicate(final String prefix) {
		super();
		this.prefix = prefix;
	}

	@Override
    public boolean test(final Line line) {
		final Subfield subfield = SubfieldUtils.getFirstSubfield(line, '4');
		if(subfield == null)
			return false;
		return subfield.getContent().startsWith(prefix);
	}

}
