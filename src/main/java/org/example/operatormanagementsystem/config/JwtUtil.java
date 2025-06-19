package org.example.operatormanagementsystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; // Import Decoders
import io.jsonwebtoken.security.Keys;
import org.example.operatormanagementsystem.entity.Users; // Giữ nguyên import Users entity nếu bạn cần generateToken(Users user)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
//ConcurrentHashMap.newKeySet() duoc dung de dam bao blacklistedTokens co the duoc truy cap va sua doi boi nhieu luong dong thoi mot cach an toan ma van duy tri hieu suat cao.

import java.security.Key; // Thay đổi từ javax.crypto.SecretKey sang java.security.Key
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.Set; // Import Set
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {

    @Value("${application.security.jwt.secret-key}") // Đổi tên property để rõ ràng và nhất quán
    private String SECRET_KEY;

    @Value("${application.security.jwt.expiration}") // Thêm cấu hình thời gian hết hạn
    private long jwtExpiration; // Thời gian sống của token (ví dụ: 86400000 = 24 giờ)

    @Value("${application.security.jwt.reset-password-expiration}")
    private long resetPasswordExpiration;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // Đổi tên từ getSigningKey() sang getSignKey() cho nhất quán với thư viện io.jsonwebtoken
    private Key getSignKey() {
        // SỬA: Sử dụng Decoders.BASE64.decode để xử lý secret key
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }


//    // Phương thức generateToken nhận UserDetails
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        String role = userDetails.getAuthorities().stream()
//                .findFirst()
//                .map(Object::toString)
//                .orElse("USER"); // Mặc định là USER nếu không có vai trò
//        claims.put("role", role);
//
//        return createToken(claims, userDetails.getUsername());
//    }

    // Phương thức generateToken nhận Users entity (Nếu bạn có dùng)
    // Giúp dễ dàng lấy các thông tin như email, role từ entity Users
    public String generateToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        if (user.getRole() != null) {
            claims.put("role", user.getRole().name()); // Lưu vai trò dưới dạng String
        } else {
            claims.put("role", "ROLE_USER"); // Mặc định nếu không có vai trò
        }
        if (user.getManager() != null) {
            claims.put("managerId", user.getManager().getManagerId());
        }
        return createToken(claims, user.getEmail()); // Sử dụng email làm subject
    }


    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Email hoặc Username
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // SỬA QUAN TRỌNG: LUÔN THÊM THỜI GIAN HẾT HẠN
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Sử dụng jwtExpiration
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Kiểm tra username khớp và token chưa hết hạn
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }



    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // SỬA: Bắt ngoại lệ khi trích xuất thời gian hết hạn để tránh NullPointerException
    private Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            System.err.println("DEBUG: Error extracting expiration from token: " + e.getMessage());
            return null; // Trả về null nếu không thể trích xuất (token lỗi hoặc thiếu claim)
        }
    }

    // SỬA: Xử lý trường hợp extractExpiration trả về null
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) {
            // Nếu không thể trích xuất thời gian hết hạn, coi như đã hết hạn hoặc không hợp lệ
            System.err.println("DEBUG: Token expiration is null, treating as expired/invalid.");
            return true;
        }
        return expiration.before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // Phương thức validate riêng cho token đặt lại mật khẩu
    public Boolean validateResetPasswordToken(String token, Users user) {
        final String email = extractUsername(token); // Subject của reset token là email
        // Lấy thời gian token được cấp phát
        Date issuedAt = extractClaim(token, Claims::getIssuedAt);

        // Kiểm tra loại token và email khớp
        // Kiểm tra xem claim 'type' có tồn tại và giá trị của nó có phải là "reset_password" không
        boolean isResetToken = "reset_password".equals(extractClaim(token, claims -> claims.get("type", String.class)));

        // Kiểm tra token chưa hết hạn
        boolean notExpired = !isTokenExpired(token);

        // Kiểm tra token được cấp sau lần đặt lại mật khẩu cuối cùng của người dùng
        // Nếu lastPasswordResetDate là null (chưa bao giờ reset), thì mọi token đều hợp lệ
        boolean issuedAfterLastReset = user.getLastPasswordResetDate() == null ||
                issuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .isAfter(user.getLastPasswordResetDate());


        return (email != null && email.equals(user.getEmail()) && isResetToken && notExpired && issuedAfterLastReset);
    }

    // Phương thức tạo token khôi phục mật khẩu
    public String generatePasswordResetToken(Users user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "reset_password"); // Đánh dấu đây là token đặt lại mật khẩu
        claims.put("jti", UUID.randomUUID().toString()); // Thêm JTI để mỗi token là duy nhất

        // GỌI PHƯƠNG THỨC createToken DUY NHẤT VỚI resetPasswordExpiration
        return createToken(claims, user.getEmail(), resetPasswordExpiration);
    }

    // PHƯƠNG THỨC createToken DUY NHẤT VÀ LINH HOẠT
    private String createToken(Map<String, Object> claims, String subject, long expirationTimeMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

}
