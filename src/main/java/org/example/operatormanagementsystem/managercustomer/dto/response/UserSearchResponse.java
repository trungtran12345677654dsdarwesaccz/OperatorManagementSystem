package org.example.operatormanagementsystem.managercustomer.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {

    private Integer id;
    
    private String fullName;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    private String role;
    
    private String gender;
    
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
} 