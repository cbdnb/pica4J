package de.dnb.basics;

import static de.dnb.basics.applicationComponents.strings.StringUtils.getPicaPlusContent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.dnb.basics.applicationComponents.StreamUtils;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.utils.PortalUtils;
import de.dnb.gnd.utils.RecordUtils;

public class Misc {

	/**
	 * Zeigt einen Text in einem Fenster.
	 *
	 * @param text beliebig
	 */
	public static void show(final String text) {

		final JEditorPane pane = new JEditorPane("text/plain", text);
		pane.setEditable(false);

		final JScrollPane scrollpane = new JScrollPane(pane);
		final JFrame jFrame = new JFrame();
		jFrame.getContentPane().add(scrollpane);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLocation(300, 100);
		jFrame.setSize(300, 500);

		final JMenuBar bar = new JMenuBar();
		jFrame.add(bar, BorderLayout.NORTH);
		final JMenu datei = new JMenu("Datei");
		bar.add(datei);
		final JMenuItem drucken = new JMenuItem("Drucken...");
		datei.add(drucken);
		drucken.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					pane.print();
				} catch (final PrinterException e1) {
					// keine Behandlung
				}
			}
		});
		jFrame.setVisible(true);

	}

	public static boolean isServerError(final int httpCode) {
		return httpCode >= 500 && httpCode < 600;
	}

	public static boolean isClientError(final int httpCode) {
		return httpCode >= 400 && httpCode < 500;
	}

	public static boolean isRedirection(final int httpCode) {
		return httpCode >= 300 && httpCode < 400;
	}

	public static boolean isRedirection(final URLConnection conn) {
		if (!(conn instanceof HttpURLConnection)) {
			return false;
		}
		final HttpURLConnection httpURLConnection = (HttpURLConnection) conn;
		try {
			return isRedirection(httpURLConnection.getResponseCode());
		} catch (final IOException e) {
			return false;
		}

	}

	//

	public static boolean isHttpError(final int httpCode) {
		return httpCode >= 400;
	}

	/**
	 * Fabriziert aus der Idn eine Portal-URI.
	 *
	 * @param idn nicht null.
	 * @return null, wenn keine Verbindung zum Portal aufgebaut werden konnte.
	 */
	public static String createURI(final String idn) {
		RangeCheckUtils.assertReferenceParamNotNull("idn", idn);

		String uri = "http://d-nb.info/" + idn;
		URL url = null;
		HttpURLConnection conn;
		try {
			url = new URL(uri);
			conn = (HttpURLConnection) url.openConnection();
			if (isHttpError(conn.getResponseCode())) {
				uri = "http://dispatch.opac.d-nb.de/" + "DB=1.1/SET=4/TTL=1/"
						+ "CMD?ACT=SRCHA&IKT=8509&SRT=LST_ty&TRM=idn+" + idn;
				url = new URL(uri);
				conn = (HttpURLConnection) url.openConnection();
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return null;
				}
			}
		} catch (final IOException e) {
			return null;
		}
		return uri;
	}

	/**
	 * dummerweise ist das kein valider xml-Code. Daher nach Pattern suchen. Nach
	 * dem Wort "Sachgruppe(n)" folgen einige Formatierungs-Tags und Umbrüche,
	 * danach die Ziffern. Sachgruppen K und S werden ausgeschlossen.
	 *
	 * DOTALL matcht auch Zeilenumbrüche.
	 */
	private static final String SACHGRUPPE_PAT = "Sachgruppe[^0-9KS]*?(\\d\\d\\d(\\.\\d+)?)\\D";

	private static final String SACHGRUPPE_K_PAT = "Sachgruppe\\D*?(K) Kinder- und Jugendliteratur";

	private static final String SACHGRUPPE_S_PAT = "Sachgruppe\\D*?(S) Schulbücher";

	private static Pattern sgPat = Pattern.compile(SACHGRUPPE_PAT, Pattern.DOTALL);

	private static Pattern sgKPat = Pattern.compile(SACHGRUPPE_K_PAT, Pattern.DOTALL);

	private static Pattern sgSPat = Pattern.compile(SACHGRUPPE_S_PAT, Pattern.DOTALL);

	/**
	 * Sucht sich aus der Portalanzeige die Sachgruppe zusammen.
	 *
	 * @param idn nicht null.
	 * @return null, wenn nichts gefunden.
	 */
	public static String getSachgruppe(final String idn) {
		final String title = PortalUtils.getRecordViaPortal(idn);
		if (title == null) {
			return null;
		}
		Matcher m = sgPat.matcher(title);
		if (m.find()) {
			return m.group(1);
		}
		m = sgKPat.matcher(title);
		if (m.find()) {
			return m.group(1);
		}
		m = sgSPat.matcher(title);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	/**
	 * Wertet die Umleitungen (redirections) aus, um zur korrekten URL-Verbindung zu
	 * kommen.
	 *
	 *
	 * @param url    beliebig
	 * @param repeat Zahl der Versuche
	 * @return korrekte Verbindung oder null
	 * @throws IOException wenn nix geht
	 */
	public static URLConnection openConnection(URL url, final int repeat) throws IOException {
		if (url == null) {
			return null;
		}
		if (repeat < 0) {
			return null;
		}
		final URLConnection connection = url.openConnection();
		if (!isRedirection(connection)) {
			return connection;
		}
		final String location = connection.getHeaderField("Location");
		if (location == null) {
			return null;
		}
		try {
			url = new URL(location);
		} catch (final MalformedURLException e) {
			return null;
		}
		return openConnection(url, repeat - 1);
	}

	/**
	 * Liefert den Inhalt der Website.
	 *
	 * @param uri nicht null, nicht leer
	 * @return null, wenn nichts gefunden wurde
	 */
	public static String getWebsite(final String uri) {
		RangeCheckUtils.assertStringParamNotNullOrEmpty("uri", uri);
		String website = "";
		InputStream urlStream = null;
		try {
			final URL url = new URL(uri);
			final URLConnection conn = openConnection(url, 4);

			urlStream = conn.getInputStream();

			final BufferedReader br1 = new BufferedReader(new InputStreamReader(urlStream, "UTF-8"));
			String read;
			while ((read = br1.readLine()) != null) {
				website += read + Constants.LINE_SEPARATOR;

			}
		} catch (final IOException e) {
			StreamUtils.safeClose(urlStream);
			return null;
		} finally {
			StreamUtils.safeClose(urlStream);
		}
		// return website;
		return website.isEmpty() ? null : website;
	}

	/**
	 *
	 * @return Den FQDN, das ist der „Fully Qualified Domain Name“ bzw. der
	 *         „vollqualifizierte Domänenname“.
	 */
	public static String getUserDomain() {
		return System.getenv("UserDnsDomain");
	}

	/**
	 * Gibt einen Infotext aus.
	 *
	 * @param component Aufrufendes Objekt (this)
	 * @param version   Versionsnummer
	 * @param file      Pfad zur Hilfedatei
	 */
	public static void showInfo(final Component component, final String version, final String file) {
		final SimpleDateFormat formatter = new SimpleDateFormat("d. M. yyyy 'um' H:mm 'Uhr'");
		final Date date = getCreationDate(component);
		// model.getCreationDate();
		final String dateStr = formatter.format(date);
		String info = "Version " + version + "\nErstellt in und für die Deutsche Nationalbibliothek"
				+ "\nAutor: Christian Baumann\n" + "Erstellungsdatum: " + dateStr;
		final String help = getTextFromFile(file, component);
		info += "\n" + help;
		final JEditorPane ar = new JEditorPane("text/html", info);
		ar.setEditable(false);
		// ar.setLineWrap(true);
		// ar.setWrapStyleWord(true);
		ar.setBackground(UIManager.getColor("Label.background"));
		final JScrollPane scrollpane = new JScrollPane(ar);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scrollpane.getVerticalScrollBar().getModel().setValue(0);
			}
		});
		final JOptionPane jOpPane = new JOptionPane(scrollpane, JOptionPane.PLAIN_MESSAGE);
		final JDialog jDialog = jOpPane.createDialog("   Info ");
		jDialog.setSize(700, 700);
		jDialog.setLocationRelativeTo(null);
		jDialog.setResizable(true);
		jDialog.setVisible(true);
	}

	/**
	 * Gibt den Inhalt einer Datei als String. Funktioniert auch bei zip- oder
	 * jar-Files. Die Datei darf keine Umlaute enthalten.
	 *
	 * @param file   Pfad zur Datei.
	 * @param object aufrufendes Objekt (this)
	 * @return Inhalt der Datei oder null.
	 */
	public static final String getTextFromFile(final String file, final Object object) {

		final BufferedReader in = new BufferedReader(
				new InputStreamReader(object.getClass().getResourceAsStream(file)));

		try {
			String help = "";
			String line = in.readLine();
			while (line != null) {
				help += "\n" + line;
				line = in.readLine();
			}
			in.close();
			return help;
		} catch (final Exception e) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler beim Helpfile", JOptionPane.OK_CANCEL_OPTION);
		}
		return null;
	}

	/**
	 * Gibt das eigene Erstellungsdatum einer jar-Datei aus.
	 *
	 * @param object Aufrufendes Objekt (this).
	 * @return Datum oder null.
	 */
	public static final Date getCreationDate(final Object object) {

		/*
		 * Die Manifest-Datei der eigenen jar-Datei ist immer aktuell. Daher Zugriff auf
		 * deren URL:
		 */
		final URL url = object.getClass().getResource("/META-INF/MANIFEST.MF");
		String fileStr = url.getFile();
		/*
		 * Die URL ist etwas komplizierter aufgebaut. Sie hat - ein Präfix "file:/" -
		 * ein Postfix, das mit "!" beginnt, welches die Dateien in der .jar
		 * kennzeichnet.
		 */
		final int pos1 = "file:/".length();
		final int pos2 = fileStr.indexOf("!");
		fileStr = fileStr.substring(pos1, pos2);
		/*
		 * fileStr enthält nun nur noch den Pfad der eigenen jar-Datei
		 */
		JarFile jarFile = null;
		Date creationDate = null;
		try {
			jarFile = new JarFile(fileStr);
			final ZipEntry zEnt = jarFile.getEntry("META-INF/MANIFEST.MF");
			creationDate = new Date(zEnt.getTime());
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler beim Datum", JOptionPane.OK_CANCEL_OPTION);
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (final IOException e) {
					// nix
				}
			}
		}
		return creationDate;
	}

	/**
	 * Macht aus Rohtiteldaten einen Kurztitel. Die Rohdaten müssen im Pica+-Format
	 * vorliegen.
	 *
	 * @param rawData nicht null.
	 * @return nicht null?
	 */
	public static String createShortTitle(final String rawData) {
		RangeCheckUtils.assertReferenceParamNotNull("rawData", rawData);
		String title;
		String creator;
		// 021A = 4000
		title = getPicaPlusContent("021A", 'a', rawData);
		// wohl Af:
		if (title == null) {
			// 4004 = 021B
			final String unselbstaendig = getPicaPlusContent("021B", 'a', rawData);
			title = getPicaPlusContent("021A", '8', rawData);
			if (title != null) {
				if (unselbstaendig != null) {
					title = title + "; " + unselbstaendig;
				}
			} else {
				// titel immer noch null, daher letzte Rettung
				title = unselbstaendig;
			}

			// 3010 = 028C (Person)
			creator = getPicaPlusContent("028C", '8', rawData);
			if (creator == null) {
				// 3120 = 029F (Körperschaft)
				creator = getPicaPlusContent("029F", '8', rawData);
			}
			// kein Af:
		}

		// 3000 = 028A
		creator = getPicaPlusContent("028A", '8', rawData);
		if (creator == null) {
			// 3100 = 029A
			creator = getPicaPlusContent("029A", '8', rawData);
		}
		if (creator != null) {
			if (creator.contains("$l")) {
				creator = creator.replace("$l", " <") + ">";
			}
			if (creator.contains("$g")) {
				creator = creator.replace("$g", " <") + ">";
			}
			if (creator.contains("$b")) {
				// $gKiel$bBibliothek -> <Kiel, Bibliothek>:
				if (creator.contains("<")) {
					creator = creator.replace("$b", ", ");
				} else {
					creator = creator.replace("$b", " <") + ">";
				}
			}
			creator = creator.replace("$c", " ");
			title = creator + " : " + title;
		}
		title = title.replace("@", "");

		return title;

	}

	public static String createExcelHyperlink(final String uri) {
		return "=HYPERLINK(\"" + uri + "\" )";
	}

	public static String createExcelHyperlink(final String uri, final String pretty) {
		return "=HYPERLINK(\"" + uri + "\";\"" + pretty + "\" )";
	}

	public static String createExcelLine(final String... strings) {
		String s = "";
		for (int i = 0; i < strings.length; i++) {
			if (i > 0) {
				s += "\t";
			}
			final String string = strings[i];
			s += string;
		}
		return s;
	}

	/**
	 * Macht aus allen Pica-Links, z.B. !040665623!, Hyperlinks, z.B. <a
	 * href=http://040665623>040665623</a>
	 *
	 * Die URLs können aus einem {@link JEditorPane} mit einem
	 * {@link HyperlinkListener} durch {@link HyperlinkEvent#getURL()} ausgelesen
	 * werden. Die idn kann wiederum aus der URL durch {@link URL#getAuthority()}
	 * gewonnen werden.
	 *
	 * @param s nicht null
	 * @return text mit Hyperlinks
	 */
	public static String convertLinksToHyperlinks(final String s) {
		RangeCheckUtils.assertReferenceParamNotNull("s", s);
		final String text = RecordUtils.PAT_LINK.matcher(s).replaceAll(" <a href=http://$1>!$1!</a>");
		return text;
	}

	/**
	 * Extrahiert den Authority-Teil der URL und ersetzt %20 durch Blank.
	 *
	 * @param e nicht null
	 * @return Authority
	 */
	public static String getAuthority(final HyperlinkEvent e) {
		final URL url = e.getURL();
		if (url == null) {
			return "";
		}
		final String name = url.getAuthority();
		return name.replace("%20", " ");
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws PrinterException
	 */
	public static void main(final String[] args) throws IOException, PrinterException {
		final String s = StringUtils.readClipboard();
		System.out.println(s);
		System.out.println(isUrlAccessible(null));
	}

	/**
	 * @param date auch null
	 * @return Datum in der Form dd-MM-yy (12-02-15), wie in der WinIBW angezeigt
	 *         oder null
	 */
	public static String asIBWDate(final Date date) {
		if (date == null) {
			return null;
		}
		final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yy");
		return format.format(date);
	}

	/**
	 * @param date auch null
	 * @return Datum in der Form dd-MM-yy kk:mm:ss.SSS (19:33:41.000), wie in der
	 *         WinIBW angezeigt oder null
	 */
	public static String asIBWTime(final Date date) {
		if (date == null) {
			return null;
		}
		final SimpleDateFormat format = new SimpleDateFormat("kk:mm:ss.SSS");
		return format.format(date);
	}

	/**
	 *
	 * @param urlString auch null
	 * @return ob die URL eine echte HTTP-Ressource darstellt (200 <= Antwort < 300)
	 */
	public static boolean isUrlAccessible(final String urlString) {
		try {

			final URI uri = new URI(urlString);
			final URL url = uri.toURL();
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			final int responseCode = connection.getResponseCode();
			return responseCode >= 200 && responseCode < 300;
		} catch (final Exception e) {
			return false;
		}
	}

}
