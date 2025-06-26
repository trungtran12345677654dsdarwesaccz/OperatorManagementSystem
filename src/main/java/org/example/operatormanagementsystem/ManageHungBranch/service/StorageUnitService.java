package org.example.operatormanagementsystem.ManageHungBranch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.operatormanagementsystem.ManageHungBranch.dto.StorageUnitDTO;
import org.example.operatormanagementsystem.entity.Manager;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.ManageHungBranch.repository.StorageUnitRepository;
import org.example.operatormanagementsystem.transportunit.repository.ManagerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public StorageUnitDTO createStorageUnit(StorageUnitDTO storageUnitDTO) {
        log.info("Tạo mới storage unit: {}", storageUnitDTO.getName());

        StorageUnit storageUnit = StorageUnit.builder()
                .name(storageUnitDTO.getName())
                .address(storageUnitDTO.getAddress())
                .phone(storageUnitDTO.getPhone())
                .status(storageUnitDTO.getStatus())
                .note(storageUnitDTO.getNote())
                .image(storageUnitDTO.getImage()) // Thêm trường image
                .createdAt(LocalDateTime.now())
                .build();

        // Gán manager nếu có
        if (storageUnitDTO.getManagerId() != null) {
            Manager manager = managerRepository.findById(storageUnitDTO.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy manager với ID: " + storageUnitDTO.getManagerId()));
            storageUnit.setManager(manager);
        }

        StorageUnit savedStorageUnit = storageUnitRepository.save(storageUnit);
        log.info("Đã tạo storage unit với ID: {}", savedStorageUnit.getStorageId());

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
        if (storageUnitDTO.getStatus() != null) {
            existingStorageUnit.setStatus(storageUnitDTO.getStatus());
        }
        if (storageUnitDTO.getNote() != null) {
            existingStorageUnit.setNote(storageUnitDTO.getNote());
        }
        if (storageUnitDTO.getImage() != null) {
            existingStorageUnit.setImage(storageUnitDTO.getImage());
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
     * Tìm kiếm storage units với điều kiện có ảnh
     */
    @Transactional(readOnly = true)
    public List<StorageUnitDTO> searchStorageUnits(String name, String address, String status, Integer managerId, Boolean hasImage) {
        log.info("Tìm kiếm storage units với các điều kiện - Name: {}, Address: {}, Status: {}, ManagerId: {}, HasImage: {}",
                name, address, status, managerId, hasImage);

        List<StorageUnit> storageUnits = storageUnitRepository.searchStorageUnits(name, address, status, managerId, hasImage);
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
     * Lấy storage units có ảnh
     */
    @Transactional(readOnly = true)
    public List<StorageUnitDTO> getStorageUnitsWithImage() {
        log.info("Lấy storage units có ảnh");
        List<StorageUnit> storageUnits = storageUnitRepository.findByImageIsNotNull();
        return storageUnits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy storage units không có ảnh
     */
    @Transactional(readOnly = true)
    public List<StorageUnitDTO> getStorageUnitsWithoutImage() {
        log.info("Lấy storage units không có ảnh");
        List<StorageUnit> storageUnits = storageUnitRepository.findByImageIsNull();
        return storageUnits.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật ảnh cho storage unit
     */
    public StorageUnitDTO updateStorageUnitImage(Integer id, String imageUrl) {
        log.info("Cập nhật ảnh cho storage unit với ID: {}", id);

        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy storage unit với ID: " + id));

        storageUnit.setImage(imageUrl);
        StorageUnit updatedStorageUnit = storageUnitRepository.save(storageUnit);

        log.info("Đã cập nhật ảnh cho storage unit với ID: {}", id);
        return convertToDTO(updatedStorageUnit);
    }

    /**
     * Xóa ảnh của storage unit
     */
    public StorageUnitDTO removeStorageUnitImage(Integer id) {
        log.info("Xóa ảnh của storage unit với ID: {}", id);

        StorageUnit storageUnit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy storage unit với ID: " + id));

        storageUnit.setImage(null);
        StorageUnit updatedStorageUnit = storageUnitRepository.save(storageUnit);

        log.info("Đã xóa ảnh của storage unit với ID: {}", id);
        return convertToDTO(updatedStorageUnit);
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
                .image(storageUnit.getImage()) // Thêm trường image
                .createdAt(storageUnit.getCreatedAt())
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
