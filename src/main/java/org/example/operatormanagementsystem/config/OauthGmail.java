package org.example.operatormanagementsystem.config;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OauthGmail {

    private final Gmail gmailService;

    public OauthGmail(Gmail gmailService) {
        this.gmailService = gmailService;
    }

    public List<String> listLatestEmails(long limit) {
        List<String> emailSnippets = new ArrayList<>();
        try {
            ListMessagesResponse messagesResponse = gmailService.users().messages()
                    .list("me")
                    .setMaxResults(limit)
                    .setQ("is:unread")  // Lọc email chưa đọc
                    .execute();

            if (messagesResponse.getMessages() == null) {
                return emailSnippets;
            }

            for (Message msg : messagesResponse.getMessages()) {
                // Lấy chi tiết email
                Message fullMessage = gmailService.users().messages()
                        .get("me", msg.getId())
                        .execute();

                // Lấy đoạn trích (snippet) của email
                emailSnippets.add(fullMessage.getSnippet());
            }

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lấy email từ Gmail API", e);
        }
        return emailSnippets;
    }
}
