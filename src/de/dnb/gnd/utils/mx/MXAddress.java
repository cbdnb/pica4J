/**
 *
 */
package de.dnb.gnd.utils.mx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.FilterUtils;

/**
 * Eine Mailboxadresse. Diese besteht aus folgenden Teilen:
 * <li>Bibliothek: {@link Library}
 * <li>Adressierungsart: {@link Adressierung}
 * <li>Redaktionstyp: {@link RedaktionsTyp}
 * <li>Untereinheiten: {@link #subadress}
 *
 *
 * @author baumann
 *
 */
public class MXAddress implements Comparable<MXAddress> {

  /**
   *
   * @return  Die Empfänger, die standardmäßig (per Skript) von der DNB aus
   *          angeschrieben werden. Ohne das Schwesterhaus der DNB, ohne "FLLB".
   */
  public static Collection<MXAddress> getStandardEmpfaenger() {
    final String mxStr = "e-DE-12-SE e-DE-384-SE e-DE-101-SE e-DE-601-SE e-DE-605-SE e-DE-603-SE "
      + "e-CH-SgKBLR e-CH-ChKBLR e-CH-AaAKBLR e-LI-VaLIL FLLB  e-DE-188-SE e-DE-255-SE "
      + "e-AT-NOELB e-AT-OBV e-DE-1-SE e-CH-000001-5 e-DE-576 e-CH-ZuSLS-SE";

    final List<String> list = Mailbox.getRawAddresses(mxStr);
    return FilterUtils.mapNullFiltered(list, MXAddress::parse);
  }

  /**
   * Zum Vergleichen, um null zu umgehen.
   */
  final static Library DUMMY_LIB = new Library("XXXX", "dummy", "zzz", "la");

  /**
   * Um null bei Vergleichen zu umgehen. Kann aber weiterverarbeitet werden.
   *
   * @return  Null-Adresse.
   */
  public static MXAddress getNullAddress() {
    return new MXAddress(Adressierung.UNBESTIMMT, false, Library.getNullLibrary(),
      RedaktionsTyp.DEFAULT, "", "");
  }

  /**
   * Vergleicht nach
   * <li>ISIL
   * <li>{@link RedaktionsTyp}
   * <li>Subadresse (F-ba)
   * <br><br>
   */
  @Override
  public int compareTo(final MXAddress o) {
    int comp;
    comp = adressierung.compareTo(o.adressierung);
    if (comp != 0)
      return comp;
    Library thisLibrary = library;
    if (thisLibrary == null)
      thisLibrary = DUMMY_LIB;
    Library otherLibrary = o.library;
    if (otherLibrary == null)
      otherLibrary = DUMMY_LIB;
    comp = thisLibrary.compareTo(otherLibrary);
    if (comp != 0)
      return comp;
    comp = redaktionsTyp.compareTo(o.redaktionsTyp);
    if (comp != 0)
      return comp;
    return subadress.compareToIgnoreCase(o.subadress);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((library == null) ? 0 : library.hashCode());
    result = prime * result + ((redaktionsTyp == null) ? 0 : redaktionsTyp.hashCode());
    result = prime * result + ((subadress == null) ? 0 : subadress.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final MXAddress other = (MXAddress) obj;
    if (library == null) {
      if (other.library != null)
        return false;
    } else if (!library.equals(other.library))
      return false;
    if (redaktionsTyp != other.redaktionsTyp)
      return false;
    if (subadress == null) {
      if (other.subadress != null)
        return false;
    } else if (!subadress.equals(other.subadress))
      return false;
    return true;
  }

  /**
   * Enum der möglichen Fälle der Adressierung in z.B. <br><br>
   *
   * a-DE-188-SE e-DE-12-SE <br><br>
   *
   * e = Empfänger im Feld 901 /a = Absender im Feld 901/ unbestimmt.
   *
   * @author baumann
   *
   */
  public enum Adressierung {
      EMPFAENGER("e"), ABSENDER("a"), UNBESTIMMT("");

    public final String text;

    private Adressierung(final String text) {
      this.text = text;
    }
  }

  /**
   * Bildet eine Mx-Adresse wie
   * <br>a-xDE-101-SE-F-ba
   * <br>ab.
   *
   * @param adressierung  entspricht a
   * @param stumm         entspricht x
   * @param library       entspricht DE-101
   * @param redaktion     entspricht SE
   * @param subadress     entspricht F-ba
   * @param raw           entspricht a-xDE-101-SE-F-ba
   */
  public MXAddress(
    final Adressierung adressierung,
    final boolean stumm,
    final Library library,
    final RedaktionsTyp redaktionsTyp,
    final String subadress,
    final String raw) {

    this.library = library;
    this.redaktionsTyp = redaktionsTyp;
    this.subadress = subadress;
    this.adressierung = adressierung;
    isStumm = stumm;
    this.raw = raw;
  }

  /**
   *
   */
  public MXAddress() {
    // TODO Auto-generated constructor stub
  }

  private Adressierung adressierung = Adressierung.UNBESTIMMT;

  /**
   * @return the adressierung
   */
  public Adressierung getAdressierung() {
    return adressierung;
  }

  /**
   * @param adressierung the adressierung to set
   */
  public void setAdressierung(final Adressierung adressierung) {
    this.adressierung = adressierung;
  }

  /**
   * mit 'x' stummgeschaltet (e-xAT-OBV)
   */
  public boolean isStumm = false;

  private Library library;

  private RedaktionsTyp redaktionsTyp = RedaktionsTyp.DEFAULT;

  /**
   * @return the redaktionsTyp
   */
  public RedaktionsTyp getRedaktionsTyp() {
    return redaktionsTyp;
  }

  /**
   * In der Regel die Teilinstitution und/oder die Person (F-ba). Eventuell
   * auch der ungeparste Teil.
   */
  private String subadress = "";

  /**
   * @return the library
   */
  public Library getLibrary() {
    return library;
  }

  /**
   * @param library the library to set
   */
  public void setLibrary(final Library library) {
    this.library = library;
  }

  /**
   * @return die Subadresse, z.B "F-ba" in "a-DE-101-SE-F-ba"
   */
  public String getSubadress() {
    return subadress;
  }

  /**
   * @param setzt die Subadresse, z.B "F-ba" in "a-DE-101-SE-F-ba";
   * wenn null, wird nichts gemacht.
   */
  public void setSubadress(final String rest) {
    if (rest != null)
      subadress = rest;
  }

  private String raw;

  /**
   * Parst eine Mailbox-Adresse wie
   * <br><br>a-xDE-101-SE-F-ba
   * <br></br>und gibt ein Objekt
   * <br><br>
   * (Adressierung, istStummgeschaltet, Redaktionstyp, Rest der Mx, Rohdaten) =
   * (ABSENDER, true, DNB, SE, F-ba, a-xDE-101-SE-F-ba)
   * <br><br>Kann keine Bibliothek erkannt werden, wird alles in den Rest verlagert.
   *
   *
   * @param mxAdStr  auch null. Wenn nicht null, dann dürfen keine Leerzeichen
   *                  im Innern vorhenden sein
   * @return    volle Adresse oder null
   */
  public static MXAddress parse(final String mxAdStr) {
    if (StringUtils.isNullOrEmpty(mxAdStr)) {
      return null;
    }
    // z.B. e-xDE-1-GKD-ba
    String raw = mxAdStr.trim();
    // Alle nicht Alphanumerischen + Zeichensetzung durch "-" ersetzen.
    // Es kann nämlich vorkommen, dass ein falscher Bindestrich eingegeben wird.
    raw = raw.replaceAll("[^\\p{Graph}]", "-");
    raw = raw.replaceAll("\\-+", "-");
    // Korrektur falscher Stummschaltung:
    if (raw.startsWith("ex-")) {
      raw = raw.replace("ex-", "e-x");
    }
    if (raw.startsWith("eX-")) {
      raw = raw.replace("eX-", "e-x");
    }

    Adressierung adressierung;
    // Position nach der Adressierungsart.
    int pos = 2;
    if (raw.startsWith("a-")) {
      adressierung = Adressierung.ABSENDER;
    } else if (raw.startsWith("e-")) {
      adressierung = Adressierung.EMPFAENGER;
    } else {
      adressierung = Adressierung.UNBESTIMMT;
      pos = 0;
    }
    boolean stumm;

    if (StringUtils.charAt(raw, pos) == 'x' || StringUtils.charAt(raw, pos) == 'X') {
      stumm = true;
      pos++;
    } else {
      stumm = false;
    }

    Library library;
    RedaktionsTyp redaktionsTyp;
    String rest;

    final String isilPlusRest = raw.substring(pos);
    // z.B. isilPlusRest = DE-1-GKD-ba
    final Pair<Library, String> pairLibRest = LibraryDB.parse(isilPlusRest);

    if (pairLibRest == null) {
      library = null;
      redaktionsTyp = RedaktionsTyp.DEFAULT;
      rest = isilPlusRest;
    } else {
      library = pairLibRest.first;
      // z.B. pairLibRest.second = GKD-ba
      // pairRedRest ist nie null!
      final Pair<RedaktionsTyp, String> pairRedRest = RedaktionsTyp.parse(pairLibRest.second);
      redaktionsTyp = pairRedRest.first;
      rest = pairRedRest.second;

    }
    return new MXAddress(adressierung, stumm, library, redaktionsTyp, rest, raw);
  }

  /**
   * @param isStumm the isStumm to set
   */
  public void setStumm(final boolean isStumm) {
    this.isStumm = isStumm;
  }

  /**
   * @param redaktion the redaktion to set
   */
  public void setRedaktion(final RedaktionsTyp redaktion) {
    redaktionsTyp = redaktion;
  }

  @Override
  public String toString() {
    String retString = "MXAddress [\n\tadressierung=" + adressierung;
    retString += ", \n\tstumm=" + isStumm;
    retString += ", \n\tlibrary=";
    retString += library != null ? library.nameKurz : "?";
    retString += ", \n\tredaktion=" + redaktionsTyp;
    retString += ", \n\trest=" + subadress + "]";
    return retString;
  }

  /**
   *
   * @param indent  Einrückungstiefe
   * @return        Stringrepräsentation mit Tabs vorneweg
   */
  public String toString(final int indent) {

    String retString = StringUtils.padding(indent, '\t');
    retString += adressierung.text;
    retString += "\t" + (isStumm ? "x" : "");
    retString += "\t" + (library != null ? library.nameKurz : "?");
    retString += "\t" + redaktionsTyp.asText;
    retString += "\t" + subadress;
    return retString;
  }

  public static void main(final String... args) {

    final MXAddress mxad = parse("e‐😀-DE‐601‐---SE");
    System.out.println(mxad);

  }

  /**
   *
   * @return  Korrigierte Mx-Adressierung
   */
  public String asMxString() {
    final List<String> list = new ArrayList<>(4);
    if (adressierung != Adressierung.UNBESTIMMT)
      list.add(adressierung.text);
    if (library != null) {
      String s = library.isil;
      if (isStumm)
        s = "x" + s;
      list.add(s);
    }
    if (redaktionsTyp != null && redaktionsTyp != RedaktionsTyp.DEFAULT)
      list.add(redaktionsTyp.asText);
    if (subadress != null && !subadress.isEmpty())
      list.add(subadress);
    return StringUtils.concatenate("-", list);
  }

}
