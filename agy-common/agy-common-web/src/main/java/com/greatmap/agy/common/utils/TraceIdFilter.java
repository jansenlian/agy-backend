package com.greatmap.agy.common.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logback MDC 日志链路追踪 Filter
 * 统一基于 TraceUtils 维护 TraceId 生命周期
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID = TraceUtils.TRACE_ID;
    public static final String TRACE_ID_HEADER = TraceUtils.TRACE_ID_HEADER;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = null;
        if (request instanceof jakarta.servlet.http.HttpServletRequest httpRequest) {
            traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        }
        TraceUtils.setTraceId(traceId);
        if (response instanceof HttpServletResponse httpServletResponse) {
            httpServletResponse.setHeader(TRACE_ID_HEADER, TraceUtils.getTraceId());
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TraceUtils.clearTraceId();
        }
    }
}
