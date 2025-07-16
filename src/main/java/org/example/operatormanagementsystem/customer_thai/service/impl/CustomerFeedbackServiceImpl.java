package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.StorageSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.TransportSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.repository.BookingCustomerRepository;
import org.example.operatormanagementsystem.customer_thai.repository.CustomerFeedbackRepository;
import org.example.operatormanagementsystem.customer_thai.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.customer_thai.repository.C_TransportUnitRepository;
import org.example.operatormanagementsystem.customer_thai.service.CustomerFeedbackService;
import org.example.operatormanagementsystem.customer_thai.service.NotificationEventService;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerFeedbackServiceImpl implements CustomerFeedbackService {

    private final CustomerFeedbackRepository feedbackRepository;
    private final BookingCustomerRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationEventService notificationEventService;
    private final StorageUnitRepository storageUnitRepository;
    private final C_TransportUnitRepository CTransportUnitRepository;

    @Override
    public FeedbackResponse createFeedback(CreateFeedbackRequest request, Integer customerId) {
        // Validate customer exists by getting user with CUSTOMER role
        Users user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (user.getCustomer() == null) {
            throw new RuntimeException("User is not a customer");
        }

        // Tạo feedback không cần booking (hoặc booking=null nếu không truyền bookingId)
        Booking booking = null;
        StorageUnit storageUnit = null;
        TransportUnit transportUnit = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId()).orElse(null);
        }
        if (request.getStorageId() != null) {
            storageUnit = storageUnitRepository.findById(request.getStorageId()).orElse(null);
        }
        if (request.getTransportId() != null) {
            transportUnit = CTransportUnitRepository.findById(request.getTransportId()).orElse(null);
        }

        Feedback.FeedbackBuilder feedbackBuilder = Feedback.builder()
                .booking(booking)
                .customer(user.getCustomer())
                .operatorStaff(booking != null ? booking.getOperatorStaff() : null)
                .content(request.getContent())
                .type(request.getType())
                .star(request.getStar())
                .likes(request.getLikes())
                .dislikes(request.getDislikes());
        if (storageUnit != null) feedbackBuilder.storageUnit(storageUnit);
        if (transportUnit != null) feedbackBuilder.transportUnit(transportUnit);
        Feedback feedback = feedbackBuilder.build();

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Tạo notification sau khi tạo feedback thành công
        try {
            Customer customer = savedFeedback.getCustomer();
            String feedbackIdStr = savedFeedback.getFeedbackId().toString();
            String feedbackType = savedFeedback.getType().toString();
            
            notificationEventService.createFeedbackNotification(
                customer, 
                feedbackIdStr, 
                feedbackType
            );
        } catch (Exception e) {
            System.err.println("Error creating feedback notification: " + e.getMessage());
            e.printStackTrace();
        }

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
        feedback.setStar(request.getStar());
        feedback.setLikes(request.getLikes());
        feedback.setDislikes(request.getDislikes());

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
    public FeedbackResponse likeFeedback(Integer feedbackId, Integer customerId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        Integer currentLikes = feedback.getLikes() != null ? feedback.getLikes() : 0;
        feedback.setLikes(currentLikes + 1);
        
        Feedback updatedFeedback = feedbackRepository.save(feedback);
        return mapToResponse(updatedFeedback);
    }

    @Override
    public FeedbackResponse dislikeFeedback(Integer feedbackId, Integer customerId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        Integer currentLikes = feedback.getLikes() != null ? feedback.getLikes() : 0;
        feedback.setLikes(Math.max(0, currentLikes - 1)); // Không cho phép âm
        
        Feedback updatedFeedback = feedbackRepository.save(feedback);
        return mapToResponse(updatedFeedback);
    }

    @Override
    public List<StorageSummaryResponse> getAllStorageWithFeedbacks() {
        List<StorageUnit> storageUnits = storageUnitRepository.findAllStorageUnits();
        
        return storageUnits.stream()
                .map(this::mapToStorageSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransportSummaryResponse> getAllTransportWithFeedbacks() {
        List<TransportUnit> transportUnits = CTransportUnitRepository.findAllTransportUnits();
        
        return transportUnits.stream()
                .map(this::mapToTransportSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FeedbackResponse> getAllFeedbacksByCustomerId(Integer customerId) {
        List<Feedback> feedbacks = feedbackRepository.findAllByCustomerId(customerId);
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StorageSummaryResponse mapToStorageSummaryResponse(StorageUnit storageUnit) {
        // Lấy feedback trực tiếp cho storage (không qua booking)
        List<Feedback> directFeedbacks = feedbackRepository.findByStorageIdWithCustomerInfo(storageUnit.getStorageId());
        
        // Tính toán thống kê
        double averageStar = directFeedbacks.stream()
                .filter(f -> f.getStar() != null)
                .mapToInt(Feedback::getStar)
                .average()
                .orElse(0.0);
        
        long totalLikes = directFeedbacks.stream()
                .filter(f -> f.getLikes() != null)
                .mapToLong(Feedback::getLikes)
                .sum();
        
        long totalDislikes = directFeedbacks.stream()
                .filter(f -> f.getDislikes() != null)
                .mapToLong(Feedback::getDislikes)
                .sum();
        
        // Map feedback sang DTO
        List<FeedbackResponse> feedbackResponses = directFeedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return StorageSummaryResponse.builder()
                .storageId(storageUnit.getStorageId())
                .name(storageUnit.getName())
                .address(storageUnit.getAddress())
                .phone(storageUnit.getPhone())
                .status(storageUnit.getStatus())
                .note(storageUnit.getNote())
                .image(storageUnit.getImage())
                .slotCount(storageUnit.getSlotCount())
                .createdAt(storageUnit.getCreatedAt())
                .managerId(storageUnit.getManager() != null ? storageUnit.getManager().getManagerId() : null)
                .managerName(storageUnit.getManager() != null && storageUnit.getManager().getUsers() != null ? 
                    storageUnit.getManager().getUsers().getFullName() : null)
                .averageStar(averageStar)
                .totalFeedbacks((long) directFeedbacks.size())
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .feedbacks(feedbackResponses)
                .build();
    }

    private TransportSummaryResponse mapToTransportSummaryResponse(TransportUnit transportUnit) {
        // Lấy feedback trực tiếp cho transport (không qua booking)
        List<Feedback> directFeedbacks = feedbackRepository.findByTransportIdWithCustomerInfo(transportUnit.getTransportId());
        
        // Tính toán thống kê
        double averageStar = directFeedbacks.stream()
                .filter(f -> f.getStar() != null)
                .mapToInt(Feedback::getStar)
                .average()
                .orElse(0.0);
        
        long totalLikes = directFeedbacks.stream()
                .filter(f -> f.getLikes() != null)
                .mapToLong(Feedback::getLikes)
                .sum();
        
        long totalDislikes = directFeedbacks.stream()
                .filter(f -> f.getDislikes() != null)
                .mapToLong(Feedback::getDislikes)
                .sum();
        
        // Map feedback sang DTO
        List<FeedbackResponse> feedbackResponses = directFeedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return TransportSummaryResponse.builder()
                .transportId(transportUnit.getTransportId())
                .nameCompany(transportUnit.getNameCompany())
                .namePersonContact(transportUnit.getNamePersonContact())
                .phone(transportUnit.getPhone())
                .licensePlate(transportUnit.getLicensePlate())
                .status(transportUnit.getStatus() != null ? transportUnit.getStatus().toString() : null)
                .note(transportUnit.getNote())
                .imageTransportUnit(transportUnit.getImageTransportUnit())
                .createdAt(transportUnit.getCreatedAt())
                .numberOfVehicles(transportUnit.getNumberOfVehicles())
                .capacityPerVehicle(transportUnit.getCapacityPerVehicle())
                .availabilityStatus(transportUnit.getAvailabilityStatus() != null ? transportUnit.getAvailabilityStatus().toString() : null)
                .certificateFrontUrl(transportUnit.getCertificateFrontUrl())
                .averageStar(averageStar)
                .totalFeedbacks((long) directFeedbacks.size())
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .feedbacks(feedbackResponses)
                .build();
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .bookingId(feedback.getBooking() != null ? feedback.getBooking().getBookingId() : null)
                .content(feedback.getContent())
                .type(feedback.getType())
                .createdAt(feedback.getCreatedAt())
                .processStatus(feedback.getProcessStatus())
                .operatorName(feedback.getOperatorStaff() != null ? 
                    feedback.getOperatorStaff().getUsers().getFullName() : null)
                .storageUnitName(
                    feedback.getStorageUnit() != null ? feedback.getStorageUnit().getName() : null
                )
                .transportUnitName(
                    feedback.getTransportUnit() != null ? feedback.getTransportUnit().getNameCompany() : null
                )
                .star(feedback.getStar())
                .likes(feedback.getLikes())
                .dislikes(feedback.getDislikes())
                .customerFullName(feedback.getCustomer() != null && feedback.getCustomer().getUsers() != null ? 
                    feedback.getCustomer().getUsers().getFullName() : null)
                .customerImage(feedback.getCustomer() != null && feedback.getCustomer().getUsers() != null ? 
                    feedback.getCustomer().getUsers().getImg() : null)
                .build();
    }
} 