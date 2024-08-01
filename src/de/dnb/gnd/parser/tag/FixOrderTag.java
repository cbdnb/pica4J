package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Repeatability;

/**
 * Für händische Festlegung, ob in fester Ordnung
 * geparst werden soll.
 *
 * @author baumann
 *
 */
public class FixOrderTag extends BibliographicTag {

  FixOrderTag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english) {
    super(pica3, picaPlus, german, repeatability, marc, english);
  }

}
