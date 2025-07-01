package org.example.operatormanagementsystem.transportunit.dto.response;

public interface ManagerPerformanceResponse {
    String getManagerName();
    String getManagerEmail();
    int getTotalProcessed();
    int getApproved();
    int getRejected();
    int getPending();
    double getApprovalRate();
    double getAvgProcessingTime();
}
