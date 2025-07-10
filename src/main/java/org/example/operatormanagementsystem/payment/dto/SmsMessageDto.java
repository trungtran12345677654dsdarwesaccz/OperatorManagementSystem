    package org.example.operatormanagementsystem.payment.dto;
    
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class SmsMessageDto {
        private String sender;
        private String message;
        private String timestamp;
    }