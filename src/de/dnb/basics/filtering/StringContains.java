package de.dnb.basics.filtering;

import java.util.function.Predicate;

import de.dnb.basics.Constants;

/**
 * Filterkriterium ENTHÄLT basierend auf String.contains().
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 */
public class StringContains implements Predicate<String> {
  private final String necessarySubstring;

  public StringContains(final String necessarySubstring) {
    RangeCheckUtils.assertReferenceParamNotNull("necessarySubstring", necessarySubstring);

    this.necessarySubstring = necessarySubstring;
  }

  @Override
  public final boolean test(final String object) {
    return object.contains(necessarySubstring);
  }

  /**
   *
   * @param indicator beliebig
   * @param prefix    auch null
   * @return          Sucht auch in der Mitte nach Unterfeldern
   */
  public static StringContains containsSubfield(final char indicator, String prefix) {
    if (prefix == null || prefix.isEmpty())
      prefix = "";
    final String nessesarySubstring = Constants.US + indicator + prefix;
    return new StringContains(nessesarySubstring);
  }
}
