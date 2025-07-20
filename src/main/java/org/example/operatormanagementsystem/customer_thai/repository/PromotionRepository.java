package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Promotion;
import org.example.operatormanagementsystem.enumeration.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository("promotionRepository_thai")
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    // Tìm promotion theo tên
    Optional<Promotion> findByName(String name);
    
    // Tìm tất cả promotion đang hoạt động (status = "ACTIVE" và trong thời gian hiệu lực)
    List<Promotion> findByStatusAndStartDateBeforeAndEndDateAfter(PromotionStatus status, Date currentDate, Date currentDate2);
    
    // Tìm promotion theo status
    List<Promotion> findByStatus(PromotionStatus status);
} 