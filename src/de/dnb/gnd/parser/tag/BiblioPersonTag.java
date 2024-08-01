package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.BiblioPersonLineFactory;

public class BiblioPersonTag extends BibliographicTag {

  BiblioPersonTag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english) {
    super(pica3, picaPlus, german, repeatability, marc, english);

  }

  @Override
  public BiblioPersonLineFactory getLineFactory() {
    return new BiblioPersonLineFactory(this);
  }

}
