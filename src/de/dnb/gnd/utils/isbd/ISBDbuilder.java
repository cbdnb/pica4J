package de.dnb.gnd.utils.isbd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SGUtils;
import de.dnb.gnd.utils.SubjectUtils;

public class ISBDbuilder {

	private ISBD isbd;

	public ISBD build(final Record record) {
		isbd = new ISBD();
		isbd.idn = record.getId();
		// Wenn's mehrere 4000er sind, wird es schwer, daher:
		isbd.idnUebergeordnet = Util.getIDNuebergeordnet(record);
		isbd.dhs = SGUtils.getDHS(record);
		isbd.dns = SGUtils.getDNS(record);
		isbd.lc = RecordUtils.getContentOfSubfield(record, "1700", 'a'); // 1. Ländercode

		final String uri = "http://d-nb.info/" + record.getId();
		// Wir nehmen an, dass der Katalogeintrag immer existiert, um
		// stundenlanges erfolgloses Wrten zu vermeiden:
		isbd.zumKatalog = new Link(uri, uri);

		isbd.neNr = RecordUtils.getContentOfSubfield(record, "2100", '0');

		isbd.schoepfer = Util.getAutor(record);
		if (isbd.schoepfer == null) {
			isbd.schoepfer = Util.getKoerperschaftVeranstaltung(record);
		}

		isbd.est = Util.getEST(record);
		isbd.titel = BibRecUtils.getMainTitle(record);
		isbd.titelzusatz = Util.getTitelzusatz(record);
		isbd.verantwortlichkeit = RecordUtils.getContentOfSubfield(record, "4000", 'h');
		isbd.zaehlung = RecordUtils.getContentOfSubfield(record, "4025", 'a');
		isbd.abhaengigerTitel = Util.abhaengigerTitel(record);

		isbd.ausgabebezeichnung = Util.ausgabebezeichung(record);
		isbd.veroeffentlichungsangaben = RecordUtils.getLines(record, "4030").stream()
				.map(Util::veroeffentlichungsAngabe).collect(Collectors.joining(" ; "));
		isbd.datum = Util.datum(record);
		isbd.weitereVeroeffAng = RecordUtils.getLines(record, "4034", "4035").stream()
				.map(Util::veroeffentlichungsAngabe).collect(Collectors.joining(" ; "));
		isbd.fruehereHaupttitel = RecordUtils.getLines(record, "4212", "4213", "4215").stream()
				.map(Util::fruehererHaupttitel).collect(Collectors.joining(" . - "));
		isbd.repro = RecordUtils.getContentOfSubfield(record, "4216", 'a');
		isbd.issn = Util.issn(record);

		isbd.links = new ArrayList<>();
		RecordUtils.getLines(record, "4715").forEach(line -> {
			final Link link = Util.link(line, record.getId());
			if (link != null) {
				isbd.links.add(link);
			}
		});

		isbd.umfang = Util.umfang(record);
		isbd.gesamttitel = Util.gesamttitel(record);
		isbd.anmerkung = Util.anmerkung(record);
		isbd.anmerkungFortlaufend = Util.anmerkungFortlaufend(record);

		isbd.hsVermerk = Util.hsVermerk(record);
		isbd.isbnEAN = Util.isbn(record);

		isbd.rswk = Util.rswk(record);
		isbd.formSW = Util.formSW(record);
		isbd.zielgruppe = Util.zielgruppe(record);

		final List<String> completeDDCNotations = SubjectUtils.getCompleteDDCNotations(record);
		isbd.ddc = completeDDCNotations.isEmpty() ? null : StringUtils.concatenate(" ◊ ", completeDDCNotations);
		isbd.listeNSW = Util.listeNSW(record);

		return isbd;
	}

	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		final ISBDbuilder builder = new ISBDbuilder();
		final ISBD isbd1 = builder.build(record);
		final ISBD isbd2 = builder.build(record);
		System.out.println(isbd1.equals(isbd2));

	}

}
