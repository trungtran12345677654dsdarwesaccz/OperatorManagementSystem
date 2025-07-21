package org.example.operatormanagementsystem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignmentRequest {
    
    @NotNull(message = "Shift ID is required")
    private Integer shiftId;
    
    @NotEmpty(message = "At least one operator must be assigned")
    private List<Integer> operatorIds;
    
    @NotNull(message = "Assignment date is required")
    private LocalDate assignmentDate;
}