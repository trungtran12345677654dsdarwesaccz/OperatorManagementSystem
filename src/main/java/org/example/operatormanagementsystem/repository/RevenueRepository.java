package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.Revenue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Integer> {

    List<Revenue> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Revenue> findByBeneficiaryId(Integer beneficiaryId);

    List<Revenue> findBySourceType(String sourceType);

    @Query("SELECT r FROM Revenue r WHERE r.booking.bookingId = :bookingId")
    List<Revenue> findByBookingId(@Param("bookingId") Integer bookingId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueBetweenDates(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // Advanced filtering with pagination
    @Query("SELECT r FROM Revenue r WHERE " +
           "(:startDate IS NULL OR r.date >= :startDate) AND " +
           "(:endDate IS NULL OR r.date <= :endDate) AND " +
           "(:beneficiaryType IS NULL OR r.beneficiaryType = :beneficiaryType) AND " +
           "(:sourceType IS NULL OR r.sourceType = :sourceType) AND " +
           "(:beneficiaryId IS NULL OR r.beneficiaryId = :beneficiaryId) AND " +
           "(:sourceId IS NULL OR r.sourceId = :sourceId)")
    Page<Revenue> findByFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("beneficiaryType") String beneficiaryType,
            @Param("sourceType") String sourceType,
            @Param("beneficiaryId") Integer beneficiaryId,
            @Param("sourceId") Integer sourceId,
            Pageable pageable
    );

    // Advanced filtering without pagination for export
    @Query("SELECT r FROM Revenue r WHERE " +
           "(:startDate IS NULL OR r.date >= :startDate) AND " +
           "(:endDate IS NULL OR r.date <= :endDate) AND " +
           "(:beneficiaryType IS NULL OR r.beneficiaryType = :beneficiaryType) AND " +
           "(:sourceType IS NULL OR r.sourceType = :sourceType) AND " +
           "(:beneficiaryId IS NULL OR r.beneficiaryId = :beneficiaryId) AND " +
           "(:sourceId IS NULL OR r.sourceId = :sourceId) " +
           "ORDER BY r.date DESC")
    List<Revenue> findByFiltersForExport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("beneficiaryType") String beneficiaryType,
            @Param("sourceType") String sourceType,
            @Param("beneficiaryId") Integer beneficiaryId,
            @Param("sourceId") Integer sourceId
    );
}
