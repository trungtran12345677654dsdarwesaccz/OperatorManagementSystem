package org.example.operatormanagementsystem.ManageHungBranch.service;

import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentDTO;
import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Lấy tất cả payments
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy payment theo ID
    public PaymentDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
        return convertToDTO(payment);
    }

    // Tìm kiếm payments theo status
    public List<PaymentDTO> searchPaymentsByStatus(String status) {
        return paymentRepository.findByStatusContainingIgnoreCase(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tìm kiếm payments theo payer type
    public List<PaymentDTO> searchPaymentsByPayerType(String payerType) {
        return paymentRepository.findByPayerTypeContainingIgnoreCase(payerType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Tạo payment mới
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = convertToEntity(paymentDTO);
        payment.setPaidDate(LocalDate.now()); // Tự động set ngày thanh toán
        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    // Cập nhật payment
    public PaymentDTO updatePayment(Integer id, PaymentDTO paymentDTO) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));

        // Cập nhật các field
        existingPayment.setPayerType(paymentDTO.getPayerType());
        existingPayment.setPayerId(paymentDTO.getPayerId());
        existingPayment.setAmount(paymentDTO.getAmount());
        existingPayment.setStatus(paymentDTO.getStatus());
        existingPayment.setNote(paymentDTO.getNote());

        Payment updatedPayment = paymentRepository.save(existingPayment);
        return convertToDTO(updatedPayment);
    }

    // Xóa payment
    public void deletePayment(Integer id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with ID: " + id);
        }
        paymentRepository.deleteById(id);
    }

    // Convert Entity to DTO
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setPayerType(payment.getPayerType());
        dto.setPayerId(payment.getPayerId());
        dto.setAmount(payment.getAmount());
        dto.setPaidDate(payment.getPaidDate());
        dto.setStatus(payment.getStatus());
        dto.setNote(payment.getNote());
        return dto;
    }

    // Convert DTO to Entity
    private Payment convertToEntity(PaymentDTO dto) {
        Payment payment = new Payment();
        payment.setPaymentId(dto.getPaymentId());
        payment.setPayerType(dto.getPayerType());
        payment.setPayerId(dto.getPayerId());
        payment.setAmount(dto.getAmount());
        payment.setPaidDate(dto.getPaidDate());
        payment.setStatus(dto.getStatus());
        payment.setNote(dto.getNote());
        return payment;
    }
}