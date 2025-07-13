package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("storageUnitRepository_thai")
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Integer> {
    
    // Lấy tất cả storage units (không join feedback)
    @Query("SELECT su FROM StorageUnit su")
    List<StorageUnit> findAllStorageUnits();
    
    // Lấy storage unit cụ thể (không join feedback)
    @Query("SELECT su FROM StorageUnit su WHERE su.storageId = :storageId")
    Optional<StorageUnit> findById(@Param("storageId") Integer storageId);
} 