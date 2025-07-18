package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Booking;
import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.example.operatormanagementsystem.entity.TransportUnit;
import org.example.operatormanagementsystem.enumeration.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    long countByStatus(String status);
    long countByStatusIn(List<String> statuses); // Thêm để hỗ trợ tương lai
    long countByStatusNot(String status);
    List<Booking> findByCustomerUsersFullNameContainingIgnoreCase(String fullName);
    List<Booking> findAllByStorageUnit_StorageId(Integer storageId);
    List<Booking> findByOperatorStaff(OperatorStaff operatorStaff);
    List<Booking> findByTransportUnit(TransportUnit transportUnit);
    Optional<Booking> findByStorageUnit_StorageIdAndSlotIndex(Integer storageId, Integer slotIndex);
    Optional<Booking> findByPaymentStatusAndTotalAndNote(PaymentStatus paymentStatus, Long total, String note);
}