package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByNameContainingIgnoreCase(String keyword);

    @Query("""
        SELECT p FROM Promotion p
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:status IS NULL OR p.status = :status)
    """)
    List<Promotion> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") String status);
    long countByStatus(String status);
}