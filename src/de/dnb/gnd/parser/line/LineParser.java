package de.dnb.gnd.parser.line;

import java.util.Collection;

import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.SWDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;

/**
 * Hilfsklasse, die das Parsen von Zeilen erleichtert.
 *
 * @author baumann
 *
 */
public final class LineParser {

  private LineParser() {
    super();
  }

  /**
   * Parst eine durch tag und contentStr gegebene Zeile.
   *
   * @param tag		nicht null.
   * @param format		pica3 oder pica+. Wird durch die Form von Tag
   * 						erkannt und erleichtert das Parsen.
   * @param contentStr	nicht null nicht leer.
   * @param ignoreMARC	Überlese beim Parsen von Pica+-Daten die Felder,
   * 						die in MARC21 üblich sind, die aber nicht zum
   * 						Pica-Format gehören.
   * @return				Eine Zeile.
   * @throws IllFormattedLineException
   * 						Wenn contentStr falsch formatiert.
   */
  public static
    Line
    parse(final Tag tag, final Format format, final String contentStr, final boolean ignoreMARC)
      throws IllFormattedLineException,
      IllegalArgumentException {
    final LineFactory factory = tag.getLineFactory();
    factory.load(format, contentStr, ignoreMARC);
    return factory.createLine();
  }

  /**
   * Wandelt eine durch tag und durch subfields gegebene Liste in eine
   * Zeile um.
   *
   * @param tag		nicht null.
   * @param subfields		nicht null, nicht leer.
   * @return				eine Zeile
   * @throws IllFormattedLineException
   * 						Wenn die subfields nicht zum Tag und zur
   * 						Wiederholbarkeit passen.
   */
  public static Line parse(final Tag tag, final Collection<Subfield> subfields)
    throws IllFormattedLineException {
    final LineFactory factory = tag.getLineFactory();
    factory.load(subfields);
    return factory.createLine();
  }

  /**
   * Hilfsmethode, die eine vollständige Zeile parst. Da der Tag als String
   * (Präfix der Zeile) vorliegt, muss noch die DB angegeben werden, in
   * der der Tag gesucht werden muss.
   *
   * @param lineStr		nicht null, nicht leer.
   * @param tagDB			nicht null.
   * @param ignoreMARC 	Überlese beim Parsen von Pica+-Daten die Felder,
   * 						die in MARC21 üblich sind, die aber nicht zum
   * 						Pica-Format gehören.
   * @return				gültige Zeile oder null, wenn keine mit
   * 						bekanntem Tag beginnende Zeile vorliegt.
   * @throws IllFormattedLineException
   * 						falsch formatierte Zeile.
   */
  public static Line parse(final String lineStr, final TagDB tagDB, final boolean ignoreMARC)
    throws IllFormattedLineException,
    IllegalArgumentException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("lineStr", lineStr);
    RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);
    final Triplett<Tag, String, String> triplett = tagDB.parseTag(lineStr);

    if (triplett != null) {
      final Tag tag = triplett.first;
      final String content = triplett.third;
      Format format;
      if (triplett.second.equals(tag.pica3)) {
        format = Format.PICA3;
      } else {
        format = Format.PICA_PLUS;
      }
      return parse(tag, format, content, ignoreMARC);
    }
    return null;
  }

  /**
   * Hilfsmethode, die eine vollständige GND-Zeile im Pica3-Format parst.
   *
   * @param lineStr		nicht null, nicht leer.
   *
   * @return				gültige Zeile oder null, wenn keine mit
   * 						bekanntem Tag beginnende Zeile vorliegt.
   * @throws IllFormattedLineException
   * 						falsch formatierte Zeile.
   */
  public static Line parseGND(final String lineStr) throws IllFormattedLineException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("lineStr", lineStr);
    return parse(lineStr, GNDTagDB.getDB(), false);
  }

  /**
   * @param args
   * @throws IllFormattedLineException
   */
  public static void main(final String[] args) throws IllFormattedLineException {

    final Line line = parse("809 |x|ja *erl", SWDTagDB.getDB(), false);
    System.out.println(line);

  }

  public static LineFactory getFactory(final String tagStr, final TagDB tagDB) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("tagStr", tagStr);
    RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);
    final Tag tag = tagDB.findTag(tagStr);
    return tag.getLineFactory();
  }

}
