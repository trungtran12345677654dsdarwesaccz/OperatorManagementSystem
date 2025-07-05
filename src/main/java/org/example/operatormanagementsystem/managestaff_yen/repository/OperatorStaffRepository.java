package org.example.operatormanagementsystem.managestaff_yen.repository;

import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserStatus;
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

    List<OperatorStaff> findByManagerManagerId(Integer managerId);

    Page<OperatorStaff> findByManagerManagerId(Integer managerId, Pageable pageable);

    @Query("""
        SELECT os FROM OperatorStaff os JOIN os.users u 
        WHERE os.manager.managerId = :managerId 
        AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
             OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
    """)
    Page<OperatorStaff> searchStaffByManagerAndTerm(
            @Param("managerId") Integer managerId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    long countByManagerManagerId(Integer managerId);

    @Query("""
        SELECT os FROM OperatorStaff os JOIN os.users u 
        WHERE os.manager.managerId = :managerId AND u.status = :status
    """)
    List<OperatorStaff> findByManagerManagerIdAndUsersStatus(
            @Param("managerId") Integer managerId,
            @Param("status") UserStatus status
    );

    boolean existsByOperatorIdAndManagerManagerId(Integer operatorId, Integer managerId);

    Optional<OperatorStaff> findByOperatorIdAndManagerManagerId(Integer operatorId, Integer managerId);

    @Query("""
        SELECT os FROM OperatorStaff os JOIN os.users u 
        WHERE os.manager.managerId = :managerId 
        AND (:searchTerm IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
             OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:status IS NULL OR u.status = :status)
    """)
    List<OperatorStaff> searchStaffByManagerAndTermForExport(
            @Param("managerId") Integer managerId,
            @Param("searchTerm") String searchTerm,
            @Param("status") UserStatus status
    );

    List<OperatorStaff> findAllByManagerManagerId(Integer managerId);

    @Query("""
        SELECT os FROM OperatorStaff os
        WHERE os.manager.managerId = :managerId
        AND (:searchTerm IS NULL OR LOWER(os.users.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(os.users.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(os.users.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:status IS NULL OR os.users.status = :status)
        AND (:gender IS NULL OR os.users.gender = :gender)
    """)
    Page<OperatorStaff> searchStaffWithFilters(
            @Param("managerId") Integer managerId,
            @Param("searchTerm") String searchTerm,
            @Param("status") UserStatus status,
            @Param("gender") UserGender gender,
            Pageable pageable
    );

    @Query("""
        SELECT os FROM OperatorStaff os 
        WHERE os.manager.managerId = :managerId 
        ORDER BY SIZE(os.bookings) DESC
    """)
    List<OperatorStaff> findTop5ByManagerOrderByBookingCountDesc(@Param("managerId") Integer managerId);

//    @Query("""
//        SELECT COUNT(os) FROM OperatorStaff os
//        WHERE os.manager.managerId = :managerId AND os.isOnline = true
//    """)
//    long countByManagerAndOnlineTrue(@Param("managerId") Integer managerId);
}
