package de.dnb.basics;

import java.util.LinkedHashMap;

public class Constants {

  /**
   * Dateinamen:
   */
  private static final String TITEL = "DNBtitel_gesamt.dat.gz";
  private static final String GND_TITEL = "DNBGNDtitel.dat.gz";
  private static final String TITEL_UND_EXEMPLARE = "DNBtitelundexemplare.dat.gz";

  /**
   * Altueller Ordner des Datenabzuges auf Netzlaufwerk.
   */
  public static final String ORDNER_ABZUG_AKTUELL = "Z:/cbs/stages/prod/vollabzug/aktuell/Pica+/";

  /**
   * Lokaler Ordner des Datenabzuges.
   */
  public static final String ORDNER_ABZUG_LOKAL = "D:/Normdaten/";

  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public static final String CR = "\r";

  public static final String LF = "\n";

  public static final String CRLF = "\r\n";

  public static final char DOLLAR = '$';

  public static final char FLORIN = 'ƒ';

  /**
   * Marc Datensatzseparator.
   */
  public static final char MARC_REC_SEP = '\u001d';

  /**
   * Marc Feldseparator.
   */
  public static final char MARC_LINE_SEP = '\u001e';

  /**
   * Marc Unterfeldseparator.
   */
  public static final char MARC_SUB_SEP = '\u001f';

  /**
   * Marc Datensatzseparator als String.
   */
  public static final String GS = "" + MARC_REC_SEP;

  /**
   * Pica+ und Marc Feldseparator als String.
   */
  public static final String RS = "" + MARC_LINE_SEP;

  /**
   * Pica+ und Marc Unterfeldseparator als String.
   */
  public static final String US = "" + MARC_SUB_SEP;

  /**
   * Alle aktuellen GND- und Titeldaten auf Laufwerk Z.
   */
  public static final String GND_TITEL_GESAMT_Z = ORDNER_ABZUG_AKTUELL + GND_TITEL;

  /**
   * Alle aktuellen Titeldaten auf Laufwerk Z.
   */
  public static final String TITEL_GESAMT_Z = ORDNER_ABZUG_AKTUELL + TITEL;

  /**
   * Alle aktuellen Titeldaten mit Exemplardaten auf Laufwerk Z.
   */
  public static final String TITEL_PLUS_EXEMPLAR_Z = ORDNER_ABZUG_AKTUELL + TITEL_UND_EXEMPLARE;

  /**
   * Alle aktuellen Titeldaten mit Exemplardaten auf Laufwerk Z.
   */
  public static final String TITEL_PLUS_EXEMPLAR_D = ORDNER_ABZUG_LOKAL + TITEL_UND_EXEMPLARE;

  /**
   * Alle auf Laufwerk D gesicherten GND- und Titeldaten .
   */
  public static final String GND_TITEL_GESAMT_D = ORDNER_ABZUG_LOKAL + GND_TITEL;

  /**
   * Alle auf Laufwerk D gesicherten Titeldaten .
   */
  public static final String TITEL_GESAMT_D = ORDNER_ABZUG_LOKAL + TITEL;

  /**
   * Eine Stichprobe der Titeldaten (inklusive Bestandsdaten) auf Laufwerk D.
   */
  public static final String TITEL_STICHPROBE = ORDNER_ABZUG_LOKAL + "DNBtitel_Stichprobe.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten geographischen Daten.
   */
  public static final String Tg = ORDNER_ABZUG_LOKAL + "DNBGND_g.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten Werke.
   */
  public static final String Tu = ORDNER_ABZUG_LOKAL + "DNBGND_u.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten Sachbegriffe .
   */
  public static final String Ts = ORDNER_ABZUG_LOKAL + "DNBGND_s.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten Körperschaften .
   */
  public static final String Tb = ORDNER_ABZUG_LOKAL + "DNBGND_b.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten Konferenzen .
   */
  public static final String Tf = ORDNER_ABZUG_LOKAL + "DNBGND_f.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten Personen .
   */
  public static final String Tp = ORDNER_ABZUG_LOKAL + "DNBGND_p.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten Crosskonkordanzen .
   */
  public static final String Tc = ORDNER_ABZUG_LOKAL + "DNBGND_c.dat.gz";

  /**
   * Alle auf Laufwerk D gesicherten GND-SWW.
   */
  public static final String GND = ORDNER_ABZUG_LOKAL + "DNBGND.dat.gz";

  /**
   * Tb, Tf, Tg, Tp, Ts, Tu ...
   */
  public static final LinkedHashMap<String, String> SATZ_TYPEN = new LinkedHashMap<>();

  static {
    SATZ_TYPEN.put("alle Normdaten", GND);
    SATZ_TYPEN.put("Tb", Tb);
    SATZ_TYPEN.put("Tf", Tf);
    SATZ_TYPEN.put("Tg", Tg);
    SATZ_TYPEN.put("Tp", Tp);
    SATZ_TYPEN.put("Ts", Ts);
    SATZ_TYPEN.put("Tu", Tu);
    SATZ_TYPEN.put("Tc", Tc);
    SATZ_TYPEN.put("Titeldaten Z", TITEL_PLUS_EXEMPLAR_Z);
    SATZ_TYPEN.put("Titeldaten D", TITEL_PLUS_EXEMPLAR_D);
    SATZ_TYPEN.put("Titelstichprobe", TITEL_STICHPROBE);
  }

  /**
   * Die auf Laufwerk D gesicherte GND-Stichprobe.
   */
  public static final String GND_STICHPROBE = ORDNER_ABZUG_LOKAL + "DNBGND_Stichprobe.dat.gz";

  /**
   * Die auf Laufwerk D gesicherte GND/Titel-Stichprobe.
   */
  public static final String GND_TITEL_STICHPROBE =
    ORDNER_ABZUG_LOKAL + "DNBGNDtitel_Stichprobe.dat.gz";

  /**
   * Die Marc-xml-Daten.
   */
  public static final String DDC_XML = "Z:/cbs/stages/prod/ddc/ddc.xml";

  /**
   * Durchschnittliche Arbeitszeit pro Jahr.
   */
  public static final long STUNDEN_PRO_JAHR = 1326;

  /**
   * Durchschnittliche Arbeitstage pro Jahr.
   */
  public static final double TAGE_PRO_TAG = 200.33;

  /**
   * Durchschnittliche Arbeitszeit pro Tag.
   */
  public static final double STUNDEN_PRO_TAG = STUNDEN_PRO_JAHR / TAGE_PRO_TAG;

  /**
   * Vollzeitäquivalent in Minuten =
   * Durchschnittliche Arbeitszeit pro Jahr.
   */
  public static final long VZÄ = STUNDEN_PRO_JAHR * 60;

  public static void main(final String[] args) {
    System.out.println(STUNDEN_PRO_TAG);
  }

}
