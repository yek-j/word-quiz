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

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public static Map<String, String> uploadWordExcel(MultipartFile file) throws IOException {
        // 파일 비어있는지 체크
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. (최대 5MB)");
        }

        // 파일 확장자 체크
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 파일만 업로드 가능합니다.");
        }

        Map<String, String> word = new HashMap<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            // 엑셀 0번째 시트
            XSSFSheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String term = formatter.formatCellValue(row.getCell(0)).trim();
                String description = formatter.formatCellValue(row.getCell(1)).trim();

                // 빈 행 스킵
                if (term.isEmpty() || description.isEmpty()) {
                    continue;
                }

                word.put(term, description);
            }
        }

        return word;
    }
}
