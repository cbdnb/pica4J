package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.io.PrintWriter;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.utils.OutputUtils;
import de.dnb.gnd.parser.Record;
import de.dnb.gnd.utils.RecordUtils;

public class Test {

	public static void main(final String[] args) throws IOException {
		final Record record = RecordUtils.readFromClip();
		final ISBDbuilder builder = new ISBDbuilder();
		final ISBD isbd = builder.build(record);
		final HTMLformatter formatter = new HTMLformatter();
		final String formatted = HTMLformatter.PRE_DOCUMENT + formatter.format(isbd);
		OutputUtils.show(formatted);
		System.out.println(formatted);
		final PrintWriter out = MyFileUtils.outputFile("D:/Temp/test.html", false);
		out.println(formatted);
	}

}
