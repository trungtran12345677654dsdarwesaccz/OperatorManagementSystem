package org.example.operatormanagementsystem.transportunit.dto.response;

public interface WeeklyActivityResponse {
    String getDayOfWeek();
    Integer getApprovals();
    Integer getRejections();
    Double getAvgProcessingTime();
}
