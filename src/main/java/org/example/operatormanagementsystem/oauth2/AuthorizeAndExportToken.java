package org.example.operatormanagementsystem.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// neu muon chay local chay modify run config modifu option add vm option cho nhap -Dspring.profiles.active=server or local
public class AuthorizeAndExportToken {

    public static void main(String[] args) throws Exception {
        // 1. Khởi tạo HTTP và JSON
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();

        // 2. Xác định profile (server hoặc local)
        String profile = System.getProperty("spring.profiles.active", "local").trim().toLowerCase();
        System.out.println("🔧 Đang chạy với profile: " + profile);

        // 3. Đường dẫn file credentials và stored token
        String credentialsPath = "src/main/resources/credentials"
                + (profile.equals("server") ? "-server" : "")
                + ".json";

        String storedTokenPath = "src/main/resources/tokens/StoredCredential"
                + (profile.equals("server") ? "-server" : "")
                + ".json";

        System.out.println("📄 Đang sử dụng file credentials: " + credentialsPath);
        System.out.println("💾 Token sẽ được lưu vào: " + storedTokenPath);

        // 4. Load credentials
        FileInputStream credentialsStream = new FileInputStream(credentialsPath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(credentialsStream));

        // 5. Khởi tạo flow xác thực OAuth
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                List.of("https://www.googleapis.com/auth/gmail.readonly")
        ).setAccessType("offline").build();

        // 6. Mở trình duyệt local để xác thực
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // 7. Tạo file StoredCredential
        Map<String, Object> tokenJson = new HashMap<>();
        tokenJson.put("access_token", credential.getAccessToken());
        tokenJson.put("refresh_token", credential.getRefreshToken());
        tokenJson.put("client_id", clientSecrets.getDetails().getClientId());
        tokenJson.put("client_secret", clientSecrets.getDetails().getClientSecret());
        tokenJson.put("type", "authorized_user");

        // 8. Ghi ra file
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(storedTokenPath), tokenJson);

        System.out.println("✅ Token đã được export thành công!");
    }
}
