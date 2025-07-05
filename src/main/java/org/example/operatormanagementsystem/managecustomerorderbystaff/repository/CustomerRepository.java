package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    long countByCreatedAtAfter(LocalDateTime date); // Đếm khách hàng mới
    long countByStatus(String status); // Đếm khách hàng theo trạng thái
}