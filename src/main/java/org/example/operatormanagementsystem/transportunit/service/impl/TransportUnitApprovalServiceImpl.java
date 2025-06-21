package org.example.operatormanagementsystem.transportunit.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitApprovalProcessRequest;
import org.example.operatormanagementsystem.entity.Manager;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.entity.TransportUnitApproval;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitApprovalResponse;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitApprovalRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitApprovalService;
import org.example.operatormanagementsystem.service.EmailService; // Import EmailService

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok sẽ tạo constructor với tất cả các final fields
public class TransportUnitApprovalServiceImpl implements TransportUnitApprovalService {

    private final TransportUnitApprovalRepository approvalRepository;
    private final TransportUnitRepository transportUnitRepository;
    private final ManagerRepository managerRepository;
    private final EmailService emailService; // Inject EmailService

    private TransportUnitApprovalResponse toResponse(TransportUnitApproval approval) {

        // 👉 ƯU TIÊN senderEmail – chỉ khi không có mới fallback sang requestedByUser
        String requestedByUserEmail = approval.getSenderEmail();
        if ((requestedByUserEmail == null || requestedByUserEmail.isBlank())
                && approval.getRequestedByUser() != null) {
            requestedByUserEmail = approval.getRequestedByUser().getEmail();
        }

        String approvedByManagerEmail = null;
        if (approval.getApprovedByManager() != null
                && approval.getApprovedByManager().getUsers() != null) {
            approvedByManagerEmail = approval.getApprovedByManager().getUsers().getEmail();
        }

        return TransportUnitApprovalResponse.builder()
                .approvalId(approval.getApprovalId())
                .transportUnitId(approval.getTransportUnit().getTransportId())
                .transportUnitName(approval.getTransportUnit().getNameCompany())
                .requestedByUserId(
                        approval.getRequestedByUser() != null
                                ? approval.getRequestedByUser().getId()
                                : null)
                .senderEmail(requestedByUserEmail)      // ✅ luôn trả đúng
                .approvedByManagerId(
                        approval.getApprovedByManager() != null
                                ? approval.getApprovedByManager().getManagerId()
                                : null)
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

        Manager manager = managerRepository.findById(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager not found with ID: " + managerUserId + ". Make sure manager_id in 'manager' table matches 'id' in 'users' table for this user."));

        approval.setStatus(request.getStatus());
        approval.setApprovedByManager(manager);
        approval.setProcessedAt(LocalDateTime.now());
        approval.setManagerNote(request.getManagerNote());

        TransportUnitApproval savedApproval = approvalRepository.save(approval);

        TransportUnit transportUnit = approval.getTransportUnit();
        if (request.getStatus().equals(ApprovalStatus.APPROVED)) {
            transportUnit.setStatus(UserStatus.ACTIVE);
        } else if (request.getStatus().equals(ApprovalStatus.REJECTED)) {
            transportUnit.setStatus(UserStatus.INACTIVE); // Bạn có thể muốn set là UserStatus.REJECTED nếu có
        }
        transportUnitRepository.save(transportUnit);

        // --- Gửi email thông báo cho người yêu cầu ---
        String recipientEmail = approval.getSenderEmail(); // <-- ƯU TIÊN LẤY TỪ senderEmail TRONG APPROVAL
        String userNameForEmail = approval.getRequestedByUser() != null ?
                approval.getRequestedByUser().getFullName() :
                approval.getTransportUnit().getNamePersonContact(); // Lấy tên người liên hệ hoặc tên từ Users

        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            try {
                // SỬA LẠI THỨ TỰ THAM SỐ: userNameForEmail trước, transportUnit.getNameCompany() sau
                emailService.sendTransportUnitApprovalNotification(
                        recipientEmail,
                        userNameForEmail, // <-- THAY ĐỔI: Đây là userName
                        transportUnit.getNameCompany(), // <-- THAY ĐỔI: Đây là transportUnitName
                        request.getStatus(),
                        request.getManagerNote()
                );
                System.out.println("DEBUG: TransportUnitApprovalServiceImpl - Email notification sent for Transport Unit '" + transportUnit.getNameCompany() + "' to " + recipientEmail + " with status: " + request.getStatus().name());
            } catch (MessagingException e) {
                System.err.println("ERROR: Failed to send Transport Unit approval notification email to " + recipientEmail + ": " + e.getMessage());
                e.printStackTrace(); // In stack trace để debug dễ hơn
            }
        } else {
            System.err.println("WARNING: Cannot send Transport Unit approval email: Sender email is null or empty for approval ID: " + approvalId);
        }
        // --- Kết thúc gửi email ---

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