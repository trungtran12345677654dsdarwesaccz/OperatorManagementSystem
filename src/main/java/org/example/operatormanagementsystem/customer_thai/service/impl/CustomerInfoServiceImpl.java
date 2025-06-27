package org.example.operatormanagementsystem.customer_thai.service.impl;

import org.example.operatormanagementsystem.customer_thai.service.CustomerInfoService;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Implementation of CustomerInfoService that correctly retrieves the current user
 * by using the email from the security context instead of username.
 */
@Service
public class CustomerInfoServiceImpl implements CustomerInfoService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Users getCurrentCustomerUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();
        
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("User email not found in security context");
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Could not find customer profile for the current user: " + email));

        return user;
    }
} 