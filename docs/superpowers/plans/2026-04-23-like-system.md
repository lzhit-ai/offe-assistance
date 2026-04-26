# 点赞系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为文章增加真实点赞/取消点赞能力，并把个人中心“点赞”统计改为当前用户所有文章累计获得的点赞数。

**Architecture:** 后端新增独立 `likes` 实体、仓储与点赞服务，维护 `articles.like_count` 并在文章详情、文章列表、用户资料统计中返回真实点赞信息。前端只在文章详情页接入点赞按钮与本地状态同步，个人中心继续复用 `auth/me` 返回的统计值，不自行汇总分页数据。

**Tech Stack:** Spring Boot、Spring Security、Spring Data JPA、H2/MySQL、Vue 3、Element Plus、Node test、vue-tsc。

---

### Task 1: 后端点赞数据模型与接口测试

**Files:**
- Create: `backend/src/test/java/com/example/getoffer/controller/LikeControllerTest.java`
- Modify: `backend/src/test/java/com/example/getoffer/controller/ArticleControllerTest.java`
- Modify: `backend/src/test/java/com/example/getoffer/controller/AuthControllerTest.java`

- [ ] **Step 1: 写后端失败测试**

```java
@Test
void usersCanLikeUnlikeOthersArticlesButCannotLikeTheirOwn() throws Exception {
    String authorToken = TestAuthHelper.registerAndGetToken(mockMvc, "like_author", "13800138140", "123456");
    String readerToken = TestAuthHelper.registerAndGetToken(mockMvc, "like_reader", "13800138141", "123456");

    String createResponse = mockMvc.perform(post("/api/v1/articles")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "title": "可点赞的文章",
                              "category": "后端",
                              "type": 1,
                              "tags": ["Spring"],
                              "content": "# like"
                            }
                            """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String articleId = JsonTestUtils.readJsonPath(createResponse, "$.data.id").toString();
    articleRepository.findById(Long.valueOf(articleId)).ifPresent(article -> {
        article.setStatus("APPROVED");
        articleRepository.save(article);
    });

    mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                    .header("Authorization", "Bearer " + readerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.liked").value(true))
            .andExpect(jsonPath("$.data.likeCount").value(1));

    mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                    .header("Authorization", "Bearer " + readerToken))
            .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/v1/articles/" + articleId + "/like")
                    .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isBadRequest());

    mockMvc.perform(delete("/api/v1/articles/" + articleId + "/like")
                    .header("Authorization", "Bearer " + readerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.liked").value(false))
            .andExpect(jsonPath("$.data.likeCount").value(0));
}
```

```java
@Test
void articleDetailAndAuthMeExposeRealLikeInformation() throws Exception {
    // 注册作者和点赞用户，创建文章并审批通过
    // 点赞后断言文章详情返回 likeCount=1、liked=true
    // 断言 /api/v1/auth/me 返回 data.stats.likeCount=1
}
```

- [ ] **Step 2: 运行失败测试并确认红灯**

Run: `./mvnw.cmd "-Dtest=LikeControllerTest,ArticleControllerTest,AuthControllerTest" test`

Expected:
- `POST /api/v1/articles/{id}/like` 映射不存在，返回 `404`
- `$.data.likeCount` / `$.data.liked` 字段缺失
- `auth/me` 的 `stats.likeCount` 仍然是旧值

- [ ] **Step 3: 提交红灯前的测试文件**

```bash
git add backend/src/test/java/com/example/getoffer/controller/LikeControllerTest.java backend/src/test/java/com/example/getoffer/controller/ArticleControllerTest.java backend/src/test/java/com/example/getoffer/controller/AuthControllerTest.java
```

### Task 2: 后端点赞实体、仓储、接口与统计实现

**Files:**
- Create: `backend/src/main/java/com/example/getoffer/entity/Like.java`
- Create: `backend/src/main/java/com/example/getoffer/repository/LikeRepository.java`
- Create: `backend/src/main/java/com/example/getoffer/service/article/ArticleLikeService.java`
- Modify: `backend/src/main/java/com/example/getoffer/entity/Article.java`
- Modify: `backend/src/main/java/com/example/getoffer/controller/ArticleController.java`
- Modify: `backend/src/main/java/com/example/getoffer/service/article/ArticleService.java`
- Modify: `backend/src/main/java/com/example/getoffer/service/auth/AuthService.java`
- Modify: `backend/src/main/java/com/example/getoffer/dto/article/ArticleSummaryResponse.java`
- Modify: `backend/src/main/java/com/example/getoffer/dto/article/ArticleDetailResponse.java`

- [ ] **Step 1: 写最小实体与仓储实现**

```java
@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_like_user_article", columnNames = {"user_id", "article_id"})
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

```java
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    Optional<Like> findByUserIdAndArticleId(Long userId, Long articleId);
    long countByArticleAuthorId(Long authorId);
}
```

- [ ] **Step 2: 为 `Article` 和 DTO 增加点赞字段**

```java
@Column(name = "like_count", nullable = false)
private long likeCount;
```

```java
private long likeCount;
@JsonProperty("liked")
private boolean liked;
```

- [ ] **Step 3: 实现点赞服务**

```java
@Service
public class ArticleLikeService {

    public ArticleDetailResponse like(Long articleId) { ... }

    public ArticleDetailResponse unlike(Long articleId) { ... }
}
```

实现要求：
- 必须登录
- 文章不存在时报错
- 作者给自己点赞时报错 `不能给自己的文章点赞`
- 重复点赞时报错 `你已经点过赞了`
- 取消未点过赞的文章时报错 `你还没有点赞这篇文章`
- 点赞时 `article.likeCount + 1`
- 取消点赞时 `article.likeCount - 1`，并用 `Math.max(0, ...)`

- [ ] **Step 4: 暴露文章点赞接口**

```java
@PostMapping("/{articleId}/like")
public ApiResponse<ArticleDetailResponse> likeArticle(@PathVariable Long articleId) {
    return ApiResponse.success(articleLikeService.like(articleId));
}

@DeleteMapping("/{articleId}/like")
public ApiResponse<ArticleDetailResponse> unlikeArticle(@PathVariable Long articleId) {
    return ApiResponse.success(articleLikeService.unlike(articleId));
}
```

- [ ] **Step 5: 把点赞状态注入文章详情、文章列表与用户资料统计**

```java
response.setLikeCount(article.getLikeCount());
response.setLiked(currentUser != null
        && likeRepository.existsByUserIdAndArticleId(currentUser.getId(), article.getId()));
```

```java
response.setStats(new UserStatsResponse(
        articleRepository.countByAuthorId(user.getId()),
        favoriteRepository.countByUserId(user.getId()),
        likeRepository.countByArticleAuthorId(user.getId())
));
```

- [ ] **Step 6: 运行后端测试并确认转绿**

Run: `./mvnw.cmd "-Dtest=LikeControllerTest,ArticleControllerTest,AuthControllerTest,FavoriteControllerTest" test`

Expected:
- `BUILD SUCCESS`
- 新增点赞测试全部通过
- 旧文章/收藏接口回归通过

- [ ] **Step 7: 提交后端实现**

```bash
git add backend/src/main/java/com/example/getoffer/entity/Like.java backend/src/main/java/com/example/getoffer/repository/LikeRepository.java backend/src/main/java/com/example/getoffer/service/article/ArticleLikeService.java backend/src/main/java/com/example/getoffer/entity/Article.java backend/src/main/java/com/example/getoffer/controller/ArticleController.java backend/src/main/java/com/example/getoffer/service/article/ArticleService.java backend/src/main/java/com/example/getoffer/service/auth/AuthService.java backend/src/main/java/com/example/getoffer/dto/article/ArticleSummaryResponse.java backend/src/main/java/com/example/getoffer/dto/article/ArticleDetailResponse.java backend/src/test/java/com/example/getoffer/controller/LikeControllerTest.java backend/src/test/java/com/example/getoffer/controller/ArticleControllerTest.java backend/src/test/java/com/example/getoffer/controller/AuthControllerTest.java
git commit -m "feat: add article like backend"
```

### Task 3: 前端 API 与页面失败测试

**Files:**
- Modify: `getoffer/tests/admin-api.test.ts`
- Modify: `getoffer/tests/api-transformers.test.ts`
- Create: `getoffer/tests/article-detail-like.test.ts`
- Modify: `getoffer/tests/profile-view.test.ts`

- [ ] **Step 1: 写前端失败测试**

```ts
test('frontend api exposes article like endpoints', () => {
  const source = readFileSync(resolve(projectRoot, 'src/api/frontend.ts'), 'utf8')

  assert.match(source, /\/api\/v1\/articles\/\$\{articleId\}\/like/)
  assert.match(source, /likeApi/)
  assert.match(source, /add/)
  assert.match(source, /remove/)
})
```

```ts
test('article detail view exposes like button states', () => {
  const source = readFileSync(resolve(process.cwd(), 'src/views/ArticleDetail.vue'), 'utf8')

  assert.match(source, /点赞/)
  assert.match(source, /取消点赞/)
  assert.match(source, /不能给自己的文章点赞/)
})
```

```ts
test('api transformers preserve likeCount and liked state', () => {
  const article = mapArticle({
    likeCount: 3,
    liked: true,
  })

  assert.equal(article.likeCount, 3)
  assert.equal(article.liked, true)
})
```

- [ ] **Step 2: 运行失败测试并确认红灯**

Run: `node --test tests/api-transformers.test.ts tests/article-detail-like.test.ts tests/profile-view.test.ts`

Expected:
- `frontend.ts` 没有点赞接口
- `ArticleDetail.vue` 没有点赞文案或作者限制
- 文章映射未完整保留点赞字段时测试失败

### Task 4: 前端点赞 API、详情页交互与个人中心统计回归

**Files:**
- Modify: `getoffer/src/api/frontend.ts`
- Modify: `getoffer/src/api/transformers.ts`
- Modify: `getoffer/src/views/ArticleDetail.vue`
- Modify: `getoffer/src/views/Profile.vue`
- Create: `getoffer/tests/article-detail-like.test.ts`
- Modify: `getoffer/tests/api-transformers.test.ts`
- Modify: `getoffer/tests/profile-view.test.ts`

- [ ] **Step 1: 为文章映射和 API 增加点赞能力**

```ts
export interface ArticleItem {
  ...
  likeCount: number
  liked: boolean
}
```

```ts
export const likeApi = {
  async add(articleId: number | string) {
    return request<ArticleItem>({
      url: `/api/v1/articles/${articleId}/like`,
      method: 'post',
    })
  },
  async remove(articleId: number | string) {
    return request<ArticleItem>({
      url: `/api/v1/articles/${articleId}/like`,
      method: 'delete',
    })
  },
}
```

- [ ] **Step 2: 在文章详情页接入点赞按钮与作者禁用逻辑**

```ts
const isAuthor = computed(() => userStore.user?.id === article.value?.authorInfo?.id)
const isLiked = ref(false)

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (isAuthor.value) {
    ElMessage.warning('不能给自己的文章点赞')
    return
  }
  ...
}
```

模板要求：
- 收藏按钮保留
- 新增点赞按钮
- 文案为 `点赞（x）` / `取消点赞（x）`
- 作者本人按钮禁用并显示 `不能给自己的文章点赞`

- [ ] **Step 3: 更新个人中心点赞统计的断言**

```ts
test('profile view renders like counter from user stats', () => {
  assert.match(profileViewSource, /userInfo\.stats\.likeCount/)
  assert.match(profileViewSource, /点赞/)
})
```

- [ ] **Step 4: 运行前端测试与类型检查**

Run:

```bash
node --test tests/api-transformers.test.ts tests/article-detail-like.test.ts tests/profile-view.test.ts
npm.cmd run type-check
npm.cmd run build
```

Expected:
- 测试全绿
- `vue-tsc --build` 通过
- `vite build` 通过

- [ ] **Step 5: 提交前端实现**

```bash
git add getoffer/src/api/frontend.ts getoffer/src/api/transformers.ts getoffer/src/views/ArticleDetail.vue getoffer/src/views/Profile.vue getoffer/tests/api-transformers.test.ts getoffer/tests/article-detail-like.test.ts getoffer/tests/profile-view.test.ts
git commit -m "feat: add article like frontend"
```

### Task 5: 全量回归与交付说明

**Files:**
- Modify: `backend/sql/seed-demo-data.sql`
- Modify: `backend/docs/demo-data.md`

- [ ] **Step 1: 如种子数据包含建表清理语句，补充 likes 与 like_count**

```sql
DELETE FROM likes;
ALTER TABLE articles ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;
```
```

- [ ] **Step 2: 运行关键回归测试**

Run:

```bash
cd backend
./mvnw.cmd "-Dtest=LikeControllerTest,ArticleControllerTest,AuthControllerTest,FavoriteControllerTest,AdminControllerTest" test

cd ../getoffer
node --test tests/*.ts
npm.cmd run type-check
npm.cmd run build
```

Expected:
- 后端关键控制器测试通过
- 前端 `tests/*.ts` 全绿
- 类型检查与构建通过

- [ ] **Step 3: 准备数据库迁移说明**

执行前提醒用户补两项数据库变更：

```sql
ALTER TABLE articles ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  CONSTRAINT uk_like_user_article UNIQUE (user_id, article_id),
  CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_like_article FOREIGN KEY (article_id) REFERENCES articles(id)
);
```
