package org.example.operatormanagementsystem.ManageHungBranch.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.service.StorageUnitOnboardingService;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.entity.StorageUnitApproval;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.example.operatormanagementsystem.ManageHungBranch.dto.request.StorageUnitEmailRequest;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.StorageUnitResponse;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitApprovalRepository;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StorageUnitOnboardingServiceImpl implements StorageUnitOnboardingService {

    private final StorageUnitRepository storageUnitRepository;
    private final StorageUnitApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    // Bạn cần chỉ định SYSTEM_USER_ID đúng
    private static final Integer SYSTEM_USER_ID = 5;

    @Override
    @Transactional
    public StorageUnitResponse onboardNewStorageUnit(StorageUnitEmailRequest request) {
        StorageUnit storageUnit = StorageUnit.builder()
                .name(request.getName())
                .address(request.getAddress())
                .slotCount(request.getSlotCount())
                .phone(request.getPhone())
                .note(request.getNote())
                .image(request.getImageUrl())
                .status("PENDING")
                .build();

        StorageUnit savedUnit = storageUnitRepository.saveAndFlush(storageUnit);

        Users senderUser = userRepository.findById(SYSTEM_USER_ID)
                .orElseThrow(() -> new RuntimeException("System user not found"));

        StorageUnitApproval approval = StorageUnitApproval.builder()
                .storageUnit(savedUnit)
                .requestedByUser(senderUser)
                .senderEmail(request.getSenderEmail())
                .status(ApprovalStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        approvalRepository.save(approval);

        return StorageUnitResponse.builder()
                .storageId(savedUnit.getStorageId())
                .name(savedUnit.getName())
                .address(savedUnit.getAddress())
                .slotCount(savedUnit.getSlotCount())
                .phone(savedUnit.getPhone())
                .status(savedUnit.getStatus())
                .note(savedUnit.getNote())
                .build();
    }
}
