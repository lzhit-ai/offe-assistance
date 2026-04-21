package com.example.getoffer.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.getoffer.common.CountProjection;
import com.example.getoffer.entity.ArticleTag;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

    @Query("select t.name as name, count(t) as count from ArticleTag t group by t.name order by count(t) desc, t.name asc")
    List<CountProjection> findHotTags(Pageable pageable);
}
