package org.example.operatormanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "Reset token cannot be empty")
    private String token;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Size(max = 72, message = "Password cannot be more than 72 characters") // Giới hạn độ dài mật khẩu để tránh BCrypt lỗi
    private String newPassword;
}
