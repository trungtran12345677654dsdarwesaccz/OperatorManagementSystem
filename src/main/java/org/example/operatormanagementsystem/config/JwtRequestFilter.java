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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;


import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

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
                        path.equals("/api/auth/customer/login") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/swagger") ||
                        path.startsWith("/api/onboarding") ||
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
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                String role = jwtUtil.extractClaim(jwt, claims -> claims.get("role", String.class));
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("🔐 JWT Filter set authentication:");
                System.out.println("User: " + username);
                System.out.println("Role: " + role);
                System.out.println("Authorities: " + authorities);
                System.out.println("URI: " + path);
                System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication());
            } else {
                System.out.println("DEBUG: Invalid token for user: " + username + " at path: " + path);
            }
        }


        chain.doFilter(request, response);
    }

}