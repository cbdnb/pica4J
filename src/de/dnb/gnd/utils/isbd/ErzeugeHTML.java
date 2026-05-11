package de.dnb.gnd.utils.isbd;

import java.awt.FileDialog;
import java.io.IOException;
import java.io.PrintWriter;

import de.dnb.basics.applicationComponents.MyFileUtils;
import de.dnb.basics.utils.OutputUtils;

public class ErzeugeHTML {

	static boolean debug = true;

	public static void main(final String[] args) throws IOException {
		FileDialog dialog = new FileDialog((java.awt.Frame) null, "Select NSW-Records", FileDialog.LOAD);
		dialog.setDirectory("D:\\Analysen\\karg\\NSW\\");
		dialog.setFile("*.txt");
		dialog.setVisible(true);
		String nswFile = dialog.getFile();
		if (nswFile == null) {
			System.out.println("No file selected. Exiting.");
			System.exit(0);
		}
		nswFile = dialog.getDirectory() + nswFile;

		dialog = new FileDialog((java.awt.Frame) null, "Select Über-Records", FileDialog.LOAD);
		dialog.setDirectory("D:\\Analysen\\karg\\NSW\\");
		dialog.setFile("*.txt");
		dialog.setVisible(true);
		String ueberFile = dialog.getFile();
		if (ueberFile != null) {
			ueberFile = dialog.getDirectory() + ueberFile;
		}

		final WV wv = WV.createWV(nswFile, ueberFile);
		final HTMLformatter formatter = new HTMLformatter();
		final String html = formatter.format(wv);
		final PrintWriter out = MyFileUtils.outputFile("D:/Analysen/karg/NSW/NSW-test.html", false);
		OutputUtils.show(html);
		out.println(html);
		System.out.println(html);

	}

}
