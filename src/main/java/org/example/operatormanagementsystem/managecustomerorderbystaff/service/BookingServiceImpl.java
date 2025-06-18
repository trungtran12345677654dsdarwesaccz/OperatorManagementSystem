package org.example.operatormanagementsystem.managecustomerorderbystaff.service;

import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.bookingrepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Lớp triển khai các phương thức quản lý booking
@Service
public class BookingServiceImpl implements BookingService {
    // Tiêm BookingRepository để tương tác với cơ sở dữ liệu
    @Autowired
    private bookingrepository bookingRepository;

    // Lấy danh sách tất cả các booking từ cơ sở dữ liệu
    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Lấy thông tin booking theo ID, trả về Optional để xử lý trường hợp không tìm thấy
    @Override
    public Optional<Booking> getBookingById(Integer id) {
        return bookingRepository.findById(id);
    }

    // Lưu hoặc tạo mới một booking vào cơ sở dữ liệu
    @Override
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    // Xóa một booking khỏi cơ sở dữ liệu theo ID
    @Override
    public void deleteBooking(Integer id) {
        bookingRepository.deleteById(id);
    }

    // Tìm kiếm danh sách booking theo tên khách hàng (tìm gần đúng)
    @Override
    public List<Booking> searchBookingsByCustomerName(String fullName) {
        return bookingRepository.findByCustomer_Users_FullNameContaining(fullName);
    }

    // Cập nhật thông tin booking theo ID
    // Ném ngoại lệ nếu không tìm thấy booking
    @Override
    public Booking updateBooking(Integer id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tìm thấy"));
        booking.setStatus(bookingDetails.getStatus()); // Cập nhật trạng thái
        booking.setDeliveryDate(bookingDetails.getDeliveryDate()); // Cập nhật ngày giao hàng
        booking.setNote(bookingDetails.getNote()); // Cập nhật ghi chú
        return bookingRepository.save(booking); // Lưu thay đổi
    }

    // Cập nhật trạng thái thanh toán cho tất cả các payment liên quan đến booking
    // Ném ngoại lệ nếu không có thông tin thanh toán
    @Override
    public Booking updatePaymentStatus(Integer bookingId, String paymentStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tìm thấy"));
        if (booking.getPayments() != null) {
            booking.getPayments().forEach(payment -> payment.setStatus(paymentStatus));
            return bookingRepository.save(booking); // Lưu thay đổi
        }
        throw new RuntimeException("Không có thông tin thanh toán liên quan đến booking này");
    }

    // Lấy tổng số lượng booking trong cơ sở dữ liệu
    @Override
    public Long getTotalBookings() {
        return bookingRepository.countAllBookings();
    }

    // Lấy tổng số tiền đã thanh toán từ tất cả các booking
    // Trả về 0.0 nếu không có dữ liệu
    @Override
    public Double getTotalPaidAmount() {
        Double sum = bookingRepository.sumPaidAmount();
        return sum != null ? sum : 0.0;
    }

    // Lấy số lượng booking đã thanh toán
    @Override
    public Long getPaidBookingsCount() {
        return (long) bookingRepository.findPaidBookings().size();
    }

    // Lấy số lượng booking chưa thanh toán
    @Override
    public Long getUnpaidBookingsCount() {
        return (long) bookingRepository.findUnpaidBookings().size();
    }
}