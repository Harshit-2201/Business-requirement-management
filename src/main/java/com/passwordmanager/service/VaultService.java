package com.passwordmanager.service;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.vault.*;
import com.passwordmanager.entity.AuditEventType;
import com.passwordmanager.entity.User;
import com.passwordmanager.entity.Vault;
import com.passwordmanager.exception.BadRequestException;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.exception.UnauthorizedException;
import com.passwordmanager.repository.CredentialRepository;
import com.passwordmanager.repository.UserRepository;
import com.passwordmanager.repository.VaultRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VaultService {

    private static final Logger logger = LoggerFactory.getLogger(VaultService.class);

    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuditService auditService;

    @Transactional
    public VaultResponse createVault(Long userId, CreateVaultRequest request, HttpServletRequest httpRequest) {
        logger.info("Creating vault for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (vaultRepository.existsByUserId(userId)) {
            throw new BadRequestException("Vault already exists for this user. Use unlock or update instead.");
        }

        Vault vault = Vault.builder()
                .user(user)
                .encryptedVaultKey(request.getEncryptedVaultKey())
                .vaultSalt(request.getVaultSalt())
                .locked(false)
                .lastUnlockedAt(LocalDateTime.now())
                .build();

        Vault saved = vaultRepository.save(vault);
        auditService.logEvent(user, AuditEventType.VAULT_UNLOCKED, "Vault created and unlocked", httpRequest);

        return mapToResponse(saved);
    }

    @Transactional
    public VaultResponse unlockVault(Long userId, UnlockVaultRequest request, HttpServletRequest httpRequest) {
        logger.info("Unlocking vault for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Vault vault = vaultRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vault not found for user: " + userId));

        if (user.getMasterKeyVerifier() == null) {
            throw new BadRequestException("Master password has not been set yet. Call /auth/setup-master-password first.");
        }

        // Verify the client's master key verifier token matches the stored zero-knowledge verifier
        if (!user.getMasterKeyVerifier().equals(request.getMasterKeyVerifier())) {
            auditService.logEvent(user, AuditEventType.LOGIN_FAILED, "Vault unlock failed: Invalid master key verifier", httpRequest);
            throw new UnauthorizedException("Invalid master key verifier token. Access denied.");
        }

        vault.setLocked(false);
        vault.setLastUnlockedAt(LocalDateTime.now());
        Vault updated = vaultRepository.save(vault);

        auditService.logEvent(user, AuditEventType.VAULT_UNLOCKED, "Vault successfully unlocked", httpRequest);

        return mapToResponse(updated);
    }

    @Transactional
    public ApiResponse<String> lockVault(Long userId, HttpServletRequest httpRequest) {
        logger.info("Locking vault for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Vault vault = vaultRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vault not found for user: " + userId));

        vault.setLocked(true);
        vaultRepository.save(vault);

        return ApiResponse.success("Vault locked successfully.");
    }

    @Transactional(readOnly = true)
    public VaultStatusResponse getVaultStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Vault vault = vaultRepository.findByUserId(userId).orElse(null);
        long totalCredentials = credentialRepository.countByUserId(userId);

        if (vault == null) {
            return VaultStatusResponse.builder()
                    .initialized(false)
                    .locked(true)
                    .totalCredentials(totalCredentials)
                    .lastUnlockedAt(null)
                    .build();
        }

        return VaultStatusResponse.builder()
                .initialized(true)
                .locked(vault.isLocked())
                .totalCredentials(totalCredentials)
                .lastUnlockedAt(vault.getLastUnlockedAt())
                .build();
    }

    private VaultResponse mapToResponse(Vault vault) {
        return VaultResponse.builder()
                .id(vault.getId())
                .encryptedVaultKey(vault.getEncryptedVaultKey())
                .vaultSalt(vault.getVaultSalt())
                .isLocked(vault.isLocked())
                .lastUnlockedAt(vault.getLastUnlockedAt())
                .build();
    }
}
