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
    @Query("""
    SELECT FUNCTION('DATE', p.paidDate), SUM(p.amount)
    FROM Payment p
    WHERE p.paidDate BETWEEN :start AND :end
    GROUP BY FUNCTION('DATE', p.paidDate)
    ORDER BY FUNCTION('DATE', p.paidDate)
""")
    List<Object[]> sumAmountByPaidDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

}
