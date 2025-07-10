package org.example.operatormanagementsystem.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class GmailConfig {

    private static final String APPLICATION_NAME = "Gmail Payment Reader";
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Bean
    public Gmail gmailService() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load credentials.json từ resources
        var in = getClass().getClassLoader().getResourceAsStream("credentials.json");
        if (in == null) throw new RuntimeException("credentials.json not found");
        var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Load token từ thư mục "tokens" bằng FileDataStoreFactory (phải là thư mục thật)
        var dataStoreFactory = new FileDataStoreFactory(new File("tokens"));
        var flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, List.of(GmailScopes.GMAIL_READONLY))
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();

        var credential = flow.loadCredential("user");
        if (credential == null || credential.getAccessToken() == null) {
            throw new IllegalStateException("Token không tồn tại hoặc bị lỗi. Vui lòng chạy local để tạo lại.");
        }

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


}
