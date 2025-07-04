package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    long countByStatus(String status);
    long countByStatusNot(String status);
    List<Booking> findByCustomerUsersFullNameContainingIgnoreCase(String fullName);
}