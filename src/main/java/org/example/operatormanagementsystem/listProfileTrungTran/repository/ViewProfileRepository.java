package org.example.operatormanagementsystem.listProfileTrungTran.repository;

import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewProfileRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
}