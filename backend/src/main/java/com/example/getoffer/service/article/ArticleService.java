package com.example.getoffer.service.article;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.article.ArticleAuthorResponse;
import com.example.getoffer.dto.article.ArticleDetailResponse;
import com.example.getoffer.dto.article.ArticleSummaryResponse;
import com.example.getoffer.dto.article.ArticleUpsertRequest;
import com.example.getoffer.entity.Article;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.FavoriteRepository;
import com.example.getoffer.repository.LikeRepository;
import com.example.getoffer.service.CurrentUserService;

import jakarta.persistence.criteria.JoinType;

@Service
public class ArticleService {

    private static final String ARTICLE_NOT_FOUND = "文章不存在";
    private static final String APPROVED = "APPROVED";

    private final ArticleRepository articleRepository;
    private final FavoriteRepository favoriteRepository;
    private final LikeRepository likeRepository;
    private final CurrentUserService currentUserService;

    public ArticleService(ArticleRepository articleRepository,
                          FavoriteRepository favoriteRepository,
                          LikeRepository likeRepository,
                          CurrentUserService currentUserService) {
        this.articleRepository = articleRepository;
        this.favoriteRepository = favoriteRepository;
        this.likeRepository = likeRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ArticleDetailResponse createArticle(ArticleUpsertRequest request) {
        validateRequest(request);
        User author = currentUserService.requireCurrentUser();
        Article article = new Article();
        applyRequest(article, request, author);
        Article saved = articleRepository.save(article);
        return toDetail(saved, author);
    }

    @Transactional
    public ArticleDetailResponse updateArticle(Long articleId, ArticleUpsertRequest request) {
        validateRequest(request);
        User currentUser = currentUserService.requireCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException(ARTICLE_NOT_FOUND));
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("无权限编辑此文章");
        }
        applyRequest(article, request, currentUser);
        Article saved = articleRepository.save(article);
        return toDetail(saved, currentUser);
    }

    @Transactional
    public ArticleDetailResponse getDetail(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException(ARTICLE_NOT_FOUND));
        User currentUser = currentUserService.getCurrentUserOrNull();
        if (!APPROVED.equals(article.getStatus())) {
            boolean isOwner = currentUser != null && currentUser.getId().equals(article.getAuthor().getId());
            if (!isOwner) {
                throw new IllegalArgumentException(ARTICLE_NOT_FOUND);
            }
        }

        article.setViewCount(article.getViewCount() + 1);
        Article saved = articleRepository.save(article);
        return toDetail(saved, currentUser);
    }

    @Transactional(readOnly = true)
    public PageResult<ArticleSummaryResponse> getArticleList(int page,
                                                             int pageSize,
                                                             Integer type,
                                                             String category,
                                                             String tag,
                                                             String keyword,
                                                             Long authorId,
                                                             String sort) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, resolveSort(sort));
        Specification<Article> specification = buildSpecification(type, category, tag, keyword, authorId, true);
        Page<ArticleSummaryResponse> mappedPage = articleRepository.findAll(specification, pageable)
                .map(article -> toSummary(article, currentUserService.getCurrentUserOrNull()));
        return PageResult.from(mappedPage, page, pageSize);
    }

    @Transactional(readOnly = true)
    public List<ArticleSummaryResponse> getHotArticles(Integer type, int limit) {
        List<Article> articles = articleRepository.findTop5ByStatusOrderByFavoriteCountDescViewCountDescCreatedAtDesc(APPROVED);
        return articles.stream()
                .filter(article -> type == null || article.getType() == type)
                .limit(limit)
                .map(article -> toSummary(article, currentUserService.getCurrentUserOrNull()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResult<ArticleSummaryResponse> getMyArticles(int page, int pageSize, Integer type) {
        User currentUser = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ArticleSummaryResponse> mappedPage = articleRepository.findByAuthorId(currentUser.getId(), pageable)
                .map(article -> toSummary(article, currentUser));
        if (type != null) {
            List<ArticleSummaryResponse> filtered = mappedPage.getContent().stream()
                    .filter(article -> article.getType() == type)
                    .collect(Collectors.toList());
            Page<ArticleSummaryResponse> filteredPage = new PageImpl<>(filtered, pageable, filtered.size());
            return PageResult.from(filteredPage, page, pageSize);
        }
        return PageResult.from(mappedPage, page, pageSize);
    }

    public ArticleSummaryResponse toSummary(Article article, User currentUser) {
        ArticleSummaryResponse response = new ArticleSummaryResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setAuthor(new ArticleAuthorResponse(article.getAuthor().getId(), article.getAuthor().getUsername()));
        response.setCategory(article.getCategory());
        response.setType(article.getType());
        response.setStatus(article.getStatus());
        response.setTags(article.getTags().stream().map(tag -> tag.getName()).toList());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        response.setViewCount(article.getViewCount());
        response.setFavoriteCount(article.getFavoriteCount());
        response.setLikeCount(article.getLikeCount());
        response.setCommentCount(article.getCommentCount());
        response.setFavorited(currentUser != null
                && favoriteRepository.existsByUserIdAndArticleId(currentUser.getId(), article.getId()));
        response.setLiked(currentUser != null
                && likeRepository.existsByUserIdAndArticleId(currentUser.getId(), article.getId()));
        return response;
    }

    private ArticleDetailResponse toDetail(Article article, User currentUser) {
        ArticleSummaryResponse summary = toSummary(article, currentUser);
        ArticleDetailResponse detail = new ArticleDetailResponse();
        detail.setId(summary.getId());
        detail.setTitle(summary.getTitle());
        detail.setAuthor(summary.getAuthor());
        detail.setCategory(summary.getCategory());
        detail.setType(summary.getType());
        detail.setStatus(summary.getStatus());
        detail.setTags(summary.getTags());
        detail.setCreatedAt(summary.getCreatedAt());
        detail.setUpdatedAt(summary.getUpdatedAt());
        detail.setViewCount(summary.getViewCount());
        detail.setFavoriteCount(summary.getFavoriteCount());
        detail.setLikeCount(summary.getLikeCount());
        detail.setCommentCount(summary.getCommentCount());
        detail.setFavorited(summary.isFavorited());
        detail.setLiked(summary.isLiked());
        detail.setContent(article.getContent());
        detail.setCanEdit(currentUser != null && currentUser.getId().equals(article.getAuthor().getId()));
        return detail;
    }

    private void applyRequest(Article article, ArticleUpsertRequest request, User author) {
        article.setTitle(request.getTitle().trim());
        article.setCategory(request.getCategory().trim());
        article.setType(request.getType());
        article.setContent(request.getContent().trim());
        article.setAuthor(author);
        article.replaceTags(request.getTags());
    }

    private void validateRequest(ArticleUpsertRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        if (request.getType() != 1 && request.getType() != 2) {
            throw new IllegalArgumentException("文章类型不合法");
        }
    }

    private Sort resolveSort(String sort) {
        if ("hot".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "favoriteCount")
                    .and(Sort.by(Sort.Direction.DESC, "viewCount"))
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private Specification<Article> buildSpecification(Integer type,
                                                      String category,
                                                      String tag,
                                                      String keyword,
                                                      Long authorId,
                                                      boolean approvedOnly) {
        return (root, query, cb) -> {
            query.distinct(true);
            var predicate = cb.conjunction();
            if (approvedOnly) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), APPROVED));
            }
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }
            if (category != null && !category.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("category"), category));
            }
            if (authorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("author").get("id"), authorId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("content"), pattern)
                ));
            }
            if (tag != null && !tag.isBlank()) {
                var join = root.join("tags", JoinType.LEFT);
                predicate = cb.and(predicate, cb.equal(join.get("name"), tag));
            }
            return predicate;
        };
    }
}
