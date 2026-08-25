package com.greatmap.agy.common.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.greatmap.agy.common.annotation.Log;
import com.greatmap.agy.common.domain.SysOperLogDTO;
import com.greatmap.agy.common.event.SysOperLogEvent;
import com.greatmap.agy.common.utils.IpUtils;
import com.greatmap.agy.common.utils.SecurityUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 操作日志切面 LogAspect (支持敏感字段自动脱敏与大响应体隔离)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(?i)\"(password|oldPassword|newPassword|confirmPassword|token|secret|clientSecret|accessToken|refreshToken|mobile|phone|phoneNumber|idCard|idNumber)\"\\s*:\\s*(\"[^\"]*\"|[^,}\\]\\s]+)"
    );

    private final ApplicationEventPublisher eventPublisher;

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            SysOperLogDTO operLog = new SysOperLogDTO();
            operLog.setStatus(1);
            operLog.setOperTime(LocalDateTime.now());

            if (e != null) {
                operLog.setStatus(0);
                String msg = e.getMessage();
                if (msg != null && msg.length() > 500) {
                    msg = msg.substring(0, 500);
                }
                operLog.setErrorMsg(msg);
            }

            if (request != null) {
                operLog.setOperIp(IpUtils.getClientIp(request));
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperName(SecurityUtils.getLoginUserName());
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            if (controllerLog != null) {
                operLog.setTitle(controllerLog.title());
                operLog.setBusinessType(controllerLog.businessType().getValue());

                // 1. 请求参数脱敏与落库
                if (controllerLog.isSaveRequestData() && joinPoint.getArgs() != null) {
                    try {
                        Object[] filterArgs = filterArgs(joinPoint.getArgs());
                        if (ArrayUtil.isNotEmpty(filterArgs)) {
                            String paramJson = JSONUtil.toJsonStr(filterArgs);
                            paramJson = maskSensitiveData(paramJson);
                            if (paramJson.length() > 2000) {
                                paramJson = paramJson.substring(0, 2000);
                            }
                            operLog.setOperParam(paramJson);
                        }
                    } catch (Exception ignored) {
                    }
                }

                // 2. 响应数据脱敏与落库 (默认关闭)
                if (controllerLog.isSaveResponseData() && jsonResult != null) {
                    try {
                        String resultJson = JSONUtil.toJsonStr(jsonResult);
                        resultJson = maskSensitiveData(resultJson);
                        if (resultJson.length() > 2000) {
                            resultJson = resultJson.substring(0, 2000);
                        }
                        operLog.setJsonResult(resultJson);
                    } catch (Exception ignored) {
                    }
                }
            }

            eventPublisher.publishEvent(new SysOperLogEvent(operLog));
        } catch (Exception exp) {
            log.error("操作日志切面处理失败: {}", exp.getMessage(), exp);
        }
    }

    /**
     * 过滤掉无法或无需序列化的特殊参数 (如 HttpRequest / HttpResponse / 文件对象)
     */
    private Object[] filterArgs(Object[] args) {
        if (args == null) {
            return new Object[0];
        }
        List<Object> validArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof ServletRequest
                    || arg instanceof ServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]
                    || arg instanceof BindingResult) {
                continue;
            }
            validArgs.add(arg);
        }
        return validArgs.toArray();
    }

    /**
     * 对 JSON 字符串中的密码、Token、手机号等 PII 敏感字段统一脱敏为 ******
     */
    public static String maskSensitiveData(String json) {
        if (StrUtil.isBlank(json)) {
            return json;
        }
        return SENSITIVE_KEY_PATTERN.matcher(json).replaceAll("\"$1\":\"******\"");
    }
}
