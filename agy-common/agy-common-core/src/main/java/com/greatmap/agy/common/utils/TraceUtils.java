package com.greatmap.agy.common.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * Logback MDC 全链路追踪统一工具类
 * 适用于 HTTP 请求、异步线程池、定时任务（@Scheduled）及 MQ 消费等全场景
 */
public final class TraceUtils {

    public static final String TRACE_ID = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private TraceUtils() {
    }

    /**
     * 获取当前线程上下文的 TraceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 获取当前线程上下文的 TraceId，若不存在则自动生成并绑定至 MDC
     */
    public static String getOrGenerateTraceId() {
        String traceId = getTraceId();
        if (StrUtil.isBlank(traceId)) {
            traceId = IdUtil.simpleUUID();
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }

    /**
     * 显式设置当前线程的 TraceId
     */
    public static void setTraceId(String traceId) {
        if (StrUtil.isNotBlank(traceId)) {
            MDC.put(TRACE_ID, traceId);
        } else {
            MDC.put(TRACE_ID, IdUtil.simpleUUID());
        }
    }

    /**
     * 清理当前线程的 TraceId
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID);
    }

    /**
     * 以自动生成的 TraceId 执行 Runnable
     */
    public static void runWithTrace(Runnable runnable) {
        runWithTrace(null, runnable);
    }

    /**
     * 以指定或自动生成的 TraceId 执行 Runnable，执行完毕自动清理 MDC
     */
    public static void runWithTrace(String traceId, Runnable runnable) {
        setTraceId(traceId);
        try {
            runnable.run();
        } finally {
            clearTraceId();
        }
    }

    /**
     * 以自动生成的 TraceId 执行 Supplier 计算
     */
    public static <T> T supplyWithTrace(Supplier<T> supplier) {
        return supplyWithTrace(null, supplier);
    }

    /**
     * 以指定或自动生成的 TraceId 执行 Supplier 计算，执行完毕自动清理 MDC
     */
    public static <T> T supplyWithTrace(String traceId, Supplier<T> supplier) {
        setTraceId(traceId);
        try {
            return supplier.get();
        } finally {
            clearTraceId();
        }
    }
}
