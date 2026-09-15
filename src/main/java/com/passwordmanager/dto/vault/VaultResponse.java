package com.passwordmanager.dto.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultResponse {
    private Long id;
    private String encryptedVaultKey;
    private String vaultSalt;
    private boolean isLocked;
    private LocalDateTime lastUnlockedAt;
}
