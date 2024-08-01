/**
 *
 */
package de.dnb.basics.utils;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dnb.basics.Misc;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;

/**
 * Lädt vom Zoll eine Liste der monatsaktuellen Umrechnungskurse Euro -> Währung.
 * Für Währungen wie DM, öS etc. werden die Standardkurse verwendet.
 * Strings, die eine Preisangabe enthalten, können in Euro umgerechnet werden.
 * Dabei werden auch z.T. die nicht-ISO-Abkürzungen verwendet.
 *
 * @author baumann
 *
 */
public class WaehrungsRechner {

  private final Map<String, Double> currency2rate = new HashMap<>();

  /**
   * Konstruktor.
   */
  public WaehrungsRechner() {
    currency2rate.put("eur", 1.0);
    currency2rate.put("dm", 1.95583);
    currency2rate.put("nlg", 2.20371);// NL
    currency2rate.put("ats", 13.7603);// Schilling
    currency2rate.put("frf", 6.55957);// Franc
    currency2rate.put("itl", 1936.27);// Lira
    currency2rate.put("bef", 40.3399);// Belgischer Franc
    currency2rate.put("sek", 11.806);// Schwedische Krone
    currency2rate.put("esp", 166.386);// Peseta
    currency2rate.put("adp", 166.386);// Peseta
    currency2rate.put("fim", 5.94573);// Finnmark

    for (int kursart = 1; kursart <= 2; kursart++) {
      final String suchfrage = macheSuchfrage(kursart);
      String antwort = Misc.getWebsite(suchfrage);
      //      System.err.println(antwort);

      antwort = antwort.replace(';', '\t');

      final String[][] table = StringUtils.makeTable(antwort);
      final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
      for (int i = 0; i < table.length; i++) {
        String currency = StringUtils.getCellAt(table, i, 3);
        final String kursStr = StringUtils.getCellAt(table, i, 2);
        Number number;
        try {
          number = numberFormat.parse(kursStr);
        } catch (final ParseException e) {
          continue;
        }
        final double kurs = number.doubleValue();
        currency = normalize(currency);
        currency2rate.put(currency, kurs);
      }
    }
  }

  /**
   *
   * @param betrag
   *            irgendwas
   * @param currency
   *            eine Währungseinheit
   * @return Euros (betrag / kurs) oder null
   */
  public Double getEuros(final double betrag, final String currency) {

    final Double kurs = getKurs(currency);
    if (kurs == null)
      return null;

    return betrag / kurs;
  }

  /**
   *
   * @param currency
   *            Währung
   * @return Währung pro Euro
   */
  public Double getKurs(String currency) {

    if (currency.equalsIgnoreCase("$"))
      currency = "USD";
    else if (currency.equalsIgnoreCase("R"))
      currency = "ZAR";
    else if (currency.equalsIgnoreCase("£"))
      currency = "GBP";
    else if (currency.equalsIgnoreCase("Ł"))
      currency = "GBP";
    else if (currency.equalsIgnoreCase("฿"))
      currency = "THB";
    else if (currency.equalsIgnoreCase("¥"))
      currency = "JPY";
    if (currency.equalsIgnoreCase("Yen"))
      currency = "JPY";
    else if (currency.equalsIgnoreCase("Y"))
      currency = "JPY";
    else if (currency.equalsIgnoreCase("₦"))
      currency = "NGN";
    else if (currency.equalsIgnoreCase("₹"))
      currency = "INR";
    else if (currency.equalsIgnoreCase("₫"))
      currency = "VND";
    else if (currency.equalsIgnoreCase("₴"))
      currency = "UAH";
    else if (currency.equalsIgnoreCase("₾"))
      currency = "GEL";
    else if (currency.equalsIgnoreCase("₪"))
      currency = "ILS";
    else if (currency.equalsIgnoreCase("S/."))
      currency = "PEN";
    else if (currency.equalsIgnoreCase("R$"))
      currency = "BRL";
    else if (currency.equalsIgnoreCase("元"))
      currency = "CNY";
    else if (currency.equalsIgnoreCase("RMB Y"))
      currency = "CNY";
    else if (currency.equalsIgnoreCase("yuan"))
      currency = "CNY";
    else if (currency.equalsIgnoreCase("៛"))
      currency = "KHR";
    else if (currency.equalsIgnoreCase("₺"))
      currency = "TRY";
    else if (currency.equalsIgnoreCase("₩"))
      currency = "KRW";
    else if (currency.equalsIgnoreCase("Zł"))
      currency = "PLN";
    else if (currency.equalsIgnoreCase("$A"))
      currency = "AUD";
    else if (currency.equalsIgnoreCase("hfl"))
      currency = "NLG";
    else if (currency.equalsIgnoreCase("ƒ"))
      currency = "NLG";
    else if (currency.equalsIgnoreCase("S"))
      currency = "ATS";
    else if (currency.equalsIgnoreCase("öS"))
      currency = "ATS";
    else if (currency.equalsIgnoreCase("FF"))
      currency = "FRF";
    else if (currency.equalsIgnoreCase("FFR"))
      currency = "FRF";
    else if (currency.equalsIgnoreCase("F"))
      currency = "FRF";
    else if (currency.equalsIgnoreCase("SFR"))
      currency = "CHF";
    else if (currency.equalsIgnoreCase("lire"))
      currency = "ITL";
    else if (currency.equalsIgnoreCase("bfr"))
      currency = "BEF";
    else if (currency.equalsIgnoreCase("Kčs"))
      currency = "CZK";
    else if (currency.equalsIgnoreCase("Kcs"))
      currency = "CZK";
    else if (currency.equalsIgnoreCase("Kč"))
      currency = "CZK";
    else if (currency.equalsIgnoreCase("dkr"))
      currency = "DKK";
    else if (currency.equalsIgnoreCase("kr."))
      currency = "DKK";
    else if (currency.equalsIgnoreCase("kr"))
      currency = "DKK";
    else if (currency.equalsIgnoreCase("skr"))
      currency = "SEK";
    else if (currency.equalsIgnoreCase("nkr"))
      currency = "NOK";
    else if (currency.equalsIgnoreCase("pta"))
      currency = "ESP";
    else if (currency.equalsIgnoreCase("ptas"))
      currency = "ESP";
    else if (currency.equalsIgnoreCase("ft"))
      currency = "HUF";
    else if (currency.equalsIgnoreCase("Fmk"))
      currency = "FIM";
    else if (currency.equalsIgnoreCase("ytl"))
      currency = "TRY";
    else if (currency.equalsIgnoreCase("tl"))
      currency = "TRY";

    currency = normalize(currency);
    return currency2rate.get(currency);
  }

  /**
   * @param currency
   * @return
   */
  private String normalize(String currency) {
    currency = currency.toLowerCase();
    currency = StringUtils.unicodeComposition(currency);
    return currency;
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) {

    final WaehrungsRechner rechner = new WaehrungsRechner();

    final Scanner scanner = new Scanner(System.in);

    System.out.println("Record kopieren");
    System.out.println("Buchstabe eingeben und Enter drücken");
    while (scanner.hasNext()) {

      final String s = scanner.next();
      final Record record = RecordUtils.readFromClip();
      final double preis = rechner.wertAllerExemplare(record);
      System.out.println(preis);

      System.out.println("Record kopieren");
      System.out.println("Buchstabe eingeben und Enter drücken");
    }

  }

  /**
   *
   * @param record  nicht null
   * @return        Wert des Exemplars (wertet Felder
   *                2000, 2300, 2305, 2310 und 2315 aus)
   */
  public double findeWert(final Record record) {
    final List<String> infos =
      RecordUtils.getContentsOfFirstSubfield(record, 'f', "2000", "2300", "2305", "2310", "2315");
    final double preis = findePreis(infos);
    return preis;
  }

  /**
   *
   * @param record  nicht null
   * @return        Wert des Exemplars * Anzahl Exemplare (Mindestzahl: 1)
   */
  public double wertAllerExemplare(final Record record) {
    final double preisProEx = findeWert(record);
    final int anzahlEx = Integer.max(1, BibRecUtils.getCountDNB(record));
    return preisProEx * anzahlEx;
  }

  /**
   * @param kursart
   * @return
   */
  private String macheSuchfrage(final int kursart) {
    final int jahr = TimeUtils.getActualYear();
    final int monat = TimeUtils.getActualMonth();

    return "https://www.zoll.de/SiteGlobals/Functions/Kurse/KursExport.csv?"
      + "view=csvexportkursesearchresultZOLLWeb" + "&kursart=" + kursart + "&startdatum_tag2=01"
      + "&startdatum_monat2=" + monat + "&startdatum_jahr2=" + jahr + "&enddatum_tag2=31"
      + "&enddatum_monat2=" + monat + "&enddatum_jahr2=" + jahr + "&sort=asc&spalte=gueltigkeit";
  }

  /**
   *
   * @param matcher
   *            hat etwas gefunden, der erste hit wird ausgewertet.
   * @return einen Preis, der auch 0 sein kann.
   */
  double findePreis(final Matcher matcher) {
    final String currency = matcher.group(2);
    final String preisStr = matcher.group(3);
    //    System.err.println(currency);
    //    System.err.println(preisStr);
    Double preis;
    try {
      preis = Double.parseDouble(preisStr);
    } catch (final NumberFormatException e) {
      return 0;
    }
    preis = getEuros(preis, currency);
    if (preis == null)
      return 0;
    else {
      if (currency.equalsIgnoreCase("KRW"))
        // Seltsamkeiten bei der Notation der koreanischen Währung, z.B. 20.000:
        preis = preis * 1000;
      return preis;
    }
  }

  /**
   *
   * @param infos
   *            Liste mit möglichen Preisen
   * @return Preis oder 0.
   */
  public double findePreis(final List<String> infos) {
    for (final String info : infos) {
      final double preis = findePreis(info);
      if (preis > 0)
        return preis;
    }
    return 0;
  }

  static final String PREFIX = "(^|\\s+)";
  static final String ZAHL = "(\\d+(\\.\\d+)?)";
  static final String PAT_STR_EINZEL = PREFIX + "([$£฿¥₦₹₫₴₾₪元៛₺₩ŁYysfr]) *" + ZAHL;
  static final String PAT_STR_ISO = PREFIX + "(\\p{Alpha}\\p{Alpha}\\p{Alpha}) *" + ZAHL;
  static final String PAT_STR_REST =
    PREFIX + "(a/\\.|zł|\\$a|ös|fl|dm|fr|lire|kčs|kč|kr\\.?|r\\$|ft|ptas|rmb y|yuan|tl) *" + ZAHL;
  static final Pattern PAT_EINZEL = Pattern.compile(PAT_STR_EINZEL);
  static final Pattern PAT_ISO = Pattern.compile(PAT_STR_ISO);
  static final Pattern PAT_REST = Pattern.compile(PAT_STR_REST);

  /**
   *
   * @param info
   *            Enhält so etwas wie EUR 23.45 (DE)
   * @return Preis oder 0.
   */
  double findePreis(String info) {

    Double preis;
    info = normalize(info);

    Matcher matcher = PAT_ISO.matcher(info);
    if (matcher.find()) {
      preis = findePreis(matcher);
      if (preis > 0)
        return preis;
    }

    matcher = PAT_REST.matcher(info);
    if (matcher.find()) {
      preis = findePreis(matcher);
      if (preis > 0)
        return preis;
    }

    matcher = PAT_EINZEL.matcher(info);
    if (matcher.find()) {
      preis = findePreis(matcher);
      if (preis > 0)
        return preis;
    }

    return 0;
  }

}
