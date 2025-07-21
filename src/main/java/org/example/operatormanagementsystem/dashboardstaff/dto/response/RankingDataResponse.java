package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankingDataResponse {
    private int rank;
    private String name;
    private String unit;
    private String revenue;
    private int trips;
    private double successRate;
    private String change;
    private String trend;
    private String avatar;
}