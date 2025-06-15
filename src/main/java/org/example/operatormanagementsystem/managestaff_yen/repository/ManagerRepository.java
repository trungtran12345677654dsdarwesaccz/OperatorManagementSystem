package org.example.operatormanagementsystem.managestaff_yen.repository;


import org.example.operatormanagementsystem.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {

    // Tìm manager theo user email
    Optional<Manager> findByUsersEmail(String email);

    // Tìm manager theo user username
    Optional<Manager> findByUsersUsername(String username);

    // Kiểm tra manager có tồn tại không
    boolean existsByManagerId(Integer managerId);
}
