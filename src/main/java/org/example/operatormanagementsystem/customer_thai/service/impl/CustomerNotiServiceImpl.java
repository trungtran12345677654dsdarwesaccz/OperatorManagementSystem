package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateNotificationRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.CustomerNotiResponse;
import org.example.operatormanagementsystem.customer_thai.repository.CustomerNotiRepository;
import org.example.operatormanagementsystem.customer_thai.service.CustomerInfoService;
import org.example.operatormanagementsystem.customer_thai.service.CustomerNotiService;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.CustomerNoti;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerNotiServiceImpl implements CustomerNotiService {
    private final CustomerNotiRepository customerNotiRepository;
    private final CustomerInfoService customerInfoService;

    @Override
    public List<CustomerNotiResponse> getMyNotifications() {
        Users user = customerInfoService.getCurrentCustomerUser();
        List<CustomerNoti> notis = customerNotiRepository.findByUserOrderByCreatedAtDesc(user);
        return notis.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        CustomerNoti noti = customerNotiRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        noti.setRead(true);
        customerNotiRepository.save(noti);
    }

    @Override
    @Transactional
    public void createNotification(CreateNotificationRequest request) {
        CustomerNoti notification = new CustomerNoti();
        notification.setTitle(request.getTitle());
        notification.setContent(request.getMessage());
        notification.setRead(request.getIsRead());
        notification.setCreatedAt(new java.util.Date());
        
        // Tìm user từ customer ID
        Users user = customerInfoService.getCurrentCustomerUser();
        notification.setUser(user);
        
        customerNotiRepository.save(notification);
    }

    private CustomerNotiResponse toResponse(CustomerNoti noti) {
        CustomerNotiResponse res = new CustomerNotiResponse();
        res.setId(noti.getId());
        res.setTitle(noti.getTitle());
        res.setContent(noti.getContent());
        res.setCreatedAt(noti.getCreatedAt());
        res.setRead(noti.getRead());
        return res;
    }
} 