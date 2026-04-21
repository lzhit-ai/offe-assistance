package com.example.getoffer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.getoffer.entity.AiSession;

public interface AiSessionRepository extends JpaRepository<AiSession, Long> {

    Page<AiSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
