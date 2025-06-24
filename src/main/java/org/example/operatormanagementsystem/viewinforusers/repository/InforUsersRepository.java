package org.example.operatormanagementsystem.viewinforusers.repository;

import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InforUsersRepository extends JpaRepository<Users, Integer> {

    // Optional vì có thể không tìm thấy
    Optional<Users> findById(Integer id);
}
