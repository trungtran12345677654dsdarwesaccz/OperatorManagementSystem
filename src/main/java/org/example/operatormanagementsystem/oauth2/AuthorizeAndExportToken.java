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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorizeAndExportToken {
    public static void main(String[] args) throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();

        // Đọc credentials
        InputStream credentialsStream = new FileInputStream("src/main/resources/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(credentialsStream));

        // Khởi tạo flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                List.of("https://www.googleapis.com/auth/gmail.readonly")
        ).setAccessType("offline").build();

        // Mở trình duyệt để người dùng xác thực
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Tạo file StoredCredential.json
        Map<String, Object> tokenJson = new HashMap<>();
        tokenJson.put("access_token", credential.getAccessToken());
        tokenJson.put("refresh_token", credential.getRefreshToken());
        tokenJson.put("client_id", clientSecrets.getDetails().getClientId());
        tokenJson.put("client_secret", clientSecrets.getDetails().getClientSecret());
        tokenJson.put("type", "authorized_user");

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(
                new File("src/main/resources/tokens/StoredCredential.json"), tokenJson
        );

        System.out.println("✅ Đã export token thành công!");
    }
}
