package org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo;

import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingDashboardRepository extends JpaRepository<Booking, Integer> {

    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE DATE(b.createdAt) = :date
    """)
    long countByDate(@Param("date") LocalDate date);

    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE DATE(b.createdAt) = :date AND b.status = :status
    """)
    long countByDateAndStatus(@Param("date") LocalDate date, @Param("status") String status);

    @Query("""
        SELECT DATE(b.createdAt), COUNT(b)
        FROM Booking b
        WHERE DATE(b.createdAt) BETWEEN :start AND :end
        GROUP BY DATE(b.createdAt)
        ORDER BY DATE(b.createdAt)
    """)
    List<Object[]> countByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
        SELECT o.operatorId, o.users.fullName, COUNT(b),
               SUM(CASE WHEN b.status = 'ONTIME' THEN 1 ELSE 0 END) * 1.0 / COUNT(b) * 100
        FROM Booking b
        JOIN b.operatorStaff o
        WHERE DATE(b.createdAt) BETWEEN :start AND :end
        GROUP BY o.operatorId, o.users.fullName
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> getTopOperatorsStatsByDate(@Param("start") LocalDate start, @Param("end") LocalDate end);

    int countByOperatorStaffAndCreatedAtBetween(OperatorStaff staff, LocalDateTime from, LocalDateTime to);
}