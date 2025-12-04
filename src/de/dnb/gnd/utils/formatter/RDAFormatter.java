package de.dnb.gnd.utils.formatter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.parser.tag.TagDB;
import de.dnb.gnd.utils.GNDUtils;
import de.dnb.gnd.utils.IFSubfieldToContent;
import de.dnb.gnd.utils.PersonUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;
import de.dnb.gnd.utils.WorkUtils;

public class RDAFormatter {

	private static final boolean REMOVE_BRACKETS = false;

	protected Record actualRecord;

	protected TagDB tagDB;

	protected boolean useBio = false;

	public final Pair<String, String> SLASH = new Pair<String, String>(" / ", "");

	public final Pair<String, String> KOMMA = new Pair<String, String>(", ", "");

	public final Pair<String, String> DOT = new Pair<String, String>(". ", "");

	public final Pair<String, String> BLANK = new Pair<String, String>(" ", "");

	public final Pair<String, String> NOTHING = new Pair<String, String>("", "");

	public final Pair<String, String> KOMMENT = new Pair<String, String>("* ", "");

	public final Pair<String, String> BRACKETS = new Pair<String, String>(" (", ")");

	public final Pair<String, String> CA = new Pair<String, String>("ca. ", "");

	public final Pair<String, String> TABLE = new Pair<String, String>("T", "--");

	public void setRecord(final Record record) {
//		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		actualRecord = record;
		tagDB = record != null ? record.tagDB : GNDTagDB.getDB();
	}

	public RDAFormatter(final Record record) {
		setRecord(record);
	}

	/**
	 * Sollen nach Personennamen die Lebensdaten ausgegeben werden?
	 *
	 * @param b ja/nein
	 */
	public void useBioData(final boolean b) {
		useBio = b;
	}

	/**
	 *
	 * @param indicator nicht null
	 * @return null, wenn Standardverhalten
	 */
	public final Pair<String, String> getPreAndPost(final Indicator indicator) {
		if (indicator == GNDTagDB.INDICATOR_100_A) {
			return NOTHING;
		}
		if (indicator == GNDTagDB.INDICATOR_100_C) {
			return BLANK;
		}
		if (indicator == GNDTagDB.INDICATOR_100_D) {
			return KOMMA;
		}
		if (indicator == GNDTagDB.INDICATOR_100_L) {
			return KOMMA;
		}
		if (indicator == GNDTagDB.INDICATOR_100_N) {
			return BLANK;
		}
		if (indicator == GNDTagDB.INDICATOR_100_P) {
			return NOTHING;
		}

		if (indicator == GNDTagDB.INDICATOR_110_A) {
			return NOTHING;
		}
		if (indicator == GNDTagDB.INDICATOR_110_B) {
			return DOT;
		}
		if (indicator == GNDTagDB.INDICATOR_110_N) {
			return BRACKETS;
		}

		if (indicator == GNDTagDB.INDICATOR_111_A) {
			return NOTHING;
		}
		if (indicator == GNDTagDB.INDICATOR_111_B) {
			return SLASH;
		}
		if (indicator == GNDTagDB.INDICATOR_111_C) {
			return BLANK;
		}

		if (indicator == GNDTagDB.INDICATOR_130_A) {
			return NOTHING;
		}
		if (indicator == GNDTagDB.INDICATOR_130_F) {
			return BRACKETS;
		}

		if (indicator == GNDTagDB.INDICATOR_130_M) {
			return KOMMA;
		}
		if (indicator == GNDTagDB.INDICATOR_130_N) {
			return KOMMA;
		}
		if (indicator == GNDTagDB.INDICATOR_130_O) {
			return KOMMA;
		}
		if (indicator == GNDTagDB.INDICATOR_130_P) {
			return DOT;
		}
		if (indicator == GNDTagDB.INDICATOR_130_R) {
			return KOMMA;
		}
		if (indicator == GNDTagDB.INDICATOR_130_S) {
			return KOMMA;
		}

		if (indicator == GNDTagDB.INDICATOR_150_A) {
			return NOTHING;
		}

		if (indicator == GNDTagDB.INDICATOR_151_A) {
			return NOTHING;
		}

		if (indicator == GNDTagDB.INDICATOR_548_C) {
			return NOTHING;
		}
		if (indicator == GNDTagDB.INDICATOR_548_D) {
			return CA;
		}

		// Vorsicht bei 151:
		if (indicator == GNDTagDB.DOLLAR_G) {
			return BRACKETS;
		}
		if (indicator == GNDTagDB.DOLLAR_X) {
			return SLASH;
		}
		if (indicator == GNDTagDB.DOLLAR_V_NR) {
			return KOMMENT;
		}
		if (indicator == GNDTagDB.DOLLAR_V_R) {
			return KOMMENT;
		}
		if (indicator == GNDTagDB.INDICATOR_153_A) {
			return NOTHING;
		}
		if (indicator == GNDTagDB.INDICATOR_153_B) {
			return TABLE;
		}
		if (indicator == GNDTagDB.INDICATOR_153_J) {
			return SLASH;
		}

		return null;
	}

	/**
	 *
	 * @param expansion auch null
	 * @return Aus Expansion in $8 gewonnene RDA-gerechte Darstellung.
	 */
	public static String formatExpansion(final String expansion) {
		final RDAFormatter formatter = new RDAFormatter(null);
		formatter.useBioData(false);
		return formatter.format(expansion);
	}

	/**
	 *
	 * @param dollar8 auch null
	 * @return Aus Expansion in $8 gewonnene RDA-gerechte Darstellung.
	 */
	public String format(final String dollar8) {
		final Pair<Line, Line> pair = GNDUtils.dollar8toline(dollar8);
		if (pair == null) {
			return null;
		}
		final Line schoepfer = pair.first;
		String s;
		try {
			s = schoepfer != null ? format(schoepfer) + ". " : "";
		} catch (final IllFormattedLineException e) {
			s = "";
		}
		String rest;
		try {
			rest = format(pair.second);
		} catch (final IllFormattedLineException e) {
			return null;
		}
		return s + rest;

	}

	/**
	 *
	 * @param line nicht null
	 * @return RDA-Format der Zeile mit Deskriptionszeichen, Unicode-Composed
	 * @throws IllFormattedLineException wenn Zeile oder verlinkter Datensatz nicht
	 *                                   korrekt
	 */
	public final String format(final Line line) throws IllFormattedLineException {
		RangeCheckUtils.assertReferenceParamNotNull("line", line);

		final Tag tag = line.getTag();
		if (tag == GNDTagDB.TAG_100 || tag == GNDTagDB.TAG_400) {
			return StringUtils.unicodeComposition(format100(line));
		}
		if (tag == GNDTagDB.TAG_111 || tag == GNDTagDB.TAG_411) {
			return StringUtils.unicodeComposition(format111(line));
		}
		if (tag == GNDTagDB.TAG_130 || tag == GNDTagDB.TAG_430) {
			return StringUtils.unicodeComposition(format130(line));
		}
		if (tag == GNDTagDB.TAG_151 || tag == GNDTagDB.TAG_451) {
			return StringUtils.unicodeComposition(format151(line));
		}
		if (tag == GNDTagDB.TAG_548) {
			return StringUtils.unicodeComposition(format548(line));
		}

		if (tag == GNDTagDB.TAG_110 || tag == GNDTagDB.TAG_410 || tag == GNDTagDB.TAG_150 || tag == GNDTagDB.TAG_450
				|| tag == GNDTagDB.TAG_153) {
			return StringUtils.unicodeComposition(formatSubfields(line.getSubfields()));
		}

		return StringUtils.unicodeComposition(RecordUtils.toPicaWithoutTag(line));
	}

	/**
	 * @param line
	 * @return
	 */
	public String exceptionHandling(final Line line, final Indicator... exceptionalIndicators) {
		final Set<Indicator> excIndSet = new HashSet<Indicator>(Arrays.asList(exceptionalIndicators));
		boolean inBracket = false;
		final List<Subfield> subs = line.getSubfields();
		String s = "";
		for (final Subfield subfield : subs) {
			final Indicator indicator = subfield.getIndicator();
			if (excIndSet.contains(indicator)) {
				if (inBracket == false) {
					s += " (";
				} else {
					s += " : ";
				}
				inBracket = true;
				final String content = getContentWithoutBrackets(subfield);
				s += content;
			} else {
				if (inBracket == true) {
					s += ")";
				}
				inBracket = false;
				s += formatStandard(subfield);
			}
		}
		if (inBracket == true) {
			s += ")";
		}
		return s;
	}

	private String format130(final Line line) throws IllFormattedLineException {
		final String s = formatSubfields(line.getSubfields());
		final String idn = actualRecord != null ? WorkUtils.getAuthorID(actualRecord) : null;
		String p = "";
		if (idn != null) {
			final Record personRecord = RecordUtils.readFromPortal(idn);
			if (personRecord != null) {
				// RDAFormatter formatter = new RDAFormatter(personRecord);
				// Line personLine = GNDUtils.getHeading(personRecord);
				// p = formatter.format(personLine) + ". ";
				p = RDAFormatter.getRDAHeading(personRecord) + ". ";
			}
		}
		return p + s;
	}

	private String format111(final Line line) {
		return exceptionHandling(line, GNDTagDB.INDICATOR_111_N, GNDTagDB.INDICATOR_111_D, GNDTagDB.INDICATOR_111_C);
	}

	private String format100(final Line line) {
		final String p = formatSubfields(line.getSubfields());
		String d = "";
		if (useBio) {
			Line line548 = null;
			try {
				line548 = PersonUtils.getDatlLine(actualRecord);
			} catch (final IllegalStateException e) {// nix
			}

			if (line548 != null) {
				d = ", " + format548(line548);
			}
		}
		return p + d;
	}

	private String format151(final Line line) {
		return exceptionHandling(line, GNDTagDB.INDICATOR_151_Z, GNDTagDB.DOLLAR_G);
	}

	private String format548(final Line line) {
		final List<Subfield> subs = line.getSubfields();
		final Pair<List<Subfield>, List<Subfield>> lists = FilterUtils.divide(subs,
				isInIndicatorList(GNDTagDB.INDICATOR_548_A, GNDTagDB.INDICATOR_548_B));
		final List<Subfield> ab = lists.first;
		if (ab.isEmpty()) {
			return formatSubfields(lists.second);
		}

		String span = "-";
		for (final Subfield subfield : ab) {
			if (subfield.getIndicator() == GNDTagDB.INDICATOR_548_A) {
				span = subfield.getContent() + "-";
			} else {
				span += subfield.getContent();
			}
		}
		return span;
	}

	public String formatSubfields(final Collection<Subfield> subfields) {
		String content = "";
		for (final Subfield subfield : subfields) {
			content += formatStandard(subfield);
		}
		return content;
	}

	/**
	 * Standardverhalten. Unterfelder, die nicht in getPreAndPost() erfasst sind,
	 * also mit null erfasst, führen zu einem Leerstring.
	 *
	 * @param pair
	 * @param subfield
	 * @return prä + content + post / ""
	 */
	public final String formatStandard(final Subfield subfield) {
		final Indicator indicator = subfield.getIndicator();
		final Pair<String, String> pair = getPreAndPost(indicator);
		if (pair == null) {
			return "";
		} else {
			final String content = getContentWithoutBrackets(subfield);
			return pair.first + content + pair.second;
		}
	}

	/**
	 * Sonderbehandlung für Klammern (im Augenblick abgeschaltet).
	 *
	 * @param subfield
	 * @param indicator
	 * @return
	 */
	public String getContentWithoutBrackets(final Subfield subfield) {
		String content = subfield.getContent();
		final Indicator indicator = subfield.getIndicator();
		if (REMOVE_BRACKETS) {
			if (indicator == GNDTagDB.DOLLAR_G) {
				content = content.replace(" (", ", ");
				content = content.replace(")", "");
			}
		}
		return content;
	}

	Predicate<Subfield> isInIndicatorList(final Indicator... indicators) {
		final Set<Indicator> set = new HashSet<>(Arrays.asList(indicators));
		return new Predicate<Subfield>() {
			@Override
			public boolean test(final Subfield subfield) {
				return set.contains(subfield.getIndicator());
			}
		};
	}

	Collection<String> getContents(final Collection<Subfield> subfields) {
		return FilterUtils.map(subfields, new IFSubfieldToContent());
	}

	/**
	 *
	 * @param record nicht null
	 * @return RDA-Ansetzung mit Deskriptionszeichen, Unicode-composed
	 * @throws IllFormattedLineException wenn Datensatz nicht korrekt formatiert ist
	 */
	public static String getRDAHeading(final Record record) throws IllFormattedLineException {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		try {
			final Line line = GNDUtils.getHeading(record);
			final RDAFormatter rdaFormatter = new RDAFormatter(record);
			return rdaFormatter.format(line);
		} catch (final Exception e) {
			System.err.println(e);
			throw new IllFormattedLineException(e.getMessage());
		}
	}

	/**
	 *
	 * @param record nicht null
	 * @return RDA-Ansetzung mit Deskriptionszeichen, ohne $v
	 * @throws IllFormattedLineException wenn Datensatz nicht korrekt formatiert ist
	 */
	public static String getPureRDAHeading(final Record record) throws IllFormattedLineException {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		try {
			Line line = GNDUtils.getHeading(record);
			line = SubfieldUtils.getNewLineRemovingSubfields(line, 'v');
			if (line == null) {
				return null;
			}
			final RDAFormatter rdaFormatter = new RDAFormatter(record);
			return rdaFormatter.format(line);
		} catch (final Exception e) {
			throw new IllFormattedLineException(e.getMessage());
		}
	}

	/**
	 *
	 * @param record nicht null
	 * @return RDA-Ansetzungen der Verweisungen mit Deskriptionszeichen
	 *
	 */
	public static Set<String> getRDAVerweise(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Set<String> verweise = new LinkedHashSet<>();
		final List<Line> lines = GNDUtils.getLines4XX(record);
		final RDAFormatter rdaFormatter = new RDAFormatter(record);
		lines.forEach(line -> {
			try {
				final String verweis = rdaFormatter.format(line);
				verweise.add(verweis);
			} catch (final Exception e) {
				// nix
			}
		});

		return verweise;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Set der RDA-Ansetzungen der Verweisungen mit Deskriptionszeichen,
	 *         ohne $T, $U, $L, $4, $5, $v, Unicode-composed. Kann verändert werden.
	 *
	 */
	public static Set<String> getReineRDAVerweise(final Record record) {
		RangeCheckUtils.assertReferenceParamNotNull("record", record);
		final Set<String> verweise = new LinkedHashSet<>();
		final List<Line> lines = GNDUtils.getLines4XX(record);
		final RDAFormatter rdaFormatter = new RDAFormatter(record);
		lines.forEach(line -> {
			try {
				line = SubfieldUtils.getNewLineRemovingSubfields(line, 'T', 'U', 'L', '4', '5', 'v');
				if (line == null) {
					return;
				}
				final String verweis = rdaFormatter.format(line);
				verweise.add(verweis);
			} catch (final Exception e) {
				// nix
			}
		});

		return verweise;
	}

	public static void main(final String[] args) throws IllFormattedLineException {
		final Record record = RecordUtils.readFromClip();
		System.out.println(getPureRDAHeading(record));
	}

}
