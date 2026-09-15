package com.passwordmanager.controller;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.tag.TagRequest;
import com.passwordmanager.dto.tag.TagResponse;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "5. Tag Module", description = "Label and filter credentials using custom color-coded tags")
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);
    private final TagService tagService;

    @PostMapping
    @Operation(summary = "Create Tag", description = "Creates a new custom label/tag with color.")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TagRequest request) {
        logger.info("REST: Create tag '{}' for user ID: {}", request.getName(), userPrincipal.getId());
        TagResponse response = tagService.createTag(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Tag created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Tags", description = "Retrieves all tags created by the authenticated user.")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TagResponse> list = tagService.getTags(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", list));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Tag", description = "Updates tag name or color hex code.")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        logger.info("REST: Update tag ID {} for user ID: {}", id, userPrincipal.getId());
        TagResponse response = tagService.updateTag(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Tag", description = "Removes a tag association.")
    public ResponseEntity<ApiResponse<String>> deleteTag(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        logger.info("REST: Delete tag ID {} for user ID: {}", id, userPrincipal.getId());
        ApiResponse<String> response = tagService.deleteTag(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }
}
