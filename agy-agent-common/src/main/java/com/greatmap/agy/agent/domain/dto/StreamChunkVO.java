package com.greatmap.agy.agent.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SSE 打字机流式逐字推送数据包
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChunkVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前增量输出的字符 Token
     */
    private String delta;

    /**
     * 是否全部生成完毕
     */
    private boolean finished;

    /**
     * 当前被触发执行的本地 Java 工具名称 (如果有的话，方便前端展示"正在查询数据库...")
     */
    private String toolName;

    /**
     * 错误信息 (如果中途发生异常)
     */
    private String error;

    public static StreamChunkVO delta(String delta) {
        return StreamChunkVO.builder().delta(delta).finished(false).build();
    }

    public static StreamChunkVO toolCall(String toolName) {
        return StreamChunkVO.builder().toolName(toolName).finished(false).build();
    }

    public static StreamChunkVO finish() {
        return StreamChunkVO.builder().finished(true).build();
    }

    public static StreamChunkVO fail(String error) {
        return StreamChunkVO.builder().error(error).finished(true).build();
    }
}
