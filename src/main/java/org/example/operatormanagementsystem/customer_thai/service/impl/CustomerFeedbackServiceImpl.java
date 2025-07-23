package org.example.operatormanagementsystem.customer_thai.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.customer_thai.dto.request.UpdateFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateStorageFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.request.CreateTransportFeedbackRequest;
import org.example.operatormanagementsystem.customer_thai.dto.response.FeedbackResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.StorageSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.dto.response.TransportSummaryResponse;
import org.example.operatormanagementsystem.customer_thai.repository.BookingCustomerRepository;
import org.example.operatormanagementsystem.customer_thai.repository.CustomerFeedbackRepository;
import org.example.operatormanagementsystem.customer_thai.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.customer_thai.repository.C_TransportUnitRepository;
import org.example.operatormanagementsystem.customer_thai.repository.FeedbackLikeDislikeRepository;
import org.example.operatormanagementsystem.customer_thai.service.CustomerFeedbackService;
import org.example.operatormanagementsystem.customer_thai.service.NotificationEventService;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.entity.FeedbackLikeDislike;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerFeedbackServiceImpl implements CustomerFeedbackService {

    private final Logger logger = LoggerFactory.getLogger(CustomerFeedbackServiceImpl.class);
    private final CustomerFeedbackRepository feedbackRepository;
    private final BookingCustomerRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationEventService notificationEventService;
    private final StorageUnitRepository storageUnitRepository;
    private final C_TransportUnitRepository CTransportUnitRepository;
    private final FeedbackLikeDislikeRepository feedbackLikeDislikeRepository;

    @Override
    public FeedbackResponse createFeedbackStorage(CreateStorageFeedbackRequest request, Integer customerId) {
        logger.info("[FEEDBACK_SERVICE] Creating STORAGE feedback");

        Users user = userRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"COMPLETED".equals(booking.getStatus())) throw new RuntimeException("Booking not completed");

        StorageUnit storageUnit = storageUnitRepository.findById(request.getStorageId()).orElseThrow(() -> new RuntimeException("Storage unit not found"));

        Feedback feedback = Feedback.builder()
                .booking(booking)
                .customer(user.getCustomer())
                .content(request.getContent())
                .type(org.example.operatormanagementsystem.enumeration.TypeFeedback.STORAGE)
                .star(request.getStar())
                .createdAt(java.time.LocalDateTime.now())
                .likes(0)
                .dislikes(0)
                .storageUnit(storageUnit)
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        notificationEventService.createFeedbackNotification(user.getCustomer(), saved.getFeedbackId().toString(), "STORAGE");
        return mapToResponse(saved, customerId);
    }

    @Override
    public FeedbackResponse createFeedbackTransport(CreateTransportFeedbackRequest request, Integer customerId) {
        logger.info("[FEEDBACK_SERVICE] Creating TRANSPORT feedback");

        Users user = userRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"COMPLETED".equals(booking.getStatus())) throw new RuntimeException("Booking not completed");

        TransportUnit transportUnit = CTransportUnitRepository.findById(request.getTransportId()).orElseThrow(() -> new RuntimeException("Transport unit not found"));

        Feedback feedback = Feedback.builder()
                .booking(booking)
                .customer(user.getCustomer())
                .content(request.getContent())
                .type(org.example.operatormanagementsystem.enumeration.TypeFeedback.TRANSPORTATION)
                .star(request.getStar())
                .createdAt(java.time.LocalDateTime.now())
                .likes(0)
                .dislikes(0)
                .transportUnit(transportUnit)
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        notificationEventService.createFeedbackNotification(user.getCustomer(), saved.getFeedbackId().toString(), "TRANSPORTATION");
        return mapToResponse(saved, customerId);
    }

    @Override
    public FeedbackResponse updateFeedback(Integer feedbackId, UpdateFeedbackRequest request, Integer customerId) {
        logger.info("[FEEDBACK_SERVICE] Starting updateFeedback for feedbackId: {}, customerId: {}", feedbackId, customerId);

        // Find feedback and validate ownership
        Feedback feedback = feedbackRepository.findByFeedbackIdAndCustomerId(feedbackId, customerId)
                .orElseThrow(() -> {
                    logger.error("[FEEDBACK_SERVICE] Feedback not found or access denied. feedbackId: {}, customerId: {}",
                        feedbackId, customerId);
                    return new RuntimeException("Feedback not found or access denied");
                });

        logger.info("[FEEDBACK_SERVICE] Feedback found and ownership validated. Updating content.");

        // Update feedback
        feedback.setContent(request.getContent());
        feedback.setType(request.getType());
        feedback.setStar(request.getStar());
        feedback.setLikes(request.getLikes());
        feedback.setDislikes(request.getDislikes());

        Feedback updatedFeedback = feedbackRepository.save(feedback);
        logger.info("[FEEDBACK_SERVICE] Feedback updated successfully: {}", updatedFeedback.getFeedbackId());

        return mapToResponse(updatedFeedback, customerId);
    }

    @Override
    public void deleteFeedback(Integer feedbackId, Integer customerId) {
        logger.info("[FEEDBACK_SERVICE] Starting deleteFeedback for feedbackId: {}, customerId: {}", feedbackId, customerId);

        // Find feedback and validate ownership
        Feedback feedback = feedbackRepository.findByFeedbackIdAndCustomerId(feedbackId, customerId)
                .orElseThrow(() -> {
                    logger.error("[FEEDBACK_SERVICE] Feedback not found or access denied for delete. feedbackId: {}, customerId: {}",
                        feedbackId, customerId);
                    return new RuntimeException("Feedback not found or access denied");
                });

        logger.info("[FEEDBACK_SERVICE] Feedback found and ownership validated. Deleting feedback.");
        feedbackRepository.delete(feedback);
        logger.info("[FEEDBACK_SERVICE] Feedback deleted successfully: {}", feedbackId);
    }

    @Override
    public FeedbackResponse likeFeedback(Integer feedbackId, Integer customerId) {
        logger.info("[FEEDBACK_SERVICE] Starting likeFeedback for feedbackId: {}, customerId: {}", feedbackId, customerId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("[FEEDBACK_SERVICE] Feedback not found for like. feedbackId: {}", feedbackId);
                    return new RuntimeException("Feedback not found");
                });

        Users user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<FeedbackLikeDislike> likeDislikeOpt = feedbackLikeDislikeRepository.findByFeedbackAndUser(feedback, user);
        boolean shouldIncreaseLike = false;
        if (likeDislikeOpt.isPresent()) {
            FeedbackLikeDislike likeDislike = likeDislikeOpt.get();
            if (Boolean.TRUE.equals(likeDislike.getIsLike())) {
                // Đã like rồi, không tăng nữa
                logger.info("[FEEDBACK_SERVICE] User already liked this feedback.");
            } else {
                // Chuyển từ dislike sang like
                likeDislike.setIsLike(true);
                likeDislike.setIsDislike(false);
                feedbackLikeDislikeRepository.save(likeDislike);
                shouldIncreaseLike = true;
                // Nếu trước đó là dislike, giảm số dislike
                Integer currentDislikes = feedback.getDislikes() != null ? feedback.getDislikes() : 0;
                if (currentDislikes > 0) feedback.setDislikes(currentDislikes - 1);
            }
        } else {
            // Chưa từng tương tác, tạo mới
            FeedbackLikeDislike newLike = FeedbackLikeDislike.builder()
                    .feedback(feedback)
                    .user(user)
                    .isLike(true)
                    .isDislike(false)
                    .build();
            feedbackLikeDislikeRepository.save(newLike);
            shouldIncreaseLike = true;
        }
        if (shouldIncreaseLike) {
            Integer currentLikes = feedback.getLikes() != null ? feedback.getLikes() : 0;
            feedback.setLikes(currentLikes + 1);
        }
        Feedback updatedFeedback = feedbackRepository.save(feedback);
        logger.info("[FEEDBACK_SERVICE] Feedback liked successfully");
        return mapToResponse(updatedFeedback, customerId);
    }

    @Override
    public FeedbackResponse dislikeFeedback(Integer feedbackId, Integer customerId) {
        logger.info("[FEEDBACK_SERVICE] Starting dislikeFeedback for feedbackId: {}, customerId: {}", feedbackId, customerId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> {
                    logger.error("[FEEDBACK_SERVICE] Feedback not found for dislike. feedbackId: {}", feedbackId);
                    return new RuntimeException("Feedback not found");
                });

        Users user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<FeedbackLikeDislike> likeDislikeOpt = feedbackLikeDislikeRepository.findByFeedbackAndUser(feedback, user);
        boolean shouldIncreaseDislike = false;
        if (likeDislikeOpt.isPresent()) {
            FeedbackLikeDislike likeDislike = likeDislikeOpt.get();
            if (Boolean.TRUE.equals(likeDislike.getIsDislike())) {
                // Đã dislike rồi, không tăng nữa
                logger.info("[FEEDBACK_SERVICE] User already disliked this feedback.");
            } else {
                // Chuyển từ like sang dislike
                likeDislike.setIsLike(false);
                likeDislike.setIsDislike(true);
                feedbackLikeDislikeRepository.save(likeDislike);
                shouldIncreaseDislike = true;
                // Nếu trước đó là like, giảm số like
                Integer currentLikes = feedback.getLikes() != null ? feedback.getLikes() : 0;
                if (currentLikes > 0) feedback.setLikes(currentLikes - 1);
            }
        } else {
            // Chưa từng tương tác, tạo mới
            FeedbackLikeDislike newDislike = FeedbackLikeDislike.builder()
                    .feedback(feedback)
                    .user(user)
                    .isLike(false)
                    .isDislike(true)
                    .build();
            feedbackLikeDislikeRepository.save(newDislike);
            shouldIncreaseDislike = true;
        }
        if (shouldIncreaseDislike) {
            Integer currentDislikes = feedback.getDislikes() != null ? feedback.getDislikes() : 0;
            feedback.setDislikes(currentDislikes + 1);
        }
        Feedback updatedFeedback = feedbackRepository.save(feedback);
        logger.info("[FEEDBACK_SERVICE] Feedback disliked successfully");
        return mapToResponse(updatedFeedback, customerId);
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
                .map(f -> mapToResponse(f, customerId))
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
                .map(f -> mapToResponse(f, f.getCustomer() != null && f.getCustomer().getUsers() != null ? f.getCustomer().getUsers().getId() : null))
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
                .map(f -> mapToResponse(f, f.getCustomer() != null && f.getCustomer().getUsers() != null ? f.getCustomer().getUsers().getId() : null))
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

    private FeedbackResponse mapToResponse(Feedback feedback, Integer currentUserId) {
        Boolean isLike = false;
        Boolean isDislike = false;
        if (currentUserId != null) {
            Optional<Users> userOpt = userRepository.findById(currentUserId);
            if (userOpt.isPresent()) {
                Optional<FeedbackLikeDislike> likeDislike = feedbackLikeDislikeRepository.findByFeedbackAndUser(feedback, userOpt.get());
                if (likeDislike.isPresent()) {
                    isLike = Boolean.TRUE.equals(likeDislike.get().getIsLike());
                    isDislike = Boolean.TRUE.equals(likeDislike.get().getIsDislike());
                }
            }
        }
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
                .isLike(isLike)
                .isDislike(isDislike)
                .build();
    }
}