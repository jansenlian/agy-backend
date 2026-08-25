package com.greatmap.agy.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 员工月度薪酬与实际成本明细实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee_salary_history")
public class EmployeeSalaryHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    private String empId;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 薪酬所属月份 (如 2025-07)
     */
    private String salaryMonth;

    /**
     * 实际成本 (除以 1.155)
     */
    private BigDecimal actualCost;
}
