package com.greatmap.agy.hr.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工 360° 全景档案实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee_profile")
public class EmployeeProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工工号
     */
    private String empId;

    /**
     * 员工姓名 (主键)
     */
    @TableId
    private String empName;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 岗位/职务
     */
    private String position;

    /**
     * 入职时间
     */
    private String entryDate;

    /**
     * 毕业时间
     */
    private String graduationDate;

    /**
     * 最高学历
     */
    private String education;

    /**
     * 毕业院校
     */
    private String school;

    /**
     * 所学专业
     */
    private String major;

    /**
     * 专业技术职称
     */
    private String professionalTitle;

    /**
     * 所在产品组
     */
    private String productGroup;

    /**
     * 参与负责项目列表
     */
    private String projects;

    /**
     * 现住址
     */
    private String residence;

    /**
     * 电脑补贴状态
     */
    private String computerSubsidy;

    /**
     * 月度底薪 (真值/合同底薪)
     */
    private java.math.BigDecimal monthSalaryRate;

    /**
     * 日成本/日底薪
     */
    private java.math.BigDecimal dayRate;
}
