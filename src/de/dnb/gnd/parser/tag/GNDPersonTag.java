package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.GNDPersonLineFactory;

public class GNDPersonTag extends GNDTag {

  public GNDPersonTag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english) {
    super(pica3, picaPlus, german, repeatability, marc, english);
  }

  @Override
  public GNDPersonLineFactory getLineFactory() {
    return new GNDPersonLineFactory(this);
  }

}
