package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Dòng này quan trọng

@Repository // Dòng này cũng cực kỳ quan trọng
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Integer> {
}