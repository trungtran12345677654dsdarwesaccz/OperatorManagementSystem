package org.example.operatormanagementsystem.transportunit.dto.response;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataResponse {
    private String period;               // e.g. "2024-05", từ createdAt hoặc requestedAt
    private int pending;                 // trạng thái đơn vị tại thời điểm đó
    private int active;
    private int inactive;
    private int totalApprovals;          // approvals processed trong khoảng đó
    private int totalRejections;
}
