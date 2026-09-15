package com.passwordmanager.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupMasterPasswordRequest {

    /**
     * Master key salt generated on client side or derived using Argon2id.
     */
    @NotBlank(message = "Master key salt is required")
    private String masterKeySalt;

    /**
     * Verifier derived from master password (Argon2id hash) without exposing master password to server.
     */
    @NotBlank(message = "Master key verifier is required")
    @Size(min = 16, message = "Verifier token is too short")
    private String masterKeyVerifier;

    /**
     * Initial encrypted vault key (symmetric key encrypted with master key derived key).
     */
    @NotBlank(message = "Encrypted vault key is required")
    private String encryptedVaultKey;
}
