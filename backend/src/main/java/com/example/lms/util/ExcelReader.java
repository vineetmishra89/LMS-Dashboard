package com.example.lms.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.*;

public class ExcelReader {

    public static List<Map<String, String>> readExcel(InputStream inputStream) throws Exception {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new RuntimeException("Excel sheet is empty");
            }

            // Read header once
            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowData = new HashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.put(headers.get(i), getCellValueAsString(cell));
                }
                dataList.add(rowData);
            }
        }
        return dataList;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double d = cell.getNumericCellValue();
                    // remove .0 for integer-like values
                    if (d == Math.rint(d)) {
                        return String.valueOf((long) d);
                    } else {
                        return String.valueOf(d);
                    }
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case BLANK:   return "";
            default:      return cell.toString();
        }
    }
}
