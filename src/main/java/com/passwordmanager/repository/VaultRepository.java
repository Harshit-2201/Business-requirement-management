package com.passwordmanager.repository;

import com.passwordmanager.entity.User;
import com.passwordmanager.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaultRepository extends JpaRepository<Vault, Long> {
    Optional<Vault> findByUser(User user);
    Optional<Vault> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
