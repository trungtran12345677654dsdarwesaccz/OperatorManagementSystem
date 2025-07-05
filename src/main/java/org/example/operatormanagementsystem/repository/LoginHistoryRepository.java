package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.LoginHistory;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserOrderByLoginTimeDesc(Users user);
}
