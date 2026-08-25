package com.greatmap.agy.common.enums;

/**
 * 业务操作类型
 */
public enum BusinessType {

    /**
     * 其它
     */
    OTHER(0),

    /**
     * 新增
     */
    INSERT(1),

    /**
     * 修改
     */
    UPDATE(2),

    /**
     * 删除
     */
    DELETE(3),

    /**
     * 授权
     */
    GRANT(4),

    /**
     * 导出
     */
    EXPORT(5),

    /**
     * 清空
     */
    CLEAN(6);

    private final int value;

    BusinessType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
