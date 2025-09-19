package de.dnb.gnd.utils.isbd;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dnb.basics.utils.DDC_Utils;
import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.BibRecUtils;
import de.dnb.gnd.utils.DDC_SG;
import de.dnb.gnd.utils.RecordUtils;
import de.dnb.gnd.utils.SGUtils;

public class Builder {

	private ISBD isbd;

	public ISBD build(Record record) {
		isbd = new ISBD();
		isbd.dhs = SGUtils.getDHS(record);
		isbd.dns = SGUtils.getDNS(record);
		isbd.lc = RecordUtils.getContentOfSubfield(record, "1700", 'a'); // 1. Ländercode

		isbd.zumKatalog = null;
		String uri = "http://d-nb.info/" + record.getId();
		isbd.zumKatalog = Link.getLink(uri, uri);

		isbd.neNr = RecordUtils.getContentOfSubfield(record, "2100", '0');

		isbd.schoepfer = Util.getAutor(record);
		if (isbd.schoepfer == null)
			isbd.schoepfer = Util.getKoerperschaftVeranstaltung(record);

		isbd.est = Util.getEST(record);
		isbd.titel = Util.getTitel(record);
		isbd.verantwortlichkeit = RecordUtils.getContentOfSubfield(record, "4000", 'h');
		isbd.zaehlung = RecordUtils.getContentOfSubfield(record, "4025", 'a');

		isbd.ausgabebezeichnung = Util.ausgabebezeichung(record);
		isbd.veroeffentlichungsangaben = RecordUtils.getLines(record, "4030").stream()
				.map(Util::veroeffentlichungsAngabe).collect(Collectors.joining(" ; "));
		isbd.datum = Util.datum(record);
		isbd.weitereVeroeffAng = RecordUtils.getLines(record, "4034", "4035").stream()
				.map(Util::veroeffentlichungsAngabe).collect(Collectors.joining(" ; "));
		isbd.fruehereHaupttitel = RecordUtils.getLines(record, "4213", "4215").stream()
				.map(Util::fruehererHaupttitel).collect(Collectors.joining(" . - "));
		isbd.repro = RecordUtils.getContentOfSubfield(record, "4216", 'a');
		isbd.issn = Util.issn(record);

		isbd.links = new ArrayList<>();
		RecordUtils.getLines(record, "4715").forEach(line -> {
			Link link = Util.link(line, record.getId());
			isbd.links.add(link);
		});

		isbd.umfang = Util.umfang(record);
		isbd.gesamttitel = Util.gesamttitel(record);
		isbd.anmerkung = Util.anmerkung(record);
		isbd.anmerkungFortlaufend = Util.anmerkungFortlaufend(record);

		isbd.hsVermerk = Util.hsVermerk(record);
		isbd.isbnEAN = Util.isbn(record);
		
		isbd.listeNSW = Util.listeNSW(record);

		return isbd;
	}

	public static void main(String[] args) {
		Record record = RecordUtils.readFromClip();
		Builder builder = new Builder();
		ISBD isbd = builder.build(record);
		System.out.println(isbd);

	}

}
