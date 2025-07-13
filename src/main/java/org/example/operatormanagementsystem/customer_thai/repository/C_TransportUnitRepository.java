package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.TransportUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("transportUnitRepository_thai")
public interface C_TransportUnitRepository extends JpaRepository<TransportUnit, Integer> {
    
    // Lấy tất cả transport units (không join feedback)
    @Query("SELECT tu FROM TransportUnit tu")
    List<TransportUnit> findAllTransportUnits();
    
    // Lấy transport unit cụ thể (không join feedback)
    @Query("SELECT tu FROM TransportUnit tu WHERE tu.transportId = :transportId")
    Optional<TransportUnit> findById(@Param("transportId") Integer transportId);
} 