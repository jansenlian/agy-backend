package com.greatmap.agy.common.annotation;

import java.lang.annotation.*;

/**
 * 匿名公开访问注解 (声明接口无需登录与鉴权，任何人均可公开访问)
 * 配合 FrameworkArchitectureTest 架构合规单测，杜绝无安全声明的裸接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous {
}
