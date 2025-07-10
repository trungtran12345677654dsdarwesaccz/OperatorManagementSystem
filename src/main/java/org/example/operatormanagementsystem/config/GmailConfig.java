package org.example.operatormanagementsystem.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.InputStreamReader;

@Configuration
public class GmailConfig {

    private static final String APPLICATION_NAME = "Gmail Payment Reader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Bean
    public Gmail gmailService() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load credentials.json
        InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream("credentials.json");
        if (credentialsStream == null) {
            throw new IllegalStateException("❌ Không tìm thấy credentials.json trong classpath");
        }

        InputStream tokenStream = getClass().getClassLoader().getResourceAsStream("tokens/StoredCredential.json");
        if (tokenStream == null) {
            throw new IllegalStateException("❌ Không tìm thấy StoredCredential.json trong classpath");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tokenNode = mapper.readTree(tokenStream);

        // Lấy client info
        String clientId = getRequiredField(tokenNode, "client_id");
        String clientSecret = getRequiredField(tokenNode, "client_secret");
        String accessToken = getRequiredField(tokenNode, "access_token");
        String refreshToken = getRequiredField(tokenNode, "refresh_token");

        // Tạo credential đúng cách
        Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
                .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                .build();

        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private String getRequiredField(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.asText().isEmpty()) {
            throw new IllegalStateException("❌ Thiếu trường bắt buộc: " + fieldName + " trong StoredCredential");
        }
        return field.asText();
    }
}
