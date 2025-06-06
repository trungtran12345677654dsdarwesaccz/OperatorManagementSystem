package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentById(Integer id) {
        return paymentRepository.findById(id);
    }

    public void deletePayment(Integer id) {
        paymentRepository.deleteById(id);
    }

    // Tìm kiếm nâng cao
    public List<Payment> searchPayments(String keyword) {
        return paymentRepository.findByPayerTypeContainingIgnoreCaseOrNoteContainingIgnoreCaseOrStatusContainingIgnoreCase(
                keyword, keyword, keyword
        );
    }
}
