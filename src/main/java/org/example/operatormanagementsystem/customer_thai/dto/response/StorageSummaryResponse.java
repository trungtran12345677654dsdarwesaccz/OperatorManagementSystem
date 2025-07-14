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
public class StorageSummaryResponse {
    
    private Integer storageId;
    private String name;
    private String address;
    private String phone;
    private String status;
    private String note;
    private String image;
    private Integer slotCount;
    private LocalDateTime createdAt;
    
    // Thông tin manager
    private Integer managerId;
    private String managerName;
    
    // Thống kê feedback
    private Double averageStar;
    private Long totalFeedbacks;
    private Long totalLikes;
    private Long totalDislikes;
    
    // Danh sách tất cả feedback
    private List<FeedbackResponse> feedbacks;
} 