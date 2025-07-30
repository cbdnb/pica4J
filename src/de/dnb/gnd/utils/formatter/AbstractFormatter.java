package de.dnb.gnd.utils.formatter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.parser.Format;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.RecordUtils;

public abstract class AbstractFormatter {

	protected Format format = Format.PICA3;

	protected String recordPre = "";
	protected String recordPost = "";

	protected String linePre = "";
	protected String linePost = "";
	protected String lineSep = Constants.LINE_SEPARATOR;

	protected String tagPre = "";
	protected String tagPost = " ";

	protected String subfieldsPre = "";
	protected String subfieldsPost = "";

	protected String subfieldSeparator = "";
	protected String subfieldPre = "";
	protected String subfieldPost = "";

	protected String indicatorSign = Character.toString(Constants.DOLLAR);
	protected String indicatorPre = "";
	protected String indicatorPost = "";

	protected Tag actualTag;

	/**
	 * Aktuelles Unterfeld ist das erste der Liste.
	 */
	protected boolean isFirstSubfield;

	protected boolean isFirstAttaching;

	protected Indicator actualIndicator;

	protected TagDB tagDB;

	public String format(final Record record) {
		Objects.requireNonNull(record);
		return format(record.getLines(), record.tagDB);
	}

	public String format(final Collection<Line> lines, final TagDB db) {
		Objects.requireNonNull(lines);
		Objects.requireNonNull(db);
		tagDB = db;
		String s = recordPre;
		final Iterator<Line> iterator = lines.iterator();
		for (; iterator.hasNext();) {
			final Line line = iterator.next();
			s += format(line);
			if (iterator.hasNext())
				s += lineSep;
		}
		s += recordPost;
		return s;
	}

	/**
	 *
	 * @param line darf auch null sein, um Leerzeilen zu erzeugen.
	 * @return Formatierte Zeile
	 */
	public String format(final Line line) {
		if (line == null)
			return linePre + linePost;
		actualTag = line.getTag();
		String s = linePre;
		s += formatTag();
		final Collection<Subfield> subfields = line.getSubfields(format);
		s += format(subfields);
		s += linePost;
		return s;
	}

	public String formatWithoutTag(final Line line) {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);
		actualTag = line.getTag();
		final Collection<Subfield> subfields = line.getSubfields(format);
		return format(subfields);
	}

	public String formatTag() {
		String s = tagPre;
		s += getTagString(actualTag);
		s += tagPost;
		return s;
	}

	public String getTagString(final Tag tag) {
		return tag.pica3;
	}

	public String format(final Collection<Subfield> subfields) {
		isFirstSubfield = true;
		isFirstAttaching = true;

		String s = subfieldsPre;
		for (final Iterator<Subfield> iterator = subfields.iterator(); iterator.hasNext();) {
			final Subfield subfield = iterator.next();
			actualIndicator = subfield.getIndicator();
			s += format(subfield);
			isFirstSubfield = false;
			if (iterator.hasNext())
				s += subfieldSeparator;
		}
		s += subfieldsPost;
		return s;
	}

	/**
	 * Standardverhalten. Muss sonst überschrieben werden.
	 *
	 * @param subfield nicht null
	 * @return formatiertes Unterfeld
	 */
	protected String format(final Subfield subfield) {
		String s = subfieldPre;
		s += formatIndicator() + subfield.getContent();
		s += subfieldPost;
		return StringUtils.unicodeComposition(s);
	}

	protected String formatIndicator() {
		return indicatorPre + indicatorSign + getIndicatorChar() + indicatorPost;
	}

	protected String getIndicatorChar() {
		return Character.toString(actualIndicator.indicatorChar);
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		final AbstractFormatter formatter = new AbstractFormatter() {
		};
		final List<Line> lines = record.getLines();
		lines.add(10, null);
		lines.add(10, null);
		lines.add(10, null);
		System.out.println(formatter.format(lines, record.tagDB));

	}

}
