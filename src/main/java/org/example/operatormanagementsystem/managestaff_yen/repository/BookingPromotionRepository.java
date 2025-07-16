package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingPromotionRepository extends JpaRepository<Booking, Long> {

    long countByPromotionIsNotNull();

    @Query("SELECT SUM(b.total) FROM Booking b WHERE b.promotion IS NOT NULL")
    Double sumTotalByPromotionNotNull();

    @Query(value = """
        SELECT DATE_FORMAT(b.created_at, :pattern) AS date, SUM(b.total) AS total
        FROM booking b
        WHERE b.promotion_id IS NOT NULL
          AND (:from IS NULL OR b.created_at >= :from)
          AND (:to IS NULL OR b.created_at <= :to)
        GROUP BY DATE_FORMAT(b.created_at, :pattern)
        ORDER BY DATE_FORMAT(b.created_at, :pattern) ASC
    """, nativeQuery = true)
    List<Object[]> sumPromotionRevenue(@Param("pattern") String pattern,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    @Query(value = """
        SELECT DATE_FORMAT(b.created_at, :pattern) AS date, COUNT(b.booking_id) AS count
        FROM booking b
        WHERE b.promotion_id IS NOT NULL
          AND (:from IS NULL OR b.created_at >= :from)
          AND (:to IS NULL OR b.created_at <= :to)
        GROUP BY DATE_FORMAT(b.created_at, :pattern)
        ORDER BY DATE_FORMAT(b.created_at, :pattern) ASC
    """, nativeQuery = true)
    List<Object[]> countBookingsWithPromotion(@Param("pattern") String pattern,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);
}
