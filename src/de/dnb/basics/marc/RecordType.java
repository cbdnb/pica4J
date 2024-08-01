/**
 *
 */
package de.dnb.basics.marc;

import java.text.ParseException;

import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;

/**
 * Die Typen der Marc21-Datensätze, die Leader an Position 06 codiert sind.
 *
 * @author baumann
 *
 */
public enum RecordType {

    AUTHORITY("Normdatensatz"), BIBLIOGRAPHIC("Titeldatensatz"), CLASSIFICATION("Klassifiaktion"),
    COMMUNITY_INFORMATION("COMMUNITY_INFORMATION"), HOLDINGS("Besitzer?");

  public final String german;

  /**
     * @param string
     */
  RecordType(final String string) {
    german = string;
  }

  /**
   * Gibt den Typ (Authority Data, Bibliographic Data, Classification Data,
   * Community Information, Holdings Data).
   *
   * @param record auch null
   * @return  Typ oder null
   */
  public static RecordType getType(final Record record) {
    if (record == null)
      return null;
    final Leader leader = record.getLeader();
    final char typeChar = leader.getTypeOfRecord();
    return getType(typeChar);
  }

  /**
   * @param leader
   * @return
   */
  public static RecordType getType(final char typeChar) {
    switch (typeChar) {
    case 'z':
      return AUTHORITY;
    case 'w':
      return CLASSIFICATION;
    case 'q':
      return COMMUNITY_INFORMATION;
    case 'u':
    case 'v':
    case 'x':
    case 'y':
      return HOLDINGS;
    default:
      return BIBLIOGRAPHIC;
    }
  }

  public static void main(final String[] args) throws ParseException {
    System.out.println(HOLDINGS.german);
  }

}
