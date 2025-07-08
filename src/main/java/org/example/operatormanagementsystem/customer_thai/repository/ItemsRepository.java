package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("itemsRepository")
public interface ItemsRepository extends JpaRepository<Items, Integer> {
    List<Items> findByBookingBookingId(Integer bookingId);
    Optional<Items> findByItemIdAndBookingBookingId(Integer itemId, Integer bookingId);
    boolean existsByItemIdAndBookingBookingId(Integer itemId, Integer bookingId);
} 