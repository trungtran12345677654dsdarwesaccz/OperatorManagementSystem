package com.swp.api.service;

import com.swp.api.model.Customer;
import com.swp.api.base.BaseService;
import java.util.List;

public interface CustomerService extends BaseService<Customer, Long> {
    void updateUserPassword(Customer customer);
    List<Customer> searchCustomers(String query);
    void blockCustomer(Long customerId);
    List<Customer> advancedSearch(String fullname, String email, String phone, String address);
}
