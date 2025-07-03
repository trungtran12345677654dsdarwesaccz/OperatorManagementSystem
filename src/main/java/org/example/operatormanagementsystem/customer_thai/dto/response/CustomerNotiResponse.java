package org.example.operatormanagementsystem.customer_thai.dto.response;

import lombok.Data;
import java.util.Date;

@Data
public class CustomerNotiResponse {
    private Long id;
    private String title;
    private String content;
    private Date createdAt;
    private Boolean read;
} 