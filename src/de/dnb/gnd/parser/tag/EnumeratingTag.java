package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.EnumeratingLineFactory;

public class EnumeratingTag extends GNDTag {

  public EnumeratingTag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english) {
    super(pica3, picaPlus, german, repeatability, marc, english);
  }

  public EnumeratingTag(
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
  public EnumeratingLineFactory getLineFactory() {
    return new EnumeratingLineFactory(this);
  }

}
