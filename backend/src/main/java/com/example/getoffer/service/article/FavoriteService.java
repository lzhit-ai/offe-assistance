package com.example.getoffer.service.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.common.PageResult;
import com.example.getoffer.dto.article.ArticleSummaryResponse;
import com.example.getoffer.dto.article.FavoriteToggleResponse;
import com.example.getoffer.entity.Article;
import com.example.getoffer.entity.Favorite;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.FavoriteRepository;
import com.example.getoffer.service.CurrentUserService;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ArticleRepository articleRepository;
    private final CurrentUserService currentUserService;
    private final ArticleService articleService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ArticleRepository articleRepository,
                           CurrentUserService currentUserService,
                           ArticleService articleService) {
        this.favoriteRepository = favoriteRepository;
        this.articleRepository = articleRepository;
        this.currentUserService = currentUserService;
        this.articleService = articleService;
    }

    @Transactional
    public FavoriteToggleResponse addFavorite(Long articleId) {
        User currentUser = currentUserService.requireCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        if (!favoriteRepository.existsByUserIdAndArticleId(currentUser.getId(), articleId)) {
            Favorite favorite = new Favorite();
            favorite.setArticle(article);
            favorite.setUser(currentUser);
            favoriteRepository.save(favorite);
            article.setFavoriteCount(article.getFavoriteCount() + 1);
            articleRepository.save(article);
        }

        return new FavoriteToggleResponse(articleId, true, article.getFavoriteCount());
    }

    @Transactional
    public FavoriteToggleResponse removeFavorite(Long articleId) {
        User currentUser = currentUserService.requireCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        favoriteRepository.findByUserIdAndArticleId(currentUser.getId(), articleId)
                .ifPresent(favorite -> {
                    favoriteRepository.delete(favorite);
                    article.setFavoriteCount(Math.max(0, article.getFavoriteCount() - 1));
                    articleRepository.save(article);
                });

        return new FavoriteToggleResponse(articleId, false, article.getFavoriteCount());
    }

    @Transactional(readOnly = true)
    public PageResult<ArticleSummaryResponse> getMyFavorites(int page, int pageSize) {
        User currentUser = currentUserService.requireCurrentUser();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ArticleSummaryResponse> mappedPage = favoriteRepository.findByUserId(currentUser.getId(), pageable)
                .map(favorite -> articleService.toSummary(favorite.getArticle(), currentUser));
        return PageResult.from(mappedPage, page, pageSize);
    }
}
