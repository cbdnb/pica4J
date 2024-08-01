package de.dnb.gnd.parser.line;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.BibliographicTag;
import de.dnb.gnd.parser.tag.FixOrderTag;
import de.dnb.gnd.utils.SubfieldUtils;

public class BibLineFactory extends LineFactory {

  public BibLineFactory(final BibliographicTag aTag) {
    super(aTag);
  }

  protected Map<Character, Indicator> actualIndicators;

  @Override
  protected void splitInSubfields() throws IllFormattedLineException {
    final BibliographicTag bibTag = (BibliographicTag) tag;
    Collection<Subfield> subfields1;
    Collection<Subfield> subfields2;
    if (format == Format.PICA_PLUS) {
      subfields1 = ParseUtils.picaPlusSplitInSubfields(contentStr, bibTag.get1stIndicatorMap());
      subfields2 = ParseUtils.picaPlusSplitInSubfields(contentStr, bibTag.get2ndIndicatorMap());

    } else {
      // Pica3
      final Set<Indicator> indicators1 = bibTag.get1stIndicators();
      final Set<Indicator> indicators2 = bibTag.get2ndIndicators();
      subfields1 = getSubfields(indicators1);
      subfields2 = getSubfields(indicators2);
    }
    if (subfields1 == null || subfields1.isEmpty()) {
      if (subfields2 == null || subfields2.isEmpty()) {
        throw new IllFormattedLineException("Kann " + contentStr + " nicht parsen, ");
      } else {
        load(subfields2);
      }
    } else {
      if (subfields2 == null || subfields2.isEmpty()) {
        load(subfields1);
      } else {
        // Beide Listen nicht leer.
        //* Komplizierter. Zunächst das mit Link bevorzugen:
        if (SubfieldUtils.containsIndicatorInSubfields('9', subfields1)) {
          load(subfields1);
          return;
        }
        if (SubfieldUtils.containsIndicatorInSubfields('9', subfields2)) {
          load(subfields2);
          return;
        }
        // also kein Link, bevorzuge das ohne "-ohne":
        if (!ParseUtils.containsOhne(subfields1)) {
          load(subfields1);
          return;
        }
        if (!ParseUtils.containsOhne(subfields2)) {
          load(subfields2);
          return;
        }
        // Bevorzuge das größere
        if (subfields1.size() > subfields2.size()) {
          load(subfields1);
          return;
        }
        if (subfields1.size() < subfields2.size()) {
          load(subfields2);
          return;
        }
        // Beide gleich groß, daher Gewichte betrachten:
        final int weight1 = BibParseUtils.getWeight(subfields1);
        final int weight2 = BibParseUtils.getWeight(subfields2);

        if (weight1 > weight2) {
          load(subfields1);
        } else if (weight1 < weight2) {
          load(subfields2);
        } else {
          // Letzte Lösung:
          load(subfields1);
        }
      }
    }
  }

  private Collection<Subfield> getSubfields(final Set<Indicator> indicators) {
    if (indicators.isEmpty())
      return null;
    if (BibParseUtils.mustBeParsedInFixOrder(indicators) || tag instanceof FixOrderTag) {
      return BibParseUtils.makeSubfieldsFixOrder(indicators, contentStr);
    } else {
      return BibParseUtils.makeSubfieldsVariableOrder(indicators, contentStr);
    }
  }

  @Override
  public Line createLine() {
    return new BibliographicLine((BibliographicTag) tag, subfieldList);
  }

  @Override
  protected void setSplittingIndicators() {
    splittingIndicators = ((BibliographicTag) tag).getIndicatorMap();
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final BibliographicTag aTag = (BibliographicTag) BibTagDB.getDB().findTag("5530");
    final BibLineFactory factory = new BibLineFactory(aTag);
    factory.load("Jahrhundert, 18. / Allg. Geschichte|16.5/XA-DE");
    System.out.println(factory.getSubfieldList());

  }

}
