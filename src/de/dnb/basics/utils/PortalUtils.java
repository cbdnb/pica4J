package de.dnb.basics.utils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import de.dnb.basics.Misc;
import de.dnb.basics.applicationComponents.StreamUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

public final class PortalUtils {

  private PortalUtils() {
  }

  public static final String DNB_URI = "https://d-nb.info/(gnd/)?([-X0123456789]+)";
  public static final Pattern DNB_URI_PAT = Pattern.compile(DNB_URI);

  public static final String ZDB_URI = "http://ld\\.zdb-services\\.de/data/.+\\.plus-1\\.mrcx";
  public static final Pattern ZDB_URI_PAT = Pattern.compile(ZDB_URI);

  /**
   * Liefert den Inhalt des Portaleintrags zur idn. Dabei ist es
   * erfreulicherweise egal, ob Titel- oder Normdatensatz, ob idn oder nid.
   *
   * @param idn	nicht null, nicht leer
   * @return		null, wenn nichts gefunden wurde
   */
  public static String getRecordViaPortal(final String idn) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("idn", idn);
    final String uri = getPortalUriString(idn);
    return Misc.getWebsite(uri);
  }

  /**
   * @param idn   nicht null
   * @return      die URI des Portaleintrags zur idn als String.
   *              Dabei ist es erfreulicherweise egal, ob Titel- oder
   *              Normdatensatz, ob idn oder nid.
   */
  public static String getPortalUriString(final String idn) {
    return "https://portal.dnb.de/opac.htm?" + "method=simpleSearch" + "&cqlMode=true"
      + "&query=idn%3D" + idn;
  }

  /**
   * @param idn   nicht null
   * @return      die URI des Portaleintrags zur idn oder null. Dabei ist es
   *              erfreulicherweise egal, ob Titel- oder Normdatensatz,
   *              ob idn oder nid.
   */
  public static URI getPortalUri(final String idn) {
    try {
      return new URI(getPortalUriString(idn));
    } catch (final URISyntaxException e) {
      return null;
    }
  }

  /**
   * Liefert den Datensatz zu idn im RDF-Turtle-Format.
   *
   * @param idn	nicht null, nicht leer, keine nid
   * @return		null, wenn nichts gefunden wurde
   */
  public static String getDNBturtle(final String idn) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("idn", idn);
    final String uri = "https://d-nb.info/" + idn + "/about/lds";
    return Misc.getWebsite(uri);
  }

  /**
   * Liefert den Datensatz zu idn im RDF-XML-Format.
   *
   * @param idn   nicht null, nicht leer, keine nid
   * @return      null, wenn nichts gefunden wurde
   */
  public static String getDNBrdf(final String idn) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("idn", idn);
    final String uri = "https://d-nb.info/" + idn + "/about/lds.rdf";
    return Misc.getWebsite(uri);
  }

  /**
   * Liefert die MARC-21-XML-Darstellung der idn. Dabei ist es
   * erfreulicherweise egal, ob Titel- oder Normdatensatz, ob idn oder nid.
   *
   * @param idn	nicht null, nicht leer
   * @return		null, wenn nichts gefunden wurde
   */
  public static String getMARC21xml(final String idn) {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("idn", idn);
    String uri = "https://d-nb.info/" + idn + "/about/marcxml";
    // https://d-nb.info/990273903/about/marcxml
    // https://d-nb.info/990273903/about/marcxml
    String website = Misc.getWebsite(uri);
    if (website != null)
      return website;

    uri = "https://d-nb.info/gnd/" + idn + "/about/marcxml";
    website = Misc.getWebsite(uri);
    return website;
  }

  /**
   *
   * @param url auch null
   * @return  Record oder null
   */
  public static Record getMarcRecord(final URL url) {
    if (url == null)
      return null;

    MarcXmlReader reader;
    InputStream urlStream = null;
    try {
      final URLConnection conn = Misc.openConnection(url, 4);
      urlStream = conn.getInputStream();

      reader = new MarcXmlReader(urlStream);
      if (reader.hasNext()) {
        final Record record = reader.next();
        return record;
      }
    } catch (final Exception e) {
      // nix
    } finally {
      reader = null;
      StreamUtils.safeClose(urlStream);
    }
    return null;
  }

  /**
   * Liefert einen Marc-Record (marc4j) zu einer idn über das Portal.
   *
   * @param idn 	beliebig, auch nid
   * @return		Gültigen Record oder null
   */
  public static Record getMarcRecord(final String idn) {

    String dnbURI = "https://d-nb.info/" + idn + "/about/marcxml";
    URL url = null;
    try {
      url = new URL(dnbURI);
    } catch (final MalformedURLException e) {
      // Kann nicht sein
    }
    Record record = getMarcRecord(url);
    if (record != null)
      return record;
    // Versuch GND:
    dnbURI = "http://d-nb.info/gnd/" + idn + "/about/marcxml";
    try {
      url = new URL(dnbURI);
    } catch (final MalformedURLException e) {
      // Kann nicht sein
    }
    record = getMarcRecord(url);
    if (record != null)
      return record;

    // neuer Versuch
    final String zdbURI = getZDBuri(idn);
    if (zdbURI == null)
      return null;
    try {
      url = new URL(zdbURI);
    } catch (final MalformedURLException e) {
      // Kann nicht sein
    }
    record = getMarcRecord(url);
    return record;
  }

  /**
   *
   * @param idn beliebig
   * @return  uri des Datensatzes in der ZDB oder null
   */
  public static String getZDBuri(final String idn) {
    final String suche = "https://zdb-katalog.de/title.xhtml?idn=" + idn;
    final String zdbWebsite = Misc.getWebsite(suche);
    if (zdbWebsite == null)
      return null;
    final Matcher m = PortalUtils.ZDB_URI_PAT.matcher(zdbWebsite);
    if (m.find())
      return m.group();
    return null;
  }

  /**
   * Findet die IDN. Das ist bei Titeldaten die eingegebene Idn,
   * bei Normdaten kann sie abweichen (nid).
   *
   * @param idn	nicht null.
   * @return			null, wenn keine Verbindung zum Portal aufgebaut
   * 					werden konnte oder wenn keine gültige idn
   * 					vorlag.
   */
  public static String getDNBidn(final String idn) {
    RangeCheckUtils.assertReferenceParamNotNull("idn", idn);
    final String rec = getRecordViaPortal(idn);
    final Matcher m = PortalUtils.DNB_URI_PAT.matcher(rec);
    if (m.find())
      return m.group(2);
    return null;
  }

  /**
   * Findet die URI.
   *
   * @param idn	nicht null.
   * @return			null, wenn keine Verbindung zum Portal aufgebaut
   * 					werden konnte oder wenn keine gültige idn
   * 					vorlag.
   */
  public static String getDNBuri(final String idn) {
    RangeCheckUtils.assertReferenceParamNotNull("idn", idn);
    // Schnellere Methode, funktioniert nicht immer:
    final Record record = getMarcRecord(idn);
    if (record != null) {
      final DataField field = (DataField) record.getVariableField("024");
      if (field != null) {
        final Subfield subf = field.getSubfield('a');
        final String data = subf.getData();
        if (data.contains("http://"))
          return data;
      }
    }
    // langsame und sichere Methode:
    final String rec = getRecordViaPortal(idn);
    final Matcher m = PortalUtils.DNB_URI_PAT.matcher(rec);
    if (m.find())
      return m.group();
    return null;
  }

  public static void main(final String[] args) {
    final String idn = "957561504";
    System.out.println(getDNBidn(idn));
  }

}
