package org.example.operatormanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.operatormanagementsystem.dto.response.RevenueResponse;
import org.example.operatormanagementsystem.entity.Revenue;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.RevenueRepository;
import org.example.operatormanagementsystem.service.RevenueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final RevenueRepository revenueRepository;

    @Override
    public List<RevenueResponse> getAllRevenues() {
        List<Revenue> revenues = revenueRepository.findAll();
        return revenues.stream()
                .map(this::convertToRevenueResponse)
                .collect(Collectors.toList());
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
                row.createCell(0).setCellValue(revenue.getRevenueId() != null ? String.valueOf(revenue.getRevenueId()) : "");
                row.createCell(1).setCellValue(revenue.getBeneficiaryType() != null ? revenue.getBeneficiaryType() : "");
                row.createCell(2).setCellValue(revenue.getBeneficiaryId() != null ? String.valueOf(revenue.getBeneficiaryId()) : "");
                row.createCell(3).setCellValue(revenue.getSourceType() != null ? revenue.getSourceType() : "");
                row.createCell(4).setCellValue(revenue.getSourceId() != null ? String.valueOf(revenue.getSourceId()) : "");
                row.createCell(5).setCellValue(revenue.getAmount() != null ? revenue.getAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(revenue.getDate() != null ? revenue.getDate().toString() : "");
                row.createCell(7).setCellValue(revenue.getDescription() != null ? revenue.getDescription() : "");
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

    @Override
    public Page<RevenueResponse> getPagedRevenues(Pageable pageable, String startDate, String endDate, String sourceType, String beneficiaryId, String bookingId, String minAmount, String maxAmount) {
        // Basic implementation: fetch all, filter in memory (for demo, replace with JPA Specification for production)
        List<Revenue> all = revenueRepository.findAll();
        List<Revenue> filtered = all.stream()
            .filter(r -> startDate == null || startDate.isEmpty() || !r.getDate().isBefore(LocalDate.parse(startDate)))
            .filter(r -> endDate == null || endDate.isEmpty() || !r.getDate().isAfter(LocalDate.parse(endDate)))
            .filter(r -> sourceType == null || sourceType.isEmpty() || sourceType.equals(r.getSourceType()))
            .filter(r -> beneficiaryId == null || beneficiaryId.isEmpty() || beneficiaryId.equals(String.valueOf(r.getBeneficiaryId())))
            .filter(r -> bookingId == null || bookingId.isEmpty() || (r.getBooking() != null && bookingId.equals(String.valueOf(r.getBooking().getBookingId()))))
            .filter(r -> minAmount == null || minAmount.isEmpty() || r.getAmount().compareTo(new BigDecimal(minAmount)) >= 0)
            .filter(r -> maxAmount == null || maxAmount.isEmpty() || r.getAmount().compareTo(new BigDecimal(maxAmount)) <= 0)
            .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<RevenueResponse> content = filtered.subList(start, end).stream().map(this::convertToRevenueResponse).toList();
        return new PageImpl<>(content, pageable, filtered.size());
    }

    @Override
    public byte[] exportToExcelWithFilter(String startDate, String endDate, String sourceType, String beneficiaryId, String bookingId, String minAmount, String maxAmount) {
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
            // Filter data (same as getPagedRevenues, but no paging)
            List<Revenue> all = revenueRepository.findAll();
            List<Revenue> filtered = all.stream()
                .filter(r -> startDate == null || startDate.isEmpty() || !r.getDate().isBefore(LocalDate.parse(startDate)))
                .filter(r -> endDate == null || endDate.isEmpty() || !r.getDate().isAfter(LocalDate.parse(endDate)))
                .filter(r -> sourceType == null || sourceType.isEmpty() || sourceType.equals(r.getSourceType()))
                .filter(r -> beneficiaryId == null || beneficiaryId.isEmpty() || beneficiaryId.equals(String.valueOf(r.getBeneficiaryId())))
                .filter(r -> bookingId == null || bookingId.isEmpty() || (r.getBooking() != null && bookingId.equals(String.valueOf(r.getBooking().getBookingId()))))
                .filter(r -> minAmount == null || minAmount.isEmpty() || r.getAmount().compareTo(new BigDecimal(minAmount)) >= 0)
                .filter(r -> maxAmount == null || maxAmount.isEmpty() || r.getAmount().compareTo(new BigDecimal(maxAmount)) <= 0)
                .toList();
            // Create data rows
            int rowNum = 1;
            for (Revenue revenue : filtered) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(revenue.getRevenueId() != null ? String.valueOf(revenue.getRevenueId()) : "");
                row.createCell(1).setCellValue(revenue.getBeneficiaryType() != null ? revenue.getBeneficiaryType() : "");
                row.createCell(2).setCellValue(revenue.getBeneficiaryId() != null ? String.valueOf(revenue.getBeneficiaryId()) : "");
                row.createCell(3).setCellValue(revenue.getSourceType() != null ? revenue.getSourceType() : "");
                row.createCell(4).setCellValue(revenue.getSourceId() != null ? String.valueOf(revenue.getSourceId()) : "");
                row.createCell(5).setCellValue(revenue.getAmount() != null ? revenue.getAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(revenue.getDate() != null ? revenue.getDate().toString() : "");
                row.createCell(7).setCellValue(revenue.getDescription() != null ? revenue.getDescription() : "");
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage());
        }
    }

    private RevenueResponse convertToRevenueResponse(Revenue revenue) {
        RevenueResponse response = new RevenueResponse();
        response.setRevenueId(revenue.getRevenueId());
        response.setBeneficiaryType(revenue.getBeneficiaryType());
        response.setBeneficiaryId(revenue.getBeneficiaryId());
        response.setSourceType(revenue.getSourceType());
        response.setSourceId(revenue.getSourceId());
        response.setAmount(revenue.getAmount());
        response.setDate(revenue.getDate());
        response.setDescription(revenue.getDescription());

        if (revenue.getBooking() != null) {
            RevenueResponse.BookingBasicInfo bookingInfo = new RevenueResponse.BookingBasicInfo();
            bookingInfo.setBookingId(revenue.getBooking().getBookingId());

            if (revenue.getBooking().getCustomer() != null) {
                RevenueResponse.CustomerBasicInfo customerInfo = new RevenueResponse.CustomerBasicInfo();
                customerInfo.setCustomerId(revenue.getBooking().getCustomer().getCustomerId());

                if (revenue.getBooking().getCustomer().getUsers() != null) {
                    RevenueResponse.UserBasicInfo userInfo = new RevenueResponse.UserBasicInfo();
                    Users user = revenue.getBooking().getCustomer().getUsers();
                    userInfo.setId(user.getId());
                    userInfo.setFullName(user.getFullName());
                    userInfo.setEmail(user.getEmail());
                    customerInfo.setUsers(userInfo);
                }

                bookingInfo.setCustomer(customerInfo);
            }

            response.setBooking(bookingInfo);
        }

        return response;
    }

}