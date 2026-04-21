# Demo Data

`DemoDataSeeder` 这套 Java 演示数据能力仍然保留，但现在默认是关闭的。

## Default Behavior

- 默认不会自动向数据库写入 Java 演示数据
- 推荐的初始化方式优先使用 SQL 脚本
- 只有当你显式设置 `APP_SEED_DEMO_DATA=true` 时，后端启动后才会尝试写入 Java 演示数据

## 知识点内容模式

如果当前站点希望展示的是“知识点摘要文章”，请执行：

1. [schema.sql](/E:/项目文件/八股code/backend/sql/schema.sql)
2. [seed-knowledge-data.sql](/E:/项目文件/八股code/backend/sql/seed-knowledge-data.sql)

这会：
- 保留 `users`
- 清空 `articles`
- 清空 `article_tags`
- 清空 `favorites`
- 写入新的知识点文章、标签和收藏数据

## 演示文章模式

如果你只是想保留旧的演示面试文章，请执行：

1. [schema.sql](/E:/项目文件/八股code/backend/sql/schema.sql)
2. [seed-demo-data.sql](/E:/项目文件/八股code/backend/sql/seed-demo-data.sql)

默认演示账号：
- 用户名：`demo_admin`
  密码：`123456`
- 用户名：`frontend_offer`
  密码：`123456`
- 用户名：`backend_offer`
  密码：`123456`

## Seed Rules

- Java 自动 seed 只在 `users`、`articles`、`favorites` 都为空时才会尝试写入
- 重复启动不会重复插入
- 如果你已经手动执行过 SQL 种子脚本，通常就不需要再开启 Java 自动 seed

## Enable Manually

需要时再手动开启：

```powershell
$env:APP_SEED_DEMO_DATA="true"
```

## Production Recommendation

上线环境建议保持默认关闭，不依赖 Java 自动 seed。

如果后续网站以知识点内容为主，推荐固定使用：
- `schema.sql`
- `seed-knowledge-data.sql`
