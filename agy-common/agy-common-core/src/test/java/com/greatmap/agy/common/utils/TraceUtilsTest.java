package com.greatmap.agy.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraceUtils 全链路追踪工具类单元测试")
class TraceUtilsTest {

    @Test
    @DisplayName("测试 runWithTrace 自动生成与清理 TraceId")
    void testRunWithTrace() {
        assertNull(TraceUtils.getTraceId());

        TraceUtils.runWithTrace(() -> {
            String insideTraceId = TraceUtils.getTraceId();
            assertNotNull(insideTraceId);
            assertFalse(insideTraceId.isEmpty());
            assertEquals(insideTraceId, MDC.get(TraceUtils.TRACE_ID));
        });

        assertNull(TraceUtils.getTraceId());
    }

    @Test
    @DisplayName("测试 runWithTrace 指定特定 TraceId")
    void testRunWithTraceSpecified() {
        String customTraceId = "CUSTOM-TRACE-ID-8888";

        TraceUtils.runWithTrace(customTraceId, () -> {
            assertEquals(customTraceId, TraceUtils.getTraceId());
        });

        assertNull(TraceUtils.getTraceId());
    }

    @Test
    @DisplayName("测试 supplyWithTrace 返回值与 TraceId 闭环")
    void testSupplyWithTrace() {
        String result = TraceUtils.supplyWithTrace("SPECIFIC-TRACE", () -> {
            assertEquals("SPECIFIC-TRACE", TraceUtils.getTraceId());
            return "SUCCESS_DATA";
        });

        assertEquals("SUCCESS_DATA", result);
        assertNull(TraceUtils.getTraceId());
    }

    @Test
    @DisplayName("测试 getOrGenerateTraceId 幂等性")
    void testGetOrGenerateTraceId() {
        assertNull(TraceUtils.getTraceId());

        String generated = TraceUtils.getOrGenerateTraceId();
        assertNotNull(generated);
        assertEquals(generated, TraceUtils.getTraceId());

        String secondCall = TraceUtils.getOrGenerateTraceId();
        assertEquals(generated, secondCall);

        TraceUtils.clearTraceId();
        assertNull(TraceUtils.getTraceId());
    }
}
