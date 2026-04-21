package com.example.getoffer.bootstrap;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.getoffer.entity.Article;
import com.example.getoffer.entity.Favorite;
import com.example.getoffer.entity.User;
import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.FavoriteRepository;
import com.example.getoffer.repository.UserRepository;

@Service
public class DemoDataSeeder {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final FavoriteRepository favoriteRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository,
                          ArticleRepository articleRepository,
                          FavoriteRepository favoriteRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.favoriteRepository = favoriteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void seedIfEmpty() {
        if (userRepository.count() > 0 || articleRepository.count() > 0 || favoriteRepository.count() > 0) {
            return;
        }

        User demoAdmin = createUser("demo_admin", "13800138000", "demo_admin@getoffer.local");
        User frontendOffer = createUser("frontend_offer", "13900139000", "frontend@getoffer.local");
        User backendOffer = createUser("backend_offer", "13700137000", "backend@getoffer.local");

        Article vueArticle = createArticle(
                frontendOffer,
                "Vue3 高频面试题整理",
                "前端",
                1,
                List.of("Vue3", "前端", "组合式API"),
                """
                # Vue3 高频面试题整理

                这篇文章整理了 Composition API、响应式原理、组件通信和性能优化等核心题目。

                - 为什么 `ref` 和 `reactive` 要分开设计
                - `watch` 与 `watchEffect` 的差异
                - 大型表单场景下如何控制渲染开销
                """,
                328,
                2,
                16);

        Article springArticle = createArticle(
                backendOffer,
                "Spring Security + JWT 实战复盘",
                "后端",
                1,
                List.of("Spring Security", "JWT", "Java"),
                """
                # Spring Security + JWT 实战复盘

                文章从过滤器链、认证管理器、权限边界和前后端联调几个角度拆开说明。

                - JWT 适合什么场景
                - 什么时候应该拒绝刷新 token
                - 如何避免接口返回结构混乱
                """,
                286,
                1,
                12);

        Article interviewArticle = createArticle(
                demoAdmin,
                "我的中厂前端面试经历",
                "前端",
                2,
                List.of("面试经历", "前端", "项目复盘"),
                """
                # 我的中厂前端面试经历

                从简历筛选、电话面、现场面到终面，每一轮问题都记录在这里，方便二次复盘。

                - 项目亮点应该怎么讲
                - 八股题和项目题如何切换节奏
                - 被追问时如何稳住表达
                """,
                451,
                1,
                21);

        Article mysqlArticle = createArticle(
                backendOffer,
                "MySQL 索引与慢查询排查清单",
                "数据库",
                1,
                List.of("MySQL", "索引", "数据库"),
                """
                # MySQL 索引与慢查询排查清单

                这篇内容偏实战，适合在准备后端面试和排查线上 SQL 时快速回顾。

                - 联合索引最左前缀原则
                - `explain` 结果应该看什么
                - 慢 SQL 的常见根因有哪些
                """,
                190,
                0,
                8);

        createFavorite(demoAdmin, vueArticle);
        createFavorite(demoAdmin, springArticle);
        createFavorite(frontendOffer, interviewArticle);
        createFavorite(backendOffer, vueArticle);
    }

    private User createUser(String username, String phone, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        return userRepository.save(user);
    }

    private Article createArticle(User author,
                                  String title,
                                  String category,
                                  int type,
                                  List<String> tags,
                                  String content,
                                  long viewCount,
                                  long favoriteCount,
                                  long commentCount) {
        Article article = new Article();
        article.setAuthor(author);
        article.setTitle(title);
        article.setCategory(category);
        article.setType(type);
        article.setContent(content);
        article.setViewCount(viewCount);
        article.setFavoriteCount(favoriteCount);
        article.setCommentCount(commentCount);
        article.replaceTags(tags);
        return articleRepository.save(article);
    }

    private void createFavorite(User user, Article article) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setArticle(article);
        favoriteRepository.save(favorite);
    }
}
