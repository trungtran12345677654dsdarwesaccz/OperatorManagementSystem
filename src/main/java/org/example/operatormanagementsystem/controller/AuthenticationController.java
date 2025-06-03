package org.example.operatormanagementsystem.controller;

import jakarta.validation.Valid;
import org.example.operatormanagementsystem.dto.request.LoginRequest;
import org.example.operatormanagementsystem.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/login")
    public ResponseEntity<GenericResponse<AuthResponseLogin>> login(@Valid @RequestBody LoginRequest request) {
        GenericResponse<AuthResponseLogin> response = GenericResponse.<AuthResponseLogin>builder()
                .message(MessageDTO.builder()
                        .messageCode(MessageCodeConstant.M001_SUCCESS)
                        .messageDetail(MessageConstant.SUCCESS)
                        .build())
                .isSuccess(true)
                .data(authenticationService.login(request))
                .build();
        return ResponseEntity.ok(response);
    }
}
