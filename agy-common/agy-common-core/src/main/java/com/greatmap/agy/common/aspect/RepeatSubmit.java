package com.greatmap.agy.common.aspect;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {
    int lockTime() default 5;
    int interval() default 5000;
    String message() default "请勿重复提交请求";
}
