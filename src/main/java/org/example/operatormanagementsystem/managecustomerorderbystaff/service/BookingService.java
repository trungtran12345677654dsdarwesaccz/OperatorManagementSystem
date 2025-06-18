package org.example.operatormanagementsystem.managecustomerorderbystaff.service;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Giao diện định nghĩa các phương thức quản lý booking
public interface BookingService {
    // Lấy danh sách tất cả các booking
    List<Booking> getAllBookings();

    // Lấy thông tin booking theo ID
    Optional<Booking> getBookingById(Integer id);

    // Lưu hoặc tạo mới một booking
    Booking saveBooking(Booking booking);

    // Xóa một booking theo ID
    void deleteBooking(Integer id);

    // Tìm kiếm booking theo tên khách hàng
    List<Booking> searchBookingsByCustomerName(String fullName);

    // Cập nhật thông tin booking
    Booking updateBooking(Integer id, Booking bookingDetails);

    // Cập nhật trạng thái thanh toán của booking
    Booking updatePaymentStatus(Integer bookingId, String paymentStatus);

    // Lấy tổng số lượng booking
    Long getTotalBookings();

    // Lấy tổng số tiền đã thanh toán
    Double getTotalPaidAmount();

    // Lấy số lượng booking đã thanh toán
    Long getPaidBookingsCount();

    // Lấy số lượng booking chưa thanh toán
    Long getUnpaidBookingsCount();
}