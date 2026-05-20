package com.cts.mfrp.zuply.Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * ExcelUtils — reads test data from Excel (.xlsx) files using Apache POI.
 * Used by @DataProvider to drive data-driven tests.
 *
 * Excel format:
 *   Row 0 = Headers (skipped)
 *   Row 1+ = Test data
 *
 * Usage:
 *   @DataProvider(name = "loginData")
 *   public Object[][] getData() throws Exception {
 *       return ExcelUtils.getTestData(
 *           "src/test/resources/testdata/LoginData.xlsx", "LoginSheet");
 *   }
 */
public class ExcelUtils {

    /**
     * Read all rows (except header) from the given sheet.
     * @param filePath  absolute or relative path to .xlsx file
     * @param sheetName name of the sheet to read
     * @return 2D Object array for TestNG @DataProvider
     */
    public static Object[][] getTestData(String filePath, String sheetName) throws IOException {
        FileInputStream fis   = new FileInputStream(filePath);
        Workbook        wb    = new XSSFWorkbook(fis);
        Sheet           sheet = wb.getSheet(sheetName);

        if (sheet == null) {
            throw new RuntimeException("Sheet not found: " + sheetName);
        }

        int totalRows = sheet.getLastRowNum();        // excludes header row 0
        int totalCols = sheet.getRow(0).getLastCellNum();

        Object[][] data = new Object[totalRows][totalCols];

        for (int r = 1; r <= totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            for (int c = 0; c < totalCols; c++) {
                Cell cell = row.getCell(c);
                data[r - 1][c] = getCellValueAsString(cell);
            }
        }

        wb.close();
        fis.close();
        return data;
    }

    /** Convert any cell type to String */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getLocalDateTimeCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default:      return "";
        }
    }

    /**
     * Get a single cell value.
     * @param filePath  path to xlsx
     * @param sheetName sheet name
     * @param row       0-based row index
     * @param col       0-based column index
     */
    public static String getCellData(String filePath, String sheetName, int row, int col)
            throws IOException {
        FileInputStream fis   = new FileInputStream(filePath);
        Workbook        wb    = new XSSFWorkbook(fis);
        Sheet           sheet = wb.getSheet(sheetName);
        String          value = getCellValueAsString(sheet.getRow(row).getCell(col));
        wb.close(); fis.close();
        return value;
    }
}