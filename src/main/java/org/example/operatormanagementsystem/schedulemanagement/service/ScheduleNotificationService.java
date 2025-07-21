package org.example.operatormanagementsystem.schedulemanagement.service;

import org.example.operatormanagementsystem.entity.ShiftAssignment;
import org.example.operatormanagementsystem.entity.TimeOffRequest;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.entity.WorkShift;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;

/**
 * Service interface for handling schedule-related notifications
 * Integrates with existing notification system to send notifications for:
 * - Shift assignments and modifications
 * - Time-off request status changes
 */
public interface ScheduleNotificationService {
    
    /**
     * Send notification when a shift is assigned to an operator
     * Requirements: 5.1
     */
    void notifyShiftAssignment(Users operator, ShiftAssignment shiftAssignment);
    
    /**
     * Send notification when a shift assignment is modified
     * Requirements: 5.2
     */
    void notifyShiftModification(Users operator, ShiftAssignment oldAssignment, ShiftAssignment newAssignment);
    
    /**
     * Send notification when a shift is cancelled/removed
     * Requirements: 5.2
     */
    void notifyShiftCancellation(Users operator, ShiftAssignment cancelledAssignment);
    
    /**
     * Send notification when time-off request status changes
     * Requirements: 5.3
     */
    void notifyTimeOffStatusChange(TimeOffRequest timeOffRequest, TimeOffStatus oldStatus, TimeOffStatus newStatus);
    
    /**
     * Send notification when a new time-off request is submitted (to managers)
     * Requirements: 5.3
     */
    void notifyNewTimeOffRequest(TimeOffRequest timeOffRequest);
    
    /**
     * Get count of unread schedule notifications for an operator
     * Requirements: 5.5
     */
    int getUnreadScheduleNotificationCount(Integer operatorId);
    
    /**
     * Mark schedule notifications as read for an operator
     * Requirements: 5.4
     */
    void markScheduleNotificationsAsRead(Integer operatorId);
}