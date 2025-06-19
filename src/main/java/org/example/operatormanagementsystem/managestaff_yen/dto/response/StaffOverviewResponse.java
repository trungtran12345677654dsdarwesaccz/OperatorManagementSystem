package org.example.operatormanagementsystem.managestaff_yen.dto.response;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffOverviewResponse {
    private long totalStaffs;
    private long activeStaffs;
    private long inactiveStaffs;
    private long blockedStaffs;
}
