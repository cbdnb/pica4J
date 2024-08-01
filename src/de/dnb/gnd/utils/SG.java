/**
 *
 */
package de.dnb.gnd.utils;

/**
 * @author baumann
 *
 */
public interface SG {

  /**
   * @return Die DDC-Sachgruppe der DNB (z.B. '530').
   */
  String getDDCString();

  /**
   * @return Die Beschreibung (z.B. 'Geschichte Europas')
   */
  String getDescription();
}
