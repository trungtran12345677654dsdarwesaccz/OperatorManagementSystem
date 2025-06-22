package org.example.operatormanagementsystem.managecustomerorderbystaff.repository;

import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Đảm bảo có dòng này

@Repository // Đảm bảo có annotation này
public interface UsersRepository extends JpaRepository<Users, Integer> {

}