package org.example.operatormanagementsystem.dashboardstaff.repository;

import org.example.operatormanagementsystem.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
}