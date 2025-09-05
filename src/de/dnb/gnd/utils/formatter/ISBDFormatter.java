package de.dnb.gnd.utils.formatter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.Subfield;
import de.dnb.gnd.parser.line.Line;
import de.dnb.gnd.parser.tag.BibTagDB;
import de.dnb.gnd.parser.tag.Tag;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SubfieldUtils;

public class ISBDFormatter {

	private BibTagDB tagDB;

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
		// Unterfelder durch Deskriptionszeichen ersetzen:
		creator = creator.replace("$b", ". ");
		// Anfang der Klammer suchen:
		Pattern pattern = Pattern.compile("\\$[dgn]");
		Matcher matcher = pattern.matcher(creator);
		if(matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			String first = creator.substring(0, start);
			String second = creator.substring(end);
			creator = first + " (" + second + ")";
		}
		
		creator = creator.replaceAll("\\$.", ", ");
		
		return creator;
	}

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

	public static String getTitel(final Record record) {
		Line line4000 = BibRecUtils.getMainTitleLine(record);
		if (line4000 == null)
			return null;
		List<Subfield> subs = SubfieldUtils.retainSubfields(line4000, 'a', 'd', 'f', 'h');
		if (subs.isEmpty())
			return null;
		return RecordUtils.toPicaWithoutTag(line4000.getTag(), subs);
	}

	public static String entferneTxx(String dollar8) {
		dollar8 = dollar8.replaceFirst(" \\[T[bfgpsukc][1-7z]\\]", "");
		return dollar8;
	}

	/**
	 * 
	 */
	public ISBDFormatter() {
		tagDB = BibTagDB.getDB();
	}

	public static void main(String[] args) {
		Record record = RecordUtils.readFromClip();
		System.out.println(getKoerperschaftVeranstaltung(record));

	}

}
