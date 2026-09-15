package com.passwordmanager.controller;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.vault.*;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.VaultService;
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
@RequestMapping("/vault")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "2. Vault Module", description = "Zero-Knowledge vault creation, unlocking, locking, and status inspection")
public class VaultController {

    private static final Logger logger = LoggerFactory.getLogger(VaultController.class);
    private final VaultService vaultService;

    @PostMapping("/create")
    @Operation(summary = "Create Vault", description = "Creates a new zero-knowledge encrypted vault for the user.")
    public ResponseEntity<ApiResponse<VaultResponse>> createVault(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateVaultRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Create vault for user ID: {}", userPrincipal.getId());
        VaultResponse response = vaultService.createVault(userPrincipal.getId(), request, httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Vault created successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/unlock")
    @Operation(summary = "Unlock Vault", description = "Unlocks the vault after validating the client's master key verifier token.")
    public ResponseEntity<ApiResponse<VaultResponse>> unlockVault(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UnlockVaultRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Unlock vault attempt for user ID: {}", userPrincipal.getId());
        VaultResponse response = vaultService.unlockVault(userPrincipal.getId(), request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Vault unlocked successfully", response));
    }

    @PostMapping("/lock")
    @Operation(summary = "Lock Vault", description = "Locks the vault immediately and invalidates current session unlock state.")
    public ResponseEntity<ApiResponse<String>> lockVault(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {
        logger.info("REST: Lock vault request for user ID: {}", userPrincipal.getId());
        ApiResponse<String> response = vaultService.lockVault(userPrincipal.getId(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Vault Status", description = "Retrieves the current initialization, lock status, and item counts.")
    public ResponseEntity<ApiResponse<VaultStatusResponse>> getVaultStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        VaultStatusResponse response = vaultService.getVaultStatus(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Vault status retrieved", response));
    }
}
