package org.example.operatormanagementsystem.managestaff_yen.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarChartDataResponse {
    private String label; // Tên khuyến mãi
    private int value;    // Số đánh giá tích cực (rating >= 4)
}