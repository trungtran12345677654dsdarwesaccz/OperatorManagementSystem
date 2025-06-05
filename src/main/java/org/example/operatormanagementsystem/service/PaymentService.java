package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository repository;

    public List<Payment> getAll() {
        return repository.findAll();
    }

    public Payment getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Payment add(Payment payment) {
        return repository.save(payment);
    }

    public Payment update(Long id, Payment payment) {
        Payment exist = repository.findById(id).orElse(null);
        if (exist != null) {
            exist.setPaymentId(payment.getPaymentId());
            exist.setPayerType(payment.getPayerType());
            exist.setAmount(payment.getAmount());
            exist.setNote(payment.getNote());
            exist.setStatus(payment.getStatus());
            return repository.save(exist);
        }
        return null;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Payment> search(String customerName, String note, String status) {
        return repository.findByCustomerNameContainingOrNoteContainingOrStatusContaining(
                customerName, note, status
        );
    }
}
