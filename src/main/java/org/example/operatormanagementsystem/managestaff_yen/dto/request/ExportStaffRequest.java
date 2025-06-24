package org.example.operatormanagementsystem.managestaff_yen.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.enumeration.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportStaffRequest {
    private UserStatus statusFilter; // null = tất cả
    private String searchTerm; // null = không filter theo search
    private String sortBy = "users.fullName";
    private String sortDir = "asc";
    private Boolean includeStatistics = true; // có xuất thống kê không
}