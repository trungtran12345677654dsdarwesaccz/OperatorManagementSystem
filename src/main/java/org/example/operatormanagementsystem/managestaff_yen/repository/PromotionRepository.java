package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
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

    @Query("""
        SELECT COUNT(p) FROM Promotion p 
        WHERE (:from IS NULL OR p.startDate >= :from) 
          AND (:to IS NULL OR p.endDate <= :to)
    """)
    long countTotal(@Param("from") Date from, @Param("to") Date to);

    @Query("""
        SELECT COUNT(p) FROM Promotion p 
        WHERE p.status = :status 
          AND (:from IS NULL OR p.startDate >= :from) 
          AND (:to IS NULL OR p.endDate <= :to)
    """)
    long countByStatus(@Param("status") String status,
                       @Param("from") Date from,
                       @Param("to") Date to);



}
