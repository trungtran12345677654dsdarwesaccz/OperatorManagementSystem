package org.example.operatormanagementsystem.managercustomer.repository;

import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.base.BaseRepository;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.enumeration.UserGender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManagerUserRepository extends BaseRepository<Users,Integer> {
    List<Users> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    List<Users> findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneContainingIgnoreCaseAndAddressContainingIgnoreCase(String s, String s1, String s2, String s3);
    Users findByEmail(String email);
    List<Users> findByRole(UserRole role);
    @Query("SELECT u FROM Users u WHERE " +
            "(:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:phone IS NULL OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) AND " +
            "(:address IS NULL OR LOWER(u.address) LIKE LOWER(CONCAT('%', :address, '%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:gender IS NULL OR u.gender = :gender) AND " +
            "(:status IS NULL OR u.status = :status)")
    Page<Users> findByFilters(
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("address") String address,
            @Param("role") UserRole role,
            @Param("gender") UserGender gender,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}