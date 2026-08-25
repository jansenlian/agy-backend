package com.greatmap.agy.common.annotation;

import com.greatmap.agy.common.enums.BusinessType;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 模块标题 (如: 用户管理 / 角色权限管理 / 字典管理)
     */
    String title() default "";

    /**
     * 业务操作类型 (新增 / 修改 / 删除 / 授权 / 导出)
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求参数 (涉及密码等敏感接口建议显式设置为 false)
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应结果 (默认关闭，防止响应数据过大膨胀数据库或泄漏 PII 敏感信息)
     */
    boolean isSaveResponseData() default false;
}
