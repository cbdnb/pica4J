package de.dnb.gnd.parser;

import java.io.Serializable;
import java.util.Comparator;

public class Pica3Comparator implements Comparator<String>, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -8028180976789385305L;

  public Pica3Comparator() {
  }

  /*
   * Damit Tags, die zu SWW gehören (380 ..) vor solchen einsortieren,
   * die zu Titeldaten gehören (1000 ...).
   */
  @Override
  public final int compare(final String o1, final String o2) {
    //		int a1 = Integer.parseInt(o1);
    //		int a2 = Integer.parseInt(o2);
    //		return a1 - a2;
    return o1.compareTo(o2);
  }
}
