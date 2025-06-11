package org.example.operatormanagementsystem.ManageHungBranch.repository;

import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Search by payer type
    List<Payment> findByPayerType(String payerType);

    // Search by payer ID
    List<Payment> findByPayerId(Integer payerId);

    // Search by status
    List<Payment> findByStatus(String status);

    // Search by date range
    List<Payment> findByPaidDateBetween(LocalDate startDate, LocalDate endDate);

    // Search by payer type and status
    List<Payment> findByPayerTypeAndStatus(String payerType, String status);

    // Search by payer type and payer ID
    List<Payment> findByPayerTypeAndPayerId(String payerType, Integer payerId);

    // Complex search with multiple criteria
    List<Payment> findByPayerTypeAndPayerIdAndStatusAndPaidDateBetween(
            String payerType, Integer payerId, String status, LocalDate startDate, LocalDate endDate);

    // Custom query to search by amount range
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
    List<Payment> findByAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount,
                                      @Param("maxAmount") java.math.BigDecimal maxAmount);

    // Custom query to search by note containing text
    @Query("SELECT p FROM Payment p WHERE p.note LIKE %:keyword%")
    List<Payment> findByNoteContaining(@Param("keyword") String keyword);

    // Get payments ordered by date (most recent first)
    List<Payment> findAllByOrderByPaidDateDesc();

    // Get payments by booking ID
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId")
    List<Payment> findByBookingId(@Param("bookingId") Integer bookingId);

    // Count payments by status
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);

    // Get total amount by status
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    java.math.BigDecimal getTotalAmountByStatus(@Param("status") String status);
}