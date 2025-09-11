package de.dnb.gnd.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.applicationComponents.StreamUtils;
import de.dnb.basics.applicationComponents.Streams;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.filtering.Between;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.exceptions.ExceptionHandler;
import de.dnb.gnd.exceptions.IgnoringHandler;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Field;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.MarcParser;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordParser;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.line.LineParser;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.SWDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.formatter.RDAFormatter;

/**
 * Enthält allgemeine Hilfsfunktionen.
 *
 * @author baumann
 *
 */
public final class RecordUtils {

	public static final TagDB BIB_TAG_DB = BibTagDB.getDB();

	public static final TagDB AUTH_TAG_DB = GNDTagDB.getDB();

	public static final TagDB SWD_TAG_DB = SWDTagDB.getDB();

	/**
	 * ! + Mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X. + !
	 */
	public static final String LINK = "!(\\d{7,11}[Xx]?)!";
	/**
	 * Mindestens 7 -11 Zahlen, eventuell gefolgt von x oder X.
	 */
	public static final Pattern PAT_LINK = Pattern.compile(LINK);

	//@formatter:off

	public static boolean containsLink(final String s) {
		final Matcher matcher = PAT_LINK.matcher(s);
		return matcher.find();
	}

	/**
	 * Fügt eine Collection von Zeilen zu record hinzu.
	 * @param record	nicht null.
	 * @param lines		nicht null.
	 * @throws OperationNotSupportedException
	 * 					wenn eine der Zeilen nicht hinzugefügt werden
	 * 					darf.
	 */
	public static
		void
		addLines(final Record record, final Collection<Line> lines)
			throws OperationNotSupportedException {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
		for (final Line line : lines) {
			record.add(line);
		}

	}

	/**
	 * Ist der Tag im Record enthalten?
	 *
	 * @param record	nicht null.
	 * @param tag		nicht null.
	 * @return			true, wenn tag im record vorkommt.
	 */
	public static boolean containsField(final Record record, final Tag tag) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		final Field field = record.getField(tag);
		return field != null;
	}

	/**
	 * Ist der Tag im Record enthalten?
	 *
	 * @param record	nicht null.
	 * @param tag		nicht null.
	 * @return			true, wenn tag im record vorkommt.
	 */
	public static boolean containsField(final Record record, final String tag) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		final Tag tag2 = record.tagDB.findTag(tag);
		final Field field = record.getField(tag2);
		return field != null;
	}

	/**
     * Sind die Tags im Record enthalten?
     *
     * @param record    nicht null.
     * @param tags       nicht null.
     * @return          true, wenn tag im record vorkommt.
     */
    public static boolean containsFields(final Record record, final String... tags) {
        RangeCheckUtils.assertReferenceParamNotNull("record", record);
        RangeCheckUtils.assertReferenceParamNotNull("tag", tags);
        for (final String tag : tags) {
            if(containsField(record, tag))
                return true;
        }
        return false;
    }

	/**
	 * Liefert zu einem Datensatz, einem Tag und einem Indikator alle Inhalte
	 * der ersten Unterfelder zu diesem Indikator. Ergebnis kann als Spalte
	 * einer Tabelle aufgefasst werden.
	 *
	 * @param record	nicht null.
	 * @param tag		nicht null.
	 * @param indicator	beliebig.
	 *
	 * @return			nicht null.
	 */
	public static List<String> getContentsOfFirstSubfields(
		final Record record,
		final String tag,
		final char indicator) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", tag);
		final Field field = getFieldGivenAsString(record, tag);
		if (field == null)
			return Collections.emptyList();

		return SubfieldUtils.getContentsOfFirstSubfields(field, indicator);
	}

	/**
	 * Liefert zu einem Tag und Indikator den Inhalt des Unterfeldes. Zum Tag
	 * muss exakt eine Zeile gehören, sonst wird null geliefert.
	 *
	 * @param record	nicht null
	 * @param tag		nicht null
	 * @param indicator	beliebig
	 * @return			Inhalt oder null.
	 */
	public static String getContentOfSubfield(
		final Record record,
		final Tag tag,
		final char indicator) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		final Line line = getTheOnlyLine(record, tag);
		if (line == null)
			return null;
		return SubfieldUtils.getContentOfFirstSubfield(line, indicator);
	}

	/**
	 * Liefert zu einem Tag und Indikator den Inhalt des ersten Unterfeldes. Zum Tag
	 * muss exakt eine Zeile gehören, sonst wird null geliefert.
	 *
	 * @param record	nicht null
	 * @param tagStr		nicht null
	 * @param indicator	beliebig
	 * @return			Inhalt oder null.
	 */
	public static String getContentOfSubfield(
		final Record record,
		final String tagStr,
		final char indicator) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tagStr", tagStr);
		final Tag tag = record.tagDB.findTag(tagStr);
		if(tag==null)
		  return null;
		return getContentOfSubfield(record, tag, indicator);
	}

	/**
	 * Liefert zu einem Datensatz und einem Tag alle Inhalte der
	 * im Tag enthaltenen Unterfelder.
	 *
	 * @param record	nicht null.
	 * @param tag		nicht null.
	 * @return			nicht null, aber leer, wenn nicht genau eine Zeile zu
	 * 					diesem Tag existiert, modifizierbar.
	 */
	public static List<String> getContentsOfSubfields(
		final Record record,
		final String tag) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", tag);
		final Line line = getTheOnlyLine(record, tag);
		if (line == null)
			return Collections.emptyList();
		else {
			final Collection<Subfield> subfields = line.getSubfields();
			return SubfieldUtils.getContentsOfSubfields(subfields);
		}
	}

	/**
	 * Liefert zu einem Datensatz und einem Tag alle Inhalte der
	 * im Tag enthaltenen Unterfelder, die mit dem Indikator übereinstimmen.
	 * <br/>Zum Tag darf nur eine Zeile gehören.
	 *
	 * @param record	nicht null.
	 * @param tag		nicht null.
	 * @param ind		beliebig
	 * @return			nicht null, aber leer, wenn nicht genau eine Zeile zu
	 * 					diesem Tag existiert.
	 */
	public static List<String> getContentsOfSubfields(
		final Record record,
		final String tag,
		final char ind) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", tag);
		final Line line = getTheOnlyLine(record, tag);
		if (line == null)
			return Collections.emptyList();
		else {
			return SubfieldUtils.getContentsOfSubfields(line, ind);
		}
	}

	//@formatter:on
	/**
	 * Liefert zu einem Datensatz und einem Tag alle Inhalte der im Tag enthaltenen
	 * Unterfelder, die mit dem Indikator übereinstimmen. <br/>
	 * Zum Tag können auch mehrere Zeilen gehören.
	 *
	 * @param record nicht null.
	 * @param tag    nicht null.
	 * @param ind    beliebig
	 * @return nicht null, aber eventuell leer, modifizierbar.
	 */
	public static List<String> getContentsOfAllSubfields(final Record record, final String tag, final char ind) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrEmpty("tag", tag);
		final Collection<Line> lines = getLines(record, tag);

		final List<String> strings = new LinkedList<String>();
		for (final Line line : lines) {
			strings.addAll(SubfieldUtils.getContentsOfSubfields(line, ind));
		}
		return strings;
	}

	/**
	 * Liefert das Feld zu record und aTag.
	 *
	 * @param record nicht null.
	 * @param aTag   nicht null, nicht leer
	 * @return Feld oder null, wenn aTag nicht existent oder kein Feld zu aTag
	 *         vorhanden.
	 */
	public static Field getFieldGivenAsString(final Record record, final String aTag) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("aTag", aTag);
		final Tag tag = record.tagDB.findTag(aTag);
		if (tag != null) {
			return record.getField(tag);
		} else {
			return null;
		}
	}

	/**
	 * Liefert a) die erste Zeile einer Collection von Tags im Record b) die Zahl
	 * der Zeilen.
	 *
	 * @param record nicht null.
	 * @param tags   nicht null.
	 * @return Paar aus Zeile und Gesamtzahl der Zeilen. Wenn keine Zeile vorhanden,
	 *         so wird (null, 0) zurückgegeben.
	 *
	 */
	public static Pair<Line, Integer> getFirstLine(final Record record, final Collection<Tag> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final List<Line> lines = getLines(record, tags);
		final Integer size = lines.size();
		if (lines.isEmpty()) {
			return new Pair<Line, Integer>(null, size);
		} else {
			return new Pair<Line, Integer>(lines.get(0), size);
		}
	}

	/**
	 * Liefert a) die erste Zeile eines Tags im Record b) die Zahl der Zeilen.
	 *
	 * @param record nicht null.
	 * @param tag    nicht null.
	 * @return Paar aus Zeile und Gesamtzahl der Zeilen. Wenn keine Zeile vorhanden,
	 *         so wird (null, 0) zurückgegeben.
	 *
	 */
	public static Pair<Line, Integer> getFirstLine(final Record record, final Tag tag) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		final Field field = record.getField(tag);
		if (field == null) {
			return new Pair<Line, Integer>(null, 0);
		}
		final Integer size = field.size();
		final Line line = field.iterator().next();
		return new Pair<Line, Integer>(line, size);
	}

	/**
	 * Liefert a) die erste Zeile eines Tags im Record b) die Zahl der Zeilen.
	 *
	 * @param record nicht null.
	 * @param aTag   nicht null.
	 * @return Paar aus Zeile und Gesamtzahl der Zeilen. Wenn keine Zeile vorhanden,
	 *         so wird (null, 0) zurückgegeben.
	 *
	 */
	public static Pair<Line, Integer> getFirstLineTagGivenAsString(final Record record, final String aTag) {
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("aTag", aTag);
		final Tag tag = record.tagDB.findTag(aTag);
		return getFirstLine(record, tag);
	}

	//@formatter:on
	/**
	 * Liefert Zeile zu einem Tag.
	 *
	 * @param record nicht null.
	 * @param aTag   nicht null, nicht leer.
	 * @return Zugehörige Zeile, wenn es die einzige ist, sonst null. Auch null,
	 *         wenn aTag nicht existiert.
	 *
	 */
	public static Line getTheOnlyLine(final Record record, final String aTag) {
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("aTag", aTag);
		final Tag tag = record.tagDB.findTag(aTag);
		if (tag == null)
			return null;
		else
			return getTheOnlyLine(record, tag);
	}

	/**
	 * Liefert Zeile zu einem Tag.
	 *
	 * @param record nicht null.
	 * @param tag    nicht null.
	 * @return Zugehörige Zeile, wenn es die einzige ist, sonst null.
	 *
	 */
	public static Line getTheOnlyLine(final Record record, final Tag tag) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		final Pair<Line, Integer> pair = getFirstLine(record, tag);
		if (pair.second == 1)
			return pair.first;
		else
			return null;
	}

	//@formatter:on
	/**
	 * Liefert zu einer Menge von Zeilen und einem Indikator alle ersten Unterfelder
	 * zu diesem Indikator, die NICHT null sind. Ergebnis kann als Spalte einer
	 * Tabelle aufgefasst werden.
	 *
	 * @param lines     nicht null.
	 * @param indicator beliebig.
	 *
	 * @return nicht null.
	 */
	public static ArrayList<Subfield> getFirstSubfields(final Iterable<Line> lines, final char indicator) {
		RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
		return FilterUtils.mapNullFiltered(lines, line -> SubfieldUtils.getFirstSubfield(line, indicator));
	}

	/**
	 * Liefert die zu den Tags gehörigen Zeilen.
	 *
	 * @param record nicht null
	 * @param tags   nicht null.
	 * @return nicht null, modifizierbare Liste.
	 */
	public static ArrayList<Line> getLines(final Record record, final Collection<? extends Tag> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final ArrayList<Line> lines = new ArrayList<Line>();
		for (final Tag tag : tags) {
			final Field field = record.getField(tag);
			if (field != null) {
				lines.addAll(field.getLines());
			}
		}
		return lines;
	}

	/**
	 * Liefert alle Zeilen mit Tag aus tags, für die predicate zutrifft.
	 *
	 * @param record    nicht null.
	 * @param predicate nicht null.
	 * @param tags      nicht null.
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLines(final Record record, final Predicate<Line> predicate,
			final Collection<? extends Tag> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("predicate", predicate);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final ArrayList<Line> lines = getLines(record, tags);
		FilterUtils.filter(lines, predicate);
		return lines;
	}

	/**
	 * Liefert alle Zeilen mit Tag aus tags, für die predicate zutrifft.
	 *
	 * @param record    nicht null.
	 * @param predicate nicht null.
	 * @param tags      nicht null.
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLines(final Record record, final Predicate<Line> predicate, final Tag... tags) {
		return getLines(record, predicate, Arrays.asList(tags));
	}

	/**
	 * Verwendung etwa:<br>
	 * <br>
	 * <code>getLines(record, "551", '4', s -> s.equals("orts"));</code>
	 *
	 * @param record            nicht null
	 * @param tag               nicht null, nicht leer
	 * @param indicator         beliebig
	 * @param subfieldPredicate Prädikat
	 * @return nicht leer,modifizierbar
	 */
	public static List<Line> getLinesWithSubfield(final Record record, final String tag, final char indicator,
			final Predicate<String> subfieldPredicate) {
		final Tag tag2 = record.tagDB.findTag(tag);
		if (tag2 == null)
			return Collections.emptyList();
		final Predicate<Line> linepred = new Predicate<Line>() {
			@Override
			public boolean test(final Line line) {
				return SubfieldUtils.getContentsOfSubfields(line, indicator).stream().anyMatch(subfieldPredicate);
			}
		};
		return getLines(record, linepred, tag2);
	}

	/**
	 * Verwendung etwa:<br>
	 * <br>
	 * <code>getLinesWithSubfield(record, "551", '4', "ortg|ortw");</code>
	 *
	 * @param record    nicht null
	 * @param tag       nicht null, nicht leer
	 * @param indicator beliebig
	 * @param regexp    regulärer Ausdruck
	 * @return nicht null, modifizierbar. Wenn Tag nicht vorhanden oder regulärer
	 *         Ausdruck falsch gebildet: leere Liste
	 */
	public static List<Line> getLinesWithSubfield(final Record record, final String tag, final char indicator,
			final String regexp) {
		Pattern pattern;
		try {
			pattern = Pattern.compile(regexp);
		} catch (final PatternSyntaxException e) {
			return Collections.emptyList();
		}
		return getLinesWithSubfield(record, tag, indicator, s -> pattern.matcher(s).matches());
	}

	/**
	 * Liefert die zu den Tags gehörigen Zeilen.
	 *
	 * @param record nicht null
	 * @param tags   nicht null.
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLines(final Record record, final String... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<String> collection = Arrays.asList(tags);
		return getLinesByTagsGivenAsString(record, collection);
	}

	/**
	 * Gibt eine Teilmenge der Tags, die zwischen from (einschließlich) und to
	 * (einschließlich) liegen.
	 * 
	 * @param record nicht null
	 * @param from   nicht null, nicht leer.
	 * @param to     nicht null, nicht leer.
	 * @return nicht null, modifizierbar.
	 */
	public static List<Line> getLinesBetween(final Record record, final String from, final String to) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("from", from);
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("to", to);
		final TagDB db = record.tagDB;
		final Collection<Tag> tags = db.getTagsBetween(from, to);
		return getLines(record, tags);
	}

	/**
	 * Liefert die zu den Tags gehörigen Zeilen.
	 *
	 * @param record nicht null
	 * @param tags   nicht null.
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLines(final Record record, final Tag... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<Tag> collection = Arrays.asList(tags);
		return getLines(record, collection);
	}

	/**
	 * Liefert zu den Tags, die pattern erfüllen, die zugehörigen Zeilen.
	 *
	 * @param record  nicht null
	 * @param pattern nicht null, nicht leer, Pica oder Pica+
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLines(final Record record, final String pattern) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("pattern", pattern);
		final Collection<Tag> collection = record.tagDB.findTagPattern(pattern);
		return getLines(record, collection);
	}

	/**
	 * Liefert die zu den Tags gehörigen Zeilen.
	 *
	 * @param record nicht null
	 * @param tags   nicht null.
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLinesByTagsGivenAsString(final Record record, final Collection<String> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final ArrayList<Line> lines = new ArrayList<Line>();
		for (final String string : tags) {
			final Tag tag = record.tagDB.findTag(string);
			if (tag != null) {
				final Field field = record.getField(tag);
				if (field != null) {
					lines.addAll(field.getLines());
				}
			}
		}
		return lines;
	}

	/**
	 * Liefert alle Zeilen mit Tag aus tags, für die predicate zutrifft.
	 *
	 * @param record    nicht null.
	 * @param predicate nicht null.
	 * @param tags      nicht null.
	 * @return nicht null, modifizierbar.
	 */
	public static ArrayList<Line> getLinesByTagsGivenAsStrings(final Record record, final Predicate<Line> predicate,
			final String... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("predicate", predicate);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final ArrayList<Line> lines = getLines(record, tags);
		FilterUtils.filter(lines, predicate);
		return lines;
	}

	/**
	 * Gibt eine neue Liste, deren Zeilen predicate erfüllen.
	 * 
	 * @param lines     nicht null
	 * @param predicate nicht null
	 * @return nicht null
	 */
	public static List<Line> filter(final Iterable<Line> lines, final Predicate<Line> predicate) {
		return FilterUtils.newFilteredList(lines, predicate);
	}

	/**
	 * Gibt die relevanten Unterfelder.
	 *
	 * @param tag       nicht null
	 * @param subfields nicht null
	 *
	 * @return nicht null, eventuell leer
	 */
	public static List<Subfield> getRelevantSubfields(final Tag tag, final Iterable<Subfield> subfields) {
		RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		return SubfieldUtils.removeSubfieldFromCollection(subfields, tag.getIgnorableIndicator());
	}

	/**
	 * Logt message in die Datei "log/logfileDatensatz.txt".
	 * 
	 * @param message beliebig.
	 */
	public static void logError(final String message) {
		File logfile;
		PrintStream log;

		try {
			final File logfolder = new File("log");
			logfolder.mkdir();

			logfile = new File("log/logfileDatensatz.txt");
			final boolean append = true;

			final FileOutputStream logOS = new FileOutputStream(logfile, append);

			log = new PrintStream(logOS);
			log.print(message);
			log.println();
			MyFileUtils.safeClose(log);
		} catch (final Exception e1) {
			// Gefährlich, aber wohl sinnvoll, da nur ein Logversuch
			// in nicht allzu wichtigen Situationen.
			e1.printStackTrace();
		}

	}

	/**
	 * Entfernt $8.
	 *
	 * @param subfields nicht null.
	 * @return gefilterte Collection.
	 */
	public static Collection<Subfield> removeExpansion(final Collection<Subfield> subfields) {
		RangeCheckUtils.assertReferenceParamNotNull("subfields", subfields);
		return FilterUtils.newFilteredList(subfields, new ExpansionFilter());
	}

	/**
	 * Entfernt Zeilen aus dem record.
	 *
	 * @param record nicht null
	 * @param lines  nicht null
	 */
	public static void removeLines(final Record record, final Collection<Line> lines) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
		for (final Line line : lines) {
			record.remove(line);
		}
	}

	/**
	 * Entfernt Zeilen aus dem record.
	 *
	 * @param record nicht null
	 * @param lines  nicht null
	 */
	public static void removeLines(final Record record, final Line... lines) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
		final Collection<Line> collection = Arrays.asList(lines);
		removeLines(record, collection);
	}

	/**
	 * Entfernt tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tags   nicht null
	 */
	public static void removeTags(final Record record, final Collection<? extends Tag> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		for (final Tag tag : tags) {
			record.removeField(tag);
		}
	}

	/**
	 * Entfernt tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tags   nicht null
	 */
	public static void removeTags(final Record record, final Tag... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<Tag> collection = Arrays.asList(tags);
		removeTags(record, collection);
	}

	/**
	 * Entfernt tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tagDB  nicht null.
	 * @param tags   nicht null, Pica3 oder Pica+, als Strings übergeben.
	 */
	public static void removeTags1(final Record record, final Collection<String> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final TagDB tagDB = record.tagDB;
		for (final String string : tags) {
			final Tag tag = tagDB.findTag(string);
			record.removeField(tag);
		}
	}

	/**
	 * Entfernt tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tags   nicht null, , Pica3 oder Pica+ als String übergeben.
	 */
	public static void removeTags(final Record record, final String... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<String> collection = Arrays.asList(tags);
		removeTags1(record, collection);
	}

	/**
	 *
	 * @param record nicht null
	 * @param first  nicht null, Pica3 als String
	 * @param last   nicht null, Pica3 als String
	 */
	public static void removeTagsBetween(final Record record, final String first, final String last) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final LinkedHashSet<Tag> tags = record.getTags();
		final Between<String> between = new Between<String>(first, last);
		tags.forEach(tag -> {
			if (between.test(tag.pica3))
				record.removeField(tag);
		});
	}

	/**
	 * Entfernt alle anderen tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tags   nicht null
	 */
	public static void retainTags(final Record record, final Collection<? extends Tag> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<Tag> tagsOfRecord = record.getTags();
		for (final Tag tag : tagsOfRecord) {
			if (!tags.contains(tag))
				record.removeField(tag);
		}
	}

	/**
	 * Entfernt alle anderen tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tags   nicht null
	 */
	public static void retainTags(final Record record, final Tag... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<Tag> collection = Arrays.asList(tags);
		retainTags(record, collection);
	}

	/**
	 * Entfernt alle anderen tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tagDB  nicht null.
	 * @param tags   nicht null, als Strings übergeben.
	 */
	public static void retainTagstrings(final Record record, final Collection<String> tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final TagDB tagDB = record.tagDB;
		RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);
		final List<Tag> tagList = FilterUtils.mapNullFiltered(tags, tag -> tagDB.findTag(tag));
		retainTags(record, tagList);
	}

	/**
	 * Entfernt alle anderen tags aus dem record.
	 *
	 * @param record nicht null
	 * @param tags   nicht null, als String übergeben.
	 */
	public static void retainTags(final Record record, final String... tags) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("tags", tags);
		final Collection<String> collection = Arrays.asList(tags);
		retainTagstrings(record, collection);
	}

	/**
	 * Ersetzt eine Zeile eines Datensatzes durch eine andere.
	 *
	 * @param record      nicht null
	 * @param original    nicht null
	 * @param replacement nicht null
	 * @return true, wenn Änderung erfolgte.
	 */
	public static boolean replace(final Record record, final Line original, final Line replacement) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("original", original);
		RangeCheckUtils.assertReferenceParamNotNull("replacement", replacement);
		final Tag tag = original.getTag();
		if (tag != replacement.getTag()) {
			throw new IllegalArgumentException("tags ungleich");
		}
		final Field field = record.getField(tag);
		RangeCheckUtils.assertReferenceParamNotNull("field", field);
		final boolean replaced = field.replace(original, replacement);
		if (replaced) {
			record.setField(field);
		}
		return replaced;
	}

	/**
	 * Liefert eine pica3- oder pica+ Repräsentation.
	 *
	 * @param lines             nicht null.
	 * @param format            pica3 oder pica+
	 * @param expanded          true, wenn Expansion der relationierten Felder
	 *                          gewünscht ist.
	 * @param lineSeparator     in der Regel \r\n
	 * @param subfieldSeparator in der Regel $
	 * @return nicht null. Unicode-Composition.
	 */
	public static String toPica(final Collection<Line> lines, final Format format, final boolean expanded,
			final String lineSeparator, final char subfieldSeparator) {
		RangeCheckUtils.assertReferenceParamNotNull("", lines);
		RangeCheckUtils.assertReferenceParamNotNull("", format);
		String s = "";
		for (final Iterator<Line> iterator = lines.iterator(); iterator.hasNext();) {
			final Line line = iterator.next();
			s += toPica(line, format, expanded, subfieldSeparator);
			if (iterator.hasNext()) {
				s += lineSeparator;
			}
		}
		return s;
	}

	/**
	 *
	 * Wandel eine Zeile in einen String um, wobei null-Werte als "" ausgegeben
	 * werden.
	 *
	 * @param line              auch null.
	 * @param format            pica3 oder pica+
	 * @param expanded          mit Expansionen ausgeben.
	 * @param subfieldSeparator TODO
	 * @return eine Zeile als String oder "". Unicode-Composition.
	 */
	public static String toPica(final Line line, final Format format, final boolean expanded,
			final char subfieldSeparator) {
		if (line == null) {
			return "";
		}
		RangeCheckUtils.assertReferenceParamNotNull("format", format);
		final Collection<Subfield> subfields = line.getSubfields(format);
		return toPica(line.getTag(), subfields, format, expanded, subfieldSeparator);
	}

	/**
	 *
	 * Wandel den Inhalt einer Zeile in einen String um, wobei null-Werte als ""
	 * ausgegeben werden.
	 *
	 * @param line              auch null.
	 * @param format            pica3 oder pica+
	 * @param expanded          mit Expansionen ausgeben.
	 * @param subfieldSeparator In der Regel $
	 * @return eine Zeile als String oder "", Unicode-Composition.
	 */
	public static String toPicaWithoutTag(final Line line, final Format format, final boolean expanded,
			final char subfieldSeparator) {
		if (line == null) {
			return "";
		}
		RangeCheckUtils.assertReferenceParamNotNull("format", format);
		final Collection<Subfield> subfields = line.getSubfields(format);
		return toPicaWithoutTag(line.getTag(), subfields, format, expanded, subfieldSeparator);
	}

	/**
	 *
	 * Wandelt den Inhalt einer Zeile in einen String um, wobei null-Werte als ""
	 * ausgegeben werden.
	 *
	 * @param line auch null.
	 *
	 * @return eine Zeile als String (pica3, mit Expansionen, $); oder "", wenn null
	 *         übergeben worde. Unicode-Composition.
	 */
	public static String toPicaWithoutTag(final Line line) {
		if (line == null) {
			return "";
		}
		return toPicaWithoutTag(line, Format.PICA3, true, Constants.DOLLAR);

	}

	/**
	 * Liefert eine pica3-Repräsentation.
	 *
	 * @param record nicht null.
	 *
	 * @return pica3, expandiert, lineSeparator = \r\n subfieldSeparator = $,
	 *         Unicode-Composition.
	 */
	public static String toPica(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return toPica(record, Format.PICA3, true, Constants.LINE_SEPARATOR, '$');
	}

	/**
	 * Liefert eine pica3- oder pica+ Repräsentation.
	 *
	 * @param record            nicht null.
	 * @param format            pica3 oder pica+
	 * @param expanded          true, wenn Expansion der relationierten Felder
	 *                          gewünscht ist.
	 * @param lineSeparator     wenn null, dann "\r\n"
	 * @param subfieldSeparator wenn '0' dann '$'
	 * @return nicht null. Unicode-Composition.
	 */
	public static String toPica(final Record record, final Format format, final boolean expanded, String lineSeparator,
			char subfieldSeparator) {
		/*
		 * Die Funktion hat eine eigene Implementierung, da der Iterator vom Format
		 * abhängt. Daher kann nicht toPica(Collection, format, ...) verwendet werden!
		 */
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		RangeCheckUtils.assertReferenceParamNotNull("format", format);
		if (lineSeparator == null)
			lineSeparator = Constants.LINE_SEPARATOR;
		if (subfieldSeparator == '0')
			subfieldSeparator = Constants.DOLLAR;
		String s = "";
		Iterator<Line> iterator;
		if (format == Format.PICA3) {
			iterator = record.iterator();
		} else {
			iterator = record.picaPlusIterator();
		}

	//@formatter:off
		for (; iterator.hasNext();) {
				//@formatter:on
			final Line line = iterator.next();
			s += toPica(line, format, expanded, subfieldSeparator);
			if (iterator.hasNext()) {
				s += lineSeparator;
			}
		}
		return s.trim();
	}

	/**
	 * Liefert eine pica3- oder pica+ Repräsentation. tag muss nicht zu subfields
	 * passen!
	 *
	 * @param tag               nicht null.
	 * @param subfields         nicht null.
	 * @param format            pica3 oder pica+
	 * @param expanded          true, wenn Expansion der relationierten Felder
	 *                          gewünscht ist.
	 * @param subfieldSeparator TODO
	 * @return nicht null. Unicode-Composition.
	 */
	public static String toPica(final Tag tag, final Collection<Subfield> subfields, final Format format,
			final boolean expanded, final char subfieldSeparator) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfields", subfields);
		RangeCheckUtils.assertReferenceParamNotNull("format", format);
		String s;
		if (format == Format.PICA3) {
			s = tag.pica3;
		} else {
			s = tag.picaPlus;
		}
		s += " " + toPicaWithoutTag(tag, subfields, format, expanded, subfieldSeparator);
		return s;
	}

	/**
	 * Liefert eine pica3- oder pica+ Repräsentation. tag muss nicht zu subfields
	 * passen!
	 *
	 * @param tag               nicht null.
	 * @param subfields         nicht null.
	 * @param format            pica3 oder pica+
	 * @param expanded          true, wenn Expansion der relationierten Felder
	 *                          gewünscht ist.
	 * @param subfieldSeparator Unterfeldtrenner, in der Regel '$' bei Pica3
	 * @return nicht null, Unicode-Composition.
	 */
	public static String toPicaWithoutTag(final Tag tag, final Collection<Subfield> subfields, final Format format,
			final boolean expanded, final char subfieldSeparator) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfields", subfields);
		RangeCheckUtils.assertReferenceParamNotNull("format", format);

		final String subfieldSeparatorStr = Character.toString(subfieldSeparator);

		Collection<Subfield> adjusted;
		if (expanded) {
			adjusted = subfields;
		} else {
			adjusted = removeExpansion(subfields);
		}
		if (format == Format.PICA3)
			adjusted = getRelevantSubfields(tag, adjusted);
		boolean isFirst = true;
		boolean isFirstAttaching = true;
		String s = "";
		for (final Subfield subfield : adjusted) {
			final Indicator indicator = subfield.getIndicator();
			final char indChar = indicator.indicatorChar;
			String content = subfield.getContent();

			// für "k p" wieder Dollars maskieren:
			if (format == Format.PICA_PLUS && subfieldSeparator == Constants.DOLLAR)
				content = content.replace("$", "$$");

			if (format == Format.PICA_PLUS) {
				s += subfieldSeparatorStr + indChar + content;
			} else { // pica3
				if (isFirst && indicator == tag.getDefaultFirst()) {
					s += content;
				} else if (indicator.prefix != null) {
					String prefix;
					// Alternatives Präfix wird benutzt,
					// wenn Indikator nicht an erster Stelle und
					// nicht anschliessend ist:
					if (!isFirst && indicator.prefixAlt != null && !indicator.isAttaching)
						prefix = indicator.prefixAlt;
					else if (isFirstAttaching && indicator.prefixAlt != null) {
						prefix = indicator.prefix;
						isFirstAttaching = false;
					} else if (!isFirstAttaching && indicator.prefixAlt != null) {
						prefix = indicator.prefixAlt;
					} else
						prefix = indicator.prefix;
					// postfix != null
					s += prefix + content + indicator.postfix;
				} else {
					// Standardverhalten für GND:
					s += subfieldSeparatorStr + indChar + content;
				}
				isFirst = false;
			}
		}
		return StringUtils.unicodeComposition(s);

	}

	/**
	 * Liefert eine pica3-Repräsentation. tag muss nicht zu subfields
	 * passen!
	 *
	 * @param tag       nicht null.
	 * @param subfields nicht null oder leer
	 * @return nicht null. Unicode-Composition.
	 */
	public static String toPicaWithoutTag(final Tag tag, final Collection<Subfield> subfields) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfields", subfields);
		return toPicaWithoutTag(tag, subfields, Format.PICA3, true, '$');
	}

	/**
	 * Ausgabe im pica3-Format, expandiert, lineSeparator = \r\n, subfieldSeparator
	 * = $.
	 *
	 * @param lines nicht null.
	 * @return nicht null.
	 */
	public static String toPicaExpanded(final Collection<Line> lines) {
		RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
		return toPica(lines, Format.PICA3, true, Constants.LINE_SEPARATOR, '$');
	}

	/**
	 * Hilft beim Aufbau von Zeilen. In eine vorgegebene Liste werden die
	 * Unterfelder aus from an der passenden Position eingefügt. Die
	 * Standardreihenfolge der Indikatoren wird von tag vorgegeben. Ist schon ein
	 * Unterfeld zu gleichem Indikator vorhanden, so wird nach diesem eingefügt.
	 *
	 *
	 * @param from einzufügende Liste, nicht null
	 * @param to   Liste, in die eingefügt werden soll.
	 * @param tag  nicht null
	 */
	public static void insertAtBestPosition(final List<Subfield> from, final List<Subfield> to, final Tag tag) {

		RangeCheckUtils.assertReferenceParamNotNull("from", from);
		RangeCheckUtils.assertReferenceParamNotNull("to", to);
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		for (final Subfield subfield : from) {
			insertAtBestPosition(subfield, to, tag);
		}
	}

	/**
	 * Hilft beim Aufbau von Zeilen. In eine vorgegebene Liste wird das Unterfeld
	 * subfield an der passenden Position eingefügt. Die Standardreihenfolge der
	 * Indikatoren wird von tag vorgegeben. Ist schon ein Unterfeld zu gleichem
	 * Indikator vorhanden, so wird nach diesem eingefügt.
	 * 
	 * @param subfield     einzufügendes Unterfeld, nicht null
	 * @param subfieldList Liste, in die eingefügt werden soll.
	 * @param tag          nicht null
	 */
	public static void insertAtBestPosition(final Subfield subfield, final List<Subfield> subfieldList, final Tag tag) {

		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertReferenceParamNotNull("subfield", subfield);
		RangeCheckUtils.assertReferenceParamNotNull("subfieldList", subfieldList);
		final Character indicator = SubfieldUtils.FUNCTION_SUBFIELD_TO_INDICATOR_CHAR.apply(subfield);
		final List<Indicator> inds = new LinkedList<Indicator>(tag.getAllIndicators());

		// Eine Standardreihenfolge aller Indikatoren herstellen:
		List<Character> allIndicators = FilterUtils.map(inds, SubfieldUtils.FUNCTION_INDICATOR_TO_CHAR);
		// Nur Indikatoren, die bis indicator liegen, berücksichtigen.
		final int indPos = allIndicators.lastIndexOf(indicator);
		allIndicators = allIndicators.subList(0, indPos + 1);
		// Liste der gegebenen Indikatoren:
		final List<Character> givenIndicators = FilterUtils.map(subfieldList,
				SubfieldUtils.FUNCTION_SUBFIELD_TO_INDICATOR_CHAR);
		/*
		 * Sinnvollste Position erraten: Für jeden Indikator der Standardreihenfolge die
		 * letzte Position finden.
		 */
		int pos = -1;
		for (final Character ind : allIndicators) {
			pos = Math.max(pos, givenIndicators.lastIndexOf(ind));
		}
		subfieldList.add(pos + 1, subfield);
	}

	/**
	 * Gibt eine normierte, nicht regelgerechte Darstellung einer Zeile.
	 * 
	 * @param line nicht null.
	 * @return nicht null.
	 */
	public static String toString(final Line line) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		return toString(line.getTag(), line.getSubfields(Format.PICA3));
	}

	/**
	 * Gibt eine normierte, nicht regelgerechte Darstellung einer Zeile, bestehend
	 * aus tag und subfields. tag muss nicht zu subfields passen!
	 *
	 * @param tag       nicht null.
	 * @param subfields nicht null, nicht leer
	 * @return nicht null.
	 */
	public static String toString(final Tag tag, final Collection<Subfield> subfields) {
		RangeCheckUtils.assertReferenceParamNotNull("tag", tag);
		RangeCheckUtils.assertCollectionParamNotNullOrEmpty("subfields", subfields);
		String s = tag.pica3 + " ";
		for (final Subfield subfield : subfields) {
			s += subfield;
		}
		return s;
	}

	/**
	 * Liest einen Datensatz von der Standardeingabe. Fehler werden ignoriert.
	 * 
	 * @param tagDB nicht null
	 * @return neuen Datensatz.
	 */
	public static Record readFromConsole(final TagDB tagDB) {
		return readFromConsole(tagDB, null);
	}

	/**
	 * Liest die xml-Darstellung vom Portal und wandelt sie, so gut es geht, in
	 * einen Datensatz um.
	 *
	 * @param idn nicht null
	 * @return Datensatz oder null
	 */
	public static Record readFromPortal(final String idn) {
		RangeCheckUtils.assertReferenceParamNotNull("", idn);
		final org.marc4j.marc.Record marcRecord = PortalUtils.getMarcRecord(idn);
		if (marcRecord == null)
			return null;
		try {
			final MarcParser parser = new MarcParser();
			final Record record = parser.parse(marcRecord);
			return record;
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Liest einen Datensatz von der Standardeingabe.
	 * 
	 * @param tagDB   nicht null
	 * @param handler beliebig
	 * @return neuen Datensatz.
	 */
	public static Record readFromConsole(final TagDB tagDB, final ExceptionHandler handler) {
		RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);
		System.out.println("Bitte Datensatz eingeben (beenden mit STRG-Z):");
		final String string = StreamUtils.readIntoString(System.in);
		final RecordParser parser = new RecordParser();
		parser.setHandler(handler);
		parser.setDefaultTagDB(tagDB);
		return parser.parse(string);
	}

	/**
	 * Liest einen Datensatz aus einer Datei.
	 * 
	 * @param fileName   micht null, nicht leer
	 * @param tagDB      nicht null
	 * @param handler    beliebig
	 * @param ignoreMARC
	 * @return neuen Datensatz.
	 * @throws FileNotFoundException wenn nicht da
	 */
	public static Record readFromFile(final String fileName, final TagDB tagDB,

			final boolean ignoreMARC) throws FileNotFoundException {
		RangeCheckUtils.assertStringParamNotNullOrWhitespace("fileName", fileName);
		RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);

		final RecordReader recordReader = new RecordReader(fileName);
		recordReader.setHandler(new IgnoringHandler());
		recordReader.setDefaultTagDB(tagDB);
		recordReader.setIgnoreMARC(ignoreMARC);
		final Record next = recordReader.next();
		MyFileUtils.safeClose(recordReader);
		return next;
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage. Die nötige Datenbank wird aus
	 * dem Datensatz ermittelt.
	 *
	 * @return neuen Datensatz oder null, wenn Clipboard nicht in String
	 *         konvertierbar.
	 */
	public static Record readFromClip() {
		final String string = StringUtils.readClipboard();
		if (string == null)
			return null;
		final RecordParser parser = new RecordParser();
		return parser.parse(string);
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage.
	 * 
	 * @param tagDB      nicht null
	 * @param handler    beliebig
	 * @param ignoreMARC Überlese beim Parsen von Pica+-Daten die Felder, die in
	 *                   MARC21 üblich sind, die aber nicht zum Pica-Format gehören.
	 * @return neuen Datensatz oder null, wenn Clipboard nicht in String
	 *         konvertierbar.
	 */
	public static Record readFromClip(final TagDB tagDB, final ExceptionHandler handler, final boolean ignoreMARC) {
		RangeCheckUtils.assertReferenceParamNotNull("tagDB", tagDB);
		final String string = StringUtils.readClipboard();
		if (string == null)
			return null;
		final RecordParser parser = new RecordParser();
		parser.setHandler(handler);
		parser.setDefaultTagDB(tagDB);
		parser.setIgnoreMARC(ignoreMARC);
		return parser.parse(string);
	}

	/**
	 * Liest einen Datensatz aus der Zwischenablage. Fehler werden ignoriert.
	 * 
	 * @param tagDB nicht null
	 * @return neuen Datensatz oder null, wenn Clipboard nicht in String
	 *         konvertierbar.
	 */
	public static Record readFromClip(final TagDB tagDB) {
		return readFromClip(tagDB, null, false);
	}

	/**
	 *
	 * @param record beliebig
	 * @return true, wenn null oder ohne Felder
	 */
	public static boolean isNullOrEmpty(final Record record) {
		return (record == null || record.getFields().isEmpty());
	}

	public static boolean isAuthority(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return record.tagDB == AUTH_TAG_DB;
	}

	public static boolean isBibliographic(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return record.tagDB == BIB_TAG_DB;
	}

	public static boolean isSWD(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return record.tagDB == SWD_TAG_DB;
	}

	/**
	 * Liefert die Satzart. Etwa Tp1 aus 005 bei Normdaten oder "Abvz" aus 0500 bei
	 * Titeldaten.
	 * 
	 * @param record nicht null
	 * @return Titeldaten oder null, wenn nicht gefunden.
	 */
	public static String getDatatype(final Record record) {
		final Line line = getTheOnlyLine(record, "002@");
		if (line == null)
			return "";
		return SubfieldUtils.getContentOfFirstSubfield(line, '0');
	}

	/**
	 * Liefert das Zeichen an Stelle index (beginnend bei 0) der Satzart (005 bei
	 * Normdaten und 0500 bei titeldaten).
	 *
	 * @param record nicht null
	 * @param index  beliebig.
	 * @return Zeichen. 0, wenn nicht gefunden oder index zu gross oder zu klein.
	 */
	public static char getDatatypeCharacterAt(final Record record, final int index) {
		final String type = getDatatype(record);
		return StringUtils.charAt(type, index);
	}

	/**
	 * Gibt die PICA-Idn.
	 *
	 * @param record nicht null
	 * @return Nummer oder null
	 */
	public static String getIDN(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		return record.getId();
	}

	/**
	 * Nimmt die DB von record und entfernt die von der SE nicht zu modifizierenden
	 * Felder.
	 *
	 * @param record nicht null.
	 */
	public static void removeUnmodifiables(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Collection<Tag> tags = record.tagDB.getUnmodifiables();
		RecordUtils.removeTags(record, tags);
	}

	//@formatter:on
	private RecordUtils() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static final String PAT_PICA_STR_AUTH = "^005 T";

	public static final Pattern PAT_PICA_AUTH = Pattern.compile(PAT_PICA_STR_AUTH, Pattern.MULTILINE);

	public static final String PAT_PICA_P_STR_AUTH = "002@ .0T";

	public static final Pattern PAT_PICA_P_AUTH = Pattern.compile(PAT_PICA_P_STR_AUTH);

	/**
	 * Enhält 021.
	 */
	public static final String PAT_PICA_STR_SWD = "^021 ";

	/**
	 * Enhält 021.
	 */
	public static final Pattern PAT_PICA_SWD = Pattern.compile(PAT_PICA_STR_SWD, Pattern.MULTILINE);

	/**
	 * Enhält 007Q .0 (pica+).
	 */
	public static final String PAT_PICA_P_STR_SWD = "007Q .0";

	/**
	 * Enhält 007Q .0 (pica+).
	 */
	public static final Pattern PAT_PICA_P_SWD = Pattern.compile(PAT_PICA_P_STR_SWD);

	/**
	 * Enhält 005 oder 0500.
	 */
	public static final String PAT_TYPE_STR = "^005 |^0500 ";

	/**
	 * Enhält 005 oder 0500.
	 */
	public static final Pattern PAT_TYPE_PICA = Pattern.compile(PAT_TYPE_STR, Pattern.MULTILINE);

	/**
	 * Enhält 002@ .0 (pica+).
	 */
	public static final String PAT_TYPE_PICA_STR = "002@ .0";

	/**
	 * Enhält 002@ .0 (pica+).
	 */
	public static final Pattern PAT_TYPE_PICA_P = Pattern.compile(PAT_TYPE_PICA_STR);

	/**
	 * Schlichte, zum Parsen benützte Methode.
	 *
	 * @param recStr nicht null
	 * @return ob Datensatz Normdatensatz ist.
	 */
	public static boolean isAuthority(final CharSequence recStr) {
		RangeCheckUtils.assertReferenceParamNotNull("recStr", recStr);
		Matcher matcher = PAT_PICA_AUTH.matcher(recStr);
		if (matcher.find())
			return true;
		matcher = PAT_PICA_P_AUTH.matcher(recStr);
		if (matcher.find())
			return true;
		return false;
	}

	/**
	 * Schlichte, zum Parsen benützte Methode.
	 *
	 * @param recStr nicht null
	 * @return ob Datensatz Normdatensatz der SWD ist.
	 */
	public static boolean isSWD(final CharSequence recStr) {
		RangeCheckUtils.assertReferenceParamNotNull("recStr", recStr);
		Matcher matcher = PAT_PICA_SWD.matcher(recStr);
		if (matcher.find())
			return true;
		matcher = PAT_PICA_P_SWD.matcher(recStr);
		if (matcher.find())
			return true;
		return false;
	}

	/**
	 * Schlichte, zum Parsen benützte Methode.
	 *
	 * @param recStr nicht null
	 * @return ob aus dem Datensatz der Datensatztyp ermittelbar ist.
	 */
	public static boolean containsRecordType(final CharSequence recStr) {
		Matcher matcher = PAT_TYPE_PICA.matcher(recStr);
		if (matcher.find())
			return true;
		matcher = PAT_TYPE_PICA_P.matcher(recStr);
		if (matcher.find())
			return true;
		return false;
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public static void main2(final String[] args)
			throws IllFormattedLineException, OperationNotSupportedException, IOException {
		final Line line = LineParser.parse("4000 Von der Physik zur Ph$ilosophie / Jörg Fidorra", BibTagDB.getDB(),
				false);
		System.out.println(line);
		System.out.println(toPica(line, Format.PICA_PLUS, true, Constants.DOLLAR));
	}

	/**
	 * Gibt den Pica3-Tag.
	 */
	public static final Function<Line, String> LINE_TO_TAG = new Function<Line, String>() {
		@Override
		public String apply(final Line x) {
			if (x == null)
				return null;
			return x.getTag().pica3;
		}
	};

	/**
	 * Liefert zu einer Menge von Zeilen die Pica3-Tags.
	 *
	 * @param lines nicht null
	 * @return nicht null, enthält keine null. Kann leer sein.
	 */
	public static Collection<String> getTags(final Collection<Line> lines) {
		return FilterUtils.mapNullFiltered(lines, LINE_TO_TAG);
	}

	/**
	 *
	 * @param lines nicht null
	 * @return Größten Tag (Pica3-Nummer) oder null, wenn lines leer
	 */
	public static String getLastTag(final Collection<Line> lines) {
		RangeCheckUtils.assertReferenceParamNotNull("lines", lines);
		return ListUtils.getMax(getTags(lines));
	}

	/**
	 *
	 * @param record nicht null
	 * @return die Zeile, die Quelle und Datum der Ersterfassung enthält oder null
	 */
	public static Line getSourceAndDateEnteredLine(final Record record) {
		return getTheOnlyLine(record, "001A");
	}

	/**
	 * Wertet Feld 001A aus.
	 *
	 * @param record nicht null
	 * @return (Quelle, Datum) der Ersterfassung oder null
	 */
	public static Pair<String, String> getSourceAndDateEntered(final Record record) {
		final Line line = getSourceAndDateEnteredLine(record);
		if (line == null)
			return null;
		final String sd = SubfieldUtils.getSourceAndDateStr(line);
		if (sd == null)
			return null;
		return SubfieldUtils.getSourceAndDateP(sd);
	}

	/**
	 * Wertet Feld 001A aus.
	 *
	 * @param record nicht null
	 * @return Quelle der Ersterfassung (z.B. 1240)
	 */
	public static String getSourceEntered(final Record record) {
		final Pair<String, String> sd = getSourceAndDateEntered(record);
		if (sd == null)
			return null;
		else
			return sd.first;
	}

	/**
	 * Standort (Frankfurt, Leipzig, DMA, anderer)
	 * 
	 * @author baumann
	 *
	 */
	public static enum STANDORT_DNB {
		F("Frankfurt"), L("Leipzig"), M("DMA"), U("anderer Standort");

		final String verbal;

		@Override
		public String toString() {
			return verbal;
		}

		/**
		 * @param string
		 */
		STANDORT_DNB(final String string) {
			verbal = string;
		}

	};

	/**
	 *
	 * @param record nicht null
	 * @return F, L oder U
	 */
	public static STANDORT_DNB getEingebenderStandort(final Record record) {
		final String source = getSourceEntered(record);
		return getStandort(source);

	}

	/**
	 * @param nutzerkennung auch null (aber in der Regl vom Typ 1180)
	 * @return F, L, U (anderer Standort) oder null
	 */
	public static STANDORT_DNB getStandort(final String nutzerkennung) {
		if (nutzerkennung == null)
			return STANDORT_DNB.U;
		if (nutzerkennung.length() != 4)
			return STANDORT_DNB.U;
		if (nutzerkennung.startsWith("11"))
			return STANDORT_DNB.L;
		if (nutzerkennung.startsWith("12"))
			return STANDORT_DNB.F;
		if (nutzerkennung.startsWith("13"))
			return STANDORT_DNB.M;
		return STANDORT_DNB.U;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Quelle der letzten Änderung (z.B. 1240)
	 */
	public static String getSourceLatestTransaction(final Record record) {
		final Pair<String, String> sd = getLatestTransaction(record);
		if (sd == null)
			return null;
		else
			return sd.first;
	}

	/**
	 *
	 * @param record nicht null
	 * @return die Zeile, die Quelle und Datum der letzten Änderung enthält oder
	 *         null
	 */
	public static Line getLatestTransactionLine(final Record record) {
		return getTheOnlyLine(record, "001B");
	}

	/**
	 * @param record nicht null
	 * @return (Quelle, Datum) der letzten Änderung
	 */
	public static Pair<String, String> getLatestTransaction(final Record record) {
		final Line line = getLatestTransactionLine(record);
		final String sd = SubfieldUtils.getSourceAndDateStr(line);
		return SubfieldUtils.getSourceAndDateP(sd);
	}

	/**
	 *
	 * @param record nicht null
	 * @return die Zeile, die Quelle und Datum der letzten Statusvergabe enthält
	 *         oder null
	 */
	public static Line getLatestStatusLine(final Record record) {
		return getTheOnlyLine(record, "001D");
	}

	/**
	 * @param record nicht null
	 * @return (Quelle, Datum) der letzten Statusvergabe
	 */
	public static Pair<String, String> getLatestStatus(final Record record) {
		final Line line = getLatestStatusLine(record);
		final String sd = SubfieldUtils.getSourceAndDateStr(line);
		return SubfieldUtils.getSourceAndDateP(sd);
	}

	/**
	 * 040/1505 (Katalogisierungsquelle) haben beide Pica+ 010E. Daher kann sehr
	 * einfach das Unterfeld $e abgeprüft werden, ob es "rda" (ignore case) enthält.
	 * 040 darf außer "rda" auch noch weitere Codes enthalten, die aber nicht
	 * abgeprüft werden.
	 *
	 * @param record nicht null
	 * @return RDA nach 040/1505 $e
	 */
	public static boolean isRDA(final Record record) {
		return "rda".equalsIgnoreCase(getContentOfSubfield(record, "010E", 'e'));
	}

	/**
	 * @param args
	 * @throws IllFormattedLineException
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public static void main(final String[] args)
			throws IllFormattedLineException, OperationNotSupportedException, IOException {
		Record record = RecordUtils.readFromClip();
		System.out.println(isAuthority(record));
	}

	/**
	 * Wertet Feld 001A aus.
	 *
	 * @param record nicht null
	 * @return Datum der Ersterfassung oder null
	 */
	public static Date getDateEntered(final Record record) {
		final Line line = getSourceAndDateEnteredLine(record);
		if (line == null)
			return null;
		return SubfieldUtils.getDate(line);
	}

	/**
	 * @param record nicht null
	 * @return Datum der letzten Änderung oder null
	 */
	public static Date getDateLatestTransaction(final Record record) {
		final Line line = getLatestTransactionLine(record);
		if (line == null)
			return null;
		final Date d = SubfieldUtils.getDate(line);
		return d;
	}

	/**
	 *
	 * @param record nicht null
	 * @return den Titel eines bibliografischen Datensatzes oder den RDA-Namen eines
	 *         Normdatensatzes oder einen leeren String
	 */
	public static String getTitle(final Record record) {
		String title = "";
		if (isBibliographic(record)) {
			title = BibRecUtils.getResponsibilityAndTitle(record);
			if (title == null)
				title = "";
		} else {
			try {
				title = RDAFormatter.getRDAHeading(record);
			} catch (final Throwable e) {
				try {
					title = GNDUtils.getNameOfRecord(record);
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		return title;
	}

	/**
	 * Liefert zu einer Menge von Tags und einem Indikator alle Inhalte des ersten
	 * Unterfeldes zu diesem Indikator, die NICHT null sind. Ergebnis kann als
	 * Spalte einer Tabelle aufgefasst werden.
	 *
	 * @param record    nicht null
	 * @param indicator beliebig
	 * @param tags      nicht null
	 * @return Inhalt aller Unterfelder, nicht null, modifizierbar
	 */
	public static List<String> getContentsOfFirstSubfield(final Record record, final char indicator,
			final String... tags) {
		final ArrayList<Line> lines = getLines(record, tags);
		return SubfieldUtils.getContentsOfFirstSubfields(lines, indicator);
	}

	/**
	 * Liefert zu einer Menge von Tags und Indikatoren alle Inhalte der Unterfelder
	 * zu diesem Indikator.
	 *
	 * @param record     nicht null
	 * @param indicators nicht null
	 * @param tags       nicht null
	 * @return Inhalt aller Unterfelder, nicht null, modifizierbar
	 */
	public static List<String> getContents(final Record record, final Collection<Character> indicators,
			final Collection<String> tags) {
		final ArrayList<Line> lines = getLinesByTagsGivenAsString(record, tags);
		return SubfieldUtils.getContents(lines, indicators);
	}

	/**
	 * Liefert zu einer Menge von Tags und Indikatoren alle Inhalte der Unterfelder
	 * zu diesem Indikator.
	 *
	 * @param record     nicht null
	 * @param tagPattern nicht null, Pica oder Pica+
	 * @param indicators nicht null
	 * @return Inhalt aller Unterfelder, nicht null, modifizierbar
	 */
	public static List<String> getContents(final Record record, final String tagPattern,
			final Character... indicators) {
		final List<Line> lines = getLines(record, tagPattern);
		return SubfieldUtils.getContents(lines, indicators);
	}

	/**
	 * @param lines beliebig
	 * @return Die Idns, eventuell mehrfach, da kein Set!
	 */
	public static List<String> extractIdns(final Collection<Line> lines) {
		if (lines == null)
			return Collections.emptyList();
		return lines.stream().map(Line::getIdnRelated).filter(idn -> idn != null).collect(Collectors.toList());
	}

	/**
	 * @param lines beliebig
	 * @return Die Idns ohne Prüfziffer, eventuell mehrfach, da kein Set!
	 */
	public static List<Integer> extractIDNints(final Iterable<Line> lines) {
		if (lines == null)
			return Collections.emptyList();
		return Streams.getStreamFromIterable(lines).map(Line::getIdnRelated).filter(idn -> idn != null)
				.map(IDNUtils::ppn2int).collect(Collectors.toList());
	}

	/**
	 * Gibt alle idns ohne Prüfziffer, mit denen der Datensatz verlinkt ist.
	 *
	 * @param record beliebig
	 * @return Die Idns ohne Prüfziffer, eventuell mehrfach, da kein Set!
	 */
	public static List<Integer> getAllIDNints(final Record record) {
		return extractIDNints(record);
	}
}
