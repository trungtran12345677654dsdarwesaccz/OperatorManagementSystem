package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.TimeOffRequest;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Integer> {
    
    List<TimeOffRequest> findByOperatorId(Integer operatorId);
    
    List<TimeOffRequest> findByOperatorIdAndStatus(Integer operatorId, TimeOffStatus status);
    
    List<TimeOffRequest> findByStatus(TimeOffStatus status);
    
    List<TimeOffRequest> findByStatusOrderByRequestDateDesc(TimeOffStatus status);
    
    @Query("SELECT tor FROM TimeOffRequest tor WHERE tor.operator.id = :operatorId " +
           "AND tor.requestDate BETWEEN :startDate AND :endDate " +
           "ORDER BY tor.requestDate DESC")
    List<TimeOffRequest> findByOperatorAndDateRange(
        @Param("operatorId") Integer operatorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT tor FROM TimeOffRequest tor WHERE tor.operator.id = :operatorId " +
           "AND ((tor.startDate <= :endDate AND tor.endDate >= :startDate)) " +
           "AND tor.status = 'APPROVED'")
    List<TimeOffRequest> findOverlappingApprovedRequests(
        @Param("operatorId") Integer operatorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT tor FROM TimeOffRequest tor WHERE " +
           "((tor.startDate <= :endDate AND tor.endDate >= :startDate)) " +
           "AND tor.status IN ('PENDING', 'APPROVED')")
    List<TimeOffRequest> findOverlappingRequests(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(tor) FROM TimeOffRequest tor WHERE tor.status = 'PENDING'")
    long countPendingRequests();
    
    @Query("SELECT tor FROM TimeOffRequest tor WHERE tor.operator.id = :operatorId " +
           "AND tor.startDate <= :date AND tor.endDate >= :date AND tor.status = 'APPROVED'")
    List<TimeOffRequest> findApprovedTimeOffForDate(
        @Param("operatorId") Integer operatorId,
        @Param("date") LocalDate date);
}