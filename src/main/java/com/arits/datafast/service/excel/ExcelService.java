package com.arits.datafast.service.excel;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

    /**
     * Reads the first sheet. Row 0 = headers, everything after = data.
     */
    public ParsedExcel parse(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalStateException("Excel file has no header row.");
            }

            int lastCol = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < lastCol; c++) {
                Cell cell = headerRow.getCell(c);
                String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                headers.add(value.isEmpty() ? "Column " + (c + 1) : value);
            }

            List<List<String>> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row, formatter)) continue;

                List<String> rowValues = new ArrayList<>();
                for (int c = 0; c < lastCol; c++) {
                    Cell cell = row.getCell(c);
                    rowValues.add(cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                rows.add(rowValues);
            }

            log.info("[Excel] Parsed '{}': sheet='{}', headers={}, dataRows={}",
                    file.getName(), sheet.getSheetName(), headers.size(), rows.size());

            return new ParsedExcel(sheet.getSheetName(), headers, rows);
        }
    }

    private boolean isRowBlank(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) return false;
        }
        return true;
    }

    public record ParsedExcel(String sheetName, List<String> headers, List<List<String>> rows) {
        public int rowCount() {
            return rows.size();
        }
    }
}