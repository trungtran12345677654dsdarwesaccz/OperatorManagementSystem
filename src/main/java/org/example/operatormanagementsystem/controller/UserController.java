package org.example.operatormanagementsystem.controller;

import org.example.operatormanagementsystem.managercustomer.dto.request.UserCreateRequest;
import org.example.operatormanagementsystem.managercustomer.dto.request.UserUpdateRequest;
import org.example.operatormanagementsystem.dto.request.UserFilterRequest;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.dto.response.PageResponse;
import org.example.operatormanagementsystem.managercustomer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
public class UserController {

    @Autowired
    private UserService userService;

    // Lấy tất cả customer hoặc tìm kiếm theo query
    @GetMapping
    public List<UserSearchResponse> getAllUsers(
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address
    ) {
        if ((fullname == null || fullname.isEmpty()) &&
                (email == null || email.isEmpty()) &&
                (phone == null || phone.isEmpty()) &&
                (address == null || address.isEmpty())) {
            return userService.findAllUsersResponse();
        }
        return userService.advancedSearchResponse(fullname, email, phone, address);
    }

    // Lấy customer theo ID
    @GetMapping("/{id}")
    public UserSearchResponse getUserById(@PathVariable int id) {
        return userService.findUserResponseById(id);
    }

    // Tạo mới customer
    @PostMapping
    public UserSearchResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    // Cập nhật thông tin customer
    @PutMapping("/{id}")
    public UserSearchResponse updateUser(@PathVariable Integer id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    // Xóa customer
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteById(id);
    }

    //chỉnh status user
    @PutMapping("/{id}/status")
    public void changeStatus(@PathVariable int id) {
        userService.blockCustomer(id);
    }
    @GetMapping("/profile")
    public ResponseEntity<UserSearchResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserSearchResponse user = userService.findUserResponseByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    //api get all staff
    @GetMapping("/staff")
    public List<UserSearchResponse> getAllStaff() {
        return userService.findAllStaffResponse();
    }
}
