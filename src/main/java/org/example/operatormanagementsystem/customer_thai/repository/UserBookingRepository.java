package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBookingRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
}
