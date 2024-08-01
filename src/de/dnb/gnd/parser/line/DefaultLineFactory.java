package de.dnb.gnd.parser.line;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.tag.DefaultGNDTag;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

public class DefaultLineFactory extends GNDLineFactory  {

	public DefaultLineFactory(final DefaultGNDTag gndTag) {
		super(gndTag);
	}

	@Override
	public final DefaultGNDLine createLine() {
		return new DefaultGNDLine((DefaultGNDTag) getTag(), subfieldList);
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException 
	 */
	public static void main(String[] args) throws IllFormattedLineException {
		Tag gNDTag = GNDTagDB.getDB().findTag("150");
		LineFactory factory = gNDTag.getLineFactory();
		factory.load(Format.PICA3, "aa$vBB", false);
		Line line = factory.createLine();
		System.out.println(line);

	}

}
