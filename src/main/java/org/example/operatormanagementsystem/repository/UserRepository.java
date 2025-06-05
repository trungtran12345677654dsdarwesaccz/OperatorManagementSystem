package org.example.operatormanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findByFullName(String fullName);
    Optional<Users> findByEmail(String email);
}
