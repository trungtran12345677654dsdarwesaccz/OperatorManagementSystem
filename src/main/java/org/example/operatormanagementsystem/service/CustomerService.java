package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.base.BaseService;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Users;
import java.util.List;

public interface CustomerService extends BaseService<Customer, Integer> {
    List<Customer> searchCustomers(String query);
    void blockCustomer(int customerId);
    List<Customer> advancedSearch(String fullname, String email, String phone, String address);
}
