# Database Schema

这份说明用于初始化 `getoffer` 一期后端所需的 MySQL 表和内容数据。

当前一期的核心表只有 4 张：
- `users`
- `articles`
- `article_tags`
- `favorites`

## Option 1: 使用 SQL 脚本初始化

这是当前最推荐的初始化方式。

### 知识点内容模式

如果你希望网站展示的是“知识点摘要文章”，请按下面顺序执行：

1. [schema.sql](/E:/项目文件/八股code/backend/sql/schema.sql)
2. [seed-knowledge-data.sql](/E:/项目文件/八股code/backend/sql/seed-knowledge-data.sql)

说明：
- `seed-knowledge-data.sql` 会清空 `articles`、`article_tags`、`favorites`
- `users` 表会保留
- 执行后，站点会展示新的知识点文章，而不是旧的演示面试文章

### 演示文章模式

如果你只是想保留旧的演示文章数据，可以执行：

1. [schema.sql](/E:/项目文件/八股code/backend/sql/schema.sql)
2. [seed-demo-data.sql](/E:/项目文件/八股code/backend/sql/seed-demo-data.sql)

默认演示账号和密码：
- `demo_admin / 123456`
- `frontend_offer / 123456`
- `backend_offer / 123456`

注意：
- `seed-demo-data.sql` 和 `seed-knowledge-data.sql` 都会操作文章相关数据
- 同一套库里不建议连续执行两者，否则后执行的脚本会覆盖前者的文章内容

## Option 2: 让 Spring Boot 自动建表

如果你已经配置好本机 MySQL，也可以直接启动后端。

当前配置在 [application.properties](/E:/项目文件/八股code/backend/src/main/resources/application.properties:1)：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/getoffer?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true&characterEncoding=utf8
spring.jpa.hibernate.ddl-auto=update
app.seed-demo-data=${APP_SEED_DEMO_DATA:false}
```

这表示：
- 数据库不存在时会尝试自动创建 `getoffer`
- 表不存在时，JPA 会按实体自动建表
- Java 自动演示数据默认关闭

启动方式：

```powershell
cd E:\项目文件\八股code\backend
$env:DB_USERNAME="你的MySQL用户名"
$env:DB_PASSWORD="你的MySQL密码"
./mvnw.cmd spring-boot:run
```

如果你确实需要启用 Java 自动演示数据，再显式开启：

```powershell
$env:APP_SEED_DEMO_DATA="true"
```

## Table Mapping

SQL 和当前 JPA 实体是一一对应的：

- `users` <- `User.java`
- `articles` <- `Article.java`
- `article_tags` <- `ArticleTag.java`
- `favorites` <- `Favorite.java`

## Notes

- 当前网站如果要展示知识点文章，优先使用 `seed-knowledge-data.sql`
- `seed-demo-data.sql` 更适合保留旧的演示数据场景
- AI、评论、点赞、后台审核等二期表结构不在这份脚本范围内
- 即使使用 SQL 脚本初始化，也仍建议保留 `ddl-auto=update` 以便实体字段小改动时自动补齐
