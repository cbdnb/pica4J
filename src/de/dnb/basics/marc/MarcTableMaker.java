/**
 *
 */
package de.dnb.basics.marc;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.utils.HTMLEntities;
import de.dnb.basics.utils.HTMLUtils;
import de.dnb.gnd.utils.BoldRenderer;

/**
 * Wandelt einen Marc-Record in eine Tabelle um.
 *
 * @author baumann
 *
 */
public class MarcTableMaker {

  private int datenWeite = 150;

  /**
   * @param datenWeite the datenWeite to set
   */
  public void setDatenWeite(final int datenWeite) {
    this.datenWeite = datenWeite;
  }

  private DefaultTableModel tableModel;
  private int rowHeight;
  private BoldRenderer cellRenderer0 = new BoldRenderer();
  private BoldRenderer cellRenderer1 = new BoldRenderer();

  /**
   *
   */
  private void makeTable(final JTable table) {
    tableModel = new DefaultTableModel();
    table.setModel(tableModel);
    rowHeight = table.getRowHeight();
    cellRenderer0 = new BoldRenderer();
    cellRenderer1 = new BoldRenderer();

    tableModel.addColumn("Feldbeschreibung");
    tableModel.addColumn("Feld");
    tableModel.addColumn("Unterfeldbeschreibung");
    tableModel.addColumn("Feldinhalt");

    final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setVerticalAlignment(SwingConstants.CENTER);
    table.setDefaultRenderer(String.class, renderer);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

  }

  /**
   *
   */
  private void adjustTable(final JTable table) {

    TableColumn c;
    c = table.getColumnModel().getColumn(0);
    c.setMinWidth(420);
    c.setMaxWidth(430);
    c.setCellRenderer(cellRenderer0);
    c = table.getColumnModel().getColumn(1);
    c.setMaxWidth(40);
    c.setCellRenderer(cellRenderer1);
    c = table.getColumnModel().getColumn(2);
    c.setCellRenderer(new BoldRenderer());
    c.setMinWidth(190);
    c.setMaxWidth(400);
    c = table.getColumnModel().getColumn(3);
    c.setMinWidth(750);
    c.setMaxWidth(820);
  }

  /**
   * Lädt den Datenaatz in die Tabelle.
   *
   * @param record  nicht null
   * @param table   nicht null
   */
  public void loadMarc(final Record record, final JTable table) {

    makeTable(table);

    adjustTable(table);

    MarcDB db;
    if (RecordType.getType(record) == RecordType.AUTHORITY)
      db = MARCAuthorityDB.getDB();
    else if (RecordType.getType(record) == RecordType.CLASSIFICATION)
      db = MARCClassificationDB.getDB();
    else
      db = MARCTitleDB.getDB();

    record.getControlFields().forEach(cf ->
    {

      final String tag = cf.getTag();
      final MarcTag mtag = db.getTag(tag);
      if (mtag == null)
        return;
      final String data = cf.getData();

      insertRow(mtag.german, mtag.marc, "", data);

    });

    final List<DataField> dfs = record.getDataFields();
    dfs.forEach(datafield ->
    {
      final String tag = datafield.getTag();
      final MarcTag marcTag = db.getTag(tag);
      if (marcTag == null) {
        System.err.println("Kein Tag: " + tag);
        return;
      }
      insertRow(marcTag.german, marcTag.marc);
      final int tagline = tableModel.getRowCount();
      cellRenderer0.addRow(tagline - 1);
      cellRenderer1.addRow(tagline - 1);

      final char ind1 = datafield.getIndicator1();
      final char ind2 = datafield.getIndicator2();
      if (Character.isLetterOrDigit(ind1)) {
        final String ind1S = marcTag.marcIndicator1;
        if (ind1S == null)
          System.err.println("unbekannter Indikator1: " + tag);
        else {
          insertRow("", "Ind.1", ind1S, Character.toString(ind1));
        }
      }
      if (Character.isLetterOrDigit(ind2)) {
        final String ind2S = marcTag.marcIndicator2;
        if (ind2S == null)
          System.err.println("unbekannter Indikator1: " + tag);
        else {
          insertRow("", "Ind.2", ind2S, Character.toString(ind2));
        }
      }

      datafield.getSubfields().forEach(sub ->
      {

        final Pair<MarcSubfieldIndicator, String> real = db.getRealData(tag, sub);
        // System.err.println(tag + "/" + sub);
        // System.err.println(real);
        final MarcSubfieldIndicator subind = real.first;
        if (subind == null) {
          System.err.println(StringUtils.concatenate("/", tag, sub));
          return;
        }
        String data = real.second;
        if (data == null) {
          System.err.println(StringUtils.concatenate("/", "Kein Unterfeld", tag, sub));
          return;
        }
        String descGerman = null;

        final String code = subind.getDACHCode();
        descGerman = subind.descGerman;

        final boolean zuLang = data.length() > datenWeite;

        data = Normalizer.normalize(data, Form.NFC);

        data = data.replaceAll("\u009c ", " @");
        data = data.replaceFirst("˜", "");
        data = data.replace('\u009c', '@');
        data = HTMLEntities.allCharacters(data);
        data = data.replaceFirst("&#152;", "");

        data = HTMLUtils.makeHtml(data);

        insertRow("", "$" + code, descGerman, data);
        if (zuLang) {
          final int line = table.getRowCount() - 1;
          table.setRowHeight(line, (rowHeight * data.length()) / datenWeite + 2 * rowHeight);
        }

      });

    });

  }

  private void insertRow(final String... strings) {
    tableModel.addRow(strings);

  }

}
