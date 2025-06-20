package org.example.operatormanagementsystem.transportunit.repository;

import org.example.operatormanagementsystem.entity.TransportUnitApproval;
import org.example.operatormanagementsystem.enumeration.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransportUnitApprovalRepository extends JpaRepository<TransportUnitApproval, Integer> {
    Optional<TransportUnitApproval> findByTransportUnit_TransportIdAndStatus(Integer transportUnitId, ApprovalStatus status);
    TransportUnitApproval findTopByTransportUnit_TransportIdOrderByRequestedAtDesc(Integer transportUnitId);
}