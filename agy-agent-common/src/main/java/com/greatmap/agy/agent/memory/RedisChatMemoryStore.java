package com.greatmap.agy.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AGY 智能体基于 Redis 的持久化会话记忆存储器
 * 支持多会话消息 JSON 序列化、TTL 自动淘汰与跨服务节点会话共享
 */
@Slf4j
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;
    private final String prefix;
    private final Duration ttl;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = prefix + memoryId;
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.error("从 Redis 读取会话记忆失败 [key: {}]: {}", key, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = prefix + memoryId;
        try {
            if (messages == null || messages.isEmpty()) {
                redisTemplate.delete(key);
                return;
            }
            String json = ChatMessageSerializer.messagesToJson(messages);
            if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
                redisTemplate.opsForValue().set(key, json, ttl);
            } else {
                redisTemplate.opsForValue().set(key, json);
            }
            log.debug("成功持久化会话记忆至 Redis [key: {}, 消息数: {}]", key, messages.size());
        } catch (Exception e) {
            log.error("持久化会话记忆至 Redis 失败 [key: {}]: {}", key, e.getMessage(), e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = prefix + memoryId;
        try {
            redisTemplate.delete(key);
            log.info("成功清理 Redis 会话记忆 [key: {}]", key);
        } catch (Exception e) {
            log.error("清理 Redis 会话记忆失败 [key: {}]: {}", key, e.getMessage(), e);
        }
    }
}
