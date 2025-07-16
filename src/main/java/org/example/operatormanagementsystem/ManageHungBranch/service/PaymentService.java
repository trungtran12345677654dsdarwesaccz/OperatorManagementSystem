package org.example.operatormanagementsystem.ManageHungBranch.service;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentDTO;
import org.example.operatormanagementsystem.ManageHungBranch.dto.PaymentSearchDTO;
import org.example.operatormanagementsystem.ManageHungBranch.repository.PaymentRepository;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Payment;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Page<PaymentDTO> searchPayments(PaymentSearchDTO searchDTO, Pageable pageable) {
        return paymentRepository.searchPayments(searchDTO, pageable)
                .map(this::convertToDTO);
    }

    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public PaymentDTO getPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        return convertToDTO(payment);
    }

    public PaymentDTO createPayment(PaymentDTO dto) {
        Payment payment = convertToEntity(dto);
        return convertToDTO(paymentRepository.save(payment));
    }

    public PaymentDTO updatePayment(Integer paymentId, PaymentDTO dto) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        updatePaymentFields(payment, dto);
        return convertToDTO(paymentRepository.save(payment));
    }

    public void deletePayment(Integer paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }
        paymentRepository.deleteById(paymentId);
    }



    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO.PaymentDTOBuilder builder = PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getBookingId() : null)
                .amount(payment.getAmount())
                .paidDate(payment.getPaidDate())
                .transactionNo(payment.getTransactionNo());

        // Thông tin người thanh toán
        if (payment.getPayer() != null) {
            builder.payerUserId(payment.getPayer().getId());
            builder.payerFullName(payment.getPayer().getFullName());
        }

        // Thông tin booking + customer
        if (payment.getBooking() != null) {
            builder.bookingCode(String.valueOf(payment.getBooking().getBookingId()));
            Customer customer = payment.getBooking().getCustomer();
            if (customer != null && customer.getUsers() != null) {
                builder.payerFullName(customer.getUsers().getFullName());
            } else {
                builder.payerFullName("Không xác định");
            }
        }

        return builder.build();
    }


    private Payment convertToEntity(PaymentDTO dto) {
        Users payer = null;
        if (dto.getPayerUserId() != null) {
            payer = new Users();
            payer.setId(dto.getPayerUserId());
        }

        return Payment.builder()
                .paymentId(dto.getPaymentId())
                .amount(dto.getAmount())
                .paidDate(dto.getPaidDate())
                .transactionNo(dto.getTransactionNo())
                .payer(payer)
                .build();
    }


    private void updatePaymentFields(Payment payment, PaymentDTO dto) {
        if (dto.getPayerUserId() != null) {
            Users payer = new Users();
            payer.setId(dto.getPayerUserId());
            payment.setPayer(payer);
        }
        payment.setAmount(dto.getAmount());
        payment.setPaidDate(dto.getPaidDate());

        payment.setTransactionNo(dto.getTransactionNo());
    }
}
