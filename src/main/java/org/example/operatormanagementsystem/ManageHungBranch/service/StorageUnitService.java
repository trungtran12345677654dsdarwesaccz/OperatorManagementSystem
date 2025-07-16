package org.example.operatormanagementsystem.ManageHungBranch.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.ManageHungBranch.dto.CreateStorageUnitDTO;
import org.example.operatormanagementsystem.ManageHungBranch.dto.StorageUnitDTO;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Manager;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StorageUnitService {

    private final StorageUnitRepository storageUnitRepository;
    private final ManagerRepository managerRepository;

    /**
     * Lấy tất cả storage units
     */
    @Transactional(readOnly = true)
    public List<StorageUnitDTO> getAllStorageUnits() {
        log.info("Lấy tất cả storage units");
        List<StorageUnit> storageUnits = storageUnitRepository.findAll();
        return storageUnits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy storage unit theo ID
     */
    @Transactional(readOnly = true)
    public Optional<StorageUnitDTO> getStorageUnitById(Integer id) {
        log.info("Lấy storage unit với ID: {}", id);
        return storageUnitRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Tạo mới storage unit
     */
    public StorageUnitDTO createStorageUnit(CreateStorageUnitDTO createStorageUnitDTO) {
        StorageUnit storageUnit = StorageUnit.builder()
                .name(createStorageUnitDTO.getName())
                .address(createStorageUnitDTO.getAddress())
                .phone(createStorageUnitDTO.getPhone())
                .status(createStorageUnitDTO.getStatus())
                .note(createStorageUnitDTO.getNote())
                .createdAt(LocalDateTime.now())
                .image(createStorageUnitDTO.getImage())
                .slotCount(createStorageUnitDTO.getSlotCount())
                .build();
        // Gán manager nếu có
        if (createStorageUnitDTO.getManagerId() != null) {
            Manager manager = managerRepository.findById(createStorageUnitDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy manager với ID: " + createStorageUnitDTO.getManagerId()));
            storageUnit.setManager(manager);
        }
        StorageUnit savedStorageUnit = storageUnitRepository.save(storageUnit);
        return convertToDTO(savedStorageUnit);
    }


    /**
     * Cập nhật storage unit
     */
    public StorageUnitDTO updateStorageUnit(Integer id, StorageUnitDTO storageUnitDTO) {
        log.info("Cập nhật storage unit với ID: {}", id);

        StorageUnit existingStorageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy storage unit với ID: " + id));

        // Cập nhật các trường nếu có giá trị mới
        if (storageUnitDTO.getName() != null) {
            existingStorageUnit.setName(storageUnitDTO.getName());
        }
        if (storageUnitDTO.getAddress() != null) {
            existingStorageUnit.setAddress(storageUnitDTO.getAddress());
        }
        if (storageUnitDTO.getPhone() != null) {
            existingStorageUnit.setPhone(storageUnitDTO.getPhone());
        }
        if (storageUnitDTO.getStatus() != null && !storageUnitDTO.getStatus().isEmpty()) {
            existingStorageUnit.setStatus(storageUnitDTO.getStatus());
        }

        if (storageUnitDTO.getNote() != null) {
            existingStorageUnit.setNote(storageUnitDTO.getNote());
        }
        // Thêm kiểm tra và cập nhật image
        if (storageUnitDTO.getImage() != null) {
            existingStorageUnit.setImage(storageUnitDTO.getImage());
        }
        if (storageUnitDTO.getSlotCount() != null) {
            existingStorageUnit.setSlotCount(storageUnitDTO.getSlotCount());
        }


        // Cập nhật manager nếu có
        if (storageUnitDTO.getManagerId() != null) {
            Manager manager = managerRepository.findById(storageUnitDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy manager với ID: " + storageUnitDTO.getManagerId()));
            existingStorageUnit.setManager(manager);
        }

        StorageUnit updatedStorageUnit = storageUnitRepository.save(existingStorageUnit);
        log.info("Đã cập nhật storage unit với ID: {}", id);

        return convertToDTO(updatedStorageUnit);
    }
    /**
     * Xóa storage unit
     */
    public void deleteStorageUnit(Integer id) {
        log.info("Xóa storage unit với ID: {}", id);

        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy storage unit với ID: " + id));

        storageUnitRepository.delete(storageUnit);
        log.info("Đã xóa storage unit với ID: {}", id);
    }

    /**
     * Tìm kiếm storage units
     */
    @Transactional(readOnly = true)
    public List<StorageUnitDTO> searchStorageUnits(String name, String address, String status, Integer managerId) {
        log.info("Tìm kiếm storage units với các điều kiện - Name: {}, Address: {}, Status: {}, ManagerId: {}",
                name, address, status, managerId);

        List<StorageUnit> storageUnits = storageUnitRepository.searchStorageUnits(name, address, status, managerId);
        return storageUnits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy storage units theo manager ID
     */
    @Transactional(readOnly = true)
    public List<StorageUnitDTO> getStorageUnitsByManagerId(Integer managerId) {
        log.info("Lấy storage units theo manager ID: {}", managerId);
        List<StorageUnit> storageUnits = storageUnitRepository.findByManagerManagerId(managerId);
        return storageUnits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Entity sang DTO
     */
    private StorageUnitDTO convertToDTO(StorageUnit storageUnit) {
        StorageUnitDTO dto = StorageUnitDTO.builder()
                .storageId(storageUnit.getStorageId())
                .name(storageUnit.getName())
                .address(storageUnit.getAddress())
                .phone(storageUnit.getPhone())
                .status(storageUnit.getStatus())
                .note(storageUnit.getNote())
                .createdAt(storageUnit.getCreatedAt())
                .image(storageUnit.getImage())
                .slotCount(storageUnit.getSlotCount())
                .bookedSlots(
                        storageUnit.getBookings().stream()
                                .map(Booking::getSlotIndex)
                                .collect(Collectors.toList())
                )
                .build();

        // Thêm thông tin manager nếu có
        if (storageUnit.getManager() != null) {
            dto.setManagerId(storageUnit.getManager().getManagerId());
            // Giả sử Users entity có trường name hoặc username
            if (storageUnit.getManager().getUsers() != null) {
                // Bạn cần thay đổi theo tên trường thực tế trong Users entity
                dto.setManagerName("Manager " + storageUnit.getManager().getManagerId());
            }
        }
        return dto;
    }
}