package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorStaffRepository extends JpaRepository<OperatorStaff, Integer> {

    // Tìm tất cả staff thuộc về một manager
    List<OperatorStaff> findByManagerManagerId(Integer managerId);

    // Tìm staff theo manager với phân trang
    Page<OperatorStaff> findByManagerManagerId(Integer managerId, Pageable pageable);

    // Tìm kiếm staff theo tên trong team của manager
    @Query("SELECT os FROM OperatorStaff os JOIN os.users u WHERE os.manager.managerId = :managerId " +
            "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<OperatorStaff> searchStaffByManagerAndTerm(@Param("managerId") Integer managerId,
                                                    @Param("searchTerm") String searchTerm,
                                                    Pageable pageable);

    // Đếm số lượng staff của một manager
    long countByManagerManagerId(Integer managerId);

    // Tìm staff theo status và manager
    @Query("SELECT os FROM OperatorStaff os JOIN os.users u WHERE os.manager.managerId = :managerId AND u.status = :status")
    List<OperatorStaff> findByManagerManagerIdAndUsersStatus(@Param("managerId") Integer managerId,
                                                             @Param("status") org.example.operatormanagementsystem.enumeration.UserStatus status);

    // Kiểm tra xem staff có thuộc về manager không
    boolean existsByOperatorIdAndManagerManagerId(Integer operatorId, Integer managerId);

    // Tìm staff theo ID và manager ID (để bảo mật)
    Optional<OperatorStaff> findByOperatorIdAndManagerManagerId(Integer operatorId, Integer managerId);
}