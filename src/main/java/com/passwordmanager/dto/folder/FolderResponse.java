package com.passwordmanager.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponse {
    private Long id;
    private String name;
    private String description;
    private int credentialCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
