package com.passwordmanager.controller;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.password.PasswordGenerateRequest;
import com.passwordmanager.dto.password.PasswordGenerateResponse;
import com.passwordmanager.entity.User;
import com.passwordmanager.repository.UserRepository;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.PasswordGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@Tag(name = "7. Password Generator", description = "Cryptographic Password Generator with Shannon entropy scoring")
public class PasswordController {

    private final PasswordGeneratorService passwordGeneratorService;
    private final UserRepository userRepository;

    @PostMapping("/generate")
    @Operation(summary = "Generate Secure Password", description = "Generates cryptographically random passwords with customizable length, character sets, and ambiguous character filtering.")
    public ResponseEntity<ApiResponse<PasswordGenerateResponse>> generate(
            @Valid @RequestBody(required = false) PasswordGenerateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {

        PasswordGenerateRequest req = request != null ? request : new PasswordGenerateRequest();
        User user = null;
        if (userPrincipal != null) {
            user = userRepository.findById(userPrincipal.getId()).orElse(null);
        }

        PasswordGenerateResponse response = passwordGeneratorService.generatePassword(req, user, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Password generated successfully", response));
    }
}
