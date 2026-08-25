package com.greatmap.agy.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * AGY 智能体基于本地磁盘文件的持久化会话记忆存储器
 * 将每个会话的消息持久化为本地磁盘 JSON 文件，适用于无外部中间件的单机长期持久化场景
 */
@Slf4j
public class FileChatMemoryStore implements ChatMemoryStore {

    private final Path baseDir;

    public FileChatMemoryStore(String dirPath) {
        this.baseDir = Paths.get(dirPath != null && !dirPath.isBlank() ? dirPath : "./data/agent-memory");
        try {
            if (!Files.exists(this.baseDir)) {
                Files.createDirectories(this.baseDir);
                log.info("【本地磁盘记忆池】创建会话存储目录: {}", this.baseDir.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("创建本地磁盘会话存储目录失败: {}", e.getMessage(), e);
        }
    }

    private Path resolveSessionFile(Object memoryId) {
        String safeFileName = memoryId.toString().replaceAll("[^a-zA-Z0-9_.-]", "_") + ".json";
        return baseDir.resolve(safeFileName);
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Path filePath = resolveSessionFile(memoryId);
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(filePath, StandardCharsets.UTF_8);
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.error("从本地磁盘读取会话记忆失败 [path: {}]: {}", filePath, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Path filePath = resolveSessionFile(memoryId);
        try {
            if (messages == null || messages.isEmpty()) {
                Files.deleteIfExists(filePath);
                return;
            }
            String json = ChatMessageSerializer.messagesToJson(messages);
            Files.writeString(filePath, json, StandardCharsets.UTF_8);
            log.debug("成功持久化会话记忆至本地磁盘 [path: {}, 消息数: {}]", filePath, messages.size());
        } catch (Exception e) {
            log.error("持久化会话记忆至本地磁盘失败 [path: {}]: {}", filePath, e.getMessage(), e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Path filePath = resolveSessionFile(memoryId);
        try {
            Files.deleteIfExists(filePath);
            log.info("成功清理本地磁盘会话记忆 [path: {}]", filePath);
        } catch (Exception e) {
            log.error("清理本地磁盘会话记忆失败 [path: {}]: {}", filePath, e.getMessage(), e);
        }
    }
}
