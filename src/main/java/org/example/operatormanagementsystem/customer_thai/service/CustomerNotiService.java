package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.CreateNotificationRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.CustomerNotiResponse;
import java.util.List;

public interface CustomerNotiService {
    List<CustomerNotiResponse> getMyNotifications();
    void markAsRead(Long notificationId);
    void createNotification(CreateNotificationRequest request);
} 