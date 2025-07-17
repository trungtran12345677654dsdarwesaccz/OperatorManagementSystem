package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateStorageFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateTransportFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.StorageSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.TransportSummaryResponse;

import java.util.List;

public interface CustomerFeedbackService {
    
    FeedbackResponse createFeedbackStorage(CreateStorageFeedbackRequest request, Integer customerId);
    FeedbackResponse createFeedbackTransport(CreateTransportFeedbackRequest request, Integer customerId);
    
    FeedbackResponse updateFeedback(Integer feedbackId, UpdateFeedbackRequest request, Integer customerId);
    
    void deleteFeedback(Integer feedbackId, Integer customerId);
    
    FeedbackResponse likeFeedback(Integer feedbackId, Integer customerId);
    
    FeedbackResponse dislikeFeedback(Integer feedbackId, Integer customerId);
    
    List<StorageSummaryResponse> getAllStorageWithFeedbacks();
    
    List<TransportSummaryResponse> getAllTransportWithFeedbacks();

    List<FeedbackResponse> getAllFeedbacksByCustomerId(Integer customerId);
} 