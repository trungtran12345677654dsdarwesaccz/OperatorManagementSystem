package org.example.operatormanagementsystem.customer_thai.service.impl;

import org.example.operatormanagementsystem.customer_thai.dto.request.CreateNotificationRequest;
import org.example.operatormanagementsystem.customer_thai.service.CustomerNotiService;
import org.example.operatormanagementsystem.customer_thai.service.NotificationEventService;
import org.example.operatormanagementsystem.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationEventServiceImpl implements NotificationEventService {
    
    @Autowired
    private CustomerNotiService customerNotiService;
    
    @Override
    public void createBookingStatusNotification(Customer customer, String bookingId, String oldStatus, String newStatus) {
        String title = "Cập nhật trạng thái Đơn hàng";
        String message = String.format("Đơn hàng #%s đã được khởi tạo và đang ở trạng thái '%s' vui lòng chờ phản hồi từ hệ thống",
                bookingId, oldStatus, newStatus);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType("BOOKING_STATUS");
        request.setIsRead(false);
        request.setCreatedAt(LocalDateTime.now());
        
        customerNotiService.createNotification(request);
    }
    
    @Override
    public void createPromotionNotification(Customer customer, String promotionTitle, String promotionDescription) {
        String title = "Khuyến mãi mới";
        String message = String.format("Bạn vừa có được khuyến mãi mới: %s - %s", promotionTitle, promotionDescription);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType("PROMOTION");
        request.setIsRead(false);
        request.setCreatedAt(LocalDateTime.now());
        
        customerNotiService.createNotification(request);
    }
    
    @Override
    public void createFeedbackNotification(Customer customer, String feedbackId, String feedbackType) {
        String title = "Khiếu nại đã được tạo";
        String message = String.format("Khiếu nại #%s loại '%s' đã được tạo thành công", feedbackId, feedbackType);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType("FEEDBACK");
        request.setIsRead(false);
        request.setCreatedAt(LocalDateTime.now());
        
        customerNotiService.createNotification(request);
    }
    
    @Override
    public void createBookingDeletedNotification(Customer customer, String bookingId) {
        String title = "Đơn hàng đã bị hủy";
        String message = String.format("Đơn hàng #%s đã được hủy thành công", bookingId);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType("BOOKING_DELETED");
        request.setIsRead(false);
        request.setCreatedAt(LocalDateTime.now());
        
        customerNotiService.createNotification(request);
    }
    
    @Override
    public void createUserInfoChangedNotification(Customer customer, String changedField) {
        String title = "Thông tin cá nhân đã được cập nhật";
        String message = String.format("Thông tin '%s' đã được cập nhật thành công", changedField);
        
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType("USER_INFO");
        request.setIsRead(false);
        request.setCreatedAt(LocalDateTime.now());
        
        customerNotiService.createNotification(request);
    }
    
    @Override
    public void createCustomNotification(Customer customer, String title, String message, String type) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setCustomerId(customer.getCustomerId());
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type);
        request.setIsRead(false);
        request.setCreatedAt(LocalDateTime.now());
        
        customerNotiService.createNotification(request);
    }
} 