package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.StorageUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageUnitRepository extends JpaRepository<StorageUnit, Integer> {

    // Tìm kiếm storage unit theo tên (case insensitive)
    List<StorageUnit> findByNameContainingIgnoreCase(String name);

    // Tìm kiếm storage unit theo địa chỉ
    List<StorageUnit> findByAddressContainingIgnoreCase(String address);

    // Tìm kiếm storage unit theo status
    List<StorageUnit> findByStatus(String status);

    // Tìm kiếm storage unit theo manager ID
    List<StorageUnit> findByManagerManagerId(Integer managerId);

    // Tìm kiếm storage unit theo nhiều điều kiện
    @Query("SELECT s FROM StorageUnit s WHERE " +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:address IS NULL OR LOWER(s.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:managerId IS NULL OR s.manager.managerId = :managerId)")
    List<StorageUnit> searchStorageUnits(@Param("name") String name,
                                         @Param("address") String address,
                                         @Param("status") String status,
                                         @Param("managerId") Integer managerId);
}