package com.greatmap.agy.agent.autoconfigure;

import com.greatmap.agy.agent.config.properties.AgyAgentProperties;
import com.greatmap.agy.agent.controller.AgyAgentStreamController;
import com.greatmap.agy.agent.core.UniversalAgent;
import com.greatmap.agy.agent.memory.FileChatMemoryStore;
import com.greatmap.agy.agent.memory.MemoryStoreType;
import com.greatmap.agy.agent.memory.PgsqlChatMemoryStore;
import com.greatmap.agy.agent.memory.RedisChatMemoryStore;
import com.greatmap.agy.agent.service.AgyAgentService;
import com.greatmap.agy.agent.service.impl.AgyAgentServiceImpl;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AGY 智能体全局自动装配核心配置类
 * 负责自动初始化模型连接池、扫描 Spring 容器内所有 @Tool 工具、挂载 JVM/DISK/REDIS/PGSQL 会话记忆与注入通用 Controller
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(AgyAgentProperties.class)
@ConditionalOnProperty(prefix = "agy.agent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgyAgentAutoConfiguration {

    /**
     * 1. 自动初始化大模型同步通信连接池 (默认 DeepSeek)
     */
    @Bean
    @ConditionalOnMissingBean(ChatLanguageModel.class)
    public ChatLanguageModel chatLanguageModel(AgyAgentProperties properties) {
        log.info("【AGY Agent 自动装配】初始化大模型连接池 -> 网关: {}, 模型: {}",
                properties.getBaseUrl(), properties.getModelName());

        return OpenAiChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModelName())
                .temperature(properties.getTemperature())
                .timeout(properties.getTimeout())
                .logRequests(properties.isLogRequests())
                .logResponses(properties.isLogResponses())
                .build();
    }

    /**
     * 2. 自动初始化大模型流式通信连接池 (用于 SSE 打字机吐字)
     */
    @Bean
    @ConditionalOnMissingBean(dev.langchain4j.model.chat.StreamingChatLanguageModel.class)
    public dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatLanguageModel(AgyAgentProperties properties) {
        log.info("【AGY Agent 自动装配】初始化大模型流式连接池 -> 网关: {}, 模型: {}",
                properties.getBaseUrl(), properties.getModelName());

        return dev.langchain4j.model.openai.OpenAiStreamingChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModelName())
                .temperature(properties.getTemperature())
                .timeout(properties.getTimeout())
                .logRequests(properties.isLogRequests())
                .logResponses(properties.isLogResponses())
                .build();
    }

    /**
     * 3. 自动初始化多介质会话记忆提供者 (支持 JVM, DISK, REDIS, PGSQL 真实连通性探测与保底降级)
     */
    @Bean
    @ConditionalOnMissingBean(ChatMemoryProvider.class)
    public ChatMemoryProvider chatMemoryProvider(
            AgyAgentProperties properties,
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
            ObjectProvider<DataSource> dataSourceProvider) {

        MemoryStoreType storeType = properties.getMemoryStoreType() != null
                ? properties.getMemoryStoreType()
                : MemoryStoreType.REDIS;

        ChatMemoryStore memoryStore = null;

        switch (storeType) {
            case REDIS -> {
                StringRedisTemplate redisTemplate = null;
                var redisProps = properties.getRedis();

                // 1. 若配置了专属 AI 记忆 Redis 主机，则优先构建独立专属连接池并主动探测连通性
                if (redisProps != null && redisProps.getHost() != null && !redisProps.getHost().isBlank()) {
                    try {
                        var redisConfig = new RedisStandaloneConfiguration(redisProps.getHost(), redisProps.getPort());
                        if (redisProps.getPassword() != null && !redisProps.getPassword().isBlank()) {
                            redisConfig.setPassword(RedisPassword.of(redisProps.getPassword()));
                        }
                        redisConfig.setDatabase(redisProps.getDatabase());
                        var factory = new LettuceConnectionFactory(redisConfig);
                        factory.afterPropertiesSet();
                        var customTemplate = new StringRedisTemplate(factory);
                        // 主动 Ping 探测连通性，防止懒连接假成功
                        customTemplate.execute((org.springframework.data.redis.connection.RedisConnection conn) -> conn.ping());
                        redisTemplate = customTemplate;
                        log.info("【AGY Agent 记忆池】成功构建并验证专属 AI 记忆 Redis 客户端 (与业务 Redis 物理隔离) -> {}:{}, db: {}",
                                redisProps.getHost(), redisProps.getPort(), redisProps.getDatabase());
                    } catch (Exception e) {
                        log.warn("【AGY Agent 记忆池】专属 Redis 客户端连通探测失败: {}, 尝试回退复用容器业务 Redis", e.getMessage());
                    }
                }

                // 2. 若未配置专属 Redis 或专属 Redis 连通失败，则尝试复用 Spring 容器中的业务 Redis 实例并探测
                if (redisTemplate == null) {
                    StringRedisTemplate containerTemplate = stringRedisTemplateProvider.getIfAvailable();
                    if (containerTemplate != null) {
                        try {
                            containerTemplate.execute((org.springframework.data.redis.connection.RedisConnection conn) -> conn.ping());
                            redisTemplate = containerTemplate;
                        } catch (Exception e) {
                            log.warn("【AGY Agent 记忆池】容器业务 Redis 连通探测不可达: {}, 将安全降级为 [JVM] 内存存储", e.getMessage());
                        }
                    }
                }

                if (redisTemplate != null && redisProps != null) {
                    log.info("【AGY Agent 记忆池】启用 [REDIS] 分布式存储 -> 前缀: {}, TTL: {} 天, 窗口: {} 条",
                            redisProps.getPrefix(), redisProps.getTtl().toDays(), properties.getMaxMessages());
                    memoryStore = new RedisChatMemoryStore(
                            redisTemplate,
                            redisProps.getPrefix(),
                            redisProps.getTtl()
                    );
                } else {
                    log.warn("【AGY Agent 记忆池】无法连接有效 Redis，已自动安全降级为 [JVM] 本地内存存储");
                }
            }
            case DISK -> {
                log.info("【AGY Agent 记忆池】启用 [DISK] 本地磁盘文件存储 -> 存储目录: {}, 窗口: {} 条",
                        properties.getDiskMemoryDir(), properties.getMaxMessages());
                memoryStore = new FileChatMemoryStore(properties.getDiskMemoryDir());
            }
            case PGSQL -> {
                DataSource dataSource = dataSourceProvider.getIfAvailable();
                var pgsqlProps = properties.getPgsql();

                // 若 Spring 容器未提供 DataSource，则根据内置配置自主构建独立 JDBC 数据源并探测
                if (dataSource == null && pgsqlProps != null && pgsqlProps.getUrl() != null && !pgsqlProps.getUrl().isBlank()) {
                    try {
                        var ds = new DriverManagerDataSource();
                        ds.setUrl(pgsqlProps.getUrl());
                        ds.setUsername(pgsqlProps.getUsername());
                        ds.setPassword(pgsqlProps.getPassword());
                        ds.setDriverClassName("org.postgresql.Driver");
                        try (Connection conn = ds.getConnection()) {
                            if (conn != null && !conn.isClosed()) {
                                dataSource = ds;
                                log.info("【AGY Agent 记忆池】成功自主构建并验证独立 PGSQL 数据源 -> {}", pgsqlProps.getUrl());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("【AGY Agent 记忆池】自主创建 PGSQL 数据源连通探测失败: {}, 降级为 [JVM] 内存存储", e.getMessage());
                    }
                }

                if (dataSource != null && pgsqlProps != null) {
                    log.info("【AGY Agent 记忆池】启用 [PGSQL] 数据库表存储 -> 存储表: {}, 窗口: {} 条",
                            pgsqlProps.getTableName(), properties.getMaxMessages());
                    memoryStore = new PgsqlChatMemoryStore(dataSource, pgsqlProps.getTableName());
                } else {
                    log.warn("【AGY Agent 记忆池】无法获取可用 PGSQL 数据源，自动安全降级为 [JVM] 内存存储");
                }
            }
            case JVM -> log.info("【AGY Agent 记忆池】启用 [JVM] 本地堆内存存储 -> 窗口: {} 条 (重启后清空)",
                    properties.getMaxMessages());
        }

        final ChatMemoryStore finalMemoryStore = memoryStore;

        return memoryId -> {
            var builder = MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(properties.getMaxMessages());
            if (finalMemoryStore != null) {
                builder.chatMemoryStore(finalMemoryStore);
            }
            return builder.build();
        };
    }

    /**
     * 4. 自动扫描 Spring 容器内所有带有 @Tool 的业务组件（支持代理类解包、父类/接口方法递归扫描与异常告警）
     */
    @Bean
    @ConditionalOnMissingBean(UniversalAgent.class)
    public UniversalAgent universalAgent(
            ChatLanguageModel chatLanguageModel,
            dev.langchain4j.model.chat.StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemoryProvider chatMemoryProvider,
            ListableBeanFactory beanFactory) {

        List<Object> toolBeans = new ArrayList<>();
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Class<?> rawType = beanFactory.getType(beanName);
            if (rawType == null) {
                continue;
            }
            String className = rawType.getName();
            if (className.startsWith("org.spring") || className.startsWith("com.fasterxml") || className.startsWith("java.")) {
                continue;
            }
            try {
                // 解包 CGLIB / JDK 动态代理，获取真实业务目标类
                Class<?> userClass = ClassUtils.getUserClass(rawType);
                AtomicBoolean hasTool = new AtomicBoolean(false);

                // 递归扫描本类及父类、接口的所有方法，不错漏继承链中的 @Tool 工具
                ReflectionUtils.doWithMethods(userClass, method -> {
                    if (AnnotationUtils.findAnnotation(method, Tool.class) != null) {
                        hasTool.set(true);
                    }
                });

                if (hasTool.get()) {
                    Object bean = beanFactory.getBean(beanName);
                    toolBeans.add(bean);
                    log.info("【AGY Agent 自动装配】发现业务工具组件 -> [{} (bean: {})]",
                            userClass.getSimpleName(), beanName);
                }
            } catch (Exception e) {
                log.warn("【AGY Agent 自动装配】扫描 Bean [{}] 的 @Tool 工具注解发生异常: {}", beanName, e.getMessage());
            }
        }

        var builder = AiServices.builder(UniversalAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider);

        if (!toolBeans.isEmpty()) {
            builder.tools(toolBeans);
            log.info("【AGY Agent 自动装配】成功挂载 {} 个业务工具类到智能体大脑！", toolBeans.size());
        }

        return builder.build();
    }

    /**
     * 5. 自动注册智能体门面服务 Bean
     */
    @Bean
    @ConditionalOnMissingBean(AgyAgentService.class)
    public AgyAgentService agyAgentService(UniversalAgent universalAgent, ChatMemoryProvider chatMemoryProvider) {
        return new AgyAgentServiceImpl(universalAgent, chatMemoryProvider);
    }

    /**
     * 6. 自动注册开箱即用的通用 SSE 流式 Controller 接口
     */
    @Bean
    @ConditionalOnMissingBean(AgyAgentStreamController.class)
    public AgyAgentStreamController agyAgentStreamController(AgyAgentService agyAgentService) {
        return new AgyAgentStreamController(agyAgentService);
    }
}
