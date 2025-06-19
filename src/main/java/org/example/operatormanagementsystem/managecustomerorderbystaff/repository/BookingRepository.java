package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Tìm các booking theo trạng thái chứa chuỗi status
    List<Booking> findByStatusContaining(String status);

    // Tìm các booking theo tên khách hàng chứa chuỗi fullName
    List<Booking> findByCustomer_Users_FullNameContaining(String fullName);

    // Tìm các booking đã thanh toán (trạng thái thanh toán là 'PAID')
    @Query("SELECT b FROM Booking b JOIN b.payments p WHERE p.status = 'PAID'")
    List<Booking> findPaidBookings();

    // Tìm các booking chưa thanh toán (bao gồm booking không có thanh toán)
    @Query("SELECT b FROM Booking b LEFT JOIN b.payments p WHERE p.status IS NULL OR p.status != 'PAID'")
    List<Booking> findUnpaidBookings();

    // Đếm tổng số lượng booking trong cơ sở dữ liệu
    @Query("SELECT COUNT(b) FROM Booking b")
    Long countAllBookings();

    // Tính tổng số tiền đã thanh toán từ các payment có trạng thái 'PAID'
    @Query("SELECT SUM(p.amount) FROM Booking b JOIN b.payments p WHERE p.status = 'PAID'")
    Double sumPaidAmount();
}