package org.example.operatormanagementsystem.managestaff_yen.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.example.operatormanagementsystem.entity.Feedback;

import java.util.List;

public interface FeedbackPromotionRepository extends JpaRepository<Feedback, Long> {

    @Query("""
        SELECT COUNT(f) FROM Feedback f
        JOIN f.booking b
        WHERE b.promotion IS NOT NULL AND f.star >= 4
    """)
    long countPositiveFeedbackWithPromotion();

    @Query("""
    SELECT p.name, COUNT(f)
    FROM Feedback f
    JOIN f.booking b
    JOIN b.promotion p
    WHERE f.star >= 4
    GROUP BY p.name
    ORDER BY COUNT(f) DESC
""")
    List<Object[]> countPositiveFeedbackGroupedByPromotion();
}
