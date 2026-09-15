package com.passwordmanager.controller;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.folder.FolderRequest;
import com.passwordmanager.dto.folder.FolderResponse;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.FolderService;
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
@RequestMapping("/folders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "4. Folder Module", description = "Organize credentials into hierarchical folders")
public class FolderController {

    private static final Logger logger = LoggerFactory.getLogger(FolderController.class);
    private final FolderService folderService;

    @PostMapping
    @Operation(summary = "Create Folder", description = "Creates a new folder for categorizing credentials.")
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody FolderRequest request) {
        logger.info("REST: Create folder '{}' for user ID: {}", request.getName(), userPrincipal.getId());
        FolderResponse response = folderService.createFolder(userPrincipal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Folder created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Folders", description = "Retrieves all folders owned by the authenticated user.")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFolders(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FolderResponse> list = folderService.getFolders(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Folders retrieved successfully", list));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Folder", description = "Updates a folder name and description.")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody FolderRequest request) {
        logger.info("REST: Update folder ID {} for user ID: {}", id, userPrincipal.getId());
        FolderResponse response = folderService.updateFolder(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Folder updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Folder", description = "Deletes a folder. Credentials inside are unassigned but not deleted.")
    public ResponseEntity<ApiResponse<String>> deleteFolder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        logger.info("REST: Delete folder ID {} for user ID: {}", id, userPrincipal.getId());
        ApiResponse<String> response = folderService.deleteFolder(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }
}
