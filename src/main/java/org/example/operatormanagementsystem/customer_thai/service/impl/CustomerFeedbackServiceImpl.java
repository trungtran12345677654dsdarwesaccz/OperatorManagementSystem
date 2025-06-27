package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.repository.BookingCustomerRepository;
import org.example.operatormanagementsystem.customer_thai.repository.CustomerFeedbackRepository;
import org.example.operatormanagementsystem.customer_thai.service.CustomerFeedbackService;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerFeedbackServiceImpl implements CustomerFeedbackService {

    private final CustomerFeedbackRepository feedbackRepository;
    private final BookingCustomerRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public FeedbackResponse createFeedback(CreateFeedbackRequest request, Integer customerId) {
        // Validate customer exists by getting user with CUSTOMER role
        Users user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (user.getCustomer() == null) {
            throw new RuntimeException("User is not a customer");
        }

        // Validate booking exists and belongs to customer
        Booking booking = bookingRepository.findByBookingIdAndCustomer_CustomerId(request.getBookingId(), customerId)
                .orElseThrow(() -> new RuntimeException("Booking not found or does not belong to this customer"));

        // Check if feedback already exists for this booking
        if (feedbackRepository.existsByBookingIdAndCustomerId(request.getBookingId(), customerId)) {
            throw new RuntimeException("Feedback already exists for this booking");
        }

        // Create new feedback
        Feedback feedback = Feedback.builder()
                .booking(booking)
                .customer(user.getCustomer())
                .operatorStaff(booking.getOperatorStaff())
                .content(request.getContent())
                .type(request.getType())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return mapToResponse(savedFeedback);
    }

    @Override
    public FeedbackResponse updateFeedback(Integer feedbackId, UpdateFeedbackRequest request, Integer customerId) {
        // Find feedback and validate ownership
        Feedback feedback = feedbackRepository.findByFeedbackIdAndCustomerId(feedbackId, customerId)
                .orElseThrow(() -> new RuntimeException("Feedback not found or access denied"));

        // Update feedback
        feedback.setContent(request.getContent());
        feedback.setType(request.getType());

        Feedback updatedFeedback = feedbackRepository.save(feedback);

        return mapToResponse(updatedFeedback);
    }

    @Override
    public void deleteFeedback(Integer feedbackId, Integer customerId) {
        // Find feedback and validate ownership
        Feedback feedback = feedbackRepository.findByFeedbackIdAndCustomerId(feedbackId, customerId)
                .orElseThrow(() -> new RuntimeException("Feedback not found or access denied"));

        feedbackRepository.delete(feedback);
    }

    @Override
    public List<FeedbackResponse> getAllFeedbacksByCustomer(Integer customerId) {
        List<Feedback> feedbacks = feedbackRepository.findByCustomerId(customerId);
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponse> getFeedbacksByBooking(Integer bookingId, Integer customerId) {
        List<Feedback> feedbacks = feedbackRepository.findByBookingIdAndCustomerId(bookingId, customerId);
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackResponse getFeedbackById(Integer feedbackId, Integer customerId) {
        Feedback feedback = feedbackRepository.findByFeedbackIdAndCustomerId(feedbackId, customerId)
                .orElseThrow(() -> new RuntimeException("Feedback not found or access denied"));

        return mapToResponse(feedback);
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .bookingId(feedback.getBooking().getBookingId())
                .content(feedback.getContent())
                .type(feedback.getType())
                .createdAt(feedback.getCreatedAt())
                .processStatus(feedback.getProcessStatus())
                .operatorName(feedback.getOperatorStaff() != null ? 
                    feedback.getOperatorStaff().getUsers().getFullName() : null)
                .storageUnitName(feedback.getBooking().getStorageUnit() != null ? 
                    feedback.getBooking().getStorageUnit().getName() : null)
                .transportUnitName(feedback.getBooking().getTransportUnit() != null ? 
                    feedback.getBooking().getTransportUnit().getNameCompany() : null)
                .build();
    }
} 