package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.gnd.utils.DDC_SG;
import de.dnb.gnd.utils.SG;
import de.dnb.gnd.utils.SGUtils;

public class ISBD implements Comparable<ISBD> {
	// Aufgelistet in der Reihenfolge der Nationalbibliografie:

	// ISBD 0 (Medienart/Medientyp) wäre Feld 0502. Wird in der NaBi nicht wiedergegeben.
	
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
	
	// Zeile 7 - ISBD 5
	String umfang;

	@Override
	public String toString() {
		List<String> zeilen = new ArrayList<>();
		String zeile1 = "<" + dhs.getDDCString();
		for (SG sg : dns) {
			zeile1 += ";" + sg.getDDCString();
		}
		zeile1 += ">";
		if (lc != null)
			zeile1 += "\t" + lc;
		zeilen.add(zeile1);
		String zeile2 = zumKatalog.adresse;
		if (neNr != null)
			zeile2 += "\t" + neNr;
		zeilen.add(zeile2);
		if (schoepfer != null)
			zeilen.add(schoepfer + ":");
		String zeile4 = (est != null ? "[" + est + "] " : "") + titel + " / " + verantwortlichkeit;
		if (zaehlung != null)
			zeile4 += " - " + zaehlung;
		zeilen.add(zeile4);
		String zeile5 = (ausgabebezeichnung != null ? ausgabebezeichnung + " - " : "") + veroeffentlichungsangaben
				+ (datum != null ? ", " + datum : "");
		if (weitereVeroeffAng != null)
			zeile5 += ". - " + weitereVeroeffAng;
		zeilen.add(zeile5);

		String zeile6 = fruehereHaupttitel != null ? fruehereHaupttitel : "";
		if (repro != null)
			zeile6 += " . - " + repro;
		if(issn!=null)
			zeile6 += " - ISSN der Vorlage " + issn;
		zeilen.add(zeile6);
		
		if(umfang!=null)
			zeilen.add(umfang);

		return StringUtils.concatenate("\n", zeilen);
	}

	Comparator<SG> myComparator = SGUtils.getSGcomparator();

	String getTitelOhneKlammeraffe() {
		if (titel == null)
			return "";
		int pos = titel.indexOf('@');
		if (pos == -1)
			return titel;
		return StringUtils.ersterBuchstabeGross(titel.substring(pos + 1));
	}

	@Override
	public int compareTo(ISBD o) {
		// sind die Sachgruppen verschieden?
		int comp = myComparator.compare(dhs, o.dhs);
		if (comp != 0)
			return comp;
		// SGG sind gleich, jetzt alphabetische Sortierung:
		return getTitelOhneKlammeraffe().compareToIgnoreCase(o.getTitelOhneKlammeraffe());
	}

}
