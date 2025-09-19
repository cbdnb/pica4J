package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.gnd.utils.SG;
import de.dnb.gnd.utils.SGUtils;

public class ISBD implements Comparable<ISBD> {
	// Aufgelistet in der Reihenfolge der Nationalbibliografie:

	// ISBD 0 (Medienart/Medientyp) wäre Feld 0502. Wird in der NaBi nicht
	// wiedergegeben.

	// Zeile 1
	SG dhs;
	List<SG> dns;
	String lc;

	// Zeile 2
	Link zumKatalog;
	String neNr; // Neuerscheinungsdienstnummer (20,N24)

	// Zeile 3
	String schoepfer;

	// Zeile 4 - ISBD 1
	String est;
	String titel;
	String verantwortlichkeit;
	String zaehlung;

	// Zeile 5 - ISBD 4
	String ausgabebezeichnung;
	String veroeffentlichungsangaben;
	String datum;
	String weitereVeroeffAng;

	// Zeile 6
	String fruehereHaupttitel;
	String repro; // Reproduktionsvermerk, 4216
	String issn;
	String anmerkungFortlaufend;

	// Zeile 7 - ISBD 5
	String umfang;

	// Zeile 8 ISBD 6
	String gesamttitel;

	// Zeile 8
	List<Link> links;

	// Zeile 9 - ISBD 7
	String anmerkung;

	// Zeile 10 - ISBD 8
	String hsVermerk;
	String isbnEAN;

	// NSW
	String listeNSW;

	@Override
	public String toString() {
		final List<String> zeilen = new ArrayList<>();

		String zeile1 = "";
		if (dhs != null) {
			zeile1 = "<" + dhs.getDDCString();
			for (final SG sg : dns) {
				zeile1 += ";" + sg.getDDCString();
			}
			zeile1 += ">";
		}
		if (lc != null) {
			zeile1 += "\t" + lc;
		}
		if (!StringUtils.isNullOrEmpty(zeile1)) {
			zeilen.add("z1> " + zeile1);
		}

		String zeile2 = zumKatalog != null ? zumKatalog.url : "";
		if (neNr != null) {
			zeile2 += "\t" + neNr;
		}
		zeilen.add("z2> " + zeile2);

		if (schoepfer != null) {
			zeilen.add("Schöpfer> " + schoepfer + ":");
		}

		String zeile4 = (est != null ? "[" + est + "] " : "") + titel;
		if (verantwortlichkeit != null) {
			zeile4 += " / " + verantwortlichkeit;
		}
		if (zaehlung != null) {
			zeile4 += " - " + zaehlung;
		}
		zeilen.add("z4> " + zeile4);

		String zeile5 = (ausgabebezeichnung != null ? ausgabebezeichnung + " - " : "") + veroeffentlichungsangaben
				+ (datum != null ? ", " + datum : "");
		if (!StringUtils.isNullOrWhitespace(weitereVeroeffAng)) {
			zeile5 += ". - " + weitereVeroeffAng;
		}
		zeilen.add("z5> " + zeile5);

		String zeile6 = fruehereHaupttitel != null ? fruehereHaupttitel : "";
		if (repro != null) {
			zeile6 += " . - " + repro;
		}
		if (!StringUtils.isNullOrWhitespace(zeile6)) {
			zeilen.add("z6> " + zeile6);
		}

		if (anmerkungFortlaufend != null) {
			zeilen.add("Anm. forlaufend> " + anmerkungFortlaufend);
		}

		final List<String> umfetc = new ArrayList<>();
		if (umfang != null) {
			umfetc.add(umfang);
		}
		if (issn != null) {
			umfetc.add(issn);
		}
		zeilen.add("Umfang, ISSN> " + StringUtils.concatenate(" - ", umfetc));

		if (anmerkung != null) {
			zeilen.add("Anmerkung> " + anmerkung);
		}

		if (gesamttitel != null) {
			zeilen.add("Gesamttitel> " + gesamttitel);
		}

		if (links != null && !links.isEmpty()) {
			zeilen.add(
					"Links> " + StringUtils.concatenate(" . - ", FilterUtils.mapNullFiltered(links, Link::toString)));
		}

		String zeile9 = hsVermerk;
		if (isbnEAN != null) {
			if (zeile9 != null) {
				zeile9 += " - " + isbnEAN;
			} else {
				zeile9 = isbnEAN;
			}
		}
		if (zeile9 != null) {
			zeilen.add("ISBN> " + zeile9);
		}

		if (listeNSW != null) {
			zeilen.add("NSW> " + listeNSW);
		}

		return Util.entferneKlammeraffe(zeilen.stream().collect(Collectors.joining("\n")));

	}

	Comparator<SG> myComparator = SGUtils.getSGcomparator();

	String getTitelOhneKlammeraffe() {
		if (titel == null) {
			return "";
		}
		final int pos = titel.indexOf('@');
		if (pos == -1) {
			return titel;
		}
		return StringUtils.ersterBuchstabeGross(titel.substring(pos + 1));
	}

	@Override
	public int compareTo(final ISBD o) {
		// sind die Sachgruppen verschieden?
		final int comp = myComparator.compare(dhs, o.dhs);
		if (comp != 0) {
			return comp;
		}
		// SGG sind gleich, jetzt alphabetische Sortierung:
		return getTitelOhneKlammeraffe().compareToIgnoreCase(o.getTitelOhneKlammeraffe());
	}

}
