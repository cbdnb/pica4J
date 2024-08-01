package de.dnb.gnd.utils;

import java.util.function.Predicate;

import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.TagDB;

public class ExpansionFilter implements Predicate<Subfield> {

  @Override
  public final boolean test(final Subfield subfield) {
    RangeCheckUtils.assertReferenceParamNotNull("subfield", subfield);
    return subfield.getIndicator() != TagDB.DOLLAR_8;
  }

}
