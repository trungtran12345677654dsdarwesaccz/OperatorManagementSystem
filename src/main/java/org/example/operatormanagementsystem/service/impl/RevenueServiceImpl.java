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
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

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
        Specification<Revenue> spec = buildRevenueSpecification(startDate, endDate, sourceType, beneficiaryId, bookingId, minAmount, maxAmount);
        Page<Revenue> page = revenueRepository.findAll(spec, pageable);
        List<RevenueResponse> content = page.getContent().stream().map(this::convertToRevenueResponse).toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
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
            // Filter data using JPA Specification
            Specification<Revenue> spec = buildRevenueSpecification(startDate, endDate, sourceType, beneficiaryId, bookingId, minAmount, maxAmount);
            List<Revenue> filtered = revenueRepository.findAll(spec);
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

    private Specification<Revenue> buildRevenueSpecification(String startDate, String endDate, String sourceType, String beneficiaryId, String bookingId, String minAmount, String maxAmount) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (startDate != null && !startDate.isEmpty()) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("date"), LocalDate.parse(startDate)));
            }
            if (endDate != null && !endDate.isEmpty()) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("date"), LocalDate.parse(endDate)));
            }
            if (sourceType != null && !sourceType.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("sourceType"), sourceType));
            }
            if (beneficiaryId != null && !beneficiaryId.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("beneficiaryId"), Integer.valueOf(beneficiaryId)));
            }
            if (bookingId != null && !bookingId.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.join("booking").get("bookingId"), Integer.valueOf(bookingId)));
            }
            if (minAmount != null && !minAmount.isEmpty()) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("amount"), new BigDecimal(minAmount)));
            }
            if (maxAmount != null && !maxAmount.isEmpty()) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("amount"), new BigDecimal(maxAmount)));
            }
            return predicate;
        };
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