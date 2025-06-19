package org.example.operatormanagementsystem.managestaff_yen.dto.response;


public class PromotionResponse {
    private Long id;
    private String status;
    private String message;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}