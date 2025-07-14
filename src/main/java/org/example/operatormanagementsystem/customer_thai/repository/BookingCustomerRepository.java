package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("bookingRepository_thai")
public interface BookingCustomerRepository extends JpaRepository<Booking, Integer> {

    @EntityGraph(attributePaths = {"storageUnit", "customer", "feedbacks"})
    List<Booking> findByCustomer_CustomerId(Integer customerId);

    @EntityGraph(attributePaths = {"storageUnit", "customer", "feedbacks"})
    Optional<Booking> findByBookingIdAndCustomer_CustomerId(Integer bookingId, Integer customerId);

    @EntityGraph(attributePaths = {"storageUnit", "customer", "feedbacks"})
    List<Booking> findByStorageUnit_StorageId(Integer storageId);

    @EntityGraph(attributePaths = {"storageUnit", "customer", "feedbacks"})
    Optional<Booking> findByStorageUnit_StorageIdAndSlotIndex(Integer storageId, Integer slotIndex);

    Optional<Booking> findTopByCustomer_Users_EmailOrderByCreatedAtDesc(String email);
} 