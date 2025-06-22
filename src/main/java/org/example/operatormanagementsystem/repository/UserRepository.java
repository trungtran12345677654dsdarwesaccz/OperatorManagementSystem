package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<Users> findByStatus(UserStatus status);
    Optional<Users> findById(int id);

    List<Users> findByStatusIn(List<UserStatus> statuses);

}
