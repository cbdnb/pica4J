package de.dnb.basics.utils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

/**
 * Einfaches Ausgabefenster, das die Ausgabe protokolliert, wenn gewünscht.
 * @author Christian_2
 *
 */
public class OutputWindow {

    private static int yInitial = 100;
    private static int xInitial = 300;
    private static final int X_DIF = 50;
    private static final int Y_DIF = 30;

    private final transient JEditorPane pane;

    private transient boolean show = true;

    public OutputWindow(final String title) {

        this.pane = new JEditorPane();
        this.pane.setEditable(false);
        this.pane.setFont(new Font(null, 0, 14));

        final JScrollPane scrollpane = new JScrollPane(this.pane);
        final JFrame jFrame = new JFrame();
        jFrame.getContentPane().add(scrollpane);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocation(xInitial, yInitial);
        xInitial += X_DIF;
        yInitial += Y_DIF;
        jFrame.setSize(300, 500);
        jFrame.setTitle(title);

        final JMenuBar bar = new JMenuBar();
        jFrame.add(bar, BorderLayout.NORTH);
        final JMenu datei = new JMenu("Datei");
        bar.add(datei);

        final JCheckBoxMenuItem accept =
            new JCheckBoxMenuItem("Eingabe protokollieren");
        accept.setState(this.show);
        datei.add(accept);
        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                OutputWindow.this.show = !OutputWindow.this.show;
            }
        });

        final JMenuItem clear = new JMenuItem("Fenster leeren");
        datei.add(clear);
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                clear();
            }
        });
        final JMenuItem drucken = new JMenuItem("Drucken...");
        datei.add(drucken);
        drucken.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    OutputWindow.this.pane.print();
                } catch (final PrinterException e1) {
                    // keine Behandlung
                }
            }
        });
        jFrame.setVisible(true);
    }

    /**
     * Fügt dem Fester den Text hinzu, sofern show == true.
     * @param text  beliebig
     */
    public final void add(final String text) {
        if (this.show) {
            OutputUtils.append(this.pane, text);
        }
    }

    /**
     * Entfernt den Fensterinhalt.
     */
    private void clear() {
        this.pane.setText("");
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(final String[] args) throws IOException {

        final OutputWindow outputWindow = new OutputWindow("Titel");
        outputWindow.add("hi");
        outputWindow.add("jA32");
        final OutputWindow outputWindow2 = new OutputWindow("2");

    }

}
