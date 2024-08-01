package de.dnb.gnd.parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineFactory;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;

/**
 * Parst im Augenblick sowohl Normdaten (ziemlich vollständig) und
 * Titeldaten (rudimentär).
 *
 * @author baumann
 *
 */
public class MarcParser {

  // Konstanten --------------------------

  public final String DNB_PREFIX = "(DE-101)";

  public final String GND_PREFIX = "(DE-588)";

  public final String URI = "(uri)";

  public final char NON_SORTING_BEGIN = 0x98;

  public final char NON_SORTING_END = 0x9C;

  public final String CA = "ca. ";

  // Pica-Daten ---------------------------

  protected TagDB db = GNDTagDB.getDB();

  protected Record picaRecord;

  protected Tag actualTag;

  protected List<de.dnb.gnd.parser.Subfield> picaSubfields;

  protected char picaChar;

  protected Map<Character, Indicator> splittingIndicators;

  protected Tag tag100;

  /**
   *  enthält ein $9
   */
  protected boolean isRelated;

  /**
   *  Enthält die relationierte idn ($9 + idn).
   */
  protected de.dnb.gnd.parser.Subfield linkSubfield;

  /**
   * nur für Normdaten
   */
  protected String ddcTable;

  // Marc-Daten ---------------------------

  protected org.marc4j.marc.Record marcRecord;

  protected DataField actualDataField;

  protected String actualMarcSubfieldContent;

  /**
   * Bei Marc als 'Code' bezeichnet.
   */
  protected char actualMarcSubfieldIndicator;

  protected char indicator1;

  protected char indicator2;

  private ControlField actualControlfield;

  /**
   *
   * @param marcRecord nicht null
   * @return	neuen Pica-Datensatz
   */
  public final Record parse(final org.marc4j.marc.Record marcRecord) {
    RangeCheckUtils.assertReferenceParamNotNull("marcRecord", marcRecord);
    setRecordType(marcRecord);
    this.marcRecord = marcRecord;
    final String idn = marcRecord.getControlNumber();
    picaRecord = new Record(idn, db);
    final List<VariableField> variableFields = marcRecord.getVariableFields();

    for (final VariableField variableField : variableFields) {
      if (variableField instanceof DataField) {
        actualDataField = (DataField) variableField;
        processDatafield();
      }
      if (variableField instanceof ControlField) {
        actualControlfield = (ControlField) variableField;
        processControlField();
      }
    }
    return picaRecord;
  }

  private void processControlField() {
    actualMarcTag = actualControlfield.getTag();
    final StringBuffer bufferDate = new StringBuffer("$0");
    final String data = actualControlfield.getData();
    switch (actualMarcTag) {
    case "005":
      actualTag = db.findTag("001B");
      // Tag
      bufferDate.append(StringUtils.charAt(data, 6));
      bufferDate.append(StringUtils.charAt(data, 7));
      bufferDate.append('-');
      // Monat
      bufferDate.append(StringUtils.charAt(data, 4));
      bufferDate.append(StringUtils.charAt(data, 5));
      bufferDate.append('-');
      // Jahr
      bufferDate.append(StringUtils.charAt(data, 2));
      bufferDate.append(StringUtils.charAt(data, 3));
      // Stunden
      bufferDate.append("$t");
      bufferDate.append(StringUtils.charAt(data, 8));
      bufferDate.append(StringUtils.charAt(data, 9));
      bufferDate.append(':');
      // Minusten
      bufferDate.append(StringUtils.charAt(data, 10));
      bufferDate.append(StringUtils.charAt(data, 11));
      bufferDate.append(':');
      // Sekunden
      bufferDate.append(StringUtils.charAt(data, 12));
      bufferDate.append(StringUtils.charAt(data, 13));
      bufferDate.append(StringUtils.charAt(data, 14));
      bufferDate.append(StringUtils.charAt(data, 15));
      bufferDate.append("00");

      break;
    case "008":
      actualTag = db.findTag("001A");
      // Tag
      bufferDate.append(StringUtils.charAt(data, 4));
      bufferDate.append(StringUtils.charAt(data, 5));
      bufferDate.append('-');
      // Monat
      bufferDate.append(StringUtils.charAt(data, 2));
      bufferDate.append(StringUtils.charAt(data, 3));
      bufferDate.append('-');
      // Jahr
      bufferDate.append(StringUtils.charAt(data, 0));
      bufferDate.append(StringUtils.charAt(data, 1));

      break;
    default:
      return;
    }
    final LineFactory factory = actualTag.getLineFactory();
    try {
      factory.load(bufferDate.toString());
      final Line line = factory.createLine();
      picaRecord.add(line);
    } catch (IllFormattedLineException | OperationNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void setRecordType(final org.marc4j.marc.Record marcRecord) {
    final Leader leader = marcRecord.getLeader();
    typeOfMarcRecord = leader.getTypeOfRecord();
    switch (typeOfMarcRecord) {
    case 'z':
      db = GNDTagDB.getDB();
      break;
    case 'a':
      db = BibTagDB.getDB();
      break;
    default:
      throw new UnsupportedOperationException("Datensatztyp unbekannt");
    }

  }

  protected void processDatafield() {
    // Globale Variable initialisieren:
    linkSubfield = null;
    ddcTable = "";
    actualMarcTag = actualDataField.getTag();
    indicator1 = actualDataField.getIndicator1();
    indicator2 = actualDataField.getIndicator2();

    // für Normdaten:
    if (typeOfMarcRecord == 'z') {
      switch (actualMarcTag) {
      case "035":
        process035();
        return;
      case "024":
        process024();
        return;

      case "079":
        process079();
        return;
      case "040":
        process040();
        return;
      }

    } else if (typeOfMarcRecord == 'a') {
      switch (actualMarcTag) {
      case "084":
        processSGG();
        return;
      }
    }

    actualTag = db.findMARCTag(actualMarcTag, indicator1, indicator2);
    if (actualTag == null)
      return;
    picaSubfields = new LinkedList<>();
    if (containsLink())
      isRelated = true;
    else
      isRelated = false;
    //		splittingIndicators = actualTag.getIndicatorMap(!isRelated);
    splittingIndicators = actualTag.getIndicatorMap(true);

    final List<org.marc4j.marc.Subfield> marcSubfields = actualDataField.getSubfields();
    for (final org.marc4j.marc.Subfield marcSubfield : marcSubfields) {

      actualMarcSubfieldIndicator = marcSubfield.getCode();
      actualMarcSubfieldContent = marcSubfield.getData();
      if (actualMarcSubfieldContent == null || actualMarcSubfieldContent.isEmpty())
        continue;
      // Wenn ein Marc-Unterfeld eine Sonderbehandlung (z.B. Splitten)
      // benötigt:
      if (actualMarcTag.equals("548") && actualMarcSubfieldIndicator == 'a') {
        processMarc548DollarA();
        continue;
      }
      if ((actualMarcTag.equals("100") || actualMarcTag.equals("400")
        || actualMarcTag.equals("700")) && actualMarcSubfieldIndicator == 'a') {
        processMarcX00DollarA();
        continue;
      }

      // Umwidmen von Indikatoren:
      picaChar = 0;
      /* $t kommt nur bei Werken vor, dann muss aber der Tag ausgetauscht
       * werden (100 -> 130). Ausserdem ist die bisherige Liste fast
       * nutzlos.
       */
      if (actualMarcSubfieldIndicator == 't'
        && searchPicaIndicator(actualMarcSubfieldIndicator) != 't') {
        picaChar = 'a';
        StringBuffer newMarcTag = new StringBuffer(actualMarcTag);
        // 100 -> 130 etc.:
        newMarcTag = newMarcTag.replace(1, 2, "3");
        actualTag = db.findTag(newMarcTag.toString());
        splittingIndicators = actualTag.getIndicatorMap(true);
        picaSubfields.clear();
        // erkannten Link retten:
        if (isRelated && linkSubfield != null)
          picaSubfields.add(linkSubfield);
      }
      // $a -> $8, wenn relationiert
      //			else if (actualMarcSubfieldIndicator == 'a' && isRelated)
      //				picaChar = '8';
      // $0 -> $9
      else if (actualMarcSubfieldIndicator == '0' && splittingIndicators.get('9') != null) {
        if (actualMarcSubfieldContent.startsWith(DNB_PREFIX)) {
          picaChar = '9';
          actualMarcSubfieldContent = actualMarcSubfieldContent.substring(DNB_PREFIX.length());
        } else
          picaChar = '!'; // ungültig
      }
      // $0(uri) -> $u
      else if (actualMarcSubfieldIndicator == '0' && actualMarcSubfieldContent.startsWith(URI)) {
        picaChar = 'u';
        actualMarcSubfieldContent = actualMarcSubfieldContent.substring(URI.length());
      }
      // $9 -> $.
      else if (actualMarcSubfieldIndicator == '9') {
        processMarcDollar9();
      }
      // 083, 089 $z enthält Hilfstafel:
      else if (actualMarcSubfieldIndicator == 'z'
        && (actualMarcTag.equals("083") || actualMarcTag.equals("089"))) {
        ddcTable = "T" + actualMarcSubfieldContent + "--";
      }
      // DDC-Haupttafel steht bei uns in $c:
      else if (actualMarcSubfieldIndicator == 'a'
        && (actualMarcTag.equals("083") || actualMarcTag.equals("089"))) {
        actualMarcSubfieldContent = ddcTable + actualMarcSubfieldContent;
        picaChar = 'c';
      }
      // Pauschal suchen:
      else {
        picaChar = searchPicaIndicator(actualMarcSubfieldIndicator);
      }

      // Jetzt Unterfeld zusammenbauen:
      final Indicator indicator = splittingIndicators.get(picaChar);
      if (indicator == null)
        continue;
      try {
        // Sonderbehandlung für Nichtsortierzeichen am Anfang:
        // Muss entfernt und und Rest mit @ eingeleitet werden:
        String prefix = "";
        String rest = actualMarcSubfieldContent;
        if (actualMarcSubfieldContent.startsWith(NON_SORTING_BEGIN + "")) {
          final int end = actualMarcSubfieldContent.indexOf(NON_SORTING_END);
          if (end > 0) {
            prefix = actualMarcSubfieldContent.substring(1, end);
            rest = " @" + actualMarcSubfieldContent.substring(end + 2);
          }
        }
        final String content = StringUtils.cleanMarc(prefix + rest);

        final de.dnb.gnd.parser.Subfield subfield =
          new de.dnb.gnd.parser.Subfield(indicator, content);
        picaSubfields.add(subfield);
        if (indicator.indicatorChar == '9')
          linkSubfield = subfield;
      } catch (final IllFormattedLineException e) {
        e.printStackTrace();
      }
    }

    if (picaSubfields.isEmpty())
      return;

    final LineFactory factory = actualTag.getLineFactory();
    try {
      factory.load(picaSubfields);
    } catch (final IllFormattedLineException e) {
      //      e.printStackTrace();
    }
    final Line line = factory.createLine();
    try {
      picaRecord.add(line);
    } catch (final OperationNotSupportedException e) {
      // TODO Auto-generated catch block
      //      e.printStackTrace();
    }
  }

  private void processSGG() {
    final List<Subfield> marcSubs = actualDataField.getSubfields('a');
    if (marcSubs.isEmpty())
      return;
    String sgg = "5050 ";
    for (final Iterator<Subfield> iterator = marcSubs.iterator(); iterator.hasNext();) {
      final Subfield subfield = iterator.next();
      sgg += subfield.getData();
      if (iterator.hasNext())
        sgg += ';';
    }
    try {
      final Line lineSGG = LineParser.parse(sgg, db, false);
      picaRecord.add(lineSGG);
    } catch (IllFormattedLineException | OperationNotSupportedException e) {

    }

  }

  protected boolean containsLink() {
    final List<Subfield> subfields = actualDataField.getSubfields('0');
    if (subfields == null || subfields.isEmpty())
      return false;
    for (final Subfield subfield : subfields) {
      if (subfield.getData().contains(DNB_PREFIX))
        return true;
    }
    return false;
  }

  protected char typeOfMarcRecord;

  private String actualMarcTag;

  protected void processMarcX00DollarA() {
    tag100 = db.findMARCTag("100", (char) 0, (char) 0);
    // Bei Personen steht "von" etc. in Nichtsortierzeichen. Das
    // kommt in $c:
    final int begin = actualMarcSubfieldContent.indexOf(NON_SORTING_BEGIN);
    if (begin != -1) {
      final int end = actualMarcSubfieldContent.indexOf(NON_SORTING_END);
      if (end > begin) {
        final Indicator indC = tag100.getIndicator('c');
        final String prefix = actualMarcSubfieldContent.substring(begin + 1, end).trim();
        actualMarcSubfieldContent = actualMarcSubfieldContent.substring(0, begin - 1).trim();
        de.dnb.gnd.parser.Subfield subC;
        try {
          subC = new de.dnb.gnd.parser.Subfield(indC, prefix);
          picaSubfields.add(subC);
        } catch (final IllFormattedLineException e) {
          //
        }
      }
    }

    if (actualMarcSubfieldContent.contains(", ")) {
      final String[] strings = actualMarcSubfieldContent.split(", ", -1);
      final Indicator indA = tag100.getIndicator('a');
      final Indicator indD = tag100.getIndicator('d');
      de.dnb.gnd.parser.Subfield subA;
      de.dnb.gnd.parser.Subfield subD;
      try {
        subA = new de.dnb.gnd.parser.Subfield(indA, strings[0]);
        picaSubfields.add(subA);
        subD = new de.dnb.gnd.parser.Subfield(indD, strings[1]);
        picaSubfields.add(subD);
      } catch (final IllFormattedLineException e) {
        //
      }

    } else {
      final Indicator indP = tag100.getIndicator('P');
      try {
        final de.dnb.gnd.parser.Subfield subP =
          new de.dnb.gnd.parser.Subfield(indP, actualMarcSubfieldContent);
        picaSubfields.add(subP);
      } catch (final IllFormattedLineException e) {
        //
      }

    }

  }

  // bib unnötig
  protected void processMarc548DollarA() {
    // "ca." / "-":
    if (actualMarcSubfieldContent.startsWith(CA)) {
      try {
        final Indicator indD = actualTag.getIndicator('d');
        de.dnb.gnd.parser.Subfield subD;
        subD =
          new de.dnb.gnd.parser.Subfield(indD, actualMarcSubfieldContent.substring(CA.length()));
        picaSubfields.add(subD);
      } catch (final IllFormattedLineException e) {
        //
      }
    } else if (actualMarcSubfieldContent.contains("-")) {
      final String[] strings = actualMarcSubfieldContent.split("-", -1);
      final Indicator indA = actualTag.getIndicator('a');
      final Indicator indB = actualTag.getIndicator('b');
      de.dnb.gnd.parser.Subfield subA;
      de.dnb.gnd.parser.Subfield subB;
      try {
        if (!strings[0].isEmpty()) {
          subA = new de.dnb.gnd.parser.Subfield(indA, strings[0]);
          picaSubfields.add(subA);
        }
        if (!strings[1].isEmpty()) {
          subB = new de.dnb.gnd.parser.Subfield(indB, strings[1]);
          picaSubfields.add(subB);
        }
      } catch (final IllFormattedLineException e) {
        //
      }
    } else {
      try {
        final Indicator indC = actualTag.getIndicator('c');
        de.dnb.gnd.parser.Subfield subC;
        subC = new de.dnb.gnd.parser.Subfield(indC, actualMarcSubfieldContent);
        picaSubfields.add(subC);
      } catch (final IllFormattedLineException e) {
        //
      }
    }

  }

  // bib leer
  protected void process040() {
    final Subfield subfieldA = actualDataField.getSubfield('a');
    if (subfieldA != null) {
      final String s = "903 $e" + subfieldA.getData();
      try {
        final Line line = LineParser.parseGND(s);
        picaRecord.add(line);
      } catch (IllFormattedLineException | OperationNotSupportedException e) {
        //
      }
    }

    final Subfield subfieldR = actualDataField.getSubfield('9');
    if (subfieldR != null) {
      final String s = "903 $r" + subfieldR.getData().substring(2);
      try {
        final Line line = LineParser.parseGND(s);
        picaRecord.add(line);
      } catch (IllFormattedLineException | OperationNotSupportedException e) {
        //
      }
    }

  }

  protected void processMarcDollar9() {
    if (StringUtils.charAt(actualMarcSubfieldContent, 1) == ':') {
      picaChar = StringUtils.charAt(actualMarcSubfieldContent, 0);
      actualMarcSubfieldContent = actualMarcSubfieldContent.substring(2);
    }
  }

  // bib leer
  protected void process024() {
    final Subfield subfield2 = actualDataField.getSubfield('2');
    if (subfield2 != null && subfield2.getData().startsWith("uri")) {
      final Subfield subfieldA = actualDataField.getSubfield('a');
      try {
        final Line line006 = LineParser.parseGND("006 " + subfieldA.getData());
        picaRecord.add(line006);
      } catch (IllFormattedLineException | OperationNotSupportedException e) {
        //
      }
    }
  }

  // bib anders
  protected void process035() {
    final Subfield subfieldA = actualDataField.getSubfield('a');
    if (subfieldA != null) {
      String content = subfieldA.getData();
      if (!content.startsWith(GND_PREFIX))
        return;
      content = content.substring(GND_PREFIX.length());
      final String field035 = "035 gnd/" + content;
      try {
        final Line line035 = LineParser.parseGND(field035);
        picaRecord.add(line035);
      } catch (IllFormattedLineException | OperationNotSupportedException e) {
        //
      }
    }
  }

  // nicht in bib
  protected void process079() {
    String gnd005 = "005 T";
    final Subfield subfieldB = actualDataField.getSubfield('b');
    final String type = subfieldB == null ? "T" : subfieldB.getData();
    final Subfield subfieldC = actualDataField.getSubfield('c');
    final String level = subfieldC == null ? "1" : subfieldC.getData();
    gnd005 += type + level;
    final ControlField field008 = (ControlField) marcRecord.getVariableField("008");
    if (field008 != null) {
      final String data008 = field008.getData();
      if (StringUtils.charAt(data008, 9) == 'b')
        gnd005 += 'e';
    }

    try {
      final Line line005 = LineParser.parseGND(gnd005);
      picaRecord.add(line005);
    } catch (IllFormattedLineException | OperationNotSupportedException e1) {
      //nix
    }

    enumerateSubfieldsAndAddLine("008", 'v');
    enumerateSubfieldsAndAddLine("011", 'q');
    enumerateSubfieldsAndAddLine("012", 'u');
  }

  protected void enumerateSubfieldsAndAddLine(String picaTag, final char ind) {
    final List<org.marc4j.marc.Subfield> subfields = actualDataField.getSubfields(ind);
    if (!subfields.isEmpty()) {
      picaTag += " ";
      for (final Iterator<Subfield> iterator = subfields.iterator(); iterator.hasNext();) {
        final Subfield subfield = iterator.next();
        picaTag += subfield.getData();
        if (iterator.hasNext())
          picaTag += ';';
      }
      try {
        final Line gndLine = LineParser.parseGND(picaTag);
        picaRecord.add(gndLine);
      } catch (IllFormattedLineException | OperationNotSupportedException e) {
        // nix
      }
    }
  }

  protected char searchPicaIndicator(final char marcIndicator) {
    final Collection<Indicator> indicators = splittingIndicators.values();
    for (final Indicator indicator : indicators) {
      if (indicator.marcIndicator == marcIndicator) {
        return indicator.indicatorChar;
      }
    }
    return 0;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final String idn = StringUtils.readClipboard();
    final org.marc4j.marc.Record marcRecord = PortalUtils.getMarcRecord(idn);
    System.out.println(marcRecord);
    final MarcParser parser = new MarcParser();
    final Record record = parser.parse(marcRecord);
    System.out.println(record);

  }

}
