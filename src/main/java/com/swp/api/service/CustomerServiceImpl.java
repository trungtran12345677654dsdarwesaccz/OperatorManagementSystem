package com.swp.api.service;

import com.swp.api.model.Customer;
import com.swp.api.repository.CustomerRepository;
import com.swp.api.base.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl extends BaseServiceImpl<Customer, Long> implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository repository) {
        super(repository);
        this.customerRepository = repository;
    }

    /**
     * Update user's password (hash password before saving if needed)
     */
    @Override
    public void updateUserPassword(Customer customer) {
        Optional<Customer> optionalCustomer = customerRepository.findById((long) customer.getCustomerId());
        if (optionalCustomer.isPresent()) {
            Customer existing = optionalCustomer.get();
            existing.setPassword(customer.getPassword());
            customerRepository.save(existing);
        } else {
            throw new RuntimeException("Customer not found");
        }
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
    public void blockCustomer(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setStatus("BLOCKED");
            customerRepository.save(customer);
        } else {
            throw new RuntimeException("Customer not found");
        }
    }
}
