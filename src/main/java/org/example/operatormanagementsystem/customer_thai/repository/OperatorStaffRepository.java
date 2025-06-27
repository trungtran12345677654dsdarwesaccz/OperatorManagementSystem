package org.example.operatormanagementsystem.customer_thai.repository;

import org.example.operatormanagementsystem.entity.OperatorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("operatorStaffRepository_thai")
public interface OperatorStaffRepository extends JpaRepository<OperatorStaff, Integer> {
} 