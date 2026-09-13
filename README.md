# 菲林日记 FilmDiary

web 前端课程设计 —— 一个记录观影生活的小应用：发现电影、管理想看/已看清单、写下影评。

## 技术栈

- **前端**：Vue 3（`<script setup>`）+ Vite + Pinia + Vue Router + Bootstrap 栅格
- **后端**：Spring Boot 4（Java 17）+ 原生 JDBC + SQLite
- **电影数据**：[TMDB](https://www.themoviedb.org/) 提供

## 功能

- **首页**：影片库浏览（类型/评分/地区/年份/排序多维筛选）、防抖搜索、无限滚动 + "加载更多"兜底按钮、观影统计卡片
- **发现**：随机推荐，支持想看/写影评快捷操作，并显示该片的记录状态
- **社区**：展示所有用户的影评——评价者、电影信息、评价时间、星级
- **观影记录**：想看 / 已看分类管理，写影评自动标记为已看
- **我的影评**：按电影一篇，支持编辑与删除
- **设置**：修改密码（旧密码校验，新密码加密存储）
- **导航栏**：顶部导航，下滑时两端向中间收拢成悬浮胶囊，回顶展开

## 安全

- 登录签发会话 Token（`X-Auth-Token` 请求头），记录/影评接口需鉴权
- 密码使用 PBKDF2WithHmacSHA256 加盐哈希存储（32768 次迭代），存量明文密码在首次登录时自动升级
- 修改密码后自动注销全部旧会话

## 运行

```bash
# 后端（端口 8080）
mvn spring-boot:run

# 前端开发（端口 5173，/api 代理到 8080）
cd frontend
npm install
npm run dev
```

生产构建：`cd frontend && npm run build`，产物复制到 `src/main/resources/static/` 后由 Spring Boot 直接托管（`base: './'` 相对路径，同时兼容 GitHub Pages / Vercel 部署纯前端）。

## 默认账号

账号 `root`，密码 `123456`（首次登录后请在「设置」中修改）
