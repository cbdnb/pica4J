/**
 * 
 */
package de.dnb.basics.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.marc4j.MarcException;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

import de.dnb.basics.Misc;
import de.dnb.basics.applicationComponents.strings.StringInputStream;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.marc.MarcUtils;

/**
 * @author baumann
 *
 */
public final class ISBNCounter {

  /*
   * MARC-XML. Suche nach Feld 949 ($F enthält die besitzende Bibliothek)
   */
  static final String SWISS_BIB =
    "http://sru.swissbib.ch/sru/search/defaultdb" + "?" + "query=+dc.identifier+%3D+" + "<isbn>"
      + "&operation=searchRetrieve" + "&recordSchema=info%3Asrw%2Fschema%2F1%2Fmarcxml-v1.1-ligh"
      + "t&maximumRecords=10&startRecord=0&recordPacking=XML" + "&availableDBs=defaultdb";

  /*
   * Pica+-XML. Suche nach: <datafield tag="101@">
   */
  static final String SWB = "http://swb2.bsz-bw.de/sru/DB=2.1/username=/password=/" + "?"
    + "query=pica.isb+%3D+%22<isbn>%22" + "&version=1.1" + "&operation=searchRetrieve"
    + "&stylesheet=http%3A%2F%2Fswb2.bsz-bw.de%2Fsru%2FDB%3D2.1%2F%3Fxsl%3DsearchRetrieveResponse"
    + "&recordSchema=pica&maximumRecords=10" + "&startRecord=1" + "&recordPacking=xml"
    + "&sortKeys=none" + "&x-info-5-mg-requestGroupings=none";

  /*
   * MARC-XML. Suche nach Feld 954 ($0 enthält die besitzende Bibliothek)
   */
  static final String GBV = "http://sru.gbv.de/gvk" + "?" + "version=1.1" + "&recordSchema=marcxml"
    + "&operation=searchRetrieve" + "&query=pica.isb+%3D+%22<isbn>%22" + "&maximumRecords=10";

  /*
   * MARC-XML. Besitzende Bibliotheken mit Sigel in Feld 049, Unterfelder $a
   * kodiert.
   */
  static final String BVB =
    "http://bvbr.bib-bvb.de:5661/bvb01sru" + "?" + "version=1.1" + "&recordSchema=marcxml"
      + "&operation=searchRetrieve" + "&query=marcxml.isbn=<isbn>" + "&maximumRecords=10";

  private static String makeQuery(String verbund, String isbn) {
    return verbund.replace("<isbn>", isbn);
  }

  /**
   * @param xml
   *            auch null
   * @return Zahl der Pica+-Felder 101@.
   */
  public static int getSWBcount(String xml) {
    if (xml == null)
      return 0;
    String subString = "<datafield tag=\"101@\">";
    return StringUtils.countMatches(xml, subString);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("ISBN kopieren und Enter drücken");
    StringUtils.readConsole();
    String isb = StringUtils.readClipboard();
    System.out.println(StringUtils.concatenate("\t", "Summe", "BVB", "CH", "GBV", "SWB"));
    List<Integer> counts = getTotalCount(isb);
    String c = StringUtils.concatenate("\t", counts);
    System.out.println(c);
  }

  /**
   * Zählt alle Exemplare der entsprechenden Verbünde.
   * 
   * @param isbn
   *            nicht null
   * @return (#alle, #BVB, #CH, #GBV, #SWB). Wenn die Anfrage nicht
   *         beantwortet werden konnte, steht an der betreffenden Stelle eine
   *         0
   */
  public static List<Integer> getTotalCount(String isbn) {
    isbn = isbn.trim();
    String query;
    String xml;

    int countBVB = 0;
    int countCH = 0;
    int countGBV = 0;
    int countSWB = 0;

    query = makeQuery(BVB, isbn);
    xml = Misc.getWebsite(query);
    countBVB = getBVBcount(xml);

    query = makeQuery(SWISS_BIB, isbn);
    xml = Misc.getWebsite(query);
    countCH = getCHcount(xml);

    query = makeQuery(GBV, isbn);
    xml = Misc.getWebsite(query);
    countGBV = getGBVcount(xml);

    query = makeQuery(SWB, isbn);
    xml = Misc.getWebsite(query);
    countSWB = getSWBcount(xml);

    int total = countBVB + countCH + countGBV + countSWB;
    return Arrays.asList(total, countBVB, countCH, countGBV, countSWB);
  }

  /**
   * @param xml
   *            auch null
   * @return Zahl der 949-Felder. Eine MarcException wird akzeptiert
   */
  private static int getCHcount(String xml) {
    if (xml == null)
      return 0;
    int count = 0;
    InputStream input = new StringInputStream(xml);

    try {
      MarcXmlReader reader = new MarcXmlReader(input);
      while (reader.hasNext()) {
        Record record = reader.next();
        count += MarcUtils.getFieldCount(record, "949");
      }
    } catch (MarcException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return count / 2;
  }

  /**
   * @param xml
   *            auch null
   * @return Zahl der 954-Felder. Eine MarcException wird akzeptiert
   */
  public static int getGBVcount(String xml) {
    if (xml == null)
      return 0;
    int count = 0;
    InputStream input = new StringInputStream(xml);

    try {
      MarcXmlReader reader = new MarcXmlReader(input);
      while (reader.hasNext()) {
        Record record = reader.next();
        count += MarcUtils.getFieldCount(record, "954");
      }
    } catch (MarcException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return count / 2;

  }

  /**
   * @param xml
   *            auch null
   * @return Zahl der 049-$a-Felder. Eine MarcException wird akzeptiert
   */
  public static int getBVBcount(String xml) {
    if (xml == null)
      return 0;
    int count = 0;
    InputStream input = new StringInputStream(xml);

    try {
      MarcXmlReader reader = new MarcXmlReader(input);
      while (reader.hasNext()) {
        Record record = reader.next();
        count += MarcUtils.getSubfieldCount(record, "049", 'a');
      }
    } catch (MarcException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return count / 2;
  }

}
