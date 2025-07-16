package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.CreateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.StorageSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.TransportSummaryResponse;

import java.util.List;

public interface CustomerFeedbackService {
    
    FeedbackResponse createFeedback(CreateFeedbackRequest request, Integer customerId);
    
    FeedbackResponse updateFeedback(Integer feedbackId, UpdateFeedbackRequest request, Integer customerId);
    
    void deleteFeedback(Integer feedbackId, Integer customerId);
    
    FeedbackResponse likeFeedback(Integer feedbackId, Integer customerId);
    
    FeedbackResponse dislikeFeedback(Integer feedbackId, Integer customerId);
    
    List<StorageSummaryResponse> getAllStorageWithFeedbacks();
    
    List<TransportSummaryResponse> getAllTransportWithFeedbacks();

    List<FeedbackResponse> getAllFeedbacksByCustomerId(Integer customerId);
} 