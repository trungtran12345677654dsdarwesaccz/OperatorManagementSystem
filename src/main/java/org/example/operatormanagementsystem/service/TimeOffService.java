package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.dto.request.TimeOffApprovalRequest;
import org.example.operatormanagementsystem.dto.request.TimeOffRequestDto;
import org.example.operatormanagementsystem.dto.response.TimeOffStatusResponse;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TimeOffService {
    
    /**
     * Submit a new time-off request
     */
    TimeOffStatusResponse submitTimeOffRequest(TimeOffRequestDto request);
    
    /**
     * Approve a time-off request
     */
    TimeOffStatusResponse approveTimeOffRequest(TimeOffApprovalRequest request);
    
    /**
     * Reject a time-off request
     */
    TimeOffStatusResponse rejectTimeOffRequest(TimeOffApprovalRequest request);
    
    /**
     * Get time-off request by ID
     */
    TimeOffStatusResponse getTimeOffRequestById(Integer requestId);
    
    /**
     * Get all time-off requests for an operator
     */
    List<TimeOffStatusResponse> getOperatorTimeOffRequests(Integer operatorId);
    
    /**
     * Get time-off requests by status
     */
    List<TimeOffStatusResponse> getTimeOffRequestsByStatus(TimeOffStatus status);
    
    /**
     * Get pending time-off requests for manager review
     */
    List<TimeOffStatusResponse> getPendingTimeOffRequests();
    
    /**
     * Get time-off requests within date range
     */
    List<TimeOffStatusResponse> getTimeOffRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get operator's time-off requests within date range
     */
    List<TimeOffStatusResponse> getOperatorTimeOffRequestsByDateRange(
        Integer operatorId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Check for conflicts with existing assignments
     */
    boolean hasConflictWithAssignments(Integer operatorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get conflict details for a time-off request
     */
    String getConflictDetails(Integer operatorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Validate time-off request dates
     */
    void validateTimeOffRequest(TimeOffRequestDto request);
    
    /**
     * Cancel a time-off request (only if pending)
     */
    void cancelTimeOffRequest(Integer requestId, Integer operatorId);
    
    /**
     * Get approved time-off for a specific date
     */
    List<TimeOffStatusResponse> getApprovedTimeOffForDate(Integer operatorId, LocalDate date);
    
    /**
     * Count pending requests
     */
    long countPendingRequests();
}