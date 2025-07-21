package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRankingResponse {
    private int rank;
    private String name;
    private String totalRevenue;
    private int totalTrips;
    private double avgSuccessRate;
    private int members;
    private String change;
    private String trend;
}