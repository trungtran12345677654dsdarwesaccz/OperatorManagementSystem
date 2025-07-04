package org.example.operatormanagementsystem.dashboardstaff.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecentActivityResponse {
    private String action;
    private String timeAgo;
}