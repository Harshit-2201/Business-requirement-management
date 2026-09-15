package com.passwordmanager.dto.credential;

import com.passwordmanager.entity.CredentialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @NotNull(message = "Credential type is required")
    private CredentialType type;

    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;

    @Size(max = 150, message = "Username cannot exceed 150 characters")
    private String username;

    /**
     * Encrypted using AES-256-GCM. Plaintext is never sent to or stored in server.
     */
    @NotBlank(message = "Encrypted password payload is required")
    private String encryptedPassword;

    /**
     * Optional encrypted notes payload.
     */
    private String notes;

    private Long folderId;

    private Set<String> tagNames;

    @Builder.Default
    private Boolean favorite = false;
}
