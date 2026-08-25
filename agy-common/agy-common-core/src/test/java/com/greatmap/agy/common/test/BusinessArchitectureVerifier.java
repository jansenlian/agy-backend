package com.greatmap.agy.common.test;

/**
 * 业务微服务架构边界与隔离性合规校验器
 * 统一收敛跨仓共享的架构断言逻辑，防止业务工程误引入 UPMS 等平台专有组件
 */
public final class BusinessArchitectureVerifier {

    private static final String[] UPMS_INTERNAL_CLASSES = {
            "com.greatmap.agy.web.controller.SysUserController",
            "com.greatmap.agy.service.service.impl.SysUserServiceImpl",
            "com.greatmap.agy.web.controller.SysRoleController",
            "com.greatmap.agy.web.controller.SysMenuController"
    };

    private BusinessArchitectureVerifier() {
    }

    /**
     * 验证当前运行环境中绝无 UPMS 重复代码 (全量剥离交由 agy-platform 唯一权威托管)
     */
    public static void verifyNoUpmsDuplicateCode() {
        for (String className : UPMS_INTERNAL_CLASSES) {
            try {
                Class.forName(className);
                throw new AssertionError(String.format(
                        "业务微服务架构违规: 检测到 UPMS 内部类 [%s]，业务微服务严禁包含 UPMS 重复代码，必须由 agy-platform 唯一权威托管！",
                        className
                ));
            } catch (ClassNotFoundException ignored) {
                // 预期行为：类必须不存在
            }
        }
    }
}
