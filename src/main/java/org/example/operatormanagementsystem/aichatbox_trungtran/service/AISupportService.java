package org.example.operatormanagementsystem.aichatbox_trungtran.service;


import org.example.operatormanagementsystem.aichatbox_trungtran.dto.request.AnswerRequest;

public interface AISupportService {
    String answerFromGemini(AnswerRequest request);
}

