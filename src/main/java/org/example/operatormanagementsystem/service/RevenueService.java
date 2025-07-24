package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.dto.response.RevenueResponse;
import org.example.operatormanagementsystem.entity.Revenue;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RevenueService {
    List<RevenueResponse> getAllRevenues();

    Revenue getRevenueById(Integer id);

    Revenue createRevenue(Revenue revenue);

    Revenue updateRevenue(Integer id, Revenue revenue);

    void deleteRevenue(Integer id);

    List<Revenue> getRevenuesByDateRange(LocalDate startDate, LocalDate endDate);

    List<Revenue> getRevenuesByBeneficiary(Integer beneficiaryId);

    List<Revenue> getRevenuesBySourceType(String sourceType);

    List<Revenue> getRevenuesByBooking(Integer bookingId);

    BigDecimal getTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate);

    byte[] exportToExcel(LocalDate startDate, LocalDate endDate);

    Page<RevenueResponse> getPagedRevenues(Pageable pageable, String startDate, String endDate, String sourceType, String beneficiaryId, String bookingId, String minAmount, String maxAmount);

    byte[] exportToExcelWithFilter(String startDate, String endDate, String sourceType, String beneficiaryId, String bookingId, String minAmount, String maxAmount);
}