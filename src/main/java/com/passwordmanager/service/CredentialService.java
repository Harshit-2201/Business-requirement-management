package com.passwordmanager.service;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.credential.CredentialRequest;
import com.passwordmanager.dto.credential.CredentialResponse;
import com.passwordmanager.entity.*;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.repository.CredentialRepository;
import com.passwordmanager.repository.FolderRepository;
import com.passwordmanager.repository.TagRepository;
import com.passwordmanager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialService.class);

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final TagRepository tagRepository;
    private final FolderService folderService;
    private final TagService tagService;
    private final AuditService auditService;

    @Transactional
    public CredentialResponse createCredential(Long userId, CredentialRequest request, HttpServletRequest httpRequest) {
        logger.info("Creating credential '{}' for user {}", request.getName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findByIdAndUserId(request.getFolderId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + request.getFolderId()));
        }

        Set<Tag> tags = resolveOrCreateTags(userId, user, request.getTagNames());

        Credential credential = Credential.builder()
                .user(user)
                .folder(folder)
                .name(request.getName())
                .type(request.getType())
                .url(request.getUrl())
                .username(request.getUsername())
                .encryptedPassword(request.getEncryptedPassword())
                .notes(request.getNotes())
                .favorite(Boolean.TRUE.equals(request.getFavorite()))
                .tags(tags)
                .passwordUpdated(LocalDateTime.now())
                .build();

        Credential saved = credentialRepository.save(credential);

        auditService.logEvent(user, AuditEventType.CREDENTIAL_CREATED,
                "Created credential: " + saved.getName() + " (" + saved.getType() + ")", httpRequest);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> getAllCredentials(Long userId) {
        return credentialRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CredentialResponse getCredentialById(Long userId, Long id, HttpServletRequest httpRequest) {
        Credential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with id: " + id));

        credential.setLastUsed(LocalDateTime.now());
        credentialRepository.save(credential);

        auditService.logEvent(credential.getUser(), AuditEventType.PASSWORD_VIEWED,
                "Viewed credential: " + credential.getName(), httpRequest);

        return mapToResponse(credential);
    }

    @Transactional
    public CredentialResponse updateCredential(Long userId, Long id, CredentialRequest request, HttpServletRequest httpRequest) {
        logger.info("Updating credential ID {} for user {}", id, userId);

        Credential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with id: " + id));

        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findByIdAndUserId(request.getFolderId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + request.getFolderId()));
        }

        Set<Tag> tags = resolveOrCreateTags(userId, credential.getUser(), request.getTagNames());

        credential.setName(request.getName());
        credential.setType(request.getType());
        credential.setUrl(request.getUrl());
        credential.setUsername(request.getUsername());
        credential.setNotes(request.getNotes());
        credential.setFolder(folder);
        credential.setTags(tags);
        if (request.getFavorite() != null) {
            credential.setFavorite(request.getFavorite());
        }

        // If encrypted password changed, update passwordUpdated timestamp
        if (request.getEncryptedPassword() != null && !request.getEncryptedPassword().equals(credential.getEncryptedPassword())) {
            credential.setEncryptedPassword(request.getEncryptedPassword());
            credential.setPasswordUpdated(LocalDateTime.now());
        }

        Credential updated = credentialRepository.save(credential);

        auditService.logEvent(credential.getUser(), AuditEventType.CREDENTIAL_UPDATED,
                "Updated credential: " + updated.getName(), httpRequest);

        return mapToResponse(updated);
    }

    @Transactional
    public ApiResponse<String> deleteCredential(Long userId, Long id, HttpServletRequest httpRequest) {
        Credential credential = credentialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with id: " + id));

        String credentialName = credential.getName();
        credentialRepository.delete(credential);

        auditService.logEvent(credential.getUser(), AuditEventType.CREDENTIAL_DELETED,
                "Deleted credential: " + credentialName, httpRequest);

        return ApiResponse.success("Credential deleted successfully.");
    }

    @Transactional(readOnly = true)
    public List<CredentialResponse> searchCredentials(Long userId, String name, String url, String username,
                                                      CredentialType type, Long folderId, String tag) {
        return credentialRepository.searchCredentials(userId, name, url, username, type, folderId, tag).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Set<Tag> resolveOrCreateTags(Long userId, User user, Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> result = new HashSet<>();
        for (String rawName : tagNames) {
            if (rawName == null || rawName.trim().isEmpty()) continue;
            String normalized = rawName.trim().toLowerCase();
            Tag tag = tagRepository.findByUserIdAndName(userId, normalized)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .user(user)
                            .name(normalized)
                            .colorHex("#4F46E5")
                            .build()));
            result.add(tag);
        }
        return result;
    }

    public CredentialResponse mapToResponse(Credential credential) {
        return CredentialResponse.builder()
                .id(credential.getId())
                .name(credential.getName())
                .type(credential.getType())
                .url(credential.getUrl())
                .username(credential.getUsername())
                .encryptedPassword(credential.getEncryptedPassword())
                .notes(credential.getNotes())
                .folder(credential.getFolder() != null ? folderService.mapToResponse(credential.getFolder()) : null)
                .tags(credential.getTags() != null ? credential.getTags().stream().map(tagService::mapToResponse).collect(Collectors.toSet()) : new HashSet<>())
                .favorite(credential.isFavorite())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .lastUsed(credential.getLastUsed())
                .passwordUpdated(credential.getPasswordUpdated())
                .build();
    }
}
