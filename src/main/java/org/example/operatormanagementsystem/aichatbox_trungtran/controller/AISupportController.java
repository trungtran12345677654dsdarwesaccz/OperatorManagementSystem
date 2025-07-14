package org.example.operatormanagementsystem.aichatbox_trungtran.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.operatormanagementsystem.aichatbox_trungtran.dto.request.AnswerRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.example.operatormanagementsystem.aichatbox_trungtran.service.AISupportService;
@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER')")
public class AISupportController {
    private final AISupportService aiSupportService;


        @PostMapping("/ask")
        public ResponseEntity<String> askQuestion(@Valid @RequestBody AnswerRequest request) {
            try {
                String answer = aiSupportService.answerFromGemini(request);
                return ResponseEntity.ok(answer);
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi xử lý AI: " + e.getMessage());
            }
        }
}


