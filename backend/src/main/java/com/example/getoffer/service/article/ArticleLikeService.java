package com.example.getoffer.service.article;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.dto.article.LikeToggleResponse;
import com.example.getoffer.entity.Article;
import com.example.getoffer.entity.Like;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.LikeRepository;
import com.example.getoffer.service.CurrentUserService;

@Service
public class ArticleLikeService {

    private final LikeRepository likeRepository;
    private final ArticleRepository articleRepository;
    private final CurrentUserService currentUserService;

    public ArticleLikeService(LikeRepository likeRepository,
                              ArticleRepository articleRepository,
                              CurrentUserService currentUserService) {
        this.likeRepository = likeRepository;
        this.articleRepository = articleRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public LikeToggleResponse like(Long articleId) {
        User currentUser = currentUserService.requireCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        if (likeRepository.existsByUserIdAndArticleId(currentUser.getId(), articleId)) {
            throw new IllegalArgumentException("你已经点过赞了");
        }

        Like like = new Like();
        like.setArticle(article);
        like.setUser(currentUser);
        likeRepository.save(like);
        article.setLikeCount(article.getLikeCount() + 1);
        articleRepository.save(article);

        return new LikeToggleResponse(articleId, true, article.getLikeCount());
    }

    @Transactional
    public LikeToggleResponse unlike(Long articleId) {
        User currentUser = currentUserService.requireCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        Like like = likeRepository.findByUserIdAndArticleId(currentUser.getId(), articleId)
                .orElseThrow(() -> new IllegalArgumentException("你还没有点赞这篇文章"));
        likeRepository.delete(like);
        article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
        articleRepository.save(article);

        return new LikeToggleResponse(articleId, false, article.getLikeCount());
    }
}
