# AGY Backend (企业级智能体与微服务架构全栈后端)

`agy-backend` 是基于 Java 25、Spring Boot 4.1.x、MyBatis-Plus、Baomidou 多数据源及 LangChain4j 构建的高性能企业级后端聚合工程 (Mono-Repo)。

---

## 一、 模块架构全景

```text
agy-backend/
├── agy-dependencies/      # [BOM] 全局依赖版本统一总控中心
├── agy-common/            # [Core] 通用底层能力基座 (认证安全、统一响应、异常收敛、Jasypt 加密、TraceId)
│   ├── agy-common-core    # 核心工具集与基础领域模型
│   └── agy-common-web     # Web 统一异常拦截、AOP 审计日志、防重复提交与安全上下文
├── agy-agent-common/      # [AI Starter] 大模型与智能体通用底座 (DeepSeek、LangChain4j、@Tool 动态注入、SSE 流式响应)
├── agy-geo/               # [Spatial] CAD/GIS 空间数据矢量计算与 DWG/DXF 转码微服务
│   ├── geo-core           # JTS 空间拓扑计算与几何算法库
│   ├── geo-dwg            # DWG/DXF 工业图纸解析与转码服务 (Port: 8088)
│   └── geo-all            # 空间能力聚合装配包
├── agy-platform/          # [Platform] 企业级中台认证与 UPMS 权限服务 (Port: 9200)
│   ├── agy-auth           # OAuth 2.1 / Token 认证中枢
│   └── agy-upms           # 用户、角色、权限与系统元数据微服务
├── agy-util/              # [DevOps Tools] 研发效能与安全工具微服务 (Port: 9202)
├── agy-aks-agent/         # [Agent] 耕地空间数据分析与全景问答智能体微服务 (Port: 8092)
├── agy-hr-agent/          # [Agent] 智能人事与运维资产 360° 全景问答微服务 (Port: 8090)
└── agy-business-demo/     # [Demo] 业务模块演示与最佳实践脚手架 (Port: 9201)
```

---

## 二、 技术栈与核心特性

- **语言与核心框架**: Java 25, Spring Boot 4.1.x
- **持久层 & 数据源**: MyBatis-Plus 3.5.x, Baomidou Dynamic DataSource (动态多数据源), Flyway 10.x 数据库版本管理
- **大模型与智能体**: LangChain4j 0.36.x, DeepSeek-V3 / R1, 动态 Tool 注册, SSE 流式输出, 内存上下文记忆
- **安全与认证**: Spring Security, Dual Token (Access Token 2h / Refresh Token 7d), RSA 非对称加解密
- **配置与脱敏**: Jasypt 配置加密, 统一环境变量占位符注入, 敏感字段全局日志自动打码
- **API 规范与文档**: SpringDoc OpenAPI 3.0 (Knife4j / Swagger UI)

---

## 三、 快速开始

### 1. 环境依赖
- JDK 25+
- Maven 3.9+
- PostgreSQL 16+
- Redis 7.0+

### 2. 全局编译构建
在项目根目录下执行:
```bash
mvn clean compile -DskipTests
```

### 3. 本地启动服务
各微服务支持独立启动，例如启动 UPMS 平台服务:
```bash
mvn spring-boot:run -pl agy-platform/agy-upms
```

### 4. 环境变量配置说明
推荐通过环境变量注入敏感凭据（本地运行若不配置则自动使用 dev 默认占位）：
- `DB_URL`: PostgreSQL 数据库连接串
- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_HOST`: Redis 主机地址
- `REDIS_PASSWORD`: Redis 访问密码
- `DEEPSEEK_API_KEY`: DeepSeek 大模型 API 密钥
- `GITLAB_TOKEN`: GitLab 访问令牌
