# 企业级通用后端架构设计与规范文档 (Backend Architecture Document)

本文档旨在为企业级通用前后端基座脚手架（Base Framework）定义后端架构设计、技术选型、工程解耦形态、分层规范与核心红线。

---

## 1. 核心技术栈与版本标准

| 维度 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **JDK 版本** | **JDK 25 LTS** | 官方长期支持版，具备最新性能与语法特性。 |
| **后端框架** | **Spring Boot 4.1.0** | 最新公网正式发布版，原生支持 JDK 25 与 Jakarta EE 10+。 |
| **通用底层包** | **agy-common 1.0.0-SNAPSHOT** | 顶级独立通用底层包，维护唯一 BaseEntity、自动填充、Result、全局异常、Jackson 与 Security。 |
| **认证与授权中心**| **Spring Authorization Server + 不透明 Token (UUID) + Redis** | 遵循官方最新 OAuth 2.1 规范，废弃密码模式与复杂的 JWT，采用 32 位随机字符串 Token (Opaque Token) + Redis 集中式存储。无公私钥包袱，支持秒级撤销与强制踢人下线。 |
| **持久层框架** | **MyBatis-Plus 3.5.9** | 最新正式版，全面支持 Spring Boot 3/4 与 PostgreSQL 方言。 |
| **数据库与迁移**| **PostgreSQL 16+ + Flyway 10.x** | 高性能开源关系型数据库，Flyway 版本化自动建表与升级。 |
| **JSON 序列化** | **Jackson (Spring Boot 原生)** | 统一使用 Spring 原生 Jackson，配置 Long 转 String（防 JS 精度丢失）、`@Dict` 动态字典自动翻译与 LocalDateTime 格式化，无需引入 Fastjson2。 |
| **在线接口文档**| **SpringDoc OpenAPI 3.0.3 (Swagger)** | Spring Boot 3/4 官方推荐的新一代 Swagger UI 组件，通过 `/swagger-ui/index.html` 访问。 |
| **Excel处理** | **EasyExcel 4.0.3** | 阿里海量数据 Excel 流式导入导出组件，SAX 算法低内存消耗，支持百万级数据防 OOM 导出。 |
| **入参校验** | Jakarta Validation | 服务端 `@Valid` / `@Validated` 入参强校验。 |
| **工具库** | **Hutool 5.8.47** | 最新安全工具库，脱敏、加密与字符串工具集合。 |

---

## 2. 工程解耦解构与物理独立工程形态

为了彻底解决“全量打包部署”、“代码重复定义”与“IDEA 中打开过多项目窗口”的开发痛点，后端采用 **通用底层工程 (`agy-common`)**、**平台基座工程 (`agy-platform`)** 与 **业务微服务工程 (`agy-util` / `agy-business-demo`)** 独立工程解耦结构：

```text
/Users/jianjian/greatmap/workspace/myself/
 ├── agy-dependencies/                  # 📐 全域版本总控 BOM (agy-dependencies.git)
 │
 ├── agy-common/                        # 📦 通用底层工程 (Git 仓库: agy-common.git)
 │    ├── agy-common-core/              # 纯净契约与工具库 (BaseEntity, Result, Exception, Rsa, Jasypt)
 │    └── agy-common-web/               # Web 运行时与自动装配 (Security, Redis, Swagger, TraceId)
 │
 ├── agy-platform/                      # 📂 平台基座工程 (Git 仓库: agy-platform.git)
 │    ├── agy-auth/                     # 🔐 认证中心 (OAuth 2.1 鉴权与 Token 签名)
 │    └── agy-upms/                     # 👥 权限中心 (Port 9200) -> 用户/角色/菜单/部门权限微服务
 │
 ├── agy-front/                         # 🎨 统一大前端门户工程 (Git 仓库: agy-front.git, Vue 3.5 + Vite 6 + Element Plus)
 │
 └── agy-util / agy-business-demo/      # 📂 业务系统工程 (平时开发唯一打开的项目)
      ├── util-api / business-api/      # 📦 契约依赖 (DTO, VO, Client, Validation) —— 仅依赖 agy-common-core
      ├── util-service/                 # 📦 业务持久层 (Entity, Mapper, ServiceImpl, Flyway)
      └── util-web / business-web/      # 🚀 业务运行入口 (Port 9202/9201, Controller, 业务启动类)
```

### 2.1 架构核心优势

1. **绝对零代码重复**：全局只有 `agy-common` 统一维护基础设施与通用契约；
2. **契约轻量化与物理隔离**：`api` 契约包仅依赖 `agy-common-core`，彻底剥离 Web 容器与持久层引擎；
3. **真正的增量部署**：日常迭代代码时，**只需重新打包并部署对应的业务 Jar**；平台基座（`agy-upms`）与底层基座（`agy-common`）部署后长期稳定常驻；
4. **统一单一源点配置**：全域配置收敛，Jasypt 密钥统一托管，开箱即用。

---

## 3. 核心红线守则 (Core Red Lines)

1. **Entity 绝对不能放入 `api` 契约模块**：
   `Entity` 是数据库表结构的物理映射。若放入 `api`，会导致外部调用方被迫引入数据库驱动及 ORM 依赖。`api` 模块只能且只允许使用干净的 `DTO` / `VO`。
2. **Mapper 绝对不能放入 `api` 契约模块**：
   所有的 Mapper 接口与 SQL XML 统一放在 `service` 持久层模块。
3. **Controller 绝对不能放入 `api` 契约模块**：
   Controller 绑定了 HTTP 路由（如 `@RestController`），必须放在 `web` 运行模块。
4. **异常堆栈脱敏**：
   全局异常拦截器绝对禁止将底层 SQL 语法错误、堆栈信息 (StackTrace) 或服务器绝对路径输出给前端，统一转译为友好的错误提示。
5. **绝对禁用拼音命名**：
   包名、类名、变量名、方法名必须具备业务语义，绝对禁止使用中文字符或拼音命名。

---

## 4. 核心规范设计细节

### 4.1 统一三段式响应体结构 (`Result<T>`)
所有 RESTful 接口响应统一使用 `agy-common` 包中的 `Result<T>` 结构，其中 `code = 0` 表示成功：
```json
{
  "code": 0,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1722931200000
}
```

### 4.2 认证与无状态 Token 权限控制 (Spring Authorization Server + Redis)
* **标准 OAuth 2.1 流程**：使用授权码模式 (Authorization Code + PKCE) 与 客户端凭证模式 (Client Credentials)，彻底废弃不安全的密码模式 (Password Grant)。
* **不透明 Token (Opaque UUID Token)**：摒弃繁重的 JWT 包袱，签发 32 位随机字符串 Token，用户信息统一由 Redis 集中式存储。
* **极速注销与下线**：管理员点击踢人下线时直接删除 Redis 对应 UUID，秒级拒绝访问；Header 体积极小（仅 32 字符）。
* **上下文获取**：通过 `agy-common` 中的 `SecurityUtils.getLoginUserId()` 随时获取当前登录人。

### 4.3 第三方 API 接入规范与白名单管控

当外部第三方公司或系统接入调取 API 时，按以下标准流程提供与管控：

#### 4.3.1 我方需要向第三方提供的 3 样内容
1. **专属接入凭证**：后台一键生成的 `client_id` (客户端ID) 与 `client_secret` (客户端密钥)。
2. **Token 换取端点**：标准 HTTP 换凭端点：`POST https://api.yourdomain.com/oauth2/token`
3. **在线 API 文档**：Swagger 在线接口文档链接：`https://api.yourdomain.com/swagger-ui/index.html`

#### 4.3.2 第三方的标准请求规范

**第一步：第三方用凭证换取 32 位 UUID Token**
```http
POST /oauth2/token HTTP/1.1
Host: api.yourdomain.com
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&client_id=client_company_a&client_secret=sec_987654321xyz
```
后端统一响应 JSON：
```json
{
  "access_token": "4f8a9b2c3d4e5f6a7b8c9d0e1f2a3b4c",
  "token_type": "Bearer",
  "expires_in": 7200,
  "scope": "gis:query user:read"
}
```

**第二步：第三方携带 Token 调用业务 API**
```http
GET /api/v1/gis/query?code=330100 HTTP/1.1
Host: api.yourdomain.com
Authorization: Bearer 4f8a9b2c3d4e5f6a7b8c9d0e1f2a3b4c
```

#### 4.3.3 API 接口白名单强管控（简易配置模式）
在系统后台管理中为每个 `client_id` 配置允许调用的接口 URL 白名单（如 `["/api/v1/users/info", "/api/v1/gis/query"]`）。
系统拦截器自动校验：
* 请求 URL 若在白名单中 $\rightarrow$ 放行并记录日志；
* 请求 URL 不在白名单中 $\rightarrow$ 直接拦截返回 `403 权限不足`。
* **随时在后台勾选/停用白名单，无需重启服务，秒级生效**。

### 4.4 敏感数据脱敏与精度防丢失机制 (`JacksonConfig`)
基于 Spring Boot 原生 Jackson 扩展 (定义于 `agy-common`)：
* **`@DataMask` 脱敏**：手机号 `138****1234`、身份证 `110101********0001`。
* **Long 转 String 序列化**：全局将 Java `Long` 类型转为 `String` 返回，防止前端 JavaScript 64 位大整数精度丢失。
* **时间格式化**：`LocalDateTime` 统一格式化为 `yyyy-MM-dd HH:mm:ss`。

### 4.5 百万级数据流式 Excel 导出处理 (EasyExcel)
* 基于 `agy-common` 内置的 `EasyExcelUtils` SAX 流式写算法，数据写一行释放一行内存。
* 搭配 MyBatis-Plus 游标查询 (`ResultHandler`)，实现百万级数据低内存开销防 OOM 导出。
* 单 Sheet 满 100 万行自动分 Sheet 切分。

### 4.6 多服务调用与 RPC 机制
* 当服务 A 需要调用服务 B 的接口时，服务 A 只需引入服务 B 的 `framework-api.jar`。
* 通过 `framework-api` 模块中定义的 `SysUserFeignClient` 发起网络调用，不需要拉取服务 B 的数据库驱动与实体类。

### 4.7 前端枚举字典自动翻译机制 (`@Dict`)
在 `agy-common` 模块内置 Jackson 动态字典序列化器 `DictSerializer` 和注解 `@Dict(dictCode = "user_status")`：
* **业务 VO 定义**：
  ```java
  public class SysUserVO {
      @Dict(dictCode = "user_status")
      private Integer status; // 值为 1
  }
  ```
* **序列化结果**：
  Jackson 会在返回给前端的 JSON 中自动注入扩展字段 `statusLabel`:
  ```json
  {
    "status": 1,
    "statusLabel": "字典翻译_1"
  }
  ```

### 4.8 PostgreSQL 跨时区时间偏差防护与 Flyway 规则 (`Asia/Shanghai`)
* **数据库连接配置**：在 PostgreSQL JDBC 连接串中显式追加 `?stringtype=unspecified&currentSchema=public` 并在应用全局及 JacksonConfig 中显式强指定时区 `Asia/Shanghai`（北京时间）。
* **Flyway 建表规范**：所有时间字段类型统一使用 PostgreSQL 的 `timestamptz` 或 `timestamp`，避免因跨机房/跨海外云部署导致的 8 小时时间偏差。

### 4.9 全局 CORS 跨域配置与 OPTIONS 预检放行 (`CorsFilter`)
在 `agy-common` 模块配置 `CorsFilter` Web 过滤器：
* **允许源与 Header**：允许任意 `Origin` 跨域源（或配置的特定域名），显式允许 `Authorization`、`Content-Type` 自定义请求头；
* **OPTIONS 预检全量放行**：对浏览器的跨域 `OPTIONS` 预检请求直接响应 HTTP 200，保证前端本地开发服务器 (`http://localhost:5006`) 联调无阻碍。

### 4.10 数据库公共字段自动填充处理 (`BaseEntity` + `MetaObjectHandler`)
所有数据库 Entity 实体类继承 `com.greatmap.agy.common.entity.BaseEntity`（包含 `create_time`, `update_time`, `create_by`, `update_by`, `del_flag`）：
* MyBatis-Plus 自动填充 Handler：在插入数据（`insert`）时自动填入当前登录人 ID 与当前时间；在更新数据（`update`）时自动更新修改人与修改时间。
* 彻底免除开发者手写 `setCreateTime(LocalDateTime.now())` 的样板代码。

### 4.11 防重复提交切面 (`@RepeatSubmit`)
在 `agy-common` 提供基于 Redis 的防重复提交注解 `@RepeatSubmit(interval = 3000)`：
* 在新增/修改等写接口 Controller 上加上该注解；
* 当前端因网络延迟连续多次快速点击【提交】按钮时，系统在 Redis 里加锁 3 秒，拦截后续的重复请求并返回 `请勿重复提示` 友好提示。

### 4.12 统一单点登录 (SSO) 与 OAuth 2.1 对接实施指南

本系统提供 **双向单点登录 (SSO) 能力**，支持：
1. **模式一（作为 IDP 身份提供者）**：其它子系统/第三方应用单点登录接入本系统；
2. **模式二（作为 SP 客户端）**：本系统无缝接入外部已有企业级 SSO（如企业微信、钉钉、CAS、Keycloak、Okta、统一政务认证平台）。

---

#### 4.12.1 模式一：本系统作为 IDP 认证中心（提供 OAuth 2.1 授权码 + PKCE 单点登录）

外部系统（第三方 Web、移动端 App、子系统）希望使用本系统的账号登录时，遵循标准 OAuth 2.1 授权码流程：

```mermaid
sequenceDiagram
    autonumber
    actor User as 用户 (浏览器/App)
    participant Client as 接入方系统 (客户端)
    participant AuthServer as agy-auth (OAuth 2.1 认证中心)
    participant UPMS as agy-upms (用户中心)

    User->>Client: 1. 点击“使用 AGY 账号登录”
    Client->>User: 2. 重定向到认证中心授权地址 (/oauth2/authorize)
    User->>AuthServer: 3. 访问 /oauth2/authorize?response_type=code&client_id=...&code_challenge=...
    AuthServer->>User: 4. 显示统一登录界面
    User->>AuthServer: 5. 提交用户名/密码并完成 MFA 校验
    AuthServer->>User: 6. 302 重定向回接入方 redirect_uri?code=AUTH_CODE
    User->>Client: 7. 携带 code 访问接入方系统
    Client->>AuthServer: 8. 后端 POST /oauth2/token (code + code_verifier + client_secret)
    AuthServer->>Client: 9. 验签成功，返回 Access Token (JWT) & Refresh Token
    Client->>AuthServer: 10. GET /userinfo (携带 Access Token)
    AuthServer->>Client: 11. 返回用户详细身份信息 (ID, username, roles, depts)
    Client->>User: 12. 建立会话，进入接入方系统主页
```

##### 实施步骤：
1. **客户端注册**：在 `agy-auth` 的 `AuthorizationServerConfig.java`（或后续扩展的数据库表 `oauth2_registered_client`）中登记客户端：
   * `client_id`：如 `crm-system`
   * `client_secret`：`{noop}crm-secret-123456`
   * `redirect_uris`：`https://crm.company.com/oauth/callback`
   * `scopes`：`openid, profile, read`
2. **标准端点一览**：
   * **授权申请端点**：`GET /oauth2/authorize`
   * **Token 换取端点**：`POST /oauth2/token`
   * **JWK 公钥端点**：`GET /oauth2/jwks`
   * **OIDC 元数据发现**：`GET /.well-known/openid-configuration`
   * **用户信息端点**：`GET /userinfo`

---

#### 4.12.2 模式二：本系统作为客户端对接外部企业 SSO（企业微信/钉钉/CAS/Keycloak）

当企业要求使用现有的统一认证中心（如企业微信、钉钉扫码、统一门户 CAS）登录本系统时，采用标准的 **OAuth2 / OIDC 回调绑定机制**：

```mermaid
sequenceDiagram
    autonumber
    actor User as 用户 (前端 Vue 3)
    participant Vue as agy-front
    participant Backend as agy-upms (后端)
    participant ThirdSSO as 外部统一认证中心 (IDP)

    User->>Vue: 1. 在登录页点击“企业微信/统一认证登录”
    Vue->>ThirdSSO: 2. 跳转到外部 IDP 登录页面
    User->>ThirdSSO: 3. 用户扫码 / 输入企业 SSO 账号
    ThirdSSO->>Vue: 4. 回调本系统前端路由 /oauth/callback?code=THIRD_CODE
    Vue->>Backend: 5. POST /api/auth/sso/callback { code, provider: "wechat" }
    Backend->>ThirdSSO: 6. 后端用 code 换取外部用户唯一标识 (unionId / openId / phone)
    Backend->>Backend: 7. 本地匹配 SysUser (根据 unionId 或手机号)
    alt 未绑定用户
        Backend->>Vue: 8a. 返回需要绑定本地账号或自动生成账号
    else 匹配成功
        Backend->>Backend: 8b. 生成本系统 Token 存入 Redis (7200s TTL)
        Backend->>Vue: 9. 返回 LoginResponseVO (Token + 用户信息 + 权限)
        Vue->>User: 10. 进入系统控制台
    end
```

##### 实施步骤：
1. **新增 SSO 认证接口**：在 `agy-upms` 的 `SysAuthController.java` 中新增端点：
   ```java
   @Operation(summary = "外部 SSO 单点登录回调换取 Token")
   @PostMapping("/sso/callback")
   public Result<LoginResponseVO> ssoCallback(@RequestBody SsoLoginDTO dto) {
       // 1. 调用第三方 API 用 code 换取 openId/phone
       // 2. 根据 openId 查询本地 sys_user 账号
       // 3. 签发系统 UUID Token 并写入 Redis (7200s)
       // 4. 返回 LoginResponseVO (Token + 用户信息 + 权限树)
       return Result.ok(sysUserService.ssoLogin(dto));
   }
   ```
2. **前端路由对接**：在 `agy-front` 的 `src/router/index.ts` 中添加白名单路由 `/oauth/callback`，组件接收 URL 参数中的 `code`，调用 `/api/auth/sso/callback` 完成自动登录并跳转系统主页。

---

---

### 4.13 端到端 RSA 密码传输加密与 Redis 密钥托管机制 (Plan B: 零配置无感自旋)

#### 4.13.1 设计背景与核心痛点
* **传统明文风险**：前端直接将用户输入的明文密码（如 `"123456"`）发送到后端接口，在浏览器 F12 控制台 Payload、Fiddler/Charles 抓包工具及网络中继节点中完全透明可见，存在极高安全合规风险；
* **配置文件长字符串痛点**：RSA 2048 位的公私钥 Base64 字符串长达 1600+ 字符，直接写在 `application.yml` 会严重污染配置文件，破坏架构整洁性；
* **服务重启错位痛点**：若纯内存随机自旋生成密钥，后端重启后停留在登录页的用户会因公钥版本错位导致首次登录失败。

#### 4.13.2 架构全景与 Redis 托管工作流

```mermaid
sequenceDiagram
    autonumber
    participant Backend as 后端服务 (RsaKeyManager)
    participant Redis as Redis 缓存 (sys:rsa:keypair)
    participant Frontend as 前端 (Web Crypto API)
    actor User as 用户

    Note over Backend,Redis: 【阶段一：后端启动 / 重启零配置托管】
    Backend->>Redis: 1. 启动时检查是否存在 key: "sys:rsa:public_key"
    alt Redis 已存在有效密钥对
        Redis-->>Backend: 2a. 返回既有 2048 位公钥与私钥
        Backend->>Backend: 3a. 直接热加载复用既有密钥对
    else Redis 无密钥对 (首次初始化 / TTL 到期)
        Backend->>Backend: 2b. 自动生成全新 2048 位 RSA 密钥对
        Backend->>Redis: 3b. 存入 Redis (设置 30 天托管有效期)
    end

    Note over Frontend,User: 【阶段二：前端打开登录页与密码传输加密】
    User->>Frontend: 4. 用户打开系统登录页面
    Frontend->>Backend: 5. 自动请求 GET /api/auth/public-key
    Backend-->>Frontend: 6. 返回当前有效的 RSA 2048 位 Base64 公钥
    User->>Frontend: 7. 输入账号密码并点击登录
    Frontend->>Frontend: 8. 原生 window.crypto.subtle (RSA-OAEP-SHA256) 加密为密文
    Frontend->>Backend: 9. 发送 POST /api/auth/login { username, password: "密文" }
    Backend->>Backend: 10. RsaUtils 使用私钥解密并比对 BCrypt Hash
    Backend->>Redis: 11. 保存 2小时 Access Token + 7天 Refresh Token
    Backend-->>Frontend: 12. 返回登录成功及双令牌凭证
```

#### 4.13.3 核心技术指标与落地细节
1. **零配置文件污染**：
   * `application.yml` 与前端 `.env` 中**无需硬编码任何 RSA 密钥长字符串**；
   * 仅需按需配置 Token 有效期即可：
     ```yaml
     framework:
       security:
         token:
           access-token-ttl: 7200      # 2小时
           refresh-token-ttl: 604800   # 7天
     ```
2. **集群分布式天然一致**：
   * 多实例后端集群部署时，所有节点统一从同一个 Redis 加载公私钥，解密绝对一致；
   * 后端单机重启、滚动更新时，密钥从 Redis 稳定读取，前端用户开着网页点登录 100% 成功，无需刷新页面；
3. **前端硬件加速与零依赖**：
   * 前端使用 W3C 标准 Web Crypto API（`window.crypto.subtle`），无需引入外部庞大 JS 库，执行速度极快；
4. **双令牌静默无感续期（7 天免密续航）**：
   * 登录成功后，前端保存 2h `accessToken` 与 7d `refreshToken`；
   * 当 `accessToken` 过期返回 401 时，Axios 拦截器自动挂起所有并发请求，并在后台静默调用 `POST /api/auth/refresh` 换取新令牌后自动重放原业务请求，用户界面零卡顿、无感知。

---

### 4.14 RBAC 后端方法级强鉴权与垂直越权防护体系 (`@EnableMethodSecurity` + `@ss` + Redis 权限上下文)

#### 4.14.1 设计背景与安全隐患消除
* **前端防君子不防小人**：前端 `v-permission` 仅控制 DOM 按钮显隐。若后端 API 没有授权校验，攻击者可通过 Postman/F12 携带合法普通 Token 直接调用 `DELETE /api/users/1` 实施垂直越权破坏；
* **有认证无授权断层消除**：彻底废弃一律发放 `ROLE_AUTHENTICATED` 的粗粒度做法，实现“**认证验身份、授权验权限**”的工业级安全闭环。

#### 4.14.2 架构全景与鉴权执行流程

```mermaid
sequenceDiagram
    autonumber
    actor User as 客户端 (普通用户 / 攻击者)
    participant Filter as TokenAuthFilter 过滤器
    participant Redis as Redis (token:perms:UUID)
    participant SpringSec as Spring Security 上下文
    participant Controller as 业务 Controller (@PreAuthorize)

    User->>Filter: 1. 携带 Token 发起请求 (如 DELETE /api/users/1)
    Filter->>Redis: 2. 查 Token 认证并提取权限列表 [token:perms:UUID]
    Redis-->>Filter: 3. 返回该用户的真实权限标识集合
    Filter->>SpringSec: 4. 组装 GrantedAuthority 并存入 SecurityContextHolder
    Filter->>Controller: 5. 执行控制器方法前触发 @PreAuthorize 拦截
    alt 拥有对应权限 (或拥有 *:*:* 超管特权)
        Controller->>User: 6a. 授权通过，执行 Service 并返回 Result.ok()
    else 权限不足 (未分配对应权限标识)
        Controller-->>SpringSec: 6b. 抛出 AccessDeniedException
        SpringSec-->>User: 7. GlobalExceptionHandler 捕获并统一返回 403 Forbidden Result
    end
```

#### 4.14.3 核心技术落地规范
1. **开启方法级安全注解**：在 `SecurityConfig` 显式标注 `@EnableMethodSecurity(prePostEnabled = true)`；
2. **自定义权限表达式引擎 (`@ss`)**：
   * 在 `agy-common` 中提供 `PermissionService`（Bean 名为 `ss`），支持：
     * `@PreAuthorize("@ss.hasPerm('sys:user:delete')")`：精确权限匹配（超级管理员 `*:*:*` 自动放行）；
     * `@PreAuthorize("@ss.hasAnyPerms('sys:user:add', 'sys:user:edit')")`：多权限或匹配；
     * `@PreAuthorize("@ss.hasRole('admin')")`：角色匹配；
3. **Controller 接口全覆盖防护矩阵**：
   * 用户管理：`@PreAuthorize("@ss.hasPerm('sys:user:...')")`
   * 角色权限：`@PreAuthorize("@ss.hasPerm('sys:role:...')")`
   * 菜单管理：`@PreAuthorize("@ss.hasPerm('sys:menu:...')")`
   * 字典管理：`@PreAuthorize("@ss.hasPerm('sys:dict:...')")`
   * 日志管理：`@PreAuthorize("@ss.hasPerm('sys:log:...')")`
4. **统一 403 异常转译**：
   * `GlobalExceptionHandler` 捕获 `AccessDeniedException`，统一输出标准 JSON：
     ```json
     {
       "code": 40301,
       "message": "抱歉，您没有操作该资源的权限，请联系管理员分配",
       "data": null,
       "timestamp": 1786111200000
     }
     ```

---

### 4.15 认证令牌持久化 Fail-Fast 快速熔断机制与 OAuth 2.1 密钥对合一规范

#### 4.15.1 痛点与体验歧义消除
* **静默吞异常导致 401 闪退**：早期若为方便无 Redis 单测而捕获写入异常，会导致“假装登录成功返回 Token，但 Redis 无记录，用户进入系统下 1 毫秒即被 401 踢出”，产生极具误导性的幽灵体验歧义；
* **OAuth 2.1 授权中心密钥错位**：若使用脚手架随机生成 RSA 密钥对和随机 KeyID，每次服务重启都会导致此前签发的 JWT Token 签名失效，造成大面积单点登录雪崩。

#### 4.15.2 核心改造前后对比矩阵

| 维度 | 改造前 (痛点风险) | 改造后 (企业级规范) | 收益 / 效果 |
| :--- | :--- | :--- | :--- |
| **登录令牌 Redis 写入异常** | `try-catch` 仅打日志并吞咽异常，继续返回 200 与 Token | 抛出 `BusinessException` 立即熔断阻断登录流程 | 遵循 **Fail-Fast** 原则，绝不下发查不到的假 Token，杜绝后续 401 闪退 |
| **OAuth 2.1 JWK 密钥生成** | 内存每次随机 `generateKeyPair()` + 随机 KeyID | 复用 `RsaUtils` 从 Redis 读取同一套 2048 位公私钥，固定 KeyID `agy-auth-key-v1` | 服务重启或多节点滚动部署，已签发 JWT 100% 验签稳定通过 |
| **OAuth 2.1 Client Secret** | `{noop}agy-secret-123456` 明文存储 | `{bcrypt}` + BCrypt 强哈希散列存储 | 符合等保三级安全合规要求，防止凭据泄露 |

#### 4.15.3 架构全景与执行流程

```mermaid
sequenceDiagram
    autonumber
    actor Client as 客户端 / OAuth 2.1 接入方
    participant AuthServer as 认证服务 (SysUserService / AuthServer)
    participant Redis as Redis (sys:rsa & token)
    participant GlobalEx as GlobalExceptionHandler

    rect rgb(240, 248, 255)
    Note over AuthServer, Redis: 场景 1：统一 RSA 密钥托管 (JWKS 与密码解密)
    AuthServer->>Redis: 启动时加载统一 RSA 密钥 (sys:rsa:public_key / private_key)
    Redis-->>AuthServer: 返回稳定的 2048 位公私钥
    AuthServer->>Client: 对外暴露固定 KeyID (agy-auth-key-v1) JWKS 端点与密码公钥
    end

    rect rgb(255, 245, 245)
    Note over Client, GlobalEx: 场景 2：登录持久化 Fail-Fast 快速失败
    Client->>AuthServer: 发起登录请求 (POST /api/auth/login)
    AuthServer->>Redis: 尝试写入 Access Token 与 Permissions (token:UUID)
    alt Redis 正常
        Redis-->>AuthServer: 写入成功
        AuthServer-->>Client: 返回 200 OK 与有效双令牌
    else Redis 宕机 / 超时 / 异常
        Redis--xAuthServer: 抛出 RedisConnectionException
        AuthServer->>GlobalEx: 捕获并立即抛出 BusinessException("认证服务异常，令牌写入失败...")
        GlobalEx-->>Client: 统一返回 HTTP 200 (code: 50000, 明确提示服务异常)
    end
    end
```

#### 4.15.4 关键类与代码落地规范
1. **Fail-Fast 熔断实现**：
   * 在 `SysUserServiceImpl`（`agy-platform` 与 `agy-business`）中统一捕获异常并抛出明确的业务异常：
     ```java
     try {
         if (stringRedisTemplate != null) {
             stringRedisTemplate.opsForValue().set(AuthConstants.TOKEN_PREFIX + accessToken, userPayload, Duration.ofSeconds(accessTtl));
             stringRedisTemplate.opsForValue().set(AuthConstants.TOKEN_PERMS_PREFIX + accessToken, JSONUtil.toJsonStr(perms), Duration.ofSeconds(accessTtl));
             stringRedisTemplate.opsForValue().set(AuthConstants.REFRESH_TOKEN_PREFIX + refreshToken, userPayload, Duration.ofSeconds(refreshTtl));
         }
     } catch (Exception e) {
         log.error("用户 [{}] 登录令牌写入 Redis 异常 (Fail-Fast 快速失败熔断): ", dto.getUsername(), e);
         throw new BusinessException("认证服务异常，令牌写入失败，请检查缓存服务状态");
     }
     ```
2. **OAuth 2.1 密钥打通实现**：
   * 在 `AuthorizationServerConfig` 中统一注入：
     ```java
     @Bean
     @ConditionalOnMissingBean
     public JWKSource<SecurityContext> jwkSource() {
         RSAPublicKey publicKey = RsaUtils.getPublicKey();
         RSAPrivateKey privateKey = RsaUtils.getPrivateKey();
         RSAKey rsaKey = new RSAKey.Builder(publicKey)
                 .privateKey(privateKey)
                 .keyID("agy-auth-key-v1")
                 .build();
         return new ImmutableJWKSet<>(new JWKSet(rsaKey));
     }
     ```
3. **OAuth 2.1 客户端数据库持久化存储 (`JdbcRegisteredClientRepository`)**：
   * 基于 PostgreSQL 基础表 `oauth2_registered_client` 在线动态维护客户端信息；
   * 平台启动时自动检查并安全注册默认内部种子客户端 `agy-client`（Secret 采用 `{bcrypt}` 强哈希），后续运维管理员可随时在数据库/管理端新增移动端、大屏端接入凭据，无需重启服务：
     ```java
     @Bean
     @ConditionalOnMissingBean
     public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
         return new JdbcRegisteredClientRepository(jdbcTemplate);
     }
     ```
4. **OAuth 2.1 签发者 Issuer 生产级动态推导**：
   * 支持通过 `application.yml` 的 `framework.security.oauth2.issuer` 显式指定生产外部域名；
   * 缺省未配置时自动根据请求的 `Host` / `X-Forwarded-Host` Header 进行智能动态推导，完美兼容各类 Nginx 反向代理与多域名单点登录架构：
     ```java
     @Bean
     @ConditionalOnMissingBean
     public AuthorizationServerSettings authorizationServerSettings() {
         AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
         if (StrUtil.isNotBlank(issuer)) {
             builder.issuer(issuer.trim());
         }
         return builder.build();
     }
     ```

---

### 4.16 Controller 接口显式安全意图声明规范与 CI/CD 架构单测卡点体系

#### 4.16.1 痛点与 Fail-Open 越权风险防范
* **方法级鉴权的天然短板**：若仅在方法上使用 `@PreAuthorize` 鉴权，一旦开发人员在新增敏感业务接口（如批量删除、重置密码、导出数据）时**疏忽漏写了注解**，由于 HTTP 层委托给了方法注解，该裸接口将面临被任意登录普通用户直接调用执行的**垂直越权（Fail-Open）风险**；
* **消灭无安全声明的裸接口**：制定“**显式声明安全意图**”铁律，所有公开对外的 Controller 映射方法必须且只能归入以下 3 类清晰声明之一。

#### 4.16.2 三类标准安全声明矩阵

| 接口类型 | 声明注解 | 适用场景 | 鉴权逻辑 |
| :--- | :--- | :--- | :--- |
| **① 敏感业务接口**<br>(需特定 RBAC 按钮权限) | `@PreAuthorize("@ss.hasPerm('sys:user:delete')")` | 用户/角色/菜单增删改、敏感业务操作 | 必须拥有对应权限字符（超管 `*:*:*` 自动放行），无权限报 **403** |
| **② 登录通用接口**<br>(仅需登录，无需特定权限) | `@PreAuthorize("isAuthenticated()")` | 获取当前登录人个人资料、修改自己密码、拉取已授权树形菜单 | 必须持有合法有效的 Token，未登录报 **401** |
| **③ 完全公开接口**<br>(免登录、匿名公开访问) | `@Anonymous` 或 `@PermitAll` | 账号密码登录、获取验证码、拉取 RSA 加密公钥、公开展示大屏 | 任何人免 Token 均可直接调用 |

#### 4.16.3 CI/CD 架构安全守护单测卡点 (`FrameworkArchitectureTest`)
* **零运行时开销**：在测试工程中通过反射与 Spring 元数据扫描所有 Controller 方法；
* **自动拦截裸接口**：若发现任何没有标注 `@PreAuthorize`、`@Anonymous` 或 `@PermitAll` 的裸接口，`mvn test` **直接抛出断言异常并中断构建发布流水线**：
  ```text
  ❌ 发现未声明安全意图的裸接口 (Fail-Open 风险): SysUserController#batchDelete()，必须显式标注 @PreAuthorize(...)、@PreAuthorize("isAuthenticated()")、@Anonymous 或 @PermitAll
  ```

---

## 5. 核心基础工程与 Git 仓库清单

| 工程名称 | Git 仓库地址 | 责任人 / 作用 |
| :--- | :--- | :--- |
| **`agy-backend`** | `https://github.com/jansenlian/agy-backend.git` | 全局后端统一聚合大工程 (含 common, dependencies, platform, util, geo, agent 等) |
| **`agy-front`** | `https://github.com/jansenlian/agy-front.git` | 统一 Web 门户大前端 (Vue 3 + Vite + Element Plus) |
| **`personal-wiki`** | `https://github.com/jansenlian/personal-wiki.git` (Private) | 个人私有知识库与运维自动化脚本 |

---

## 6. 项目工程包结构路径

根包名统一采用反域名格式 `com.greatmap.agy`：

```text
com.greatmap.agy/
 ├── (agy-common 模块)
 │    ├── aspect/          # 切面逻辑 (RepeatSubmitAspect)
 │    ├── config/          # 全局配置 (SecurityConfig, JacksonConfig, CorsConfig)
 │    ├── desensitize/     # 数据脱敏 (@DataMask, DataMaskSerializer)
 │    ├── dict/            # 动态字典翻译 (@Dict, DictSerializer)
 │    ├── domain/          # 统一响应 (Result<T>)
 │    ├── entity/          # 通用数据库实体基类 (BaseEntity)
 │    ├── exception/       # 业务异常 (ErrorCode, BusinessException, GlobalExceptionHandler)
 │    ├── excel/           # 海量数据 Excel 导入导出工具 (EasyExcelUtils)
 │    ├── handler/         # 自动填充 Handler (MyBatisPlusMetaObjectHandler)
 │    └── utils/           # 通用工具 (SecurityUtils, TraceIdFilter)
 │
 ├── (api 契约模块: util-api / business-api)
 │    ├── client/          # 声明式 HTTP / Feign 客户端 (SysUserClient)
 │    ├── dto/             # 请求 DTO (LoginRequestDTO, SysUserCreateDTO)
 │    └── vo/              # 响应 VO (LoginResponseVO, SysUserVO, SysMenuVO)
 │
 ├── (service 业务持久层: util-service / business-service)
 │    ├── entity/          # 业务实体 (SysUser, SysRole, SysMenu, SysLoginLog 继承 BaseEntity)
 │    ├── mapper/          # MyBatis-Plus Mapper (SysUserMapper, SysRoleMapper...)
 │    └── service/         # 业务接口与实现 (SysUserService, SysUserServiceImpl...)
 │
 └── (web 运行入口模块: util-web / business-web)
      ├── controller/      # RESTful 控制器 (SysUserController, SysAuthController...)
      └── AgyWebApplication.java  # 业务服务启动类
```

---

## 7. AGY 三大核心能力底座接入与集成规范 (SDK & Starter Guide)

为保证全集团各微服务技术栈高度统一，业务系统应按需引入以下三大核心能力底座：

### 7.1 `agy-common`（全局通用底层基座）

#### ① Maven 依赖引入
```xml
<dependency>
    <groupId>com.greatmap.agy</groupId>
    <artifactId>agy-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### ② 赋能核心能力
* **统一响应包装**: `Result.success(data)`、`Result.fail(code, message)`；
* **全局异常体系**: 抛出 `BusinessException`，自动被 `GlobalExceptionHandler` 拦截为标准响应，防堆栈泄露；
* **全链路 TraceId**: Logback 自动携带 MDC `traceId`，配合 `TraceIdFilter` 实现前后端全链路追踪；
* **隐私脱敏与字典**: 实体字段标注 `@DataMask(MaskType.MOBILE)` 或 `@Dict(dictCode = "user_status")` 自动序列化翻译；
* **防重复提交**: 方法标注 `@RepeatSubmit(interval = 5000)` 自动基于 Redis 分布式锁防抖。

---

### 7.2 `agy-agent-common`（AI 智能体自动装配 Starter）

#### ① Maven 依赖引入
```xml
<dependency>
    <groupId>com.greatmap.agy</groupId>
    <artifactId>agy-agent-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### ② 业务配置文件 `application.yml`
```yaml
agy:
  agent:
    enabled: true
    base-url: https://api.deepseek.com
    api-key: ${DEEPSEEK_API_KEY:sk-xxxxxxxxxxxxxxxxxxxx}
    model-name: deepseek-chat
    temperature: 0.5
    max-messages: 10          # 单用户会话记忆窗口大小
    log-requests: false
    log-responses: false
```

#### ③ 业务 Tool 极速挂载与调用
业务微服务中只需在任意 Spring Bean 方法上添加 `@Tool` 注解，Starter 会在启动期自动扫描并将其注册为大模型可调用的 Function Calling 工具：

```java
@Component
public class MyBusinessTools {

    @Tool("根据合同编号查询合同详细执行进度与回款金额")
    public String queryContractDetail(@P("合同编号，如 HT2026001") String contractNo) {
        // 执行数据库查询并返回结构化结果或 JSON 字符串
        return contractService.getDetail(contractNo);
    }
}
```

#### ④ 自动提供的开箱即用接口
* `POST /api/agent/chat`: 同步智能问答
* `GET/POST /api/agent/chat/stream`: SSE 打字机流式实时问答（前端通过 EventSource 或 Fetch 实时逐字渲染）
* `DELETE /api/agent/memory/{memoryId}`: 重置清空指定会话记忆

---

### 7.3 `agy-geo`（CAD / GIS 空间算法与矢量化工具包）

#### ① Maven 依赖引入
```xml
<!-- 引入全量空间工具包 (含 JTS 构面与 DWG 二进制提取) -->
<dependency>
    <groupId>com.greatmap.agy</groupId>
    <artifactId>geo-all</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

或按需仅引入子模块：
* `com.greatmap.agy:geo-core`: 纯 JTS 几何运算、坐标系变换与拓扑构面；
* `com.greatmap.agy:geo-dwg`: AutoCAD DWG/DXF 文件流解析器。

#### ② 核心 API 调用代码
```java
// 1. 本地内存流式解析 DWG 图纸
ParseOptions options = ParseOptions.builder()
        .polygonize(true)        // 自动拓扑构面
        .matchAttributes(true)   // 挂接地块文字注记
        .minAreaThreshold(0.01)  // 过滤碎图斑
        .build();

// 2. 提取标准 GeoJSON FeatureCollection (直接供前端 OpenLayers/Mapbox 渲染)
GeoJsonResult result = DwgFeatureExtractor.extract(inputStream, options);
```

#### ③ 独立微服务运行
若需要作为独立微服务运行，直接启动 `geo-dwg` 模块下的 `DwgToolApplication.java`（端口: 8088），即可通过 Swagger 或 `/api/v1/cad/*` 接口对外提供服务。
