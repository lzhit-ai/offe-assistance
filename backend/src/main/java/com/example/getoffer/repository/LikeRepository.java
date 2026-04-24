package com.example.getoffer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.getoffer.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    Optional<Like> findByUserIdAndArticleId(Long userId, Long articleId);

    long countByArticleAuthorId(Long authorId);
}
