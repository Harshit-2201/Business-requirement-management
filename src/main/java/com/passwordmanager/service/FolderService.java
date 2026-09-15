package com.passwordmanager.service;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.folder.FolderRequest;
import com.passwordmanager.dto.folder.FolderResponse;
import com.passwordmanager.entity.Folder;
import com.passwordmanager.entity.User;
import com.passwordmanager.exception.BadRequestException;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.repository.FolderRepository;
import com.passwordmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private static final Logger logger = LoggerFactory.getLogger(FolderService.class);

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Transactional
    public FolderResponse createFolder(Long userId, FolderRequest request) {
        logger.info("Creating folder '{}' for user {}", request.getName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (folderRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BadRequestException("Folder with name '" + request.getName() + "' already exists.");
        }

        Folder folder = Folder.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Folder saved = folderRepository.save(folder);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getFolders(Long userId) {
        return folderRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FolderResponse updateFolder(Long userId, Long folderId, FolderRequest request) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + folderId));

        if (!folder.getName().equalsIgnoreCase(request.getName()) &&
                folderRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BadRequestException("Folder with name '" + request.getName() + "' already exists.");
        }

        folder.setName(request.getName());
        folder.setDescription(request.getDescription());
        Folder updated = folderRepository.save(folder);
        return mapToResponse(updated);
    }

    @Transactional
    public ApiResponse<String> deleteFolder(Long userId, Long folderId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + folderId));

        folderRepository.delete(folder);
        return ApiResponse.success("Folder deleted successfully.");
    }

    public FolderResponse mapToResponse(Folder folder) {
        if (folder == null) return null;
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .credentialCount(folder.getCredentials() != null ? folder.getCredentials().size() : 0)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}
