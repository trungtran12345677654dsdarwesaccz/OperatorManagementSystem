package org.example.operatormanagementsystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private StaffDetailService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        //  CHẶN FILTER đối với các endpoint công khai
        if (
                path.equals("/api/auth/login") ||
                        path.equals("/api/auth/sendOTP") ||
                        path.equals("/api/auth/login/verify-otp") ||
                        path.equals("/api/auth/request-status-change") ||
                        path.equals("/api/auth/register") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/swagger") ||
                        path.startsWith("/webjars")
        ) {
            chain.doFilter(request, response); // bỏ qua filter, chuyển tiếp request
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }


        if (jwt != null && jwtUtil.isTokenBlacklisted(jwt)) {
            System.out.println("DEBUG: Blacklisted token detected for path: " + path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401 Unauthorized
            response.setContentType("application/json");
            // Tùy chọn: Thêm message body để client dễ debug
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token has been invalidated (logged out).\"}");
            return; // Ngừng xử lý request
        }

        // 3. Tiếp tục quy trình xác thực nếu token chưa bị blacklist
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // Phương thức validateToken() trong JwtUtil giờ đã bao gồm kiểm tra blacklist
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                // Nếu validateToken trả về false (do hết hạn, chữ ký sai, hoặc bất kỳ lý do nào khác ngoài blacklist)
                // In ra lỗi và có thể gửi 401 Unauthorized nếu cần
                System.out.println("DEBUG: Token validation failed for user: " + username + " for path: " + path);
                // Bạn có thể chọn gửi lỗi 401 ở đây nếu muốn chặt chẽ hơn.
                // Tuy nhiên, thường thì nếu không set Authentication, Spring Security
                // sẽ tự động trả về 403 Forbidden hoặc 401 Unauthorized sau đó.
            }
        }

        chain.doFilter(request, response);
    }

}
