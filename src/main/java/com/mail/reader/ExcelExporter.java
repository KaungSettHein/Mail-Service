package com.mail.reader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExcelExporter {

    public static void exportToExcel(List<EmailMessage> emails, String filePath) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Emails");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Email");
        header.createCell(2).setCellValue("Date");
        header.createCell(3).setCellValue("Subject");
        header.createCell(4).setCellValue("Body");
        header.createCell(5).setCellValue("Attachments");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < emails.size(); i++) {
            EmailMessage email = emails.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(email.getPersonalName());
            row.createCell(1).setCellValue(email.getFrom());
            row.createCell(2).setCellValue(sdf.format(email.getSentDate()));
            row.createCell(3).setCellValue(email.getSubject());
            row.createCell(4).setCellValue(email.getBody());
            row.createCell(5).setCellValue(String.join(", ", email.getAttachments()));
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        workbook.close();
    }
}