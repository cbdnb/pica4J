package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.basics.utils.HTMLEntities;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.RecordUtils;

public class HTMLformatter {

	private ISBD isbd;

	/**
	 * @param isbd
	 */
	public HTMLformatter(final ISBD isbd) {
		this.isbd = isbd;
	}

	public void setISBD(final ISBD isbd) {
		this.isbd = isbd;
	}

	public String format() {
		// @formatter:off
		String html =
				"<table>\n"+
					"\t<tr style=\"font-size:9px;\">" +
						"<td>" + HTMLEntities.htmlAngleBrackets(isbd.sgg()) + "</td>" +
						"<td style=\"text-align:right;\">" +
							(isbd.lc!=null?isbd.lc:"") + "</td></tr>\n";
		html += "\t<tr style=\"font-size:9px;\">" +
					"<td >" + isbd.zumKatalog.toHTML() + "</td>" +
					"<td style=\"text-align:right;\">" +
						(isbd.neNr!=null?isbd.neNr:"") + "</td></tr>\n";
		html += "\t<tr style=\"font-size:12px;\">" +
					"<td colspan=\"2\">" +
						"<b>" + isbd.getHaupteintragung() + " </b>" +
						getRest() + "</td></tr>\n";
		if(isbd.rswk!=null) {
			html += "\t<tr style=\"font-size:9px;\" colspan=\"2\"><td>"
					+ "<i>SW: " + isbd.rswk + "</i></td></tr>\n";
		}
		if(isbd.ddc!=null) {
			html += "\t<tr style=\"font-size:9px;\" colspan=\"2\"><td>"
					+ "<i>DDC: " + isbd.ddc + "</i></td></tr>\n";
		}
		html += "</table>";
		return html;
	}

	protected String getRest() {
		// @formatter:on
		final List<String> teile = new ArrayList<>();
		teile.add(isbd.getTitelnachHaupteintragung());

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

		if (isbd.links != null && !isbd.links.isEmpty()) {
			teile.add(StringUtils.concatenate(" . - ", FilterUtils.mapNullFiltered(isbd.links, Link::toHTML)));
		}

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
		return Util.entferneKlammeraffe(StringUtils.concatenate(". - \n", teile));
	}

	public static void main(final String[] args) throws IOException {
		final Record record = RecordUtils.readFromClip();
		final Builder builder = new Builder();
		final ISBD isbd = builder.build(record);
		final HTMLformatter formatter = new HTMLformatter(isbd);
		final PrintWriter out = MyFileUtils.outputFile("D:/Analysen/karg/NSW/test.html", false);
		out.println(formatter.format());

	}

}
