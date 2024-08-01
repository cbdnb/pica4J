package de.dnb.gnd.parser.line;

import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.BibliographicTag;
import de.dnb.gnd.parser.tag.HoldingsTag;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;

public class HoldingsFactory extends BibLineFactory {

  BibTagDB db;

  public HoldingsFactory(final BibliographicTag aTag) {
    super(aTag);
  }

  @Override
  public Line createLine() throws IllegalArgumentException {
    // verzögerte Auswertung:
    if (db == null)
      db = BibTagDB.getDB();
    String dollarX = SubfieldUtils.getContentOfFirstSubfield(subfieldList, 'x');
    final String prefix = ((HoldingsTag) tag).getPrefix();
    if (prefix.length() == 3)
      dollarX = dollarX.substring(1);
    final String pica3Str = ((HoldingsTag) tag).getPrefix() + dollarX;
    final Tag pica3Tag = db.getPica3(pica3Str);
    return new BibliographicLine((BibliographicTag) pica3Tag, subfieldList);
  }

  public static void main(final String[] args) throws IllFormattedLineException {
    final Record record = BibRecUtils.readFromClip();

    System.out.println(record);
    System.out.println();
    System.out.println(RecordUtils.toPica(record));
  }

}
