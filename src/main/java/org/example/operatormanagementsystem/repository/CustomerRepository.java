package org.example.operatormanagementsystem.repository;

import org.example.operatormanagementsystem.base.BaseRepository;
import org.example.operatormanagementsystem.entity.Customer;

import java.util.List;

public interface  CustomerRepository extends BaseRepository<Customer,Integer> {
    List<Customer> findByFullnameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    List<Customer> findByFullnameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneContainingIgnoreCaseAndAddressContainingIgnoreCase(String s, String s1, String s2, String s3);
}