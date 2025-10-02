package de.dnb.gnd.utils.isbd;

import java.io.IOException;
import java.util.TreeSet;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.parser.RecordReader;

/**
 * Sammelt zu einer Record-Liste die idns der übergeordneten Records.
 *
 * Vorgehen: Path der Records in Zwischenablage. Die idns der übergeordneten
 * Datensätze werden in die Zwischenablage kopiert.
 *
 * @author baumann
 *
 */
public class SammleIDNUebergeordnet {

	public static void main(final String[] args) throws IOException {
		final String fileName = StringUtils.readClipboard();
		final RecordReader reader = RecordReader.getMatchingReader(fileName);
		final TreeSet<String> idns = new TreeSet<>();
		reader.forEach(rec -> {
			final String idn = Util.getIDNuebergeordnet(rec);
			if (idn != null) {
				idns.add(idn);
			}
		});
		idns.forEach(idn -> System.out.println(idn));
		final String liste = StringUtils.concatenate("//", idns);
		StringUtils.writeToClipboard(liste);

	}

}
