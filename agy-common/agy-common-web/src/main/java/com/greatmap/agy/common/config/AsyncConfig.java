package com.greatmap.agy.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务与线程池配置类 (用于日志异步落库、IP 异步解析等后台异步任务)
 * 拒绝策略采用 DiscardOldestPolicy / 告警抛弃，严禁使用 CallerRunsPolicy 阻塞主请求线程！
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("agy-async-");
        // 🔴 核心加固：队列满时作废最早未处理任务，绝对绝不退回主请求线程同步执行 (防外部网络延迟拖垮主线程池)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        // 🟢 全链路追踪：自动复制主线程 MDC TraceId 到子线程
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}
