package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerPerformanceResponse {
    private String managerName;          // lấy từ approvedByManager.getName()
    private String managerEmail;         // approvedByManager.getEmail()
    private int totalProcessed;          // tất cả đơn xử lý bởi người này
    private int approved;
    private int rejected;
    private int pending;                 // nếu có record đã assigned nhưng chưa xử lý
    private double approvalRate;
    private double avgProcessingTime;
}
