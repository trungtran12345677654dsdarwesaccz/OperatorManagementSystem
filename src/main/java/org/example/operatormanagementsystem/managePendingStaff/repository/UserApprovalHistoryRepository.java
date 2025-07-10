package org.example.operatormanagementsystem.managePendingStaff.repository;

import org.example.operatormanagementsystem.entity.UserApprovalHistory;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserApprovalHistoryRepository extends JpaRepository<UserApprovalHistory, Long> {
    List<UserApprovalHistory> findByUserOrderByApprovedAtDesc(Users user);
    List<UserApprovalHistory> findByUserAndStatusOrderByApprovedAtDesc(Users user, ApprovalStatus status);
    List<UserApprovalHistory> findByApprovedByOrderByApprovedAtDesc(Users approvedBy);
    @Query("""
    SELECT u FROM UserApprovalHistory u
    WHERE
        (:userEmail IS NULL OR EXISTS (
            SELECT 1 FROM Users usr WHERE usr = u.user AND LOWER(usr.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))
        ))
        AND (:approvedByEmail IS NULL OR EXISTS (
            SELECT 1 FROM Users apv WHERE apv = u.approvedBy AND LOWER(apv.email) LIKE LOWER(CONCAT('%', :approvedByEmail, '%'))
        ))
        AND (:status IS NULL OR u.status = :status)
        AND (:fromDate IS NULL OR u.approvedAt >= :fromDate)
        AND (:toDate IS NULL OR u.approvedAt <= :toDate)
    """)
    Page<UserApprovalHistory> searchApprovalHistory(
            @Param("userEmail") String userEmail,
            @Param("approvedByEmail") String approvedByEmail,
            @Param("status") ApprovalStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("""
    SELECT u FROM UserApprovalHistory u
    WHERE LOWER(u.approvedBy.email) = LOWER(:email)
    ORDER BY u.approvedAt DESC
    """)
    Page<UserApprovalHistory> findByManagerEmail(
            @Param("email") String email,
            Pageable pageable
    );



}