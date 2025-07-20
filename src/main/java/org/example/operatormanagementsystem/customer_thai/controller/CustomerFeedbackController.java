package org.example.operatormanagementsystem.customer_thai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.StorageSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.TransportSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.service.CustomerFeedbackService;
import org.example.operatormanagementsystem.customer_thai.service.CustomerInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateStorageFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateTransportFeedbackRequest;

@RestController
@RequestMapping("/api/customer/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerFeedbackController {

    private final Logger logger = LoggerFactory.getLogger(CustomerFeedbackController.class);
    private final CustomerFeedbackService feedbackService;
    private final CustomerInfoService customerInfoService;

    // TẠO FEEDBACK CHO TRANSPORT
    @PostMapping("/transport")
    public ResponseEntity<FeedbackResponse> createTransportFeedback(@RequestBody CreateTransportFeedbackRequest request) {
        try {
            logger.info("[FEEDBACK] Received createTransportFeedback request: {}", request);
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID retrieved: {}", customerId);
            FeedbackResponse response = feedbackService.createFeedbackTransport(request, customerId);
            logger.info("[FEEDBACK] Transport feedback created successfully with ID: {}", response.getFeedbackId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in createTransportFeedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    // TẠO FEEDBACK CHO STORAGE
    @PostMapping("/storage")
    public ResponseEntity<FeedbackResponse> createStorageFeedback(@RequestBody CreateStorageFeedbackRequest request) {
        try {
            logger.info("[FEEDBACK] Received createStorageFeedback request: {}", request);
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID retrieved: {}", customerId);
            FeedbackResponse response = feedbackService.createFeedbackStorage(request, customerId);
            logger.info("[FEEDBACK] Storage feedback created successfully with ID: {}", response.getFeedbackId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in createStorageFeedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Integer feedbackId,
            @Valid @RequestBody UpdateFeedbackRequest request) {
        try {
            logger.info("[FEEDBACK] Received updateFeedback request for feedbackId: {}", feedbackId);
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID for update: {}", customerId);
            
            FeedbackResponse response = feedbackService.updateFeedback(feedbackId, request, customerId);
            logger.info("[FEEDBACK] Feedback updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in updateFeedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer feedbackId) {
        try {
            logger.info("[FEEDBACK] Received deleteFeedback request for feedbackId: {}", feedbackId);
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID for delete: {}", customerId);
            
            feedbackService.deleteFeedback(feedbackId, customerId);
            logger.info("[FEEDBACK] Feedback deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in deleteFeedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/{feedbackId}/like")
    public ResponseEntity<FeedbackResponse> likeFeedback(@PathVariable Integer feedbackId) {
        try {
            logger.info("[FEEDBACK] Received likeFeedback request for feedbackId: {}", feedbackId);
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID for like: {}", customerId);
            
            FeedbackResponse response = feedbackService.likeFeedback(feedbackId, customerId);
            logger.info("[FEEDBACK] Feedback liked successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in likeFeedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/{feedbackId}/dislike")
    public ResponseEntity<FeedbackResponse> dislikeFeedback(@PathVariable Integer feedbackId) {
        try {
            logger.info("[FEEDBACK] Received dislikeFeedback request for feedbackId: {}", feedbackId);
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID for dislike: {}", customerId);
            
            FeedbackResponse response = feedbackService.dislikeFeedback(feedbackId, customerId);
            logger.info("[FEEDBACK] Feedback disliked successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in dislikeFeedback: {}", e.getMessage(), e);
            throw e;
        }
    }

    // API mới: Lấy tất cả storage units với feedback
    @GetMapping("/storage-units")
    public ResponseEntity<List<StorageSummaryResponse>> getAllStorageWithFeedbacks() {
        try {
            logger.info("[FEEDBACK] Received getAllStorageWithFeedbacks request");
            List<StorageSummaryResponse> storageUnits = feedbackService.getAllStorageWithFeedbacks();
            logger.info("[FEEDBACK] Retrieved {} storage units with feedbacks", storageUnits.size());
            return ResponseEntity.ok(storageUnits);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in getAllStorageWithFeedbacks: {}", e.getMessage(), e);
            throw e;
        }
    }

    // API mới: Lấy tất cả transport units với feedback
    @GetMapping("/transport-units")
    public ResponseEntity<List<TransportSummaryResponse>> getAllTransportWithFeedbacks() {
        try {
            logger.info("[FEEDBACK] Received getAllTransportWithFeedbacks request");
            List<TransportSummaryResponse> transportUnits = feedbackService.getAllTransportWithFeedbacks();
            logger.info("[FEEDBACK] Retrieved {} transport units with feedbacks", transportUnits.size());
            return ResponseEntity.ok(transportUnits);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in getAllTransportWithFeedbacks: {}", e.getMessage(), e);
            throw e;
        }
    }

    // API mới: Lấy tất cả feedback của customer đang đăng nhập
    @GetMapping("/myfeedbacks")
    public ResponseEntity<List<FeedbackResponse>> getMyFeedbacks() {
        try {
            logger.info("[FEEDBACK] Received getMyFeedbacks request");
            Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
            logger.info("[FEEDBACK] Customer ID for myfeedbacks: {}", customerId);
            
            List<FeedbackResponse> feedbacks = feedbackService.getAllFeedbacksByCustomerId(customerId);
            logger.info("[FEEDBACK] Retrieved {} feedbacks", feedbacks.size());
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            logger.error("[FEEDBACK] Error in getMyFeedbacks: {}", e.getMessage(), e);
            throw e;
        }
    }
} 