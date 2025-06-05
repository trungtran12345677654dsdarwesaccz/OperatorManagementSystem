package org.example.operatormanagementsystem.controller;

import org.example.operatormanagementsystem.dto.request.PasswordUpdateRequest;
import org.example.operatormanagementsystem.entity.Customer;
import org.example.operatormanagementsystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Lấy tất cả customer hoặc tìm kiếm theo query
    @GetMapping
    public List<Customer> getAllCustomers(
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address
    ) {
        if ((fullname == null || fullname.isEmpty()) &&
                (email == null || email.isEmpty()) &&
                (phone == null || phone.isEmpty()) &&
                (address == null || address.isEmpty())) {
            return customerService.findAll();
        }
        return customerService.advancedSearch(fullname, email, phone, address);
    }


    // Lấy customer theo ID
    @GetMapping("/{id}")
    public Optional<Customer> getCustomerById(@PathVariable int id) {
        return customerService.findById(id);
    }

    // Tạo mới customer
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

    // Cập nhật thông tin customer
    @PutMapping("/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setCustomerId(id.intValue());
        return customerService.save(customer);
    }

    // Xóa customer
    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable int id) {
        customerService.deleteById(id);
    }

    // 3. Khóa tài khoản customer
    @PutMapping("/{id}/block")
    public String blockCustomer(@PathVariable int id) {
        customerService.blockCustomer(id);
        return "Customer account blocked";
    }

}