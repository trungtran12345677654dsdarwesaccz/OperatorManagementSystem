package org.example.operatormanagementsystem.customer_thai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
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

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks() {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        List<FeedbackResponse> feedbacks = feedbackService.getAllFeedbacksByCustomer(customerId);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByBooking(@PathVariable Integer bookingId) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByBooking(bookingId, customerId);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Integer feedbackId) {
        Integer customerId = customerInfoService.getCurrentCustomerUser().getCustomer().getCustomerId();
        FeedbackResponse feedback = feedbackService.getFeedbackById(feedbackId, customerId);
        return ResponseEntity.ok(feedback);
    }
} 