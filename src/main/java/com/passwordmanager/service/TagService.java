package com.passwordmanager.service;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.tag.TagRequest;
import com.passwordmanager.dto.tag.TagResponse;
import com.passwordmanager.entity.Tag;
import com.passwordmanager.entity.User;
import com.passwordmanager.exception.BadRequestException;
import com.passwordmanager.exception.ResourceNotFoundException;
import com.passwordmanager.repository.TagRepository;
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
public class TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Transactional
    public TagResponse createTag(Long userId, TagRequest request) {
        logger.info("Creating tag '{}' for user {}", request.getName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (tagRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new BadRequestException("Tag with name '" + request.getName() + "' already exists.");
        }

        Tag tag = Tag.builder()
                .user(user)
                .name(request.getName().trim().toLowerCase())
                .colorHex(request.getColorHex() != null ? request.getColorHex() : "#4F46E5")
                .build();

        Tag saved = tagRepository.save(tag);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTags(Long userId) {
        return tagRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponse updateTag(Long userId, Long tagId, TagRequest request) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        String normalizedName = request.getName().trim().toLowerCase();
        if (!tag.getName().equalsIgnoreCase(normalizedName) &&
                tagRepository.existsByUserIdAndName(userId, normalizedName)) {
            throw new BadRequestException("Tag with name '" + normalizedName + "' already exists.");
        }

        tag.setName(normalizedName);
        if (request.getColorHex() != null) {
            tag.setColorHex(request.getColorHex());
        }

        Tag updated = tagRepository.save(tag);
        return mapToResponse(updated);
    }

    @Transactional
    public ApiResponse<String> deleteTag(Long userId, Long tagId) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        tagRepository.delete(tag);
        return ApiResponse.success("Tag deleted successfully.");
    }

    public TagResponse mapToResponse(Tag tag) {
        if (tag == null) return null;
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .colorHex(tag.getColorHex())
                .credentialCount(tag.getCredentials() != null ? tag.getCredentials().size() : 0)
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
