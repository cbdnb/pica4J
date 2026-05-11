/**
 *
 *
 * Vorgehen:
 * <ol>
 * <li>Suche der NSW-Datensätze mit "f nsw listeNSW" in der WinIBW.
 * <li>Download der NSW-Datensätze im Pica+-Format mit "dow s1 p".
 * <li>Aufruf von {@link SammleIDNUebergeordnet#main(String[])}, um die idns der
 * übergeordneten Records zu sammeln. Diese befinden sich nun in der
 * Zwischenablage.
 * <li>Diese (wenigen) können in die WinIBW geladen werden, um in einem weiteren
 * Downloadfile gespeichert zu werden. Skript: "downloadIDNListe"
 * <li>{@link ErzeugeHTML#main(String[])} aufrufen, um die HTML-Datei zu
 * erzeugen.
 * <ol/>
 * <br>
 * Beispielcode aus {@link de.dnb.gnd.utils.isbd.HTMLformatter#main(String[])}:
 * <br>
 * <code> <br>
 * final WV wv = WV.createWV("D:/Analysen/karg/NSW/nsw.txt",
 * "D:/Analysen/karg/NSW/nswUeber.txt"); <br>
 * final HTMLformatter formatter = new HTMLformatter(); <br>
 * final String html = formatter.format(wv); <br>
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
package de.dnb.gnd.utils.isbd;