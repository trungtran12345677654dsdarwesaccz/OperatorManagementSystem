package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.base.BaseRepository;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
}
