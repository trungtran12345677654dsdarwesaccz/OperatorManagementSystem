package org.example.operatormanagementsystem.managecustomerorderbystaff.service.impl;

import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.UsersRepository; // Import UsersRepository
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final UsersRepository usersRepository; // Thêm UsersRepository

    @Override
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    @Override
    public double getTotalPaidAmount() {
        return bookingRepository.findAll().stream()
                .filter(b -> "Paid".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(b -> 100.0) // Replace with actual amount calculation
                .sum();
    }

    @Override
    public long getPaidBookingsCount() {
        return bookingRepository.countByStatus("Paid");
    }

    @Override
    public long getUnpaidBookingsCount() {
        return bookingRepository.countByStatusNot("Paid");
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Optional<Booking> getBookingById(Integer id) {
        return bookingRepository.findById(id);
    }

    @Override
    public Booking updateBooking(Integer id, BookingRequest bookingUpdatesRequest) {
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (bookingUpdatesRequest.getStatus() != null) {
            existingBooking.setStatus(bookingUpdatesRequest.getStatus());
        }
        if (bookingUpdatesRequest.getDeliveryDate() != null) {
            existingBooking.setDeliveryDate(bookingUpdatesRequest.getDeliveryDate());
        }
        if (bookingUpdatesRequest.getNote() != null) {
            existingBooking.setNote(bookingUpdatesRequest.getNote());
        }

        if (bookingUpdatesRequest.getCustomerId() != null) {
            Customer customer = customerRepository.findById(bookingUpdatesRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found for update with ID: " + bookingUpdatesRequest.getCustomerId()));
            existingBooking.setCustomer(customer);
            if (bookingUpdatesRequest.getCustomerFullName() != null && !bookingUpdatesRequest.getCustomerFullName().isEmpty()) {
                Users user = customer.getUsers();
                if (user != null) {
                    user.setFullName(bookingUpdatesRequest.getCustomerFullName());
                    usersRepository.save(user); // Sử dụng usersRepository đã inject
                }
            }
        }

        if (bookingUpdatesRequest.getTotal() != null) {
            existingBooking.setTotal(bookingUpdatesRequest.getTotal());
        }
        if (bookingUpdatesRequest.getPaymentStatus() != null) {
            try {
                existingBooking.setPaymentStatus(PaymentStatus.valueOf(bookingUpdatesRequest.getPaymentStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid payment status: " + bookingUpdatesRequest.getPaymentStatus());
            }
        }

        return bookingRepository.save(existingBooking);
    }

    @Override
    public List<Booking> searchBookingsByCustomerName(String fullName) {
        return bookingRepository.findByCustomerUsersFullNameContainingIgnoreCase(fullName);
    }

    @Override
    public Booking updatePaymentStatus(Integer id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        try {
            booking.setPaymentStatus(PaymentStatus.valueOf(status.toUpperCase()));
            System.out.println("Cập nhật paymentStatus thành: " + status + " cho booking ID: " + id);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + status);
        }
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("Booking sau khi lưu: " + savedBooking.getPaymentStatus());
        return savedBooking;
    }
}