package com.greatmap.agy.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraceIdFilter 日志链路追踪过滤器单元测试")
public class TraceIdFilterTest {

    private final TraceIdFilter traceIdFilter = new TraceIdFilter();

    @Test
    @DisplayName("请求未携带 X-Trace-Id 时应自动生成 TraceId 并注入 MDC 与响应头")
    public void testAutoGenerateTraceId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                // 验证在执行链内部能够正确获取 MDC 中的 traceId
                String currentTraceId = MDC.get(TraceIdFilter.TRACE_ID);
                assertNotNull(currentTraceId, "执行链内部 MDC 中应包含 traceId");
                assertFalse(currentTraceId.trim().isEmpty(), "MDC traceId 不应为空");
            }
        };

        traceIdFilter.doFilter(request, response, filterChain);

        // 验证响应头中包含 X-Trace-Id
        String responseTraceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertNotNull(responseTraceId, "响应头中应包含 X-Trace-Id");
        assertFalse(responseTraceId.trim().isEmpty());

        // 验证请求结束后 MDC 已清理，防止线程复用污染
        assertNull(MDC.get(TraceIdFilter.TRACE_ID), "请求执行完毕后 MDC traceId 应被彻底清理");
    }

    @Test
    @DisplayName("请求携带 X-Trace-Id 时应继承并透传该 TraceId 实现跨服务全链路闭环")
    public void testInheritClientTraceId() throws Exception {
        String clientTraceId = "custom-trace-id-888888";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, clientTraceId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                assertEquals(clientTraceId, MDC.get(TraceIdFilter.TRACE_ID), "MDC 中应继承客户端传入的 traceId");
            }
        };

        traceIdFilter.doFilter(request, response, filterChain);

        assertEquals(clientTraceId, response.getHeader(TraceIdFilter.TRACE_ID_HEADER), "响应头中应透传相同的 traceId");
        assertNull(MDC.get(TraceIdFilter.TRACE_ID), "请求执行完毕后 MDC traceId 应被清理");
    }
}
