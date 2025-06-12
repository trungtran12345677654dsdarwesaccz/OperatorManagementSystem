package org.example.operatormanagementsystem.service.impl;

import org.example.operatormanagementsystem.base.BaseServiceImpl;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.repository.CustomerRepository;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl extends BaseServiceImpl<Customer, Integer> implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository repository, UserRepository userRepository) {
        super(repository);
        this.customerRepository = repository;
        this.userRepository = userRepository;
    }


    /**
     * Find by name email or address
     */
    public List<Customer> advancedSearch(String fullname, String email, String phone, String address) {
        // Triển khai logic tìm kiếm ở đây
        return customerRepository.findByFullnameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneContainingIgnoreCaseAndAddressContainingIgnoreCase(
                fullname != null ? fullname : "",
                email != null ? email : "",
                phone != null ? phone : "",
                address != null ? address : ""
        );
    }

    /**
     * Search customers by name or email (case-insensitive)
     */
    @Override
    public List<Customer> searchCustomers(String query) {
        return customerRepository.findByFullnameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    /**
     * Block customer account (set status to "BLOCKED")
     */
    @Override
    public void blockCustomer(int customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setUsers(UserStatus.INACTIVE);
            customerRepository.save(customer);
        } else {
            throw new RuntimeException("Customer not found");
        }
    }
}