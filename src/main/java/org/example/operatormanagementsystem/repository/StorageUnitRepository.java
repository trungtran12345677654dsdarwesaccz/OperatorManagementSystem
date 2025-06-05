package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Long> {

    List<StorageUnit> findByNameContainingOrAddressContainingOrStatusContaining(String name, String address, String status);

}

