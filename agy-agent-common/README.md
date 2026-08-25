# agy-agent-common (AGY 智能体通用底层核心依赖包)

基于 **Java 25 + Spring Boot 4.1.0 + LangChain4j + DeepSeek** 打造的企业级 AI Agent 通用自动装配底座 (Starter / Common).

---

## 🌟 核心特性与架构能力

1. **自动装配 (AutoConfiguration)**: 零代码接入，引入依赖即可自动装配 DeepSeek 模型连接池、记忆管理器与 Tool 调度器.
2. **Spring Bean 自动发现**: 自动扫描并装载整个 Spring 容器内所有带有 `@Tool` 注解的业务 Service，无需手动配置.
3. **多用户会话记忆隔离 (Chat Memory)**: 内置高可用滑动窗口记忆与 Redis 分布式会话拓展支持.
4. **开箱即用 SSE 流式打字机**: 内置标准 `/api/agent/chat/stream` 接口，原生支持前端 Web/桌面端逐字打字机推流.
5. **高度可扩展**: 遵循 Spring 官方标准，所有核心组件均支持 `@ConditionalOnMissingBean` 自定义覆盖.

---

## 📦 目录结构

```text
agy-agent-common/
├── pom.xml                                    # Spring Boot 4.1.0 + Java 25 依赖配置
├── README.md
└── src/main/
    ├── java/com/greatmap/agy/agent/
    │   ├── AgentApplication.java             # 本地测试启动入口
    │   ├── autoconfigure/
    │   │   └── AgyAgentAutoConfiguration.java # 核心自动装配中心
    │   ├── config/properties/
    │   │   └── AgyAgentProperties.java        # yml 属性绑定 (agy.agent.*)
    │   ├── controller/
    │   │   └── AgyAgentStreamController.java  # 通用 RESTful & SSE 流式控制层
    │   ├── core/
    │   │   └── UniversalAgent.java            # 声明式智能体顶层接口
    │   ├── domain/
    │   │   ├── Result.java                    # 统一响应结构体
    │   │   └── dto/
    │   │       ├── AgentChatRequest.java      # 入参 DTO
    │   │       └── StreamChunkVO.java         # SSE 增量数据包
    │   ├── service/
    │   │   ├── AgyAgentService.java           # 智能体门面接口
    │   │   └── impl/AgyAgentServiceImpl.java  # 智能体门面实现
    │   └── tools/
    │       └── EcommerceTools.java            # @Component 示例业务工具
    └── resources/
        ├── application.yml                    # 默认配置文件
        └── META-INF/spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## 🚀 业务模块使用指南

### 1. 引入 Maven 依赖
```xml
<dependency>
    <groupId>com.greatmap.agy</groupId>
    <artifactId>agy-agent-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置 `application.yml`
```yaml
agy:
  agent:
    enabled: true
    base-url: https://api.deepseek.com
    api-key: ${DEEPSEEK_API_KEY:sk-xxx}
    model-name: deepseek-chat
```

### 3. 随手在自己的业务 Service 上加 `@Tool`
```java
@Service
public class OrderServiceImpl implements OrderService {

    @Tool("根据订单号查询订单当前状态")
    public String queryStatus(String orderNo) {
        return "订单 " + orderNo + " 已出库";
    }
}
```
无需任何额外配置，项目启动后该业务方法即可被大模型自动感知与调用！
