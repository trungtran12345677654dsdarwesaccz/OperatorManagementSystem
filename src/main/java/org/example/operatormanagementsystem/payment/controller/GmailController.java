package org.example.operatormanagementsystem.payment.controller;

import org.example.operatormanagementsystem.config.OauthGmail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gmail")

public class GmailController {

    private final OauthGmail oauthGmail;

    public GmailController(OauthGmail oauthGmail) {
        this.oauthGmail = oauthGmail;
    }

    @GetMapping
    public ResponseEntity<List<String>> readMailIsNotRead(
            @RequestParam(value = "limit", defaultValue = "5") Long limit) {

        List<String> emails = oauthGmail.listLatestEmails(limit);

        return ResponseEntity.ok(emails);
    }
}
