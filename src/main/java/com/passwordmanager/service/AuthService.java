package com.passwordmanager.service;

import com.passwordmanager.dto.auth.*;
import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.entity.AuditEventType;
import com.passwordmanager.entity.User;
import com.passwordmanager.entity.Vault;
import com.passwordmanager.exception.BadRequestException;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.repository.UserRepository;
import com.passwordmanager.repository.VaultRepository;
import com.passwordmanager.security.JwtTokenProvider;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.util.Argon2Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final VaultRepository vaultRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final Argon2Util argon2Util;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        logger.info("Registering user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        // Generate client-side salt for zero-knowledge key derivation
        String masterKeySalt = argon2Util.generateSalt();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .masterKeySalt(masterKeySalt)
                .masterPasswordSet(false)
                .role("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token for immediate access
        String token = tokenProvider.generateTokenFromUserId(savedUser.getId(), savedUser.getUsername());

        auditService.logEvent(savedUser, AuditEventType.LOGIN, "User registered and logged in", httpRequest);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .isMasterPasswordSet(savedUser.isMasterPasswordSet())
                .masterKeySalt(savedUser.getMasterKeySalt())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        logger.info("Login attempt for: {}", request.getUsernameOrEmail());

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            auditService.logEvent(user, AuditEventType.LOGIN, "Successful login", httpRequest);

            return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isMasterPasswordSet(user.isMasterPasswordSet())
                .masterKeySalt(user.getMasterKeySalt())
                .build();

        } catch (BadCredentialsException ex) {
            auditService.logEvent(user, AuditEventType.LOGIN_FAILED, "Failed login attempt: invalid credentials", httpRequest);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public ApiResponse<String> setupMasterPassword(Long userId, SetupMasterPasswordRequest request, HttpServletRequest httpRequest) {
        logger.info("Setting up master password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setMasterKeySalt(request.getMasterKeySalt());
        user.setMasterKeyVerifier(request.getMasterKeyVerifier());
        user.setMasterPasswordSet(true);
        userRepository.save(user);

        // Initialize or update vault
        Vault vault = vaultRepository.findByUserId(userId).orElse(
                Vault.builder()
                        .user(user)
                        .vaultSalt(request.getMasterKeySalt())
                        .encryptedVaultKey(request.getEncryptedVaultKey())
                        .locked(false) // immediately unlock upon setup
                        .build()
        );

        vault.setEncryptedVaultKey(request.getEncryptedVaultKey());
        vault.setVaultSalt(request.getMasterKeySalt());
        vault.setLocked(false);
        vaultRepository.save(vault);

        auditService.logEvent(user, AuditEventType.VAULT_UNLOCKED, "Master password configured and vault initialized", httpRequest);

        return ApiResponse.success("Master password successfully configured.");
    }

    @Transactional
    public ApiResponse<String> changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        logger.info("Changing login/master password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        if (request.getNewMasterKeySalt() != null && request.getNewMasterKeyVerifier() != null) {
            user.setMasterKeySalt(request.getNewMasterKeySalt());
            user.setMasterKeyVerifier(request.getNewMasterKeyVerifier());
        }

        userRepository.save(user);

        if (request.getNewEncryptedVaultKey() != null) {
            vaultRepository.findByUserId(userId).ifPresent(vault -> {
                vault.setEncryptedVaultKey(request.getNewEncryptedVaultKey());
                if (request.getNewMasterKeySalt() != null) {
                    vault.setVaultSalt(request.getNewMasterKeySalt());
                }
                vaultRepository.save(vault);
            });
        }

        auditService.logEvent(user, AuditEventType.LOGIN, "Account password updated successfully", httpRequest);

        return ApiResponse.success("Password changed successfully.");
    }
}
