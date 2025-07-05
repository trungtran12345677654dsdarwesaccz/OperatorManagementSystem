package org.example.operatormanagementsystem.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
@Entity
@Table(name = "user_usage_stat")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserUsageStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Users user;

    private int loginCount;

    private long totalOnlineSeconds;

    private LocalDateTime lastLoginAt;

    private int apiCallsToday;

    @Column(name = "usage_date")
    private LocalDate currentDate;

}
