package de.dnb.gnd.utils.isbd;

import java.util.List;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.utils.DDC_SG;
import de.dnb.gnd.utils.SG;

public class ISBD {
	// Aufgelistet in der Reihenfolge der Nationalbibliografie:

	// Zeile 1
	SG dhs;
	List<SG> dns;
	String lc;

	// Zeile 2
	Link zumKatalog;
	String neNr; // Neuerscheinungsdienstnummer (20,N24)

	// Zeile 3
	String titel;
	String verantwortlichkeit;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		String zeile1 = dhs.toString() + " " + dns + "\t" + lc;
		String zeile2 = zumKatalog.toString() + "\t" + neNr;
		String zeile3 = titel + " / " + verantwortlichkeit;
		return StringUtils.concatenate("\n", zeile1, zeile2, zeile3);
	}

}
