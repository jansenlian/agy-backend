package com.greatmap.agy.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.greatmap.agy.common.exception.BusinessException;
import com.greatmap.agy.common.exception.ErrorCode;
import com.greatmap.agy.common.utils.IpUtils;
import com.greatmap.agy.common.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面处理
 * 1. 登录用户以 userId 隔离，未登录/匿名用户自动回退真实客户端 IP 隔离，彻底防止 null 锁共享冲突
 * 2. 优先支持毫秒级 interval 精度控制（向下兼容秒级 lockTime）
 * 3. 尊重注解上定义的自定义提示文案 message
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        Long userId = SecurityUtils.getLoginUserId();
        String identifier;
        if (userId != null) {
            identifier = "user:" + userId;
        } else {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            identifier = "ip:" + IpUtils.getClientIp(request);
        }

        String methodName = joinPoint.getSignature().toShortString();
        String lockKey = "repeat_submit:" + identifier + ":" + methodName;

        // 计算锁过期时间 (优先读取毫秒 interval，若未特别设置或设置了 lockTime 则按对应值处理)
        long lockMillis = repeatSubmit.interval() > 0 ? repeatSubmit.interval() : repeatSubmit.lockTime() * 1000L;

        Boolean success = false;
        try {
            if (redisTemplate != null) {
                success = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", lockMillis, TimeUnit.MILLISECONDS);
            } else {
                success = true;
            }
        } catch (Exception ignored) {
            success = true; // 如果 Redis 故障防重熔断放行，避免阻塞业务
        }

        if (Boolean.FALSE.equals(success)) {
            String customMessage = repeatSubmit.message();
            String errorMsg = StrUtil.isNotBlank(customMessage) ? customMessage : ErrorCode.REPEAT_SUBMIT.getMessage();
            throw new BusinessException(ErrorCode.REPEAT_SUBMIT.getCode(), errorMsg);
        }

        return joinPoint.proceed();
    }
}
