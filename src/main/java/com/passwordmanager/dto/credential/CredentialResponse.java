package com.passwordmanager.dto.credential;

import com.passwordmanager.dto.folder.FolderResponse;
import com.passwordmanager.dto.tag.TagResponse;
import com.passwordmanager.entity.CredentialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialResponse {
    private Long id;
    private String name;
    private CredentialType type;
    private String url;
    private String username;
    private String encryptedPassword;
    private String notes;
    private FolderResponse folder;
    private Set<TagResponse> tags;
    private boolean favorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsed;
    private LocalDateTime passwordUpdated;
}
