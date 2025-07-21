package org.example.operatormanagementsystem.schedulemanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateNotificationRequest;
import org.example.operatormanagementsystem.customer_thai.service.CustomerNotiService;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.TimeOffStatus;
import org.example.operatormanagementsystem.managestaff_yen.repository.OperatorStaffRepository;
import org.example.operatormanagementsystem.repository.NotificationRepository;
import org.example.operatormanagementsystem.schedulemanagement.service.ScheduleNotificationService;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleNotificationServiceImpl implements ScheduleNotificationService {

    private final CustomerNotiService customerNotiService;
    private final NotificationRepository notificationRepository;
    private final ManagerRepository managerRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void notifyShiftAssignment(Users operator, ShiftAssignment shiftAssignment) {
        try {
            String title = "Phân công ca làm việc mới";
            String message = String.format(
                    "Bạn đã được phân công ca làm việc '%s' vào ngày %s (%s - %s). Vui lòng kiểm tra lịch làm việc của bạn.",
                    shiftAssignment.getWorkShift().getShiftName(),
                    shiftAssignment.getAssignmentDate().format(DATE_FORMATTER),
                    shiftAssignment.getWorkShift().getStartTime().format(TIME_FORMATTER),
                    shiftAssignment.getWorkShift().getEndTime().format(TIME_FORMATTER)
            );

            createOperatorNotification(operator, title, message, "SHIFT_ASSIGNMENT");

            log.info("Shift assignment notification sent to operator {}: {}",
                    operator.getId(), shiftAssignment.getAssignmentId());

        } catch (Exception e) {
            log.error("Failed to send shift assignment notification to operator {}: {}",
                    operator.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void notifyShiftModification(Users operator, ShiftAssignment oldAssignment, ShiftAssignment newAssignment) {
        try {
            String title = "Thay đổi ca làm việc";
            String message = String.format(
                    "Ca làm việc của bạn đã được thay đổi:\n" +
                            "Trước: %s vào ngày %s (%s - %s)\n" +
                            "Sau: %s vào ngày %s (%s - %s)",
                    oldAssignment.getWorkShift().getShiftName(),
                    oldAssignment.getAssignmentDate().format(DATE_FORMATTER),
                    oldAssignment.getWorkShift().getStartTime().format(TIME_FORMATTER),
                    oldAssignment.getWorkShift().getEndTime().format(TIME_FORMATTER),
                    newAssignment.getWorkShift().getShiftName(),
                    newAssignment.getAssignmentDate().format(DATE_FORMATTER),
                    newAssignment.getWorkShift().getStartTime().format(TIME_FORMATTER),
                    newAssignment.getWorkShift().getEndTime().format(TIME_FORMATTER)
            );

            createOperatorNotification(operator, title, message, "SHIFT_MODIFICATION");

            log.info("Shift modification notification sent to operator {}: {} -> {}",
                    operator.getId(), oldAssignment.getAssignmentId(), newAssignment.getAssignmentId());

        } catch (Exception e) {
            log.error("Failed to send shift modification notification to operator {}: {}",
                    operator.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void notifyShiftCancellation(Users operator, ShiftAssignment cancelledAssignment) {
        try {
            String title = "Hủy ca làm việc";
            String message = String.format(
                    "Ca làm việc '%s' vào ngày %s (%s - %s) đã được hủy. Vui lòng kiểm tra lịch làm việc cập nhật.",
                    cancelledAssignment.getWorkShift().getShiftName(),
                    cancelledAssignment.getAssignmentDate().format(DATE_FORMATTER),
                    cancelledAssignment.getWorkShift().getStartTime().format(TIME_FORMATTER),
                    cancelledAssignment.getWorkShift().getEndTime().format(TIME_FORMATTER)
            );

            createOperatorNotification(operator, title, message, "SHIFT_CANCELLATION");

            log.info("Shift cancellation notification sent to operator {}: {}",
                    operator.getId(), cancelledAssignment.getAssignmentId());

        } catch (Exception e) {
            log.error("Failed to send shift cancellation notification to operator {}: {}",
                    operator.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void notifyTimeOffStatusChange(TimeOffRequest timeOffRequest, TimeOffStatus oldStatus, TimeOffStatus newStatus) {
        try {
            String title = getTimeOffStatusTitle(newStatus);
            String message = String.format(
                    "Yêu cầu nghỉ phép từ ngày %s đến %s đã được %s.\n%s",
                    timeOffRequest.getStartDate().format(DATE_FORMATTER),
                    timeOffRequest.getEndDate().format(DATE_FORMATTER),
                    getStatusText(newStatus),
                    timeOffRequest.getManagerComments() != null ?
                            "Ghi chú từ quản lý: " + timeOffRequest.getManagerComments() : ""
            );

            createOperatorNotification(timeOffRequest.getOperator(), title, message, "TIME_OFF_STATUS");

            log.info("Time-off status change notification sent to operator {}: {} -> {}",
                    timeOffRequest.getOperator().getId(), oldStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to send time-off status notification to operator {}: {}",
                    timeOffRequest.getOperator().getId(), e.getMessage(), e);
        }
    }

    @Override
    public void notifyNewTimeOffRequest(TimeOffRequest timeOffRequest) {
        try {
            // Notify all managers about new time-off request
            List<Manager> managers = managerRepository.findAll();

            String title = "Yêu cầu nghỉ phép mới";
            String message = String.format(
                    "Nhân viên %s đã gửi yêu cầu nghỉ phép từ ngày %s đến %s.\nLý do: %s\nVui lòng xem xét và phê duyệt.",
                    timeOffRequest.getOperator().getFullName(),
                    timeOffRequest.getStartDate().format(DATE_FORMATTER),
                    timeOffRequest.getEndDate().format(DATE_FORMATTER),
                    timeOffRequest.getReason()
            );

            for (Manager manager : managers) {
                createManagerNotification(manager, title, message, "NEW_TIME_OFF_REQUEST");
            }

            log.info("New time-off request notification sent to {} managers for request {}",
                    managers.size(), timeOffRequest.getRequestId());

        } catch (Exception e) {
            log.error("Failed to send new time-off request notification for request {}: {}",
                    timeOffRequest.getRequestId(), e.getMessage(), e);
        }
    }

    @Override
    public int getUnreadScheduleNotificationCount(Integer operatorId) {
        try {
            // Count unread notifications for schedule-related types
            return notificationRepository.countUnreadScheduleNotifications(operatorId);
        } catch (Exception e) {
            log.error("Failed to get unread notification count for operator {}: {}", operatorId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public void markScheduleNotificationsAsRead(Integer operatorId) {
        try {
            notificationRepository.markScheduleNotificationsAsRead(operatorId);
            log.info("Marked schedule notifications as read for operator {}", operatorId);
        } catch (Exception e) {
            log.error("Failed to mark notifications as read for operator {}: {}", operatorId, e.getMessage(), e);
        }
    }

    private void createOperatorNotification(Users operator, String title, String message, String type) {
        // Check if operator has associated customer account for notification system
        if (operator.getCustomer() != null) {
            CreateNotificationRequest request = new CreateNotificationRequest(
                    operator.getCustomer().getCustomerId(),
                    title,
                    message,
                    type,
                    false,
                    LocalDateTime.now()
            );

            customerNotiService.createNotification(request);
        } else {
            // Fallback: create direct notification record
            createDirectNotification(operator.getEmail(), title, message);
        }
    }

    private void createManagerNotification(Manager manager, String title, String message, String type) {
        // Check if manager has associated customer account for notification system
        if (manager.getUsers() != null && manager.getUsers().getCustomer() != null) {
            CreateNotificationRequest request = new CreateNotificationRequest(
                    manager.getUsers().getCustomer().getCustomerId(),
                    title,
                    message,
                    type,
                    false,
                    LocalDateTime.now()
            );

            customerNotiService.createNotification(request);
        } else {
            // Fallback: create direct notification record
            createDirectNotification(manager.getUsers().getEmail(), title, message);
        }
    }

    private void createDirectNotification(String email, String title, String message) {
        try {
            Notification notification = Notification.builder()
                    .sentTo(email)
                    .subject(title)
                    .content(message)
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to create direct notification for {}: {}", email, e.getMessage(), e);
        }
    }

    private String getTimeOffStatusTitle(TimeOffStatus status) {
        return switch (status) {
            case APPROVED -> "Yêu cầu nghỉ phép được duyệt";
            case REJECTED -> "Yêu cầu nghỉ phép bị từ chối";
            case PENDING -> "Yêu cầu nghỉ phép đang chờ xử lý";
            default -> "Cập nhật yêu cầu nghỉ phép";
        };
    }

    private String getStatusText(TimeOffStatus status) {
        return switch (status) {
            case APPROVED -> "chấp thuận";
            case REJECTED -> "từ chối";
            case PENDING -> "đang chờ xử lý";
            default -> "cập nhật";
        };
    }
}