package de.dnb.gnd.utils.isbd;

import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.RecordUtils;

public class Test {

	public static void main(final String[] args) {
		final Record record = RecordUtils.readFromClip();
		final ISBDbuilder builder = new ISBDbuilder();
		final ISBD isbd = builder.build(record);
		final HTMLformatter formatter = new HTMLformatter(isbd);
		OutputUtils.show(formatter.format());
		System.out.println(isbd);

	}

}
