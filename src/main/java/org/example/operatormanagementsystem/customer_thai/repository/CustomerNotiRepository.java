package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.CustomerNoti;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerNotiRepository extends JpaRepository<CustomerNoti, Long> {
    List<CustomerNoti> findByUserOrderByCreatedAtDesc(Users user);
} 