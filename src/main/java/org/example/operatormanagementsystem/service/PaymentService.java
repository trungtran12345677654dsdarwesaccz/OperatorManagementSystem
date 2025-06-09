package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Get all payments (View receipts list)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payment by ID (View receipts detail)
    public Payment getPaymentById(Integer id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.orElse(null);
    }

    // Search payments by multiple criteria (Search receipts)
    public List<Payment> searchPayments(String payerType, Integer payerId, String status,
                                        LocalDate startDate, LocalDate endDate) {
        if (payerType != null && payerId != null && status != null && startDate != null && endDate != null) {
            return paymentRepository.findByPayerTypeAndPayerIdAndStatusAndPaidDateBetween(
                    payerType, payerId, status, startDate, endDate);
        } else if (payerType != null && status != null) {
            return paymentRepository.findByPayerTypeAndStatus(payerType, status);
        } else if (startDate != null && endDate != null) {
            return paymentRepository.findByPaidDateBetween(startDate, endDate);
        } else if (payerType != null) {
            return paymentRepository.findByPayerType(payerType);
        } else if (status != null) {
            return paymentRepository.findByStatus(status);
        } else if (payerId != null) {
            return paymentRepository.findByPayerId(payerId);
        } else {
            return paymentRepository.findAll();
        }
    }

    // Create new payment (Manage Customer Receipts)
    public Payment createPayment(Payment payment) {
        // Set default values if needed
        if (payment.getPaidDate() == null) {
            payment.setPaidDate(LocalDate.now());
        }
        if (payment.getStatus() == null || payment.getStatus().isEmpty()) {
            payment.setStatus("PENDING");
        }
        return paymentRepository.save(payment);
    }

    // Update existing payment (Manage Customer Receipts)
    public Payment updatePayment(Integer id, Payment paymentDetails) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();

            // Update fields
            if (paymentDetails.getPayerType() != null) {
                payment.setPayerType(paymentDetails.getPayerType());
            }
            if (paymentDetails.getPayerId() != null) {
                payment.setPayerId(paymentDetails.getPayerId());
            }
            if (paymentDetails.getAmount() != null) {
                payment.setAmount(paymentDetails.getAmount());
            }
            if (paymentDetails.getPaidDate() != null) {
                payment.setPaidDate(paymentDetails.getPaidDate());
            }
            if (paymentDetails.getStatus() != null) {
                payment.setStatus(paymentDetails.getStatus());
            }
            if (paymentDetails.getNote() != null) {
                payment.setNote(paymentDetails.getNote());
            }
            if (paymentDetails.getBooking() != null) {
                payment.setBooking(paymentDetails.getBooking());
            }

            return paymentRepository.save(payment);
        }
        return null;
    }

    // Delete payment (Manage Customer Receipts)
    public boolean deletePayment(Integer id) {
        if (paymentRepository.existsById(id)) {
            paymentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get payments by status
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    // Get payments by payer type
    public List<Payment> getPaymentsByPayerType(String payerType) {
        return paymentRepository.findByPayerType(payerType);
    }

    // Get payments by payer ID
    public List<Payment> getPaymentsByPayerId(Integer payerId) {
        return paymentRepository.findByPayerId(payerId);
    }

    // Get payments by date range
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaidDateBetween(startDate, endDate);
    }
}