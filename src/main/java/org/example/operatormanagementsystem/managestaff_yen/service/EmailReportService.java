package org.example.operatormanagementsystem.managestaff_yen.service;

import jakarta.mail.MessagingException;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffPerformanceResponse;

public interface EmailReportService {
    void sendPerformancePraiseEmail(String recipientEmail, String fullName, int performanceScore) throws MessagingException;;
    void sendWarningEmail(String recipientEmail, String fullName, int performanceScore) throws MessagingException;
}