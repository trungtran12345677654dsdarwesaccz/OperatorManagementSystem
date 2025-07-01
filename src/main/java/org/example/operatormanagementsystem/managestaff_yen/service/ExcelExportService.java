package org.example.operatormanagementsystem.managestaff_yen.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.OperatorStaffResponse;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffOverviewResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public byte[] exportStaffToExcel(List<OperatorStaffResponse> staffList,
                                     StaffOverviewResponse overview,
                                     Integer managerId,
                                     boolean includeStatistics) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet chính cho danh sách nhân viên
            Sheet staffSheet = workbook.createSheet("Danh sách nhân viên");

            // Tạo styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            int rowNum = 0;

            // Tiêu đề báo cáo
            Row titleRow = staffSheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DANH SÁCH NHÂN VIÊN");
            titleCell.setCellStyle(createTitleStyle(workbook));
            staffSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

            // Thông tin xuất báo cáo
            Row infoRow = staffSheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Ngày xuất: " + LocalDateTime.now().format(DATE_FORMATTER));
            infoRow.createCell(5).setCellValue("Manager ID: " + managerId);

            // Dòng trống
            staffSheet.createRow(rowNum++);

            // Headers
            Row headerRow = staffSheet.createRow(rowNum++);
            String[] headers = {
                    "STT", "Mã NV", "Họ tên", "Tên đăng nhập", "Email",
                    "Số điện thoại", "Địa chỉ", "Giới tính", "Trạng thái",
                    "Ngày tạo", "Tổng booking", "Tổng feedback", "Tổng chatbot"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int stt = 1;
            for (OperatorStaffResponse staff : staffList) {
                Row dataRow = staffSheet.createRow(rowNum++);

                dataRow.createCell(0).setCellValue(stt++);
                dataRow.createCell(1).setCellValue("OP" + String.format("%04d", staff.getOperatorId()));
                dataRow.createCell(2).setCellValue(staff.getFullName());
                dataRow.createCell(3).setCellValue(staff.getUsername());
                dataRow.createCell(4).setCellValue(staff.getEmail());
                dataRow.createCell(5).setCellValue(staff.getPhone() != null ? staff.getPhone() : "");
                dataRow.createCell(6).setCellValue(staff.getAddress() != null ? staff.getAddress() : "");
                dataRow.createCell(7).setCellValue(staff.getGender() != null ? staff.getGender().toString() : "");
                dataRow.createCell(8).setCellValue(staff.getStatus() != null ? staff.getStatus().toString() : "");

                Cell dateCell = dataRow.createCell(9);
                if (staff.getCreatedAt() != null) {
                    dateCell.setCellValue(staff.getCreatedAt().format(DATE_FORMATTER));
                }
                dateCell.setCellStyle(dateStyle);

                Cell bookingCell = dataRow.createCell(10);
                bookingCell.setCellValue(staff.getTotalBookings());
                bookingCell.setCellStyle(numberStyle);

                Cell feedbackCell = dataRow.createCell(11);
                feedbackCell.setCellValue(staff.getTotalFeedbacks());
                feedbackCell.setCellStyle(numberStyle);

                Cell chatbotCell = dataRow.createCell(12);
                chatbotCell.setCellValue(staff.getTotalChatbotLogs());
                chatbotCell.setCellStyle(numberStyle);

                // Apply data style to other cells
                for (int i = 0; i < 10; i++) {
                    if (i != 9) { // Skip date cell
                        dataRow.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }

            // Thêm sheet thống kê nếu cần
            if (includeStatistics && overview != null) {
                createStatisticsSheet(workbook, overview);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                staffSheet.autoSizeColumn(i);
                // Set minimum width
                if (staffSheet.getColumnWidth(i) < 2000) {
                    staffSheet.setColumnWidth(i, 2000);
                }
            }

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createStatisticsSheet(Workbook workbook, StaffOverviewResponse overview) {
        Sheet statsSheet = workbook.createSheet("Thống kê");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;

        // Title
        Row titleRow = statsSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("THỐNG KÊ NHÂN VIÊN");
        titleCell.setCellStyle(createTitleStyle(workbook));

        statsSheet.createRow(rowNum++); // Empty row

        // Statistics data
        String[][] statsData = {
                {"Tổng số nhân viên", String.valueOf(overview.getTotalStaffs())},
                {"Nhân viên đang hoạt động", String.valueOf(overview.getActiveStaffs())},
                {"Nhân viên không hoạt động", String.valueOf(overview.getInactiveStaffs())},
                {"Nhân viên bị khóa", String.valueOf(overview.getBlockedStaffs())}
        };

        for (String[] stat : statsData) {
            Row row = statsSheet.createRow(rowNum++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(stat[0]);
            labelCell.setCellStyle(headerStyle);

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(stat[1]);
            valueCell.setCellStyle(dataStyle);
        }

        // Auto-size columns
        statsSheet.autoSizeColumn(0);
        statsSheet.autoSizeColumn(1);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    public String generateFileName(Integer managerId) {
        return String.format("Staff_Export_Manager_%d_%s.xlsx",
                managerId,
                LocalDateTime.now().format(FILE_DATE_FORMATTER));
    }
}