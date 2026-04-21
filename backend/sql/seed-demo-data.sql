USE getoffer;

SET NAMES utf8mb4;

INSERT INTO users (username, phone, email, avatar, password_hash, register_time)
SELECT
  'demo_admin',
  '13800138000',
  'demo_admin@getoffer.local',
  NULL,
  '$2a$10$ssDkrtmGlpGPWoyRsC8S.edRxpsUWNSP36aPV8yWr4X59TIwwyLha',
  '2026-04-20 09:00:00'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE username = 'demo_admin'
);

INSERT INTO users (username, phone, email, avatar, password_hash, register_time)
SELECT
  'frontend_offer',
  '13900139000',
  'frontend@getoffer.local',
  NULL,
  '$2a$10$ssDkrtmGlpGPWoyRsC8S.edRxpsUWNSP36aPV8yWr4X59TIwwyLha',
  '2026-04-20 09:05:00'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE username = 'frontend_offer'
);

INSERT INTO users (username, phone, email, avatar, password_hash, register_time)
SELECT
  'backend_offer',
  '13700137000',
  'backend@getoffer.local',
  NULL,
  '$2a$10$ssDkrtmGlpGPWoyRsC8S.edRxpsUWNSP36aPV8yWr4X59TIwwyLha',
  '2026-04-20 09:10:00'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE username = 'backend_offer'
);

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'Vue3 高频面试题整理',
  '前端',
  1,
  '# Vue3 高频面试题整理\n\n这篇文章梳理了组合式 API、响应式原理、组件通信与性能优化等高频 Vue3 面试题。',
  '2026-04-20 10:00:00',
  '2026-04-20 10:00:00',
  328,
  2,
  16,
  u.id
FROM users u
WHERE u.username = 'frontend_offer'
  AND NOT EXISTS (
    SELECT 1 FROM articles WHERE title = 'Vue3 高频面试题整理'
  );

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'Spring Security + JWT 实战复盘',
  '后端',
  1,
  '# Spring Security + JWT 实战复盘\n\n这篇文章从过滤器链、认证管理器、权限边界和前后端联调几个角度复盘了 Spring Security + JWT 的常见问题。',
  '2026-04-20 10:20:00',
  '2026-04-20 10:20:00',
  286,
  1,
  12,
  u.id
FROM users u
WHERE u.username = 'backend_offer'
  AND NOT EXISTS (
    SELECT 1 FROM articles WHERE title = 'Spring Security + JWT 实战复盘'
  );

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  '前端面试经历复盘',
  '前端',
  2,
  '# 前端面试经历复盘\n\n这里整理了从简历筛选、电话面试到现场面试的关键问题，以及项目追问时的回答思路。',
  '2026-04-20 10:40:00',
  '2026-04-20 10:40:00',
  451,
  1,
  21,
  u.id
FROM users u
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (
    SELECT 1 FROM articles WHERE title = '前端面试经历复盘'
  );

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'MySQL 索引与慢查询排查清单',
  '数据库',
  1,
  '# MySQL 索引与慢查询排查清单\n\n这篇内容聚焦联合索引、执行计划和慢查询排查，适合面试准备和日常排障。',
  '2026-04-20 11:00:00',
  '2026-04-20 11:00:00',
  190,
  0,
  8,
  u.id
FROM users u
WHERE u.username = 'backend_offer'
  AND NOT EXISTS (
    SELECT 1 FROM articles WHERE title = 'MySQL 索引与慢查询排查清单'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'Vue3'
FROM articles a
WHERE a.title = 'Vue3 高频面试题整理'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = 'Vue3'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '前端'
FROM articles a
WHERE a.title = 'Vue3 高频面试题整理'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '前端'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '组合式API'
FROM articles a
WHERE a.title = 'Vue3 高频面试题整理'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '组合式API'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'Spring Security'
FROM articles a
WHERE a.title = 'Spring Security + JWT 实战复盘'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = 'Spring Security'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'JWT'
FROM articles a
WHERE a.title = 'Spring Security + JWT 实战复盘'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = 'JWT'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'Java'
FROM articles a
WHERE a.title = 'Spring Security + JWT 实战复盘'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = 'Java'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '面试经历'
FROM articles a
WHERE a.title = '前端面试经历复盘'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '面试经历'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '前端'
FROM articles a
WHERE a.title = '前端面试经历复盘'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '前端'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '项目复盘'
FROM articles a
WHERE a.title = '前端面试经历复盘'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '项目复盘'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'MySQL'
FROM articles a
WHERE a.title = 'MySQL 索引与慢查询排查清单'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = 'MySQL'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '索引'
FROM articles a
WHERE a.title = 'MySQL 索引与慢查询排查清单'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '索引'
  );

INSERT INTO article_tags (article_id, name)
SELECT a.id, '数据库'
FROM articles a
WHERE a.title = 'MySQL 索引与慢查询排查清单'
  AND NOT EXISTS (
    SELECT 1 FROM article_tags t
    WHERE t.article_id = a.id AND t.name = '数据库'
  );

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 11:20:00'
FROM users u
JOIN articles a ON a.title = 'Vue3 高频面试题整理'
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (
    SELECT 1 FROM favorites f
    WHERE f.user_id = u.id AND f.article_id = a.id
  );

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 11:21:00'
FROM users u
JOIN articles a ON a.title = 'Spring Security + JWT 实战复盘'
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (
    SELECT 1 FROM favorites f
    WHERE f.user_id = u.id AND f.article_id = a.id
  );

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 11:22:00'
FROM users u
JOIN articles a ON a.title = '前端面试经历复盘'
WHERE u.username = 'frontend_offer'
  AND NOT EXISTS (
    SELECT 1 FROM favorites f
    WHERE f.user_id = u.id AND f.article_id = a.id
  );

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 11:23:00'
FROM users u
JOIN articles a ON a.title = 'Vue3 高频面试题整理'
WHERE u.username = 'backend_offer'
  AND NOT EXISTS (
    SELECT 1 FROM favorites f
    WHERE f.user_id = u.id AND f.article_id = a.id
  );
