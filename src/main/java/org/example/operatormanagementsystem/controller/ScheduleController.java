package org.example.operatormanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.dto.response.ScheduleCalendarResponse;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.managercustomer.service.UserService;
import org.example.operatormanagementsystem.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    private final UserService userService;

    // Helper to get current operatorId from authentication
    private Integer getCurrentOperatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserSearchResponse operator = userService.findUserResponseByEmail(email);
        return operator.getId();
    }
    
    /**
     * Get calendar data for the authenticated operator and date
     */
    @GetMapping("/calendar")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<ScheduleCalendarResponse> getCalendarData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Integer operatorId = getCurrentOperatorId();
        log.debug("Getting calendar data for operator {} on date {}", operatorId, date);
        ScheduleCalendarResponse response = scheduleService.getCalendarData(operatorId, date);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get calendar data for the authenticated operator within a date range
     */
    @GetMapping("/calendar/range")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<ScheduleCalendarResponse>> getCalendarDataRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Integer operatorId = getCurrentOperatorId();
        log.debug("Getting calendar data range for operator {} from {} to {}", operatorId, startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (startDate.plusDays(90).isBefore(endDate)) {
            throw new IllegalArgumentException("Date range cannot exceed 90 days");
        }
        List<ScheduleCalendarResponse> response = scheduleService.getCalendarDataRange(operatorId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get weekly schedule for the authenticated operator
     */
    @GetMapping("/weekly")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<ScheduleCalendarResponse>> getWeeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        Integer operatorId = getCurrentOperatorId();
        log.debug("Getting weekly schedule for operator {} starting from {}", operatorId, weekStartDate);
        List<ScheduleCalendarResponse> response = scheduleService.getWeeklySchedule(operatorId, weekStartDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get monthly schedule for the authenticated operator
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<ScheduleCalendarResponse>> getMonthlySchedule(
            @RequestParam int year,
            @RequestParam int month) {
        Integer operatorId = getCurrentOperatorId();
        log.debug("Getting monthly schedule for operator {} for {}/{}", operatorId, month, year);
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 2020 || year > 2030) {
            throw new IllegalArgumentException("Year must be between 2020 and 2030");
        }
        List<ScheduleCalendarResponse> response = scheduleService.getMonthlySchedule(operatorId, year, month);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get assigned orders for the authenticated operator within date range
     */
    @GetMapping("/orders")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<List<ScheduleCalendarResponse.OrderSummary>> getAssignedOrders(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Integer operatorId = getCurrentOperatorId();
        log.debug("Getting assigned orders for operator {} from {} to {}", operatorId, startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        List<ScheduleCalendarResponse.OrderSummary> response = scheduleService.getAssignedOrders(operatorId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current day schedule for the authenticated operator
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
    public ResponseEntity<ScheduleCalendarResponse> getTodaySchedule() {
        Integer operatorId = getCurrentOperatorId();
        log.debug("Getting today's schedule for operator {}", operatorId);
        LocalDate today = LocalDate.now();
        ScheduleCalendarResponse response = scheduleService.getCalendarData(operatorId, today);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get schedule summary for multiple operators (manager view)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ScheduleCalendarResponse>> getScheduleSummary(
            @RequestParam List<Integer> operatorIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.debug("Getting schedule summary for operators {} on date {}", operatorIds, date);
        
        // Limit the number of operators to prevent excessive load
        if (operatorIds.size() > 50) {
            throw new IllegalArgumentException("Cannot query more than 50 operators at once");
        }
        
        List<ScheduleCalendarResponse> responses = operatorIds.stream()
                .map(operatorId -> scheduleService.getCalendarData(operatorId, date))
                .toList();
        
        return ResponseEntity.ok(responses);
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
        log.error("Runtime error in ScheduleController: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body("An error occurred while processing the request");
    }
}