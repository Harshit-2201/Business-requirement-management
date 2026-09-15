package com.passwordmanager.controller;

import com.passwordmanager.dto.common.ApiResponse;
import com.passwordmanager.dto.credential.CredentialResponse;
import com.passwordmanager.entity.CredentialType;
import com.passwordmanager.security.UserPrincipal;
import com.passwordmanager.service.CredentialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/credentials/search")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "6. Search Module", description = "Multi-criteria search across credentials by Name, URL, Username, Folder, Tags, and Credential Type")
public class SearchController {

    private final CredentialService credentialService;

    @GetMapping
    @Operation(summary = "Search Credentials", description = "Searches vault items matching criteria. All query parameters are optional.")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> search(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Search by item name") @RequestParam(required = false) String name,
            @Parameter(description = "Search by URL") @RequestParam(required = false) String url,
            @Parameter(description = "Search by username") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by credential type") @RequestParam(required = false) CredentialType type,
            @Parameter(description = "Filter by folder ID") @RequestParam(required = false) Long folderId,
            @Parameter(description = "Filter by tag name") @RequestParam(required = false) String tag) {

        List<CredentialResponse> results = credentialService.searchCredentials(
                userPrincipal.getId(), name, url, username, type, folderId, tag
        );
        return ResponseEntity.ok(ApiResponse.success("Search completed with " + results.size() + " matches", results));
    }
}
