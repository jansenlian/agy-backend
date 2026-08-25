package com.greatmap.agy.api.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.greatmap.agy.common.desensitize.DataMask;
import com.greatmap.agy.common.desensitize.DataMaskType;
import com.greatmap.agy.common.dict.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图 VO
 */
@Data
@Schema(description = "系统用户基础视图信息")
public class SysUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (Long 转 String 序列化防精度丢失)
     */
    @Schema(description = "用户主键 ID (String 类型防 JS 精度丢失)", example = "1954312890123456789")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登录账号
     */
    @Schema(description = "登录用户名/账号", example = "admin")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", example = "超级管理员")
    private String realName;

    /**
     * 手机号 (自动脱敏 138****1234)
     */
    @Schema(description = "手机号码 (返回时自动脱敏)", example = "138****8888")
    @DataMask(DataMaskType.PHONE)
    private String mobile;

    /**
     * 账号状态 (1-正常 0-禁用)
     */
    @Schema(description = "账号状态 (1-正常 0-禁用)", example = "1")
    @Dict(dictCode = "user_status")
    private Integer status;

    /**
     * 已绑定的系统角色名称列表
     */
    @Schema(description = "已绑定的系统角色名称列表")
    private List<String> roleNames;

    /**
     * 创建时间
     */
    @Schema(description = "账号创建时间", example = "2026-08-14 10:00:00")
    private LocalDateTime createTime;
}
