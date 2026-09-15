package com.passwordmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vaults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "encrypted_vault_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedVaultKey;

    @Column(name = "vault_salt", nullable = false, length = 128)
    private String vaultSalt;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = true;

    @Column(name = "last_unlocked_at")
    private LocalDateTime lastUnlockedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
