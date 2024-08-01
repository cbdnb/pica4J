package de.dnb.basics.clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import de.dnb.basics.Constants;
import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.utils.OutputWindow;

/**
 * Simpler Client. Vernalsst den Server, Zwischenablage auszulesen, und legt die
 * Antwort des Servers in die Zwischenablage. Ist der Server nicht aktiv, wird
 * versucht, ihn zu starten.
 *
 * @author baumann
 *
 */
public class Client {

  final int port;
  private Socket socket;

  private String classpath = "";
  private final String server;
  private OutputWindow outputWindow;
  protected boolean showOutput = true;

  /**
   * Startet den Client, wenn nötig, auch den Server. Für den Start des
   * Servers wird das Kommando "javaw -cp &lt;classpath&gt; &lt;Klasse&gt;"
   * verwendet.
   * 
   * @param port
   *            beliebig > 0
   * @param classpath     wenn null, wird der eigene Klassenpfad
   *                      genommen. Wird ignoriert, wenn der Server in einem
   *                      jar-File vorliegt.
   * @param server        Name der auszuführenden Klasse oder Pfad des
   *                      jar-Files. Dieser Pfad kann auch relativ sein.
   *                      Für Testzwecke: In Eclipse wird dieser relative
   *                      Pfad vom Wurzelverzeichnis des betreffenden
   *                      Projektes aus gebildet.
   *                      
   */
  public Client(final int port, final String classpath, final String server) {
    super();
    this.port = port;
    this.classpath = classpath;
    this.server = server;

  }

  public final void work() {

    try {
      socket = new Socket("localhost", port);
    } catch (final IOException e1) {
      try {
        startServer();
        socket = new Socket("localhost", port);
      } catch (final IOException e) {
        return;
      }
    }

    if (showOutput)
      outputWindow = new OutputWindow("Client");
    try {
      final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String response = "";
      String line = in.readLine();
      while (line != null && !line.equals(Constants.RS)) {
        response += line + "\n";
        if (showOutput)
          outputWindow.add("vom server: " + line);
        line = in.readLine();
      }
      socket.close();
      StringUtils.writeToClipboard(response);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void startServer() throws IOException {
    final Runtime proc = Runtime.getRuntime();
    String exString = "javaw ";
    final String exeDir = FileUtils.getExecutionDirectory();
    if (server.endsWith(".jar")) {
      exString += "-jar ";
    } else {
      exString += " -classpath ";
      if (classpath != null)
        exString += classpath;

      exString += ";" + exeDir;
    }

    exString += " " + server;
    System.err.println(exString);
    proc.exec(exString);
    try {
      Thread.sleep(300);
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(final String[] args) throws IOException {

    new Client(8181, null, "de.dnb.basics.clientServer.Server").work();
    //        new Client(8181, null, "Server.jar");
  }

}
