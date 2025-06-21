// src/main/java/org/example/operatormanagementsystem/managecustomerorderbystaff/service/impl/BookingServiceImpl.java
package org.example.operatormanagementsystem.managecustomerorderbystaff.service.impl;

import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.Customer; // Cần import Customer
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest; // Import BookingRequest DTO
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.CustomerRepository;
import org.example.operatormanagementsystem.managecustomerorderbystaff.service.BookingService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime; // Giữ lại nếu cần cho các logic khác
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository; // Giữ lại vì updateBooking có logic Customer

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

    // @Override
    // public Booking saveBooking(Booking booking) { // Đã bỏ chức năng tạo mới
    //    // ...
    // }

    @Override
    public Booking updateBooking(Integer id, BookingRequest bookingUpdatesRequest) { // Nhận BookingRequest
        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        // Cập nhật các trường nếu chúng được cung cấp trong request
        if (bookingUpdatesRequest.getStatus() != null) {
            existingBooking.setStatus(bookingUpdatesRequest.getStatus());
        }
        if (bookingUpdatesRequest.getDeliveryDate() != null) {
            existingBooking.setDeliveryDate(bookingUpdatesRequest.getDeliveryDate());
        }
        if (bookingUpdatesRequest.getNote() != null) {
            existingBooking.setNote(bookingUpdatesRequest.getNote());
        }

        // Cập nhật Customer nếu CustomerId được cung cấp trong request
        // Giả định bookingUpdatesRequest.getCustomerId() là ID của entity Customer
        if (bookingUpdatesRequest.getCustomerId() != null) {
            Customer customer = customerRepository.findById(bookingUpdatesRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found for update with ID: " + bookingUpdatesRequest.getCustomerId()));
            existingBooking.setCustomer(customer);
        }
        // Thêm logic cập nhật cho StorageUnit, TransportUnit, OperatorStaff nếu cần
        // Ví dụ:
        // if (bookingUpdatesRequest.getStorageUnitId() != null) {
        //     StorageUnit storageUnit = storageUnitRepository.findById(bookingUpdatesRequest.getStorageUnitId())
        //             .orElseThrow(() -> new RuntimeException("Storage Unit not found with ID: " + bookingUpdatesRequest.getStorageUnitId()));
        //     existingBooking.setStorageUnit(storageUnit);
        // }
        // ... (tương tự cho TransportUnit và OperatorStaff nếu có)

        return bookingRepository.save(existingBooking);
    }

    // @Override
    // public void deleteBooking(Integer id) { // Đã bỏ chức năng xóa
    //     // ...
    // }

    @Override
    public List<Booking> searchBookingsByCustomerName(String fullName) {
        return bookingRepository.findByCustomerUsersFullNameContainingIgnoreCase(fullName);
    }

    @Override
    public void updatePaymentStatus(Integer id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        booking.setStatus(status);
        bookingRepository.save(booking);
    }
}