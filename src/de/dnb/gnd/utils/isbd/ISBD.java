package de.dnb.gnd.utils.isbd;

import java.util.Comparator;
import java.util.List;

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
	String titelzusatz;
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

	// RSWK, DDC
	String rswk;
	String ddc;

	// NSW
	String listeNSW;

	/**
	 *
	 * @return [EST] + Titel (4000 $a)
	 */
	public String getHaupttitel() {
		return (est != null ? "[" + est + "] " : "") + titel;
	}

	public String sgg() {
		String zeile1 = "";
		if (dhs != null) {
			zeile1 = "<" + dhs.getDDCString();
			for (final SG sg : dns) {
				zeile1 += ";" + sg.getDDCString();
			}
			zeile1 += ">";
		}
		return zeile1;
	}

	final static Comparator<SG> COMPARATOR = SGUtils.getSGcomparator();

	/**
	 * Nur für Vergleichszwecke!
	 *
	 * @return Schöpfer/EST/Titel
	 */
	String getSchoepferOderTitelOhneKlammeraffe() {
		if (titel == null) {
			return "";
		}
		if (schoepfer != null) {
			return schoepfer;
		}
		String tit = est;
		if (tit == null) {
			tit = titel;
		}
		final int pos = tit.indexOf('@');
		if (pos == -1) {
			return tit;
		}
		return tit.substring(pos + 1);
	}

	/**
	 *
	 * @return Schöpfer bei Werken mit ebensolchem, ansonsten Haupttitel (EST +
	 *         Titel ohne Verantwortlichkeit)
	 */
	public String getHaupteintragung() {
		if (schoepfer != null) {
			return schoepfer + ": ";
		}
		return getHaupttitel();
	}

	public String getTitelnachHaupteintragung() {
		String nachHE = schoepfer != null ? getHaupttitel() : "";
		if (titelzusatz != null) {
			nachHE += titelzusatz;
		}
		if (verantwortlichkeit != null) {
			nachHE += " / " + verantwortlichkeit;
		}
		if (zaehlung != null) {
			nachHE += " - " + zaehlung;
		}
		return nachHE;
	}

	@Override
	public int compareTo(final ISBD o) {
		// sind die Sachgruppen verschieden?
		final int comp = COMPARATOR.compare(dhs, o.dhs);
		if (comp != 0) {
			return comp;
		}
		// SGG sind gleich, jetzt alphabetische Sortierung:
		return getSchoepferOderTitelOhneKlammeraffe().compareToIgnoreCase(o.getSchoepferOderTitelOhneKlammeraffe());
	}

	@Override
	public String toString() {
		return "dhs=" + dhs + "\ndns = " + dns + "\nlc = " + lc + "\nzumKatalog = " + zumKatalog + "\nneNr = " + neNr
				+ "\nschoepfer = " + schoepfer + "\nest = " + est + "\ntitel = " + titel + "\ntitelzusatz = "
				+ titelzusatz + "\nverantwortlichkeit = " + verantwortlichkeit + "\nzaehlung = " + zaehlung
				+ "\nausgabebezeichnung = " + ausgabebezeichnung + "\nveroeffentlichungsangaben = "
				+ veroeffentlichungsangaben + "\ndatum = " + datum + "\nweitereVeroeffAng = " + weitereVeroeffAng
				+ "\nfruehereHaupttitel = " + fruehereHaupttitel + "\nrepro = " + repro + "\nissn = " + issn
				+ "\nanmerkungFortlaufend = " + anmerkungFortlaufend + "\numfang = " + umfang + "\ngesamttitel = "
				+ gesamttitel + "\nlinks = " + links + "\nanmerkung = " + anmerkung + "\nhsVermerk = " + hsVermerk
				+ "\nisbnEAN = " + isbnEAN + "\nrswk = " + rswk + "\nddc = " + ddc + "\nlisteNSW = " + listeNSW;
	}

}
