package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManagerBookingRepository extends JpaRepository<Booking, Integer> {

    @Query("""
        SELECT COUNT(b) FROM Booking b 
        WHERE b.operatorStaff.manager.managerId = :managerId AND DATE(b.createdAt) = :createdDate
    """)
    long countByManagerAndCreatedDate(@Param("managerId") Integer managerId, @Param("createdDate") LocalDate createdDate);

    @Query("""
        SELECT COUNT(b) FROM Booking b 
        WHERE b.operatorStaff.manager.managerId = :managerId AND DATE(b.createdAt) = :createdDate AND b.status = :status
    """)
    long countByManagerAndCreatedDateAndStatus(@Param("managerId") Integer managerId, @Param("createdDate") LocalDate createdDate, @Param("status") String status);

    @Query("""
        SELECT COUNT(b) FROM Booking b 
        WHERE b.operatorStaff.manager.managerId = :managerId AND b.status = :status
    """)
    long countByManagerAndStatus(@Param("managerId") Integer managerId, @Param("status") String status);

    @Query("""
        SELECT SUM(b.total) FROM Booking b 
        WHERE b.operatorStaff.manager.managerId = :managerId AND DATE(b.createdAt) = :createdDate
    """)
    Long sumRevenueByManagerAndCreatedDate(@Param("managerId") Integer managerId, @Param("createdDate") LocalDate createdDate);

    @Query("""
        SELECT COUNT(b) FROM Booking b 
        WHERE b.operatorStaff.manager.managerId = :managerId 
        AND FUNCTION('YEAR', b.createdAt) = :year AND FUNCTION('MONTH', b.createdAt) = :month 
        AND b.status = :status
    """)
    long countByManagerAndMonthAndStatus(@Param("managerId") Integer managerId, @Param("year") int year, @Param("month") int month, @Param("status") String status);

    @Query("""
        SELECT b FROM Booking b 
        WHERE b.operatorStaff.manager.managerId = :managerId AND b.note IS NOT NULL 
        ORDER BY b.createdAt DESC
    """)
    List<Booking> findRecentIssuesByManager(@Param("managerId") Integer managerId);
}
