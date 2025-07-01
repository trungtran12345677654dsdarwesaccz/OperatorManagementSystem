package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}