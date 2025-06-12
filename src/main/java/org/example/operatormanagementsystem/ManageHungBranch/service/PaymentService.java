package org.example.operatormanagementsystem.ManageHungBranch.service;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentDTO;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentSearchDTO;
import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Page<PaymentDTO> searchPayments(PaymentSearchDTO searchDTO, Pageable pageable) {
        Page<Payment> payments = paymentRepository.searchPayments(searchDTO, pageable);
        return payments.map(this::convertToDTO);
    }

    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(this::convertToDTO);
    }

    public PaymentDTO getPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        return convertToDTO(payment);
    }

    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = convertToEntity(paymentDTO);
        // Sử dụng save() để lưu vào database
        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    public PaymentDTO updatePayment(Integer paymentId, PaymentDTO paymentDTO) {
        Payment existingPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        updatePaymentFields(existingPayment, paymentDTO);
        // Sử dụng save() để cập nhật vào database
        Payment updatedPayment = paymentRepository.save(existingPayment);
        return convertToDTO(updatedPayment);
    }

    public void deletePayment(Integer paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        paymentRepository.deleteById(paymentId);
    }

    public List<PaymentDTO> getOverduePayments() {
        List<Payment> overduePayments = paymentRepository.findOverduePayments();
        return overduePayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getPendingPayments() {
        List<Payment> pendingPayments = paymentRepository.findByStatus("PENDING");
        return pendingPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getBookingId() : null)
                .payerType(payment.getPayerType())
                .payerId(payment.getPayerId())
                .amount(payment.getAmount())
                .paidDate(payment.getPaidDate())
                .status(payment.getStatus())
                .note(payment.getNote())
                .build();

        // Tính toán thông tin quá hạn
        if (payment.getPaidDate() != null && "PENDING".equals(payment.getStatus())) {
            LocalDate today = LocalDate.now();
            if (payment.getPaidDate().isBefore(today)) {
                dto.setIsOverdue(true);
                dto.setDaysPastDue((int) ChronoUnit.DAYS.between(payment.getPaidDate(), today));
            } else {
                dto.setIsOverdue(false);
                dto.setDaysPastDue(0);
            }
        }

        return dto;
    }

    private Payment convertToEntity(PaymentDTO dto) {
        return Payment.builder()
                .paymentId(dto.getPaymentId())
                .payerType(dto.getPayerType())
                .payerId(dto.getPayerId())
                .amount(dto.getAmount())
                .paidDate(dto.getPaidDate())
                .status(dto.getStatus())
                .note(dto.getNote())
                .build();
    }

    private void updatePaymentFields(Payment payment, PaymentDTO dto) {
        payment.setPayerType(dto.getPayerType());
        payment.setPayerId(dto.getPayerId());
        payment.setAmount(dto.getAmount());
        payment.setPaidDate(dto.getPaidDate());
        payment.setStatus(dto.getStatus());
        payment.setNote(dto.getNote());
    }
}