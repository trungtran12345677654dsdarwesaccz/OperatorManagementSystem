package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    // Thêm phương thức tùy chỉnh nếu cần
}