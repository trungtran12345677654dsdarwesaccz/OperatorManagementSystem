package org.example.operatormanagementsystem.aichatbox_trungtran.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotBlank(message = "Question cannot be blank")
    private String question;

}

