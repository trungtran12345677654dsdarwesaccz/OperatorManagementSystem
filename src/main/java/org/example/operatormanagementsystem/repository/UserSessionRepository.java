package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.UserSession;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserAndActiveTrue(Users user);
    void deleteByToken(String token);
}

