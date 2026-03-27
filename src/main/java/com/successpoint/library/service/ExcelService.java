package com.successpoint.library.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.successpoint.library.entity.Student;
@Service
public class ExcelService {

    public ByteArrayInputStream generateExcel(List<Student> students) throws IOException {
        String[] columns = {"ID", "Name", "Mobile No", "Email", "Original Joining", "Last Renewal", "Due Date", "Renewals", "Current Fees", "Total Fees", "Months"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students Data");

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
            }

            // Fill Data Rows
            int rowIdx = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getMobileNumber());
                row.createCell(3).setCellValue(student.getEmail());
                row.createCell(4).setCellValue(student.getOriginalJoiningDate() != null ? student.getOriginalJoiningDate().toString() : (student.getJoiningDate() != null ? student.getJoiningDate().toString() : ""));
                row.createCell(5).setCellValue(student.getLastRenewalDate() != null ? student.getLastRenewalDate().toString() : "N/A");
                row.createCell(6).setCellValue(student.getDueDate() != null ? student.getDueDate().toString() : "");
                row.createCell(7).setCellValue(student.getRenewalCount());
                row.createCell(8).setCellValue(student.getFeesPaid());
                row.createCell(9).setCellValue(student.getTotalFeesCollected());
                row.createCell(10).setCellValue(student.getFeesPeriodMonths());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}