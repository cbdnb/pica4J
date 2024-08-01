package de.dnb.gnd.parser.line;

import java.util.HashMap;
import java.util.Map;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.tag.GNDTag;
import de.dnb.gnd.parser.tag.GNDTextTag;

/**
 * Für Mailboxen oder ähnliches, in denen auch Dollarzeichen vorkommen
 * können.
 *
 * @author baumann
 *
 */
public class TextLineFactory extends GNDLineFactory {

  public TextLineFactory(final GNDTextTag gndTag) {
    super(gndTag);
  }

  @Override
  public final DefaultGNDLine createLine() {
    return new DefaultGNDLine((GNDTag) getTag(), subfieldList);
  }

  /**
   * content liegt in der Form $i...$j... vor.
   *
   * @throws IllFormattedLineException
   * 					Wenn der erste Indikator unbekannt ist oder ein
   * 					Unterfeld anderweitig nicht konstuiert werden kann.
   */
  @Override
  protected final void splitInSubfields() throws IllFormattedLineException {
    final int length = contentStr.length();
    int splitPos = 0;
    /*
     * Zählt die verbrauchten Indikatoren. Bei schon benutzten
     * Indikatoren darf nicht mehr aufgespalten werden.
     */
    final Map<Character, Boolean> indicatorsUsed = new HashMap<>();
    for (final Character indChar : splittingIndicators.keySet()) {
      indicatorsUsed.put(indChar, false);
    }
    char nextIndChar = contentStr.charAt(1);
    Indicator ind = splittingIndicators.get(nextIndChar);
    if (ind == null)
      throw new IllFormattedLineException("Erster Indikator unbekannt");
    indicatorsUsed.put(nextIndChar, true);
    int nextPos = contentStr.indexOf(subfieldSeparator, splitPos + 2);

    while (nextPos != -1 && nextPos < length - 1) {
      nextIndChar = contentStr.charAt(nextPos + 1);
      final Indicator nextInd = splittingIndicators.get(nextIndChar);

      if (nextInd != null && !indicatorsUsed.get(nextIndChar)) {
        final String subContent = contentStr.substring(splitPos + 2, nextPos);
        createSubfieldAndAddToList(ind, subContent);
        indicatorsUsed.put(nextIndChar, true);
        ind = nextInd;
        splitPos = nextPos;
      }
      // weitersuchen:
      nextPos = contentStr.indexOf(subfieldSeparator, nextPos + 2);
    }
    // letztes Stückchen auch noch mitnehmen:
    final String subContent = contentStr.substring(splitPos + 2);
    createSubfieldAndAddToList(ind, subContent);
  }

  /**
   * @param args
   * @throws IllFormattedLineException
   */
  public static void main(final String[] args) throws IllFormattedLineException {
    final TextLineFactory factory = new TextLineFactory(GNDTextTag.getMxTag());
    factory.load("$z2012-12-18" + "$ba-DE-101-SE-F-hfm e-DE-603"
      + "$aBitte Feld 548 einleiten mit Unterfeld $b. "
      + "Datensatz wahrscheinlich dublett zu nid 1027953158");
    final Line line = factory.createLine();
    System.out.println(line);

  }

}
