# AGY 平台基座工程 (Platform Repository)

基于 Maven 多模块单运行主模块架构的 AGY 平台后端基座工程。

## 📂 模块设计

- 🔐 **`agy-auth`**：OAuth 2.1 认证中心子模块（Token 颁发、非对称加密密钥托管与端点鉴权，无独立启动类）。
- 👥 **`agy-upms`**：平台唯一主运行微服务与权限基础服务（🔥**整个仓库有且仅有这一个 AgyUpmsApplication 启动类▶️**，运行端口: **`9200`**）。

---

## 🛠️ 打包与启动

### 1. 后端构建与打包
```bash
# 根目录下执行 Maven 打包（生成 agy-upms 可执行 Fat Jar）
mvn clean package -DskipTests
```

### 2. 后端启动
* **IDEA 本地启动**：
  直接运行 `com.greatmap.agy.AgyUpmsApplication` 的 `main` 方法（端口：`9200`）。
* **服务器后台启动命令**：
  ```bash
  nohup java -jar agy-upms/target/agy-upms-1.0.0-SNAPSHOT.jar > upms.log 2>&1 &
  ```

### 3. 前端门户说明
所有平台系统管理（用户/角色/菜单/字典/日志）与业务界面已全量收敛至独立的大前端工程 **[`agy-front`](../agy-front/)**（Vue 3.5 + Vite 6 + TypeScript + Element Plus + Pinia）中统一维护与运行。

平时进行具体业务微服务开发时，**无需在本地 IDEA 里打开本工程**！
具体的业务开发请打开业务系统仓库 `agy-util` 或 `agy-business-demo`。

---

## 📖 核心技术与架构规范文档中心 (Docs)

* 📄 **后端架构规格书**：详见 [docs/BACKEND_ARCHITECTURE.md](docs/BACKEND_ARCHITECTURE.md)
  * **Section 4.13**：RSA 密钥对全生命周期 Redis 零配置自旋托管与双令牌无感刷新机制；
  * **Section 4.14**：RBAC 后端方法级强鉴权与垂直越权防护体系（`@EnableMethodSecurity` + `@ss` + Redis 权限上下文）；
  * **Section 4.15**：认证令牌持久化 Fail-Fast 快速熔断机制与 OAuth 2.1 密钥对合一规范（`agy-auth-key-v1` + `{bcrypt}`）。
* 🎨 **前端架构规格书**：详见 [docs/FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md)
  * Vue 3 + Vite + Element Plus + 动态路由鉴权与双 Token 拦截器全生命周期。
* 🗺️ **框架演进规划书**：详见 [docs/FRAMEWORK_PLAN.md](docs/FRAMEWORK_PLAN.md)
