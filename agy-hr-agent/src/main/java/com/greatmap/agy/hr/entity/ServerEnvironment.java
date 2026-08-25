package com.greatmap.agy.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 研发测试环境与服务器资产实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("server_environment")
public class ServerEnvironment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联项目名称
     */
    private String projectName;

    /**
     * 操作系统
     */
    private String os;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 登录账号
     */
    private String account;

    /**
     * 登录密码
     */
    private String password;

    /**
     * Swagger / 服务访问地址
     */
    private String swaggerUrl;

    /**
     * 磁盘信息
     */
    private String diskInfo;
}
