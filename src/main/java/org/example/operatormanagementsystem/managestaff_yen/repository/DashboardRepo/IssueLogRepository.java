package org.example.operatormanagementsystem.managestaff_yen.repository.DashboardRepo;

import org.example.operatormanagementsystem.entity.IssueLog;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IssueLogRepository extends JpaRepository<IssueLog, Integer> {

    @Query(value = """
        SELECT * FROM issue_log
        WHERE DATE(created_at) BETWEEN :from AND :to
        ORDER BY created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<IssueLog> findRecentIssuesNative(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("limit") int limit
    );

    int countByBooking_OperatorStaffAndCreatedAtBetween(OperatorStaff staff, LocalDateTime from, LocalDateTime to);

}
