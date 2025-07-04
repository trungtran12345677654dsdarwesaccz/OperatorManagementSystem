package org.example.operatormanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.operatormanagementsystem.dto.request.RevenueFilterRequest;
import org.example.operatormanagementsystem.dto.response.PageResponse;
import org.example.operatormanagementsystem.dto.response.RevenueResponse;
import org.example.operatormanagementsystem.entity.Revenue;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.RevenueRepository;
import org.example.operatormanagementsystem.service.RevenueService;
import org.example.operatormanagementsystem.utils.VndFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
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
                    "Source ID", "Amount (VND)", "Date", "Description"};
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
                row.createCell(5).setCellValue(VndFormatter.format(revenue.getAmount()));
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

    @Override
    public byte[] exportToExcelWithFilters(LocalDate startDate, LocalDate endDate, String beneficiaryType, String sourceType, Integer beneficiaryId, Integer sourceId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue Data");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Revenue ID", "Beneficiary Type", "Beneficiary ID", "Source Type",
                    "Source ID", "Amount (VND)", "Date", "Description"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Get filtered revenue data using repository
            List<Revenue> revenues = revenueRepository.findByFiltersForExport(
                startDate, endDate, beneficiaryType, sourceType, beneficiaryId, sourceId);

            // Create data rows
            int rowNum = 1;
            
            for (Revenue revenue : revenues) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(revenue.getRevenueId());
                row.createCell(1).setCellValue(revenue.getBeneficiaryType());
                row.createCell(2).setCellValue(revenue.getBeneficiaryId());
                row.createCell(3).setCellValue(revenue.getSourceType());
                row.createCell(4).setCellValue(revenue.getSourceId());
                row.createCell(5).setCellValue(VndFormatter.format(revenue.getAmount()));
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

    @Override
    public PageResponse<RevenueResponse> getRevenuesWithFilters(RevenueFilterRequest filterRequest) {
        // Create pageable with sorting
        Sort sort = Sort.by(
            filterRequest.getSortDirection().equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, 
            filterRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
            filterRequest.getPage(), 
            filterRequest.getSize(), 
            sort
        );

        // Get filtered and paginated data
        Page<Revenue> revenuePage = revenueRepository.findByFilters(
            filterRequest.getStartDate(),
            filterRequest.getEndDate(),
            filterRequest.getBeneficiaryType(),
            filterRequest.getSourceType(),
            filterRequest.getBeneficiaryId(),
            filterRequest.getSourceId(),
            pageable
        );

        // Convert to response DTOs
        List<RevenueResponse> revenueResponses = revenuePage.getContent().stream()
            .map(this::convertToRevenueResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(
            revenueResponses,
            filterRequest.getPage(),
            filterRequest.getSize(),
            revenuePage.getTotalElements()
        );
    }
    private RevenueResponse convertToRevenueResponse(Revenue revenue) {
        RevenueResponse response = new RevenueResponse();
        response.setRevenueId(revenue.getRevenueId());
        response.setBeneficiaryType(revenue.getBeneficiaryType());
        response.setBeneficiaryId(revenue.getBeneficiaryId());
        response.setSourceType(revenue.getSourceType());
        response.setSourceId(revenue.getSourceId());
        response.setAmount(revenue.getAmount());
        
        // Format amount as VND
        response.setFormattedAmount(VndFormatter.format(revenue.getAmount()));
        
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