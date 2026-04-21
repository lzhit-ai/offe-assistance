package com.example.getoffer.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.getoffer.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    Optional<Favorite> findByUserIdAndArticleId(Long userId, Long articleId);

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    long countByArticleId(Long articleId);
}
