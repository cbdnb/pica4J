package de.dnb.gnd.utils.isbd;

import java.net.URL;

import de.dnb.basics.Misc;
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
		if (checkUri(uri)) {
			Link link = new Link();
			link.adresse = uri;
			link.text = uri;
			isbd.zumKatalog = link;
		}
		isbd.neNr = RecordUtils.getContentOfSubfield(record, "2100", '0');
		
		isbd.titel = Util.getTitel(record);
		isbd.verantwortlichkeit = RecordUtils.getContentOfSubfield(record, "4000", 'h');

		return isbd;
	}

	static public boolean checkUri(String uri) {
		return Misc.getWebsite(uri) != null;
	}

	public static void main(String[] args) {
		Record record = RecordUtils.readFromClip();
		Builder builder = new Builder();
		ISBD isbd = builder.build(record);
		System.out.println(isbd);

	}

}
