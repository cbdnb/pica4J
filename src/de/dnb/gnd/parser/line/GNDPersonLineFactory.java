package de.dnb.gnd.parser.line;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.GNDPersonTag;
import de.dnb.gnd.parser.tag.Tag;

public class GNDPersonLineFactory extends GNDLineFactory {

	public GNDPersonLineFactory(final GNDPersonTag tag) {
		super(tag);
	}

	@Override
	public final Line createLine() {
		return new GNDPersonLine((GNDPersonTag) getTag(), subfieldList);
	}

	@Override
	protected final void replaceDescriptionSigns() {

		if (!related) {
			/*
			 * Finde "$a..., " und ersetze durch $a...$d.
			 * ($1 im Replacement ist der Inhalt der 
			 *  Capturing group 1)
			 * 
			 */
			// etwa: "(\\$a[^$]*), "
			String regex =
				"(" + escapedSeparator + "a[^" + subfieldSeparator
					+ "]*), ";
			// etwa: "$1\\$d"
			String replacement = "$1" + escapedSeparator + 'd';
			contentStr = contentStr.replaceAll(regex, replacement);

		}
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException 
	 */
	public static void main(String[] args) throws IllFormattedLineException {
		Tag gndTag = GNDTagDB.getDB().findTag("100");
		LineFactory factory = gndTag.getLineFactory();
		factory.load(Format.PICA3, "aa, bb", false);
		Line line = factory.createLine();
		System.out.println(line);

	}

}
