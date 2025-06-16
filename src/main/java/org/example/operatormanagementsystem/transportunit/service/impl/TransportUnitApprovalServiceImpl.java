package org.example.operatormanagementsystem.transportunit.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitApprovalResponse;
import org.example.operatormanagementsystem.entity.Manager;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.entity.TransportUnitApproval;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitApprovalRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportUnitApprovalServiceImpl implements TransportUnitApprovalService {

    private final TransportUnitApprovalRepository approvalRepository;
    private final TransportUnitRepository transportUnitRepository;
    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;

    private TransportUnitApprovalResponse toResponse(TransportUnitApproval approval) {
        String requestedByUserEmail = approval.getRequestedByUser() != null ? approval.getRequestedByUser().getEmail() : null;
        String approvedByManagerEmail = null;
        if (approval.getApprovedByManager() != null && approval.getApprovedByManager().getUsers() != null) {
            approvedByManagerEmail = approval.getApprovedByManager().getUsers().getEmail();
        }

        return TransportUnitApprovalResponse.builder()
                .approvalId(approval.getApprovalId())
                .transportUnitId(approval.getTransportUnit().getTransportId())
                .transportUnitName(approval.getTransportUnit().getNameCompany())
                .requestedByUserId(approval.getRequestedByUser().getId())
                .requestedByUserEmail(requestedByUserEmail)
                .approvedByManagerId(approval.getApprovedByManager() != null ? approval.getApprovedByManager().getManagerId() : null)
                .approvedByManagerEmail(approvedByManagerEmail)
                .status(approval.getStatus())
                .requestedAt(approval.getRequestedAt())
                .processedAt(approval.getProcessedAt())
                .managerNote(approval.getManagerNote())
                .build();
    }

    @Override
    @Transactional
    public TransportUnitApprovalResponse processApproval(
            Integer approvalId,
            TransportUnitApprovalProcessRequest request,
            Integer managerUserId) {

        TransportUnitApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Transport Unit Approval not found with ID: " + approvalId));

        if (!approval.getStatus().equals(ApprovalStatus.PENDING)) {
            throw new IllegalStateException("Approval is not in PENDING state. Current status: " + approval.getStatus());
        }

        // Manager ID ở đây là User ID của Manager (ví dụ: Users.id)
        // Bạn cần tìm Manager entity dựa trên User ID của nó
        Manager manager = managerRepository.findById(managerUserId) // Cần tìm Manager entity bằng manager_id
                .orElseThrow(() -> new RuntimeException("Manager not found with ID: " + managerUserId));

        // Cập nhật trạng thái phê duyệt
        approval.setStatus(request.getStatus());
        approval.setApprovedByManager(manager);
        approval.setProcessedAt(LocalDateTime.now());
        approval.setManagerNote(request.getManagerNote());

        TransportUnitApproval savedApproval = approvalRepository.save(approval);

        // Cập nhật trạng thái của TransportUnit dựa trên quyết định phê duyệt
        TransportUnit transportUnit = approval.getTransportUnit();
        if (request.getStatus().equals(ApprovalStatus.APPROVED)) {
            transportUnit.setStatus(UserStatus.ACTIVE);
        } else if (request.getStatus().equals(ApprovalStatus.REJECTED)) {
            transportUnit.setStatus(UserStatus.INACTIVE);
        }
        transportUnitRepository.save(transportUnit);

        return toResponse(savedApproval);
    }

    @Override
    public TransportUnitApprovalResponse getApprovalById(Integer approvalId) {
        TransportUnitApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Transport Unit Approval not found with ID: " + approvalId));
        return toResponse(approval);
    }

    @Override
    public List<TransportUnitApprovalResponse> getAllPendingApprovals() {
        return approvalRepository.findAll().stream()
                .filter(approval -> approval.getStatus().equals(ApprovalStatus.PENDING))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
