package org.example.operatormanagementsystem.transportunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetricsResponse {
    private double avgApprovalTime;        // hours
    private double avgRejectionTime;       // hours
    private int bottleneckCount;
    private List<String> topRejectionReasons; // extracted từ managerNote
    private double systemEfficiency;
}
