package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Feedback;
import org.example.operatormanagementsystem.entity.StorageUnit;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    @Query("SELECT f FROM Feedback f WHERE f.feedbackId = :feedbackId AND f.customer.customerId = :customerId")
    Optional<Feedback> findByFeedbackIdAndCustomerId(@Param("feedbackId") Integer feedbackId, @Param("customerId") Integer customerId);
    
    // Lấy feedback trực tiếp cho storage unit
    @Query("SELECT f FROM Feedback f WHERE f.storageUnit = :storageUnit")
    List<Feedback> findByStorageUnit(@Param("storageUnit") StorageUnit storageUnit);
    
    // Lấy feedback trực tiếp cho transport unit
    @Query("SELECT f FROM Feedback f WHERE f.transportUnit = :transportUnit")
    List<Feedback> findByTransportUnit(@Param("transportUnit") TransportUnit transportUnit);
    
    // Lấy feedback trực tiếp cho storage unit theo ID với customer info
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.customer c LEFT JOIN FETCH c.users WHERE f.storageUnit.storageId = :storageId")
    List<Feedback> findByStorageIdWithCustomerInfo(@Param("storageId") Integer storageId);
    
    // Lấy feedback trực tiếp cho transport unit theo ID với customer info
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.customer c LEFT JOIN FETCH c.users WHERE f.transportUnit.transportId = :transportId")
    List<Feedback> findByTransportIdWithCustomerInfo(@Param("transportId") Integer transportId);
    
    // Lấy tất cả feedback theo customer_id
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.customer c LEFT JOIN FETCH c.users WHERE f.customer.customerId = :customerId ORDER BY f.createdAt DESC")
    List<Feedback> findAllByCustomerId(@Param("customerId") Integer customerId);

    // Kiểm tra trùng feedback storage cho cùng booking và storage
    boolean existsByBooking_BookingIdAndStorageUnit_StorageIdAndIsStorageTrue(Integer bookingId, Integer storageId);

    // Kiểm tra trùng feedback transport cho cùng booking và transport
    boolean existsByBooking_BookingIdAndTransportUnit_TransportIdAndIsTransportTrue(Integer bookingId, Integer transportId);
} 