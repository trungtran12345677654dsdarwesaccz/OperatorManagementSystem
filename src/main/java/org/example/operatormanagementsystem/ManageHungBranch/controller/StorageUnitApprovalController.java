package org.example.operatormanagementsystem.ManageHungBranch.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.StorageUnitApprovalResponse;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageUnitApprovalService;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage-unit-approvals")
@RequiredArgsConstructor
public class StorageUnitApprovalController {

    private final StorageUnitApprovalService approvalService;
    private final UserRepository userRepository;
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
// Thay đổi kiểu trả về từ List thành Page
    public ResponseEntity<Page<StorageUnitApprovalResponse>> getAllPendingApprovals(
            // Thêm 2 tham số này để nhận page và size từ URL
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StorageUnitApprovalResponse> responsePage = approvalService.getAllPendingApprovals(pageable);


        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/by-storage-unit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<StorageUnitApprovalResponse> getApprovalById(@PathVariable Integer id) {

        try {
            return ResponseEntity.ok(approvalService.getApprovalById(id));
        } catch (RuntimeException e) {
            System.err.println("Error getting Storage Unit Approval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/history/all-paged")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<StorageUnitApprovalResponse>> getApprovalHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StorageUnitApprovalResponse> historyPage = approvalService.getHistory(pageable);
        return ResponseEntity.ok(historyPage);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<StorageUnitApprovalResponse>> getAllPendingApprovals() {
        List<StorageUnitApprovalResponse> response = approvalService.getAllPendingApprovals();
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<StorageUnitApprovalResponse> approveApproval(
            @PathVariable Integer id,
            @RequestBody(required = false) StorageUnitApprovalProcessRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Users currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Logged in user not found in DB."));
            Integer managerUserId = currentUser.getId();

            if (request == null) {
                request = StorageUnitApprovalProcessRequest.builder()
                        .status(org.example.operatormanagementsystem.enumeration.ApprovalStatus.APPROVED)
                        .managerNote("Đã được chấp thuận bởi quản lý.")
                        .build();
            } else {
                request.setStatus(org.example.operatormanagementsystem.enumeration.ApprovalStatus.APPROVED);
            }

            StorageUnitApprovalResponse response = approvalService.processApproval(id, request, managerUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error approving Storage Unit Approval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<StorageUnitApprovalResponse> rejectApproval(
            @PathVariable Integer id,
            @RequestBody(required = false) StorageUnitApprovalProcessRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Users currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Logged in user not found in DB."));
            Integer managerUserId = currentUser.getId();

            if (request == null) {
                request = StorageUnitApprovalProcessRequest.builder()
                        .status(org.example.operatormanagementsystem.enumeration.ApprovalStatus.REJECTED)
                        .managerNote("Đã bị từ chối bởi quản lý.")
                        .build();
            } else {
                request.setStatus(org.example.operatormanagementsystem.enumeration.ApprovalStatus.REJECTED);
            }

            StorageUnitApprovalResponse response = approvalService.processApproval(id, request, managerUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error rejecting Storage Unit Approval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
