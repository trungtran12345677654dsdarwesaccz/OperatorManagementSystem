package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("storageUnitRepository_thai")
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Integer> {
} 