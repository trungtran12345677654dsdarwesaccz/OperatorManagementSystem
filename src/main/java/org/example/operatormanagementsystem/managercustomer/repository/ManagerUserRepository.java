package org.example.operatormanagementsystem.managercustomer.repository;


import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.base.BaseRepository;

import java.util.List;

public interface ManagerUserRepository extends BaseRepository<Users,Integer> {
    List<Users> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    List<Users> findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneContainingIgnoreCaseAndAddressContainingIgnoreCase(String s, String s1, String s2, String s3);
}