package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.entity.Customer;

public interface NotificationEventService {
    
    /**
     * Tạo notification khi booking status thay đổi
     */
    void createBookingStatusNotification(Customer customer, String bookingId, String oldStatus, String newStatus);
    
    /**
     * Tạo notification khi có promotion mới
     */
    void createPromotionNotification(Customer customer, String promotionTitle, String promotionDescription);
    
    /**
     * Tạo notification khi feedback được tạo
     */
    void createFeedbackNotification(Customer customer, String feedbackId, String feedbackType);
    
    /**
     * Tạo notification khi booking bị xóa
     */
    void createBookingDeletedNotification(Customer customer, String bookingId);
    
    /**
     * Tạo notification khi thông tin user thay đổi
     */
    void createUserInfoChangedNotification(Customer customer, String changedField);
    
    /**
     * Tạo notification tùy chỉnh
     */
    void createCustomNotification(Customer customer, String title, String message, String type);
} 