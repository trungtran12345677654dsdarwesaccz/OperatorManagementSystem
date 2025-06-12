package org.example.operatormanagementsystem.ManageHungBranch.repository;

import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentSearchDTO;
import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Tìm theo trạng thái
    List<Payment> findByStatus(String status);

    // Tìm payment quá hạn
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.paidDate < CURRENT_DATE")
    List<Payment> findOverduePayments();

    // Tìm payment theo khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paidDate BETWEEN :fromDate AND :toDate")
    List<Payment> findByDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // Tìm payment theo payer type
    List<Payment> findByPayerType(String payerType);

    // Tìm payment theo payer ID
    List<Payment> findByPayerId(Integer payerId);

    // Custom search method (cần implement bằng Specification hoặc @Query phức tạp)
    @Query("SELECT p FROM Payment p WHERE " +
            "(:#{#search.status} IS NULL OR p.status = :#{#search.status}) AND " +
            "(:#{#search.payerType} IS NULL OR p.payerType = :#{#search.payerType}) AND " +
            "(:#{#search.payerId} IS NULL OR p.payerId = :#{#search.payerId}) AND " +
            "(:#{#search.fromDate} IS NULL OR p.paidDate >= :#{#search.fromDate}) AND " +
            "(:#{#search.toDate} IS NULL OR p.paidDate <= :#{#search.toDate}) AND " +
            "(:#{#search.minAmount} IS NULL OR p.amount >= :#{#search.minAmount}) AND " +
            "(:#{#search.maxAmount} IS NULL OR p.amount <= :#{#search.maxAmount})")
    Page<Payment> searchPayments(@Param("search") PaymentSearchDTO search, Pageable pageable);

    // Thống kê payment theo trạng thái
    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> getPaymentStatsByStatus();

    // Tổng số tiền theo trạng thái
    @Query("SELECT p.status, SUM(p.amount) FROM Payment p GROUP BY p.status")
    List<Object[]> getTotalAmountByStatus();
}