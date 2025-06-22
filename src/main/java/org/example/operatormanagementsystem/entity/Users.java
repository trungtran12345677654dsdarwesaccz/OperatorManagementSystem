
package org.example.operatormanagementsystem.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.example.operatormanagementsystem.enumeration.UserGender;
import org.example.operatormanagementsystem.enumeration.UserRole;
import org.example.operatormanagementsystem.enumeration.UserStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@ToString(of = {"id", "username", "email"})
public class Users  implements UserDetails { // Class name from your image

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

    @Column(name = "username", nullable = false, length = 100)
    private String username;



    @Column(name = "email", nullable = false, length = 100) // Uniqueness handled by @Table
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name ="role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private UserGender gender;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "password", nullable = false, length = 100) // Should be password_hash
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Customer customer;

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Manager manager;

    @OneToOne(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OperatorStaff operatorStaff;

    @Column(name = "last_password_reset_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime lastPasswordResetDate; // Thời gian cuối cùng mật khẩu được đặt lại thành công




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về danh sách các quyền (roles) của người dùng.
        // Dựa trên trường 'role' (enum UserRole) bạn đã có.
        // Spring Security thường yêu cầu các quyền có tiền tố "ROLE_".
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }



    @Override
    public boolean isAccountNonExpired() {
        // Trả về true nếu tài khoản không hết hạn.
        // Mặc định là true nếu bạn không có logic hết hạn tài khoản.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Trả về true nếu tài khoản không bị khóa.
        // Mặc định là true nếu bạn không có logic khóa tài khoản.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Trả về true nếu thông tin xác thực (mật khẩu) không hết hạn.
        // Mặc định là true nếu bạn không có logic hết hạn mật khẩu.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Trả về true nếu tài khoản được kích hoạt.
        // Dựa trên trường 'status' (enum UserStatus) bạn đã có.
        return this.status == UserStatus.ACTIVE;
    }

}