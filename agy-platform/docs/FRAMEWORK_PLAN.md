# 全局通用前后端基座框架（Base Framework）搭建实施计划

本计划旨在综合整合 `jinhua/fj` 与 `ymgd/yj` 两个项目的优点，打造一套**生产级、自给自足、物理隔离、强类型约束**的通用前后端脚手架框架。

> **已敲定核心规格书**：
> - 📄 **后端架构规格书**：详见 [BACKEND_ARCHITECTURE.md](file:///Users/jianjian/greatmap/workspace/myself/BACKEND_ARCHITECTURE.md)
> - 📄 **前端架构规格书**：详见 [FRONTEND_ARCHITECTURE.md](file:///Users/jianjian/greatmap/workspace/myself/FRONTEND_ARCHITECTURE.md)

---

## 核心技术选型与规范状态

| 模块 | 技术选型 | 说明 / 状态 |
| :--- | :--- | :--- |
| **JDK 版本** | **JDK 25 LTS** | **[已确定]** 长期支持版，具备最新性能与语法特性。 |
| **后端框架** | **Spring Boot 4.1.0** | **[已确定]** 最新公网正式版，原生支持 JDK 25。 |
| **持久层框架** | **MyBatis-Plus 3.5.17** | **[已确定]** 最新正式版，原生适配 PostgreSQL。 |
| **数据库/迁移** | **PostgreSQL 16+ + Flyway** | **[已确定]** 高性能 PostgreSQL，自动 Flyway 版本管理。 |
| **认证与授权** | **Spring Auth Server + UUID + Redis** | **[已确定]** OAuth 2.1 授权码 PKCE 模式 + 纯 UUID Token 集中管控，淘汰 JWT。 |
| **Excel 组件** | **EasyExcel 4.0.3** | **[已确定]** 百万级数据流式防 OOM 导入导出。 |
| **后端架构** | **4 模块单向依赖架构** | **[已确定]** 对齐 `java-backend-standards` 物理隔离规范。 |
| **基础包名** | **`com.greatmap.agy`** | **[已确定]** 反域名无拼音规范。 |
| **前端环境** | Node.js 24 LTS + pnpm 9.x | **[已确定]** 官方最新 Active LTS 长期支持版 + 全局缓存加速。 |
| **前端框架** | Vue 3.5+ + Vite 6+ | **[已确定]** 组合式 API + 秒级热重载。 |
| **前端语言** | TypeScript 5.x | **[已确定]** 100% 接口出入参强类型覆盖，彻底规避 undefined。 |
| **UI 框架** | Element Plus 2.8+ | **[已确定]** 现代企业级后台 UI 组件库。 |
| **前端路由** | Vue Router 4.x (History 模式) | **[已确定]** HTML5 History 模式 (`createWebHistory`) + 100% 后端动态路由。 |

---

## 拟建项目目录结构设计

采用 **平台基座工程 (`agy-platform`)** 与 **业务系统工程 (`agy-business`)** 双独立项目彻底解耦形式：

```text
/Users/jianjian/greatmap/workspace/myself/
 ├── agy-common/                        # 📂 公共底座起步依赖 (com.greatmap.agy.common)
 ├── agy-platform/                      # 📂 独立工程 A：平台基座工程 (auth + upms，常驻运行，IDEA无需打开)
 └── agy-business/                      # 📂 独立工程 B：业务系统工程 (业务领域微服务 + 前端工程)
```
