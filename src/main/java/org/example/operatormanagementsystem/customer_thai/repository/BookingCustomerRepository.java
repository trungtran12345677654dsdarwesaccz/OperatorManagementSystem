package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("bookingRepository_thai")
public interface BookingCustomerRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByCustomer_CustomerId(Integer customerId);

    Optional<Booking> findByBookingIdAndCustomer_CustomerId(Integer bookingId, Integer customerId);

    List<Booking> findByStorageUnit_StorageId(Integer storageId);

    Optional<Booking> findByStorageUnit_StorageIdAndSlotIndex(Integer storageId, Integer slotIndex);
} 