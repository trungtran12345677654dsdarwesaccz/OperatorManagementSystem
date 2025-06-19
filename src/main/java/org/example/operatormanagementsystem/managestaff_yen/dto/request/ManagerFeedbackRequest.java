package org.example.operatormanagementsystem.managestaff_yen.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerFeedbackRequest {

    @NotBlank(message = "Feedback content is required")
    @Size(max = 1000)
    private String content;

    private Integer rating; // 1-5 stars
}
