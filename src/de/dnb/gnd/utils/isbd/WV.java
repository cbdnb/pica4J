package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.parser.MarcParser;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.utils.SG;

public class WV {

	List<ISBD> wvRaw = new ArrayList<>();

	final ISBDbuilder builder = new ISBDbuilder();

	public void loadRaw(final String file) throws IOException {
		RecordReader.getMatchingReader(file).forEach(rec -> {
			final ISBD isbd = builder.build(rec);
			wvRaw.add(isbd);
		});
	}

	Map<String, ISBD> idn2Uebergeordnet = new HashMap<>();

	public void loadIDN2uebergeordnet(final String file) throws IOException {
		RecordReader.getMatchingReader(file).forEach(rec -> {
			final ISBD isbd = builder.build(rec);
			idn2Uebergeordnet.put(rec.getId(), isbd);
		});
	}

	public static class Eintrag implements Comparable<Eintrag> {
		ISBD isbd;
		TreeSet<ISBD> untergeordnete = new TreeSet<>();

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
			return Objects.equals(isbd, other.isbd);
		}

		@Override
		public int compareTo(final Eintrag o) {
			return isbd.compareTo(o.isbd);
		}
	}

	MarcParser marcParser = new MarcParser();

	/**
	 * Wenn der übergeordnete Datensatz benötigt wird, aber noch nicht vorhenden
	 * ist, wird er vom Portal geholt und in {@link #idn2Uebergeordnet} eingefügt.
	 *
	 * @param isbd nicht null.
	 * @return DHS des Datensatzes oder die des übergeordneten Datensatzes. Die kann
	 *         auch null sein.
	 */
	public SG getDHS(final ISBD isbd) {
		// alter Code, in der Annahme, dass die eigene DHS sticht:
//		if (isbd.dhs != null) {
//			// Kann auch im Widerspruch zur DHS des übergeordneten Titels stehen:
//			return isbd.dhs;
//		}
		if (!isbd.isAbhaengig()) {
			return isbd.dhs;
		}
		// Ab jetzt: Ist abhängig.
		final String idnUebergeordnet = isbd.idnUebergeordnet;
		final ISBD uebergeordnet = idn2Uebergeordnet.get(idnUebergeordnet);
		if (uebergeordnet != null) {
			return uebergeordnet.dhs;
		}
		// Unangenehmster Fall: Müssen aus Portal holen, da nicht in idn2Uebergeordnet.
		final org.marc4j.marc.Record marcRecord = PortalUtils.getMarcRecord(idnUebergeordnet);
		final Record record = marcParser.parse(marcRecord);
		if (record == null) {
			return null;
		}
		final ISBD isbdUebergeornet = builder.build(record);
		idn2Uebergeordnet.put(idnUebergeordnet, isbdUebergeornet);
		return isbdUebergeornet.dhs;
	}

	Map<SG, TreeSet<Eintrag>> sg2eintraege = new HashMap<>();

	public void verarbeiteRaw() {
		wvRaw.forEach(isbd -> {
			final SG dhs = getDHS(isbd);
			// ab jetzt sollte auch ein übergeordneter Datensatz vorhanden sein. Was wenn
			// nicht?
			if (!isbd.isAbhaengig()) {
				final Eintrag eintrag = new Eintrag();
				eintrag.isbd = isbd;
				final TreeSet<Eintrag> eintraege = sg2eintraege.getOrDefault(dhs, new TreeSet<>());
				eintraege.add(eintrag);
				if (!sg2eintraege.containsKey(dhs)) {
					sg2eintraege.put(dhs, eintraege);// nötig?
				}
				return;
			}
			// ab jetzt abhaengig:
			final String idnUebergeordnet = isbd.idnUebergeordnet;
			final ISBD uebergeordnet = idn2Uebergeordnet.get(idnUebergeordnet);
			if (uebergeordnet == null) {
				return; // Kannste machen nix!
			}
			// Übergeordnet existiert, ist aber schon eine Eintragsliste vorhanden?
			final TreeSet<Eintrag> eintraege = sg2eintraege.getOrDefault(dhs, new TreeSet<>());
			if (!sg2eintraege.containsKey(dhs)) {
				sg2eintraege.put(dhs, eintraege);// nötig?
			}
			Eintrag eintragUebergeordnet = null;
			for (final Eintrag actualEintrag : eintraege) {
				if (actualEintrag.isbd.equals(isbd)) {
					eintragUebergeordnet = actualEintrag;
					break;
				}
			}
			if (eintragUebergeordnet == null) {
				eintragUebergeordnet = new Eintrag();
				eintragUebergeordnet.isbd = isbd;
				eintraege.add(eintragUebergeordnet);
			}
			eintragUebergeordnet.untergeordnete.add(isbd);
		});
	};

	public static void main(final String[] args) {
		// TODO Auto-generated method stub

	}

}
