package com.greatmap.agy.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.greatmap.agy.common.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 数据库公共字段自动填充处理器
 * 自动填充 BaseEntity 中的 createBy, createTime, updateBy, updateTime
 */
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long loginUserId = SecurityUtils.getLoginUserId();
        LocalDateTime now = LocalDateTime.now();

        // 审计字段填充 (仅当存在有效登录用户上下文时才填充 createBy / updateBy)
        if (loginUserId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, loginUserId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, loginUserId);
            this.setFieldValByName("createBy", loginUserId, metaObject);
            this.setFieldValByName("updateBy", loginUserId, metaObject);
        }

        // 时间字段填充 (无条件填充当前准确时刻)
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.setFieldValByName("createTime", now, metaObject);
        this.setFieldValByName("updateTime", now, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long loginUserId = SecurityUtils.getLoginUserId();
        LocalDateTime now = LocalDateTime.now();

        if (loginUserId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, loginUserId);
            this.setFieldValByName("updateBy", loginUserId, metaObject);
        }

        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.setFieldValByName("updateTime", now, metaObject);
    }
}
