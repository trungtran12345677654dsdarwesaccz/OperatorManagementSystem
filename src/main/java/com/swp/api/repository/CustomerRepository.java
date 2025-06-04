package com.swp.api.repository;

import com.swp.api.base.BaseRepository;
import  com.swp.api.model.Customer;

import java.util.List;

public interface  CustomerRepository extends BaseRepository<Customer,Long> {
    List<Customer> findByFullnameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    List<Customer> findByFullnameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneContainingIgnoreCaseAndAddressContainingIgnoreCase(String s, String s1, String s2, String s3);
}
