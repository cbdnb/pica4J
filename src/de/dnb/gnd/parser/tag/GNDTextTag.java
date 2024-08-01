package de.dnb.gnd.parser.tag;

import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Repeatability;
import de.dnb.gnd.parser.line.TextLineFactory;

public class GNDTextTag extends GNDTag {

  public static GNDTextTag getMxTag() {
    final GNDTextTag MX_TAG =
      new GNDTextTag("901", "047A/01", "Mailbox", Repeatability.REPEATABLE, "912", "", "901");
    MX_TAG.add(new Indicator('z', "Datum", Repeatability.NON_REPEATABLE, ""));
    MX_TAG.add(new Indicator('b', "Absender/Empfänger", Repeatability.NON_REPEATABLE, ""));
    MX_TAG.add(new Indicator('a', "Freitext", Repeatability.NON_REPEATABLE, ""));
    return MX_TAG;
  }

  GNDTextTag(
    final String pica3,
    final String picaPlus,
    final String german,
    final Repeatability repeatability,
    final String marc,
    final String english) {
    super(pica3, picaPlus, german, repeatability, marc, english);
  }

  public GNDTextTag(
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
  public TextLineFactory getLineFactory() {
    return new TextLineFactory(this);
  }

}
