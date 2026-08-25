package com.greatmap.agy.agent.config.properties;

import com.greatmap.agy.agent.memory.MemoryStoreType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AGY 智能体通用全局配置属性
 * 绑定 application.yml 中的 agy.agent.* 前缀
 * 内置 JVM, DISK, REDIS, PGSQL 四大存储介质的完整连接配置与开箱即用默认值
 */
@Data
@ConfigurationProperties(prefix = "agy.agent")
public class AgyAgentProperties {

    /**
     * 是否开启智能体自动装配
     */
    private boolean enabled = true;

    /**
     * 大模型 API 网关基础地址 (默认 DeepSeek 官方地址)
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 大模型 API 密钥 (优先从环境变量读取)
     */
    private String apiKey = "";

    /**
     * 调用的模型规格名称 (如 deepseek-chat, deepseek-coder, gpt-4o)
     */
    private String modelName = "deepseek-chat";

    /**
     * 模型回答随机度与创造力 (0.0 最严谨, 1.0 最发散)
     */
    private Double temperature = 0.7;

    /**
     * 单次请求最长超时时间
     */
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * 每个会话滑动窗口保留的最大历史消息数量
     */
    private Integer maxMessages = 10;

    /**
     * 会话记忆存储类型:
     * - JVM: 本地堆内存 (极速响应，重启清空)
     * - DISK: 本地磁盘 JSON 文件存储 (单机持久化)
     * - REDIS: Redis 分布式缓存 (高并发生产推荐)
     * - PGSQL: PostgreSQL 数据库表存储 (永久归档与审计)
     */
    private MemoryStoreType memoryStoreType = MemoryStoreType.REDIS;

    /**
     * 1. 本地磁盘存储配置 (DISK 模式)
     */
    private String diskMemoryDir = "/data/agy/agent-memory";

    /**
     * 2. Redis 存储专属配置 (REDIS 模式，开箱即用默认值)
     */
    private RedisConfig redis = new RedisConfig();

    /**
     * 3. PGSQL 数据库专属存储配置 (PGSQL 模式，开箱即用默认值)
     */
    private PgsqlConfig pgsql = new PgsqlConfig();

    /**
     * 是否打印发送给大模型的完整 HTTP 请求报文
     */
    private boolean logRequests = false;

    /**
     * 是否打印大模型返回的完整 HTTP 响应报文
     */
    private boolean logResponses = false;

    /**
     * 默认系统全局人设与系统提示词
     */
    private String defaultSystemPrompt = """
            你是一名由 AGY 智能架构驱动的企业级超级智能业务助理.
            你具备严谨的逻辑推理能力，并且有权调用业务系统注册的本地 Java 工具来查询数据与执行操作.
            请始终基于工具返回的真实数据进行回答，保持专业、严谨且亲切的语气.
            """;

    /**
     * Redis 独立连接配置
     */
    @Data
    public static class RedisConfig {
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private String prefix = "agy:agent:memory:";
        private Duration ttl = Duration.ofDays(7);
        private Duration timeout = Duration.ofSeconds(10);
    }

    /**
     * PGSQL 独立连接配置
     */
    @Data
    public static class PgsqlConfig {
        private String url = "jdbc:postgresql://localhost:5432/agy_agent";
        private String username = "postgres";
        private String password = "";
        private String tableName = "agy_agent_chat_memory";
    }
}
