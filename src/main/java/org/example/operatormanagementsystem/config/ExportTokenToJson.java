package org.example.operatormanagementsystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExportTokenToJson {

    public static void export(GoogleAuthorizationCodeFlow flow, GoogleClientSecrets clientSecrets) throws Exception {
        Credential credential = flow.loadCredential("user");
        if (credential == null) {
            throw new IllegalStateException("No stored credential found for user");
        }

        Map<String, Object> tokenJson = new HashMap<>();
        tokenJson.put("access_token", credential.getAccessToken());
        tokenJson.put("refresh_token", credential.getRefreshToken());
        tokenJson.put("client_id", clientSecrets.getDetails().getClientId());
        tokenJson.put("client_secret", clientSecrets.getDetails().getClientSecret());
        tokenJson.put("type", "authorized_user");

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("StoredCredential.json"), tokenJson);

        System.out.println("✅ Token đã được xuất ra StoredCredential.json");
    }
}
