package org.example.operatormanagementsystem.managecustomerorderbystaff.service;

import org.example.operatormanagementsystem.ManageHungBranch.dto.response.BookingDetailResponse;
import org.example.operatormanagementsystem.ManageHungBranch.dto.response.SlotsInfoResponse;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managecustomerorderbystaff.dto.request.BookingRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookingService {
    long getTotalBookings();
    double getTotalPaidAmount();
    long getPaidBookingsCount();
    long getUnpaidBookingsCount();
    List<Booking> getAllBookings();
    Optional<Booking> getBookingById(Integer id);
    // Booking saveBooking(Booking booking); // Đã bỏ chức năng tạo mới
    Booking updateBooking(Integer id, BookingRequest bookingUpdates); // Cập nhật để nhận DTO
    // void deleteBooking(Integer id); // Đã bỏ chức năng xóa
    List<Booking> searchBookingsByCustomerName(String fullName);
    Booking updatePaymentStatus(Integer id, String status); // Thay đổi từ void thành Booking
    SlotsInfoResponse getSlotsInfo(Integer storageId);
    void deleteBooking(Integer id);
    Optional<BookingDetailResponse> getBookingDetail(Integer storageId, Integer slotIndex);
    BookingDetailResponse createBooking(BookingRequest req);
    Map<String, List<Integer>> getAllRelatedIds();


}
