/**
 *
 */
package de.dnb.gnd.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import de.dnb.basics.collections.CollectionUtils;
import de.dnb.gnd.parser.Record;

/**
 * Filter auf 0500, 0599 und 0600. Der Regelfall ist:
 * <br>gedruckt
 * <br>Reihe A
 * <br>selbstständig
 * <br>fertig bearbeitet.
 *
 * <pre>
 * 1. Wenn ein Status (0599 ... : a -> a) nicht in der
 *    Liste der notwendigen -> falsch. "Kein Status" wird mit (char)0 markiert,
 *    Soll der Status ignoriert werden, so muss
 *    {@link this#setIgnoreStatus(boolean)} aufgerufen werden.
 * 2. Wenn die Form (0500 Aac -> A) nicht korrekt (=nicht in der Liste der Notwendigen)
 *    ist -> falsch
 * 3. Die Erscheinungsform (0500 Aac -> a):
 *    a. Wenn die Liste der notwendigen nicht leer ist und sie nicht drin -> falsch
 *    b. wenn sie in der Liste der falschen -> falsch
 * 4. Die Status der Beschreibung (0500 Aac -> c):
 *    a. Wenn die Liste der notwendigen nicht leer ist und er nicht drin -> falsch
 *    b. wenn er in der Liste der falschen -> falsch
 * 5. Die Zuordnung (0500 Maqm->m):
 *    a. Wenn die Liste der notwendigen nicht leer ist und sie nicht drin -> falsch
 *    b. wenn sie in der Liste der falschen -> falsch
 * 6. Codeangaben (0600 ro;rm;nt -> [ro, rm, nt], müssen nicht immer vorkommen):
 *    a. ignoreCode == true -> wahr. Dieses Verhalten kann über {@link #setIgnoreCodes(boolean)}
 *       geändert werden.
 *    b. Codeangaben leer -> wahr
 *    c. (Codeangaben nicht leer):
 *      i. eine Codeangabe bei den falschen -> falsch
 *      ii. eine Codeangabe bei den notwendigen -> wahr
 *      iii. -> falsch
 *</pre2>
 *
 *
 * @author baumann
 *
 */
public class StatusAndCodeFilter implements Predicate<Record> {

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final Record record = RecordUtils.readFromClip();
    final StatusAndCodeFilter filter = filterAZc();
    System.out.println(filter.test(record));

  }

  /**
   * @return
   * gedruckt
   * <br>Reihe A
   * <br>selbstständig
   * <br>fertig bearbeitet.
   */
  public static StatusAndCodeFilter reiheA_selbststaendig() {
    return new StatusAndCodeFilter();
  }

  /**
   * @return
   *
   * <br>selbstständig
   * <br>fertig bearbeitet. Sollen alle erfasst werden, so muss {@link this#setIgnoreStatus(boolean)}
   *      auf true gesetzt werden!
   */
  public static StatusAndCodeFilter filterMusikalie() {
    final StatusAndCodeFilter filter = filterTontraeger();
    filter.formNotwendig.add('M');
    filter.zuordnungNotwendig.add((char) 0);

    filter.codesNotwendig.addAll(Arrays.asList(

      "mu", // Retrokonversion der Handbibliothek des DMA - Musikalien/Liederbücher
      "m1", // Partitur, nur im DMA
      "m2", // Stimmen, nur im DMA
      "m3", // Klavierauszug, nur im DMA
      "m4", // Studienpartitur, nur im DMA
      "m5", // Chorpartitur, nur im DMA
      "nt", // Noten
      "ul", // Retrokonversion der Handbibliothek des DMA - unselbstständige Literatur
      "un" //  unveränderte Neuauflage, nur im DMA

    ));
    return filter;
  }

  /**
   * @return
   *
   *
   * <br>selbstständig
   * <br>fertig bearbeitet.
   */
  public static StatusAndCodeFilter filterTontraeger() {
    final StatusAndCodeFilter filter = new StatusAndCodeFilter();

    //@formatter:off
    /**
     * Position 1 von 0500 = G/O.
     */
    filter.formNotwendig = new HashSet<Character>(Arrays.asList(
        'G', /* Musiktonträger*/
        'O')); /* Elektronische Ressource im Fernzugriff*/

    /**
     * Position 2 von 0500 = irrelevant.
     */
    filter.erscheinungsformFalsch.clear();

    /**
     * Position 3 von 0500 irrelevant.
     */
    filter.statusDerBeschreibungFalsch.clear();
    filter.statusDerBeschreibungNotwendig.clear();

    /**
     * Position 4 von 0500
     */
    filter.zuordnungFalsch.clear();
    filter.zuordnungNotwendig =
      new HashSet<Character>(Arrays.asList(
        'h', /* Historischer Tonträger*/
        'l', /* Leihmaterial (Bonner Katalog)*/
        'm' /* DMA*/
         ));

    /**
     * Status in 0599 (z.B. ' : a') oder nicht vorhanden, da fertig bearbeitet.
     */
    filter.statusseNotwendig.clear();
    filter.statusseNotwendig.add((char) 0);

    /**
     * Shortcut
     */
    filter.codesFalsch.clear();

    filter.codesNotwendig = new HashSet<>(
      Arrays.asList(
        "ab", /*Altbestand, nur im DMA*/
        "cs", /*Mini-CD 8 cm, nur im DMA*/
        "ep", /*elektronische Publikation, nur im DMA*/
        "ge", /*Geschenk, nur im DMA*/
        "gv", /*GVL-Auftragsnummer, GEMA-Materialien, nur im DMA*/
        "im", /*Import, nur im DMA*/
        "ka", /*Kauf, nur im DMA*/
        "la", /*Limitierte, gezählte Auflage, nur im DMA*/
        "lm", /*Leihmaterial, nur im DMA*/
        "md", /*Multimedia, nur im DMA*/
        "mk", /*Medienkombination, nur im DMA*/
        "mp", /*MP3-Format, nur im DMA*/
        "ms", /*Maxi-Single, CD-Maxi-Single, nur im DMA*/
        "mt", /*Musiktonträger*/
        "na", /*niedrige Auflage, nur im DMA*/
        "nb", /*Retrokonversion DMA - nicht im Bestand*/
        "ne", /*nicht erschienen, nur im DMA*/
        "nh", /*nicht im Handel erschienen (schließt Promo-Copy ein), nur im DMA*/
        "nl", /*nicht lieferbar, nur im DMA*/
        "np", /*Netzpublikation, nur im DMA*/
        "nv", /*Bestellnummer in der Vorlage nicht genannt, nur im DMA*/
        "pd", /*picture disc, nur im DMA*/
        "rm",  /* Musikalien und Musikschriften, Reihe M der DNB (wird automatisch
                vergeben)*/
        "rt", /*Musiktonträger, Reihe T der DNB (wird automatisch vergeben)*/
        "sa", /*Superaudio-CD, nur im DMA*/
        "tm", /*Teil-Musik*/
        "un", /*unveränderte Neuauflage, nur im DMA*/
        "vo", /*vorläufige Aufnahme, nur im DMA*/
        "zz" /* Selektionscode für zusätzliche Anzeige in der Reihe M*/
        ));
    //@formatter:on

    return filter;
  }

  /**
   * @return
   * gedruckt
   * <br>Reihe A
   * <br>auch unselbstständig (Af)
   * <br>keine Übersetzung deutschsprachiger Werke: bis Ende
                   2003 Reihe G der DNB, Teil 2, ab 2004 Reihe A
   * <br>fertig bearbeitet.
   */
  public static StatusAndCodeFilter filterGedrucktKeineUebersetzung() {
    final StatusAndCodeFilter filter = new StatusAndCodeFilter();
    filter.erscheinungsformFalsch.remove('f');
    filter.codesNotwendig.remove("ru");
    filter.codesFalsch.add("ru");
    return filter;
  }

  /**
   * @return
   * gedruckt
   * <br>Reihe A
   * <br>auch unselbstständig (Af)
   * <br>auchÜbersetzung deutschsprachiger Werke
   * <br>fertig bearbeitet.
   */
  public static StatusAndCodeFilter reiheA_Gedruckt_ImBestand() {
    final StatusAndCodeFilter filter = new StatusAndCodeFilter();
    filter.erscheinungsformFalsch.remove('f');
    return filter;
  }

  /**
   * @return
   * gedruckt
   * <br>Reihe A, B, H
   * <br>auch unselbstständig (Af)
   * <br>auchÜbersetzung deutschsprachiger Werke
   * <br>fertig bearbeitet.
   */
  public static StatusAndCodeFilter gedruckt_ImBestand() {
    final StatusAndCodeFilter filter = new StatusAndCodeFilter();
    filter.erscheinungsformFalsch.remove('f');
    filter.codesFalsch = new HashSet<>(Arrays.asList(
    //@formatter:off
      "rc", /* Karten, Reihe C der DNB */
      "ro", /* Netzpublikationen ab Bibliografiejahrgang 2010, Reihe O
               der DNB*/
      "rt", /* Musiktonträger, Reihe T der DNB
               (wird automatisch vergeben)*/
      "rm")); /* Musikalien und Musikschriften, Reihe M der
                    DNB (wird automatisch vergeben)*/

    filter.codesNotwendig = new HashSet<>(Arrays.asList(
      "li", /* Hinweis auf weiterführende Literaturangaben - Altdaten*/
      "öb", /* relevant für Öffentliche Bibliotheken - Altdaten*/
      "pn", /* andere Ausgabe*/
      "pb", /* parallele Ausgaben*/
      "ra", /* Monografien und Periodica des Verlagsbuchhandels,
               Reihe A der DNB*/
      "rh", /* Hochschulprüfungsarbeiten, Reihe H der DNB */
      "rb", /* Monografien und Periodica außerhalb des
               Verlagsbuchhandels, Reihe B der DNB*/
      "ru"/* Übersetzungen deutschsprachiger Werke: bis Ende
               2003 Reihe G der DNB, Teil 2, ab 2004 Reihe A*/));
    //@formatter:on
    return filter;
  }

  /**
   * @return
   * <br>alle Reihen
   * <br>0500: Pos. 3: kein q, Pos. 4: kein s
   * <br>0599: kein Eintrag, oder a, b, f , da dann schon im Haus
   *
   */
  public static StatusAndCodeFilter imBestand() {
    final StatusAndCodeFilter filter = new StatusAndCodeFilter();

    // pos 1
    filter.formNotwendig.clear();
    filter.formFalsch.clear();
    // pos 2
    filter.erscheinungsformNotwendig.clear();
    filter.erscheinungsformFalsch.clear();
    // pos 3: kein q
    filter.statusDerBeschreibungNotwendig.clear();
    filter.statusDerBeschreibungFalsch.clear();
    filter.statusDerBeschreibungFalsch.add('q');
    // pos 4: kein s
    filter.zuordnungNotwendig.clear();
    filter.zuordnungFalsch.clear();
    filter.zuordnungFalsch.add('s');
    // ra...
    filter.codesFalsch.clear();
    filter.codesNotwendig.clear();
    // 0599: auch f und b, da dann schon im Haus:
    filter.statusseNotwendig.add('f');
    filter.statusseNotwendig.add('b');

    return filter;
  }

  /**
   * 0500 kann an Position 3 ein q, an Position 4 ein s enthalten (Beispiel Aaqs).
   * Beides sind Kennzichen für Bücher nicht im Bestand der DNB (etwa gedruckt in Zürich).
   * filter  wird verändert.
   */
  public void auchNichtImBestand() {
    // pos 3
    statusDerBeschreibungFalsch.remove('q');
    // pos 4
    zuordnungFalsch.remove('s');
  }

  /**
   *
   * @return  Ac, Acn, Aco ... Zc, AE
   */
  public static StatusAndCodeFilter filterAZc() {
    final StatusAndCodeFilter filter = new StatusAndCodeFilter();
    filter.formNotwendig.add('Z');

    filter.erscheinungsformNotwendig.add('c');
    filter.erscheinungsformNotwendig.add('E');
    filter.erscheinungsformNotwendig.add('b'); /* Zeitschrift (vor 01.03.2007 nur verwendet
                                                  für Zeitschriften mit offener
                                                  oder zusammenfassender Bandaufführung) */
    filter.erscheinungsformNotwendig.add('d'); //  Schriftenreihe; Sammlung

    filter.statusDerBeschreibungNotwendig.add((char) 0);
    filter.statusDerBeschreibungNotwendig.add('l');
    filter.statusDerBeschreibungNotwendig.add('n');
    filter.statusDerBeschreibungNotwendig.add('o');
    filter.statusDerBeschreibungNotwendig.add('r');
    filter.statusDerBeschreibungNotwendig.add('s');
    filter.statusDerBeschreibungNotwendig.add('t');
    filter.statusDerBeschreibungNotwendig.add('v');

    filter.zuordnungFalsch.remove('z');

    //keine Codes beachten:
    filter.setIgnoreCodes(true);
    return filter;
  }

  /**
   * Position 1 von 0500. z.B. A von AF. Wenn leer, dann irrelevant. Sonst wird erzwungen.
   */
  private Set<Character> formNotwendig;

  /**
   * Position 1 von 0500. z.B. A von AF.
   */
  private final Set<Character> formFalsch = new HashSet<>();

  /**
   * Position 2 von 0500. z.B. F von AF. Wenn leer, dann irrelevant. Sonst wird erzwungen.
   */
  private final Set<Character> erscheinungsformNotwendig = new HashSet<>(); // meist leer

  /**
   * Position 2 von 0500. z.B. F von AF.
   */
  private final Set<Character> erscheinungsformFalsch;

  /**
   * Position 3 von 0500. Meist leer, dann irrelevant. Sonst wird erzwungen.
   */
  private final Set<Character> statusDerBeschreibungNotwendig = new HashSet<>();

  /**
   * Position 3 von 0500.
   */
  private final Set<Character> statusDerBeschreibungFalsch;

  /**
   * Position 4 von 0500. Wenn leer, irrelevant.
   */
  private final Set<Character> zuordnungFalsch;

  /**
   * Position 4 von 0500. Wenn leer, irrelevant.
   */
  private Set<Character> zuordnungNotwendig;

  /**
   * Status in 0599 (z.B. ' : a'). Wenn der Status nicht mittels {@link #setIgnoreStatus(boolean)}
   * auf true gesetzt wurde, dann muss mindestens ein (char)0 in der Liste enthalten sein.
   * Das ist das Kennzeichen, dass der Datensatz fertig und angezeigt ist.
   *
   */
  private final Set<Character> statusseNotwendig;

  /**
   * etwa 0600 rb.
   */
  private Set<String> codesFalsch;

  /**
   * etwa 0600 ra. Wenn leer, dann irrelevant. Sonst wird erzwungen.
   */
  private Set<String> codesNotwendig;

  /**
   * Auch unfertige Bücher werden akzeptiert. Standard: falsch.
   */
  private boolean ignoreStatus = false;

  /**
   *
   * @param ignoreStatus Auch unfertige Bücher werden akzeptiert. Standard: falsch.
   * @return  den Filter
   */
  public StatusAndCodeFilter setIgnoreStatus(final boolean ignoreStatus) {
    this.ignoreStatus = ignoreStatus;
    return this;
  }

  /**
   * Codeangaben in 0600 werden ignoriert. Standard: falsch.
   */
  private boolean ignoreCodes = false;

  /**
  *
  * @param ignoreCodes Codeangaben (ra, rh ..) in 0600 werden ignoriert. Standard: falsch.
  * @return den Filter
  */
  public StatusAndCodeFilter setIgnoreCodes(final boolean ignoreCodes) {
    this.ignoreCodes = ignoreCodes;
    return this;
  }

  /**
   * @return
   * gedruckt
   * <br>Reihe A
   * <br>selbstständig
   * <br>fertig bearbeitet.
   */
  private StatusAndCodeFilter() {

    /**
     * Position 1 von 0500 = A.
     */
    //@formatter:off
      formNotwendig = new HashSet<Character>(Arrays.asList(
          'A' /* Druckschrift */));
      /**
       * Position 2 von 0500 = f, l, v.
       */
      erscheinungsformFalsch = new HashSet<Character>(Arrays.asList(
          'f', /* Unselbstständiger Teil (Band, Heft)
                  eines mehrbändigen begrenzten
                  Werkes oder einer Zeitschrift */
          'l', /* Unselbstständiger Teil (z.B. Sammelbandbeitrag)
                  einer einteiligen Ressource, Zeitschriftenartikel
                  oder Zeitschriftenheft, unselbstständige
                  Werke der Musik */
          'v')); /* Verkürzte Bandaufführung in Form eines Untersatzes
                    mit Hinweis auf die Ordnungsblöcke der betreffenden
                    Stücktitelaufnahmen (in den Altdaten der DNB
                    Frankfurt, Bibliografie-Jahrgänge 1972-1984,
                    erstes Halbjahr und in den Altdaten des DMA,
                    Bibliografie-Jahrgänge 1976-1996) */
      /**
       * Position 3 von 0500.
       */
      statusDerBeschreibungFalsch = new HashSet<Character>(Arrays.asList(
          'a', /* Provisorischer Datensatz (Bestelldatensatz)*/
          'B', /* temporäre Kennzeichnung einer mögliche Dublette (s. 1698)*/
          'c', /* Datensatz Neuerscheinungsdienst (ND)*/
          'f', /* Fremddaten (z.B. Online-Ressourcen Geschäftsgang NP,
                  Edition Corvey*/
          'i', /* Datensatz für EP-Image (Bereitstellungssystem,
                  nur in Verbindung mit
                  2. Position = "l")*/
          'm', /* Mahnung*/
          'q', /* Bibliografische Meldung, kein Exemplar vorhanden*/
          'v', /* Korrekturberechtigungsstatus im ZDB-Bestand*/
          'w')); /* Konvertierte Daten der Handbibliothek des DMA;
                    bei 1. Pos. G oder M:
                    Primärkatalogisierung nicht bibliografierelevant*/


      /**
       * Position 4 von 0500
       */
      zuordnungFalsch = new HashSet<Character>(Arrays.asList(
          'h', /* Historischer Tonträger*/
          'l', /* Leihmaterial (Bonner Katalog)*/
          'm', /* DMA*/
          's', /* Datensatz ohne Bestand in DNB*/
          'o', /* Zeitschriftenartikel (neu 03/2009)*/
          'z')); /* Datensatz im ZDB-Bestand*/


      zuordnungNotwendig  = new HashSet<>();

      /**
       * Status in 0599 (z.B. ' : a')
       */
      statusseNotwendig = new HashSet<>(Arrays.asList(
        (char)0, /*fertig angezeigt*/
        'a', /*  bereit zur Anzeige */
        'g', /* gravierende Korrektur */
        'k'  /* Lizenzfreie Online-Ressource wird kostenpflichtig */
          ));


      /**
       * Shortcut
       */
      codesFalsch = new HashSet<>(Arrays.asList(
          "rc", /* Karten, Reihe C der DNB */
          "rh", /* Hochschulprüfungsarbeiten, Reihe H der DNB */
          "rb", /* Monografien und Periodica außerhalb des
                   Verlagsbuchhandels, Reihe B der DNB*/
          "ro", /* Netzpublikationen ab Bibliografiejahrgang 2010, Reihe O
                   der DNB*/
          "rt", /* Musiktonträger, Reihe T der DNB
                   (wird automatisch vergeben)*/
          "rm")); /* Musikalien und Musikschriften, Reihe M der
                        DNB (wird automatisch vergeben)*/

      codesNotwendig = new HashSet<>(Arrays.asList(
          "li", /* Hinweis auf weiterführende Literaturangaben - Altdaten*/
          "öb", /* relevant für Öffentliche Bibliotheken - Altdaten*/
          "pn", /* andere Ausgabe*/
          "pb", /* parallele Ausgaben*/
          "ra", /* Monografien und Periodica des Verlagsbuchhandels,
                   Reihe A der DNB*/
          "ru"/* Übersetzungen deutschsprachiger Werke: bis Ende
                   2003 Reihe G der DNB, Teil 2, ab 2004 Reihe A*/));
          //@formatter:on
  }

  @Override
  public boolean test(final Record record) {
    // --------- STATUS aus 0599
    // etwa b bei "0599 23-09-15 : b"
    if (!ignoreStatus && !statusseNotwendig.isEmpty()) {
      if (CollectionUtils.intersection(statusseNotwendig, BibRecUtils.getStatuses(record))
        .isEmpty())
        return false;
    }

    // --------- PHYSISCHE FORM, 1. Position von 0500,
    // etwa A = Druckschrift bei Aa

    final char c1 = BibRecUtils.getPhysikalischeForm(record);
    if (!formNotwendig.isEmpty())
      if (!formNotwendig.contains(c1)) {
        return false;
      }
    if (formFalsch.contains(c1)) {
      return false;
    }

    // --------- BIBLIOGRAFISCHE ERSCHEINUNGSFORM, 2. Position von 0500,
    // etwa a = Einzelne Einheit bei Aa

    final char c2 = BibRecUtils.getBibliografischeErscheinungsform(record);
    if (!erscheinungsformNotwendig.isEmpty()) {
      if (!erscheinungsformNotwendig.contains(c2)) {
        return false;
      }
    }
    if (erscheinungsformFalsch.contains(c2)) {
      return false;
    }

    // --------- STATUS DER BESCHREIBUNG, 3. Position von 0500,
    // etwa f = Fremddaten bei Olfo

    final char c3 = BibRecUtils.getStatusderBeschreibung(record);
    if (!statusDerBeschreibungNotwendig.isEmpty()) {
      if (!statusDerBeschreibungNotwendig.contains(c3)) {
        return false;
      }
    } else if (statusDerBeschreibungFalsch.contains(c3)) {
      return false;
    }

    // --------- ZUORDNUNG, 4. Position von 0500,
    // etwa o = Automatisch eingespielter Datensatz bei Olfo

    final char c4 = BibRecUtils.getZuordnungDesDatensatzes(record);
    if (!zuordnungNotwendig.isEmpty()) {
      if (!zuordnungNotwendig.contains(c4)) {
        return false;
      }
    }
    if (zuordnungFalsch.contains(c4)) {
      return false;
    }

    // ----------- CODES (ra, rb ...)

    //  Letzter Schritt: Sind uns die Codes egal?
    if (ignoreCodes)
      return true; // dann fertig

    final List<String> codeAngaben = BibRecUtils.getCodes(record);

    // leere 0600 werden toleriert, da bei alten Titeln denkbar
    if (codeAngaben.isEmpty())
      return true;

    if (!codesNotwendig.isEmpty()) {
      if (CollectionUtils.intersection(codesNotwendig, codeAngaben).isEmpty())
        return false;
    }
    if (!CollectionUtils.intersection(codesFalsch, codeAngaben).isEmpty())
      return false;

    // Alle Tests anstandsfrei passiert:
    return true;
  }

}
