# Offer Assistance

一个面向求职面试场景的全栈项目，围绕八股整理、面经分享、文章检索、收藏管理和 AI 问答提供一套完整的学习与辅助体验。

## 项目介绍

Offer Assistance 旨在把常见的面试准备流程整合到同一个平台中。用户可以浏览热门内容、按分类检索文章、上传自己的面经或知识总结、收藏重点内容，并在登录后使用 AI 助手进行连续对话，提升刷题和复盘效率。

## 核心功能

- 用户注册、登录、退出登录与个人信息查看
- 八股文章与面经内容展示、详情查看、关键词搜索
- 热门内容推荐与分类浏览
- 文章上传与内容维护
- 收藏夹管理，支持沉淀个人重点内容
- AI 助手会话管理，支持流式聊天与历史消息保存

## 技术栈

- 前端：Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios
- 后端：Spring Boot、Spring Security、Spring Data JPA、JWT
- 数据库：MySQL
- AI 接入：DeepSeek / OpenAI 兼容接口

## 项目亮点

- 采用前后端分离架构，功能模块清晰，便于继续扩展
- 同时覆盖内容平台与 AI 助手两类典型业务场景
- 后端提供鉴权、分页、收藏、会话和流式响应等基础能力
- 已包含测试文件与初始化 SQL，方便本地联调和后续演示

## 项目结构

```text
.
├─ getoffer/   # Vue 前端
├─ backend/    # Spring Boot 后端
├─ scripts/    # 前后端启动脚本
└─ start-getoffer.bat
```

## 本地运行

### 运行环境

- Node.js 20+
- JDK 25
- MySQL 8+

### 启动前准备

1. 在 `scripts` 目录下基于 `start-backend.local.example.ps1` 新建 `start-backend.local.ps1`
2. 配置本地数据库账号、数据库密码、JWT 密钥和 AI 接口密钥

示例配置：

```powershell
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your-mysql-password'
$env:JWT_SECRET = 'your-local-jwt-secret'
$env:DEEPSEEK_BASE_URL = 'https://openrouter.fans'
$env:DEEPSEEK_MODEL = 'deepseek-chat'
$env:DEEPSEEK_API_KEY = 'your-api-key'
```

### 启动方式

方式一：在项目根目录直接双击或运行：

```powershell
.\start-getoffer.bat
```

方式二：分别启动前后端：

```powershell
.\scripts\start-backend.ps1
.\scripts\start-frontend.ps1
```

启动后默认访问：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

## 适用场景

- 作为个人求职项目展示
- 作为前后端分离练手项目
- 作为接入 AI 问答能力的内容平台原型
