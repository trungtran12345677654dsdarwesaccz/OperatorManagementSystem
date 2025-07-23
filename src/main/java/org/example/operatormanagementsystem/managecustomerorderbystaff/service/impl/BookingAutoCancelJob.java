package org.example.operatormanagementsystem.managecustomerorderbystaff.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.managecustomerorderbystaff.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingAutoCancelJob {
    private static final Logger logger = LoggerFactory.getLogger(BookingAutoCancelJob.class);
    private final BookingRepository bookingRepository;

    // Chạy mỗi phút để đảm bảo độ lệch nhỏ nhất
    @Scheduled(cron = "0 0 * * * *")
    public void autoCancelUnpaidBookings() {
        logger.info("Đang chạy job autoCancelUnpaidBookings...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysAgo = now.minusDays(2); // Kiểm tra sau 2 ngày
        List<Booking> bookings = bookingRepository.findAll();
        int cancelCount = 0;
        for (Booking booking : bookings) {
            if ((booking.getPaymentStatus() == null || !"COMPLETED".equalsIgnoreCase(booking.getPaymentStatus().name()))
                    && booking.getCreatedAt() != null
                    && booking.getStatus() != null
                    && !"CANCELED".equalsIgnoreCase(booking.getStatus())
                    && booking.getCreatedAt().isBefore(twoDaysAgo)) {
                booking.setStatus("CANCELED");
                bookingRepository.save(booking);
                cancelCount++;
                logger.info("Đơn hàng ID {} đã bị huỷ tự động do quá hạn thanh toán.", booking.getBookingId());
            }
        }
        if (cancelCount > 0) {
            logger.info("Đã huỷ tự động {} đơn hàng chưa thanh toán quá 2 ngày.", cancelCount);
        }
    }
} 