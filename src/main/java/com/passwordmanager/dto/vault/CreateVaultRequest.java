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
public class CreateVaultRequest {

    @NotBlank(message = "Encrypted vault key is required")
    private String encryptedVaultKey;

    @NotBlank(message = "Vault salt is required")
    private String vaultSalt;
}
