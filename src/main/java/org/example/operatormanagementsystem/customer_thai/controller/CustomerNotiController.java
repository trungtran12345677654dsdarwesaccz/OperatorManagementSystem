package org.example.operatormanagementsystem.customer_thai.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.response.CustomerNotiResponse;
import org.example.operatormanagementsystem.customer_thai.service.CustomerNotiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/notifications")
@RequiredArgsConstructor
public class CustomerNotiController {
    private final CustomerNotiService customerNotiService;

    @GetMapping
    public ResponseEntity<List<CustomerNotiResponse>> getMyNotifications() {
        return ResponseEntity.ok(customerNotiService.getMyNotifications());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        customerNotiService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
} 