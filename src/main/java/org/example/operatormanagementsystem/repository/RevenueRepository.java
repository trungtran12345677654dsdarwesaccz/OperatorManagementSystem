package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.Revenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Integer>, JpaSpecificationExecutor<Revenue> {

    List<Revenue> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Revenue> findByBeneficiaryId(Integer beneficiaryId);

    List<Revenue> findBySourceType(String sourceType);

    @Query("SELECT r FROM Revenue r WHERE r.booking.bookingId = :bookingId")
    List<Revenue> findByBookingId(@Param("bookingId") Integer bookingId);


    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE r.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueBetweenDates(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
