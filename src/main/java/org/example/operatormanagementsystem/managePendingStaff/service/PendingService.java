package org.example.operatormanagementsystem.managePendingStaff.service;

import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.managePendingStaff.dto.request.ApprovalHistoryFilterRequest;
import org.example.operatormanagementsystem.managePendingStaff.dto.request.PendingUserFilterRequest;
import org.example.operatormanagementsystem.managePendingStaff.dto.response.UserApprovalHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PendingService {
    Users updateStatusByManager(String email, UserStatus newStatus);

    UserResponse getUserDetailsForManager(String email);

    Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable);


    Page<UserResponse> getUsersNeedingManagerAction(Pageable pageable);

    Page<UserResponse> searchPendingUsers(PendingUserFilterRequest request, Pageable pageable);

    Page<UserApprovalHistoryResponse> getApprovalHistoryFiltered(ApprovalHistoryFilterRequest request, Pageable pageable);

    Page<UserApprovalHistoryResponse> getAllApprovalHistories(Pageable pageable);

    List<UserApprovalHistoryResponse> getApprovalHistoryByUser(String email);

    List<UserApprovalHistoryResponse> getAllApprovalHistories();
}
