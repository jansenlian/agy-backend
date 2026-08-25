package com.greatmap.agy.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局 Web 请求出入参及耗时监控切面 (支持慢接口智能告警)
 */
@Slf4j
@Aspect
@Component
public class WebLogAspect {

    /**
     * 慢接口告警阈值 (毫秒)
     */
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000L;

    @Pointcut("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *) || execution(* com.greatmap..*.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String uri = request != null ? request.getRequestURI() : "Unknown";
        String httpMethod = request != null ? request.getMethod() : "Unknown";
        String remoteAddr = getClientIp(request);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        List<String> paramSummary = summarizeArgs(joinPoint.getArgs());

        log.info("▶ 请求接入: [{} {}] 来自 IP: {}, 方法: {}.{}(), 参数: {}",
                httpMethod, uri, remoteAddr, className, methodName, paramSummary);

        try {
            Object result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            if (costTime > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("🐢 慢请求告警: [{} {}] 处理耗时过长, 耗时: {}ms (阈值: {}ms)",
                        httpMethod, uri, costTime, SLOW_REQUEST_THRESHOLD_MS);
            } else {
                log.info("◀ 请求响应: [{} {}] 处理完成, 耗时: {}ms", httpMethod, uri, costTime);
            }
            return result;
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("✖ 请求异常: [{} {}] 处理失败, 耗时: {}ms, 异常: {}", httpMethod, uri, costTime, e.getMessage());
            throw e;
        }
    }

    private List<String> summarizeArgs(Object[] args) {
        List<String> summary = new ArrayList<>();
        if (args == null) {
            return summary;
        }
        for (Object arg : args) {
            if (arg instanceof MultipartFile file) {
                summary.add("MultipartFile[name=" + file.getOriginalFilename() + ", size=" + (file.getSize() / 1024) + "KB]");
            } else if (arg != null && !(arg instanceof jakarta.servlet.ServletRequest) && !(arg instanceof jakarta.servlet.ServletResponse)) {
                summary.add(String.valueOf(arg));
            }
        }
        return summary;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
