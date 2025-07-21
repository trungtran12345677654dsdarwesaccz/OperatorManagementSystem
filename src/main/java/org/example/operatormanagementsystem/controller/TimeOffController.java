package org.example.operatormanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.dto.request.TimeOffApprovalRequest;
import org.example.operatormanagementsystem.dto.request.TimeOffRequestDto;
import org.example.operatormanagementsystem.dto.response.TimeOffStatusResponse;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.managercustomer.service.UserService;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.service.TimeOffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timeoff")
@RequiredArgsConstructor
@Slf4j
public class TimeOffController {
    
    private final TimeOffService timeOffService;
    private final OperatorStaffRepository operatorStaffRepository;
    @Autowired
    private UserService userService;
    /**
     * Submit a new time-off request
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<TimeOffStatusResponse> submitTimeOffRequest(@Valid @RequestBody TimeOffRequestDto request) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            UserSearchResponse user = userService.findUserResponseByEmail(email);
            request.setOperatorId(user.getId());

        // Kiểm tra lại operatorId sau khi đã cố gắng lấy từ token
        if (request.getOperatorId() == null) {
            log.error("Operator ID is still null after attempting to retrieve from authentication");
            throw new IllegalArgumentException("Operator ID is required but was not provided and could not be determined from authentication");
        }
        
        log.debug("Submitting time-off request for operator {}", request.getOperatorId());
        
        TimeOffStatusResponse response = timeOffService.submitTimeOffRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Approve a time-off request
     */
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TimeOffStatusResponse> approveTimeOffRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody TimeOffApprovalRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserSearchResponse user = userService.findUserResponseByEmail(email);
        
        // Ensure the request ID matches the path variable
        request.setRequestId(requestId);
        
        TimeOffStatusResponse response = timeOffService.approveTimeOffRequest(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reject a time-off request
     */
    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TimeOffStatusResponse> rejectTimeOffRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody TimeOffApprovalRequest request) {

        request.setRequestId(requestId);
        
        TimeOffStatusResponse response = timeOffService.rejectTimeOffRequest(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time-off request by ID
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<TimeOffStatusResponse> getTimeOffRequestById(@PathVariable Integer requestId) {
        log.debug("Getting time-off request by ID: {}", requestId);
        
        TimeOffStatusResponse response = timeOffService.getTimeOffRequestById(requestId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all time-off requests for an operator
     */
    @GetMapping("/staff-request")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<TimeOffStatusResponse>> getOperatorTimeOffRequests() {
        log.debug("Getting time-off requests for operator: ");
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            UserSearchResponse operator = userService.findUserResponseByEmail(email);
            Integer operatorId = operator.getId();
        
        List<TimeOffStatusResponse> response = timeOffService.getOperatorTimeOffRequests(operatorId);
        log.debug("Time-off requests for operator: {}", response);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time-off requests by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TimeOffStatusResponse>> getTimeOffRequestsByStatus(@PathVariable TimeOffStatus status) {
        log.debug("Getting time-off requests by status: {}", status);
        
        List<TimeOffStatusResponse> response = timeOffService.getTimeOffRequestsByStatus(status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get pending time-off requests for manager review
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TimeOffStatusResponse>> getPendingTimeOffRequests() {
        log.debug("Getting pending time-off requests");
        
        List<TimeOffStatusResponse> response = timeOffService.getPendingTimeOffRequests();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time-off requests within date range
     */
    @GetMapping("/range")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TimeOffStatusResponse>> getTimeOffRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Getting time-off requests from {} to {}", startDate, endDate);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        // Limit the range to prevent excessive data
        if (startDate.plusDays(365).isBefore(endDate)) {
            throw new IllegalArgumentException("Date range cannot exceed 365 days");
        }
        
        List<TimeOffStatusResponse> response = timeOffService.getTimeOffRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get operator's time-off requests within date range
     */
    @GetMapping("/requests/{operatorId}/range")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<TimeOffStatusResponse>> getOperatorTimeOffRequestsByDateRange(
            @PathVariable Integer operatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Getting time-off requests for operator {} from {} to {}", operatorId, startDate, endDate);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        // Limit the range to prevent excessive data
        if (startDate.plusDays(365).isBefore(endDate)) {
            throw new IllegalArgumentException("Date range cannot exceed 365 days");
        }
        
        List<TimeOffStatusResponse> response = timeOffService.getOperatorTimeOffRequestsByDateRange(
                operatorId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a time-off request (only if pending)
     */
    @DeleteMapping("/{requestId}/cancel")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> cancelTimeOffRequest(
            @PathVariable Integer requestId) {
        
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            UserSearchResponse operator = userService.findUserResponseByEmail(email);
            Integer operatorId = operator.getId();
        
        timeOffService.cancelTimeOffRequest(requestId, operatorId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get approved time-off for a specific date and operator
     */
    @GetMapping("/approved/{operatorId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<TimeOffStatusResponse>> getApprovedTimeOffForDate(
            @PathVariable Integer operatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        
        log.debug("Getting approved time-off for operator {} on date {}", operatorId, date);
        
        List<TimeOffStatusResponse> response = timeOffService.getApprovedTimeOffForDate(operatorId, date);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get time-off statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TimeOffStatistics> getTimeOffStatistics() {
        log.debug("Getting time-off statistics");
        
        long pendingCount = timeOffService.countPendingRequests();
        List<TimeOffStatusResponse> allPending = timeOffService.getPendingTimeOffRequests();
        List<TimeOffStatusResponse> allApproved = timeOffService.getTimeOffRequestsByStatus(TimeOffStatus.APPROVED);
        List<TimeOffStatusResponse> allRejected = timeOffService.getTimeOffRequestsByStatus(TimeOffStatus.REJECTED);
        
        TimeOffStatistics stats = TimeOffStatistics.builder()
                .pendingRequests(pendingCount)
                .approvedRequests(allApproved.size())
                .rejectedRequests(allRejected.size())
                .totalRequests(allPending.size() + allApproved.size() + allRejected.size())
                .build();
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Bulk approve multiple time-off requests
     */
    @PutMapping("/bulk-approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<BulkOperationResult> bulkApproveTimeOffRequests(
            @Valid @RequestBody BulkApprovalRequest request) {

        // Validate bulk operation size
        if (request.getRequestIds().size() > 50) {
            throw new IllegalArgumentException("Cannot process more than 50 requests at once");
        }
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Integer requestId : request.getRequestIds()) {
            try {
                TimeOffApprovalRequest approvalRequest = TimeOffApprovalRequest.builder()
                        .requestId(requestId)
                        .managerComments(request.getManagerComments())
                        .build();
                
                timeOffService.approveTimeOffRequest(approvalRequest);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to approve time-off request {}: {}", requestId, e.getMessage());
                failureCount++;
            }
        }
        
        BulkOperationResult result = BulkOperationResult.builder()
                .totalProcessed(request.getRequestIds().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .build();
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get time-off summary for multiple operators (manager view)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<Integer, List<TimeOffStatusResponse>>> getTimeOffSummary(
            @RequestParam List<Integer> operatorIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Getting time-off summary for operators {} from {} to {}", operatorIds, startDate, endDate);
        
        // Limit the number of operators to prevent excessive load
        if (operatorIds.size() > 50) {
            throw new IllegalArgumentException("Cannot query more than 50 operators at once");
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        Map<Integer, List<TimeOffStatusResponse>> summary = operatorIds.stream()
                .collect(java.util.stream.Collectors.toMap(
                        operatorId -> operatorId,
                        operatorId -> timeOffService.getOperatorTimeOffRequestsByDateRange(operatorId, startDate, endDate)
                ));
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleValidationException(IllegalArgumentException e) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
    /**
     * Exception handler for general errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error in TimeOffController: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body("An error occurred while processing the request");
    }
    
    // DTOs for bulk operations and statistics
    
    public static class BulkApprovalRequest {
        private List<Integer> requestIds;
        private Integer managerId;
        private String managerComments;
        
        // Getters and setters
        public List<Integer> getRequestIds() { return requestIds; }
        public void setRequestIds(List<Integer> requestIds) { this.requestIds = requestIds; }
        public Integer getManagerId() { return managerId; }
        public void setManagerId(Integer managerId) { this.managerId = managerId; }
        public String getManagerComments() { return managerComments; }
        public void setManagerComments(String managerComments) { this.managerComments = managerComments; }
    }
    
    public static class BulkOperationResult {
        private int totalProcessed;
        private int successCount;
        private int failureCount;
        
        public static BulkOperationResultBuilder builder() {
            return new BulkOperationResultBuilder();
        }
        
        public static class BulkOperationResultBuilder {
            private int totalProcessed;
            private int successCount;
            private int failureCount;
            
            public BulkOperationResultBuilder totalProcessed(int totalProcessed) {
                this.totalProcessed = totalProcessed;
                return this;
            }
            
            public BulkOperationResultBuilder successCount(int successCount) {
                this.successCount = successCount;
                return this;
            }
            
            public BulkOperationResultBuilder failureCount(int failureCount) {
                this.failureCount = failureCount;
                return this;
            }
            
            public BulkOperationResult build() {
                BulkOperationResult result = new BulkOperationResult();
                result.totalProcessed = this.totalProcessed;
                result.successCount = this.successCount;
                result.failureCount = this.failureCount;
                return result;
            }
        }
        
        // Getters
        public int getTotalProcessed() { return totalProcessed; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
    }
    
    public static class TimeOffStatistics {
        private long pendingRequests;
        private long approvedRequests;
        private long rejectedRequests;
        private long totalRequests;
        
        public static TimeOffStatisticsBuilder builder() {
            return new TimeOffStatisticsBuilder();
        }
        
        public static class TimeOffStatisticsBuilder {
            private long pendingRequests;
            private long approvedRequests;
            private long rejectedRequests;
            private long totalRequests;
            
            public TimeOffStatisticsBuilder pendingRequests(long pendingRequests) {
                this.pendingRequests = pendingRequests;
                return this;
            }
            
            public TimeOffStatisticsBuilder approvedRequests(long approvedRequests) {
                this.approvedRequests = approvedRequests;
                return this;
            }
            
            public TimeOffStatisticsBuilder rejectedRequests(long rejectedRequests) {
                this.rejectedRequests = rejectedRequests;
                return this;
            }
            
            public TimeOffStatisticsBuilder totalRequests(long totalRequests) {
                this.totalRequests = totalRequests;
                return this;
            }
            
            public TimeOffStatistics build() {
                TimeOffStatistics stats = new TimeOffStatistics();
                stats.pendingRequests = this.pendingRequests;
                stats.approvedRequests = this.approvedRequests;
                stats.rejectedRequests = this.rejectedRequests;
                stats.totalRequests = this.totalRequests;
                return stats;
            }
        }
        
        // Getters
        public long getPendingRequests() { return pendingRequests; }
        public long getApprovedRequests() { return approvedRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
        public long getTotalRequests() { return totalRequests; }
    }
}