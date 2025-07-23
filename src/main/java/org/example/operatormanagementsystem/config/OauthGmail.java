package org.example.operatormanagementsystem.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OauthGmail {

    private static final Logger logger = LoggerFactory.getLogger(OauthGmail.class);

    private com.google.api.client.http.HttpTransport transport;
    private GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.refresh.token}")
    private String refreshToken;

    private com.google.api.client.http.HttpTransport getTransport() {
        if (transport == null) {
            try {
                transport = GoogleNetHttpTransport.newTrustedTransport();
            } catch (GeneralSecurityException | java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
        return transport;
    }

    public Gmail getGmailService() {
        try {
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(getTransport())
                    .setJsonFactory(jsonFactory)
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setRefreshToken(refreshToken);
            credential.refreshToken();

            return new Gmail.Builder(getTransport(), jsonFactory, credential)
                    .setApplicationName("Gmail Java AutoAuth")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> listLatestEmails(long limit) {
        try {
            Gmail service = getGmailService();

            List<Message> messages = Optional.ofNullable(
                            service.users().messages().list("me")
                                    .setMaxResults(limit)
                                    .setQ("is:read [SMSFW] New text message from 'MBBANK'")
                                    .execute()
                                    .getMessages())
                    .orElse(List.of());

            return messages.stream().map(msg -> {
                try {
                    Message fullMessage = service.users().messages()
                            .get("me", msg.getId())
                            .setFormat("full")
                            .execute();



                    List<MessagePartHeader> headers = fullMessage.getPayload().getHeaders();
                    String subject = headers.stream()
                            .filter(h -> "Subject".equalsIgnoreCase(h.getName()))
                            .findFirst()
                            .map(h -> h.getValue())
                            .orElse(null);

                    String from = headers.stream()
                            .filter(h -> "From".equalsIgnoreCase(h.getName()))
                            .findFirst()
                            .map(h -> h.getValue())
                            .orElse("(No From)");

                    String bodyText = getBodyFromMessage(fullMessage);

                    return " " + (subject != null ? subject : "") + "\n " + from + "\n Nội dung:\n" + bodyText;

                } catch (Exception e) {
                    return null;
                }
            }).filter(s -> s != null).collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    private String getBodyFromMessage(Message message) {
        List<MessagePart> parts = message.getPayload().getParts();

        if (parts == null || parts.isEmpty()) {
            return decodeBase64(message.getPayload().getBody().getData());
        }

        MessagePart part = parts.stream()
                .filter(p -> "text/plain".equalsIgnoreCase(p.getMimeType()))
                .findFirst()
                .orElse(parts.get(0));

        MessagePartBody body = part.getBody();
        return decodeBase64(body.getData());
    }

    private String decodeBase64(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "(Không đọc được nội dung)";
        }
    }
}
