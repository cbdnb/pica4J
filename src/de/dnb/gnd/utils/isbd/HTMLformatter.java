package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.utils.HTMLEntities;
import de.dnb.basics.utils.HTMLUtils;
import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.utils.SG;

public class HTMLformatter {

	private static final String ZEILE_ENDE = "</td></tr>\n";
	private static final String SE_ZEILE_ENDE = "</i></td></tr>\n";
	private static final String SE_ZEILE_ANFANG = "\t<tr style=\"font-size:9px;\" colspan=\"2\"><td><i>";
	private static final String TITEL_ZEILE = "\t<tr style=\"font-size:9px;\">";
	private ISBD isbd;

	/**
	 * @param isbd
	 */
	public HTMLformatter(final ISBD isbd) {
		this.isbd = isbd;
	}

	public HTMLformatter() {
		this.isbd = null;
	}

	public void setISBD(final ISBD isbd) {
		this.isbd = isbd;
	}

	private String getHaupteintragung() {
		if (isbd.abhaengigerTitel == null) {
			final String haupteintragung = "<b>" + isbd.getHaupteintragung();
			if (haupteintragung.contains("]")) {
				return haupteintragung.replace("]", "] </b>");
			} else {
				return haupteintragung + " </b>";
			}
		} else {
			return HANGING_PRE + isbd.abhaengigerTitel;// .replaceFirst(", ", "<br>");
		}
	}

	public String format(final WV wv) {
		String s = PRE_DOCUMENT;
		final Collection<Eintragsliste> listen = wv.getEintragslisten();
		for (final Eintragsliste liste : listen) {
			s += "\n" + format(liste);
		}
		return s;
	}

	public String format(final Eintragsliste liste) {
		String s = "";
		final SG dhs = liste.dhs;
		String dhsStr;
		if (dhs == null) {
			dhsStr = "Keine Hauptsachgruppe vergeben";
		} else {
			dhsStr = dhs.getDDCString() + " " + dhs.getDescription();
		}
		s += "\n<br>" + HTMLUtils.heading(dhsStr, 4);
		final Collection<Eintrag> eintraege = liste.getEintraege();
		for (final Eintrag eintrag : eintraege) {
			s += "\n<br>" + format(eintrag);
		}
		return s;
	}

	public String format(final Eintrag eintrag) {
		String s = "";
		s += "\n" + format(eintrag.isbd);
		final Collection<ISBD> untergeordnete = eintrag.getUntergeordnete();
		for (final ISBD isbd : untergeordnete) {
			s += "\n" + format(isbd);
		}
		return s;
	}

	public String format(final ISBD isbd) {
		if (isbd == null) {
			return null;
		}
		setISBD(isbd);
		return format();
	}

	public String format() {
		// @formatter:off
		String html =
				"<table>\n"+
					TITEL_ZEILE +
						"<td>" + HTMLEntities.htmlAngleBrackets(isbd.sgg()) + "</td>" +
						"<td style=\"text-align:right;\">" +
							(isbd.lc!=null?isbd.lc:"")
					+ ZEILE_ENDE;
		html += TITEL_ZEILE +
					"<td >" + isbd.zumKatalog.toHTML() + "</td>" +
					"<td style=\"text-align:right;\">" +
						(isbd.neNr!=null?isbd.neNr:"")
				+ ZEILE_ENDE;
		html += "\t<tr style=\"font-size:12px;\">" +
					"<td colspan=\"2\">" +
						getHaupteintragung() +
						getRest()
				+ ZEILE_ENDE;
		if(isbd.rswk!=null) {
			html += SE_ZEILE_ANFANG
					+ "SW: " + isbd.rswk
					+ SE_ZEILE_ENDE;
		}
		if(isbd.ddc!=null) {
			html += SE_ZEILE_ANFANG
					+ "DDC: " + isbd.ddc
					+ SE_ZEILE_ENDE;
		}
		if(isbd.formSW!=null) {
			html += SE_ZEILE_ANFANG
					+ "FSW: " + isbd.formSW
					+ SE_ZEILE_ENDE;
		}
		if(isbd.zielgruppe!=null) {
			html += SE_ZEILE_ANFANG
					+ "Zielgruppe: "+isbd.zielgruppe
					+ SE_ZEILE_ENDE;
		}
		if(isbd.listeNSW!=null) {
			html += SE_ZEILE_ANFANG
					+ isbd.listeNSW
					+ SE_ZEILE_ENDE;
		}
		html += "</table>";
		// Wenn man vom Portal lädt, kann das auftreten:
		html = html.replace("[[", "[");
		html = html.replace("]]", "]");
		html = Util.entferneKlammeraffe(html);
		return html;
	}

	public static String PRE_DOCUMENT =
		"<div class=\"mehrspaltig\">"+
		"<style>\r\n"
		+ "blockquote {\r\n"
		+ "  margin-left: 1rem;\r\n"
		+ "}\r\n" +
//		+ "p {\r\n"
//		+ "   text-indent: -1rem;\r\n"
//		+ "}\r\n"
		".mehrspaltig {\r\n"
		+ "	columns: 3 16em;\r\n"
		+ "	hyphens: auto;\r\n"
		+ "	orphans: 3;\r\n"
		+ "}"
		+ "</style>\n";


	public static final String HANGING_PRE = "<blockquote><p>";
	public static final String HANGING_POST = "</p></blockquote>";
	private List<String> teile;

	protected String getRest() {
		final List<String> teile = restVorLinks();
		if (isbd.links != null && !isbd.links.isEmpty()) {
			teile.add(getLinks());
		}
		teile.addAll(restNachLinks());
		String ergebnis = Util.entferneKlammeraffe(StringUtils.concatenate(". - \n", teile));
		if (isbd.abhaengigerTitel != null) {
			ergebnis += HANGING_POST;
		}
		return ergebnis;
	}

	public String getLinks() {
		return StringUtils.concatenate(" . - ", FilterUtils.mapNullFiltered(isbd.links, Link::toHTML));
	}

	public List<String> restNachLinks() {
		final ArrayList<String>teile = new ArrayList<>();
		if (isbd.anmerkung != null) {
			teile.add(isbd.anmerkung);
		}

		String hsVermerkPlusISBN = isbd.hsVermerk;
		if (isbd.isbnEAN != null) {
			if (hsVermerkPlusISBN != null) {
				hsVermerkPlusISBN += " - " + isbd.isbnEAN;
			} else {
				hsVermerkPlusISBN = isbd.isbnEAN;
			}
		}
		if (hsVermerkPlusISBN != null) {
			teile.add(hsVermerkPlusISBN);
		}
		return teile;
	}

	public List<String> restVorLinks() {
		final ArrayList<String> teile = new ArrayList<>();
		if (isbd.getTitelnachHaupteintragung() != null) {
			teile.add(isbd.getTitelnachHaupteintragung());
		}

		String ausgabeBezBisWeiterVeroff = "";
		if (isbd.ausgabebezeichnung != null) {
			ausgabeBezBisWeiterVeroff = isbd.ausgabebezeichnung;
		}
		if (!StringUtils.isNullOrWhitespace(isbd.veroeffentlichungsangaben)) {
			if (!ausgabeBezBisWeiterVeroff.isBlank()) {
				ausgabeBezBisWeiterVeroff += ". - ";
			}
			ausgabeBezBisWeiterVeroff += isbd.veroeffentlichungsangaben;
		}
		if (isbd.datum != null) {
			ausgabeBezBisWeiterVeroff += ", " + isbd.datum;
		}
		if (!StringUtils.isNullOrWhitespace(isbd.weitereVeroeffAng)) {
			ausgabeBezBisWeiterVeroff += ". - " + isbd.weitereVeroeffAng;
		}
		if (!StringUtils.isNullOrWhitespace(ausgabeBezBisWeiterVeroff)) {
			teile.add(ausgabeBezBisWeiterVeroff);
		}

		String fruehererHTplusrepro = isbd.fruehereHaupttitel != null ? isbd.fruehereHaupttitel : "";
		if (isbd.repro != null) {
			fruehererHTplusrepro += " . - " + isbd.repro;
		}
		if (!StringUtils.isNullOrWhitespace(fruehererHTplusrepro)) {
			teile.add(fruehererHTplusrepro);
		}

		if (isbd.anmerkungFortlaufend != null) {
			teile.add(isbd.anmerkungFortlaufend);
		}

		final List<String> umfangPlusISSN = new ArrayList<>();
		if (isbd.umfang != null) {
			umfangPlusISSN.add(isbd.umfang);
		}
		if (isbd.issn != null) {
			umfangPlusISSN.add(isbd.issn);
		}
		final String concatenated = StringUtils.concatenate(" - ", umfangPlusISSN);
		if (!StringUtils.isNullOrWhitespace(concatenated)) {
			teile.add(concatenated);
		}

		if (isbd.gesamttitel != null) {
			teile.add(isbd.gesamttitel);
		}
		return teile;
	}

	public static void main(final String[] args) throws IOException {
		final WV wv = WV.createWV("D:/Analysen/karg/NSW/nsw.txt", "D:/Analysen/karg/NSW/nswUeber.txt");
		final HTMLformatter formatter = new HTMLformatter();
		final String html = formatter.format(wv);

		final PrintWriter out = MyFileUtils.outputFile("D:/Analysen/karg/NSW/NSW-test.html", false);
//		final String formatted = PRE_DOCUMENT + formatter.format();
		OutputUtils.show(html);
		out.println(html);
		System.out.println(html);

	}

}
