package org.example.operatormanagementsystem.transportunit.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Tìm các booking theo transportId
    List<Booking> findByTransportUnit_TransportId(Integer transportId);

    // Tìm theo status + ngày giao
    List<Booking> findByStatusAndDeliveryDateBetween(String status, LocalDateTime from, LocalDateTime to);

    // Tìm theo keyword trong note
    List<Booking> findByNoteContainingIgnoreCase(String keyword);
}