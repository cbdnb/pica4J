package de.dnb.basics.clientServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.utils.OutputWindow;

/**
 * Server sammelt die vom Client gesendeten Zeilen, verarbeitet sie mit
 * {@link #transform()} und sendet sie an den Client zurück. 
 * transform() muss zu diesem Zweck überschrieben werden. Das Ende der
 * Transmission zeigt der Client durch das Zeichen 0x001e and.
 * 
 * 
 * @author baumann
 *
 */
public abstract class Server {

    private int port;

    private ServerSocket serverSocket;

    private OutputWindow outputWindow;

    private Socket socket;

    private void accept() {
        try {

            serverSocket = new ServerSocket(port);
            while (true) {
                socket = serverSocket.accept();
                String clip = StringUtils.readClipboard();
                String transformed = "";

                try {
                    transformed = transform(clip);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(transformed);
                out.flush();
                FileUtils.safeClose(socket);
            }
        } catch (IOException e) {
            FileUtils.safeClose(socket);
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        FileUtils.safeClose(socket);
        FileUtils.safeClose(serverSocket);
    };

    public abstract String transform(String s);

    /**
     * Instanziiert und startet den Server.
     * 
     * @param port	beliebig.
     */
    public Server(final int port) {
        super();
        this.port = port;
        outputWindow = new OutputWindow("Server");
        accept();
    }

    protected void addInfo(String s) {
        outputWindow.add(s);
    }

    public static void main(String[] args) throws IOException {

        Server server = new Server(8181) {
            @Override
            public String transform(String s) {
                addInfo(s);
                return "" + s.length();
            }
        };

    }

}
