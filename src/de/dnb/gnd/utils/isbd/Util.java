package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.gnd.parser.Field;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;

public class Util {

	/**
	 * 
	 * @param record nicht null
	 * @return Körperschaft oder Veranstaltung(!) aus $8 oder Unterfeldern oder null
	 */
	public static String getKoerperschaftVeranstaltung(Record record) {
		Line line3100 = RecordUtils.getTheOnlyLine(record, "3100");
		if (line3100 == null)
			return null;
		String koerperschaft = SubfieldUtils.getContentOfFirstSubfield(line3100, '8');
		if (koerperschaft == null) {
			List<Subfield> subs = SubfieldUtils.retainSubfields(line3100, 'a', 'b', 'c', 'd', 'g', 'n');
			if (subs.isEmpty())
				return null;
			koerperschaft = RecordUtils.toPicaWithoutTag(line3100.getTag(), subs);
		}
		koerperschaft = normalisiereKoeVeranst(koerperschaft);
		return koerperschaft;
	}

	private static String normalisiereKoeVeranst(String creator) {
		creator = StringUtils.unicodeComposition(creator);
		creator = entferneTxx(creator);
		// Unterfeld $b durch Deskriptionszeichen ". " ersetzen:
		creator = creator.replace("$b", ". ");
		// Anfang der Klammer suchen, die Klammer umschließt alles Weitere bis zum
		// Schluss:
		Pattern pattern = Pattern.compile("\\$[dgn]");
		Matcher matcher = pattern.matcher(creator);
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			String first = creator.substring(0, start);
			String second = creator.substring(end);
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
	public static String getAutor(Record record) {
		Line line3000 = RecordUtils.getTheOnlyLine(record, "3000");
		if (line3000 == null)
			return null;
		String creator = SubfieldUtils.getContentOfFirstSubfield(line3000, '8');
		if (creator == null) {
			List<Subfield> subs = SubfieldUtils.retainSubfields(line3000, 'P', 'a', 'd', 'c', 'n', 'l');
			if (subs.isEmpty())
				return null;
			creator = RecordUtils.toPicaWithoutTag(line3000.getTag(), subs);
		}
		creator = normalisierePerson(creator);
		return creator;
	}

	/**
	 * 
	 * @param record nicht null
	 * @return Einheitssachtitel aus 3210/3220, $8 oder Unterfeldern oder null
	 */
	public static String getEST(Record record) {
		// Einfachster Fall
		String est = RecordUtils.getContentOfSubfield(record, "3220", 'a');
		if (est != null)
			return est;

		Line line3210 = RecordUtils.getTheOnlyLine(record, "3210");
		if (line3210 == null)
			return null;
		est = SubfieldUtils.getContentOfFirstSubfield(line3210, '8');
		if (est != null) {
			// Autor entfernen:
			int pos = est.indexOf("$a");
			if (pos != -1)
				est = est.substring(pos + 2);
			// jetzt noch $k und $o anhängen:
			List<Subfield> subs = SubfieldUtils.retainSubfields(line3210, 'k', 'o');
			for (Subfield subfield : subs) {
				est += "$" + subfield.getIndicator().indicatorChar + subfield.getContent();
			}
		} else {
			List<Subfield> subs = SubfieldUtils.retainSubfields(line3210, 'a', 'f', 'g', 'm', 'n', 'p', 's', 'k', 'r',
					'o');
			if (subs.isEmpty())
				return null;
			est = RecordUtils.toPicaWithoutTag(line3210.getTag(), subs);
		}
		est = normalisiereEST(est);
		return est;
	}

	private static String normalisiereEST(String est) {
		est = entferneTxx(est);
		String[] split = est.split("\\$");
		est = split[0];
		for (int i = 1; i < split.length; i++) {
			String frac = split[i];
			char first = StringUtils.charAt(frac, 0);
			String second = frac.substring(1);
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

	private static String normalisierePerson(String creator) {
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
	 * Titel aus 4000.
	 * 
	 * @param record nicht null
	 * @return Titel. Die @ werden nicht entfernt, damit eine Sortierung möglich
	 *         ist.
	 */
	public static String getTitel(final Record record) {
		Line line4000 = BibRecUtils.getMainTitleLine(record);
		if (line4000 == null)
			return null;
		List<Subfield> subs = SubfieldUtils.retainSubfields(line4000, 'a', 'd', 'f');
		if (subs.isEmpty())
			return null;
		return RecordUtils.toPicaWithoutTag(line4000.getTag(), subs);
	}

	public static String entferneTxx(String dollar8) {
		dollar8 = dollar8.replaceFirst(" \\[T[bfgpsukc][1-7z]\\]", "");
		return dollar8;
	}

	public static String veroeffentlichungsAngabe(Line line403X) {
		List<Subfield> subs = SubfieldUtils.getSubfields(line403X, Arrays.asList('p', 'n'));
		String angabe = RecordUtils.toPicaWithoutTag(line403X.getTag(), subs);
		// jetzt noch Sonderformatierung für $h
		String dollarH = SubfieldUtils.getContentOfFirstSubfield(line403X, 'h');
		if (dollarH != null)
			angabe += " [" + dollarH + "]";
		return angabe;
	}

	public static String fruehererHaupttitel(Line line4213) {
		List<Subfield> subs = SubfieldUtils.getSubfields(line4213, Arrays.asList('b', 'a'));
		String titel = RecordUtils.toPicaWithoutTag(line4213.getTag(), subs);
		return titel;
	}

	/**
	 * Titel aus 4000.
	 * 
	 * @param record nicht null
	 * @return Titel. Die @ werden nicht entfernt, damit eine Sortierung möglich
	 *         ist.
	 */
	public static String ausgabebezeichung(final Record record) {
		// nach aller Erfahrung nur das erste Feld:
		Pair<Line, Integer> pair = RecordUtils.getFirstLineTagGivenAsString(record, "4020");
		if (pair.second == 0)
			return null;
		Line line4020 = pair.first;
		List<Subfield> subs = SubfieldUtils.retainSubfields(line4020, 'a', 'c');
		if (subs.isEmpty())
			return null;
		return RecordUtils.toPicaWithoutTag(line4020.getTag(), subs);
	}

	public static String datum(Record record) {
		Line line1100 = RecordUtils.getTheOnlyLine(record, "1100");
		if (line1100 == null)
			return null;
		if (SubfieldUtils.containsIndicator(line1100, 'n')) {
			String datum = SubfieldUtils.getContentOfFirstSubfield(line1100, 'n');
			if (datum.endsWith("-"))
				datum += "...";
			return datum;
		}
		if (SubfieldUtils.containsIndicator(line1100, 'a')) {
			if (SubfieldUtils.containsIndicator(line1100, 'b'))
				return SubfieldUtils.getContentOfFirstSubfield(line1100, 'a') + "-"
						+ SubfieldUtils.getContentOfFirstSubfield(line1100, 'b');
			else
				return SubfieldUtils.getContentOfFirstSubfield(line1100, 'a');
		}
		return null;
	}

	public static String umfang(Record record) {

		String umf = RecordUtils.getContentOfSubfield(record, "4060", 'a');

		String sonstige = RecordUtils.getContentOfSubfield(record, "4061", 'a');
		if (sonstige != null) {
			if (umf != null)
				umf += " : " + sonstige;
			else
				umf = sonstige;
		}
		String format = RecordUtils.getContentOfSubfield(record, "4062", 'a');
		if (format != null) {
			if (umf != null)
				umf += " ; " + format;
			else
				umf = format;
		}
		String begleit = RecordUtils.getContentOfSubfield(record, "4063", 'a');
		if (begleit != null) {
			if (umf != null)
				umf += " + " + begleit;
			else
				umf = begleit;
		}
		return umf;
	}

	public static Link link(Line line4715, String idn) {
		String url = SubfieldUtils.getContentOfFirstSubfield(line4715, 'u');
		if (url == null)
			return null;
		// Entscheiden, ob Inhaltsverzeichnis oder Repository:
		String dollarc = SubfieldUtils.getContentOfFirstSubfield(line4715, 'c');
		if (url.equals("$") && dollarc.equals("04"))
			return Link.getLink("Inhaltsverzeichnis", "http://d-nb.info/" + idn + "/04");
		else if (url.contains("deposit.dnb.de"))
			return Link.getLink("Inhaltstext", url);
		else
			return Link.getLink("Angaben zum Inhalt", url);
	}

	public static String entferneKlammeraffe(String s) {
		if (StringUtils.isNullOrWhitespace(s))
			return s;
		return s.replaceAll("\\@", "");
	}

	public static String gesamttitel(Record record) {
		String gesamt = RecordUtils.getContentOfSubfield(record, "4180", '8');
		if (gesamt == null)
			gesamt = RecordUtils.getContentOfSubfield(record, "4180", 'a');
		if (gesamt == null)
			gesamt = RecordUtils.getContentOfSubfield(record, "4190", 'a');
		if (gesamt == null)
			return null;
		String dollarl = RecordUtils.getContentOfSubfield(record, "4180", 'l');
		if (dollarl != null)
			gesamt += " ; " + dollarl;
		String dollare = RecordUtils.getContentOfSubfield(record, "4180", 'e');
		if (dollare != null)
			gesamt += " : " + dollare;
		gesamt = gesamt.replaceAll("[\\[\\]]", "");
		gesamt = "(" + gesamt + ")";
		gesamt = entferneKlammeraffe(gesamt);

		return gesamt;
	}

	public static String isbn(Record record) {
		List<String> nummern = new ArrayList<>();
		ArrayList<Line> lines = RecordUtils.getLines(record, "2000");
		if (lines.isEmpty())
			return null;
		for (Line line : lines) {
			String isbn = SubfieldUtils.getContentOfFirstSubfield(line, '0');
			if (isbn != null) {
				isbn = "ISBN " + isbn;
				String infos = SubfieldUtils.getContentOfFirstSubfield(line, 'f');
				if (infos != null)
					isbn += " " + infos;
				nummern.add(isbn);
			}
		}

		ArrayList<Line> lines2040 = RecordUtils.getLines(record, "2040");
		if (lines.isEmpty())
			return null;
		for (Line line : lines2040) {
			String isbn = SubfieldUtils.getContentOfFirstSubfield(line, '0');
			if (isbn != null) {
				isbn = "EAN " + isbn;
				String infos = SubfieldUtils.getContentOfFirstSubfield(line, 'f');
				if (infos != null)
					isbn += " " + infos;
				nummern.add(isbn);
			}
		}
		return StringUtils.concatenate(" - ", nummern);
	}

	public static String hsVermerk(Record record) {
		ArrayList<Line> lines = RecordUtils.getLines(record, "4204");
		if (lines.isEmpty())
			return null;
		List<String> hsVermerke = new ArrayList<>();// Mehrere?
		lines.forEach(line -> {
			hsVermerke.add(line.getSubfields().stream().map(Subfield::getContent).collect(Collectors.joining(", ")));
		});
		return StringUtils.concatenate("; ", hsVermerke);
	}

	public static String issn(Record record) {
		String issn = RecordUtils.getContentOfSubfield(record, "2005", '0');
		if (issn != null)
			return "ISSN (autorisiert) " + issn;
		issn = RecordUtils.getContentOfSubfield(record, "2010", '0');
		if (issn != null)
			return "ISSN der Vorlage " + issn;
		return null;
	}

	public static String anmerkung(Record record) {
		List<String> subs = RecordUtils.getContentsOfFirstSubfield(record, 'a', "4201");
		if (subs.isEmpty())
			return null;
		return StringUtils.concatenate(" . - ", subs);
	}

	public static String anmerkungFortlaufend(Record record) {
		List<String> subs = RecordUtils.getContentsOfFirstSubfield(record, 'a', "4225", "4226");
		if (subs.isEmpty())
			return null;
		return StringUtils.concatenate(" . - ", subs);
	}

	public static String listeNSW(Record record) {		
		Line lineNSW = RecordUtils.getTheOnlyLine(record, "0604");		
		if (lineNSW == null)
			return null;
		String dollara = SubfieldUtils.getContentOfFirstSubfield(lineNSW, 'a');		
		dollara = dollara != null ? dollara + ": " : "";
		String rest = SubfieldUtils.removeSubfields(lineNSW, 'a').stream().map(Subfield::getContent)
				.collect(Collectors.joining(" "));
		return dollara + rest;
	}

//	public static void main(String[] args) {
//		Record record = RecordUtils.readFromClip();
//		System.out.println(isbn(record));
//	}

}
