# MuXcraft · 木心工坊

> 一个收纳项目、经历、技术栈、想法与一切值得被做出来之物的个人数字工坊。

![Vue](https://img.shields.io/badge/Vue-3.2-42b883?logo=vuedotjs&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-3.x-646cff?logo=vite&logoColor=white)
![Vue Router](https://img.shields.io/badge/Vue_Router-4-42b883?logo=vuedotjs&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=springboot&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3-000000)
![JWT](https://img.shields.io/badge/JWT-Auth-black?logo=jsonwebtokens&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-Live-010101?logo=websocket&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-Deploy-009639?logo=nginx&logoColor=white)
![Hexo](https://img.shields.io/badge/Hexo-Static_Blog-0E83CD?logo=hexo&logoColor=white)
![GitHub Pages](https://img.shields.io/badge/GitHub_Pages-Hosted-222222?logo=githubpages&logoColor=white)

线上地址：[https://muxcraft.cfd](https://muxcraft.cfd)

---

## ✨ 功能特性

### 公开站点
- **首页**：毛玻璃顶部导航（滚动淡入）、欢迎动画、横向滚动项目/经历时间线
- **项目板块**：项目卡片横向滚动，封面图渐变叠加，点击进入详情（GITHUB / DEMO 双按钮）
- **技术栈**：按分类分组展示（前端 / 后端 / 工具）
- **经历节点**：时间线排布
- **博客 / 文章**：Hexo + workflow 静态生成，存储 服务器/GitHub Pages
- **留言板**：登录后留言、回复（嵌套"回复 xxx"标识）、头像渲染、管理员角标
- **i18n**：中英一键切换（MuXcraft ↔ 木心工坊）

### 实时聊天
- 右下角悬浮气泡，WebSockeT 实时推送
- 会话列表（置顶 / 删除）、未读红点提醒
- 消息多选删除、清空聊天记录
- 屏蔽用户（双向，后端拦截）
- 移动端自适应，仿微信输入框

### 管理员后台
- 站点 / 资料、项目、经历、技术栈、文章/博客、成就、用户、评论、留言、建议 多 Tab CRUD
- 图片上传（阿里云 OSS / 本地双方案）
- 项目技术栈多选（id 数组绑定）
- 用户管理（搜索 / 分页 / 启用禁用 / 删除确认）
- 表格长文本省略号 + hover 横向滚动 tooltip

---

## 🛠 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3.2 + Vite 3 + Vue Router + 自研 i18n + WebSocket |
| 后端 | Spring Boot 3.2 + MyBatis + JWT + WebSocket |
| 存储 | MySQL + 阿里云 OSS / 本地磁盘 |
| 部署 | Nginx（宝塔反代 `/api/`）+ GitHub Pages（Hexo 静态站） |
| 静态博客 | Hexo + 自研 muxcraft 主题 |

---

## 📁 目录结构

```

muxcraft/                      # 父工程
├── muxcraft-admin/            # 管理员后台
├── muxcraft-chat/             # 聊天模块
├── muxcraft-comment/          # 通用工具模块
├── muxcraft-content/          # 内容展示模块
├── muxcraft-infrastructure/   # 安全架构模块
├── muxcraft-interaction/      # 互动模块
├── muxcraft-system/           # 网站系统模块
├── muxcraft-web/              # 主模块
├──vue-muxcraft/              
其他├── src/                    # 前端工程
    │   ├── api/                # 接口封装
    │   ├── components/         # 通用组件（ChatWidget、MessageDetailModal…）
    │   ├── pages/              # 页面
    │   ├── composables/        # WebSocket / UI 反馈
    │   ├── i18n/               # 中英字典
    │   └── utils/              # 时间格式化等
    ├── hexo-theme-muxcraft/    # Hexo 静态博客主题
    其他

```

---

## 🚀 本地开发

### 前端

```bash
cd vue/muxcraft
npm install
npm run dev      # http://localhost:5173
```

### 后端

```bash
# 需 JDK 17+ / Maven 3.8+
mvn spring-boot:run
# 默认 8080 端口，context-path=/api
```

### 环境变量

```
# application.yml
oss.endpoint / oss.accessKey / oss.secret / oss.bucket
jwt.secret
spring.datasource.url / username / password
```

---

## 📝 接口文档

前端调用规范见仓库内 `API.md`（企业级 REST 文档，含状态码、错误提示、分页、屏蔽/聊天专用接口）。

---

## 📸 预览

> 首页风格：米白纸感底色 + 青绿/淡蓝渐变 + 圆角卡片

```
┌─────────────────────────────────┐
│  MUXCRAFT          EN  [头像]  ≡│
│                                 │
│       欢迎来到                   │
│       木心工坊  (青绿渐变)       │
│                                 │
│   [ 进入工坊 → ]                │
└─────────────────────────────────┘
```

手机端布局：

- 聊天面板全屏 75vh
- 输入框左大右小，仿微信
- 侧边栏比例宽度，滚动锁定背景

---

## 📄 License

MIT © MuXin
所有内容均为真实，网页由 AI 辅助完成。
