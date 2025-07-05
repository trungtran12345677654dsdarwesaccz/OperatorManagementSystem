package org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo;


import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingDashboardRepository extends JpaRepository<Booking, Integer> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetweenAndStatus(LocalDateTime start, LocalDateTime end, String status);

    @Query("SELECT DATE(b.createdAt), COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end GROUP BY DATE(b.createdAt) ORDER BY DATE(b.createdAt)")
    List<Object[]> countByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
        SELECT o.operatorId, o.users.fullName, COUNT(b), 
               SUM(CASE WHEN b.status = 'ONTIME' THEN 1 ELSE 0 END) * 1.0 / COUNT(b) * 100
        FROM Booking b 
        JOIN b.operatorStaff o 
        GROUP BY o.operatorId, o.users.fullName 
        ORDER BY COUNT(b) DESC
        """)
    List<Object[]> getTopOperatorsStats();
}
