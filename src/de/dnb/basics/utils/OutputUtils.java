package de.dnb.basics.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import de.dnb.basics.applicationComponents.StreamUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

public class OutputUtils {

  public static void main(final String[] args) {
    // TODO Auto-generated method stub

  }

  /**
   * Druck Inhalt einer Text-Komponente und nutzt Seitenränder voll aus.
   * Kein Dialog.
   *
   * @param component nicht null
   */
  public static void printTextComponent(final JTextComponent component) {
    RangeCheckUtils.assertReferenceParamNotNull("component", component);
    try {
      // PrinterJob holen:
      final PrinterJob printerJob = PrinterJob.getPrinterJob();
      // Standard-PageFormat holen (bei uns a4 - beinhaltet
      // Größe in Pixeln und Ränder:
      final PageFormat pageFormat = printerJob.defaultPage();
      //aktuelles Paper holen:
      final Paper paper = pageFormat.getPaper();
      paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
      // Den Bedruckbaren Rand auf Seitengröße anpassen:
      pageFormat.setPaper(paper);
      printerJob.setPrintable(component.getPrintable(null, null), pageFormat);
      printerJob.print();
    } catch (final PrinterException e1) {
      // keine Behandlung
    }
  }

  /**
   * Druckt ohne störende Dialoge. Seite wird voll ausgenutzt.
   *
   * @param html				sollte als html formatiert sein.
   * @throws PrinterException Wenn irgendwas mit Drucker nicht klappt.
   *
   */
  public static void printWithoutDialog(final String html) throws PrinterException {
    /*
     * Aus
     * https://docs.oracle.com/javase/tutorial/uiswing/misc/printtext.html
     */
    final JEditorPane jTextComponent = new JEditorPane();
    jTextComponent.setContentType("text/html");
    jTextComponent.setText(html);
    printTextComponent(jTextComponent);
  }

  /**
   *
   * Druckt mit Druckdialog.
   *
   * @param html				sollte als html formatiert sein.
   * @throws PrinterException Wenn irgendwas mit Drucker nicht klappt.
   * @return					true, wenn ausgedruckt wurde, false, wenn
   * 							abgebrochen wurde.
   */
  public static boolean print(final String html) throws PrinterException {
    /*
     * Aus
     * https://docs.oracle.com/javase/tutorial/uiswing/misc/printtext.html
     */
    final JEditorPane jTextComponent = new JEditorPane();
    jTextComponent.setContentType("text/html");
    jTextComponent.setText(html);
    final boolean complete = jTextComponent.print();
    return complete;
  }

  /**
   *
   * @param fileName
   * @throws FileNotFoundException
   * @throws PrinterException			Wenn irgendwas mit Drucker nicht klappt.
   */
  public static void file2Printer(final String fileName)
    throws FileNotFoundException,
    PrinterException {
    final String s = StreamUtils.readIntoString(new FileInputStream(fileName));
    print(s);
  }

  /**
   * Zeigt einen Text in einem Ausgabefenster. Von dort kann gedruckt werden.
   * Standardmaße:
   * <br>x = 300
   * <br>y = 100
   * <br>breite = 900
   * <br>höhe = 900
   *
   * @param html  nicht null, am besten html-formatiert
   */
  public static void show(final String html) {
    RangeCheckUtils.assertReferenceParamNotNull("", html);
    show(html, 300, 100, 900, 900);
  }

  /**
   * Zeigt einen Text in einem Ausgabefenster. Von dort kann gedruckt werden.
   *
   * @param html  nicht null, am besten html-formatiert
   */
  public static
    void
    show(final String html, final int x, final int y, final int width, final int height) {
    RangeCheckUtils.assertReferenceParamNotNull("", html);
    final Pattern titlePattern = Pattern.compile("<title>(.*)</title>");
    final Matcher matcher = titlePattern.matcher(html);
    String match = "";
    if (matcher.find())
      match = matcher.group(1);
    final String title = match;
    final JEditorPane pane = new JEditorPane("text/html", html);
    pane.setEditable(false);

    final JScrollPane scrollpane = new JScrollPane(pane);
    final JFrame jFrame = new JFrame(title);
    jFrame.getContentPane().add(scrollpane);
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setLocation(x, y);
    jFrame.setSize(width, height);

    final JMenuBar bar = new JMenuBar();
    jFrame.add(bar, BorderLayout.NORTH);
    final JMenu datei = new JMenu("Datei");
    bar.add(datei);
    final JMenuItem drucken = new JMenuItem("Drucken...");
    datei.add(drucken);
    drucken.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        printTextComponent(pane);
      }
    });
    jFrame.setVisible(true);
  }

  /**
   * Zeigt einen Text in einem Ausgabefenster. Von dort kann gedruckt werden.
   *
   * @param html  nicht null, am besten html-formatiert
   */
  public static void show(final Component component, String title) {
    RangeCheckUtils.assertReferenceParamNotNull("component", component);
    if (title == null)
      title = "";
    final JScrollPane scrollpane = new JScrollPane(component);
    final JFrame jFrame = new JFrame(title);
    jFrame.getContentPane().add(scrollpane);
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setLocation(300, 100);
    jFrame.setSize(900, 900);

    jFrame.setVisible(true);
  }

  public static void append(final JTextComponent component, final String text) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final String s = component.getText();
        if (s.isEmpty())
          component.setText(text);
        else
          component.setText(s + "\n" + text);
      }
    });
  }
}
