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

        // Load client secrets.
        var in = getClass().getClassLoader().getResourceAsStream("credentials.json");
        if (in == null) throw new RuntimeException("credentials.json not found in resources!");

        var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        var flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, List.of(GmailScopes.GMAIL_READONLY))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY_PATH).toFile()))
                .setAccessType("offline")
                .build();

        var credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        // Create Gmail service
        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
