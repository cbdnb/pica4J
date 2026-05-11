package de.dnb.gnd.utils.isbd;

import java.awt.FileDialog;
import java.io.IOException;
import java.util.TreeSet;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.gnd.parser.RecordReader;

/**
 * Sammelt zu einer Record-Liste die idns der übergeordneten Records.
 *
 * Vorgehen: Path der NSW-Records in Zwischenablage. Die idns der übergeordneten
 * Datensätze werden in die Zwischenablage kopiert. Diese wenigen können in die
 * WinIBW geladen werden, um in einem weiteren Downloadfile gespeichert zu
 * werden.
 *
 * Danach kann {@link WV#createWV(String, String)} aufgerufen werden. Das
 * erzeugte WV kann man mittels {@link HTMLformatter#format(WV)} in eine
 * HTML-Datei umwandeln.
 *
 * Beispielcode:
 *
 * <code> <br>
 * final WV wv = WV.createWV("D:/Analysen/karg/NSW/nsw.txt",
 * "D:/Analysen/karg/NSW/nswUeber.txt"); <br>
 * final HTMLformatter formatter = new HTMLformatter(); <br>
 * final String html = formatter.format(wv);
 *
 * final PrintWriter out =
 * MyFileUtils.outputFile("D:/Analysen/karg/NSW/NSW-test.html", false); <br>
 *
 * OutputUtils.show(html); <br>
 * out.println(html); <br>
 * System.out.println(html);<code/>
 *
 *
 *
 * @author baumann
 *
 */
public class SammleIDNUebergeordnet {

	/**
	 * Schritt 1
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final FileDialog dialog = new FileDialog((java.awt.Frame) null,
				"Wähle NSW-Records, um übergeordnete zu extrahieren", FileDialog.LOAD);
		dialog.setDirectory("D:\\Analysen\\karg\\NSW\\");
		dialog.setFile("*.txt");
		dialog.setVisible(true);
		String nswFile = dialog.getFile();
		if (nswFile == null) {
			System.out.println("No file selected. Exiting.");
			System.exit(0);
		}
		nswFile = dialog.getDirectory() + nswFile;

		final RecordReader reader = RecordReader.getMatchingReader(nswFile);
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
		System.exit(0);
	}

}
