package org.example.operatormanagementsystem.config;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration // Đánh dấu đây là lớp cấu hình của Spring
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig { // Hoặc tên lớp cấu hình bảo mật của bạn
    private final JwtRequestFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    // private final PasswordEncoder passwordEncoder; // Không cần tiêm PasswordEncoder vào đây nữa, vì nó sẽ được tạo trong cùng lớp
    // Constructor đã được @AllArgsConstructor tạo ra sẽ không cần PasswordEncoder nữa
    // nếu bạn định nghĩa nó là một @Bean trong cùng lớp này.


    @Bean// Đánh dấu phương thức này sẽ tạo ra một Spring Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Sử dụng BCryptPasswordEncoder
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Gọi phương thức @Bean để lấy instance
        return new ProviderManager(List.of(authProvider));
    }

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/register", "/api/auth/login", "/api/auth/me", "/api/auth/sendOTP", "/api/auth/verifyOTP", "/auth/verify-email-code",
            "/api/user/forget-password", "/api/user/reset-password", "/profiles/create/**", "/webhook/payment",
            "/api/users", "/api/users/{id}", "/api/users/{id}/status", "/api/auth/login/verify-otp", "/api/auth/sendOTP",
            "/api/auth/request-status-change", "/api/auth/manager/update-status/{email}",
            "/api/auth/manager/users-for-action", "/api/auth/manager/user-details/{email}",
            "/api/revenues", "/api/revenues/**", "/api/revenues/date-range", "/api/revenues/beneficiary/{beneficiaryId}",
            "/api/revenues/source-type/{sourceType}", "/api/revenues/booking/{bookingId}",
            "/api/revenues/total", "/api/revenues/total/**", "/api/revenues/export/excel", "/api/revenues/export/excel/**",
            "/api/auth/customer/login"
    };

    private static final String[] GET_PUBLIC_ENDPOINTS = {
            "/blogs/**", "/profiles/**", "/banner/**"
    };

    private static final String[] WHITELIST_ENDPOINTS = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/onboarding/**",
                                "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/manager/**").hasAuthority("ROLE_MANAGER")
                        .requestMatchers("/api/promotions/**").hasRole("MANAGER")
                        .requestMatchers("/api/user/**").hasRole("STAFF")
                        .requestMatchers("/api/transport-units/**").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-approvals/**").hasRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/dashboard-stats").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/historical-data").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/weekly-activity").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/manager-performance").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/status-distribution").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/approval-trends").hasAnyRole("MANAGER")
                        .requestMatchers("/api/transport-unit-analytics/performance-metrics").hasRole("MANAGER")
                        // .requestMatchers("/api/revenues/**").hasAnyAuthority("STAFF", "MANAGER") // Temporarily disabled for testing




                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",// Vite dev server
                "http://localhost:3000",    // React dev server
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174", // Alternative localhost
                "http://127.0.0.1:3000"     // Alternative localhost
        ));
//        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setExposedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

//    NGHĨA LÀ CSRF TOKEN LÀ CÁI TRÁNH BỊ GỬI REQUEST TỪ 1 TRANG WEB KHÁC KÈM TOKEN ĐĂNG NHAAPH
//    Ở WEB TỐT CÒN CORS LÀ CHI CHO PHEP NHUNG CAI TRNAG NAO DC GUI REQUEST CHO NHAU
}