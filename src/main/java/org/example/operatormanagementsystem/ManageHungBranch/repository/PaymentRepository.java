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
<<<<<<< HEAD


=======
    // Tìm theo trạng thái
    List<Payment> findByStatus(String status);
    List<Payment> findByStatusIn(List<String> statuses); // Thêm để hỗ trợ tương lai
>>>>>>> origin/phong

    List<Payment> findTop3ByBooking_Customer_Users_EmailOrderByPaidDateDesc(String email);


    // Tìm payment theo khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paidDate BETWEEN :fromDate AND :toDate")
    List<Payment> findByDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // Custom search method
    @Query("""
    SELECT p FROM Payment p
    WHERE 
      (:#{#search.fromDate} IS NULL OR p.paidDate >= :#{#search.fromDate})
      AND (:#{#search.toDate} IS NULL OR p.paidDate <= :#{#search.toDate})
      AND (:#{#search.minAmount} IS NULL OR p.amount >= :#{#search.minAmount})
      AND (:#{#search.maxAmount} IS NULL OR p.amount <= :#{#search.maxAmount})
      AND (:#{#search.payerId} IS NULL OR p.payer.id = :#{#search.payerId})
""")
    Page<Payment> searchPayments(@Param("search") PaymentSearchDTO search, Pageable pageable);

<<<<<<< HEAD
    Payment findByTransactionNo(String transactionNo);
=======
    // Thống kê payment theo trạng thái
    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> getPaymentStatsByStatus();

    // Tổng số tiền theo trạng thái
    @Query("SELECT p.status, SUM(p.amount) FROM Payment p GROUP BY p.status")
    List<Object[]> getTotalAmountByStatus();
>>>>>>> origin/phong
}