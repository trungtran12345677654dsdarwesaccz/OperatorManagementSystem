package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.ShiftAssignment;
import org.example.operatormanagementsystem.enumeration.ShiftAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Integer> {
    
    List<ShiftAssignment> findByOperatorIdAndAssignmentDateBetween(
        Integer operatorId, LocalDate startDate, LocalDate endDate);
    
    List<ShiftAssignment> findByOperatorIdAndAssignmentDate(
        Integer operatorId, LocalDate assignmentDate);
    
    List<ShiftAssignment> findByWorkShiftShiftIdAndAssignmentDate(
        Integer shiftId, LocalDate assignmentDate);
    
    List<ShiftAssignment> findByAssignmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ShiftAssignment> findByStatus(ShiftAssignmentStatus status);
    
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.operator.id = :operatorId " +
           "AND sa.assignmentDate = :assignmentDate AND sa.status = :status")
    List<ShiftAssignment> findByOperatorAndDateAndStatus(
        @Param("operatorId") Integer operatorId, 
        @Param("assignmentDate") LocalDate assignmentDate,
        @Param("status") ShiftAssignmentStatus status);
    
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.operator.id = :operatorId " +
           "AND sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.assignmentDate, sa.workShift.startTime")
    List<ShiftAssignment> findOperatorSchedule(
        @Param("operatorId") Integer operatorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
    
    boolean existsByOperatorIdAndWorkShiftShiftIdAndAssignmentDate(
        Integer operatorId, Integer shiftId, LocalDate assignmentDate);

    // Add this method to get all assignments for a shift
    List<ShiftAssignment> findByWorkShiftShiftId(Integer shiftId);
}