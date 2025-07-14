package org.example.operatormanagementsystem.customer_thai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.TypeFeedback;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedbackRequest {
    
    @NotBlank(message = "Feedback content is required")
    private String content;
    
    @NotNull(message = "Feedback type is required")
    private TypeFeedback type;

    private Integer star;
    private Integer likes;
    private Integer dislikes;
} 