package com.greatmap.agy.aks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 预警信息表实体(JGPT_YJ)
 */
@Data
@TableName("JGPT_YJ")
public class Yj implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("AREA_CODE")
    private String areaCode;

    @TableField("AREA_NAME")
    private String areaName;

    @TableField("QXDM")
    private String qxdm;

    @TableField("QXMC")
    private String qxmc;

    /**
     * 预警类型(01:合同到期, 02:地块异常, 03:费用收缴异常, 04:人员异常, 05:非粮化预警)
     */
    @TableField("TYPE")
    private String type;

    /**
     * 预警细分类型
     */
    @TableField("LX")
    private String lx;

    /**
     * 预警标题
     */
    @TableField("BT")
    private String bt;

    /**
     * 预警详情描述
     */
    @TableField("XQ")
    private String xq;

    /**
     * 关联合同编号
     */
    @TableField("HTBH")
    private String htbh;

    /**
     * 关联地块编号
     */
    @TableField("DKBH")
    private String dkbh;

    /**
     * 关联合同ID
     */
    @TableField("HTID")
    private String htid;

    /**
     * 关联地块ID
     */
    @TableField("DKID")
    private String dkid;

    /**
     * 第几次生成预警
     */
    @TableField("NUM")
    private String num;

    /**
     * 创建时间
     */
    @TableField("GMT_CREATE")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @TableField("GMT_MODIFIED")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtModified;
}
