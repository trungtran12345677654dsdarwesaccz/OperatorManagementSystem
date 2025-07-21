package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.dto.response.ScheduleCalendarResponse;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    
    /**
     * Get calendar data for a specific operator and date
     */
    ScheduleCalendarResponse getCalendarData(Integer operatorId, LocalDate date);
    
    /**
     * Get calendar data for a specific operator within a date range
     */
    List<ScheduleCalendarResponse> getCalendarDataRange(Integer operatorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get weekly schedule for an operator
     */
    List<ScheduleCalendarResponse> getWeeklySchedule(Integer operatorId, LocalDate weekStartDate);
    
    /**
     * Get monthly schedule for an operator
     */
    List<ScheduleCalendarResponse> getMonthlySchedule(Integer operatorId, int year, int month);
    
    /**
     * Get assigned orders for an operator within date range
     */
    List<ScheduleCalendarResponse.OrderSummary> getAssignedOrders(Integer operatorId, LocalDate startDate, LocalDate endDate);
}