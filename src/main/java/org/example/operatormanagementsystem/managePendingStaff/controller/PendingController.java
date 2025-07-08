package org.example.operatormanagementsystem.managePendingStaff.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.config.JwtUtil;
import org.example.operatormanagementsystem.dto.request.ManagerStatusUpdateRequest;
import org.example.operatormanagementsystem.dto.response.UserResponse;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.managePendingStaff.dto.request.ApprovalHistoryFilterRequest;
import org.example.operatormanagementsystem.managePendingStaff.dto.request.PendingUserFilterRequest;
import org.example.operatormanagementsystem.managePendingStaff.dto.response.UserApprovalHistoryResponse;
import org.example.operatormanagementsystem.managePendingStaff.service.PendingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/pending-staff")
@PreAuthorize("hasRole('MANAGER')")
public class PendingController {

    private final PendingService pendingService;

    @PostMapping("/manager/update-status/{email}")
    public ResponseEntity<String> updateUserStatusByManager(@PathVariable String email,
                                                            @Valid @RequestBody ManagerStatusUpdateRequest request) {
        try {
            Users updatedUser = pendingService.updateStatusByManager(email, request.getNewStatus());
            String responseMessage;
            if (request.getNewStatus() == UserStatus.ACTIVE) {
                responseMessage = "User '" + updatedUser.getEmail() + "' with email " + email + " has been activated.";
            } else if (request.getNewStatus() == UserStatus.REJECTED) {
                responseMessage = "User '" + updatedUser.getEmail() + "' with email " + email + " has been rejected.";
            } else if (request.getNewStatus() == UserStatus.INACTIVE) {
                responseMessage = "User '" + updatedUser.getEmail() + "' with email " + email + " has been set to inactive.";
            } else {
                responseMessage = "User '" + updatedUser.getEmail() + "' with email " + email + " status updated to " + request.getNewStatus();
            }
            return ResponseEntity.ok(responseMessage);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/manager/users-by-status/{status}")
    public ResponseEntity<Page<UserResponse>> getUsersByStatus(@PathVariable UserStatus status,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = pendingService.getUsersByStatus(status, pageable);
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    @GetMapping("/manager/users-for-action")
    public ResponseEntity<Page<UserResponse>> getUsersNeedingManagerAction(@RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = pendingService.getUsersNeedingManagerAction(pageable);
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }

    @GetMapping("/manager/user-details/{email}")
    public ResponseEntity<UserResponse> getUserDetails(@PathVariable String email) {
        try {
            UserResponse response = pendingService.getUserDetailsForManager(email);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/manager/approval-history/{email}")
    public ResponseEntity<List<UserApprovalHistoryResponse>> getApprovalHistory(@PathVariable String email) {
        try {
            List<UserApprovalHistoryResponse> historyList = pendingService.getApprovalHistoryByUser(email);
            return historyList.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(historyList);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/manager/approval-history/search")
    public ResponseEntity<Page<UserApprovalHistoryResponse>> searchApprovalHistory(
            @RequestBody ApprovalHistoryFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("approvedAt").descending());
        Page<UserApprovalHistoryResponse> result = pendingService.getApprovalHistoryFiltered(request, pageable);
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/manager/approval-history/all")
    public ResponseEntity<List<UserApprovalHistoryResponse>> getAllApprovalHistories() {
        List<UserApprovalHistoryResponse> historyList = pendingService.getAllApprovalHistories();
        return historyList.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(historyList);
    }

    @GetMapping("/manager/approval-history/all-paged")
    public ResponseEntity<Page<UserApprovalHistoryResponse>> getAllApprovalHistoriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("approvedAt").descending());
        Page<UserApprovalHistoryResponse> result = pendingService.getAllApprovalHistories(pageable);
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @PostMapping("/manager/search-pending-users")
    public ResponseEntity<Page<UserResponse>> searchPendingUsers(
            @RequestBody PendingUserFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = pendingService.searchPendingUsers(request, pageable);
        return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
    }
}
