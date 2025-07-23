package org.example.operatormanagementsystem.managercustomer.service;
import org.example.operatormanagementsystem.base.BaseRepository;
import org.example.operatormanagementsystem.base.BaseServiceImpl;
import org.example.operatormanagementsystem.managercustomer.repository.ManagerUserRepository;
import org.example.operatormanagementsystem.managercustomer.dto.request.UserCreateRequest;
import org.example.operatormanagementsystem.managercustomer.dto.request.UserUpdateRequest;
import org.example.operatormanagementsystem.dto.request.UserFilterRequest;
import org.example.operatormanagementsystem.managercustomer.dto.response.UserSearchResponse;
import org.example.operatormanagementsystem.dto.response.PageResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.example.operatormanagementsystem.entity.Users;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserGender;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends BaseServiceImpl<Users, Integer> implements UserService {

    private final ManagerUserRepository managerUserRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(BaseRepository<Users, Integer> repository, ManagerUserRepository managerUserRepository) {
        super(repository);
        this.managerUserRepository = managerUserRepository;
    }

    /**
     * Find by name email or address
     */
    public List<Users> advancedSearch(String full_name, String email, String phone, String address) {
        // Triển khai logic tìm kiếm ở đây
        return managerUserRepository.findByFullNameContainingIgnoreCaseAndEmailContainingIgnoreCaseAndPhoneContainingIgnoreCaseAndAddressContainingIgnoreCase(
                full_name != null ? full_name : "",
                email != null ? email : "",
                phone != null ? phone : "",
                address != null ? address : ""
        );
    }

    /**
     * Search customers by name or email (case-insensitive)
     */
    @Override
    public List<Users> searchUsers(String query) {
        return managerUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    /**
     * Block customer account (set status to "BLOCKED")
     */
    @Override
    public void blockCustomer(int customerId) {
        Optional<Users> user = managerUserRepository.findById(customerId);
        if (user.isPresent()) {
            Users userblock = user.get();
            userblock.setStatus(UserStatus.INACTIVE);
            managerUserRepository.save(userblock);
        } else {
            throw new RuntimeException("Customer not found");
        }
    }

    // New DTO methods implementation
    @Override
    public List<UserSearchResponse> findAllUsersResponse() {
        List<Users> users = findAll();
        return users.stream()
                .map(this::convertToUserSearchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResponse> advancedSearchResponse(String fullname, String email, String phone, String address) {
        List<Users> users = advancedSearch(fullname, email, phone, address);
        return users.stream()
                .map(this::convertToUserSearchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserSearchResponse findUserResponseById(int id) {
        Optional<Users> user = findById(id);
        if (user.isPresent()) {
            return convertToUserSearchResponse(user.get());
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    @Override
    public UserSearchResponse createUser(UserCreateRequest request) {
        Users user = Users.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(UserRole.valueOf(request.getRole()))
                .gender(request.getGender() != null ? UserGender.valueOf(request.getGender()) : null)
                .password(passwordEncoder != null ? passwordEncoder.encode(request.getPassword()) : request.getPassword())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Users savedUser = save(user);
        return convertToUserSearchResponse(savedUser);
    }

    @Override
    public UserSearchResponse updateUser(Integer id, UserUpdateRequest request) {
        Optional<Users> existingUserOpt = findById(id);
        if (!existingUserOpt.isPresent()) {
            throw new RuntimeException("User not found with id: " + id);
        }

        Users existingUser = existingUserOpt.get();

        // Update only non-null fields
        if (request.getFullName() != null) {
            existingUser.setFullName(request.getFullName());
        }
        if (request.getUsername() != null) {
            existingUser.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            existingUser.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            existingUser.setAddress(request.getAddress());
        }
        if (request.getRole() != null) {
            existingUser.setRole(UserRole.valueOf(request.getRole()));
        }
        if (request.getGender() != null) {
            existingUser.setGender(UserGender.valueOf(request.getGender()));
        }

        Users updatedUser = save(existingUser);
        return convertToUserSearchResponse(updatedUser);
    }

    private UserSearchResponse convertToUserSearchResponse(Users user) {
        return UserSearchResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
    @Override
    public PageResponse<UserSearchResponse> getUsersWithFilters(UserFilterRequest filterRequest) {
        // Create pageable with sorting
        Sort sort = Sort.by(
                filterRequest.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                filterRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                sort
        );

        // Convert string filters to enums
        UserRole role = null;
        UserGender gender = null;
        UserStatus status = null;

        if (filterRequest.getRole() != null && !filterRequest.getRole().isEmpty()) {
            try {
                role = UserRole.valueOf(filterRequest.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid role, will be ignored
            }
        }

        if (filterRequest.getGender() != null && !filterRequest.getGender().isEmpty()) {
            try {
                gender = UserGender.valueOf(filterRequest.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid gender, will be ignored
            }
        }

        if (filterRequest.getStatus() != null && !filterRequest.getStatus().isEmpty()) {
            try {
                status = UserStatus.valueOf(filterRequest.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, will be ignored
            }
        }

        // Get filtered and paginated data
        Page<Users> userPage = managerUserRepository.findByFilters(
                filterRequest.getFullName(),
                filterRequest.getEmail(),
                filterRequest.getPhone(),
                filterRequest.getAddress(),
                role,
                gender,
                status,
                pageable
        );

        // Convert to response DTOs
        List<UserSearchResponse> userResponses = userPage.getContent().stream()
                .map(this::convertToUserSearchResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                userResponses,
                filterRequest.getPage(),
                filterRequest.getSize(),
                userPage.getTotalElements()
        );
    }

    @Override
    public UserSearchResponse findUserResponseByEmail(String email) {
        Users user = managerUserRepository.findByEmail(email);
        if (user == null) return null;
        return convertToUserSearchResponse(user);
    }
    @Override
    public Users findUsersResponseByEmail(String email) {
        Users user = managerUserRepository.findByEmail(email);
        if (user == null) return null;
        return user;
    }

    @Override
    public List<UserSearchResponse> findAllStaffResponse() {
        List<Users> users = managerUserRepository.findByRole(UserRole.STAFF);
        return users.stream()
                .map(this::convertToUserSearchResponse)
                .collect(Collectors.toList());
    }
}
