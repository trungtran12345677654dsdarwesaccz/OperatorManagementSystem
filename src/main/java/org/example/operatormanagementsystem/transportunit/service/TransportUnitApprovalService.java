package org.example.operatormanagementsystem.transportunit.service;


import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitApprovalResponse;

import java.util.List;

public interface TransportUnitApprovalService {
    TransportUnitApprovalResponse processApproval(
            Integer approvalId,
            TransportUnitApprovalProcessRequest request,
            Integer managerUserId);

    TransportUnitApprovalResponse getApprovalById(Integer approvalId);

    List<TransportUnitApprovalResponse> getAllPendingApprovals();
}
