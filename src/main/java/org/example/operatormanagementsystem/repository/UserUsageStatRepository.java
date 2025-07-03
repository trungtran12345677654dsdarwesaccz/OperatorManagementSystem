package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.UserUsageStat;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserUsageStatRepository extends JpaRepository<UserUsageStat, Long> {
    Optional<UserUsageStat> findByUser(Users user);
}
