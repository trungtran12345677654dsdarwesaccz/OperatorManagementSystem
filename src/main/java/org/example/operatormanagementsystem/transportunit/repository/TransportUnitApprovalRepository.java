package org.example.operatormanagementsystem.transportunit.repository;

import org.example.operatormanagementsystem.entity.TransportUnitApproval;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.transportunit.dto.response.ApprovalTrendResponse;
import org.example.operatormanagementsystem.transportunit.dto.response.ManagerPerformanceResponse;
import org.example.operatormanagementsystem.transportunit.dto.response.WeeklyActivityResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransportUnitApprovalRepository extends JpaRepository<TransportUnitApproval, Integer> {

    Optional<TransportUnitApproval> findByTransportUnit_TransportIdAndStatus(Integer transportUnitId, ApprovalStatus status);

    TransportUnitApproval findTopByTransportUnit_TransportIdOrderByRequestedAtDesc(Integer transportUnitId);

    int countByStatus(ApprovalStatus status);

    @Query("SELECT COUNT(a) FROM TransportUnitApproval a WHERE a.status = :status AND FUNCTION('DATE', a.processedAt) = CURRENT_DATE")
    int countTodayByStatus(@Param("status") ApprovalStatus status);

    @Query("SELECT COUNT(a) FROM TransportUnitApproval a WHERE a.status = :status")
    long countByApprovalStatus(@Param("status") ApprovalStatus status);

    @Query("SELECT COUNT(a) FROM TransportUnitApproval a WHERE a.status IN (:approved, :rejected)")
    long countTotalApprovedAndRejected(@Param("approved") ApprovalStatus approved, @Param("rejected") ApprovalStatus rejected);

    @Query("SELECT a FROM TransportUnitApproval a WHERE a.status IN (:s1, :s2) AND a.processedAt IS NOT NULL")
    List<TransportUnitApproval> findProcessedApprovals(@Param("s1") ApprovalStatus s1, @Param("s2") ApprovalStatus s2);

    @Query(
            value = "SELECT " +
                    "day_of_week, " +
                    "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approvals, " +
                    "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejections, " +
                    "ROUND(AVG(TIMESTAMPDIFF(SECOND, requested_at, processed_at)) / 3600, 2) AS avgProcessingTime " +
                    "FROM ( " +
                    "   SELECT approval_id, status, requested_at, processed_at, DAYNAME(processed_at) AS day_of_week " +
                    "   FROM transport_unit_approval " +
                    "   WHERE processed_at IS NOT NULL " +
                    ") AS t " +
                    "GROUP BY day_of_week " +
                    "ORDER BY FIELD(day_of_week, 'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')",
            nativeQuery = true
    )
    List<WeeklyActivityResponse> getWeeklyActivityStats();



    @Query(
            value = "SELECT " +
                    "u.full_name AS managerName, " +
                    "u.email AS managerEmail, " +
                    "COUNT(a.approval_id) AS totalProcessed, " +
                    "SUM(CASE WHEN a.status = 'APPROVED' THEN 1 ELSE 0 END) AS approved, " +
                    "SUM(CASE WHEN a.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected, " +
                    "SUM(CASE WHEN a.status = 'PENDING' THEN 1 ELSE 0 END) AS pending, " +
                    "ROUND(SUM(CASE WHEN a.status = 'APPROVED' THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 2) AS approvalRate, " +
                    "ROUND(AVG(TIMESTAMPDIFF(SECOND, a.requested_at, a.processed_at)) / 3600, 2) AS avgProcessingTime " +
                    "FROM transport_unit_approval a " +
                    "JOIN manager m ON a.approved_by_manager_id = m.manager_id " +
                    "JOIN users u ON m.manager_id = u.id " +
                    "WHERE a.processed_at IS NOT NULL AND a.requested_at BETWEEN :start AND :end " +
                    "GROUP BY u.id",
            nativeQuery = true
    )
    List<ManagerPerformanceResponse> getManagerPerformanceBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );




    @Query(
            value = "SELECT " +
                    "DATE(processed_at) AS date, " +
                    "COUNT(*) AS submissions, " +
                    "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approvals, " +
                    "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejections, " +
                    "ROUND(SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) * 1.0 / COUNT(*), 2) AS approvalRate " +
                    "FROM transport_unit_approval " +
                    "WHERE processed_at >= :fromDate " +
                    "GROUP BY DATE(processed_at) " +
                    "ORDER BY DATE(processed_at)",
            nativeQuery = true
    )
    List<ApprovalTrendResponse> getApprovalTrendsSince(@Param("fromDate") LocalDate fromDate);




    @Query(
            value = "SELECT " +
                    "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) * 1.0 / COUNT(*) " +
                    "FROM transport_unit_approval " +
                    "WHERE status IN ('APPROVED', 'REJECTED')",
            nativeQuery = true
    )
    double countApprovalRate();

    @Query(
            value = "SELECT " +
                    "AVG(TIMESTAMPDIFF(SECOND, requested_at, processed_at)) / 3600 " +
                    "FROM transport_unit_approval " +
                    "WHERE status IN ('APPROVED', 'REJECTED') AND processed_at IS NOT NULL",
            nativeQuery = true
    )
    double calculateAvgProcessingTime();

}
