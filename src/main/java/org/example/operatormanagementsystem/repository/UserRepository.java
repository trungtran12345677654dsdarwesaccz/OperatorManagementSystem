package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Page<Users> findByStatus(UserStatus status, Pageable pageable);

    Optional<Users> findById(int id);
    Optional<Users> findByFullName(String fullName); // Thêm dòng này

    @Query("SELECT u FROM Users u WHERE u.status = 'PENDING_APPROVAL' " +
            "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
            "AND (:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (:address IS NULL OR LOWER(u.address) LIKE LOWER(CONCAT('%', :address, '%')))")
    Page<Users> filterPendingUsers(@Param("email") String email,
                                   @Param("username") String username,
                                   @Param("fullName") String fullName,
                                   @Param("gender") UserGender gender,
                                   @Param("address") String address,
                                   Pageable pageable);


}
