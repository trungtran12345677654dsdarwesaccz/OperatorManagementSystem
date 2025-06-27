    package org.example.operatormanagementsystem.customer_thai.controller;

    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.example.operatormanagementsystem.customer_thai.dto.request.LoginRequest;
    import org.example.operatormanagementsystem.customer_thai.dto.response.LoginResponse;
    import org.example.operatormanagementsystem.customer_thai.service.CustomerAuthService;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/api/auth/customer")
    @RequiredArgsConstructor
    public class CustomerAuthController {

        @Qualifier("customerAuthServiceImpl_thai")
        private final CustomerAuthService customerAuthService;

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
            LoginResponse response = customerAuthService.login(request);
            return ResponseEntity.ok(response);
        }
    }
