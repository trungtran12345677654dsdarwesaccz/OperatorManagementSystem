package com.swp.api.controller;

import com.swp.api.model.Customer;
import com.swp.api.service.CustomerService;
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
    public Optional<Customer> getCustomerById(@PathVariable Long id) {
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
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteById(id);
    }

    // 2. Cập nhật mật khẩu customer
    @PutMapping("/{id}/password")
    public String updateCustomerPassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request) {
        // Tạo đối tượng Customer với id và password mới
        Customer customer = new Customer();
        customer.setCustomerId(id.intValue());
        customer.setPassword(request.getNewPassword());
        customerService.updateUserPassword(customer);
        return "Password updated successfully";
    }

    // 3. Khóa tài khoản customer
    @PutMapping("/{id}/block")
    public String blockCustomer(@PathVariable Long id) {
        customerService.blockCustomer(id);
        return "Customer account blocked";
    }

    // Request body cho cập nhật mật khẩu
    public static class PasswordUpdateRequest {
        private String newPassword;
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
