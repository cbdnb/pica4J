/**
 *
 */
package de.dnb.gnd.utils;

import java.awt.Component;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Ein Tabellen-Renderer, dem man Zeilennummern hinzufügen kann. Diese Zeilen
 * werden fett und kursiv dargestellt. Verwendung etwa:
 * <br>
 * <br>
 * <code>
 * BoldRenderer cellRenderer0 = new BoldRenderer();
 * <br>...<br>
 * c = table.getColumnModel().getColumn(0);
 * <br>
 * c.setCellRenderer(cellRenderer0);
 * <br>...<br>
 * cellRenderer0.addRow(row);
 *
 * </code>
 *
 * <br><br>Zusätzlich wird noch ein Tooltip angezeigt.
 *
 * @author baumann
 *
 */
public class BoldRenderer extends DefaultTableCellRenderer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final Set<Integer> rows = new HashSet<>();

  @Override
  public Component getTableCellRendererComponent(
    final JTable tblData,
    final Object value,
    final boolean isSelected,
    final boolean hasFocus,
    final int row,
    final int column) {
    final Component cellComponent =
      super.getTableCellRendererComponent(tblData, value, isSelected, hasFocus, row, column);

    if (rows.contains(row)) {
      cellComponent.setFont(cellComponent.getFont().deriveFont(Font.BOLD + Font.ITALIC));
    }

    final JLabel c = (JLabel) cellComponent;
    final String text = c.getText();
    c.setToolTipText(text);

    return cellComponent;
  }

  public void addRow(final int r) {
    rows.add(r);
  }
}
