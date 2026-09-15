package com.passwordmanager.dto.vault;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockVaultRequest {

    /**
     * Master password verifier proof or master key derived verification token.
     */
    @NotBlank(message = "Master key verification token is required")
    private String masterKeyVerifier;
}
