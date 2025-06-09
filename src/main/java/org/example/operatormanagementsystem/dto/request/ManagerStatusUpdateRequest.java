package org.example.operatormanagementsystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.operatormanagementsystem.enumeration.UserStatus; // Import UserStatus

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerStatusUpdateRequest {
    @NotNull(message = "New status is required.")
    private UserStatus newStatus;
}