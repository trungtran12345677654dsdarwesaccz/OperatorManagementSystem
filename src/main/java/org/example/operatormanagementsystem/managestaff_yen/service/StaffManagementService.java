package org.example.operatormanagementsystem.managestaff_yen.service;

import org.example.operatormanagementsystem.managestaff_yen.dto.request.ManagerFeedbackRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.request.UpdateStaffRequest;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.OperatorStaffResponse;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffListResponse;
import org.example.operatormanagementsystem.managestaff_yen.dto.response.StaffOverviewResponse;

public interface StaffManagementService {
    StaffListResponse viewStaffInformation(Integer managerId, int page, int size, String sortBy, String sortDir);
    StaffListResponse searchStaff(Integer managerId, String searchTerm, int page, int size);
    OperatorStaffResponse updateStaffInformation(Integer managerId, Integer operatorId, UpdateStaffRequest request);
    void blockOrDeleteStaffAccount(Integer managerId, Integer operatorId, boolean permanentDelete);
    void feedbackToStaff(Integer managerId, Integer operatorId, ManagerFeedbackRequest request);
    StaffOverviewResponse getStaffOverview(Integer managerId);
    OperatorStaffResponse getStaffDetails(Integer managerId, Integer operatorId); // <-- Sửa tại đây
}
