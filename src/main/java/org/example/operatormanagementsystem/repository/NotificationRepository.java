package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    /**
     * Count unread schedule-related notifications for an operator
     * Schedule notification types: SHIFT_ASSIGNMENT, SHIFT_MODIFICATION, SHIFT_CANCELLATION, TIME_OFF_STATUS
     */
    @Query("""
        SELECT COUNT(n) FROM Notification n 
        JOIN OperatorStaff os ON os.users.email = n.sentTo 
        WHERE os.operatorId = :operatorId 
        AND n.subject LIKE '%ca làm việc%' OR n.subject LIKE '%nghỉ phép%'
        """)
    int countUnreadScheduleNotifications(@Param("operatorId") Integer operatorId);
    
    /**
     * Mark schedule-related notifications as read for an operator
     * This is a placeholder method - actual implementation would depend on 
     * having an 'isRead' field in the Notification entity
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n SET n.sentAt = n.sentAt 
        WHERE n.sentTo IN (
            SELECT os.users.email FROM OperatorStaff os WHERE os.operatorId = :operatorId
        )
        AND (n.subject LIKE '%ca làm việc%' OR n.subject LIKE '%nghỉ phép%')
        """)
    void markScheduleNotificationsAsRead(@Param("operatorId") Integer operatorId);
}