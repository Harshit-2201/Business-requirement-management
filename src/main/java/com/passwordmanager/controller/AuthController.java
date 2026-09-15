package com.passwordmanager.controller;

import com.passwordmanager.dto.auth.*;
import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication Module", description = "User registration, authentication, master password setup and password rotation")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Registers a new user and returns JWT access token.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Register request for user: {}", request.getUsername());
        AuthResponse response = authService.register(request, httpRequest);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login User", description = "Authenticates user credentials and returns JWT access token.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Login request for: {}", request.getUsernameOrEmail());
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/setup-master-password")
    @Operation(summary = "Master Password Setup", description = "Zero-Knowledge setup: Stores masterKeySalt, verifier hash, and encrypted vault key.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<String>> setupMasterPassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SetupMasterPasswordRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Master password setup for user ID: {}", userPrincipal.getId());
        ApiResponse<String> response = authService.setupMasterPassword(userPrincipal.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Changes account password and optionally updates master key verifier.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Change password request for user ID: {}", userPrincipal.getId());
        ApiResponse<String> response = authService.changePassword(userPrincipal.getId(), request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
