
package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerNameContainingOrNoteContainingOrStatusContaining(
            String customerName, String note, String status
    );
}
