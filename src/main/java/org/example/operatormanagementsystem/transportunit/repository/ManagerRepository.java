package org.example.operatormanagementsystem.transportunit.repository;

import org.example.operatormanagementsystem.entity.Manager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
    Optional<Manager> findByUsers_Id(Integer userId);


    Optional<Manager> findByUsersEmail(String email);


    Optional<Manager> findByUsersUsername(String username);


    boolean existsByManagerId(Integer managerId);

    List<Manager> findByUsersUsernameContainingIgnoreCase(String username);


    Page<Manager> findAll(Pageable pageable);

}