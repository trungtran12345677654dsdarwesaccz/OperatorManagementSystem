package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    long countByCreatedAtAfter(LocalDateTime date);
    long countByStatus(String status);
    long countByStatusIn(List<String> statuses); // Thêm để hỗ trợ tương lai
}