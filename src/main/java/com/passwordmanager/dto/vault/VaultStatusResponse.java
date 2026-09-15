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
public class VaultStatusResponse {
    private boolean initialized;
    private boolean locked;
    private long totalCredentials;
    private LocalDateTime lastUnlockedAt;
}
