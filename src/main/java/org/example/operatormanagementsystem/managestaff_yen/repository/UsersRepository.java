package org.example.operatormanagementsystem.managestaff_yen.repository;


import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    // Tìm user theo email
    Optional<Users> findByEmail(String email);

    // Tìm user theo username
    Optional<Users> findByUsername(String username);

    // Tìm user theo role
    List<Users> findByRole(UserRole role);

    // Tìm user theo status
    List<Users> findByStatus(UserStatus status);

    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);

    // Kiểm tra username đã tồn tại
    boolean existsByUsername(String username);
}