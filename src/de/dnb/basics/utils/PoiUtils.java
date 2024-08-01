
package de.dnb.basics.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;

/**
 * Apache poi hat einige Macken,
 * daher Utilities zum Bearbeiten von
 * Dokumenten.
 *
 * @author baumann
 *
 */
public final class PoiUtils {

    /**
     * 
     */
    private PoiUtils() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    /**
     * @param targetSheet   nicht null
     * @param row           nicht null
     */
    public static void append(final Sheet targetSheet, final Row row) {
        final int newRowNum = targetSheet.getLastRowNum() + 1;
        final Row newRow = targetSheet.createRow(newRowNum);
        copy(newRow, row);
    }

    /**
     *
     * @param targetRow neue Zeile, in die die alte
     *                  kopiert wird
     * @param sourceRow Quellzeile
     */
    @SuppressWarnings("deprecation")
    public static void copy(final Row targetRow, final Row sourceRow) {
        final Workbook workbook = sourceRow.getSheet().getWorkbook();
        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            final Cell oldCell = sourceRow.getCell(i);
            Cell newCell = targetRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }

            // Copy style from old cell and apply to new cell
            final CellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());

            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());

            // Set the cell data value
            switch (oldCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                newCell.setCellValue(oldCell.getRichStringCellValue());
                break;
            }
        }
    }

    /**
     * Get a specific cell from a sheet. If the cell doesn't exist,
     * then create it.
     * 
     * @param sheet			not null
     * @param rowIndex		The 0 based row number	
     * @param columnIndex	The 0 based column number
     * @return				The cell indicated by row and column
     */
    public static Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
    	return CellUtil.getCell(CellUtil.getRow(rowIndex, sheet), columnIndex);
    }

}
