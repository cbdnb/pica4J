package de.dnb.gnd.utils;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.TagDB;

public abstract class ClipTransformer {

  public ClipTransformer() {
    super();
    tagDB = GNDTagDB.getDB();
    try {
      defaultSourceLine = LineParser.parseGND("670 Vorlage");
    } catch (final IllFormattedLineException e) {

      e.printStackTrace();
    }
  }

  private final TagDB tagDB;
  private Line defaultSourceLine;

  /**
   * Template-Funktion. Übernimmt Daten aus der Zwischenablage und schreibt
   * die geänderten Daten in diese.
   * 
   * Ruft transform(Record) auf.
   * Diese Methode soll eine Ausnahme werfen, wenn die Verarbeitung
   * nicht durchgeführt werden kann. Das führt zum Schreiben eines
   * leeren Strings in die Zwischenablage.
   * 
   * 
   * 
   */
  public final void transform() {

    String s;
    try {
      final Record recordOld = RecordUtils.readFromClip();
      if (recordOld == null) {
        throw new IllegalArgumentException();
      }

      RecordUtils.removeUnmodifiables(recordOld);
      // Wenn eventuell die falsche Satzart genommen wurde ...
      if (RecordUtils.isNullOrEmpty(recordOld) || recordOld.tagDB != tagDB) {
        throw new IllegalArgumentException();
      }

      final Record recordNew = recordOld.clone();

      transform(recordNew);

      if (!GNDUtils.containsEntityTypes(recordNew))
        addEntityType(recordNew);
      if (!GNDUtils.containsSource(recordNew))
        addSource(recordNew);
      if (!GNDUtils.containsGNDClassification(recordNew))
        addGNDClassification(recordNew);
      if (!GNDUtils.containsCountryCode(recordNew))
        addCountryCode(recordNew);

      // Unveränderte Datensätze nicht übergeben:
      if (equals(recordOld, recordNew)) {
        throw new IllegalArgumentException();
      }

      s = RecordUtils.toPica(recordNew, Format.PICA3, false, null, '0');
    } catch (final Exception e) {
      s = "";
    }

    System.out.println(s);
    StringUtils.writeToClipboard(s);
  }

  protected abstract void addCountryCode(Record recordNew);

  /**
   * Default-Methode. Tut nichts, da nicht immer ein Ländercode nötig ist.
   * @param recordNew	beliebig
   */
  protected void defaultAddCountryCode(final Record recordNew) {
    // nix
  }

  protected abstract void addGNDClassification(Record recordNew);

  protected void addSource(final Record record) {
    try {
      record.add(defaultSourceLine);
    } catch (final OperationNotSupportedException e) {
      // nix
    }
  }

  protected abstract void addEntityType(Record record);

  /**
   * Diese Methode macht die eigentliche Arbeit. Ein Abbruch soll über
   * das Werfen einer Ausnahme signalisiert werden.
   * 
   * @param record		beliebig, ist ein Clone und kann
   * 						nach Erfordernissen verändert werden.
   * @throws Exception	wenn abgebrochen werden soll/muss.
   */
  protected abstract void transform(Record record) throws Exception;

  /**
   * Irgendeine Gleichheit beider Datensätze. Findet keine substanzielle 
   * Änderung statt, wird in der aufrufenden Template-Funktion die
   * Verarbeitung abgebrochen.
   * 
   * Diese Methode kann überschrieben werden  (könnte z.B. gewisse
   * Kommentare ignorieren).
   * 
   * @param 	recordOld	nicht null
   * @param 	recordNew	beliebig
   * @return	true, wenn als gleich angesehen.
   */
  protected boolean equals(final Record recordOld, final Record recordNew) {
    return recordOld.equals(recordNew);
  }
}
