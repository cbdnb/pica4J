package de.dnb.gnd.parser.line;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.tag.EnumeratingTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

public class EnumeratingLineFactory extends GNDLineFactory  {

	public EnumeratingLineFactory(final EnumeratingTag tag) {
		super(tag);
	}

	@Override
	public final EnumeratingLine createLine() {
		return new EnumeratingLine((EnumeratingTag) tag, subfieldList);
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException 
	 */
	public static void main(String[] args) throws IllFormattedLineException {
		Tag tag = GNDTagDB.getDB().findTag("008");
		LineFactory factory = tag.getLineFactory();
		factory.load(Format.PICA3, "aa;bb", false);
		Line line = factory.createLine();
		System.out.println(line);

	}

}
