package org.example.operatormanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.operatormanagementsystem.entity.Revenue;
import org.example.operatormanagementsystem.repository.RevenueRepository;
import org.example.operatormanagementsystem.service.RevenueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final RevenueRepository revenueRepository;

    @Override
    public List<Revenue> getAllRevenues() {
        return revenueRepository.findAll();
    }

    @Override
    public Revenue getRevenueById(Integer id) {
        return revenueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Revenue not found with id: " + id));
    }

    @Override
    @Transactional
    public Revenue createRevenue(Revenue revenue) {
        return revenueRepository.save(revenue);
    }

    @Override
    @Transactional
    public Revenue updateRevenue(Integer id, Revenue revenue) {
        if (!revenueRepository.existsById(id)) {
            throw new RuntimeException("Revenue not found with id: " + id);
        }
        revenue.setRevenueId(id);
        return revenueRepository.save(revenue);
    }

    @Override
    @Transactional
    public void deleteRevenue(Integer id) {
        revenueRepository.deleteById(id);
    }

    @Override
    public List<Revenue> getRevenuesByDateRange(LocalDate startDate, LocalDate endDate) {
        return revenueRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public List<Revenue> getRevenuesByBeneficiary(Integer beneficiaryId) {
        return revenueRepository.findByBeneficiaryId(beneficiaryId);
    }

    @Override
    public List<Revenue> getRevenuesBySourceType(String sourceType) {
        return revenueRepository.findBySourceType(sourceType);
    }

    @Override
    public List<Revenue> getRevenuesByBooking(Integer bookingId) {
        return revenueRepository.findByBookingId(bookingId);
    }

    @Override
    public BigDecimal getTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        return revenueRepository.getTotalRevenueBetweenDates(startDate, endDate);
    }

    @Override
    public byte[] exportToExcel(LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue Data");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Revenue ID", "Beneficiary Type", "Beneficiary ID", "Source Type",
                    "Source ID", "Amount", "Date", "Description"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Get revenue data
            List<Revenue> revenues = getRevenuesByDateRange(startDate, endDate);

            // Create data rows
            int rowNum = 1;
            for (Revenue revenue : revenues) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(revenue.getRevenueId());
                row.createCell(1).setCellValue(revenue.getBeneficiaryType());
                row.createCell(2).setCellValue(revenue.getBeneficiaryId());
                row.createCell(3).setCellValue(revenue.getSourceType());
                row.createCell(4).setCellValue(revenue.getSourceId());
                row.createCell(5).setCellValue(revenue.getAmount().doubleValue());
                row.createCell(6).setCellValue(revenue.getDate().toString());
                row.createCell(7).setCellValue(revenue.getDescription());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage());
        }
    }
}