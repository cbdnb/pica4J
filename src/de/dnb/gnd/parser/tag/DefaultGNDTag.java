package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.DefaultLineFactory;

public class DefaultGNDTag extends GNDTag {

	public DefaultGNDTag(
		final String pica3,
		final String picaPlus,
		final String german,
		final Repeatability repeatability,
		final String marc,
		final String english) {
		super(pica3, picaPlus, german, repeatability, marc, english);
	}

	public DefaultGNDTag(
		final String pica3,
		final String picaPlus,
		final String german,
		final Repeatability repeatability,
		final String marc,
		final String english,
		final String aleph) {
		super(pica3, picaPlus, german, repeatability, marc, english, aleph);
	}

	@Override
	public DefaultLineFactory getLineFactory() {
		return new DefaultLineFactory(this);
	}

}
