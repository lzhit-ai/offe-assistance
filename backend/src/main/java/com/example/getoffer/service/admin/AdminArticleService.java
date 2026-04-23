package com.example.getoffer.service.admin;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.admin.AdminArticleResponse;
import com.example.getoffer.entity.Article;
import com.example.getoffer.repository.ArticleRepository;

import jakarta.persistence.criteria.JoinType;

@Service
public class AdminArticleService {

    private final AdminAccessService adminAccessService;
    private final ArticleRepository articleRepository;

    public AdminArticleService(AdminAccessService adminAccessService, ArticleRepository articleRepository) {
        this.adminAccessService = adminAccessService;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<AdminArticleResponse> getArticles(int page,
                                                        int pageSize,
                                                        String keyword,
                                                        String category,
                                                        String status) {
        adminAccessService.requireAdmin();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Article> specification = buildSpecification(keyword, category, status);
        Page<AdminArticleResponse> mapped = articleRepository.findAll(specification, pageable).map(this::toResponse);
        return PageResult.from(mapped, page, pageSize);
    }

    @Transactional
    public AdminArticleResponse approve(Long articleId) {
        adminAccessService.requireAdmin();
        Article article = requireArticle(articleId);
        article.setStatus("APPROVED");
        return toResponse(articleRepository.save(article));
    }

    @Transactional
    public AdminArticleResponse reject(Long articleId) {
        adminAccessService.requireAdmin();
        Article article = requireArticle(articleId);
        article.setStatus("REJECTED");
        return toResponse(articleRepository.save(article));
    }

    @Transactional
    public Map<String, Object> delete(Long articleId) {
        adminAccessService.requireAdmin();
        Article article = requireArticle(articleId);
        articleRepository.delete(article);
        return Map.of("success", true);
    }

    private Article requireArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
    }

    private AdminArticleResponse toResponse(Article article) {
        AdminArticleResponse response = new AdminArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setAuthorId(article.getAuthor().getId());
        response.setAuthorName(article.getAuthor().getUsername());
        response.setCategory(article.getCategory());
        response.setType(article.getType());
        response.setStatus(article.getStatus());
        response.setTags(article.getTags().stream().map(tag -> tag.getName()).toList());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        return response;
    }

    private Specification<Article> buildSpecification(String keyword, String category, String status) {
        return (root, query, cb) -> {
            query.distinct(true);
            var predicate = cb.conjunction();
            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status.trim()));
            }
            if (category != null && !category.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category.trim()));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("content"), pattern),
                        cb.like(root.join("author", JoinType.INNER).get("username"), pattern)
                ));
            }
            return predicate;
        };
    }
}
