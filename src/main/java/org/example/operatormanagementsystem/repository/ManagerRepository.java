package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
    // Repository cơ bản cho Manager entity
    // Các phương thức CRUD cơ bản sẽ được kế thừa từ JpaRepository
}