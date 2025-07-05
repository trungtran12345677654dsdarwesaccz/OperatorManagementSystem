package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    @Query("SELECT f FROM Feedback f WHERE f.customer.customerId = :customerId")
    List<Feedback> findByCustomerId(@Param("customerId") Integer customerId);
    
    @Query("SELECT f FROM Feedback f WHERE f.booking.bookingId = :bookingId AND f.customer.customerId = :customerId")
    List<Feedback> findByBookingIdAndCustomerId(@Param("bookingId") Integer bookingId, @Param("customerId") Integer customerId);
    
    @Query("SELECT f FROM Feedback f WHERE f.feedbackId = :feedbackId AND f.customer.customerId = :customerId")
    Optional<Feedback> findByFeedbackIdAndCustomerId(@Param("feedbackId") Integer feedbackId, @Param("customerId") Integer customerId);
    
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.booking.bookingId = :bookingId AND f.customer.customerId = :customerId")
    boolean existsByBookingIdAndCustomerId(@Param("bookingId") Integer bookingId, @Param("customerId") Integer customerId);
} 