package com.example.getoffer.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.example.getoffer.common.CountProjection;
import com.example.getoffer.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    @Override
    Page<Article> findAll(Specification<Article> spec, Pageable pageable);

    Page<Article> findByAuthorId(Long authorId, Pageable pageable);

    long countByAuthorId(Long authorId);

    List<Article> findTop5ByStatusOrderByFavoriteCountDescViewCountDescCreatedAtDesc(String status);

    @Query("select a.category as name, count(a) as count from Article a group by a.category order by count(a) desc, a.category asc")
    List<CountProjection> findCategoryCounts();
}
