/**
 *
 */
package de.dnb.gnd.utils;

import static de.dnb.gnd.utils.GNDUtils.assertGNDRecord;
import static de.dnb.gnd.utils.RecordUtils.getLines;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.applicationComponents.tuples.Triplett;
import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.TimeUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

/**
 * Enthält spezielle Hilfsfunktionen für die Satzart Tu.
 *
 * @author baumann
 *
 */
public final class WorkUtils {

  private static final GNDTagDB TAG_DB = GNDTagDB.getDB();

  private static final Tag TAG_130 = GNDTagDB.TAG_130;

  public static final List<String> ALLOWED_TAGS = Arrays.asList("130", "430");

  private WorkUtils() {
    super();
  }

  /**
   *
   * @param record    nicht null
   * @return          überprüft auf Tu*
   */
  public static boolean isWork(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    return (RecordUtils.getDatatypeCharacterAt(record, 0) == 'T'
      && RecordUtils.getDatatypeCharacterAt(record, 1) == 'u');
  }

  /**
   * Gibt die Zeile mit dem Titel des Werkes.
   *
   * @param record	nicht null.
   * @return			Den Titel. Wenn keiner vorhanden wird eine
   * 					IllegalStateException geworfen.
   */
  public static Line getTitleLine(final Record record) {
    final Line titleLine = RecordUtils.getTheOnlyLine(record, TAG_130);
    if (titleLine == null)
      throw new IllegalStateException("Keine 130.");
    else
      return titleLine;
  }

  /**
   * Gibt den Titel des Werkes im Pica3-Format mit Unterfeldern.
   * Kommentare werden vorher entfernt.
   *
   * @param record	nicht null.
   * @return			Den Titel. Wenn keiner vorhanden wird eine
   * 					IllegalStateException geworfen. Unicode-Composition.
   */
  public static String getNormedTitle(final Record record) {
    final Line titleLine = getTitleLine(record);
    return normalizeTitelLine(titleLine);
  }

  /**
   * Gibt den Titel des Werkes im Pica3-Format, wie er in $8
   * angezeigt würde.
   *
   * @param record	nicht null.
   * @return			Den Titel. Wenn keiner vorhanden ist oder mehrere
   * 					erste Autoren vorhanden sind, wird eine
   * 					{@link IllegalStateException} geworfen. Unicode-Composition.
   */
  public static String getExpansionTitle(final Record record) {
    String title = getNormedTitle(record);
    final Line authorLine = getAuthorLine(record);
    if (authorLine != null) {
      final String nameOfRelatedRecord = GNDUtils.getNameOfRelatedRecord(authorLine);
      if (nameOfRelatedRecord != null)
        title = nameOfRelatedRecord + "$a" + title;
    }
    return title;

  }

  /**
   * Gibt den Titel als String ohne störende Unterfelder $4, $5, $v.
   *
   * @param line	nicht null, als GNDTag nur 130 und 430 zugelassen.
   * @return		Titel im Pica3-Format. Kann $-Zeichen enthalten. Unicode-Composition.
   */
  public static String normalizeTitelLine(final Line line) {
    final List<Subfield> subfields = SubfieldUtils.getNamingRelevantSubfields(line);
    return RecordUtils.toPicaWithoutTag(TAG_130, subfields, Format.PICA3, false, '$');
  }

  /**
   * Liefert eine Liste von Unterfeldern aus dem Werktitel ohne
   * störende Unterfelder $4, $5, $v.
   *
   * @param 	record	nicht null.
   * @return			Liste.
   */
  public static List<Subfield> normalizedSubfields(final Record record) {
    GNDUtils.assertGNDRecord(record);
    final Line line = getTitleLine(record);
    return SubfieldUtils.getNamingRelevantSubfields(line);
  }

  /**
   * Gibt die Zeile mit dem einzigen ersten Verfasser in 5XX.
   *
   * @param record	nicht null.
   * @return			Den einzigen ersten Verfasser, Künstler oder
   * 					Komponisten, null bei anonymen Werken.
   * 					Sollten mehrere erste Verfasser existieren, wird eine
   * 					{@link IllegalStateException} geworfen.
   */
  public static Line getAuthorLine(final Record record) {
    GNDUtils.assertGNDRecord(record);
    final Collection<Tag> gndTags = TAG_DB.getRelatedTag5XX();
    final List<Line> lines = RecordUtils.getLines(record, WorkUtils::isAuthorLine, gndTags);
    if (lines.isEmpty())
      return null;
    else if (lines.size() != 1)
      throw new IllegalStateException("Mehrere erste Autoren in idn: " + record.getId());
    else {
      return lines.get(0);
    }
  }

  /**
   * Gibt die idn des einzigen ersten Verfassers.
   *
   * @param record	nicht null.
   * @return			Die idn des einzigen ersten Verfassers, Künstlers oder
   * 					Komponisten, sofern relationiert. Ansonsten null.
   * 					Sollten mehrere erste Verfasser existieren, wird eine
   * 					IllegalStateException geworfen.
   */
  public static String getAuthorID(final Record record) {
    GNDUtils.assertGNDRecord(record);
    final Line line = getAuthorLine(record);
    if (line == null)
      return null;
    else {
      return line.getIdnRelated();
    }
  }

  /**
   *
   * @param record  Werkdatensatz, sonst Exception
   * @return        (Datum aus undokumentiertem(!) Feld $E oder null, Datum ist unsicher).
   *                <br>Das Datum ist auch null, wenn es nicht geparst werden kann.
   *                <br>Die Unsicherheit ergibt sich aus dem Vorhandensein eines 'X' im Datum.
   */
  public static Pair<LocalDate, Boolean> getAutorGeburtsdatum(final Record record) {
    GNDUtils.assertGNDRecord(record);
    final Line line = getAuthorLine(record);
    if (line == null) {
      return new Pair<>(null, false);
    }
    final String sub = SubfieldUtils.getContentOfFirstSubfield(line, 'E');
    final boolean geburtUnsicher = StringUtils.contains(sub, "X", true);
    return new Pair<>(TimeUtils.localDateFrom548(sub, false), geburtUnsicher);
  }

  /**
  *
  * @param record  Werkdatensatz, sonst Exception
  * @return        (Datum aus undokumentiertem(!) Feld $G oder null, Datum ist unsicher)
  *                <br>Das Datum ist auch null, wenn es nicht geparst werden kann.
  *                <br>Die Unsicherheit ergibt sich aus dem Vorhandensein eines 'X' im Datum.
  */
  public static Pair<LocalDate, Boolean> getAutorSterbedatum(final Record record) {
    GNDUtils.assertGNDRecord(record);
    final Line line = getAuthorLine(record);
    if (line == null) {
      return new Pair<>(null, false);
    }
    final String sub = SubfieldUtils.getContentOfFirstSubfield(line, 'G');
    final boolean todUnsicher = StringUtils.contains(sub, "X", true);
    return new Pair<>(TimeUtils.localDateFrom548(sub, true), todUnsicher);
  }

  /**
   *
   * @param record  nicht null
   * @return        (Lebensdaten, Geburt unsicher, Tod unsicher)
   * ,              <br>Lebensdaten, sofern die Felder $E und $G vorhanden sind,
   *                sonst das maximale Intervall.
   *                <br> Geburt/Tod unsicher, wenn 'X' enthalten
   */
  public static Triplett<Between<LocalDate>, Boolean, Boolean> getAutorLebensdaten(
    final Record record) {
    final Pair<LocalDate, Boolean> gebPair = getAutorGeburtsdatum(record);
    LocalDate geb = gebPair.first;
    if (geb == null)
      geb = LocalDate.MIN;
    final Pair<LocalDate, Boolean> todPair = getAutorSterbedatum(record);
    LocalDate tod = todPair.first;
    if (tod == null)
      tod = LocalDate.MAX;
    return new Triplett<>(new Between<>(geb, tod), gebPair.second, todPair.second);
  }

  /**
  * Geschätzte Wirkungsdaten. Wenn überhaupt keine Lebensdaten vorliegen, wird
  * das maximale Intervall zurückgegeben. Sonst wird der Beginn der Schaffenszeit
  * mit dem 5. Lebensjahr vermutet. Liegen entweder Geburts- oder Todesdatum
  * nicht vor, wird die
  * maximale Schaffenzeit auf 95 Jahre geschätzt.
  *
  * @param record  nicht null
  * @return        Die Wirkungsdaten, sofern die Felder $E und $G vorhanden sind,
  *                sonst das maximale Intervall.
  */
  public static Between<LocalDate> getAutorWirkungsdaten(final Record record) {
    final Pair<LocalDate, Boolean> gebPair = getAutorGeburtsdatum(record);
    final Pair<LocalDate, Boolean> todPair = getAutorSterbedatum(record);

    final boolean geburtUnsicher = gebPair.second;
    final boolean todUnsicher = todPair.second;
    final LocalDate geburt = gebPair.first;
    final LocalDate tod = todPair.first;
    return PersonUtils.getWirkungsdaten(geburt, tod, geburtUnsicher, todUnsicher);

  }

  /**
   * Test auf $4datj.
   */
  static final Predicate<Line> predicateErscheinung = new Predicate<Line>() {
    String dollar4 = "datj";

    @Override
    public boolean test(final Line line) {
      return GNDUtils.testDollar4(line, dollar4);
    }
  };

  /**
   * Da fast jedes der Felder 5XX einen Autor enthalten kann, empfielt es sich,
   * den Tag der zurückgegebenen Zeile auszuwerten.
   *
   * @param line  nicht null
   * @return Zeile enthält "aut1", "kom1" oder "kue1".
   */
  public static boolean isAuthorLine(final Line line) {
    if (SubfieldUtils.containsIndicator(line, '4')) {
      final Subfield subfield4 = SubfieldUtils.getFirstSubfield(line, '4');
      final String subCont = subfield4.getContent();
      return subCont.equals("aut1") || subCont.equals("kom1") || subCont.equals("kue1");
    } else
      return false;
  }

  /**
   * Liefert Zeile mit Erscheinungsdaten (548 ... $4datj).
   *
   * @param record  nicht null
   * @return      null, wenn keine vorhanden, sonst die erste Zeile
   */
  public static Line getErscheinungsLine(final Record record) {
    Objects.requireNonNull(record);
    final ArrayList<Line> lines548 =
      RecordUtils.getLines(record, predicateErscheinung, GNDTagDB.TAG_548);
    if (lines548.isEmpty())
      return null;
    else
      return lines548.get(0);
  }

  /**
   * Liefert Zeile mit Erstellungsdaten (548 ... $4dats).
   *
   * @param record  nicht null
   * @return      null, wenn keine vorhanden, sonst die erste Zeile
   */
  public static Line getErstellungsLine(final Record record) {
    Objects.requireNonNull(record);
    final ArrayList<Line> lines548 =
      RecordUtils.getLines(record, GNDUtils.predicateErstellung, GNDTagDB.TAG_548);
    if (lines548.isEmpty())
      return null;
    else
      return lines548.get(0);
  }

  /**
   *
   * @param record  nicht null
   * @return        Das Erstellungsdatum, sofern vorhanden, sonst das
   *                Erscheinungsdatum. Alle Daten müssen in die Wirkungsspanne
   *                des Autors fallen. Sonst wird die Wirkungsspanne zurückgegeben,
   *                die bei fehlender Information das maximale Intervall
   *                {@link TimeUtils#MAX_INTERVAL}
   *                umfassen kann.
   */
  public static Between<LocalDate> getErstellungsDaten(final Record record) {
    final Between<LocalDate> wirkungsdaten = getAutorWirkungsdaten(record);
    Between<LocalDate> erstellDaten;
    // 1. Versuch
    Line line548 = getErstellungsLine(record);
    if (line548 != null) {
      erstellDaten = GNDUtils.getDaten(line548).first;
      if (wirkungsdaten.intersects(erstellDaten))
        return erstellDaten.getIntersection(wirkungsdaten);
      else {
        if (diffOK(wirkungsdaten, erstellDaten)) {
          return erstellDaten;
        }
      }
    }
    // 2. Versuch:
    line548 = getErscheinungsLine(record);
    if (line548 != null) {
      erstellDaten = GNDUtils.getDaten(line548).first;
      if (wirkungsdaten.intersects(erstellDaten))
        return erstellDaten.getIntersection(wirkungsdaten);
      else {
        if (diffOK(wirkungsdaten, erstellDaten)) {
          return erstellDaten;
        }
      }
    }
    // 3. Versuch:
    try {

      final String title = getNormedTitle(record);
      final Pattern pattern = Pattern.compile("\\((\\d\\d\\d\\d)\\)");
      final Matcher matcher = pattern.matcher(title);
      if (matcher.find()) {
        final String jahr = matcher.group(1);
        final LocalDate datumL = TimeUtils.localDateFrom548(jahr, false);
        if (datumL != null) {
          final LocalDate datumH = TimeUtils.localDateFrom548(jahr, true);
          erstellDaten = new Between<>(datumL, datumH);
          if (wirkungsdaten.intersects(erstellDaten))
            return erstellDaten.getIntersection(wirkungsdaten);
          else {
            if (diffOK(wirkungsdaten, erstellDaten)) {
              return erstellDaten;
            }
          }
        }

      }
    } catch (final IllegalStateException e) {

    }
    return wirkungsdaten;
  }

  /**
   * @param lebensdaten
   * @param erstellDaten
   * @return              Erstellungsende - Tod < {@link PersonUtils#MAX_NACH_TOD}
   */
  private static
    boolean
    diffOK(final Between<LocalDate> lebensdaten, final Between<LocalDate> erstellDaten) {
    final int lebensende = lebensdaten.higherBound.getYear();
    final int erstellungsende = erstellDaten.higherBound.getYear();
    final int diff = erstellungsende - lebensende;
    return diff >= 0 && diff <= PersonUtils.MAX_NACH_TOD;
  }

  /**
   * Liefert eine Liste der alten Werktitel ohne Komponisten aus der 913.
   * <br><br>
   * Die 913 enthält in der Regel eine Boilerplate
   * 	"913 $Sswd$ipt$a".
   * Dann folgt der eventuelle Komponist, gefolgt von
   * 	", ",
   * danach der Titel, danach die alte nid
   * 	"$04425694-2".
   *
   * Also z.B.
   * <br><br>913 $Sswd$it$aMagnus liber <Musik>$04759556-5
   * @param record	nicht null.
   * @return Liste der Werktitel, eventuell leer.
   */
  public static ArrayList<String> getOriginalTitles(final Record record) {
    GNDUtils.assertGNDRecord(record);
    final List<String> headings = GNDUtils.getOriginalHeadings(record);
    final ArrayList<String> titles = new ArrayList<>();
    for (final String heading : headings) {
      final String[] parts = heading.split(": ");
      if (parts.length == 2) {
        titles.add(parts[1].replace("{", ""));
      } else
        titles.add(heading.replace("{", ""));
    }
    return titles;
  }

  /**
   *
   * @param record    nicht null
   * @return          ist Werk und hat wim oder wif
   */
  public static boolean isMusicalWork(final Record record) {
    return GNDUtils.containsEntityType(record, "wim") || GNDUtils.containsEntityType(record, "wif");
  }

  public static boolean isMusicalExpression(final Record record) {
    if (isExpression(record)) {
      final List<String> cls = GNDUtils.getGNDClassifications(record);
      return StringUtils.containsPrefix(cls, "14.");
    } else
      return false;
  }

  /**
  * "Normale" Werke (wit), meist literarisch oder Gesetze.
  *
  * @param record    nicht null
  * @return          ist Werk und hat wit
  */
  public static boolean isNormalWork(final Record record) {
    return GNDUtils.containsEntityType(record, "wit");
  }

  /**
   * Expression (wie), auch für musikalische Werke.
   *
   * @param record    nicht null
   * @return          ist Werk und hat wit
   */
  public static boolean isExpression(final Record record) {
    return GNDUtils.containsEntityType(record, "wie");
  }

  /**
   * Sammlung (win).
   *
   * @param record    nicht null
   * @return          ist Werk und hat wit
   */
  public static boolean isSammlung(final Record record) {
    return GNDUtils.containsEntityType(record, "win");
  }

  /**
   * Schriftdenkmal (wis).
   *
   * @param record    nicht null
   * @return          ist Werk und hat wit
   */
  public static boolean isSchriftdenkmal(final Record record) {
    return GNDUtils.containsEntityType(record, "wis");
  }

  /**
   * Liefert die 380-Felder, wenn ein Werk vorliegt.
   *
   * @param record    nicht null und GND-Datensatz.
   * @return          nicht null, modifizierbar.
   */
  public static Collection<Line> getLines380(final Record record) {
    if (!isWork(record))
      return Collections.emptyList();
    return getLines(record, "380");
  }

  /**
   * Liefert die 382-Felder, wenn ein Werk vorliegt.
   *
   * @param record    nicht null und GND-Datensatz.
   * @return          nicht null.
   */
  public static Collection<Line> getLines382(final Record record) {
    if (!isWork(record))
      return Collections.emptyList();
    return getLines(record, "382");
  }

  /**
   * 
   * @param record	nicht null
   * @return		Tonart in 384
   */
  public static String getKey(final Record record) {
    return RecordUtils.getContentOfSubfield(record, "384", 'a');
  }

  /**
   * Gibt die IDNs (ohne Prüfziffer) aller Instrumente eines Werkes (Feld 382).
   *
   * @param record  nicht null
   * @return      nicht null
   */
  public static List<Integer> getIstrIdns(final Record record) {
    return getLines382(record).stream()
      .map(line -> SubfieldUtils.getContentOfFirstSubfield(line, '9'))
      .filter(IDNUtils::isKorrektePPN).map(IDNUtils::ppn2int).collect(Collectors.toList());
  }

  /**
   *
   * @param record    nicht null
   * @return          enthält Form des Werkes in 380?
   */
  public static boolean contains380(final Record record) {
    assertGNDRecord(record);
    return !getLines380(record).isEmpty();
  }

  /**
   * Liefert die idns der 380-Felder, wenn ein Werk vorliegt.
   *
   * @param record    nicht null und GND-Datensatz.
   * @return          nicht null.
   */
  public static Collection<String> get380IDNs(final Record record) {
    if (!isWork(record))
      return Collections.emptyList();
    return RecordUtils.getContentsOfAllSubfields(record, "380", '9');
  }

  /**
   * Gibt alle partitiven Oberbegriffe, also alle 530-Zeilen, deren
   * $4 "obpa" lautet.
   *
   * @param record  nicht null
   * @return      nicht null
   */
  public static List<Line> getPartitiveOBB(final Record record) {
    RangeCheckUtils.assertReferenceParamNotNull("record", record);
    final List<Line> obb = RecordUtils.getLines(record, "530");
    return GNDUtils.getLinesWithDollar4(obb, "obpa");
  }

  /**
   * @param args
   * @throws IOException
   */
  public static void main2(final String[] args) throws IOException {
    final RecordReader reader = RecordReader.getMatchingReader(Constants.Tu);
    reader.forEach(record ->
    {
      try {
        //        getAutorWirkungsdaten(record);
        System.out.println(getAuthorLine(record));
        System.out.println(getAutorLebensdaten(record));
        System.out.println(getAutorWirkungsdaten(record));
        System.out.println();
      } catch (final Exception e) {

      }

    });
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
    System.out.println(isNormalWork(record));
  }

}
