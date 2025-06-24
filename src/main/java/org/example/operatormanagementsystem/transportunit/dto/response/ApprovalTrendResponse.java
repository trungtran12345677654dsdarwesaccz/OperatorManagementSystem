package org.example.operatormanagementsystem.transportunit.dto.response;

public interface ApprovalTrendResponse {
    String getDate();            // do SQL trả DATE AS String
    Integer getSubmissions();
    Integer getApprovals();
    Integer getRejections();
    Double getApprovalRate();
}
