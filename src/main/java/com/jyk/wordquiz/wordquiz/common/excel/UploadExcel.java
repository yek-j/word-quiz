package com.jyk.wordquiz.wordquiz.common.excel;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadExcel {
    public static Map<String, String> uploadWordExcel(MultipartFile file) throws IOException {
        Map<String, String> word = new HashMap<>();

        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());

        // 엑셀 0번째 시트
        XSSFSheet sheet = workbook.getSheetAt(0);

        for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = sheet.getRow(i);
            DataFormatter formatter = new DataFormatter();

            String term = formatter.formatCellValue(row.getCell(0));
            String description =  formatter.formatCellValue(row.getCell(1));

            word.put(term, description);
        }

        return word;
    }
}
