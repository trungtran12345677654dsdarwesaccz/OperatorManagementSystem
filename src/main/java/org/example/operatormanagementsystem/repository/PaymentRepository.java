package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByPayerTypeContainingIgnoreCaseOrNoteContainingIgnoreCaseOrStatusContainingIgnoreCase(
            String payerType, String note, String status);
}
