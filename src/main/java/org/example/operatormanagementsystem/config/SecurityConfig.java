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

    /**
     * Cấu hình và cung cấp một bean PasswordEncoder.
     * Spring Security sẽ sử dụng bean này để mã hóa và kiểm tra mật khẩu.
     * BCryptPasswordEncoder là một lựa chọn phổ biến và an toàn.
     * @return Một instance của PasswordEncoder (BCryptPasswordEncoder).
     */
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
            "/api/user/forgot-password","/api/user/reset-password","/profiles/create/**", "/webhook/payment",
            "/api/users", "/api/users/{id}", "/api/users/{id}/status", "/api/auth/login/verify-otp","/api/auth/sendOTP",
            "/api/auth/request-status-change","/api/auth/manager/update-status/{email}",
            "/api/auth/manager/users-for-action",  "/api/auth/manager/user-details/{email}","/api/promotions/**"


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
                        // Cho phép các endpoint trong danh sách trắng (Swagger, API docs)
                        .requestMatchers(WHITELIST_ENDPOINTS).permitAll()
                        // Cho phép các endpoint công khai đã xác định
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Cho phép các POST method công khai cụ thể
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/manager/**").hasAuthority("ROLE_MANAGER")
                        .requestMatchers("/api/promotions/**").hasRole("MANAGER")

                        // Bất kỳ request nào khác đều phải được xác thực
                        // Và sau khi xác thực, @PreAuthorize sẽ kiểm tra vai trò
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5175",    // Vite dev server
                "http://localhost:3000",    // React dev server
                "http://127.0.0.1:5175",    // Alternative localhost
                "http://127.0.0.1:3000"     // Alternative localhost
        ));
//        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setExposedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    // --- THÊM BEAN NÀY VÀO ĐÂY ---
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Đặt tiền tố mặc định là chuỗi rỗng
    }
}
