package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.gnd.exceptions.IllFormattedLineException;
import de.dnb.gnd.parser.Indicator;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.GNDTagDB;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;
import de.dnb.gnd.utils.SubjectUtils;
import de.dnb.gnd.utils.formatter.RDAFormatter;

public class Util {

	/**
	 *
	 * @param record nicht null
	 * @return Körperschaft oder Veranstaltung(!) aus $8 oder Unterfeldern oder null
	 */
	public static String getKoerperschaftVeranstaltung(final Record record) {
		final Pair<Line, Integer> pair = RecordUtils.getFirstLineTagGivenAsString(record, "3100");
		if (pair.second == 0) {
			return null;
		}
		final Line line3100 = pair.first;
		String koerperschaft = SubfieldUtils.getContentOfFirstSubfield(line3100, '8');
		if (koerperschaft == null) {
			final List<Subfield> subs3100 = SubfieldUtils.retainSubfields(line3100, 'a', 'b', 'c', 'd', 'g', 'n', 'x');
			if (subs3100.isEmpty()) {
				return null;
			}
			/*
			 * Jetzt wird es kompliziert, denn wir müssen von 3100 nach 111 übersetzen. 111
			 * nehmen wir, da Veranstaltungen mehr Unterfelder haben.
			 */
			final List<Subfield> subs111 = new ArrayList<>();
			subs3100.forEach(sub3100 -> {
				final char c3100 = sub3100.getIndicator().indicatorChar;
				final String content3100 = sub3100.getContent();
				final Indicator ind111;
				if (c3100 == 'c') {
					ind111 = GNDTagDB.TAG_111.getIndicator('g');
				} else if (c3100 == 'x') {
					ind111 = GNDTagDB.TAG_111.getIndicator('n');
				} else {
					ind111 = GNDTagDB.TAG_111.getIndicator(c3100);
				}
				try {
					subs111.add(new Subfield(ind111, content3100));
				} catch (final IllFormattedLineException e) {
					// nix
				}
			});
			koerperschaft = RecordUtils.toPicaWithoutTag(GNDTagDB.TAG_111, subs111);
			// Veranstaltung wird gewählt, da die umfangreichere Menge an
			// Unterfeldern:
			koerperschaft += " [Tf1]";
		}
		/*
		 * Wird versuchsweise durch die RDA-Form ersetzt, wie man sie z.B. bei
		 * Wiesenmüller findet. Die auskommentierte Form ist die in der aufgegebenen
		 * NaBi verwendete:
		 */
		// koerperschaft = normalisiereKoeVeranst(koerperschaft);
		koerperschaft = RDAFormatter.formatExpansion(koerperschaft);
		return koerperschaft;
	}

	/**
	 *
	 * @param creator nicht null
	 * @return Ersetzt $b durch ". ", klammert alles ab $dgn bis zum Ende ein und
	 *         ersetzt die restlichen $. durch ", ". Das ist so nicht mehr
	 *         RDA-gerecht.
	 */
	@Deprecated
	public static String normalisiereKoeVeranst(String creator) {
		creator = StringUtils.unicodeComposition(creator);
		creator = entferneTxx(creator);
		// Unterfeld $b durch Deskriptionszeichen ". " ersetzen:
		creator = creator.replace("$b", ". ");
		// Anfang der Klammer suchen, die Klammer umschließt alles Weitere bis zum
		// Schluss:
		final Pattern pattern = Pattern.compile("\\$[dgn]");
		final Matcher matcher = pattern.matcher(creator);
		if (matcher.find()) {
			final int start = matcher.start();
			final int end = matcher.end();
			final String first = creator.substring(0, start);
			final String second = creator.substring(end);
			creator = first + " (" + second + ")";
		}

		// Alle weitere $x... werden durch ", " ersetzt:
		creator = creator.replaceAll("\\$.", ", ");
		return creator;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Autor aus $8 oder Unterfeldern oder null
	 */
	public static String getAutor(final Record record) {
		final Pair<Line, Integer> pair = RecordUtils.getFirstLineTagGivenAsString(record, "3000");
		if (pair.second == 0) {
			return null;
		}
		final Line line3000 = pair.first;
		String creator = SubfieldUtils.getContentOfFirstSubfield(line3000, '8');
		if (creator == null) {
			final List<Subfield> subs3000 = SubfieldUtils.retainSubfields(line3000, '5', 'a', 'd', 'c', 'n', 'l');
			if (subs3000.isEmpty()) {
				return null;
			}
			// Jetzt wird es kompliziert, denn wir müssen von 3000 nach 100 übersetzen:
			final List<Subfield> subs100 = new ArrayList<>();
			subs3000.forEach(sub3000 -> {
				final char c3000 = sub3000.getIndicator().indicatorChar;
				final String content3000 = sub3000.getContent();
				final Indicator ind100 = c3000 != '5' ? GNDTagDB.TAG_100.getIndicator(c3000)
						: GNDTagDB.TAG_100.getIndicator('P');
				try {
					subs100.add(new Subfield(ind100, content3000));
				} catch (final IllFormattedLineException e) {
					// nix
				}
			});
			creator = RecordUtils.toPicaWithoutTag(GNDTagDB.TAG_100, subs100);
			creator += " [Tp1]";
		}
		/*
		 * Wird versuchsweise durch RDA ersetzt, da das das aktuellere ist. Die alte
		 * Form ist die, die in der NaBi verwendet wurde.
		 */
		// creator = normalisierePerson(creator);
		creator = RDAFormatter.formatExpansion(creator);
		return creator;
	}

	/**
	 *
	 * @param record nicht null
	 * @return Einheitssachtitel aus 3210/3220, $8 oder Unterfeldern oder null. Man
	 *         könnte (sollte?) auch hier den RDAFormatter benutzen - mal sehen!
	 */
	public static String getEST(final Record record) {
		// Einfachster Fall
		String est = RecordUtils.getContentOfSubfield(record, "3220", 'a');
		if (est != null) {
			return est;
		}

		final Line line3210 = RecordUtils.getTheOnlyLine(record, "3210");
		if (line3210 == null) {
			return null;
		}
		est = SubfieldUtils.getContentOfFirstSubfield(line3210, '8');
		if (est != null) {
			// Autor entfernen:
			final int pos = est.indexOf("$a");
			if (pos != -1) {
				est = est.substring(pos + 2);
			}
			// jetzt noch $k und $o anhängen:
			final List<Subfield> subs = SubfieldUtils.retainSubfields(line3210, 'k', 'o');
			for (final Subfield subfield : subs) {
				est += "$" + subfield.getIndicator().indicatorChar + subfield.getContent();
			}
		} else {
			final List<Subfield> subs = SubfieldUtils.retainSubfields(line3210, 'a', 'f', 'g', 'm', 'n', 'p', 's', 'k',
					'r', 'o');
			if (subs.isEmpty()) {
				return null;
			}
			est = RecordUtils.toPicaWithoutTag(line3210.getTag(), subs);
		}
		est = normalisiereEST(est);
		return est;
	}

	private static String normalisiereEST(String est) {
		est = entferneTxx(est);
		final String[] split = est.split("\\$");
		est = split[0];
		for (int i = 1; i < split.length; i++) {
			final String frac = split[i];
			final char first = StringUtils.charAt(frac, 0);
			final String second = frac.substring(1);
			switch (first) {
			case 'a':
				est += second;
				break;
			case 'p':
			case 'k':
				est += ". " + second;
				break;
			case 'g':
			case 'r':
			case 'f':
				est += " (" + second + ")";
				break;
			case 'o':
			case 's':
				est += " / " + second;
				break;
			case 'm':
			case 'n':
				est += ", " + second;
				break;
			default:
				break;
			}
		}

		return est;
	}

	@Deprecated
	public static String normalisierePerson(String creator) {
		creator = StringUtils.unicodeComposition(creator);
		creator = entferneTxx(creator);
		// Unterfelder durch Deskriptionszeichen ersetzen:
		creator = creator.replace("$c", " ");
		creator = creator.replace("$n", ", ");
		creator = creator.replace("$l", ", ");
		creator = creator.replace("$P", "");
		creator = creator.replace("$d", ", ");
		return creator;
	}

	/**
	 * Titelzusatz und Paralleltitel aus 4000.
	 *
	 * @param record nicht null
	 * @return Titel. Die @ werden nicht entfernt, damit eine Sortierung möglich
	 *         ist.
	 */
	public static String getTitelzusatz(final Record record) {
		final Line line4000 = BibRecUtils.getMainTitleLine(record);
		if (line4000 == null) {
			return null;
		}
		final List<Subfield> subs = SubfieldUtils.retainSubfields(line4000, 'd', 'f');
		if (subs.isEmpty()) {
			return null;
		}
		return RecordUtils.toPicaWithoutTag(line4000.getTag(), subs);
	}

	public static String entferneTxx(String dollar8) {
		dollar8 = dollar8.replaceFirst(" \\[T[bfgpsukc][1-7z]\\]", "");
		return dollar8;
	}

	public static String veroeffentlichungsAngabe(final Line line403X) {
		final List<Subfield> subs = SubfieldUtils.getSubfields(line403X, Arrays.asList('p', 'n'));
		String angabe = RecordUtils.toPicaWithoutTag(line403X.getTag(), subs);
		// jetzt noch Sonderformatierung für $h
		final String dollarH = SubfieldUtils.getContentOfFirstSubfield(line403X, 'h');
		if (dollarH != null) {
			angabe += " [" + dollarH + "]";
		}
		return angabe;
	}

	public static String fruehererHaupttitel(final Line line4213) {
		final List<Subfield> subs = SubfieldUtils.getSubfields(line4213, Arrays.asList('b', 'a'));
		final String titel = RecordUtils.toPicaWithoutTag(line4213.getTag(), subs);
		return titel;
	}

	/**
	 * Ausgabebezeichnung aus 4020.
	 *
	 * @param record nicht null
	 * @return Titel. Die @ werden nicht entfernt, damit eine Sortierung möglich
	 *         ist.
	 */
	public static String ausgabebezeichung(final Record record) {
		// nach aller Erfahrung nur das erste Feld:
		final Pair<Line, Integer> pair = RecordUtils.getFirstLineTagGivenAsString(record, "4020");
		if (pair.second == 0) {
			return null;
		}
		final Line line4020 = pair.first;
		final List<Subfield> subs = SubfieldUtils.retainSubfields(line4020, 'a', 'c');
		if (subs.isEmpty()) {
			return null;
		}
		return RecordUtils.toPicaWithoutTag(line4020.getTag(), subs);
	}

	public static String datum(final Record record) {
		final Line line1100 = RecordUtils.getTheOnlyLine(record, "1100");
		if (line1100 == null) {
			return null;
		}
		if (SubfieldUtils.containsIndicator(line1100, 'n')) {
			String datum = SubfieldUtils.getContentOfFirstSubfield(line1100, 'n');
			if (datum.endsWith("-")) {
				datum += "...";
			}
			return datum;
		}
		if (SubfieldUtils.containsIndicator(line1100, 'a')) {
			if (SubfieldUtils.containsIndicator(line1100, 'b')) {
				return SubfieldUtils.getContentOfFirstSubfield(line1100, 'a') + "-"
						+ SubfieldUtils.getContentOfFirstSubfield(line1100, 'b');
			} else {
				return SubfieldUtils.getContentOfFirstSubfield(line1100, 'a');
			}
		}
		return null;
	}

	public static String umfang(final Record record) {

		String umf = RecordUtils.getContentOfSubfield(record, "4060", 'a');

		final String sonstige = RecordUtils.getContentOfSubfield(record, "4061", 'a');
		if (sonstige != null) {
			if (umf != null) {
				umf += " : " + sonstige;
			} else {
				umf = sonstige;
			}
		}
		final String format = RecordUtils.getContentOfSubfield(record, "4062", 'a');
		if (format != null) {
			if (umf != null) {
				umf += " ; " + format;
			} else {
				umf = format;
			}
		}
		final String begleit = RecordUtils.getContentOfSubfield(record, "4063", 'a');
		if (begleit != null) {
			if (umf != null) {
				umf += " + " + begleit;
			} else {
				umf = begleit;
			}
		}
		return umf;
	}

	/**
	 *
	 * @param line4715 nicht null
	 * @param idn      nicht null
	 * @return auch null, wenn Link ins Leere führt. Links auf die DNB werden als
	 *         korrekt angesehen, um eventuelle Ausfälle des Portals zu umgehen.
	 */
	public static Link link(final Line line4715, final String idn) {
		final String url = SubfieldUtils.getContentOfFirstSubfield(line4715, 'u');
		if (url == null) {
			return null;
		}
		// Entscheiden, ob Inhaltsverzeichnis oder Repository:
		final String dollarc = SubfieldUtils.getContentOfFirstSubfield(line4715, 'c');
		if (url.equals("$") && dollarc.equals("04")) {
			return new Link("Inhaltsverzeichnis", "https://d-nb.info/" + idn + "/04");
		} else if (url.contains("deposit.dnb.de")) {
			return new Link("Inhaltstext", url);
		} else {
			return Link.getLink("Angaben zum Inhalt", url);
		}
	}

	public static String entferneKlammeraffe(final String s) {
		if (StringUtils.isNullOrWhitespace(s)) {
			return s;
		}
		return s.replaceAll("\\@", "");
	}

	public static String gesamttitel(final Record record) {
		String gesamt = RecordUtils.getContentOfSubfield(record, "4180", '8');
		if (gesamt == null) {
			gesamt = RecordUtils.getContentOfSubfield(record, "4180", 'a');
		}
		if (gesamt == null) {
			gesamt = RecordUtils.getContentOfSubfield(record, "4190", 'a');
		}
		if (gesamt == null) {
			return null;
		}
		final String dollarl = RecordUtils.getContentOfSubfield(record, "4180", 'l');
		if (dollarl != null) {
			gesamt += " ; " + dollarl;
		}
		final String dollare = RecordUtils.getContentOfSubfield(record, "4180", 'e');
		if (dollare != null) {
			gesamt += " : " + dollare;
		}
		gesamt = gesamt.replaceAll("[\\[\\]]", "");
		gesamt = "(" + gesamt + ")";
		gesamt = entferneKlammeraffe(gesamt);

		return gesamt;
	}

	public static String isbn(final Record record) {
		final List<String> nummern = new ArrayList<>();
		final ArrayList<Line> lines = RecordUtils.getLines(record, "2000");
		if (lines.isEmpty()) {
			return null;
		}
		for (final Line line : lines) {
			String isbn = SubfieldUtils.getContentOfFirstSubfield(line, '0');
			if (isbn != null) {
				isbn = "ISBN " + isbn;
			}
			final String infos = SubfieldUtils.getContentOfFirstSubfield(line, 'f');
			if (infos != null) {
				if (isbn != null) {
					isbn += " " + infos;
				} else {
					isbn = infos;
				}
			}
			nummern.add(isbn);
		}

		final ArrayList<Line> lines2040 = RecordUtils.getLines(record, "2040");
		if (lines.isEmpty()) {
			return null;
		}
		for (final Line line : lines2040) {
			String isbn = SubfieldUtils.getContentOfFirstSubfield(line, '0');
			if (isbn != null) {
				isbn = "EAN " + isbn;
				final String infos = SubfieldUtils.getContentOfFirstSubfield(line, 'f');
				if (infos != null) {
					isbn += " " + infos;
				}
				nummern.add(isbn);
			}
		}
		return StringUtils.concatenate(" - ", nummern);
	}

	public static String hsVermerk(final Record record) {
		final ArrayList<Line> lines = RecordUtils.getLines(record, "4204");
		if (lines.isEmpty()) {
			return null;
		}
		final List<String> hsVermerke = new ArrayList<>();// Mehrere?
		lines.forEach(line -> {
			hsVermerke.add(line.getSubfields().stream().map(Subfield::getContent).collect(Collectors.joining(", ")));
		});
		return StringUtils.concatenate("; ", hsVermerke);
	}

	public static String issn(final Record record) {
		String issn = RecordUtils.getContentOfSubfield(record, "2005", '0');
		if (issn != null) {
			return "ISSN (autorisiert) " + issn;
		}
		issn = RecordUtils.getContentOfSubfield(record, "2010", '0');
		if (issn != null) {
			return "ISSN der Vorlage " + issn;
		}
		return null;
	}

	public static String anmerkung(final Record record) {
		final List<String> subs = RecordUtils.getContentsOfFirstSubfield(record, 'a', "4201");
		if (subs.isEmpty()) {
			return null;
		}
		return StringUtils.concatenate(" . - ", subs);
	}

	public static String anmerkungFortlaufend(final Record record) {
		final List<String> subs = RecordUtils.getContentsOfFirstSubfield(record, 'a', "4225", "4226");
		if (subs.isEmpty()) {
			return null;
		}
		return StringUtils.concatenate(" . - ", subs);
	}

	public static String listeNSW(final Record record) {
		final Line lineNSW = RecordUtils.getTheOnlyLine(record, "0604");
		if (lineNSW == null) {
			return null;
		}
		String dollara = SubfieldUtils.getContentOfFirstSubfield(lineNSW, 'a');
		dollara = dollara != null ? dollara + ": " : "";
		final String rest = SubfieldUtils.removeSubfields(lineNSW, 'a').stream().map(Subfield::getContent)
				.collect(Collectors.joining(" "));
		return dollara + rest;
	}

	/**
	 *
	 * @param rswkLine
	 * @return RDA-gerechte Darstellung aus $8 (dann wird der Typ ausgewertet) oder
	 *         $a.
	 */
	private static String toSw(final Line rswkLine) {
		final String dollar8 = SubfieldUtils.getContentOfFirstSubfield(rswkLine, '8');
		if (dollar8 != null) {
			return RDAFormatter.formatExpansion(dollar8);
		} else {
			final String dollara = SubfieldUtils.getContentOfFirstSubfield(rswkLine, 'a');
			if (dollara == null) {
				return null;
			} else {
				return dollara.replaceAll("^[zf] ", "");
			}
		}

	}

	public static String rswk(final Record record) {
		final List<String> seqsStr = new ArrayList<>();
		final List<List<Line>> secs = SubjectUtils.getRSWKSequences(record);
		secs.forEach(seq -> {
			final List<String> sww = FilterUtils.mapNullFiltered(seq, Util::toSw);
			if (!sww.isEmpty()) {
				seqsStr.add(StringUtils.concatenate(" ; ", sww));
			}
		});
		if (seqsStr.isEmpty()) {
			return null;
		}
		return StringUtils.concatenate(" ◊ ", seqsStr);
	}

	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		System.out.println(rswk(record));
	}

	public static String abhaengigerTitel(final Record record) {
		final Pair<Line, Integer> pair = RecordUtils.getFirstLineTagGivenAsString(record, "4004");
		if (pair.second == 0) {
			return null;
		}
		final Line line4004 = pair.first;
		final List<Subfield> subs4004 = SubfieldUtils.retainSubfields(line4004, 'a', 'd', 'f', 'h');
		String titel = subs4004.isEmpty() ? "" : RecordUtils.toPicaWithoutTag(line4004.getTag(), subs4004);
		final String dollarl = SubfieldUtils.getContentOfFirstSubfield(line4004, 'l');
		if (dollarl != null) {
			titel = dollarl + ", " + titel;
		}
		return titel;
	}

}
