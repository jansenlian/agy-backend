package com.greatmap.agy.agent.memory;

/**
 * 智能体会话记忆存储类型枚举
 */
public enum MemoryStoreType {
    /**
     * JVM 本地堆内存 (开箱即用，微秒级零延迟，重启后重置)
     */
    JVM,

    /**
     * 本地磁盘 JSON 文件存储 (单机跨重启持久化，零外部中间件依赖)
     */
    DISK,

    /**
     * Redis 分布式缓存 (高并发毫秒级，支持 TTL 自动过期淘汰与多节点共享)
     */
    REDIS,

    /**
     * PostgreSQL 数据库表存储 (结构化永久归档，支持数据长期留存与审计)
     */
    PGSQL
}
