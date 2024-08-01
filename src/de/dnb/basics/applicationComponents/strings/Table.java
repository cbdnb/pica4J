package de.dnb.basics.applicationComponents.strings;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * Implementiert eine einfache Tabelle mit fester Spaltenzahl.
 * @author baumann
 *
 * @param <T>   Typ der Zellen
 */
public class Table<T> implements Iterable<List<T>> {

  private final List<List<T>> theRows;
  private final int columnCount;

  public int getRowCount() {
    return theRows.size();
  }

  @Override
  public final Iterator<List<T>> iterator() {
    return theRows.iterator();
  }

  public Table(final int columnCount) {
    theRows = new ArrayList<>();
    this.columnCount = columnCount;
  }

  /**
   *
   * @param row   Zeile von der im Konstruktor spezifizierten Länge
   *              columnCount.
   */
  public final void addRow(final List<T> row) {
    if (row == null || row.size() != columnCount)
      throw new IllegalArgumentException("Size != " + columnCount);
    theRows.add(row);
  }

  public final Table<T> transpose() {
    final Table<T> table = new Table<>(theRows.size());

    return table;
  }

  /**
   *
   * @param row   Zeile von der im Konstruktor spezifizierten Länge
   *              columnCount.
   */
  public void addRow(final T... row) {
    addRow(Arrays.asList(row));
  }

  public List<T> getRow(final int row) {
    if (row < 0 || row >= theRows.size())
      return null;
    else
      return theRows.get(row);
  }

  public T getCellAt(final int rowNumber, final int columnNumber) {
    final List<T> row = getRow(rowNumber);
    return ListUtils.getElement(row, columnNumber).orElse(null);
  }

  public void setCellAt(final T elem, final int rowNumber, final int columnNumber) {
    final List<T> row = getRow(rowNumber);
  }

  public void makeRowAt(final int rowNumber) {
    final List<T> row = getRow(rowNumber);
    if (row == null) {

    }
  }

  @Override
  public final String toString() {
    return StringUtils.concatenate("\n", theRows, row -> StringUtils.concatenate("\t", row));
  }

  /**
   * Liefert eine Tabelle von Strings (Standardfall).
   * @param columnCount   Spaltenzahl
   * @return              Stringtabelle
   */
  public static Table<String> getStringTable(final int columnCount) {
    return new Table<>(columnCount);
  }

  /**
   * Wandelt einen String in eine Tabelle um. Nimmt an, dass als
   * Zeilentrenner ein \n, als Trennzeichen für die Zellen
   * ein \t vorliegt (Standard-Excel).
   *
   * @param s nicht null, nicht leer. Darf zu Beginn keine Leerzeilen
   *          enthalten
   * @return  neue Tabelle; wirft Fehler, wenn die Zeilenlängen
   *          unterschiedlich sind.
   */
  public static Table<String> getStringTable(final String s) {
    RangeCheckUtils.assertStringParamNotNullOrWhitespace("", s);
    final String[] rows = s.split("\n|\r\n|\r|\u0085|\u2028|\u2029");
    String[] cells = rows[0].split("\t", -1);
    final int colCount = cells.length;
    final Table<String> table = new Table<>(colCount);
    for (final String row : rows) {
      cells = row.split("\t", -1);
      table.addRow(cells);
    }
    return table;
  }

  /**
   *
   * @param searchTerm    beleibig
   * @param column        beliebig
   * @return              die Zeile, die in der angegebenen Spalte
   *                      den Suchbegriff enthält, sonst -1
   */
  public final int findRow(final T searchTerm, final int column) {
    final int rowCount = theRows.size();
    for (int i = 0; i < rowCount; i++) {
      final T cellAt = getCellAt(i, column);
      if (StringUtils.equals(searchTerm, cellAt)) {
        return i;
      }
    }
    return -1;
  }

  /**
   *
   * @param filename  Dort ist die Tabelle gespeichert
   * @return          Tabelle
   * @throws FileNotFoundException    wenn Datei nicht existiert
   */
  public static Table<String> getStringTableFromFile(final String filename)
    throws FileNotFoundException {
    final String s = StringUtils.readIntoString(filename);
    return getStringTable(s);
  }

  public static void main(final String[] args) throws FileNotFoundException {
    final String s = StringUtils.readClipboard();
    final Table<String> table = getStringTable(s);

    System.out.println(table.toString());

  }
}
