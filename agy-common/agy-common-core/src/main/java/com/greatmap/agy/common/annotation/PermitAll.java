package com.greatmap.agy.common.annotation;

import java.lang.annotation.*;

/**
 * 允许公开访问注解 (声明接口无需特定 RBAC 按钮权限即可访问)
 * 配合 FrameworkArchitectureTest 架构合规单测，杜绝无安全声明的裸接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermitAll {
}
