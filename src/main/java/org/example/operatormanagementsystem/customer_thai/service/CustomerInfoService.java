package org.example.operatormanagementsystem.customer_thai.service;

import org.example.operatormanagementsystem.entity.Users;

/**
 * A dedicated service for the 'customer_thai' package to correctly retrieve
 * the currently authenticated user's information.
 */
public interface CustomerInfoService {
    /**
     * Gets the full Users object for the currently logged-in user.
     * @return The Users entity.
     * @throws RuntimeException if the user is not authenticated or not found.
     */
    Users getCurrentCustomerUser();
} 