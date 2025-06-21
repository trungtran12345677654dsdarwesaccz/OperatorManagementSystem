package org.example.operatormanagementsystem.transportunit.repository;

import org.example.operatormanagementsystem.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
    Optional<Manager> findByUsers_Id(Integer userId);

    // Tìm manager theo user email
    Optional<Manager> findByUsersEmail(String email);

    // Tìm manager theo user username
    Optional<Manager> findByUsersUsername(String username);

    // Kiểm tra manager có tồn tại không
    boolean existsByManagerId(Integer managerId);
}