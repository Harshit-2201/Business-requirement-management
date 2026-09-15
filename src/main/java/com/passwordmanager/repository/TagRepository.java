package com.passwordmanager.repository;

import com.passwordmanager.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserIdOrderByNameAsc(Long userId);
    Optional<Tag> findByIdAndUserId(Long id, Long userId);
    Optional<Tag> findByUserIdAndName(Long userId, String name);
    List<Tag> findByUserIdAndNameIn(Long userId, Set<String> names);
    boolean existsByUserIdAndName(Long userId, String name);
}
