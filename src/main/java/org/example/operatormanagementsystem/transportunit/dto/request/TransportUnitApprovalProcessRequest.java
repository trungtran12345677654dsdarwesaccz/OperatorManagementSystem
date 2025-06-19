package org.example.operatormanagementsystem.transportunit.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportUnitApprovalProcessRequest {
    @NotNull(message = "Approval status cannot be null")
    private ApprovalStatus status;

    @Size(max = 500, message = "Manager note cannot exceed 500 characters")
    private String managerNote;
}
