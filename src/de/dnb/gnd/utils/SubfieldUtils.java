package de.dnb.gnd.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.TimeUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineFactory;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;

/**
 * Enthält die Utilities, die mit Unterfeldern und Zeilen zu tun haben.
 * @author baumann
 *
 */
public final class SubfieldUtils {

  private SubfieldUtils() {
  }

  public static final Function<Subfield, Character> FUNCTION_SUBFIELD_TO_INDICATOR_CHAR =
    subfield -> subfield.getIndicator().indicatorChar;

  public static final Function<Indicator, Character> FUNCTION_INDICATOR_TO_CHAR =
    indicator -> indicator.indicatorChar;

  public static final Function<Subfield, String> FUNCTION_SUBFIELD_TO_CONTENT =
    subfield -> subfield.getContent();

  public static
    boolean
    containsIndicator(final char indicator, final Collection<Indicator> indicators) {
    return FilterUtils.contains(indicators, element -> element.indicatorChar == indicator);
  }

  /**
   * Ist der Indikator ind in der Zeile enthalten?
   *
   * @param line	nicht null.
   * @param ind	beliebig.
   * @return		true, wenn in line enthalten.
   */
  public static boolean containsIndicator(final Line line, final char ind) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    return SubfieldUtils.getFirstSubfield(line, ind) != null;
  }

  /**
   * Sind die Indikatoren inds in der Zeile enthalten?
   *
   * @param line  nicht null.
   * @param inds auch leer.
   * @return    true, wenn in line enthalten.
   */
  public static boolean containsIndicators(final Line line, final char... inds) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    for (final char ind : inds) {
      if (!containsIndicator(line, ind))
        return false;
    }
    return true;
  }

  //@formatter:off
	/**
	 * enthält die Zeile ein Unterfeld mit Indikator indicator?
	 * @param line		nicht null.
	 * @param indicator	nicht null.
	 * @return			Enthalten?
	 */
	public static boolean containsIndicator(
		final Line line,
		final Indicator indicator) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
		return
			getFirstSubfield(line.getSubfields(Format.PICA3), indicator)
				!= null;
	}

	public static boolean containsIndicatorInSubfields(
		final char indicator,
		final Collection<Subfield> subfields) {
		final Predicate<Subfield> predicate = new Predicate<Subfield>() {
			@Override
			public boolean test(final Subfield element) {
				return element.getIndicator().indicatorChar == indicator;
			}
		};
		return FilterUtils.contains(subfields, predicate);
	}

	/**
	 * Liefert den Inhalt des ersten Subfelds mit Indikator ind, sonst null.
	 * @param line	nicht null.
	 * @param ind	beliebig.
	 * @return		Inhalt des ersten Unterfelds oder null.
	 */
	public static String getContentOfFirstSubfield(
		final Line line,
		final char ind) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		final Subfield subfield = getFirstSubfield(line, ind);
		if (subfield != null)
			return subfield.getContent();
		else
			return null;
	}

	//@formatter:on
  /**
   * Liefert zu einer Menge von Zeilen und einem Indikator alle Inhalte
   * der ersten Unterfelder zu diesem Indikator, die NICHT null sind. Ergebnis kann als Spalte
   * einer Tabelle aufgefasst werden.
   *
   * @param lines		nicht null.
   * @param indicator	beliebig.
   *
   * @return			nicht null, eventuell leer, veränderbar.
   */
  public static
    List<String>
    getContentsOfFirstSubfields(final Iterable<Line> lines, final char indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
    final Collection<Subfield> subfields = RecordUtils.getFirstSubfields(lines, indicator);
    return SubfieldUtils.getContentsOfSubfields(subfields);
  }

  /**
   * Liefert zu einer Menge von Unterfeldern alle
   * Inhalte der Unterfelder (veränderbar).
   *
   * @param subfields		nicht null.
   * @return				nicht null, aber eventuell leer.
   */
  public static List<String> getContentsOfSubfields(final Iterable<Subfield> subfields) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    return FilterUtils.map(subfields, SubfieldUtils.FUNCTION_SUBFIELD_TO_CONTENT);
  }

  /**
   * Liefert zu Zeile alle Inhalte der in der Zeile enthaltenen Unterfelder.
   *
   * @param line			nicht null.
   * @return				nicht null, aber eventuell leer, modifizierbar.
   */
  public static List<String> getContentsOfSubfields(final Line line) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    final Collection<Subfield> subfields = line.getSubfields();
    return getContentsOfSubfields(subfields);
  }

  /**
   * Liefert das erste Subfeld mit Indikator ind, sonst null.
   * @param subfields	nicht null.
   * @param ind		beliebig.
   * @return			Erstes Unterfeld oder null.
   */
  public static Subfield getFirstSubfield(final Iterable<Subfield> subfields, final char ind) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    final Predicate<Subfield> predicate = new Predicate<Subfield>() {
      @Override
      public boolean test(final Subfield subfield) {
        return subfield.getIndicator().indicatorChar == ind;
      }
    };
    return FilterUtils.find(subfields, predicate);
  }

  /**
   * Liefert den Inhalt des ersten Subfeld mit Indikator ind, sonst null.
   * @param subfields	nicht null.
   * @param ind		beliebig.
   * @return			Inhalt des ersten Unterfeld oder null.
   */
  public static
    String
    getContentOfFirstSubfield(final Iterable<Subfield> subfields, final char ind) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    final Subfield subfield = getFirstSubfield(subfields, ind);
    if (subfield == null)
      return null;
    else
      return subfield.getContent();
  }

  /**
   * Liefert das erste Subfeld mit Indikator indicator, sonst null.
   * @param subfields	nicht null.
   * @param indicator	nicht null.
   * @return			Erstes Unterfeld oder null.
   */
  public static
    Subfield
    getFirstSubfield(final Iterable<Subfield> subfields, final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    final Predicate<Subfield> predicate = new Predicate<Subfield>() {
      @Override
      public boolean test(final Subfield subfield) {
        return subfield.getIndicator() == indicator;
      }
    };
    return FilterUtils.find(subfields, predicate);
  }

  /**
   * Liefert das erste Subfeld mit Indikator ind, sonst null.
   * @param line	nicht null.
   * @param ind	beliebig.
   * @return		Erstes Unterfeld oder null.
   */
  public static Subfield getFirstSubfield(final Line line, final char ind) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    final Collection<Subfield> subfields = line.getSubfields();
    return SubfieldUtils.getFirstSubfield(subfields, ind);
  }

  /**
   * Liefert das erste Subfeld mit Indikator indicator, sonst null.
   * @param line	nicht null.
   * @param indicator	nicht null.
   * @return			Erstes Unterfeld oder null.
   */
  public static Subfield getFirstSubfield(final Line line, final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    RangeCheckUtils.assertReferenceParamNotNull("indicator", indicator);
    return getFirstSubfield(line.getSubfields(Format.PICA3), indicator);
  }

  /**
   * Liefert zu einer Zeile und einem Indikator alle
   * Unterfelder mit diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param line		nicht null.
   * @param indicator	beliebig.
   *
   * @return			Neue Liste, nicht null, modifizierbar.
   */
  public static List<Subfield> getSubfields(final Line line, final char indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    return SubfieldUtils.getSubfieldsFromCollection(line.getSubfields(), indicator);
  }

  /**
   * Liefert zu einer Zeile und einem Indikator alle
   * Unterfelder mit diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param line		nicht null.
   * @param indicator	beliebig.
   *
   * @return			Neue Liste, nicht null, modifizierbar.
   */
  public static List<String> getContentsOfSubfields(final Line line, final char indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    return getContentsOfSubfields(getSubfields(line, indicator));
  }

  /**
   * Liefert zu einer Menge von Unterfeldern und einem Indikator alle
   * Unterfelder zu diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param subfields		nicht null.
   * @param indicator		beliebig.
   *
   * @return				Neue Liste, nicht null.
   */
  public static
    List<Subfield>
    getSubfieldsFromCollection(final Iterable<Subfield> subfields, final char indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    final Predicate<Subfield> predicate = new Predicate<Subfield>() {
      @Override
      public boolean test(final Subfield subfield) {
        return subfield.getIndicator().indicatorChar == indicator;
      }
    };
    return FilterUtils.newFilteredList(subfields, predicate);
  }

  /**
   * Liefert eine Liste von Unterfeldern aus einer Zeile ohne
   * störende Unterfelder $4, $5, $v ...
   *
   * @param 	line	nicht null.
   * @return			nicht null.
   */
  public static List<Subfield> getNamingRelevantSubfields(final Line line) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    List<Subfield> subfields = line.getSubfields();
    subfields = getRelevanteUnterfelder(subfields);
    return subfields;
  }

  /**
   * Liefert eine Liste von Unterfeldern aus einer Zeile ohne
   * störende Unterfelder $4, $5, $v ...
   *
   * @param   subfields nicht null.
   * @return      nicht null.
   */
  public static List<Subfield> getRelevanteUnterfelder(List<Subfield> subfields) {
    final List<Character> irrelevantIndicators =
      Arrays.asList('4', '5', 'v', 'X', 'Y', 'Z', 'L', 'u', 'S', '0', '2', 'T', 'U', '8', '9');
    subfields = SubfieldUtils.removeSubfieldsFromCollection(subfields, irrelevantIndicators);
    return subfields;
  }

  /**
   * Entfernt aus  einer Menge von Unterfeldern und einem Indikator alle
   * Unterfelder zu diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param subfields		nicht null.
   * @param indicator		beliebig.
   *
   * @return				Neue Liste, nicht null.
   */
  public static
    List<Subfield>
    removeSubfieldFromCollection(final Iterable<Subfield> subfields, final Indicator indicator) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    final Predicate<Subfield> predicate = new Predicate<Subfield>() {
      @Override
      public boolean test(final Subfield subfield) {
        return subfield.getIndicator() != indicator;
      }
    };
    return FilterUtils.newFilteredList(subfields, predicate);
  }

  /**
   * Entfernt aus einer Zeile und einem Indikator alle
   * Unterfelder mit diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param line			nicht null.
   * @param indicators	beliebig.
   *
   * @return			 	nicht null.
   */
  public static List<Subfield> removeSubfields(final Line line, final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    return removeSubfieldsFromCollection(line.getSubfields(), indicators);
  }

  /**
   * Entfernt aus  einer Menge von Unterfeldern und einem Indikator alle
   * Unterfelder zu diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param subfields		nicht null.
   * @param indicators		beliebig.
   *
   * @return				Neue Liste, nicht null.
   */
  public static List<Subfield> removeSubfieldsFromCollection(
    final Iterable<Subfield> subfields,
    final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    final List<Character> indicatorCollection = Arrays.asList(indicators);
    return SubfieldUtils.removeSubfieldsFromCollection(subfields, indicatorCollection);
  }

  /**
   * Entfernt aus  einer Menge von Unterfeldern und einem Indikator alle
   * Unterfelder zu diesem Indikator. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param subfields		nicht null.
   * @param indicators	nicht null.
   *
   * @return				Neue Liste, nicht null.
   */
  public static List<Subfield> removeSubfieldsFromCollection(
    final Iterable<Subfield> subfields,
    final Collection<Character> indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    final Predicate<Subfield> predicate = new Predicate<Subfield>() {
      @Override
      public boolean test(final Subfield subfield) {
        return !indicators.contains(subfield.getIndicator().indicatorChar);
      }
    };
    return FilterUtils.newFilteredList(subfields, predicate);
  }

  /**
   * Gibt eine Zeile mit ersetzten Unterfeldern oder null. Für die
   * Ersetzung der Unterfelder ist die equals()-Relation massgeblich.
   *
   * @param line			nicht null.
   * @param original		nicht null.
   * @param replacement	nicht null.
   * @return				Neue Zeile oder die alte, wenn nichts
   * 						ersetzt wurde,
   * @throws IllFormattedLineException
   * 						Wenn die neue Zeile nicht zulässig ist.
   */
  public static
    Line
    replaceAll(final Line line, final Subfield original, final Subfield replacement)
      throws IllFormattedLineException {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    RangeCheckUtils.assertReferenceParamNotNull("original", original);
    RangeCheckUtils.assertReferenceParamNotNull("replacement", replacement);
    final Tag tag = line.getTag();
    final List<Subfield> subfields = line.getSubfields();
    final boolean repl = Collections.replaceAll(subfields, original, replacement);
    if (repl) {
      final Line newLine = LineParser.parse(tag, subfields);
      return newLine;
    } else {
      return line;
    }

  }

  /**
   * Behält aus  einer Menge von Unterfeldern und einer Indikatorlist alle
   * Unterfelder zu diesem Indikator bei. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param subfields		nicht null.
   * @param indicators	beliebig.
   *
   * @return				Neue Liste, nicht null.
   */
  public static
    List<Subfield>
    retainSubfields(final Iterable<Subfield> subfields, final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    return getSubfields(subfields, Arrays.asList(indicators));
  }

  /**
   * Behält aus  einer Menge von Unterfeldern und einer Indikatorlist alle
   * Unterfelder zu diesem Indikator bei. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden.
   *
   * @param subfields		nicht null.
   * @param indicators	n.
   *
   * @return				Neue Liste, nicht null.
   */
  public static
    List<Subfield>
    getSubfields(final Iterable<Subfield> subfields, final Collection<Character> indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    final Predicate<Subfield> predicate = new Predicate<Subfield>() {
      @Override
      public boolean test(final Subfield subfield) {
        return indicators.contains(subfield.getIndicator().indicatorChar);
      }
    };
    return FilterUtils.newFilteredList(subfields, predicate);
  }

  /**
   * Liefert zu einer Zeile und einer Indikatorliste alle
   * Unterfelder zu diesen Indikatoren. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden. Reihenfolge Pica3.
   *
   * @param line		nicht null.
   * @param indicators	beliebig
   *
   * @return				Neue Liste, nicht null.
   */
  public static List<Subfield> retainSubfields(final Line line, final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    return getSubfields(line, Arrays.asList(indicators));
  }

  /**
   * Liefert zu einer Zeile und einer Indikatorliste alle
   * Unterfelder zu diesen Indikatoren. Ergebnis kann als gefilterte Zeile
   * einer Tabelle aufgefasst werden. Reihenfolge Pica3.
   *
   * @param line    nicht null.
   * @param indicators  nicht null
   *
   * @return        Neue Liste, nicht null.
   */
  public static
    List<Subfield>
    getSubfields(final Line line, final Collection<Character> indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    final List<Subfield> list = line.getSubfields();
    return getSubfields(list, indicators);
  }

  /**
   * Sind die beiden Listen gleich, wenn man indicators NICHT berücksichtigt?
   *
   * @param subfields1	nicht null
   * @param subfields2	nicht null
   * @param indicators	nicht null
   *
   * @return	true, wenn Listen gleich
   */
  public static boolean equalsRemoving(
    final Iterable<Subfield> subfields1,
    final Iterable<Subfield> subfields2,
    final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields1", subfields1);
    RangeCheckUtils.assertReferenceParamNotNull("subfields2", subfields2);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    final List<Subfield> list1 = removeSubfieldsFromCollection(subfields1, indicators);
    final List<Subfield> list2 = removeSubfieldsFromCollection(subfields2, indicators);
    return list1.equals(list2);
  }

  /**
   * Sind die Unterfelder gleich, wenn man indicators NICHT berücksichtigt?
   *
   * @param line			nicht null
   * @param subfields2	nicht null
   * @param indicators	nicht null
   *
   * @return	true, wenn Listen gleich
   */
  public static boolean equalsRemoving(
    final Line line,
    final Iterable<Subfield> subfields2,
    final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    RangeCheckUtils.assertReferenceParamNotNull("subfields2", subfields2);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    return equalsRemoving(line.getSubfields(), subfields2, indicators);
  }

  /**
   * Sind die Unterfelder gleich, wenn man indicators NICHT berücksichtigt?
   *
   * @param line1			nicht null
   * @param line2			nicht null
   * @param indicators	nicht null
   *
   * @return	true, wenn Listen gleich (die Tags werden ignoriert!)
   */
  public static
    boolean
    equalsRemoving(final Line line1, final Line line2, final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line1", line1);
    RangeCheckUtils.assertReferenceParamNotNull("line2", line1);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    return equalsRemoving(line1.getSubfields(), line2.getSubfields(), indicators);
  }

  /**
   * Sind die beiden Listen gleich, wenn man NUR indicators berücksichtigt?
   *
   * @param subfields1	nicht null
   * @param subfields2	nicht null
   * @param indicators	nicht null
   *
   * @return	true, wenn Listen gleich
   */
  public static boolean equalsRetaining(
    final Iterable<Subfield> subfields1,
    final Iterable<Subfield> subfields2,
    final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields1", subfields1);
    RangeCheckUtils.assertReferenceParamNotNull("subfields2", subfields2);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    final List<Subfield> list1 = retainSubfields(subfields1, indicators);
    final List<Subfield> list2 = retainSubfields(subfields2, indicators);
    return list1.equals(list2);
  }

  /**
   * Sind die Unterfelder gleich, wenn man wenn man
   * NUR indicators berücksichtigt?
   *
   * @param line			nicht null
   * @param subfields2	nicht null
   * @param indicators	nicht null
   *
   * @return	true, wenn Listen gleich
   */
  public static boolean equalsRetaining(
    final Line line,
    final Iterable<Subfield> subfields2,
    final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line", line);
    RangeCheckUtils.assertReferenceParamNotNull("subfields2", subfields2);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    return equalsRetaining(line.getSubfields(), subfields2, indicators);
  }

  /**
   * Sind die Unterfelder gleich, wenn man NUR indicators berücksichtigt?
   *
   * @param line1			nicht null
   * @param line2			nicht null
   * @param indicators	nicht null
   *
   * @return	true, wenn Listen gleich (die Tags werden ignoriert!)
   */
  public static
    boolean
    equalsRetaining(final Line line1, final Line line2, final Character... indicators) {
    RangeCheckUtils.assertReferenceParamNotNull("line1", line1);
    RangeCheckUtils.assertReferenceParamNotNull("line2", line1);
    RangeCheckUtils.assertReferenceParamNotNull("indicators", indicators);
    return equalsRetaining(line1.getSubfields(), line2.getSubfields(), indicators);
  }

  /**
   * Gibt ein neues Unterfeld $g mit Inhalten der ursprünglichen
   * Unterfelder durch ",_" aneinandergereiht.
   *
   * @param subfields nicht null
   * @return          null, wenn Fehler
   */
  public static Subfield mergeDollarG(final Collection<Subfield> subfields) {
    RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
    final String s = StringUtils.concatenate(",  ", subfields, IFSubfieldToContent.FUNCTION);
    Subfield subfield;
    try {
      subfield = new Subfield(GNDTagDB.DOLLAR_G, s);
      return subfield;
    } catch (final IllFormattedLineException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Quelle und Datum von Eingabe, Änderung und Status liegen z.B. in der Form<br><br>
   * 001A ƒ01140:09-11-21
   * <br><br> vor
   *
   * @param line  nicht null
   * @return      Quelle und Datum in Unterfeld $0, sofern eine
   *              Zeile 001A, 001B oder 001D vorliegt (oder null)
   */
  public static String getSourceAndDateStr(final Line line) {
    return getContentOfFirstSubfield(line, '0');
  }

  /**
   * Quelle und Datum von Eingabe, Änderung und Status liegen z.B. in der Form<br><br>
   * 001A ƒ01140:09-11-21
   * <br><br> vor
   * @param line  nicht null
   * @return      (Quelle, Datum), null sonst
   */
  public static Pair<String, String> getSourceAndDateP(final Line line) {
    final String sd = getSourceAndDateStr(line);
    if (sd == null)
      return null;
    else
      return getSourceAndDateP(sd);
  }

  /**
   * Quelle und Datum von Eingabe, Änderung und Status liegen z.B. in der Form<br><br>
   * 001A ƒ01140:09-11-21
   * <br><br> vor
   *
   * @param sourceDateString  nicht null
   * @return                  (Quelle, Datum), die im String durch
   *                          : getrennt sind; null sonst
   */
  public static Pair<String, String> getSourceAndDateP(final String sourceDateString) {
    Objects.requireNonNull(sourceDateString);
    final String[] arr = sourceDateString.split("\\:");
    if (arr.length != 2)
      return null;
    return new Pair<String, String>(arr[0], arr[1]);
  }

  /**
   * Quelle und Datum von Eingabe, Änderung und Status liegen z.B. in der Form<br><br>
   * 001A ƒ01140:09-11-21
   * <br><br> vor
   *
   * @param line  nicht null
   * @return      Datum oder null
   */
  public static Date getDate(final Line line) {
    final Pair<String, String> p = getSourceAndDateP(line);
    if (p == null)
      return null;
    final String dStr = p.second;
    return TimeUtils.parsePicaDate(dStr);
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
    final ArrayList<Line> lines = RecordUtils.getLines(record, "5050");
    final boolean notEHD =
      lines.stream().anyMatch(line -> !SubfieldUtils.containsIndicators(line, 'E', 'H', 'D'));
    System.out.println(notEHD);
  }

  /**
   *
   * Gibt eine neue Zeile, die nur noch die angegebenen Unterfelder
   * enthält oder null.
   *
   * @param line				nicht null
   * @param indicatorChars	nicht leer, nicht null
   * @return					neue Zeile oder null, wenn keine
   * 							Unterfelder übrigbleiben
   */
  public static
    Line
    getNewLineRetainingSubfields(final Line line, final Character... indicatorChars) {
    Objects.requireNonNull(line);
    Objects.requireNonNull(indicatorChars, "Liste der Indikatoren leer");
    if (indicatorChars.length == 0)
      throw new IllegalArgumentException("Liste der Indikatoren leer");
    final List<Subfield> subs = retainSubfields(line, indicatorChars);
    if (subs.isEmpty())
      return null;
    final Tag tag = line.getTag();
    final LineFactory factory = tag.getLineFactory();
    try {
      factory.load(subs);
    } catch (final IllFormattedLineException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final Line newLine = factory.createLine();
    return newLine;
  }

  /**
  *
  * Gibt eine neue Zeile, die die angegebenen Unterfelder
  * entfernt oder null.
  *
  * @param line        nicht null
  * @param indicatorChars  nicht leer, nicht null
  * @return          neue Zeile oder null, wenn keine
  *              Unterfelder übrigbleiben
  */
  public static
    Line
    getNewLineRemovingSubfields(final Line line, final Character... indicatorChars) {
    Objects.requireNonNull(line);
    Objects.requireNonNull(indicatorChars, "Liste der Indikatoren leer");
    if (indicatorChars.length == 0)
      throw new IllegalArgumentException("Liste der Indikatoren leer");
    final List<Subfield> subs = removeSubfields(line, indicatorChars);
    if (subs.isEmpty())
      return null;
    final Tag tag = line.getTag();
    final LineFactory factory = tag.getLineFactory();
    try {
      factory.load(subs);
    } catch (final IllFormattedLineException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final Line newLine = factory.createLine();
    return newLine;
  }

  /**
   * Der Aktionscode (z.B. SGG, ALL ...) ist
   * in 4821 $c enthalten.
   *
   * @param line nicht null
   * @return  Aktionscode (z.B. SGG, ALL ...)
   */
  public static String getActionscode(final Line line) {
    return getContentOfFirstSubfield(line, 'c');
  }

  /**
   * Die Bibliotheksnummer/Abteilungskennung (z.B. 1250) ist
   * in 4821 $N enthalten.
   *
   * @param line nicht null
   * @return  Inhalt von $N (Nutzerkennung im Feld 4821)
   */
  public static String getNutzerkennung(final Line line) {
    return getContentOfFirstSubfield(line, 'N');
  }

  /**
   * Extrahiert $D aus den EHKD-Feldern (und allen anderen)
   *
   * @param line nicht null
   * @return      Datum in der Form jjjj-mm-tt
   */
  public static String getDollarD(final Line line) {
    final String dollarD = getContentOfFirstSubfield(line, 'D');
    return dollarD;
  }

  /**
   * Extrahiert Date aus $D aus den EHKD-Feldern (und allen anderen)
   *
   * @param line nicht null
   * @return  Date
   */
  public static Date getDateAusDollarD(final Line line) {
    final String dollarD = getDollarD(line);
    return TimeUtils.parseMxDate(dollarD);
  }

  /**
   * Der Geschäftsgangstyp (z.B. StatZUG, StatIE) ist
   * in 4821 $z enthalten.
   *
   * @param line  nicht null
   * @return Geschäftsgangstyp (z.B. StatZUG, StatIE) aus 4821
   */
  public static String getGeschaeftsgang(final Line line) {
    return getContentOfFirstSubfield(line, 'z');
  }

  /**
   * @param lines         nicht null
   * @param indicatorList nicht null
   * @return  Alle Inhalte der durch die Indikatorliste gegebenen Indikatoren
   */
  public static
    List<String>
    getContents(final Collection<Line> lines, final Collection<Character> indicatorList) {
    final List<String> conts = new ArrayList<>();
    lines.forEach(line ->
    {
      final List<Subfield> subfields = getSubfields(line, indicatorList);
      conts.addAll(getContentsOfSubfields(subfields));
    });
    return conts;
  }

  /**
   * @param lines         nicht null
   * @param indicators    beliebig
   * @return  Alle Inhalte der durch die Indikatorliste gegebenen Indikatoren
   */
  public static
    List<String>
    getContents(final Collection<Line> lines, final Character... indicators) {
    return getContents(lines, Arrays.asList(indicators));
  }

}
