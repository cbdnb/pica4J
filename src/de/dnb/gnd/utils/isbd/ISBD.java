package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SG;
import de.dnb.gnd.utils.SGUtils;

public class ISBD implements Comparable<ISBD> {
	// Aufgelistet in der Reihenfolge der Nationalbibliografie:

	// ISBD 0 (Medienart/Medientyp) wäre Feld 0502. Wird in der NaBi nicht
	// wiedergegeben.
	String idnUebergeordnet;

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
	String abhaengigerTitel;
	String uebergeordnetertitel;

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
	String formSW;
	String zielgruppe;

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
	 * @return Schöpfer/Titel. Es wird nicht nach dem EST sortiert!
	 */
	String getSchoepferOderTitelOhneKlammeraffe() {
		String sortiermerkmal;
		if (schoepfer != null) {
			sortiermerkmal = schoepfer;
		} else {
			sortiermerkmal = titel;
		}
		if (sortiermerkmal == null) {
			return null;
		}
		final int pos = sortiermerkmal.indexOf('@');
		if (pos == -1) {
			return sortiermerkmal;
		} else {
			return sortiermerkmal.substring(pos + 1);
		}
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
		if (nachHE != null) {
			nachHE = "";
		}
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
		// @formatter:off
		return  "idn übergeordnet = " + idnUebergeordnet +
				"\ndhs = " + dhs +
				"\ndns = " + dns +
				"\nlc = " + lc +
				"\nzumKatalog = " + zumKatalog +
				"\nneNr = " + neNr +
				"\nschoepfer = " + schoepfer +
				"\nest = " + est +
				"\ntitel = " + titel +
				"\ntitelzusatz = " + titelzusatz +
				"\nverantwortlichkeit = " + verantwortlichkeit +
				"\nzaehlung = " + zaehlung +
				"\nAbhängiger Titel = " + abhaengigerTitel +
				"\nÜbergeordneter Titel = " + uebergeordnetertitel +
				"\nAusgabebezeichnung = " + ausgabebezeichnung +
				"\nVeroeffentlichungsangaben = " + veroeffentlichungsangaben +
				"\nDatum = " + datum +
				"\nWeitereVeroeffAng = " + weitereVeroeffAng +
				"\nFruehere Haupttitel = " + fruehereHaupttitel +
				"\nRepro = " + repro +
				"\nISSN = " + issn +
				"\nAnmerkungFortlaufend = " + anmerkungFortlaufend +
				"\nUmfang = " + umfang +
				"\nGesamttitel = " + gesamttitel +
				"\nLinks = " + links +
				"\nAnmerkung = " + anmerkung +
				"\nHSVermerk = " + hsVermerk +
				"\nISBN/EAN = " + isbnEAN +
				"\nRSWK = " + rswk +
				"\nDDC = " + ddc +
				"\nForm-SW = " + formSW +
				"\nZielgruppe = " + zielgruppe +
				"\nListe NSW = " + listeNSW;
	}


	@Override
	public int hashCode() {
		return Objects.hash(abhaengigerTitel, anmerkung, anmerkungFortlaufend, ausgabebezeichnung, datum, ddc, dhs, dns,
				est, formSW, fruehereHaupttitel, gesamttitel, hsVermerk, idnUebergeordnet, isbnEAN, issn, lc, links,
				listeNSW, neNr, repro, rswk, schoepfer, titel, titelzusatz, uebergeordnetertitel, umfang,
				verantwortlichkeit, veroeffentlichungsangaben, weitereVeroeffAng, zaehlung, zumKatalog, zielgruppe);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ISBD other = (ISBD) obj;
		return Objects.equals(abhaengigerTitel, other.abhaengigerTitel) && Objects.equals(anmerkung, other.anmerkung)
				&& Objects.equals(anmerkungFortlaufend, other.anmerkungFortlaufend)
				&& Objects.equals(ausgabebezeichnung, other.ausgabebezeichnung) && Objects.equals(datum, other.datum)
				&& Objects.equals(ddc, other.ddc) && Objects.equals(dhs, other.dhs) && Objects.equals(dns, other.dns)
				&& Objects.equals(est, other.est) && Objects.equals(formSW, other.formSW)
				&& Objects.equals(fruehereHaupttitel, other.fruehereHaupttitel)
				&& Objects.equals(gesamttitel, other.gesamttitel) && Objects.equals(hsVermerk, other.hsVermerk)
				&& Objects.equals(idnUebergeordnet, other.idnUebergeordnet) && Objects.equals(isbnEAN, other.isbnEAN)
				&& Objects.equals(issn, other.issn) && Objects.equals(lc, other.lc)
				&& Objects.equals(links, other.links) && Objects.equals(listeNSW, other.listeNSW)
				&& Objects.equals(neNr, other.neNr) && Objects.equals(repro, other.repro)
				&& Objects.equals(rswk, other.rswk) && Objects.equals(schoepfer, other.schoepfer)
				&& Objects.equals(titel, other.titel) && Objects.equals(titelzusatz, other.titelzusatz)
				&& Objects.equals(uebergeordnetertitel, other.uebergeordnetertitel)
				&& Objects.equals(umfang, other.umfang) && Objects.equals(verantwortlichkeit, other.verantwortlichkeit)
				&& Objects.equals(veroeffentlichungsangaben, other.veroeffentlichungsangaben)
				&& Objects.equals(weitereVeroeffAng, other.weitereVeroeffAng)
				&& Objects.equals(zaehlung, other.zaehlung) && Objects.equals(zumKatalog, other.zumKatalog) &&  Objects.equals(zielgruppe, other.zielgruppe);
	}

	public static void main(final String[] args) throws IOException {

		final Record record = RecordUtils.readFromClip();
		final Builder builder = new Builder();
		final ISBD isbd = builder.build(record);
		final HTMLformatter formatter = new HTMLformatter(isbd);
		final PrintWriter out = MyFileUtils.outputFile("D:/Analysen/karg/NSW/test.html", false);
		final String formatted = HTMLformatter.PRE_DOCUMENT + formatter.format();
		OutputUtils.show(formatted);
		System.out.println(formatted);
		out.println(formatted);

	}

}
