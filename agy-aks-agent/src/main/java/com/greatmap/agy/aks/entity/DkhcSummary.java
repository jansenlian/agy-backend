package com.greatmap.agy.aks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 地块现状核查概要实体(JGPT_DKHC)
 * 精确对齐PostgreSQL数据表字段定义
 */
@Data
@TableName("JGPT_DKHC")
public class DkhcSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 核查地块编号
     */
    @TableField("HCDKBH")
    private String hcdkbh;

    /**
     * 地块编号
     */
    @TableField("DKBH")
    private String dkbh;

    /**
     * 合同编号
     */
    @TableField("HTBH")
    private String htbh;

    @TableField("AREA_CODE")
    private String areaCode;

    @TableField("AREA_NAME")
    private String areaName;

    @TableField("QXDM")
    private String qxdm;

    @TableField("QXMC")
    private String qxmc;

    /**
     * 核查地块面积(亩)
     */
    @TableField("HTMJ_M")
    private BigDecimal dkmj;

    /**
     * 土地坐落
     */
    @TableField("TDZL")
    private String dkmc;

    /**
     * 土地权属
     */
    @TableField("TDQS")
    private String tdqs;

    /**
     * 土地地类
     */
    @TableField("TDDL")
    private String tddl;

    /**
     * 种植类型
     */
    @TableField("ZZLX")
    private String zzlx;

    /**
     * 种植作物
     */
    @TableField("ZZZW")
    private String zzzw;

    /**
     * 占用人姓名/责任人
     */
    @TableField("ZYRXM")
    private String hcnrzrr;

    /**
     * 承包人名称
     */
    @TableField("CBRMC")
    private String hcnrcbr;

    /**
     * 耕作方式
     */
    @TableField("GZFS")
    private String gzfs;

    /**
     * 删除标记('0':正常, '1':删除)
     */
    @TableField("DEL_FLAG")
    private String delFlag;
}
