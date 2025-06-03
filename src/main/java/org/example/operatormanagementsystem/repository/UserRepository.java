package org.example.operatormanagementsystem.repository;

import org.springframework.security.core.userdetails.User;
import org.example.operatormanagementsystem.entity.Users;

import java.util.Optional;

public interface UserRepository {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String username);
}
