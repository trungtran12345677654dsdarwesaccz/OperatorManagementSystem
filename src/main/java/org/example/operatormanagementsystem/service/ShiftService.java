package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.dto.request.CreateShiftRequest;
import org.example.operatormanagementsystem.dto.request.ShiftAssignmentRequest;
import org.example.operatormanagementsystem.dto.response.ShiftDetailsResponse;
import org.example.operatormanagementsystem.entity.WorkShift;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;

import java.time.LocalDate;
import java.util.List;

public interface ShiftService {
    
    /**
     * Create a new work shift
     */
    ShiftDetailsResponse createShift(CreateShiftRequest request);
    
    /**
     * Update an existing work shift
     */
    ShiftDetailsResponse updateShift(Integer shiftId, CreateShiftRequest request);
    
    /**
     * Get shift details by ID
     */
    ShiftDetailsResponse getShiftById(Integer shiftId);
    
    /**
     * Get all active shifts
     */
    List<ShiftDetailsResponse> getAllActiveShifts();
    
    /**
     * Get all shifts (active and inactive)
     */
    List<ShiftDetailsResponse> getAllShifts();
    
    /**
     * Delete a shift (soft delete - set inactive)
     */
    void deleteShift(Integer shiftId);
    
    /**
     * Assign operators to a shift for a specific date
     */
    void assignOperatorsToShift(ShiftAssignmentRequest request);
    
    /**
     * Remove operator assignment from a shift
     */
    void removeOperatorAssignment(Integer shiftId, Integer operatorId, LocalDate assignmentDate);
    
    /**
     * Get shift assignments for a specific operator
     */
    List<ShiftDetailsResponse> getOperatorShiftAssignments(Integer operatorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Check for shift time conflicts
     */
    boolean hasTimeConflict(Integer shiftId, Integer operatorId, LocalDate assignmentDate);
    
    /**
     * Validate shift time ranges
     */
    void validateShiftTimes(CreateShiftRequest request);
    
    /**
     * Get available operators for a shift assignment
     */
    List<UserSearchResponse> getAvailableOperators(Integer shiftId, LocalDate assignmentDate);
}