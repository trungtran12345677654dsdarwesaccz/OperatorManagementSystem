package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.TypeFeedback;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    
    private Integer feedbackId;
    private Integer bookingId;
    private String content;
    private TypeFeedback type;
    private LocalDateTime createdAt;
    private LocalDateTime processStatus;
    private String operatorName;
    private String storageUnitName;
    private String transportUnitName;
    private Integer star;
    private Integer likes;
    private Integer dislikes;
    
    // Customer info
    private String customerFullName;
    private String customerImage;
    private Boolean isLike;
    private Boolean isDislike;
} 