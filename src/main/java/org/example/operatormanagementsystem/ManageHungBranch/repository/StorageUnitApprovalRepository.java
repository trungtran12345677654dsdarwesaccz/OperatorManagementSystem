package org.example.operatormanagementsystem.ManageHungBranch.repository;

import org.example.operatormanagementsystem.entity.StorageUnitApproval;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StorageUnitApprovalRepository extends JpaRepository<StorageUnitApproval, Integer> {
    Optional<StorageUnitApproval> findByStorageUnit_StorageIdAndStatus(Integer storageUnitId, ApprovalStatus status);
    List<StorageUnitApproval> findByStatus(ApprovalStatus status);
    Page<StorageUnitApproval> findByStatus(ApprovalStatus status, Pageable pageable);
    Page<StorageUnitApproval> findByStatusNot(ApprovalStatus status, Pageable pageable);
}

