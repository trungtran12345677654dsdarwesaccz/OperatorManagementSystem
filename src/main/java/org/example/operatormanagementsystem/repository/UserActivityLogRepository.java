package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.UserActivityLog;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Integer> {
    List<UserActivityLog> findByUserOrderByTimestampDesc(Users user);
}
