package org.example.operatormanagementsystem.transportunit.service.impl;

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

    // THAY THẾ BẰNG ID THỰC TẾ CỦA USER SYSTEM TỪ SQL INSERT SCRIPT
    // SAU KHI BẠN CHẠY SCRIPT VÀ LẤY ID TỪ PRINT STATEMENT.
    private static final Integer SYSTEM_USER_ID = 5; // VÍ DỤ: Cần thay thế bằng ID thực tế

    private TransportUnitResponse toResponse(TransportUnit entity) {
        return TransportUnitResponse.builder()
                .transportId(entity.getTransportId())
                .nameCompany(entity.getNameCompany())
                .namePersonContact(entity.getNamePersonContact())
                .phone(entity.getPhone())
                .licensePlate(entity.getLicensePlate())
                .status(entity.getStatus())
                .note(entity.getNote())
                .build();
    }

    @Override
    @Transactional
    public TransportUnitResponse onboardNewTransportUnit(TransportUnitEmailRequest request) {
        TransportUnit newUnit = TransportUnit.builder()
                .nameCompany(request.getNameCompany())
                .namePersonContact(request.getNamePersonContact())
                .phone(request.getPhone())
                .licensePlate(request.getLicensePlate())
                .status(UserStatus.PENDING_APPROVAL)
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();
        TransportUnit savedUnit = transportUnitRepository.save(newUnit);

        Users systemUser = userRepository.findById(SYSTEM_USER_ID)
                .orElseThrow(() -> new RuntimeException("System user not found with ID: " + SYSTEM_USER_ID + ". Please ensure SYSTEM_USER_ID is correct and this user exists in DB."));

        TransportUnitApproval approval = TransportUnitApproval.builder()
                .transportUnit(savedUnit)
                .requestedByUser(systemUser)
                .status(ApprovalStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        approvalRepository.save(approval);

        return toResponse(savedUnit);
    }
}
