package com.greatmap.agy.aks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同信息概要实体(JGPT_HTXX)
 * 精确对齐PostgreSQL数据表字段定义
 */
@Data
@TableName("JGPT_HTXX")
public class HtxxSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("HTBH")
    private String htbh;

    @TableField("DKBH")
    private String dkbh;

    @TableField("AREA_CODE")
    private String areaCode;

    @TableField("AREA_NAME")
    private String areaName;

    @TableField("QXDM")
    private String qxdm;

    @TableField("QXMC")
    private String qxmc;

    /**
     * 发包方/出租方名称
     */
    @TableField("CZFFBMC")
    private String czffbmc;

    /**
     * 承包方姓名(个人)
     */
    @TableField("CBFXM")
    private String cbfxm;

    /**
     * 承包方企业名称
     */
    @TableField("CBFQYMC")
    private String cbfqymc;

    /**
     * 承包方身份证号
     */
    @TableField("CBFSFZH")
    private String cbfsfzh;

    /**
     * 承包方组织机构代码
     */
    @TableField("CBFZZJGDM")
    private String cbfzzjgdm;

    /**
     * 承包方联系电话
     */
    @TableField("CBFLXDH")
    private String cbrlxdh;

    /**
     * 合同面积(亩)
     */
    @TableField("HTMJ_M")
    private BigDecimal htcbzmj;

    /**
     * 承包费年均价-货币(元/亩)
     */
    @TableField("CBFJJHB")
    private BigDecimal ndzjdj;

    /**
     * 应缴承包费总额-货币(元)
     */
    @TableField("CBFYJZEHB")
    private BigDecimal zjze;

    /**
     * 合同签订日期
     */
    @TableField("HTQDSJ")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date htqdrq;

    /**
     * 合同有效期开始时间
     */
    @TableField("HTYXQKSSJ")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date htyxqqssj;

    /**
     * 合同有效期结束时间
     */
    @TableField("HTYXQJSSJ")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date htyxqjssj;

    /**
     * 土地用途
     */
    @TableField("TDYT")
    private String tdyt;

    /**
     * 合同状态
     */
    @TableField("HTZT")
    private String status;

    /**
     * 整改状态: 0-未整改 1-已整改
     */
    @TableField("ZG_STATUS")
    private Integer zgStatus;

    /**
     * 删除标记('0':正常, '1':删除)
     */
    @TableField("DEL_FLAG")
    private String delFlag;
}
