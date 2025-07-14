package org.example.operatormanagementsystem.customer_thai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.StorageSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.TransportSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.service.CustomerFeedbackService;
import org.example.operatormanagementsystem.customer_thai.service.CustomerInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerFeedbackController {

    private final CustomerFeedbackService feedbackService;
    private final CustomerInfoService customerInfoService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        FeedbackResponse response = feedbackService.createFeedback(request, customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/storage/{storageId}")
    public ResponseEntity<FeedbackResponse> createFeedbackForStorage(@PathVariable Integer storageId, @Valid @RequestBody CreateFeedbackRequest request) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        request.setStorageId(storageId);
        request.setBookingId(null); 
        FeedbackResponse response = feedbackService.createFeedback(request, customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transport/{transportId}")
    public ResponseEntity<FeedbackResponse> createFeedbackForTransport(@PathVariable Integer transportId, @Valid @RequestBody CreateFeedbackRequest request) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        request.setTransportId(transportId);
        request.setBookingId(null); 
        FeedbackResponse response = feedbackService.createFeedback(request, customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Integer feedbackId,
            @Valid @RequestBody UpdateFeedbackRequest request) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        FeedbackResponse response = feedbackService.updateFeedback(feedbackId, request, customerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer feedbackId) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        feedbackService.deleteFeedback(feedbackId, customerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{feedbackId}/like")
    public ResponseEntity<FeedbackResponse> likeFeedback(@PathVariable Integer feedbackId) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        FeedbackResponse response = feedbackService.likeFeedback(feedbackId, customerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{feedbackId}/dislike")
    public ResponseEntity<FeedbackResponse> dislikeFeedback(@PathVariable Integer feedbackId) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        FeedbackResponse response = feedbackService.dislikeFeedback(feedbackId, customerId);
        return ResponseEntity.ok(response);
    }

    // API mới: Lấy tất cả storage units với feedback
    @GetMapping("/storage-units")
    public ResponseEntity<List<StorageSummaryResponse>> getAllStorageWithFeedbacks() {
        List<StorageSummaryResponse> storageUnits = feedbackService.getAllStorageWithFeedbacks();
        return ResponseEntity.ok(storageUnits);
    }

    // API mới: Lấy tất cả transport units với feedback
    @GetMapping("/transport-units")
    public ResponseEntity<List<TransportSummaryResponse>> getAllTransportWithFeedbacks() {
        List<TransportSummaryResponse> transportUnits = feedbackService.getAllTransportWithFeedbacks();
        return ResponseEntity.ok(transportUnits);
    }
} 