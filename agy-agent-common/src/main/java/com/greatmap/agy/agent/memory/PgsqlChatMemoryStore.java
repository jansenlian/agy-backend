package com.greatmap.agy.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * AGY 智能体基于 PostgreSQL 数据库表的持久化会话记忆存储器
 * 自动建表、支持多会话 JSON 字段存储与数据库层长期历史留存
 */
@Slf4j
public class PgsqlChatMemoryStore implements ChatMemoryStore {

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;

    public PgsqlChatMemoryStore(DataSource dataSource, String tableName) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tableName = (tableName != null && !tableName.isBlank()) ? tableName : "agy_agent_chat_memory";
        initTableIfNotExists();
    }

    private void initTableIfNotExists() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "memory_id VARCHAR(128) PRIMARY KEY, " +
                    "messages_json TEXT NOT NULL, " +
                    "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            jdbcTemplate.execute(sql);
            log.info("【PGSQL 数据库记忆池】检查并初始化会话记忆表: [{}] 成功", tableName);
        } catch (Exception e) {
            log.warn("初始化 PGSQL 会话记忆表提示: {}", e.getMessage());
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sql = "SELECT messages_json FROM " + tableName + " WHERE memory_id = ?";
        try {
            List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("messages_json"), memoryId.toString());
            if (results.isEmpty() || results.get(0) == null || results.get(0).isBlank()) {
                return new ArrayList<>();
            }
            return ChatMessageDeserializer.messagesFromJson(results.get(0));
        } catch (Exception e) {
            log.error("从 PGSQL 读取会话记忆失败 [memoryId: {}]: {}", memoryId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            deleteMessages(memoryId);
            return;
        }
        String json = ChatMessageSerializer.messagesToJson(messages);
        String sql = "INSERT INTO " + tableName + " (memory_id, messages_json, update_time) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (memory_id) DO UPDATE SET " +
                "messages_json = EXCLUDED.messages_json, " +
                "update_time = CURRENT_TIMESTAMP";
        try {
            jdbcTemplate.update(sql, memoryId.toString(), json);
            log.debug("成功持久化会话记忆至 PGSQL [memoryId: {}, 消息数: {}]", memoryId, messages.size());
        } catch (Exception e) {
            log.error("持久化会话记忆至 PGSQL 失败 [memoryId: {}]: {}", memoryId, e.getMessage(), e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sql = "DELETE FROM " + tableName + " WHERE memory_id = ?";
        try {
            jdbcTemplate.update(sql, memoryId.toString());
            log.info("成功清理 PGSQL 会话记忆 [memoryId: {}]", memoryId);
        } catch (Exception e) {
            log.error("清理 PGSQL 会话记忆失败 [memoryId: {}]: {}", memoryId, e.getMessage(), e);
        }
    }
}
