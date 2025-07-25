package org.example.operatormanagementsystem.transportunit.service.impl;
import org.example.operatormanagementsystem.enumeration.TransportAvailabilityStatus;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.entity.TransportUnitApproval;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitApprovalRepository;
import org.example.operatormanagementsystem.transportunit.repository.TransportUnitRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.transportunit.dto.request.TransportUnitEmailRequest;
import org.example.operatormanagementsystem.transportunit.dto.response.TransportUnitResponse;
import org.example.operatormanagementsystem.transportunit.service.TransportUnitOnboardingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransportUnitOnboardingServiceImpl implements TransportUnitOnboardingService {

    private final TransportUnitRepository transportUnitRepository;
    private final TransportUnitApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    // THAY THẾ BẰNG ID THỰC TẾ CỦA USER SYSTEM TỪ SQL INSERT SCRIPT CỦA BẠN.
    // SAU KHI BẠN CHẠY SCRIPT VÀ LẤY ID TỪ PRINT STATEMENT.
    // Đảm bảo user với ID này tồn tại trong bảng Users và có vai trò phù hợp (ví dụ: SYSTEM_USER, ADMIN, v.v.).
    private static final Integer SYSTEM_USER_ID = 8; // VÍ DỤ: Cần thay thế bằng ID thực tế của System User

    private TransportUnitResponse toResponse(TransportUnit entity) {
        return TransportUnitResponse.builder()
                .transportId(entity.getTransportId())
                .nameCompany(entity.getNameCompany())
                .namePersonContact(entity.getNamePersonContact())
                .phone(entity.getPhone())
                .licensePlate(entity.getLicensePlate())
                .numberOfVehicles(entity.getNumberOfVehicles())
                .capacityPerVehicle(entity.getCapacityPerVehicle())
                .availabilityStatus(entity.getAvailabilityStatus())
                .certificateBackUrl(entity.getCertificateBackUrl())
                .certificateFrontUrl(entity.getCertificateFrontUrl())
                .status(entity.getStatus())
                .note(entity.getNote())

                .build();
    }

    @Transactional
    public TransportUnitResponse onboardNewTransportUnit(TransportUnitEmailRequest request) {
        // 1. Lưu transport unit trước
        TransportUnit newUnit = TransportUnit.builder()
                .nameCompany(request.getNameCompany())
                .namePersonContact(request.getNamePersonContact())
                .phone(request.getPhone())
                .licensePlate(request.getLicensePlate())
                .numberOfVehicles(request.getNumberOfVehicles())
                .capacityPerVehicle(request.getCapacityPerVehicle())
                .availabilityStatus(request.getAvailabilityStatus() != null
                        ? request.getAvailabilityStatus()
                        : TransportAvailabilityStatus.AVAILABLE)
                .certificateFrontUrl(request.getCertificateFrontUrl())
                .certificateBackUrl(request.getCertificateBackUrl())
                .status(UserStatus.PENDING_APPROVAL)
                .note(request.getNote())
                .build();

        // BẮT BUỘC phải flush để lấy ID ngay
        TransportUnit savedUnit = transportUnitRepository.saveAndFlush(newUnit);
        System.out.println(" Transport ID: " + savedUnit.getTransportId());

        // 2. Gán vào approval
        Users systemUser = userRepository.findById(SYSTEM_USER_ID)
                .orElseThrow(() -> new RuntimeException("System user not found"));

        TransportUnitApproval approval = new TransportUnitApproval();
        approval.setTransportUnit(savedUnit); // Không set approvalId bằng tay!
        approval.setRequestedByUser(systemUser);
        approval.setSenderEmail(request.getSenderEmail());
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setRequestedAt(LocalDateTime.now());

        approvalRepository.save(approval);

        return toResponse(savedUnit);
    }



}