package de.dnb.gnd.utils.isbd;

import java.util.Comparator;
import java.util.List;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.FilterUtils;
import de.dnb.gnd.utils.DDC_SG;
import de.dnb.gnd.utils.SG;
import de.dnb.gnd.utils.SGUtils;

public class ISBD implements Comparable<ISBD> {
	// Aufgelistet in der Reihenfolge der Nationalbibliografie:

	// Zeile 1
	SG dhs;
	List<SG> dns;
	String lc;

	// Zeile 2
	Link zumKatalog;
	String neNr; // Neuerscheinungsdienstnummer (20,N24)

	// Zeile 3
	String schoepfer;

	// Zeile 4
	String est;
	String titel;
	String verantwortlichkeit;
	String zaehlung;

	// Zeile 5
	String ausgabebezeichnung;
	String veroeffentlichungsangabe;
	String datum;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		String zeile1 = "<" + dhs.getDDCString();
		for (SG sg : dns) {
			zeile1 += ";" + sg.getDDCString();
		}
		zeile1 += ">";
		if (lc != null)
			zeile1 += "\t" + lc;
		String zeile2 = zumKatalog.adresse;
		if (neNr != null)
			zeile2 += "\t" + neNr;
		String zeile3 = schoepfer + ":";
		String zeile4 = (est != null ? "[" + est + "] " : "") + titel + " / " + verantwortlichkeit;
		if (zaehlung != null)
			zeile4 += " - " + zaehlung;
		return StringUtils.concatenate("\n", zeile1, zeile2, zeile3, zeile4);
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
