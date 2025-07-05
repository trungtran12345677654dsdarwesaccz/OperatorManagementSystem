package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.CreateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;

import java.util.List;

public interface CustomerFeedbackService {
    
    FeedbackResponse createFeedback(CreateFeedbackRequest request, Integer customerId);
    
    FeedbackResponse updateFeedback(Integer feedbackId, UpdateFeedbackRequest request, Integer customerId);
    
    void deleteFeedback(Integer feedbackId, Integer customerId);
    
    List<FeedbackResponse> getAllFeedbacksByCustomer(Integer customerId);
    
    List<FeedbackResponse> getFeedbacksByBooking(Integer bookingId, Integer customerId);
    
    FeedbackResponse getFeedbackById(Integer feedbackId, Integer customerId);
} 