package org.example.operatormanagementsystem.ManageHungBranch.service;

import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.StorageUnitApprovalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StorageUnitApprovalService {
    StorageUnitApprovalResponse processApproval(
            Integer approvalId,
            StorageUnitApprovalProcessRequest request,
            Integer managerUserId);

    StorageUnitApprovalResponse getApprovalById(Integer approvalId);

    List<StorageUnitApprovalResponse> getAllPendingApprovals();
    Page<StorageUnitApprovalResponse> getAllPendingApprovals(Pageable pageable);
    Page<StorageUnitApprovalResponse> getHistory(Pageable pageable);
}

