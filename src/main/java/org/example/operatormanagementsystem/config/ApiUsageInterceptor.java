package org.example.operatormanagementsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.entity.UserUsageStat;
import org.example.operatormanagementsystem.repository.UserRepository;
import org.example.operatormanagementsystem.repository.UserUsageStatRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ApiUsageInterceptor implements HandlerInterceptor {

    private final UserUsageStatRepository usageStatRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = jwtUtil.resolveToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractUsername(token);
            userRepository.findByEmail(email).ifPresent(user -> {
                UserUsageStat stat = usageStatRepository.findByUser(user)
                        .orElseGet(() -> {
                            UserUsageStat s = new UserUsageStat();
                            s.setUser(user);
                            s.setCurrentDate(LocalDate.now());
                            return s;
                        });

                // Nếu sang ngày mới thì reset apiCallsToday
                if (!LocalDate.now().equals(stat.getCurrentDate())) {
                    stat.setApiCallsToday(0);
                    stat.setCurrentDate(LocalDate.now());
                }

                stat.setApiCallsToday(stat.getApiCallsToday() + 1);
                usageStatRepository.save(stat);
            });
        }
        return true;
    }
}
