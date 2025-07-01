package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.entity.Revenue;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public interface RevenueService {
    List<Revenue> getAllRevenues();

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
}