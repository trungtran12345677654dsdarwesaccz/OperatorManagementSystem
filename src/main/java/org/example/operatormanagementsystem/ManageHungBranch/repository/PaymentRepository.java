package org.example.operatormanagementsystem.ManageHungBranch.repository;

import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Tìm kiếm theo status (không phân biệt hoa thường)
    List<Payment> findByStatusContainingIgnoreCase(String status);

    // Tìm kiếm theo payer type (không phân biệt hoa thường)
    List<Payment> findByPayerTypeContainingIgnoreCase(String payerType);

    // Tìm payments theo payer ID
    List<Payment> findByPayerId(Integer payerId);
}