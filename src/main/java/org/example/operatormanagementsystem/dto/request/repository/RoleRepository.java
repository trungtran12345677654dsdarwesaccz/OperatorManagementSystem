package org.example.operatormanagementsystem.dto.request.repository;

import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByRole(UserRole role);
}
