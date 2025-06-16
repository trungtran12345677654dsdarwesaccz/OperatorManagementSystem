package org.example.operatormanagementsystem.transportunit.controller;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitApprovalResponse;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitApprovalService;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport-unit-approvals")
@RequiredArgsConstructor
public class TransportUnitApprovalController {

    private final TransportUnitApprovalService approvalService;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<TransportUnitApprovalResponse> getApprovalById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(approvalService.getApprovalById(id));
        } catch (RuntimeException e) {
            System.err.println("Error getting Transport Unit Approval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TransportUnitApprovalResponse>> getAllPendingApprovals() {
        List<TransportUnitApprovalResponse> response = approvalService.getAllPendingApprovals();
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TransportUnitApprovalResponse> approveApproval(
            @PathVariable Integer id,
            @RequestBody(required = false) TransportUnitApprovalProcessRequest request) {
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
                request = TransportUnitApprovalProcessRequest.builder()
                        .status(org.example.operatormanagementsystem.enumeration.ApprovalStatus.APPROVED)
                        .managerNote("Approved by manager.")
                        .build();
            } else {
                request.setStatus(org.example.operatormanagementsystem.enumeration.ApprovalStatus.APPROVED);
            }

            TransportUnitApprovalResponse response = approvalService.processApproval(id, request, managerUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error approving Transport Unit Approval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TransportUnitApprovalResponse> rejectApproval(
            @PathVariable Integer id,
            @RequestBody(required = false) TransportUnitApprovalProcessRequest request) {
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
                request = TransportUnitApprovalProcessRequest.builder()
                        .status(org.example.operatormanagementsystem.enumeration.ApprovalStatus.REJECTED)
                        .managerNote("Rejected by manager.")
                        .build();
            } else {
                request.setStatus(org.example.operatormanagementsystem.enumeration.ApprovalStatus.REJECTED);
            }

            TransportUnitApprovalResponse response = approvalService.processApproval(id, request, managerUserId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error rejecting Transport Unit Approval: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
