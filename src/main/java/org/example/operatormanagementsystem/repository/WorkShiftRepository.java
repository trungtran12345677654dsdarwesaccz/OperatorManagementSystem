package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Integer> {
    
    List<WorkShift> findByIsActiveTrue();
    
    List<WorkShift> findByShiftNameContainingIgnoreCase(String shiftName);
    
    @Query("SELECT ws FROM WorkShift ws WHERE ws.isActive = true AND " +
           "((ws.startTime <= :startTime AND ws.endTime > :startTime) OR " +
           "(ws.startTime < :endTime AND ws.endTime >= :endTime) OR " +
           "(ws.startTime >= :startTime AND ws.endTime <= :endTime))")
    List<WorkShift> findOverlappingShifts(@Param("startTime") LocalTime startTime, 
                                         @Param("endTime") LocalTime endTime);
    
    @Query("SELECT ws FROM WorkShift ws WHERE ws.isActive = true ORDER BY ws.startTime")
    List<WorkShift> findAllActiveOrderByStartTime();
}