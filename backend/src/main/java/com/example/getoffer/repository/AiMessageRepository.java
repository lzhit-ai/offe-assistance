package com.example.getoffer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.getoffer.entity.AiMessage;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    Page<AiMessage> findBySessionIdOrderByCreatedAtAscIdAsc(Long sessionId, Pageable pageable);

    List<AiMessage> findBySessionIdOrderByCreatedAtAscIdAsc(Long sessionId);

    Optional<AiMessage> findTopBySessionIdOrderByCreatedAtDescIdDesc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
