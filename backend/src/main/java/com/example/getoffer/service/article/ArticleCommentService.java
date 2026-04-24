package com.example.getoffer.service.article;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.dto.comment.ArticleCommentCreateRequest;
import com.example.getoffer.dto.comment.ArticleCommentDeleteResponse;
import com.example.getoffer.dto.comment.ArticleCommentResponse;
import com.example.getoffer.dto.comment.CommentAuthorResponse;
import com.example.getoffer.entity.Article;
import com.example.getoffer.entity.Comment;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.CommentRepository;
import com.example.getoffer.service.CurrentUserService;

@Service
public class ArticleCommentService {

    private static final String ARTICLE_NOT_FOUND = "文章不存在";
    private static final String APPROVED = "APPROVED";

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final CurrentUserService currentUserService;

    public ArticleCommentService(CommentRepository commentRepository,
                                 ArticleRepository articleRepository,
                                 CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ArticleCommentResponse> listComments(Long articleId) {
        User currentUser = currentUserService.getCurrentUserOrNull();
        Article article = findAccessibleArticle(articleId, currentUser);
        return commentRepository.findByArticleIdOrderByCreatedAtDescIdDesc(article.getId()).stream()
                .map(comment -> toResponse(comment, currentUser))
                .toList();
    }

    @Transactional
    public ArticleCommentResponse createComment(Long articleId, ArticleCommentCreateRequest request) {
        User currentUser = currentUserService.requireCurrentUser();
        Article article = findAccessibleArticle(articleId, currentUser);
        String content = normalizeContent(request);

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setUser(currentUser);
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);

        article.setCommentCount(article.getCommentCount() + 1);
        articleRepository.save(article);

        return toResponse(saved, currentUser);
    }

    @Transactional
    public ArticleCommentDeleteResponse deleteComment(Long articleId, Long commentId) {
        User currentUser = currentUserService.requireCurrentUser();
        findAccessibleArticle(articleId, currentUser);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        if (!comment.getArticle().getId().equals(articleId)) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }

        Article article = comment.getArticle();
        commentRepository.delete(comment);
        article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
        articleRepository.save(article);

        return new ArticleCommentDeleteResponse(commentId, article.getCommentCount());
    }

    private Article findAccessibleArticle(Long articleId, User currentUser) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException(ARTICLE_NOT_FOUND));
        boolean isOwner = currentUser != null && currentUser.getId().equals(article.getAuthor().getId());
        if (!APPROVED.equals(article.getStatus()) && !isOwner) {
            throw new IllegalArgumentException(ARTICLE_NOT_FOUND);
        }
        return article;
    }

    private String normalizeContent(ArticleCommentCreateRequest request) {
        String content = request == null ? null : request.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        String normalized = content.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("评论内容不能超过 500 个字符");
        }
        return normalized;
    }

    private ArticleCommentResponse toResponse(Comment comment, User currentUser) {
        ArticleCommentResponse response = new ArticleCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setAuthor(new CommentAuthorResponse(
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getNickname(),
                comment.getUser().getAvatar()));
        response.setCanDelete(currentUser != null && currentUser.getId().equals(comment.getUser().getId()));
        return response;
    }
}
