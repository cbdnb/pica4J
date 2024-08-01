package de.dnb.gnd.parser.tag;

/**
 * Wenn nur die Rohdaten wirklich wichtig sind.
 *
 * @author baumann
 *
 */
public final class EmptyTagDB extends TagDB {

  private static EmptyTagDB tagDB = new EmptyTagDB();

  public static EmptyTagDB getDB() {
    return tagDB;
  }

  private EmptyTagDB() {
  }

}
