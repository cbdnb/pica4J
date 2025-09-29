package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import de.dnb.gnd.utils.SG;

public class Eintrag implements Comparable<Eintrag> {

	@Override
	public String toString() {
		String unter = "";
		for (final ISBD isbd : untergeordnete) {
			unter += "\n\t" + isbd.getSortiermerkmal();
		}
		return isbd.getSortiermerkmal() + unter;
	}

	public final ISBD isbd;

	/**
	 * @param isbd
	 */
	public Eintrag(final ISBD isbd) {
		this.isbd = isbd;
	}

	private final TreeSet<ISBD> untergeordnete = new TreeSet<>();

	public Collection<ISBD> getUntergeordnete() {
		return new ArrayList<>(untergeordnete);
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
		final Eintrag other = (Eintrag) obj;
		return Objects.equals(isbd.idn, other.isbd.idn);
	}

	@Override
	public int compareTo(final Eintrag o) {
		return isbd.compareTo(o.isbd);
	}

	public void addUntergeordnet(final ISBD isbd) {
		untergeordnete.add(isbd);
	}

	public SG getSG() {
		return isbd.dhs;
	}
}