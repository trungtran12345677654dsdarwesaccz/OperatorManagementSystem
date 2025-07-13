package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportSummaryResponse {
    
    private Integer transportId;
    private String nameCompany;
    private String namePersonContact;
    private String phone;
    private String licensePlate;
    private String status;
    private String note;
    private String imageTransportUnit;
    private LocalDateTime createdAt;
    private Integer numberOfVehicles;
    private Double capacityPerVehicle;
    private String availabilityStatus;
    private String certificateFrontUrl;
    
    // Thống kê feedback
    private Double averageStar;
    private Long totalFeedbacks;
    private Long totalLikes;
    private Long totalDislikes;
    
    // Danh sách tất cả feedback
    private List<FeedbackResponse> feedbacks;
} 