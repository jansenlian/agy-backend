package com.greatmap.agy.aks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预警规则与明细配置表实体(JGPT_YJ_MX)
 */
@Data
@TableName("JGPT_YJ_MX")
public class YjMx implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 监测指标项(01:合同到期, 02:地块面积偏差, 03:收缴租金逾期, 04:人员违规)
     */
    @TableField("JCZBX")
    private String jczbx;

    /**
     * 预警类型名称
     */
    @TableField("YJLX")
    private String yjlx;

    /**
     * 触发条件数值(如合同提前预警天数,面积偏差百分比等)
     */
    @TableField("CFTJ")
    private BigDecimal cftj;

    /**
     * 触发规则描述
     */
    @TableField("CFGZ")
    private String cfgz;

    /**
     * 状态(0:停用, 1:启用)
     */
    @TableField("STATUS")
    private Integer status;
}
