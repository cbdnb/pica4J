package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import de.dnb.gnd.utils.SG;

/**
 * Die Liste der zu einer SG ({@link #dhs} gehörigen WV-Einträge. Diese bekommt
 * man mit {@link #getEintraege()}. Listen sind vergleichbar über ihre
 * Sachgruppe. Innerhalb der Liste sind die Einträge alphabetisch sortiert.
 */
public class Eintragsliste implements Comparable<Eintragsliste> {

	private final TreeSet<Eintrag> eintraegeZurSG = new TreeSet<>();

	public final SG dhs;

	/**
	 * @param dhs auch null
	 */
	public Eintragsliste(final SG dhs) {
		this.dhs = dhs;
	}

	public void add(final Eintrag eintrag) {
		eintraegeZurSG.add(eintrag);
	}

	public Collection<Eintrag> getEintraege() {
		return new ArrayList<>(eintraegeZurSG);
	}

	@Override
	public String toString() {
		String s = "";
		for (final Eintrag eintrag : eintraegeZurSG) {
			s += "\n" + eintrag;
		}
		final String ddcString = dhs == null ? null : dhs.getDDCString();
		return ddcString + s;
	}

	@Override
	public int compareTo(final Eintragsliste o) {
		return ISBD.COMPARATOR.compare(dhs, o.dhs);
	}

}
