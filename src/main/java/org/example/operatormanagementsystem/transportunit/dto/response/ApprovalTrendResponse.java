package org.example.operatormanagementsystem.transportunit.dto.response;

public interface ApprovalTrendResponse {
    String getDate();
    Integer getSubmissions();
    Integer getApprovals();
    Integer getRejections();
    Double getApprovalRate();
}
