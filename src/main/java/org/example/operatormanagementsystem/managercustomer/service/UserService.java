package org.example.operatormanagementsystem.managercustomer.service;

import org.example.operatormanagementsystem.base.BaseService;
import org.example.operatormanagementsystem.dto.request.UserFilterRequest;
import org.example.operatormanagementsystem.dto.response.PageResponse;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.managercustomer.dto.request.UserCreateRequest;
import org.example.operatormanagementsystem.managercustomer.dto.request.UserUpdateRequest;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;

import java.util.List;

public interface UserService extends BaseService<Users, Integer> {
    List<Users> searchUsers(String query);
    void blockCustomer(int customerId);
    List<Users> advancedSearch(String fullname, String email, String phone, String address);

    // New DTO methods
    List<UserSearchResponse> findAllUsersResponse();
    List<UserSearchResponse> advancedSearchResponse(String fullname, String email, String phone, String address);
    UserSearchResponse findUserResponseById(int id);
    UserSearchResponse createUser(UserCreateRequest request);
    UserSearchResponse updateUser(Integer id, UserUpdateRequest request);
    UserSearchResponse findUserResponseByEmail(String email);
    Users findUsersResponseByEmail(String email);
    PageResponse<UserSearchResponse> getUsersWithFilters(UserFilterRequest filterRequest);
    List<UserSearchResponse> findAllStaffResponse();
    List<UserSearchResponse> findAllCustomersManagedByStaff(String staffEmail);
    List<UserSearchResponse> advancedSearchCustomersManagedByStaff(String staffEmail, String fullname, String email, String phone, String address);
}