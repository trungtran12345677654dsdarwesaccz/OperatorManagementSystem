package org.example.operatormanagementsystem.dto.response;

import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDetailsResponse {
    private Integer shiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private boolean isActive;
    private List<OperatorAssignment> assignedOperators;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperatorAssignment {
        private Integer operatorId;
        private String operatorName;
        private String email;
        private String assignmentStatus;
        private String assignmentDate;
    }
}