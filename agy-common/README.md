# agy-common: AGY 企业级极速基座通用核心依赖包

`agy-common` 是面向现代微服务及单体应用打造的**通用底层基础设施与开箱即用核心库**.基于 Spring Boot 4.1.0、Java 25 与 MyBatis-Plus 构建，采用 Spring Boot SPI 自动装配机制（`AutoConfiguration.imports`）。

业务子系统（如 `dwg-tool`、`agy-platform`、`jinhua` 等）只需在 `pom.xml` 中引入本依赖，**无需编写繁琐配置类，即可实现 90% 以上核心能力的零配置秒级接入**.

---

## 🌟 核心功能矩阵 (6大 支柱能力)

```
agy-common
├── 1. 全链路日志与可观测体系 (TraceId / 慢接口 / 慢SQL / 异步透传)
├── 2. 统一接口契约与异常收敛体系 (Result / BusinessException / GlobalExceptionHandler)
├── 3. 数据持久化与持久层安全防线 (MyBatis-Plus / 防全表更新删除 / 乐观锁 / 自动填充)
├── 4. 业务操作审计与隐私数据脱敏 (@Log / PII正则脱敏 / @DataMask / 大报文截断)
├── 5. 传输安全与加密认证防护 (RSA非对称端到端加密 / BCrypt / CORS白名单)
└── 6. 企业级高频工具库与能力增强 (EasyExcel / Jasypt配置加密 / 接口防重提交 / IP与UA解析)
```

---

### 1. 📊 全链路日志与可观测体系 (纯日志, 0数据库负担)

| 组件 | 类名 / 路径 | 功能说明 | 接入方式 |
| :--- | :--- | :--- | :--- |
| **MDC 链路追踪** | `TraceIdFilter.java` | 拦截 HTTP 请求，从 `X-Trace-Id` 获取或自动生成 UUID 注入 MDC，并在响应头回传 | 自动装配，零配置生效 |
| **异步线程池透传** | `MdcTaskDecorator.java` | 自动将主线程 MDC TraceId 复制给 `@Async` 及子线程，任务结束后自动清理 | 自动装配进 `AsyncConfig` |
| **非 HTTP 链路工具** | `TraceUtils.java` | 供定时任务（`@Scheduled`）、MQ 消费者及后台线程手动/函数式绑定 TraceId | `TraceUtils.runWithTrace(...)` |
| **全局请求出入参切面** | `WebLogAspect.java` | 拦截 Controller，记录请求 IP、URL、方法、参数摘要，**耗时 >1000ms 自动触发慢接口告警** | 自动装配，零注解生效 |
| **MyBatis 慢 SQL 监控** | `SlowSqlInterceptor.java` | 毫秒级统计单条 SQL 执行时间，快路径开销 `<0.01%`，**SQL 耗时 >500ms 自动打印慢 SQL 告警** | 自动装配，零配置生效 |

---

### 2. 🛡️ 统一接口契约与全局异常收敛

- **统一响应体 (`Result<T>`)**:
  - 提供标准化 RESTful 结构: `{ "code": 200, "message": "操作成功", "data": {...}, "traceId": "a1b2c3d4", "timestamp": 1786589833 }`
  - 包含快捷构造方法: `Result.ok()`, `Result.ok(data)`, `Result.fail(message)`, `Result.fail(code, message)`
- **全局异常拦截器 (`GlobalExceptionHandler`)**:
  - 自动捕获 `BusinessException` 业务异常，输出友好提示.
  - 自动拦截 `MethodArgumentNotValidException` 参数校验异常，提取精准错误字段.
  - 拦截所有未处理底层 `Exception`，记录 ERROR 日志并对外统一响应 `系统繁忙，请稍后重试`，**彻底杜绝底层数据库异常或代码堆栈直接泄露给前端**.

---

### 3. 💾 数据持久化与持久层安全防线

- **防全表更新与删除插件 (`BlockAttackInnerInterceptor`)**:
  - 物理阻断任何无 `WHERE` 条件的 `UPDATE` 或 `DELETE` 危险 SQL，防止生产环境误删全表数据.
- **乐观锁支持 (`OptimisticLockerInnerInterceptor`)**:
  - 实体增加 `@Version` 注解即可自动启用并发乐观锁防护.
- **审计字段自动填充 (`MyBatisPlusMetaObjectHandler`)**:
  - 继承 `BaseEntity` 的实体在执行插入/更新时，自动注入 `createTime`, `createBy`, `updateTime`, `updateBy`, `delFlag`.
- **通用实体基类**:
  - `IdEntity`: 提供 `Long id` 主键定义与雪花算法自增策略.
  - `BaseEntity`: 包含主键、审计字段、逻辑删除标识的基础抽象类.

---

### 4. 📝 业务操作审计与数据脱敏

- **声明式操作日志注解 (`@Log`)**:
  - 在 Controller 方法上标注即可记录操作审计:
    ```java
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public Result<Void> addUser(@RequestBody UserDTO dto) { ... }
    ```
- **PII 隐私字段智能正则脱敏**:
  - `LogAspect` 在捕获请求参数和响应体时，自动对 `password`, `token`, `mobile`, `idCard` 等敏感字段打码为 `******`.
- **大报文截断保护**:
  - 操作日志内容超过 2000 字符时自动熔断截断，防止数据库字段撑爆或引发 OOM.
- **Spring Event 异步解耦**:
  - 切面仅发布 `SysOperLogEvent` 领域事件，由具体业务系统（如 `agy-platform`）按需监听并异步入库，对业务主线程 0 阻塞.
- **Jackson 响应数据脱敏 (`@DataMask`)**:
  - 支持在实体类字段上标注 `@DataMask(type = DataMaskType.PHONE)`，序列化给前端时自动打码.

---

### 5. 🔒 传输安全与加密认证防护

- **RSA 端到端非对称加密 (`RsaUtils` / `RsaKeyManager`)**:
  - 前端使用公钥对密码、身份证等核心隐私密文传输，后端私钥解密，拒绝明文泄露.
  - 支持 Redis 密钥对托管与本地内存保底降级机制.
- **密码安全哈希 (`PasswordSecurity`)**:
  - 基于 BCrypt 强哈希算法提供密码散列与比对.
- **CORS 跨域白名单机制 (`CorsConfig`)**:
  - 支持动态配置跨域允许域名，杜绝 `*` 通配符带来的安全漏洞.
- **安全白名单注解 (`@Anonymous`, `@PermitAll`)**:
  - 便捷声明无需 Token 拦截的公开接口.

---

### 6. 🧰 企业级高频工具库

- **`EasyExcelUtils`**: 基于阿里 EasyExcel 封装的通用单行/批量导入导出工具类.
- **`IpUtils` & `UserAgentUtils`**: 穿透 Nginx 反向代理获取真实客户端 IP，智能解析浏览器类型与操作系统.
- **`RepeatSubmit` & `RepeatSubmitAspect`**: 基于 Redis / 内存缓存的接口防重复提交与幂等防护切面.
- **`JasyptConfig`**: 支持 `application.yml` 中使用 `ENC(...)` 加密数据库密码等核心配置.
- **`OpenApiConfig`**: 统一标准化 SpringDoc / Swagger 3 接口文档分组与 Token 调试授权.

---

## 🚀 快速接入指南 (3步 极简集成)

### 第 1 步: 引入 Maven 依赖

在业务子工程（如 `dwg-tool` 或 `jinhua`）的 `pom.xml` 中引入:

```xml
<dependency>
    <groupId>com.greatmap.agy</groupId>
    <artifactId>agy-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

### 第 2 步: 配置 `application.yml` (推荐)

```yaml
spring:
  application:
    name: dwg-tool # 项目名称 (日志将自动隔离落入 /data/dwg-tool/log)

logging:
  file:
    path: /data/${spring.application.name}/log
```

---

### 第 3 步: 配置 `logback-spring.xml`

在业务工程的 `src/main/resources/logback-spring.xml` 中配置三文件异步分流，关键参数如下:

```xml
<springProperty scope="context" name="APP_NAME" source="spring.application.name" defaultValue="app-service"/>
<springProperty scope="context" name="LOG_PATH" source="logging.file.path" defaultValue="/data/${APP_NAME}/log"/>

<!-- 控制台内嵌 TraceId 全链路高亮 -->
<property name="CONSOLE_LOG_PATTERN"
          value="%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr([%15.15t]){faint} %clr([%X{traceId}]){cyan} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>
```

---

## 🧪 自动化测试与质量保证

本依赖包已通过全量自动化单元测试验证:
- `TraceIdFilterTest`: 验证 MDC 注入与响应头回传
- `PasswordSecurityTest`: 验证 BCrypt 哈希加密与比对
- `RsaUtilsTest`: 验证 RSA 非对称加解密
- `CorsConfigTest`: 验证 CORS 跨域白名单机制
- `ResultTest`: 验证标准 RESTful 返回体格式化与链路绑定

```bash
# 运行单元测试
mvn clean test
```
