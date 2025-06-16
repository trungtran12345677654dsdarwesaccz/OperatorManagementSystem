package org.example.operatormanagementsystem.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email không được để trống ")
    private String email;
// @NotPlank : chuỗi (String) không được rỗng (null), không được trống ("")
// và không được chỉ chứa các ký tự khoảng trắng (ví dụ: " ").
//    @NotBlank(message = "Mật khẩu không được rỗng")
//    @Size(min = 6, message = "Mật khẩu phải từ 6 Kí tự ")
//    @Pattern(
//            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
//            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, và 1 số"
//    )
    private String password;
}
