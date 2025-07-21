package org.example.operatormanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.dto.request.CreateShiftRequest;
import org.example.operatormanagementsystem.dto.request.ShiftAssignmentRequest;
import org.example.operatormanagementsystem.dto.response.ShiftDetailsResponse;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.service.ShiftService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Slf4j
public class ShiftController {
    
    private final ShiftService shiftService;
    
    /**
     * Create a new work shift
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ShiftDetailsResponse> createShift(@Valid @RequestBody CreateShiftRequest request) {
        log.debug("Creating new shift: {}", request.getShiftName());
        
        ShiftDetailsResponse response = shiftService.createShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update an existing work shift
     */
    @PutMapping("/{shiftId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ShiftDetailsResponse> updateShift(
            @PathVariable Integer shiftId,
            @Valid @RequestBody CreateShiftRequest request) {
        
        log.debug("Updating shift ID: {}", shiftId);
        
        ShiftDetailsResponse response = shiftService.updateShift(shiftId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get shift details by ID
     */
    @GetMapping("/{shiftId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('MANAGER')")
    public ResponseEntity<ShiftDetailsResponse> getShiftById(@PathVariable Integer shiftId) {
        log.debug("Getting shift by ID: {}", shiftId);
        
        ShiftDetailsResponse response = shiftService.getShiftById(shiftId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all active shifts
     */
    @GetMapping
    @PreAuthorize("hasRole('OPERATOR') or hasRole('MANAGER')")
    public ResponseEntity<List<ShiftDetailsResponse>> getAllActiveShifts() {
        log.debug("Getting all active shifts");
        
        List<ShiftDetailsResponse> response = shiftService.getAllActiveShifts();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all shifts (active and inactive) - Manager only
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ShiftDetailsResponse>> getAllShifts() {
        log.debug("Getting all shifts");
        
        List<ShiftDetailsResponse> response = shiftService.getAllShifts();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a shift (soft delete - set inactive)
     */
    @DeleteMapping("/{shiftId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteShift(@PathVariable Integer shiftId) {
        log.debug("Deleting shift ID: {}", shiftId);
        
        shiftService.deleteShift(shiftId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Assign operators to a shift for a specific date
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> assignOperatorsToShift(@Valid @RequestBody ShiftAssignmentRequest request) {

        // Validate assignment date is not in the past
        if (request.getAssignmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot assign shifts for past dates");
        }
        
        // Validate operator list size
        if (request.getOperatorIds().size() > 20) {
            throw new IllegalArgumentException("Cannot assign more than 20 operators to a single shift");
        }
        
        shiftService.assignOperatorsToShift(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove operator assignment from a shift
     */
    @DeleteMapping("/{shiftId}/assignments")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeOperatorAssignment(
            @PathVariable Integer shiftId,
            @RequestParam Integer operatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignmentDate) {

        shiftService.removeOperatorAssignment(shiftId, operatorId, assignmentDate);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get shift assignments for a specific operator
     */
    @GetMapping("/assignments/{operatorId}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('MANAGER')")
    public ResponseEntity<List<ShiftDetailsResponse>> getOperatorShiftAssignments(
            @PathVariable Integer operatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting shift assignments for operator {} from {} to {}", 
                 operatorId, startDate, endDate);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        // Limit the range to prevent excessive data
        if (startDate.plusDays(90).isBefore(endDate)) {
            throw new IllegalArgumentException("Date range cannot exceed 90 days");
        }
        
        List<ShiftDetailsResponse> response = shiftService.getOperatorShiftAssignments(
                operatorId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check for shift time conflicts for an operator
     */
    @GetMapping("/conflicts/check")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Boolean> checkTimeConflict(
            @RequestParam Integer shiftId,
            @RequestParam Integer operatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignmentDate) {
        
        log.debug("Checking time conflict for operator {} on shift {} for date {}", 
                 operatorId, shiftId, assignmentDate);
        
        boolean hasConflict = shiftService.hasTimeConflict(shiftId, operatorId, assignmentDate);
        return ResponseEntity.ok(hasConflict);
    }
    
    /**
     * Get available operators for a shift assignment
     */
    @GetMapping("/{shiftId}/available-operators")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserSearchResponse>> getAvailableOperators(
            @PathVariable Integer shiftId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate assignmentDate) {
        
        
        List<UserSearchResponse> availableOperators = shiftService.getAvailableOperators(shiftId, assignmentDate);
        return ResponseEntity.ok(availableOperators);
    }
    
    /**
     * Bulk assign multiple shifts to operators
     */
    @PostMapping("/bulk-assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> bulkAssignShifts(@Valid @RequestBody List<ShiftAssignmentRequest> requests) {
        log.debug("Bulk assigning {} shift assignments", requests.size());
        
        // Validate bulk assignment size
        if (requests.size() > 100) {
            throw new IllegalArgumentException("Cannot process more than 100 assignments at once");
        }
        
        // Validate each request
        for (ShiftAssignmentRequest request : requests) {
            if (request.getAssignmentDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Cannot assign shifts for past dates");
            }
            if (request.getOperatorIds().size() > 20) {
                throw new IllegalArgumentException("Cannot assign more than 20 operators to a single shift");
            }
        }
        
        // Process each assignment
        for (ShiftAssignmentRequest request : requests) {
            try {
                shiftService.assignOperatorsToShift(request);
            } catch (Exception e) {
                log.error("Failed to assign shift {} for date {}: {}", 
                         request.getShiftId(), request.getAssignmentDate(), e.getMessage());
                // Continue with other assignments but log the error
            }
        }
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get shift statistics (Manager only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ShiftStatistics> getShiftStatistics() {
        log.debug("Getting shift statistics");
        
        List<ShiftDetailsResponse> allShifts = shiftService.getAllShifts();
        List<ShiftDetailsResponse> activeShifts = shiftService.getAllActiveShifts();
        
        ShiftStatistics stats = ShiftStatistics.builder()
                .totalShifts(allShifts.size())
                .activeShifts(activeShifts.size())
                .inactiveShifts(allShifts.size() - activeShifts.size())
                .build();
        
        return ResponseEntity.ok(stats);
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
        log.error("Runtime error in ShiftController: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body("An error occurred while processing the request");
    }
    
    /**
     * Statistics DTO for shift statistics endpoint
     */
    public static class ShiftStatistics {
        private int totalShifts;
        private int activeShifts;
        private int inactiveShifts;
        
        public static ShiftStatisticsBuilder builder() {
            return new ShiftStatisticsBuilder();
        }
        
        public static class ShiftStatisticsBuilder {
            private int totalShifts;
            private int activeShifts;
            private int inactiveShifts;
            
            public ShiftStatisticsBuilder totalShifts(int totalShifts) {
                this.totalShifts = totalShifts;
                return this;
            }
            
            public ShiftStatisticsBuilder activeShifts(int activeShifts) {
                this.activeShifts = activeShifts;
                return this;
            }
            
            public ShiftStatisticsBuilder inactiveShifts(int inactiveShifts) {
                this.inactiveShifts = inactiveShifts;
                return this;
            }
            
            public ShiftStatistics build() {
                ShiftStatistics stats = new ShiftStatistics();
                stats.totalShifts = this.totalShifts;
                stats.activeShifts = this.activeShifts;
                stats.inactiveShifts = this.inactiveShifts;
                return stats;
            }
        }
        
        // Getters
        public int getTotalShifts() { return totalShifts; }
        public int getActiveShifts() { return activeShifts; }
        public int getInactiveShifts() { return inactiveShifts; }
    }
}