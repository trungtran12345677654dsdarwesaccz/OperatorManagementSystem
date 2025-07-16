package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieChartSegmentResponse {
    private String label; // ACTIVE, UPCOMING, EXPIRED
    private long value;   // số lượng tương ứng
}