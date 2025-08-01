package org.example.operatormanagementsystem.ManageHungBranch.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageEmailService;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageUnitApprovalService;
import org.example.operatormanagementsystem.entity.*;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.StorageUnitApprovalResponse;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitApprovalRepository;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.example.operatormanagementsystem.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageUnitApprovalServiceImpl implements StorageUnitApprovalService {

    private final StorageUnitApprovalRepository approvalRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final ManagerRepository managerRepository;
    private final StorageEmailService storageEmailService;
    private static final Logger logger = LoggerFactory.getLogger(StorageUnitApprovalServiceImpl.class);

    private StorageUnitApprovalResponse toResponse(StorageUnitApproval approval) {
        String requestedByUserEmail = approval.getSenderEmail();
        String approvedByManagerEmail = approval.getApprovedByManager() != null && approval.getApprovedByManager().getUsers() != null
                ? approval.getApprovedByManager().getUsers().getEmail() : null;

        StorageUnit unit = approval.getStorageUnit();

        return StorageUnitApprovalResponse.builder()
                .approvalId(approval.getApprovalId())
                .storageUnitId(unit != null ? unit.getStorageId() : null)
                .storageUnitName(unit != null ? unit.getName() : null)
                .requestedByUserId(approval.getRequestedByUser() != null ? approval.getRequestedByUser().getId() : null)
                .senderEmail(requestedByUserEmail)
                .approvedByManagerId(approval.getApprovedByManager() != null ? approval.getApprovedByManager().getManagerId() : null)
                .approvedByManagerEmail(approvedByManagerEmail)
                .status(approval.getStatus())
                .requestedAt(approval.getRequestedAt())
                .processedAt(approval.getProcessedAt())
                .managerNote(approval.getManagerNote())
                .address(approval.getStorageUnit().getAddress())
                .phone(approval.getStorageUnit().getPhone())
                .slotCount(approval.getStorageUnit().getSlotCount())
                .imageUrl(approval.getStorageUnit().getImage())
                .build();
    }

    @Override
    @Transactional
    public StorageUnitApprovalResponse processApproval(
            Integer approvalId,
            StorageUnitApprovalProcessRequest request,
            Integer managerUserId) {

        StorageUnitApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Storage Unit Approval not found with ID: " + approvalId));

        if (!approval.getStatus().equals(ApprovalStatus.PENDING)) {
            throw new IllegalStateException("Approval is not in PENDING state. Current status: " + approval.getStatus());
        }

        Manager manager = managerRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager not found with ID: " + managerUserId));

        approval.setStatus(request.getStatus());
        approval.setApprovedByManager(manager);
        approval.setProcessedAt(LocalDateTime.now());
        approval.setManagerNote(request.getManagerNote());

        StorageUnitApproval savedApproval = approvalRepository.save(approval);

        StorageUnit storageUnit = approval.getStorageUnit();
        if (request.getStatus().equals(ApprovalStatus.APPROVED)) {
            storageUnit.setStatus("ACTIVE");
        } else if (request.getStatus().equals(ApprovalStatus.REJECTED)) {
            storageUnit.setStatus("REJECTED");
        }
        storageUnitRepository.save(storageUnit);

        // Gửi email từ tài khoản hệ thống
        String recipientEmail = approval.getSenderEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            recipientEmail = approval.getRequestedByUser() != null ? approval.getRequestedByUser().getEmail() : null;
        }

        boolean emailSent = false;
        String emailError = null;

        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            if (recipientEmail.toLowerCase().endsWith("@gmail.com")) {
                try {
                    storageEmailService.sendStorageUnitApprovalNotification(
                            recipientEmail,
                            approval.getRequestedByUser() != null ? approval.getRequestedByUser().getFullName() : "Bạn",
                            approval.getStatus(),
                            approval.getManagerNote()
                    );
                    emailSent = true;
                    logger.info("Email sent from system account to: {}", recipientEmail);
                } catch (Exception e) {
                    emailError = "Failed to send email to " + recipientEmail + ": " + e.getMessage();
                    logger.error(emailError, e);
                }
            } else {
                emailError = "Không gửi email cho approvalId " + approval.getApprovalId() +
                        ": Email không phải Gmail (" + recipientEmail + ")";
                logger.warn(emailError);
            }
        } else {
            emailError = "Không có email để gửi thông báo duyệt cho approvalId " + approval.getApprovalId();
            logger.warn(emailError);
        }

        StorageUnitApprovalResponse response = toResponse(savedApproval);
        if (!emailSent && emailError != null) {
            response.setEmailError(emailError); // Thêm thông báo lỗi vào response
        }
        return response;
    }

    @Override
    public StorageUnitApprovalResponse getApprovalById(Integer approvalId) {
        StorageUnitApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Storage Unit Approval not found with ID: " + approvalId));
        return toResponse(approval);
    }

    @Override
    public List<StorageUnitApprovalResponse> getAllPendingApprovals() {
        return approvalRepository.findByStatus(ApprovalStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<StorageUnitApprovalResponse> getAllPendingApprovals(Pageable pageable) {
        Page<StorageUnitApproval> approvalPage = approvalRepository.findByStatus(ApprovalStatus.PENDING, pageable);
        return approvalPage.map(this::toResponse);
    }

    @Override
    public Page<StorageUnitApprovalResponse> getHistory(Pageable pageable) {
        Page<StorageUnitApproval> historyPage = approvalRepository.findByStatusNot(ApprovalStatus.PENDING, pageable);
        return historyPage.map(this::toResponse);
    }
}
