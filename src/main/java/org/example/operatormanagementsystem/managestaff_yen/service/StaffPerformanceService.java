package org.example.operatormanagementsystem.managestaff_yen.service;

import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffPerformanceResponse;

import java.util.List;

public interface StaffPerformanceService {

    List<StaffPerformanceResponse> calculateAll();

    void sendPerformanceEmails();
}