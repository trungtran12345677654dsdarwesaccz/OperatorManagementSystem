package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    // ✅ Thêm method tìm theo tên (ignore case)
    List<Promotion> findByNameContainingIgnoreCase(String keyword);
}
