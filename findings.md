# Findings & Decisions

## Requirements
- 用户认为当前“头像上传”和“修改昵称”功能仍未真正实现
- 用户希望先对整个项目做一遍完整遍历，帮助双方统一项目理解，再继续推进需求
- 最终需要明确项目结构、用户链路、个人中心相关落点，以及头像/昵称需求为什么目前体验不符合预期
- 用户新增反馈：修改后运行 Java 服务启动变慢，AI 调用回复也明显变慢，需要定位根因

## Research Findings
- 仓库根目录包含前端 `getoffer`、后端 `backend`、文档目录 `docs`、脚本目录 `scripts`
- 当前分支为 `main`，相对 `origin/main` 超前 7 个提交
- 当前工作区存在未提交改动：
  - `getoffer/src/views/Profile.vue`
  - `getoffer/tests/profile-view.test.js`
- 当前工作区存在未跟踪文件：
  - `README.md`
  - `docs/superpowers/plans/`
- 前端当前仍有 10 个源码 `.js` 文件与 10 个测试 `.js` 文件
- `tsconfig.app.json` 当前未开启 `allowJs` / `checkJs`，因此这些源码 `.js` 文件并不处于 `vue-tsc` 的严格类型检查范围内
- Vite 别名与 TS 配置已经存在，`@/*` 路径映射对 `.ts` 迁移是友好的
- 测试文件大量直接以显式 `.js` 扩展名导入源码模块；源码改名后，测试必须同步改名或改导入路径
- 根目录 `start-getoffer.bat` 会分别启动 `scripts/start-backend.ps1` 与 `scripts/start-frontend.ps1`，然后打开 `http://localhost:5173`
- 前端是 `Vue 3 + Vite + Vue Router + Pinia + Element Plus`
- 后端是 `Spring Boot + Spring Security + Spring Data JPA + JWT + MySQL`
- 后端测试使用 H2，Java 版本配置为 25
- 前端源码中入口文件实际是 `src/main.ts`，路由文件是 `src/router/index.ts`
- 前端用户登录态存放在 `src/stores/admin.js`，使用 `localStorage` 保存 `token` 和 `user`
- 前端主入口 `src/main.ts` 负责挂载 Pinia、Router 与 Element Plus
- 路由层包含 `home`、文章列表/详情、上传、收藏、AI、个人中心
- `upload`、`favorites`、`ai`、`profile` 都要求登录
- 未登录访问受保护路由时，`route-access.js` 会把用户重定向到首页，并附带 `login=1` 与原始 `redirect`
- 会话失效处理在 `src/auth/session.js` 中，负责清空本地 `token` 和 `user`
- `src/api/http.js` 统一设置后端基地址，默认是 `http://localhost:8080`，并在请求头里附带 `Bearer token`
- `src/api/frontend.js` 已定义 `userApi.updateProfile()` 和 `userApi.uploadAvatar()`，对应接口分别是 `PATCH /api/v1/users/me/profile` 与 `POST /api/v1/users/me/avatar`
- `src/api/transformers.js` 会把后端返回的 `nickname`、`avatar`、`registerTime` 映射为前端使用的 `user profile`
- `src/components/Navbar.vue` 里的头像和展示名直接来自 `userStore.user`，因此个人资料更新后导航栏理论上应同步显示最新头像与昵称
- `src/views/Profile.vue` 是个人中心主页面，进入页面后会并行请求 `authApi.me()` 与 `articleApi.getMine()`
- 当前个人中心默认页签是 `articles`，昵称编辑表单在 `info` 页签内
- 头像上传入口位于个人中心头部，当前本地工作区版本已改为通过 `on-change` 触发上传
- 昵称保存会调用 `userApi.updateProfile()`，头像上传会调用 `userApi.uploadAvatar()`，两者成功后都会通过 `applyProfile()` 回写到 `userStore`
- `src/utils/user-profile.js` 规定展示名优先使用 `nickname`，回退到 `username`；昵称前端长度校验为最多 10 个字符，头像只允许图片且限制 2MB
- 登录/注册弹窗仍只围绕 `username + password (+ phone)`，说明当前项目语义上 `username` 仍是登录账号，`nickname` 仅作为资料字段使用
- 后端主应用入口是 `backend/src/main/java/com/example/getoffer/BackendApplication.java`
- 后端 `application.properties` 使用 MySQL，数据库名默认 `getoffer`
- JPA 配置为 `spring.jpa.hibernate.ddl-auto=update`，这意味着表结构会随实体尝试自动更新，而不是依赖显式 migration 框架
- 上传目录配置为 `app.upload-dir=${APP_UPLOAD_DIR:uploads}`，默认落到项目运行目录下的 `uploads`
- Multipart 上传大小限制为 2MB，与前端头像大小限制一致
- `AuthController` 提供注册、登录、`/auth/me`、退出登录；`/auth/me` 是前端个人中心加载用户资料的核心接口
- `AuthService.toUserProfile()` 会把 `username`、`nickname`、`phone`、`email`、`avatar`、`registerTime` 和统计信息一起返回给前端
- `UserController` 在 `/api/v1/users/me` 下提供“我的文章”“修改资料”“上传头像”接口
- `UserProfileService.updateProfile()` 会按当前登录 `username` 找用户、校验昵称唯一性，再更新 `nickname`
- `UserProfileService.uploadAvatar()` 会先校验文件类型和大小，再把文件保存到 `<uploadDir>/avatars/` 下，并把数据库 `avatar` 更新为 `/uploads/avatars/<filename>`
- `User` 实体映射到 `users` 表，字段包括 `username`、`nickname`、`phone`、`email`、`avatar`、`passwordHash`、`registerTime`
- `nickname` 在实体上被标记为 `unique = true`
- `UserRepository` 提供 `findByUsername`、`existsByUsername` 和 `existsByNicknameAndIdNot`
- 安全配置要求除注册、登录、公开文章和元数据外，其余接口都必须带认证
- `StaticResourceConfig` 把 `/uploads/**` 映射到本地上传目录，因此数据库中的 `avatar` 设计上应保存相对访问路径，而不是二进制内容
- 全局异常处理会把 `IllegalArgumentException` 统一返回 400，把未处理异常统一返回 500
- Demo 数据仅创建了 `username`、`phone`、`email`、`passwordHash`，没有预置 `nickname` 或 `avatar`
- Demo 数据是否注入取决于 `app.seed-demo-data=true`，默认配置下是关闭的
- 因此数据库中 `nickname` 和 `avatar` 初始为 `NULL` 是符合当前项目设计的
- 实际启动脚本 `scripts/start-backend.ps1` 会强制把 `APP_SEED_DEMO_DATA=false`
- 启动脚本要求必须配置 `DB_PASSWORD` 和 `DEEPSEEK_API_KEY`，否则后端不会启动成功
- 后端测试环境使用 H2 内存库，`app.upload-dir=target/test-uploads`
- 测试代码中已经覆盖了昵称更新成功、昵称重复失败、头像上传成功、非图片拒绝等路径
- 启动脚本会切换工作目录到 `backend` 后再执行 `mvnw spring-boot:run`
- 因此默认 `app.upload-dir=uploads` 的真实落点是 `backend/uploads`，不是仓库根目录的 `uploads`
- 当前仓库里已经存在 `backend/uploads` 目录，说明后端运行期间至少创建过上传目录
- `backend/uploads/avatars` 目录下已经存在多张实际图片文件，文件名带有 `1-时间戳-随机串`，符合 `UserProfileService.buildAvatarFilename()` 的生成规则
- 这说明头像上传逻辑至少曾经成功执行到“文件落盘”这一步，问题不一定是后端完全没收到请求
- 如果数据库截图仍然显示 `avatar = NULL`，更可能需要继续核对：截图是否刷新、当前应用连接的数据库是否就是截图中的库、或上传时前后端是否运行在同一套最新代码上
- 当前机器上 `localhost:8080` 确实有 Java 进程在监听，说明之前已有后端实例在运行
- 冷启动后端脚本 `scripts/start-backend.ps1` 后，到 `GET /api/v1/metadata/categories` 可用的实测时间约为 `22.35s`
- 这个启动路径实际执行的是 `mvnw.cmd spring-boot:run`，会带上 Maven 启动和编译过程，不是“直接运行已构建产物”
- 直接访问 `https://openrouter.fans` 的基础网络连通实测约 `1.23s`
- 通过后端实际调用一次 `POST /api/v1/ai/sessions/{id}/messages/stream`，最小测试请求在 `61.51s` 后失败，报错为“Unable to read data from the transport connection: The connection was closed.”
- `AiChatService.streamMessage()` 每次发送消息都会先从数据库读取当前会话的全部历史消息，再整体传给 `deepSeekClient.streamChat(...)`
- `DeepSeekApiClient` 使用的仍是外部远程地址 `https://openrouter.fans`，并不是本地模型；请求采用流式 HTTP，连接超时 20 秒、单次请求超时 3 分钟

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| 先按“项目入口 -> 前端 -> 后端 -> 数据库 -> 结论”顺序遍历 | 这样最容易把用户看到的问题和真实代码链路对应起来 |
| 以“能否平滑迁移到 TS”为目标做审查，不直接修改代码 | 当前用户要先评估风险和可行性 |
| 对“启动慢”和“AI 慢”分别做实测 | 先把 Maven 启动耗时和外部 AI 链路耗时分开，避免混在一起猜 |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| 自动 session catchup 未产出有效内容 | 改为手动记录当前状态并继续 |
| 按常规 `.js` 路径读取前端入口文件失败 | 根据实际文件列表切换为 `.ts` 文件继续遍历 |
| 第一次后端启动计时被现有 8080 进程干扰 | 先确认监听进程，再停止旧进程后重测冷启动耗时 |

## Resources
- 根目录：`E:\项目文件\八股code`
- 前端目录：`E:\项目文件\八股code\getoffer`
- 后端目录：`E:\项目文件\八股code\backend`
- 一键启动脚本：`E:\项目文件\八股code\start-getoffer.bat`
- 前端入口：`E:\项目文件\八股code\getoffer\src\main.ts`
- 前端路由：`E:\项目文件\八股code\getoffer\src\router\index.ts`
- 前端 API 层：`E:\项目文件\八股code\getoffer\src\api\frontend.js`
- 顶部导航：`E:\项目文件\八股code\getoffer\src\components\Navbar.vue`
- 后端配置：`E:\项目文件\八股code\backend\src\main\resources\application.properties`
- Demo 数据：`E:\项目文件\八股code\backend\src\main\java\com\example\getoffer\bootstrap\DemoDataSeeder.java`
- 后端启动脚本：`E:\项目文件\八股code\scripts\start-backend.ps1`
- 后端测试配置：`E:\项目文件\八股code\backend\src\test\resources\application-test.properties`
- 前端 TS 配置：`E:\项目文件\八股code\getoffer\tsconfig.app.json`
- 前端构建配置：`E:\项目文件\八股code\getoffer\vite.config.ts`

## Visual/Browser Findings
- 用户提供的数据库截图显示 `users` 表已有 `avatar`、`nickname` 字段，但当前 3 条记录的 `avatar` 与 `nickname` 都是 `NULL`
- 用户提供的个人中心截图显示顶部有“更换头像”按钮，但未展示“个人信息”页签内容，因此用户无法直接看到昵称编辑入口
- 本地文件系统显示 `backend/uploads/avatars` 下已经存在头像文件，这与“数据库截图仍为 NULL”形成了需要继续核对的差异
