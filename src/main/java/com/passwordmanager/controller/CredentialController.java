package com.passwordmanager.controller;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.credential.CredentialRequest;
import com.passwordmanager.dto.credential.CredentialResponse;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.CredentialService;
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

import java.util.List;

@RestController
@RequestMapping("/credentials")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "3. Credential Module", description = "Full CRUD operations for encrypted credentials (Website, Database, API Keys, Cloud, Server, Secure Notes)")
public class CredentialController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialController.class);
    private final CredentialService credentialService;

    @PostMapping
    @Operation(summary = "Create Credential", description = "Stores an encrypted credential item with associated folder and tags.")
    public ResponseEntity<ApiResponse<CredentialResponse>> createCredential(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CredentialRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Create credential '{}' for user ID: {}", request.getName(), userPrincipal.getId());
        CredentialResponse response = credentialService.createCredential(userPrincipal.getId(), request, httpRequest);
        return new ResponseEntity<>(ApiResponse.success("Credential created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get All Credentials", description = "Retrieves all encrypted credentials belonging to the authenticated user.")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> getAllCredentials(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CredentialResponse> list = credentialService.getAllCredentials(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Credentials retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Credential By ID", description = "Fetches a specific credential, updates lastUsed timestamp, and logs PASSWORD_VIEWED audit event.")
    public ResponseEntity<ApiResponse<CredentialResponse>> getCredentialById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        CredentialResponse response = credentialService.getCredentialById(userPrincipal.getId(), id, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Credential retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Credential", description = "Updates an existing encrypted credential item.")
    public ResponseEntity<ApiResponse<CredentialResponse>> updateCredential(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody CredentialRequest request,
            HttpServletRequest httpRequest) {
        logger.info("REST: Update credential ID {} for user ID: {}", id, userPrincipal.getId());
        CredentialResponse response = credentialService.updateCredential(userPrincipal.getId(), id, request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Credential updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Credential", description = "Permanently removes a credential item from the vault.")
    public ResponseEntity<ApiResponse<String>> deleteCredential(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        logger.info("REST: Delete credential ID {} for user ID: {}", id, userPrincipal.getId());
        ApiResponse<String> response = credentialService.deleteCredential(userPrincipal.getId(), id, httpRequest);
        return ResponseEntity.ok(response);
    }
}
