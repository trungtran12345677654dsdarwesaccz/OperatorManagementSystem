package org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo;


import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentDashboardRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByPaidDate(LocalDate paidDate);

    @Query("SELECT p.paidDate, SUM(p.amount) FROM Payment p WHERE p.paidDate BETWEEN :start AND :end GROUP BY p.paidDate ORDER BY p.paidDate")
    List<Object[]> sumAmountByPaidDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
