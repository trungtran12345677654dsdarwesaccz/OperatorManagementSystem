package org.example.operatormanagementsystem.service;

import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.example.operatormanagementsystem.entity.Users;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserUtilService {
    private final UserRepository userRepository;

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    public Users getCurrentUser() {
        String username = getCurrentUsername();
        if (username != null) {
            Optional<Users> user = userRepository.findByUsername(username);
            return user.orElse(null);
        }
        return null;
    }

    public Integer getIdCurrentUser() {
        String username = getCurrentUsername();
        if (username != null) {
            Optional<Users> user = userRepository.findById(getCurrentUser().getId());
            return user.get().getId();
        }
        return null;
    }
}
