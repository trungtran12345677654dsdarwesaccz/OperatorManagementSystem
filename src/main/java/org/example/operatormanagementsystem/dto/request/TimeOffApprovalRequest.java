package org.example.operatormanagementsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeOffApprovalRequest {
    
    @NotNull(message = "Request ID is required")
    private Integer requestId;

    private String managerComments;
}