/**
 *
 */
package de.dnb.gnd.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.collections.Multiset;
import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.GNDPersonLine;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.GNDTagDB;

/**
 * @author baumann
 *
 */
public class PersonUtils {

  /**
   * (null, true, true), wenn nichts geparst werden konnte.
   */
  private static final Triplett<Between<LocalDate>, Boolean, Boolean> NULL_TRIPLETT =
    new Triplett<>(null, true, true);

  /**
   * Längste denkbare Lebensdauer, 122 Jahre.
   */
  public static final int LEBENSDAUER_MAX = 122;

  /**
   * Frühester Zeitpunkt der Schaffenszeit, 5 Jahre.
   */
  public static final int SCHAFFEN_BEGINN_MIN = 5;

  /**
   * Maximale Zahl an Jahren zwischen Todesjahr und Publikationsjahr,
   * 10 Jahre.
   */
  static final int MAX_NACH_TOD = 10;
  /**
   * Test auf $4datl.
   */
  final static Predicate<Line> predicateLebensdaten = new Predicate<Line>() {
    String dollar4 = "datl";

    @Override
    public boolean test(final Line line) {
      return GNDUtils.testDollar4(line, dollar4);
    }
  };

  /**
   * Liefert Zeile mit Lebensdaten (548 ... $4datl).
   *
   * @param record	nicht null
   * @return			null, wenn keine vorhanden, die Zeile, wenn genau
   * 					eine vorhanden, wirft eine IllegalStateException,
   * 					wenn mehrere vorhanden
   */
  public static Line getDatlLine(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final ArrayList<Line> lines548 =
      RecordUtils.getLines(record, predicateLebensdaten, GNDTagDB.TAG_548);
    if (lines548.size() == 1)
      return lines548.get(0);
    else if (lines548.isEmpty())
      return null;
    else
      throw new IllegalStateException("Datensatz " + record.getId() + " hat mehrere Lebensdaten");
  }

  /**
   * Daten aus Feld 548 $4 datl/datx. Intervalle. Ungenaue Angaben (aus $d)
   * werden berücksichtigt, wenn keine anderen vorliegen.
   *
   * @param record  nicht null, Personendatensatz
   * @return        nicht null!<br>
   *                (Ein abgeschlossenes Intervall, Geburt ist unsicher, Tod ist unsicher)
   *                <br>Das Intervall ist null, wenn keine Daten vorhanden, dann sind
   *                Anfang/Ende unsicher.
   *                Für Zeitpunkte t, das Intervall
   *                [t1, t2], sofern ti tagesgenau ist, ansonsten [1.1.t1, 31.12.t2].
   *                Änhlich wird für alle nicht tagesgenauen Werte verfahren.
   *                Ist einer der Werte nicht vorhanden (=null), so wird das
   *                minimale/maximale Datum verwendet.
   *                <br>Anfang/Ende sind unsicher, wenn ein 'X' vorkommt, wenn
   *                die Daten aus $d stammen oder wenn Anfang/Ende nicht vorhanden sind.
   *
   */
  public static Triplett<Between<LocalDate>, Boolean, Boolean> getLebensdaten(final Record record) {
    final List<Line> linesDatx = RecordUtils.getLinesWithSubfield(record, "548", '4', "datx");
    final List<Line> linesDatl = RecordUtils.getLinesWithSubfield(record, "548", '4', "datl");
    if (linesDatx.isEmpty() && linesDatl.isEmpty())
      return NULL_TRIPLETT;

    for (final Line lineDatx : linesDatx) {
      final Triplett<Between<LocalDate>, Boolean, Boolean> dates = getLebensdaten(lineDatx);
      if (!dates.equals(NULL_TRIPLETT))
        return dates;
    }

    for (final Line lineDatl : linesDatl) {
      final Triplett<Between<LocalDate>, Boolean, Boolean> dates = getLebensdaten(lineDatl);
      if (!dates.equals(NULL_TRIPLETT))
        return dates;
    }

    return NULL_TRIPLETT;

  }

  /**
   *
   * @param line548 auch null
   * @return        nicht null!<br>
   *                (Ein abgeschlossenes Intervall, Geburt ist unsicher, Tod ist unsicher)
   *                <br>Das Intervall ist null, wenn keine vernünftige Daten vorhanden,
   *                dann sind Anfang/Ende unsicher ({@link PersonUtils#NULL_TRIPLETT})
   *                <br>Für Zeitpunkte t, das Intervall
   *                [t1, t2], sofern ti tagesgenau ist, ansonsten [1.1.t1, 31.12.t2].
   *                Änhlich wird für alle nicht tagesgenauen Werte verfahren.
   *                Ist einer der Werte nicht vorhanden (=null), so wird die
   *                maximale Lebenszeit verwendet.
   *                <br>Manchmal sind die Daten total falsch, aber sicher.
   *                <br>z.B. 548 14101629 $b 23051663 $4 datx.
   *                <br>Das wird durch Abgleich mit der maximalen
   *                Lebenserwartung und mit vernünftigen Jahreszahlen ausgeschlossen.
   *                <br>Anfang/Ende sind unsicher, wenn ein 'X' vorkommt, wenn
   *                die Daten aus $d stammen oder wenn Anfang/Ende nicht vorhanden sind.
   */
  public static Triplett<Between<LocalDate>, Boolean, Boolean> getLebensdaten(final Line line548) {

    if (line548 == null)
      return NULL_TRIPLETT;

    final Triplett<Between<LocalDate>, Boolean, Boolean> dates = GNDUtils.getDaten(line548);
    final Between<LocalDate> intervallRaw = dates.first;
    // Eigentlich unnötig, aber zur Sicherheit:
    if (intervallRaw == null)
      return NULL_TRIPLETT;
    // Die Schnittstelle ist ein wenig anders als bei allgemeinen Intervallen, die auch
    // "Geschichte Anfänge-" sein können. In einem solchen Fall sind Anfang ud Ende bekannt.
    // Bei Lebensdaten muss man davon ausgehen, dass gar nichts bekannt ist:
    LocalDate geburt = intervallRaw.lowerBound;
    final boolean geburtUnsicher = LocalDate.MIN.equals(geburt) || dates.second;
    LocalDate tod = intervallRaw.higherBound;
    final boolean todUnsicher = LocalDate.MAX.equals(tod) || dates.third;

    final int yearGeb = geburt.getYear();
    final int yearTod = tod.getYear();
    Between<LocalDate> intervall = intervallRaw;
    if (lebensdatenUnplausibel(yearGeb, yearTod)) {
      if (!geburtUnsicher && !todUnsicher)
        return NULL_TRIPLETT;
      if (geburtUnsicher && todUnsicher)
        return NULL_TRIPLETT;
      if (!geburtUnsicher && todUnsicher) {
        tod = geburt.plusYears(PersonUtils.LEBENSDAUER_MAX);
      }
      if (geburtUnsicher && !todUnsicher) {
        geburt = tod.minusYears(PersonUtils.LEBENSDAUER_MAX);
      }
      intervall = new Between<>(geburt, tod);
    }

    return new Triplett<>(intervall, geburtUnsicher, todUnsicher);

  }

  /*Jetzt kommt der Clou: Manchmal sind die Daten total falsch, aber sicher.
  z.B. 548 14101629 $b 23051663 $4 datx.
  Das muss durch Abgleich mit der maximalen
  Lebenserwartung und mit vernünftigen Jahreszahlen ausgeschlossen werden:*/
  private static boolean lebensdatenUnplausibel(final int yearGeb, final int yearTod) {
    if (yearTod - yearGeb > LEBENSDAUER_MAX)
      return true;
    if (yearTod < yearGeb)
      return true;
    if (yearGeb < -10000)
      return true;
    if (yearTod > 3000)
      return true;
    return false;
  }

  /**
   * Geschätzte Wirkungsdaten. Entweder aus 548 $4datw oder aus den Lebensdaten.
   * Wenn überhaupt keine Lebensdaten vorliegen, wird
   * das maximale Intervall zurückgegeben. Sonst wird der Beginn der Schaffenszeit
   * mit der Geburt (!) vermutet. Liegen entweder Geburts- oder Todesdatum
   * nicht vor, wird die
   * maximale Schaffenzeit auf 95 Jahre geschätzt.
   *
   * @param record          nicht null
   * @return                Die Wirkungsdaten, sofern die Felder Geburt und/oder Tod vorhanden sind,
  *                         sonst null.
   */
  public static Between<LocalDate> getWirkungsdaten(final Record record) {
    Triplett<Between<LocalDate>, Boolean, Boolean> datenTripel;
    final Triplett<Between<LocalDate>, Boolean, Boolean> lebenAusRecord = getLebensdaten(record);
    datenTripel = lebenAusRecord;

    Triplett<Between<LocalDate>, Boolean, Boolean> wirkungAusRecord = NULL_TRIPLETT;
    final List<Line> lines = RecordUtils.getLinesWithSubfield(record, "548", '4', "datw");
    if (!lines.isEmpty()) {
      final Line line548 = lines.get(0);
      wirkungAusRecord = GNDUtils.getDaten(line548);
      datenTripel = wirkungAusRecord;
    }

    final Between<LocalDate> wirkungsInterv = wirkungAusRecord.first;
    final Between<LocalDate> lebensInterv = lebenAusRecord.first;
    if (wirkungsInterv == null && lebensInterv == null)
      return null;

    // wenn zuviel Unfug zu erwarten ist:
    if (wirkungsInterv != null && lebensInterv != null) {
      if (!wirkungsInterv.intersects(lebensInterv)) {
        return null;
      } else {
        return wirkungsInterv.getIntersection(lebensInterv);
      }
    }

    // => daten != null:
    final Between<LocalDate> daten = datenTripel.first;
    LocalDate geburt = daten.lowerBound;
    LocalDate tod = daten.higherBound;
    // Normieren:
    if (LocalDate.MIN.equals(geburt))
      geburt = null;
    if (LocalDate.MAX.equals(tod))
      tod = null;

    final boolean anfangUnsicher = datenTripel.second || geburt == null;
    final boolean endeUnsicher = datenTripel.third || tod == null;

    return getWirkungsdaten(geburt, tod, anfangUnsicher, endeUnsicher);
  }

  /**
   * Geschätzte Wirkungsdaten. Wenn überhaupt keine Lebensdaten vorliegen, wird
   * null zurückgegeben. Sonst wird der Beginn der Schaffenszeit
   * mit der Geburt(!) vermutet. Liegen entweder Geburts- oder Todesdatum
   * nicht vor, wird die
   * maximale Lebenszeit auf 122 Jahre geschätzt.
   *
   * @param geburt          auch null (gleich behandelt wie {@link LocalDate#MIN}),
   *                        wenn nicht gegeben
   * @param tod             auch null (gleich behandelt wie {@link LocalDate#MAX}),
   *                        wenn nicht gegeben
   * @param geburtUnsicher  z.B., wenn in der Datumsangabe ein 'X' auftaucht
   * @param todUnsicher     z.B., wenn in der Datumsangabe ein 'X' auftaucht
   * @return                Die Wirkungsdaten, sofern die Felder Geburt und Tod vorhanden sind,
  *                         sonst null.
   */
  @SuppressWarnings("null")
  public static Between<LocalDate> getWirkungsdaten(
    LocalDate geburt,
    LocalDate tod,
    final boolean geburtUnsicher,
    final boolean todUnsicher) {

    if (geburt != null && tod != null) {
      if (geburt.compareTo(tod) > 0)
        throw new IllegalArgumentException("Tod vor Geburt");
    }

    // Normieren:
    if (LocalDate.MIN.equals(geburt))
      geburt = null;
    if (LocalDate.MAX.equals(tod))
      tod = null;

    if (geburt == null && tod == null)
      return null;
    LocalDate anfang;
    LocalDate ende;

    if (geburt == null) {
      //        =>     g=null,t<>null
      anfang = tod.minusYears(PersonUtils.LEBENSDAUER_MAX);
      ende = tod;
    } else {
      // => g<>null
      anfang = geburt;
      if (tod == null) {
        //              g<>null,t=null
        ende = anfang.plusYears(PersonUtils.LEBENSDAUER_MAX);
      } else {
        ende = tod;//   g<>null,t<>null
      }
    }

    final int yearEnd = ende.getYear();
    final int yearBegin = anfang.getYear();
    if (yearEnd - yearBegin > PersonUtils.LEBENSDAUER_MAX) {
      // Zurechtruckeln des Intervalls
      if (geburtUnsicher && !todUnsicher) {
        anfang = ende.minusYears(PersonUtils.LEBENSDAUER_MAX);
      }
      if (!geburtUnsicher && todUnsicher) {
        ende = anfang.plusYears(PersonUtils.LEBENSDAUER_MAX);
      }
      // Auf sicher gehen, da Dinge wie "$dum 1365/76" nicht wirklich vernünftig
      // geparst werden können:
      if (geburtUnsicher && todUnsicher) {
        return null;
      }
      if (!geburtUnsicher && !todUnsicher) {
        if (lebensdatenUnplausibel(yearBegin, yearEnd)) {
          return null;
        }
      }
    }

    return Between.getOrdered(anfang, ende);
  }

  /**
   *
   * @param record  Person, nicht null
   * @return        Idns der Wirkungsorte (ortw), eventuell leer.
   */
  public static List<String> getWirkungsorte(final Record record) {
    final List<Line> linesW = RecordUtils.getLinesWithSubfield(record, "551", '4', "ortw");
    return FilterUtils.map(linesW, line -> SubfieldUtils.getContentOfFirstSubfield(line, '9'));
  }

  /**
   * Kann Musiker erkennen.
   *
   * @param record  Person, nicht null
   * @param musikberufe    Liste von idns von Berufen, auch null
   * @return        ist Musiker*in
   */
  public static boolean isMusiker(final Record record, final Set<String> musikberufe) {
    if (musikberufe != null) {
      final List<Line> linesBer = RecordUtils.getLinesWithSubfield(record, "550", '4', "beru|berc");
      final List<String> berufIDNs =
        FilterUtils.map(linesBer, line -> SubfieldUtils.getContentOfFirstSubfield(line, '9'));
      for (final String id : berufIDNs) {
        if (musikberufe.contains(id))
          return true;
      }
    }
    return GNDUtils.containsGNDClassification(record, "14.4p");
  }

  /**
   * @param args
   * @throws IllFormattedLineException
   * @throws OperationNotSupportedException
   * @throws IOException
   */
  public static void main1(final String[] args)
    throws IllFormattedLineException,
    OperationNotSupportedException,
    IOException {
    final Record record = RecordUtils.readFromClip();
    System.out.println(getLebensdaten(record));
    System.out.println(getWirkungsdaten(record));
  }

  /**
   * @param args
   * @throws IllFormattedLineException
   * @throws OperationNotSupportedException
   * @throws IOException
   */
  public static void main(final String[] args)
    throws IllFormattedLineException,
    OperationNotSupportedException,
    IOException {
    final Record record = RecordUtils.readFromClip();
    final Line line100 = RecordUtils.getTheOnlyLine(record, "100");
    System.out.println(getName((GNDPersonLine) line100, false));
    System.out.println(getName((GNDPersonLine) line100, true));
  }

  /**
   * Gibt die IDNs (ohne Prüfziffer) aller Berufe einer Person,
   * aus allen 550-Zeilen, deren $4 "ber#" lautet. Eventuell mehrfach,
   * da Liste!
   *
   * @param record  nicht null
   * @return      nicht null
   */
  public static List<Integer> getProfessionIdns(final Record record) {
    return RecordUtils.extractIDNints(PersonUtils.getProfessions(record));
  }

  /**
   * Gibt die IDNs (ohne Prüfziffer) aller Instrumente einer Person,
   * aus allen 550-Zeilen, deren $4 "istr" lautet.
   *
   * @param record  nicht null
   * @return      nicht null
   */
  public static List<Integer> getIstrIdns(final Record record) {
    return RecordUtils.extractIDNints(PersonUtils.getInstruments(record));
  }

  /**
   * Gibt alle Berufe einer Person, also alle 550-Zeilen, deren $4 "ber#" lautet.
   *
   * @param record  nicht null
   * @return      nicht null
   */
  public static List<Line> getProfessions(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<Line> relSach = RecordUtils.getLines(record, "550");
    return GNDUtils.getLinesWithDollar4(relSach, "ber");
  }

  /**
   * Gibt alle Instrumente einer Person, also alle 550-Zeilen, deren $4 "istr" lautet.
   *
   * @param record  nicht null
   * @return      nicht null
   */
  public static List<Line> getInstruments(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<Line> relSach = RecordUtils.getLines(record, "550");
    return GNDUtils.getLinesWithDollar4(relSach, "istr");
  }

  private static final Multiset<String> AD = new Multiset<>("a", "d");
  private static final Multiset<String> AC = new Multiset<>("a", "c");
  private static final Multiset<String> ACD = new Multiset<>("a", "c", "d");
  private static final Multiset<String> ADL = new Multiset<>("a", "d", "l");
  private static final Multiset<String> ADLT = new Multiset<>("a", "d", "l_terr");
  private static final Multiset<String> ACDL = new Multiset<>("a", "c", "d", "l");
  private static final Multiset<String> ADN = new Multiset<>("a", "d", "n");
  private static final Multiset<String> ACDN = new Multiset<>("a", "c", "d", "n");
  private static final Multiset<String> P = new Multiset<>("P");
  private static final Multiset<String> PN = new Multiset<>("P", "n");
  private static final Multiset<String> PL = new Multiset<>("P", "l");
  private static final Multiset<String> AL = new Multiset<>("a", "l");
  private static final Multiset<String> PLN = new Multiset<>("P", "l", "n");
  private static final Multiset<String> P_LT_L = new Multiset<>("P", "l_terr");
  private static final Multiset<String> P_LT_LN = new Multiset<>("P", "l_terr", "n");
  private static final Multiset<String> P_LF = new Multiset<>("P", "l_fam");
  private static final Multiset<String> P_LF_C = new Multiset<>("P", "l_fam", "c");
  private static final Multiset<String> PC = new Multiset<>("P", "c");

  /**
   * Versucht den Namen zu erraten, wie er umgangssprachlich gebraucht wird. Also:
   * <li> Karl Müller
   * <li> Kaiser Wilhelm I. (d.h. ohne Territorium für Kaiser, Pharaonen und Sultane)
   * <li> König Bernardo von Italien (sonst)
   *
   *
   * @param line100 nicht null
   * @param defensiv Kein Territorium
   *
   * @return  den erratenen Namen nach deutscher Konvention
   */
  public static String getName(final GNDPersonLine line100, final boolean defensiv) {
    final List<Subfield> subs = SubfieldUtils.getNamingRelevantSubfields(line100);
    final Multiset<String> signatur = new Multiset<>();
    for (final Subfield sub : subs) {
      final String cont = sub.getContent();
      final char indC = sub.getIndicator().indicatorChar;
      if (indC != 'l')
        signatur.add(Character.toString(indC));
      else {
        if (cont.contains(", "))
          signatur.add("l_terr");
        else if (cont.contains(" : "))
          signatur.add("l_fam");
        else
          signatur.add(Character.toString(indC));
      }
    }

    // In $8 wird führendes P nicht richtig dargestellt:
    final boolean expansionFalsch = signatur.contains("a")
      && (signatur.contains("l") || signatur.contains("l_terr") || signatur.contains("l_fam"))
      && !signatur.contains("P");
    if (expansionFalsch) {
      signatur.remove("a");
      signatur.add("P");
      try {
        final Subfield dollarP =
          new Subfield(line100.getTag(), "$P" + line100.getFamilyName().getContent());
        line100.setPersonalName(dollarP);
      } catch (final IllFormattedLineException e) {
        e.printStackTrace();
      }
      line100.setFamilyName(null);
    }

    // ADL auch??:
    if (signatur.equals(AD) || signatur.equals(ADL) || signatur.equals(ADLT)) {
      return line100.getFirstName().getContent() + " " + line100.getFamilyName().getContent();
    }
    // ACDL auch??:
    if (signatur.equals(ACD) || signatur.equals(ACDL)) {
      return line100.getFirstName().getContent() + " " + line100.getPrefix().getContent() + " "
        + line100.getFamilyName().getContent();
    }
    // Überhaupt korrekt angesetzt?:
    if (signatur.equals(AC)) {
      return line100.getPrefix().getContent() + " " + line100.getFamilyName().getContent();
    }
    if (signatur.equals(ADN)) {
      return line100.getFirstName().getContent() + " " + line100.getFamilyName().getContent() + " "
        + line100.getZaehlung().getContent();
    }
    if (signatur.equals(ACDN)) {
      return line100.getFirstName().getContent() + " " + line100.getPrefix().getContent() + " "
        + line100.getFamilyName().getContent() + " " + line100.getZaehlung().getContent();
    }
    if (signatur.equals(PL)) {
      return line100.getPersonalName().getContent() + " (" + line100.getTerritorium().getContent()
        + ")";
    }
    if (signatur.equals(P)) {
      return line100.getPersonalName().getContent();
    }
    if (signatur.equals(PC)) {
      return line100.getPrefix().getContent() + " " + line100.getPersonalName().getContent();
    }
    if (signatur.equals(PLN) || signatur.equals(PN)) {
      return line100.getPersonalName().getContent() + " " + line100.getZaehlung().getContent();
    }
    if (signatur.contains("l_terr")) {
      final String terrPlusTitel = line100.getTerritorium().getContent();
      final String[] teile = terrPlusTitel.split(", ", 2);
      final String terr = teile[0];
      final String titel = teile[1];
      if (signatur.equals(P_LT_L)) {
        String name = titel + " " + line100.getPersonalName().getContent();
        if (defensiv || titel.equals("Kaiser") || titel.equals("Imperator")
          || titel.equals("Sultan") || titel.equals("Pharao")) {
          return name;
        }
        name += (terr.contains("von") ? " " : " von ") + terr;
        return name;
      }
      if (signatur.equals(P_LT_LN)) {
        String name = titel + " " + line100.getPersonalName().getContent() + " "
          + line100.getZaehlung().getContent();
        if (defensiv || titel.equals("Kaiser") || titel.equals("Imperator")
          || titel.equals("Sultan") || titel.equals("Pharao"))
          return name;
        name += (terr.contains("von") ? " " : " von ") + terr;
        return name;
      }
    }
    // Nicht immer sauber:
    if (signatur.contains("l_fam")) {
      final String terrPlusTitel = line100.getTerritorium().getContent();
      final String[] teile = terrPlusTitel.split(" : ", 2);
      final String fam = teile[0];
      final String rest = teile[1];
      if (signatur.equals(P_LF)) {
        return fam + " " + line100.getPersonalName().getContent() + " (" + rest + ")";
      }
      if (signatur.equals(P_LF_C)) {
        return fam + " " + line100.getPrefix().getContent() + " "
          + line100.getPersonalName().getContent() + " (" + rest + ")";
      }
    }

    // Notlösung:
    return StringUtils.concatenate(" ", FilterUtils.mapNullFiltered(subs, Subfield::getContent));

  }
}
