package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.parser.MarcParser;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.parser.RecordReader;
import de.dnb.gnd.utils.SG;

/**
 * Repräsentiert ein nach Sachgruppen sortiertes WV. Innerhalb der Sachgruppen
 * wird alphabetisch sortiert. Die einzelnen Sachgruppen bekommt man mit
 * {@link #getEintragsliste(SG)}, alle SGG mit {@link #getEintragslisten()}.
 */
public class WV {

	private final Map<String, ISBD> idn2isbdRaw = new HashMap<>();

	private final ISBDbuilder builder = new ISBDbuilder();

	private void loadRaw(final String file) throws IOException {
		RecordReader.getMatchingReader(file).forEach(rec -> {
			final ISBD isbd = builder.build(rec);
			idn2isbdRaw.put(rec.getId(), isbd);
		});
	}

	/**
	 *
	 */
	private WV() {
		super();
		// TODO Auto-generated constructor stub
	}

	private final Map<String, Eintrag> idn2Uebergeordnet = new HashMap<>();

	private void ladeUebergeordnete(final String file) {
		if (file == null) {
			return;
		}
		try {
			RecordReader.getMatchingReader(file).forEach(rec -> {
				final ISBD isbd = builder.build(rec);
				final Eintrag eintrag = new Eintrag(isbd);
				idn2Uebergeordnet.put(rec.getId(), eintrag);
			});
		} catch (final IOException e) {
			// nix
		}
	}

	private final MarcParser marcParser = new MarcParser();

	/**
	 * Wenn der übergeordnete Datensatz benötigt wird, aber noch nicht vorhenden
	 * ist, wird er vom Portal geholt und in {@link #idn2Uebergeordnet} eingefügt.
	 *
	 * @param isbd nicht null.
	 * @return DHS des Datensatzes oder die des übergeordneten Datensatzes. Die kann
	 *         auch null sein.
	 */
	private SG getDHS(final ISBD isbd) {
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
		final boolean mapContains = idn2Uebergeordnet.containsKey(idnUebergeordnet);
		Eintrag uebergeordnet = null;
		if (mapContains) {
			uebergeordnet = idn2Uebergeordnet.get(idnUebergeordnet);
		} else {
			// Denkbar, dass der übergeordnete mit den anderen zusammen geladen wurde:
			final ISBD raw = idn2isbdRaw.get(idnUebergeordnet);
			if (raw != null) {
				uebergeordnet = new Eintrag(raw);
			}
		}
		if (uebergeordnet == null) {
			// Unangenehmster Fall: Müssen aus Portal holen, da nicht in geladen.
			final org.marc4j.marc.Record marcRecord = PortalUtils.getMarcRecord(idnUebergeordnet);
			final Record record = marcParser.parse(marcRecord);
			if (record == null) {
				return null;
			}
			final ISBD raw = builder.build(record);
			uebergeordnet = new Eintrag(raw);
		}
		if (!mapContains) {
			idn2Uebergeordnet.put(idnUebergeordnet, uebergeordnet);
		}
		return uebergeordnet.getSG();
	}

	private final Map<SG, Eintragsliste> sg2eintragsliste = new HashMap<>();

	public void verarbeiteRaw() {
		idn2isbdRaw.forEach((idn, isbd) -> {
			final SG dhs = getDHS(isbd);
			// Ab jetzt sollte auch ein übergeordneter Datensatz vorhanden sein, da das
			// getDHS() zumindest versucht. Was wenn nicht erfolgreich?
			if (!isbd.isAbhaengig()) {
				final Eintrag eintrag = new Eintrag(isbd);
				final Eintragsliste eintragsliste = sg2eintragsliste.getOrDefault(dhs, new Eintragsliste(dhs));
				eintragsliste.add(eintrag);
				if (!sg2eintragsliste.containsKey(dhs)) {
					sg2eintragsliste.put(dhs, eintragsliste);
				}
				return;
			}

			// ab jetzt abhaengig:
			final String idnUebergeordnet = isbd.idnUebergeordnet;
			final ISBD isbdUebergeordnet = idn2Uebergeordnet.get(idnUebergeordnet).isbd;
			if (isbdUebergeordnet == null) {
				return; // Kannste machen nix!
			}
			// Übergeordnet existiert, ist aber schon eine Eintragsliste vorhanden zur SG?
			final Eintragsliste eintragslisteUeber = sg2eintragsliste.getOrDefault(dhs, new Eintragsliste(dhs));
			if (!sg2eintragsliste.containsKey(dhs)) {
				sg2eintragsliste.put(dhs, eintragslisteUeber);
			}
			// Ist den schon ein Eintrag in dieser Liste vorhanden?
			Eintrag eintragUebergeordnet = null;
			// Schlichte Suche:
			for (final Eintrag actualEintragUeber : eintragslisteUeber.getEintraege()) {
//				System.err.println(actualEintrag + " / " + isbd.getSortiermerkmal());
				if (actualEintragUeber.isbd.equals(isbdUebergeordnet)) {
					eintragUebergeordnet = actualEintragUeber;
					break;
				}
			}
			if (eintragUebergeordnet == null) {
				eintragUebergeordnet = new Eintrag(isbdUebergeordnet);
				eintragslisteUeber.add(eintragUebergeordnet);
			}

			eintragUebergeordnet.addUntergeordnet(isbd);
		});
	};

	public Eintragsliste getEintragsliste(final SG dhs) {
		return sg2eintragsliste.get(dhs);
	}

	public Collection<Eintragsliste> getEintragslisten() {
		return new TreeSet<>(sg2eintragsliste.values());
	}

	/**
	 *
	 * @param downloadFile hier liegt das WV, am besten im Pica+-Format. Es geht
	 *                     aber auch gzip.
	 * @param broaderFile  Hier liegen die übergeordneten Titel. Diese können über
	 *                     eine IDN-Liste ermittelt werden. ersatzweise werden sie
	 *                     von Portal geholt - was mit Darstellungseinschränkungen
	 *                     verbunden sein kann.
	 * @return Ein WV.
	 * @throws IOException downloadFile nicht existiert.
	 */
	public static WV createWV(final String downloadFile, final String broaderFile) throws IOException {
		final WV wv = new WV();
		wv.loadRaw(downloadFile);
		wv.ladeUebergeordnete(broaderFile);
		wv.verarbeiteRaw();
		return wv;
	}

	public static void main(final String[] args) throws IOException {
		final WV wv = createWV("D:/Analysen/karg/NSW/WVtest.txt", null);
		System.out.println(wv);
	}

	@Override
	public String toString() {
		String s = "";
		for (final Eintragsliste eintragsliste : getEintragslisten()) {
			s += "\n\n" + eintragsliste;
		}
		return "WV:" + s;
	}

}
