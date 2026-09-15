package com.passwordmanager.repository;

import com.passwordmanager.entity.Credential;
import com.passwordmanager.entity.CredentialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long>, JpaSpecificationExecutor<Credential> {

    List<Credential> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<Credential> findByIdAndUserId(Long id, Long userId);

    List<Credential> findByUserIdAndFavoriteTrue(Long userId);

    List<Credential> findByUserIdAndFolderId(Long userId, Long folderId);

    List<Credential> findByUserIdAndType(Long userId, CredentialType type);

    long countByUserId(Long userId);

    @Query("SELECT c FROM Credential c " +
           "LEFT JOIN c.tags t " +
           "LEFT JOIN c.folder f " +
           "WHERE c.user.id = :userId " +
           "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:url IS NULL OR LOWER(c.url) LIKE LOWER(CONCAT('%', :url, '%'))) " +
           "AND (:username IS NULL OR LOWER(c.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:folderId IS NULL OR f.id = :folderId) " +
           "AND (:tag IS NULL OR LOWER(t.name) = LOWER(:tag)) " +
           "ORDER BY c.updatedAt DESC")
    List<Credential> searchCredentials(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("url") String url,
            @Param("username") String username,
            @Param("type") CredentialType type,
            @Param("folderId") Long folderId,
            @Param("tag") String tag
    );
}
