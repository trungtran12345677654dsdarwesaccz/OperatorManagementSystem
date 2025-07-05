package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.ManagerFeedbackToStaff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ManagerFeedbackToStaffRepository extends JpaRepository<ManagerFeedbackToStaff, Integer> {

    // Tìm feedback theo manager
    List<ManagerFeedbackToStaff> findByManagerManagerId(Integer managerId);

    // Tìm feedback theo staff
    List<ManagerFeedbackToStaff> findByOperatorStaffOperatorId(Integer operatorId);

    // Tìm feedback theo manager và staff với phân trang
    Page<ManagerFeedbackToStaff> findByManagerManagerIdAndOperatorStaffOperatorId(
            Integer managerId, Integer operatorId, Pageable pageable);

    // Tìm feedback trong khoảng thời gian
    @Query("""
        SELECT mf FROM ManagerFeedbackToStaff mf 
        WHERE mf.manager.managerId = :managerId 
        AND mf.createdAt BETWEEN :startDate AND :endDate
    """)
    List<ManagerFeedbackToStaff> findByManagerAndDateRange(
            @Param("managerId") Integer managerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Đếm số feedback của manager
    long countByManagerManagerId(Integer managerId);

    // Đếm số feedback nhận được của staff
    long countByOperatorStaffOperatorId(Integer operatorId);

    // ✅ Tính điểm trung bình của operator, sử dụng COALESCE để tránh null
    @Query("""
        SELECT COALESCE(AVG(mf.rating), 0.0) FROM ManagerFeedbackToStaff mf 
        WHERE mf.operatorStaff.operatorId = :operatorId
    """)
    Double getAverageRatingForOperator(@Param("operatorId") Integer operatorId);
}
